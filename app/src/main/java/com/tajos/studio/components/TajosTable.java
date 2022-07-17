package com.tajos.studio.components;

import com.tajos.studio.action.FormulasState;
import com.tajos.studio.action.StateManager;
import com.tajos.studio.action.TableCellsState;
import com.tajos.studio.action.TableShiftState;
import com.tajos.studio.table.TableCellData;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.interfaces.TableSelectionListener;
import com.tajos.studio.interfaces.TextStyles;
import com.tajos.studio.table.CellClipboard;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.data.FilteredData;
import com.tajos.studio.table.Formulator;
import com.tajos.studio.table.TableDefaultCellRenderer;
import com.tajos.studio.table.TableCellEditor;
import com.tajos.studio.table.TableCellListener;
import com.tajos.studio.table.TableColumnHeader;
import com.tajos.studio.table.TableModel;
import com.tajos.studio.table.TableUI;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TajosTable extends JTable implements KeyBinds {
    private static TajosTable instance;
    public static TajosTable instance() {
        return instance;
    }
    private List<JMenuItem> mMenuItems = new ArrayList<>();
    private TableCellEditor mTableCellEditor;
    private final DefaultTableCellRenderer unEditableCellRenderer = new DefaultTableCellRenderer();
    
    private boolean initialized = false;
    
    private boolean isSelectAllCells = false;
    private List<List<CellClipboard>> mCellClipboardLst = new ArrayList<>();
    private TableSelectionListener mColSelectionListener;
    private int mColumns;
    private boolean mEnabled = true;
    private boolean isDragging = false;
    private int[] mInitialColWidth;
    private int[] mInitialRowHeight;
    private TajosMenuTextField mInvoker;
    private int[] mMaxOccupiedCells;
    //                           index 0   index 1
    // [row] [col] [rectangles [odd rect][orig rect]]
    private Map<Integer, Map<Integer, Rectangle>> mOddCells = new HashMap<>();
    private JPopupMenu mPopupMenu;
    private List<Component> mRibbonGrp;
    private TableSelectionListener mRowSelectionListener;
    
    private int mRows;
    private int mSelectedRowPHolder = -1, mSelectedColPholder = -1;
    private HeaderType mSelectedHeader;
    
    private final TableDefaultCellRenderer mRenderer;
    
    private TablePopupMenuManager mTablePopupMenuManager;
    private StateManager mTableStateManager = new StateManager();
    private StateManager mFormulasStateManager = new StateManager();
    private final MouseInputAdapter mouseListener = new MouseInputAdapter() {
        List<Integer> selectedRowsList = new ArrayList<>();
        List<Integer> selectedColumnsList = new ArrayList<>();
        
        @Override
        public void mouseEntered(MouseEvent e) {
            if (!mEnabled)
                return;
            
            GradeUtils.setPrecisionCursor(TajosTable.this);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!mEnabled)
                return;
            
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!mEnabled)
                return;
            
            isSelectAllCells = false;
            
            if (isEditing()) {
                disableRibbon(true);
            } else {
                disableRibbon(false);
            }
            
            int row = rowAtPoint(e.getPoint());
            int col = columnAtPoint(e.getPoint());
            mSelectedRowPHolder = getSelectedRow(); mSelectedColPholder = getSelectedColumn();
            mRows = getRowCount(); mColumns = getColumnCount();
            
            if (!tablePressedListeners.isEmpty()) {
                for (OnTablePressedListener listener : tablePressedListeners) {
                    listener.onPressed(row, col);
                }
            }
            
            if (SwingUtilities.isLeftMouseButton(e) && !isEditing()) {
                _triggerCellSelection(row, col);
            }
            
            // region: show popup menu when right clicked
            if (SwingUtilities.isRightMouseButton(e)) {
                Point p = e.getPoint();
                int _row = rowAtPoint(p);
                int _col = columnAtPoint(p);
                // region: disable the menuitems if the user click in row 0
                // and col 0 or col 1. Otherwise, enable it.
                if ((_row == 0 && _col == 0) ||
                    (_row == 0 && _col == 1) ||
                    isOnFilteredMode())
                {
                    for (JMenuItem item : mMenuItems) {
                        item.setEnabled(false);
                    }
                } else {
                    for (JMenuItem item : mMenuItems) {
                        item.setEnabled(true);
                    }
                }
                // region end
                for (int selectedRow : getSelectedRows()) {
                    for (int selectedCol : getSelectedColumns()) {
                        if (selectedCol == _col && selectedRow == _row) {
                            requestFocusInWindow();
                            mTablePopupMenuManager = new TablePopupMenuManager(TajosTable.this, getSelectedRows(), getSelectedColumns());
                            mPopupMenu.show(TajosTable.this, e.getX(), e.getY());
                        }
                    }
                }
            }
            // region end
            revalidate();
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!mEnabled)
                return;
            
            int row = rowAtPoint(e.getPoint());
            int col = columnAtPoint(e.getPoint());
            // region: performs cell selection
            if (!selectedRowsList.contains(row))
                selectedRowsList.add(row);
            
            if (!selectedColumnsList.contains(col))
                selectedColumnsList.add(col);
            
            if (row < selectedRowsList.get(selectedRowsList.size()-1) ||
               (row > selectedRowsList.get(selectedRowsList.size()-1) &&
                row <= selectedRowsList.get(0)))
            {
                selectedRowsList.remove(selectedRowsList.size()-1);
            }
            
            if (col < selectedColumnsList.get(selectedColumnsList.size()-1) ||
               (col > selectedColumnsList.get(selectedColumnsList.size()-1) &&
                col <= selectedColumnsList.get(0)))
            {
                selectedColumnsList.remove(selectedColumnsList.size()-1);
            }
            // region end
            int[] rows = selectedRowsList.stream().mapToInt(i->i).toArray();
            int[] cols = selectedColumnsList.stream().mapToInt(i->i).toArray();
            
            if (!tablePressedListeners.isEmpty()) {
                for (OnTablePressedListener listener : tablePressedListeners) {
                    listener.onDragging(rows, cols);
                }
            }
            
            isDragging = true;
            _triggerCellSelection(-1, -1);
            
            revalidate();
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            selectedRowsList.clear();
            selectedColumnsList.clear();
            isDragging = false;
            if (!tablePressedListeners.isEmpty()) {
                for (OnTablePressedListener listener : tablePressedListeners) {
                    listener.onReleased();
                }
            }
        }
    };
    private OnSaveKeyListener saveKeyListener;
    /**
     * Saves the state every time the user starts editing the cell.
     * For undo and redo later.
     */
    private final TableCellListener.OnEditingCellListener tableOnEditingCellListener = () -> {
        // on editing cell
        _saveState("", -1, null, null);
    };
    
    private List<OnTablePressedListener> tablePressedListeners = new ArrayList<>();
    private OnTextStyleKeysListener txStyleKeysListener;
    private int updaterCount = 3;
    List<Color> ribbonColorHolder = new ArrayList<>();
    
    public TajosTable() {
        super();
        mRenderer = new TableDefaultCellRenderer(this);
        mTableCellEditor = new TableCellEditor(this);
        
        setUI(new TableUI());
        setTableHeader(null);
        setColumnSelectionAllowed(true);
        setDefaultRenderer(Object.class, mRenderer);
        setDefaultEditor(Object.class, mTableCellEditor);
        addPropertyChangeListener(new TableCellListener(this,tableOnEditingCellListener));
        
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addKeyListener(this);
        
        instance = this;
    }
    
    private boolean isOnFilteredMode() {
        TableModel model = (TableModel) getModel();
        return model.isOnFilteredMode();
    }
    
    public boolean isDragging() {
        return isDragging;
    }

    public int _getSelectedRow() {
        return mSelectedRowPHolder;
    }

    public int _getSelectedColumn() {
        return mSelectedColPholder;
    }
    
    public void clearSaveStates() {
        mTableStateManager = new StateManager();
        mFormulasStateManager = new StateManager();
    }
    
    public void addColumn() {
        TableModel model = (TableModel) getModel();
        model.addColumn();
    }
    public void addColumnSelectionListener(TableSelectionListener lstener) {
        mColSelectionListener = lstener;
    }
    
    public void addRow() {
        TableModel model = (TableModel) getModel();
        model.addRow();
    }
    public void addRowSelectionListener(TableSelectionListener lstener) {
        mRowSelectionListener = lstener;
    }
    
    public boolean isIDExist(String id) {
        final TableModel model = (TableModel) getModel();
        return model.isIDExist(id);
    }
    
    public boolean isNameExist(String name) {
        final TableModel model = (TableModel) getModel();
        return model.isNameExist(name);
    }
    
    public void clearInitialColWidth() {
        mInitialColWidth = null;
    }
    public void clearInitialRowHeight() {
        mInitialRowHeight = null;
    }
    public void disableRibbon(boolean disable) {
        int index = 0;
        boolean isFromEnabling = false;
        for (Component c : mRibbonGrp) {
            if (c instanceof ImageViewer img) {
                if (disable) {
                    ribbonColorHolder.add(img.getColorImage());
                    img.setColorImage(Color.GRAY);
                } else {
                    if (!ribbonColorHolder.isEmpty())
                        img.setColorImage(ribbonColorHolder.get(index));
                    
                    isFromEnabling = true;
                }
            }

            if (c instanceof RoundedPanel panel) {
                if (disable) {
                    ribbonColorHolder.add(panel.getBackground());
                    panel.setBackground(Color.GRAY);
                } else {
                    if (!ribbonColorHolder.isEmpty())
                        panel.setBackground(ribbonColorHolder.get(index));
                    
                    isFromEnabling = true;
                }
            }
            index++;
        }
        if (!ribbonColorHolder.isEmpty() && isFromEnabling)
            ribbonColorHolder.clear();
    }
    
    public void disableTable() {
        mEnabled = false;
        super.disable();
    }
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (!getFilterData().isEmpty()) {
            if (row != 0) {
                if (row > getFilterData().size()) //||
//                    column > getFilterData().get(row-1).size()-1)
                {
                    return false;
                }
            }
        }
        
        if (e instanceof KeyEvent && 
            (CTRL_Z((KeyEvent)e) || CTRL_Y((KeyEvent)e) ||
            CTRL_S((KeyEvent)e) || DELETE((KeyEvent)e))) 
        {
            return false;
        }
        
        if ((row == 0 && column == 0) ||
            row == 0 && column == 1)
            return false;
        
        boolean result = super.editCellAt(row, column, e);
        final Component editor = getEditorComponent();
        if (editor == null || !(editor instanceof JTextComponent)) {
            return result;
        }
        if (e instanceof MouseEvent) {
            EventQueue.invokeLater(() -> {
                ((JTextComponent) editor).selectAll();
            });
        } else if (e instanceof KeyEvent) {
            ((JTextComponent) editor).selectAll();
        }
        return result;
    }
    
    public void enableTable() {
        mEnabled = true;
        super.enable();
    }
    
    public List<List<Object>> getTableData() {
        TableModel model = (TableModel) this.getModel();
        return model.getData();
    }
    
    public void setFilteredData(List<FilteredData> filterData) {
        TableModel model = (TableModel) getModel();
        model.setFilteredData(filterData);
    }
    
    public List<FilteredData> getFilterData() {
        if (!(getModel() instanceof TableModel))
            return new ArrayList<>();
        
        TableModel model = (TableModel) getModel();
        return model.getFilteredData();
    }
    
    public Map<Integer, Map<Integer, String>> getFormulas() {
        TableModel model = (TableModel) getModel();
        return model.getFormulas();
    }
    
    public void fireCellDataChanged() {
        _saveState("", -1, null, null);
    }
    
    public TableDefaultCellRenderer getRenderer() {
        return mRenderer;
    }
    
    @Override
    public javax.swing.table.TableCellEditor getCellEditor(int row, int column) {
        TableCellEditor editor = (TableCellEditor) super.getCellEditor(row, column);
        
        TableDefaultCellRenderer renderer = (TableDefaultCellRenderer) getCellRenderer(row, column);
        Component c = renderer.getTableCellRendererComponent(this, renderer.getText(), true, true, row, column);
        editor.getEditorRenderer().setBackground(c.getBackground());
        
        boolean _null = TableDefaultCellRenderer.getCellTextStyle().get(row) == null;
        // if there's any cell text styles
        if (!_null) {
            CellTextStyle style = TableDefaultCellRenderer.getCellTextStyle().get(row).get(column);
            if (style != null) {
                editor.removeAttributes();
                Color fontColor = style.getFontColor() != null ? style.getFontColor() : GradeUtils.Colors.darkBlueColor;
                editor.getEditorRenderer().setForeground(fontColor);
                _initEditorStyles(style, editor);
                
                return editor;
            }
        }
        
        editor.removeAttributes();
        return editor;
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if (row == 0 && column == 0) {
            TableDefaultCellRenderer renderer = (TableDefaultCellRenderer) super.getCellRenderer(row, column);
            unEditableCellRenderer.setText("School ID");
            unEditableCellRenderer.setFont(GradeUtils.getDefaultFont(12));
            unEditableCellRenderer.setToolTipText("""
                                                  (non-editable) Use to identify students when they search thier grades.\t
                                                  This is where the server looks to search student grades""");
            unEditableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            unEditableCellRenderer.setForeground(renderer.getForeground());
            unEditableCellRenderer.setBackground(GradeUtils.Colors.semiCreamWhiteBlueColor);
            return unEditableCellRenderer;
        } else if (row == 0 && column == 1) {
            TableDefaultCellRenderer renderer = (TableDefaultCellRenderer) super.getCellRenderer(row, column);
            unEditableCellRenderer.setText("Name");
            unEditableCellRenderer.setFont(GradeUtils.getDefaultFont(12));
            unEditableCellRenderer.setToolTipText("""
                                                  (non-editable)""");
            unEditableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            unEditableCellRenderer.setForeground(renderer.getForeground());
            unEditableCellRenderer.setBackground(GradeUtils.Colors.semiCreamWhiteBlueColor);
            return unEditableCellRenderer;
        }
        return super.getCellRenderer(row, column);
    }
    
    public int [] getInitialColWidth(int col) {
        if (mInitialColWidth == null && col != -1) {
            mInitialColWidth = new int[] {col, getColumnModel().getColumn(col).getWidth()};
            return mInitialColWidth;
        }
        
        if (mInitialColWidth == null && col == -1)
            return null;
        
        return mInitialColWidth;
    }
    
    /**
     * Get the initial row height for resizing later
     * @param row - {@code -1} if you don't want to find a specific row height
     * @return
     */
    public int[] getInitialRowHeight(int row) {
        if (mInitialRowHeight == null && row != -1) {
            mInitialRowHeight = new int[] { row, getRowHeight(row) };
            return mInitialRowHeight;
        }
        
        if (mInitialRowHeight == null && row == -1)
            return null;
        
        return mInitialRowHeight;
    }
    
    public TajosMenuTextField getInvoker() {
        return mInvoker;
    }
    
    public void setInvoker(TajosMenuTextField invoker) {
        mInvoker = invoker;
    }

    public int [] getMaxOccupiedCells() {
        if (mMaxOccupiedCells == null)
            return new int[] {0,0};
        
        return mMaxOccupiedCells;
    }
    
    public void setMaxOccupiedCells(int [] occupiedCell) {
        mMaxOccupiedCells = occupiedCell;
    }
    
    public Map<Integer, Map<Integer, Rectangle>> getOddCells() {
        return mOddCells;
    }
    
    public void setOddCells(Map<Integer, Map<Integer, Rectangle>> oddCells) {
        if (oddCells == null) {
            mOddCells = new HashMap<>();
            return;
        }
        
        mOddCells = oddCells;
    }
    
    public JPopupMenu getPopupMenu() {
        return mPopupMenu;
    }
    
    public void setPopupMenu(JPopupMenu popup) {
        mPopupMenu = popup;
        
        List<Component> menuItems = Arrays.asList(popup.getComponents());
        
        for (Component c : menuItems) {
            if (c instanceof JMenuItem menuItem) {
                mMenuItems.add(menuItem);
                menuItem.addActionListener((ActionEvent e) -> {
                    switch (menuItem.getText()) {
                        case "Copy" -> {
                            _copy();
                        }
                        case "Cut" -> {
                            _cut();
                        }
                        case "Shift Cell Down" -> {
                            mTablePopupMenuManager.shiftCellDown();
                        }
                        case "Shift Entire Row Down" -> {
                            mTablePopupMenuManager.shiftEntireRowDown();
                        }
                        case "Shift Cell Right" -> {
                            mTablePopupMenuManager.shiftCellRight();
                        }
                        case "Shift Entire Column Right" -> {
                            mTablePopupMenuManager.shiftEntireColumnRight();
                        }
                    }
                });
            }
            
            if (c instanceof JMenu pasteOptionsItem) {
                Component[] pasteOptions = pasteOptionsItem.getMenuComponents();
                
                for (Component c2 : pasteOptions) {
                    JMenuItem item = (JMenuItem) c2;
                    
                    item.addActionListener((ActionEvent e) -> {
                        switch (item.getText()) {
                            case "Cell Content Only" -> {
                                _paste(false);
                            }
                            case "Formulas" -> {
                                _paste(true);
                            }
                            case "Delete Cell Content Only" -> {
                                _deleteContent();
                            }
                            case "Shift Cell Left" -> {
                                mTablePopupMenuManager.shiftCellLeft();
                            }
                            case "Shift Cell Up" -> {
                                mTablePopupMenuManager.shiftCellUp();
                            }
                            case "Shift Column Left" -> {
                                mTablePopupMenuManager.shiftEntireColumnLeft();
                            }
                            case "Shift Row Up" -> {
                                mTablePopupMenuManager.shiftEntireRowUp();
                            }
                        }
                    });
                }
            }
        }
    }
    
    public int getPreciseRowCount() {
        return getModel().getRowCount();
    }
    
    public int getPreferredColumnCount() {
        TableModel model = (TableModel) getModel();
        return model.getPreferredColCount();
    }
    
    public void setPreferredColumnCount(int count) {
        TableModel model = (TableModel) getModel();
        model.setPreferredColCount(count);
    }
    
    public int getPreferredRowCount() {
        TableModel model = (TableModel) getModel();
        return model.getPreferredRowCount();
    }
    
    public void setPreferredRowCount(int count) {
        TableModel model = (TableModel) getModel();
        model.setPreferredRowCount(count);
    }
    
    public OnSaveKeyListener getSaveKeyListener() {
        return saveKeyListener;
    }
    
    public void setSaveKeyListener(OnSaveKeyListener saveKeyListener) {
        this.saveKeyListener = saveKeyListener;
    }
    
    public HeaderType getSelectedHeader() {
        return mSelectedHeader;
    }
    
    public void setSelectedHeader(HeaderType type) {
        mSelectedHeader = type;
    }
    
    public void setTableData(List<List<Object>> data) {
        TableModel model = (TableModel) getModel();
        model.setData(data);
    }
    
    public void addTablePressedListener(OnTablePressedListener listener) {
        if (tablePressedListeners.contains(listener))
            return;
        
        tablePressedListeners.add(listener);
    }
    
    public OnTextStyleKeysListener getTxStyleKeysListener() {
        return txStyleKeysListener;
    }
    
    public void setTxStyleKeysListener(OnTextStyleKeysListener txStyleKeysListener) {
        this.txStyleKeysListener = txStyleKeysListener;
    }
    
    public boolean isDisable() {
        return !mEnabled;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean bool) {
        initialized = bool;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        
        if (!isNotPrintable(e)) { // if it's printable
            mInvoker.setIsSavedState(false);
        }
        // undo
        if (CTRL_Z(e)) {
            if (_isOnFilterMode())
                return;
            
            _undo();
        }
        // redo
        if (CTRL_Y(e)) {
            if (_isOnFilterMode())
                return;
            
            _redo();
        }
        
        // delete cell content
        if (DELETE(e)) {
            if (_isOnFilterMode())
                return;
            
            _deleteContent();
            return;
        }
        
        if (CTRL_C(e)) {
            if (_isOnFilterMode())
                return;
            
            _copy();
        }
        
        if (CTRL_X(e)) {
            if (_isOnFilterMode())
                return;
            
            _cut();
        }
        
        if (CTRL_V(e)) {
            if (_isOnFilterMode())
                return;
            
            _paste(false);
        }
        
        if (CTRL_S(e)) {
            if (_isOnFilterMode())
                return;
            
            saveKeyListener.onSaveKeyPressed();
        }
        
        if (CTRL_B(e)) {
            if (_isOnFilterMode())
                return;
            
            txStyleKeysListener.onBoldKeyPressed();
        }
        
        if (CTRL_I(e)) {
            if (_isOnFilterMode())
                return;
            
            txStyleKeysListener.onItalicKeyPressed();
        }
        
        if (CTRL_U(e)) {
            if (_isOnFilterMode())
                return;
            
            txStyleKeysListener.onUnderlineKeyPressed();
        }
        
        // region: table cell navigation using keys
        _cellNavigation(e);
        // region end
        revalidate();
        repaint();
    }
    
    private void _cellNavigation(KeyEvent e) {
        if (ESCAPE(e)) {
            mTableCellEditor.resetEditor();
        }
        
        if (TAB(e)) {
            if (_cellHasError())
                return;
            
            if (mSelectedColPholder != mColumns-1) {
                if (!SHIFT_DOWN(e)) {
                    ++mSelectedColPholder;
                    mColumns = getColumnCount();
                } else {
                    if (mSelectedColPholder == 0) {
                        mSelectedColPholder = mColumns-1;
                        --mSelectedRowPHolder;
                    } else
                        --mSelectedColPholder;
                }
            } else {
                if (!SHIFT_DOWN(e)) {
                    mSelectedColPholder = 0;
                    ++mSelectedRowPHolder;
                } else {
                    --mSelectedColPholder;
                }
            }
            
            _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }
        
        if (!ALT_DOWN(e) && ENTER(e)) {
            
            if (_cellHasError())
                return;
            
            if (mSelectedRowPHolder == mRows-2) 
            {
                addRow();
                mRows = getRowCount();
            }
            
            if (SHIFT_DOWN(e)) {
                if (mSelectedRowPHolder != 0)
                    --mSelectedRowPHolder;
                else if (mSelectedColPholder != 0) {
                    mSelectedRowPHolder = getRowCount()-1;
                    --mSelectedColPholder;
                } else {
                    mSelectedColPholder = getColumnCount()-1;
                    mSelectedRowPHolder = getRowCount()-1;
                }
                _removeLastRow();
                mRows = getRowCount();
            } else {
                ++mSelectedRowPHolder;
            }
            
            _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }

        if (!ALT_DOWN(e) && ARROW_LEFT(e)) {
            if (_cellHasError())
                return;
            
            if (mSelectedColPholder != 0)
                --mSelectedColPholder;
            
            if (!CTRL_DOWN(e))
                _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }
        
        if (!ALT_DOWN(e) && ARROW_RIGHT(e)) {
            if (_cellHasError())
                return;
            
            if (mSelectedColPholder != mColumns-1)
                ++mSelectedColPholder;
            
            if (!CTRL_DOWN(e))
                _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }
        
        if (!ALT_DOWN(e)&& ARROW_DOWN(e)) {
            if (_cellHasError())
                return;
            
            if (mSelectedRowPHolder == mRows-2)
                addRow();
            
            if (mSelectedRowPHolder == mRows-1)
                return;
            
            mRows = getRowCount();
            ++mSelectedRowPHolder;
            
            if (!CTRL_DOWN(e))
                _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }
        
        if (!ALT_DOWN(e) && ARROW_UP(e)) {
            if (_cellHasError())
                return;
            
            if (mSelectedRowPHolder != 0)
                --mSelectedRowPHolder;
            
            _removeLastRow();
            mRows = getRowCount();
            
            if (!CTRL_DOWN(e))
                _triggerCellSelection(mSelectedRowPHolder, mSelectedColPholder);
        }
        
        if (CTRL_SHIFT_DOWN(e) && ARROW_DOWN(e)) {
            setRowSelectionInterval(getSelectedRow(), getMaxOccupiedCells()[0]-2);
        }
    }
    
    private boolean _cellHasError() {
        final Object[] errResult = mTableCellEditor.hasErrors();
            
        if (errResult != null)
            return (boolean)errResult[0];
        
        return false;
    }
    
    private boolean _isOnFilterMode() {
        if (!getFilterData().isEmpty()) {
            JOptionPane.showMessageDialog(new JFrame(), 
                "Filter is enabled, disable it first");

            return true;
        }
        
        return false;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public void linkRibbonComponents(List<Component> ribbonGrp) {
        mRibbonGrp = ribbonGrp;
    }
    
    public void removeLastColumn() {
        TableModel model = (TableModel) getModel();
        model.removeColumnContent(model.getColumnCount()-1);
    }
    
    public void removeLastRow() {
        TableModel model = (TableModel) getModel();
        model.removeRowContent(model.getRowCount()-1);
    }
    public void _saveState(String TAG, int max, int[] _rows, int [] _cols) {
        int[] rows = _rows == null ? getSelectedRows() : _rows;
        int[] cols = _cols == null ? getSelectedColumns() : _cols;

        List<List<TableCellData>> data = new ArrayList<>();
        TableModel model = (TableModel) getModel();
        for (int row : rows) {
            List<TableCellData> colsData = new ArrayList<>();
            for (int col : cols) {
                Object val = model.getValueAt(row, col);
                Map<Integer, Map<Integer, CellTextStyle>> stylesMap = TableDefaultCellRenderer.getCellTextStyle();

                CellTextStyle style = null;
                if (stylesMap != null && (stylesMap.get(row) != null && stylesMap.get(row).get(col) != null)) {
                    CellTextStyle styleOrig = stylesMap.get(row).get(col);
                    //region: copy the styles
                    style = new CellTextStyle();
                    style.setFontColor(styleOrig.getFontColor());
                    style.setBackgroundColor(styleOrig.getBgColor());
                    style.setHorizontalAlignment(style.getHorizontalAlignment());
                    
                    List<TextStyles> origStyles = styleOrig.getStyles();
                    List<TextStyles> copyOrigStyles = new ArrayList<>();
                    for (TextStyles origStyle : origStyles) {
                        copyOrigStyles.add(origStyle);
                    }
                    style.setStyles(copyOrigStyles);
                    //region end
                    // region: copy the selected all
                    CellTextStyle.SelectedAll selectedAllOrig = styleOrig.getSelectedAllList();
                    if (selectedAllOrig != null) {
                        CellTextStyle.SelectedAll selectedAll = new CellTextStyle.SelectedAll();
                        selectedAll.addSelectedAllRow(row, selectedAllOrig.getColorInRow(row));
                        selectedAll.addSelectedAllCol(col, selectedAllOrig.getColorInCol(col));
                        style.putSelectedAllList(selectedAll);
                    }
                    //region end
                }
                
                TableCellData cellData = new TableCellData(val, style, row, col);
                colsData.add(cellData);
            }
            data.add(colsData);
        }
        
        if (null == TAG) {
            mTableStateManager.addNewState(new TableCellsState(this, data));
        } else switch (TAG) {
            case "SHIFT-COL" -> mTableStateManager.addNewState(new TableShiftState(TAG, this, data, max));
            case "SHIFT-ROW" -> mTableStateManager.addNewState(new TableShiftState(TAG, this, data, max));
            default -> mTableStateManager.addNewState(new TableCellsState(this, data));
        }
    }
    
    public void selectAllCells(boolean selectAll, boolean isFromMultipleSelection) {
        isSelectAllCells = isSelectAllCells == selectAll ? !selectAll : selectAll;
        
        if (isSelectAllCells) {
            setColumnSelectionInterval(0, getColumnCount()-1);
            setRowSelectionInterval(0, getRowCount()-1);
            
            mRowSelectionListener.selectAllRows(true);
            mColSelectionListener.selectAllColumns(true);
        } else {
            setColumnSelectionInterval(0, 0);
            setRowSelectionInterval(0, 0);
            
            _triggerCellSelection(-1, -1);
        }
    }
    
    public void setInitialColWidth(int col, int colWidth) {
        if (mInitialColWidth == null) {
            mInitialColWidth = new int[] {col, colWidth};
            
            repaint();
            return;
        }
            
        mInitialColWidth[0] = col;
        mInitialColWidth[1] = colWidth;
        
        repaint();
    }
    
    public void setInitialRowHeight(int row, int rowHeight) {
        if (mInitialRowHeight == null) {
            mInitialRowHeight = new int[] {row, rowHeight};
            
            repaint();
            return;
        }
        
        mInitialRowHeight[0] = row;
        mInitialRowHeight[1] = rowHeight;
        
        repaint();
    }
    
    /**
     * Store the odd cells bounds if there's any for the cell renderer
     * @param row - the number of row
     * @param col - the number of col 
     * @param oddCellRect - the {@code Rectangle} bounds of an odd cell
     */
    public void storeOddCells(int row, int col, Rectangle oddCellRect) {
        Map<Integer, Rectangle> colMap = new HashMap<>();
        
        if (mOddCells != null &&
            mOddCells.get(row) != null)
        {
           colMap = mOddCells.get(row);
           colMap.put(col, oddCellRect);
        } else {
            colMap.put(col, oddCellRect);
        }
        
        mOddCells.put(row, colMap);
    }
    
    public void updateTable() {
        updaterCount = 0;
        revalidate();
        repaint();
    }
    
    /**
     * Copy cell content.
     */
    private void _copy() {
        mCellClipboardLst.clear();
        TableModel model = (TableModel) getModel();
        
        boolean firstInit = true;
        for (int row : getSelectedRows()) {
            
            List<CellClipboard> columnCellClipboards = new ArrayList<>();
            for (int col : getSelectedColumns()) {
                
                if ((row == 0 && col == 0) ||
                    (row == 0 && col == 1))
                    continue;
                
                Map<Integer, Map<Integer, CellTextStyle>> stylesMap = TableDefaultCellRenderer.getCellTextStyle();
                
                if (model.getValueAt(row, col) == null)
                    continue;
                
                // if there's no any cell styles
                if (stylesMap.get(row) == null || stylesMap.get(row).get(col) == null) {
                    String formula = "";
                    if (model.getFormulas().get(row) != null &&
                        model.getFormulas().get(row).get(col) != null &&
                        firstInit)
                    {
                        formula = model.getFormulas().get(row).get(col);
                        firstInit = false;
                    }
                    
                    String val = (String) model.getValueAt(row, col);
                    CellClipboard clipboard = new CellClipboard(val);
                    clipboard.setRow(row);
                    clipboard.setCol(col);
                    if (!formula.isEmpty() && Formulator.isOneRowFormula(formula)) {
                        clipboard.setFormula(formula);
                    }
                    
                    columnCellClipboards.add(clipboard);
                    continue;
                }
                // else, if there's any cell styles
                CellTextStyle style = stylesMap.get(row).get(col);
                
                String val = (String) model.getValueAt(row, col);
                CellClipboard clipboard = new CellClipboard(val);
                clipboard.setCellTextStyle(style);
                clipboard.setRow(row);
                clipboard.setCol(col);
                
                columnCellClipboards.add(clipboard);
            }
            mCellClipboardLst.add(columnCellClipboards);
        }
    }
    
    private void _cut() {
        _copy();
        _saveState("", -1, 
                _getRowsToSave(), 
                _getColsToSave());
        
        _deleteContent();
    }
    
    /**
     * Delete cell content.
     */
    private void _deleteContent() {
        _saveState("", -1, _getRowsToSave(), _getColsToSave());
        TableModel model = (TableModel) getModel();
        for (int row : getSelectedRows()) {
            for (int col : getSelectedColumns()) {
                if ((row == 0 && col == 0) ||
                    (row == 0 && col == 1))
                    continue;
                
                model.setValueAt(null, row, col);
                TableDefaultCellRenderer.removeCellStyle(row, col);
            }
        }
    }
    
    private void _initEditorStyles(CellTextStyle style, TableCellEditor editor) {
        for (TextStyles txStyles : style.getStyles()) {
            switch (txStyles) {
                case BOLD -> {
                    editor.setBold();
                } case ITALIC -> {
                    editor.setItalic();
                } case UNDERLINED -> {
                    editor.setUnderline();
                }
            }
        }
        switch (style.getHorizontalAlignment()) {
            case LEFT_ALIGN -> {
                editor.setLeftAlign();
            } case CENTER_ALIGN -> {
                editor.setCenterAlign();
            } case RIGHT_ALIGN -> {
                editor.setRightAlign();
            }
        }
    }
    
    private void _paste(boolean formulaOnly) {
        
        if (mCellClipboardLst.isEmpty())
            return;
        
        int[] rows = getSelectedRows();
        int[] cols = getSelectedColumns();
        
        if ((rows[0] == 0 && cols[0] == 0) ||
            rows[0] == 0 && cols[0] == 1)
        {
            JOptionPane.showMessageDialog(new JFrame(), 
                        "You can't paste on a non-editable cell");
            return;
        }
        
        final TableModel model = (TableModel) getModel();
        _saveState("", -1, _getRowsToSave(), _getColsToSave());
        if (formulaOnly) {
            _saveFormulasState(model);
            String formula = "";
            final int maxOccupiedRow = getMaxOccupiedCells()[0];
            for (int row : rows) {
                for (int col : cols) {
                    if (row == 0)
                        break;
                    
                    if (col == 0)
                        continue;
                    
                    final CellClipboard clip = mCellClipboardLst.get(0).get(0);
                    
                    if (clip.getFormula() != null && formula.isEmpty()) {
                        formula = clip.getFormula();
                    }
                    
                    if (formula.isEmpty())
                        return;
                    
                    final String toPasteFormula = Formulator.changeCellRows(
                            formula, row+1);
                    
                    final Formulator formulator = new Formulator(toPasteFormula);
                    final TajosScrollPane sPane = (TajosScrollPane) this.getParent().getParent();
                    final TableColumnHeader columnHeader = 
                        (TableColumnHeader) sPane.getColumnHeader().getComponent(0);
                    
                    formulator.calculate(this, columnHeader);
                    model.setValueAt(formulator.getResult(), row, col);
                    model.addFormula(row, col, toPasteFormula);
                }
                
                if (row > maxOccupiedRow-2)
                    break;
            }
            
            return;
        }
        
        int row = rows[0], col = cols[0];
        for (List<CellClipboard> clipRow : mCellClipboardLst) {
            if (row >= getRowCount()) {
                JOptionPane.showMessageDialog(new JFrame(), 
                        "Can't paste anymore. Some cells content are not pasted because "
                        + "you reached at the end of the table, you can"
                        + " scroll to add cells before pasting.");
                
                return;
            }
            
            for (CellClipboard clipColumn : clipRow) {
                if (col >= getColumnCount()) {
                    JOptionPane.showMessageDialog(new JFrame(), 
                        "Can't paste anymore. Some cells content are not pasted because "
                        + "you reached at the end of the table, you can"
                        + " scroll to add cells before pasting");
                
                    return;
                }
                
                model.setValueAt(clipColumn.getContent(), row, col);
                TajosTable.instance().getRenderer().putCellStyles(row, col, clipColumn.getCellTextStyle());
                
                col++;
            }
            col = cols[0];
            row++;
        }
        updateTable();
    }
    
    private void _saveFormulasState(TableModel model) {
        mFormulasStateManager.addNewState(
            new FormulasState(model.getFormulas(), model));
    }
    
    private int [] _getRowsToSave() {
        int maxRow = getRowCount();
        
        int[] rows = new int[maxRow];
        for (int row=0; row<maxRow; row++) {
            rows[row] = row;
        }
        
        return rows;
    }
    
    private int[] _getColsToSave() {
        int maxCol = getColumnCount();
        
        int[] cols = new int[maxCol];
        for (int col=0; col<maxCol; col++) {
            cols[col] = col;
        }
        
        return cols;
    }
    
    private void _redo() {
        mTableStateManager.redo();
    }
    
    private void _removeLastRow() {
        // region: automatically remove rows
        int prefRows = getPreferredRowCount() > getMaxOccupiedCells()[0] ?
                getPreferredRowCount() : getMaxOccupiedCells()[0];
        
        if (getPreciseRowCount() > prefRows) {
            removeLastRow();
        }
        // region end
    }
    
    /**
     * Trigger a cell selection to notify the column header
     * and row header that a cell/s is/are selected
     */
    private void _triggerCellSelection(int row , int col) {
        int[] selectedRows, selectedCols;
        
        if (row == -1 && col == -1) {
            selectedRows = getSelectedRows();
            selectedCols = getSelectedColumns();
        } else {
            selectedRows = new int[] {row};
            selectedCols = new int[] {col};
        }

        Map<Integer, Integer> rows = new HashMap<>();
        Map<Integer, Integer> cols = new HashMap<>();
        for (int irow : selectedRows)
            rows.put(irow, irow);

        for (int icol : selectedCols)
            cols.put(icol, icol);

        mRowSelectionListener.onRowsSelected(rows);
        mColSelectionListener.onColumnsSelected(cols);
    }
    
    private void _undo() {
        mTableStateManager.undo();
        mFormulasStateManager.undo();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // region: we need to update the table 3 times for the UI to be properly render
        if (updaterCount < 3) {
            ++updaterCount;
            revalidate();
            repaint();
        }
        // region end
    }
    
    private static class TablePopupMenuManager {

        private final TajosTable mTable;
        private final int[] mRow;
        private final int[] mCol;
        private final TableModel mModel;
        
        /**
         * JPopupmenu manager for table cells
         * @param table - The table
         * @param row - The cell row index
         * @param col - The cell column index
         */
        public TablePopupMenuManager(TajosTable table, int[] row, int[] col) {
            mTable = table;
            mRow = row;
            mCol = col;
            
            mModel = (TableModel) mTable.getModel();
        }
        
        public void shiftCellDown() {
            saveStateShiftBottom(mCol);
            for (int row : mRow) {
                for (int col : mCol) {
                    if ((row == 0 && col == 0) ||
                        (row == 0 && col == 1))
                        continue;
                    
                    mModel.shiftCellDown(row, col);
                }
            }
        }
        
        public void shiftCellUp() {
            mTable._saveState("", -1, mTable._getRowsToSave(), mTable._getColsToSave());
            
            for (int row : mRow) {
                for (int col : mCol) {
                    if ((row == 0 && col == 0) ||
                        (row == 0 && col == 1))
                        continue;
                    
                    mModel.shiftCellUp(mRow[0], col);
                }
            }
        }
        
        public void shiftEntireRowUp() {
            mTable._saveState("", -1, mTable._getRowsToSave(), mTable._getColsToSave());
            
            for (int row : mRow) {
                mModel.shiftRowUp(mRow[0], -1);
            }
        }
        
        public void shiftCellRight() {
            saveStateShiftRight(mRow);
            for (int row : mRow) {
                for (int col : mCol) {
                    if ((row == 0 && col == 0) ||
                        (row == 0 && col == 1))
                        continue;
                    
                    mModel.shiftCellRight(row, col);
                }
            }
        }
        
        public void shiftCellLeft() {
            mTable._saveState("", -1, mTable._getRowsToSave(), mTable._getColsToSave());
            
            for (int row : mRow) {
                for (int col : mCol) {
                    if ((row == 0 && col == 0) ||
                        (row == 0 && col == 1))
                        continue;
                    
                    mModel.shiftCellLeft(row, mCol[0]);
                }
            }
        }
        
        public void shiftEntireColumnLeft() {
            mTable._saveState("", -1, mTable._getRowsToSave(), mTable._getColsToSave());
            
            for (int row : mRow) {
                for (int col : mCol) {
                    mModel.shiftColumnLeft(row, mCol[0]);
                }
            }
        }
        
        public void shiftEntireRowDown() {
            int[] cols = new int[mTable.getColumnCount()];
            for (int i=0; i<mTable.getColumnCount(); i++)
                cols[i] = i;
            
            saveStateShiftBottom(cols);
            for (int row : mRow)
                mModel.shiftRowDown(row);
        }

        private void shiftEntireColumnRight() {
            int[] rows = new int[mTable.getRowCount()];
            for (int i=0; i<mTable.getRowCount(); i++)
                rows[i] = i;
            
            saveStateShiftRight(rows);
            for (int col : mCol)
                mModel.shiftColumnRight(col);
        }
        
        private void saveStateShiftRight(int [] _rows) {
            int max = mModel.getColumnCount();
            
            int span = max - mCol[0];
            int[] cols = new int[span];
            for (int i=mCol[0], j=0; i<max; i++, j++) {
                cols[j] = i;
            }
            mTable._saveState("SHIFT-COL", mModel.getColumnCount(), _rows, cols);
        }
        
        private void saveStateShiftBottom(int[] _cols) {
            int max = mModel.getRowCount();
            
            int span = max - mRow[0];
            int[] rows = new int[span];
            for (int i=mRow[0], j=0; i<max; i++, j++) {
                rows[j] = i;
            }
            mTable._saveState("SHIFT-ROW", mModel.getRowCount(), rows, _cols);
        }
    }
    
    public enum HeaderType {
        ROW_H , COLUMN_H
    }
    public interface OnTablePressedListener {
        void onPressed(int row, int col);
        void onDragging(int [] row, int [] col);
        void onReleased();
    }
    public interface OnTextStyleKeysListener {
        void onBoldKeyPressed();
        void onItalicKeyPressed();
        void onUnderlineKeyPressed();
    }
    public interface OnSaveKeyListener {
        void onSaveKeyPressed();
    }
    
    /**
     * Use to calculate how many rows and columns should be displayed in the screen.
     */
    public class RowsAndColumnsCalculator {
        
        private final Component mRootPane;
        
        private final int MIN_COL_WIDTH = 75;
        private final int MIN_ROW_HEIGHT = 23;
        
        public RowsAndColumnsCalculator(Component rootPane) {
            mRootPane = rootPane;
        }
        
        public int getCalculatedRows() {
            final int rowHeight = MIN_ROW_HEIGHT,
                    columnHeaderHeight = 0;
            
            int rows = (mRootPane.getHeight() - columnHeaderHeight) / rowHeight;
            
            return rows;
        }
        
        public int getCalculatedCols() {
            final int colWidth = MIN_COL_WIDTH,
                    rowHeaderWidth = 0;
            
            int cols = (mRootPane.getWidth() - rowHeaderWidth) / colWidth;
            int rem = (mRootPane.getWidth() - rowHeaderWidth) % colWidth;
            
            if (rem < colWidth) {
                cols+=1;
            }
            
            return cols;
        }
    }
}