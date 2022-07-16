package com.tajos.studio.components;

import com.tajos.studio.interfaces.Margins;
import com.tajos.studio.MenuManager;
import com.tajos.studio.MenuTxFieldGroup;
import com.tajos.studio.WorkbookManager;
import com.tajos.studio.interfaces.MenuTxFieldModel;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.action.MenuRenamedState;
import com.tajos.studio.data.CellsDimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.table.TableModel;

/**
 * This custom component is intended only for the menu panel
 * @author Rene Tajos Jr.
 */
public class TajosMenuTextField extends JTextField implements Margins, MenuTxFieldModel, KeyBinds, Serializable {

    private MenuManager.SheetTxFieldListener sheetListener;
    
    private Color mOldBgColor;
    private final Color mDefaultBgColor = GradeUtils.Colors.creamyWhiteBlueColor;
    private final Color mSelectedSheetPanelBgColor = GradeUtils.Colors.highlightColor;
    private final Color mForegroundColor = GradeUtils.Colors.darkBlueColor;
    private final Color mEditingColor = GradeUtils.Colors.pinkColor;
    private int mTopMargin;
    private int mLeftMargin;
    private int mBottomMargin;
    private int mRightMargin;
    
    public SupportedComponent supportedComponentName = SupportedComponent.T_TEXTFIELD;
    private TxtFieldType mType;
    private boolean mIsEditable = false;
    private MenuManager mMenuManager;
    
    private final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    private final Cursor editingCursor = new Cursor(Cursor.TEXT_CURSOR);
    
    private String titlePHolder;
    
    private int position;
    private String mCappedTx;
    private String mOrigTx;
    private boolean isSelected = false;
    
    private boolean mIsMouseHovering;
    private boolean mIsPressed;
    
    private MenuTxFieldGroup mGroup;
    private String mActionCmd;
    private WorkbookSheetsGroup mWorkBookSheetsGroup;
    private TajosMenuTextField mSheetMaster;
    
    private boolean isSaved = false;
    // data
    private Map<Integer, Map<Integer, CellTextStyle>> mCellStyles = new HashMap<>();
    private Map<Integer, Map<Integer, String>> mFormulas = new HashMap<>();
    private List<List<Object>> mData = new ArrayList<>();
    private Map<Integer, Map<Integer, Rectangle>> mOddCells;
    private CellTextStyle.SelectedAll mSelectedAll;
    private List<List<CellsDimension>> mCellsDimension;
    // end
    public TajosMenuTextField() {
        defaultCaret.setBlinkRate(TajosMenuTextField.this.getCaret().getBlinkRate());
        addKeyListener(this);
        addMouseListener(mouseListener);
        addFocusListener(focusListener);
        addActionListener(enterKeyListener);
        setCaret(defaultCaret);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // UNDO
        if (CTRL_Z(e)) {
            mMenuManager.getStateManager().undo();
        }
        // REDO
        if (CTRL_Y(e)) {
            mMenuManager.getStateManager().redo();
        }
        // COPY
        if (CTRL_C(e)) {
            mMenuManager.copy(this);
        }
        // PASTE
        if (CTRL_V(e)) {
            mMenuManager.paste(this);
        }
        // Rename ==> Accelerator key is pressed
        if (CTRL_R(e)) {
            if (mIsEditable)
                return;

            mMenuManager.getStateManager().addNewState(new MenuRenamedState(TajosMenuTextField.this));
            mGroup.setSelected(TajosMenuTextField.this, true, true); // Enable Renaming/Editing on this txField
        }

        if (DELETE(e)) {
            final int result = GradeUtils.showWrkbkSheetDeletionDialog(getText());

            if (result == JOptionPane.OK_OPTION) {
                if (getType() == TxtFieldType.WORKBOOK) {
                    mMenuManager.deleteWorkbook(TajosMenuTextField.this);
                    mMenuManager.refreshDeletedItems(this, getGroup(), TxtFieldType.WORKBOOK);
                } else {
                    mMenuManager.deleteSheet(TajosMenuTextField.this);
                    mMenuManager.refreshDeletedItems(this, getGroup(), TxtFieldType.SHEET);
                }
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    
    /**
     * @Enum {@TextFieldType} to know what type this component have;
     */
    public enum TxtFieldType {
        WORKBOOK, SHEET
    }
    
    public void setSheetTypeListener(MenuManager.SheetTxFieldListener listener) {
        sheetListener = listener;
    }
    
    /**
     * attach the {@code MenuManager} of the menuPanel to access its functions.
     * @param menuManager the manager of menuPanel;
     */
    public void attachMenuManager(MenuManager menuManager) {
        mMenuManager = menuManager;
    }
    
    /**
     * {@code FocusListener} for {@code TajosMenuTextField} component to know if the window has lost focus on this component.
     */
    private FocusAdapter focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            if (getType() == TxtFieldType.SHEET && sheetListener != null)
                sheetListener.onFocusLost();
            
            if (mIsEditable) {
                // if the text is empty, set the text as Untitled
                if (getText().equals(""))
                    setText("Untitled");

                if (isTextTooLarge(getText())) {
                    mOrigTx = getText();
                    mCappedTx = getCappedText(getText() + "...");
                    setText(mCappedTx);
                } else {
                    mOrigTx = getText();
                }
                
                setEditingMode(false);
                setSelected(true);
            }
        }
    };
    
    /**
    * {@code KeyListener} - this will detect if the key ENTER is pressed or not on this component.
    * 
    */
    private AbstractAction enterKeyListener = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            // if the text is empty, set the text as Untitled
            if (getText().equals("")) {
                setText("Untitled");
            }
            
            if (isTextTooLarge(getText())) {
                mOrigTx = getText();
                mCappedTx = getCappedText(getText() + "...");
                setText(mCappedTx);
            } else {
                mOrigTx = getText();
            }
            
            mGroup.setSelected(TajosMenuTextField.this ,true, false);
            
            if (getType() == TxtFieldType.SHEET) {
                if (sheetListener != null)
                    sheetListener.onEnterKeyPressed();
                
                getParent().repaint();
                getParent().revalidate();
            }
        }
    };
    
    /**
    * @MouseListener for this {@TajosMenuTextField component}
    */
    private MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            
            mIsMouseHovering = true;
            
            if (_isTitled() && !mIsEditable)
                return;

            if (!mIsEditable) {
                setCursor(handCursor);
            } else {
                setCursor(editingCursor);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mIsMouseHovering = false;
            setCursor(defaultCursor);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setPressed(false);
            
            if (e.isPopupTrigger()) {
                boolean triggered = _triggerPopupLogic(e);
                if (triggered) {
                    _showPopupMenu(TajosMenuTextField.this, e.getX(), e.getY());
                    requestFocusInWindow();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                boolean triggered = _triggerPopupLogic(e);
                if (triggered) {
                    _showPopupMenu(TajosMenuTextField.this, e.getX(), e.getY());
                    requestFocusInWindow();
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            setPressed(true);
            
            if (e.getButton() == MouseEvent.BUTTON2) // if middle button is clicked! We dont support it just return
                return;
            
            if (e.getButton() == MouseEvent.BUTTON3 && !_isTitled())
                return;
            // region: if its right click
            if (e.isPopupTrigger()) {
                boolean triggered = _triggerPopupLogic(e);
                if (triggered) {
                    _showPopupMenu(TajosMenuTextField.this, e.getX(), e.getY());
                    requestFocusInWindow();
                }
                return;
            }
            // region: OPEN THE SHEET
            if (_isTitled()) { // checks if the textfield/workbook/sheetlabel has already been titled
                if (isEditable())
                    return;
                
                if (mGroup.getSelection() == getModel())
                    return;
                
                TajosMenuTextField thisTx = (TajosMenuTextField) mGroup.getSelection();
                if (thisTx != null && !thisTx.isSaved()) {
                    
                    if (getType() == TxtFieldType.WORKBOOK)
                        return;
                    
                    int result = JOptionPane.showConfirmDialog(new JFrame(), thisTx.getText() + " is not yet saved, do you wish to continue?", 
                            "Confirm", JOptionPane.WARNING_MESSAGE);
                    
                    if (result == JOptionPane.OK_OPTION) {
                        if (getType() != TxtFieldType.WORKBOOK)
                            WorkbookManager.instance().openTable(TajosMenuTextField.this);
                        
                        mGroup.setSelected(TajosMenuTextField.this, true, false);
                    }
                } else {
                    if (getType() != TxtFieldType.WORKBOOK)
                            WorkbookManager.instance().openTable(TajosMenuTextField.this);
                    
                    mGroup.setSelected(TajosMenuTextField.this, true, false);
                }
                
                return;
            }
            // region end
            // region: CREATE A SHEET/WORKBOOK
            //** if the textfield type is WORKBOOK // ==> This is for Adding new Workbook
            if (getType() == TxtFieldType.WORKBOOK) {
                mMenuManager.addNewSheet(-1, false, TajosMenuTextField.this);
                mMenuManager.addNewWorkbook(true);
                setType(TxtFieldType.WORKBOOK);
                setText(titlePHolder);
                mGroup.setSelected(TajosMenuTextField.this, true, true);
                repaint();
                revalidate();
            // else if the texfield type is SHEET // ==> This is for Adding new SHEET
            } else {
                TajosMenuTextField thisTx = (TajosMenuTextField) mGroup.getSelection();
                if (thisTx != null && !thisTx.isSaved() && TajosTable.instance().isInitialized()) {
                    int result = JOptionPane.showConfirmDialog(new JFrame(), thisTx.getText() + " is not yet saved, do you wish to continue?", 
                            "Confirm", JOptionPane.WARNING_MESSAGE);
                    
                    if (result == JOptionPane.OK_OPTION) {
                        _addNewSheet();
                        
                        WorkbookManager.instance().blankTable(TajosMenuTextField.this);
                    }
                } else {
                    _addNewSheet();
                    
                    WorkbookManager.instance().blankTable(TajosMenuTextField.this);
                }
                
                repaint();
                revalidate();
            }
            // region end
            if (mIsEditable) {
                setCursor(editingCursor);
                return;
            }
            
            setCursor(defaultCursor);
        }

        private void _showPopupMenu(MenuTxFieldModel c, int x, int y) {
            mMenuManager.showPopupMenu(c, x, y);
        }
    };
    
    private boolean _triggerPopupLogic(MouseEvent e) {
        if (!_isTitled() || mIsEditable)
            return false;
        
        if (mGroup.getSelection() == getModel())
            return true;

        TajosMenuTextField thisTx = (TajosMenuTextField) mGroup.getSelection();
        if (thisTx != null && !thisTx.isSaved()) {
            int result = JOptionPane.showConfirmDialog(new JFrame(), thisTx.getText() + " is not yet saved, do you wish to continue?", 
                    "Confirm", JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                WorkbookManager.instance().openTable(TajosMenuTextField.this);
                mGroup.setSelected(TajosMenuTextField.this, true, false);
                
                return true;
            }
        } else if (thisTx != null && thisTx.isSaved) {
            WorkbookManager.instance().openTable(TajosMenuTextField.this);

            mGroup.setSelected(TajosMenuTextField.this, true, false);
            return true;
        }

        return false;
    }
    
    private void _addNewSheet() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final RoundedPanel sheetPanel = (RoundedPanel) getParent();
        mMenuManager.addNewSheet(sheetPanel.getPosition() + 1, true, getSheetMaster());
        setType(TxtFieldType.SHEET);
        setText(titlePHolder);
        mGroup.setSelected(TajosMenuTextField.this, true, true);
    }
    
    /**
     * TajosDefaultCaret, this is a customized DefaultCaret
     * This is used to avoid text selection when the user click multiple times on this {@TajosMenuTextField component}
     */
    private final DefaultCaret defaultCaret = new DefaultCaret() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1 && !mIsEditable) {
                return;
            }
            
            super.mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                return;
            }
            
            super.mousePressed(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!isEditable()) {
                return;
            }
            
            super.mouseDragged(e);
        }
    };
    
    /**
     * A {@code Class} to store the sheets added under this {@code Workbook type} of {@code TajosMenuTextField}
     */
    public class WorkbookSheetsGroup implements Serializable {
        protected List<Component> components = new ArrayList<>();
        private final RoundedPanel mMenuPanel;
        
        public WorkbookSheetsGroup(RoundedPanel menuPanel) {
            mMenuPanel = menuPanel;
        }
        
        /**
         * Adds the sheet under this {@code Workbook type} of {@code TajosMenuTextField}
         * @param b The sheets under this {@code Workbook type} of {@code TajosMenuTextField}
         */
        public void add(Component b) {
            if (b == null) {
                return;
            }
            
            components.add(b);
        }
        
        public void addAll(List<Component> c) {
            components.removeAll(components);
            components.addAll(c);
        }
        
        public int size() {
            return components.size();
        }
        
        public List<Component> getElements() {
            return components;
        }
        
        public List<Component> copyElements() {
            List<Component> lst = new ArrayList<>();
            lst.addAll(components);
            
            return lst;
        }
        
        /**
         * Removes a component under this group;
         * @param c The component to be removed;
         */
        public void remove(Component c) {
            components.remove(c);
            ((TajosMenuTextField)c).getGroup().remove((TajosMenuTextField)c);
        }
        
        /**
         * This will remove all the sheets under this {@code Workbook type} of {@code TajosMenuTextField}
         */
        public void removeAllSheets() {
            for (Component s : components) {
                final TajosMenuTextField t = (TajosMenuTextField) s;
                t.getGroup().remove(t);
                mMenuPanel.remove(s.getParent());
            }
            
            components.removeAll(components);
        }
    }
    
    /**
     * Saves or put the data of this sheet
     * @param data
     * @param cellsDimensions
     * @param oddCells
     * @param cellStyles 
     * @param selectedAll 
     */
    public void putData(List<List<Object>> data, List<List<CellsDimension>> cellsDimensions, Map<Integer, Map<Integer, Rectangle>> oddCells, 
            Map<Integer, Map<Integer, CellTextStyle>> cellStyles, CellTextStyle.SelectedAll selectedAll) 
    {
        mData = data;
        mOddCells = oddCells;
        mCellStyles = cellStyles;
        mSelectedAll = selectedAll;
        mCellsDimension = cellsDimensions;
    }

    public List<List<CellsDimension>> getCellsDimension() {
        return mCellsDimension;
    }

    public void setCellsDimension(List<List<CellsDimension>> mCellsDimension) {
        this.mCellsDimension = mCellsDimension;
    }

    public Map<Integer, Map<Integer, String>> getFormulas() {
        return mFormulas;
    }

    public void setFormulas(Map<Integer, Map<Integer, String>> mFormulas) {
        this.mFormulas = mFormulas;
    }
    
    public CellTextStyle.SelectedAll getSelectedAllData() {
        return mSelectedAll;
    }
    
    public List<List<Object>> getData() {
        return mData;
    }
    
    public Map<Integer, Map<Integer, Rectangle>> getOddCellsData() {
        return mOddCells;
    }
    
    public Map<Integer, Map<Integer, CellTextStyle>> getCellStylesData() {
        return mCellStyles;
    }
    
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().equals(MenuManager.SHEET_STR) || getText().equals(MenuManager.WORKBOOK_STR)) {
            setForeground(mMenuManager.getDefaultForegroundColor());
        }
    }
    
    public void setIsSavedState(boolean _isSaved) {
        isSaved = _isSaved;
    }
    
    public boolean isSaved() {
        return isSaved;
    }
    
    /**
     * Sets the group for the sheets under this {@code Workbook type} of {@code TajosMenuTextField} component
     * We need to store the sheets under this {@code Workbook type} of {@code TajosMenuTextField} so we can locate
     * and delete this sheets if this {@code Workbook} will be deleted.
     * @param grp The {@code WorkbookSheetsGroup} for this component
     */
    public void setWorkbookSheetsGroup(WorkbookSheetsGroup grp) {
        mWorkBookSheetsGroup = grp;
    }
    
    /**
     * 
     * @return The {@code WorkbookSheetsGroup} of this {@code Workbook type} of {@code TajosMenuTextField} component
     */
    public WorkbookSheetsGroup getWorkbookSheetsGroup() {
        return mWorkBookSheetsGroup;
    }
    
    /**
     * Sets the workbook master of this Sheet,
     * {@code WorkbookMaster} means, the workbook where this sheet is under;
     * @param m 
     */
    public void setSheetMaster(TajosMenuTextField m) {
        mSheetMaster = m;
    }
    
    /**
     * 
     * @return the Workbook instance where this sheet is under;
     */
    public TajosMenuTextField getSheetMaster() {
        return mSheetMaster;
    }
    
    /**
     * @Description This will detect if the text that was typed on this component is too long or not.
     * @param text the text or string that the user typed on this {@TajosMenuTextField} component.
     * @return {@code true} if the text is too long. Otherwise, {@code false} if the text is not long
     */
    private boolean isTextTooLarge(String text) {
        final Rectangle2D textRect = getFontMetrics(getFont()).getStringBounds(text, getGraphics());
        final int textWidthThreshold = getParent().getWidth() - getMarginLeft();
        
        return textRect.getWidth() > textWidthThreshold - 8;
    }
    
    /**
    * This is a {@code recursive method}
    * @return the text to display whether it was capped because the text is too long or not.
    * if it was capped, it will show three dots "..." in the last part of the text
    */
    private String getCappedText(String text) {
        if (isTextTooLarge(text)) {
            String cappedText = text.substring(0, text.length()-6) + "...";
            return getCappedText(cappedText);
        }
        
        return text;
    }
    
    public String getOriginalText() {
        return mOrigTx;
    }
    
    /**
     * This will set this {@code TajosMenuTextField} to editing mode or not.
     * @param isEditing {@code true} if this component is set to be for editing mode, {@code false} if this is set to be not for editing mode
     */
    @Override
    public void setEditingMode(boolean isEditing) {
        if (isEditing) {
            mIsEditable = isEditing;
            // set the original text always if its in editing mode
            if (mOrigTx != null)
                setText(mOrigTx);
            
            setEditable(true);
            setForeground(mForegroundColor);
            setSelectionStart(0);
            setSelectionEnd(getText().length());
            getCaret().setVisible(true);
            return;
        }
        
        mIsEditable = isEditing;
        setEditable(false);
        setSelectionStart(0);
        setSelectionEnd(0);
        getCaret().setVisible(false);
        setCursor(defaultCursor);
    }
    
    /**
     * @Description this will detect if this {@TajosMenuTextField} has already a title
     * @return {@code true} if this component has already been titled, else {@code false} if its not titled;
     */
    private boolean _isTitled() {
        return getType() == TxtFieldType.SHEET && !(getText().equals(MenuManager.SHEET_STR)) ||
               getType() == TxtFieldType.WORKBOOK && !(getText().equals(MenuManager.WORKBOOK_STR));
    }
    
    /**
     * This will set the transparency of this component.
     * @param isTransparent {@code true} for transparent and {@code false} for not transparent.
     */
    public void setTransparent(boolean isTransparent) {
        if (isTransparent) {
            mOldBgColor = getBackground();
            setBackground(GradeUtils.Colors.transparent);
        } else {
            if (mOldBgColor == null) {
                setBackground(Color.BLACK);
                return;
            }
            
            setBackground(mOldBgColor);
        }
    }
    
    /**
     * {@code Warning:} if this method will be called, we should be sure that this is a Sheet Type of {@code TajosMenuTextField} component
     * because a {@code Sheet} type have a background panel of its own while a {@code Workbook} type don't have, we will change the
     * {@code background color} of its {@code background panel} so it is necessary to identify what type this component have;
     * @param isSelected {@code true} if this component is set to be selected, {@code false} if this is set to not be selected
    */
    @Override
    public void setSelected(boolean isSelected) {
        if (getType() == TxtFieldType.WORKBOOK)
            return;
        
        this.isSelected = isSelected;
        // but if this textfield is type SHEET then let's change the background when this is selected
        if (isSelected) {
            
            _select(getParent());
        } else {
            setEditingMode(false);
            _unselect(getParent());
        }
    }
    
    /**
     * @Description unselect and change the background panel of this component to its {@default color}
     * @param panel the {@background panel} of this component
     */
    private void _unselect(Container panel) {
        setBackground(mDefaultBgColor);
        panel.setBackground(mDefaultBgColor);
    }
    
    /**
     * @Description select and change the background panel of this component to its {@selection color}
     * @param panel the {@background panel} of this component
     */
    private void _select(Container panel) {
        if (mIsEditable) {
            setBackground(mEditingColor);
            panel.setBackground(mEditingColor);
            return;
        } 
        
        setBackground(mSelectedSheetPanelBgColor);
        panel.setBackground(mSelectedSheetPanelBgColor);
    }
    
    /**
     * set the {@code index position} of this component from its {@code LayoutManager ListViewLayout};
     * {@code Warning} this is only useful with customized {@code LayoutManager ListViewLayout}
     * @param pos the {@code index position} of this component relative to its parent.
     */
    public void setPosition(int pos) {
        position = pos;
    }
    
    /**
     * @Warning this is only useful with customized {@LayoutManager ListViewLayout}
     * @return the {@index position} of this component relative to its parent
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * @Description set the default font of this component
     * @param size the {@font size} for the font of this component
     */
    public void setDefaultFont(int size) {
        setFont(GradeUtils.getDefaultFont(size));
    }
    
    /**
     * @Description set the type of this component
     * @param type the possible {@TxtFieldType type} of this component
     */
    public void setType(TxtFieldType type) {
        mType = type;
        // init the hint according to its type
        if (mType == TxtFieldType.WORKBOOK) {
            titlePHolder = "workbook" + (mMenuManager.getWorkBookCount()-1);
            titlePHolder = validateName(titlePHolder, mMenuManager.getWorkBookCount()-1);
        } else {
            titlePHolder = "table" + mMenuManager.getSheetCount();
            titlePHolder = validateName(titlePHolder, mMenuManager.getSheetCount());
        }
    }
    
    /**
     * Validates the new name if it's valid or not, if the name already
     * existed in the group it will recurse until it can generate a name
     * that's not exist in the group.
     * @param name The Name to be validated.
     * @param count The size of the group.
     */
    private String validateName(String name, int count) {
        if (getGroup() == null)
            return name;
        
        boolean isUnique = getGroup().isNameUnique(name);
        if (!isUnique) {
            ++count;
            
            return validateName(name + count, count);
        }
        
        return name;
    }
    
    /**
     * 
     * @return the {@TxtFieldType type} of this component
     */
    public TxtFieldType getType() {
        return mType;
    }

    /**
     * @Description set the margins of this component
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @param margins the possible margins for this component
     */
    @Override
    public void setMargins(int margins) {
        mTopMargin = margins;
        mLeftMargin = margins;
        mRightMargin = margins;
        mBottomMargin = margins;
    }

    /**
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @return all the margins of this component
     * {@LeftMargin} {@TopMargin} {@RightMargin} {@BottomMargin}
     */
    @Override
    public int[] getAllMargins() {
        return new int[] {mTopMargin,mLeftMargin,mRightMargin,mBottomMargin};
    }

    /**
     * @Description set the top margin
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @param margin the top margin
     */
    @Override
    public void setMarginTop(int margin) {
       mTopMargin = margin;
    }

    /**
     * @return the top margin
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     */
    @Override
    public int getMarginTop() {
        return mTopMargin;
    }

    /**
     * @Description set the left margin
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @param margin the left margin
     */
    @Override
    public void setMarginLeft(int margin) {
        mLeftMargin = margin;
    }

    /**
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @return the left margin
     */
    @Override
    public int getMarginLeft() {
        return mLeftMargin;
    }

    /**
     * @Description set the right margin
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @param margin the right margin
     */
    @Override
    public void setMarginRight(int margin) {
        mRightMargin = margin;
    }

    /**
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @return the right margin
     */
    @Override
    public int getMarginRight() {
        return mRightMargin;
    }

    /**
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @Description set the bottom margin
     * @param marg the bottom margin
     */
    @Override
    public void setMarginBottom(int marg) {
        mBottomMargin = marg;
    }

    /**
     * @Warning this will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @return the bottom margin
     */
    @Override
    public int getMarginBottom() {
        return mBottomMargin;
    }
    
    /**
     * 
     * @return the {@MenuTxField model} of this component
     */
    public MenuTxFieldModel getModel() {
        return this;
    }
    
    /**
     * 
     * @return {@code true} if this component is selected, {@code false} if its not selected
     */
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * 
     * @return {@code true} if this component is pressed, {@code false} if its not pressed
     */
    @Override
    public boolean isPressed() {
        return mIsPressed;
    }

    /**
     * 
     * @return {@code true} if the mouse is hovering on this component, {@code false} if its not
     */
    @Override
    public boolean isRollover() {
        return mIsMouseHovering;
    }

    /**
     * @Description sets the button whether its pressed or not
     * @param b {@code true} if its pressed, {@code false} if its not pressed
     */
    @Override
    public void setPressed(boolean b) {
        mIsPressed = b;
    }

    /**
     * @Description sets the action command for this component
     * @param command the command in {@string} format
     */
    @Override
    public void setActionCommand(String command) {
        mActionCmd = command;
    }

    /**
     * 
     * @return the {@string} command of this component
     */
    @Override
    public String getActionCommand() {
        return mActionCmd;
    }

    /**
     * @Description set the {@MenuTxFieldGroup group} for this component
     * @param group the {@MenuTxFieldGroup group} for this component
     */
    @Override
    public void setGroup(MenuTxFieldGroup group) {
        mGroup = group;
    }
    
    /**
     * 
     * @return the {@MenuTxFieldGroup group} of this component
     */
    @Override
    public MenuTxFieldGroup getGroup() {
        return mGroup;
    }
    
    /**
     * @return 
    * @NOTSUPPORTED
    */
    @Override
    public Object[] getSelectedObjects() {return null;}
    
    /**
     * @param enabled
    * @NOTSUPPORTED
    */
    @Override
    public void setEnabled(boolean enabled) {}
    /**
    * @NOTSUPPORTED
    */
    @Override
    public void setRollover(boolean b) {}
    /**
    * @NOTSUPPORTED
    */
    @Override
    public void addItemListener(ItemListener l) {}
    /**
    * @NOTSUPPORTED
    */
    @Override
    public void removeItemListener(ItemListener l) {}
    /**
    * @NOTSUPPORTED
    */
    @Override
    public void addChangeListener(ChangeListener l) {}
    /**
    * @NOTSUPPORTED
    */
    @Override
    public void removeChangeListener(ChangeListener l) {}
}