package com.tajos.studio.activities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tajos.studio.net.DBManager;
import com.tajos.studio.data.Sheet;
import com.tajos.studio.GradeApp;
import com.tajos.studio.MenuManager;
import com.tajos.studio.MenuTxFieldGroup;
import com.tajos.studio.WorkbookManager;
import com.tajos.studio.action.StateManager;
import com.tajos.studio.components.TajosMenuTextField;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.components.TajosTable.RowsAndColumnsCalculator;
import com.tajos.studio.dialogs.UserProfileDialog;
import com.tajos.studio.data.CellsDimension;
import com.tajos.studio.data.Workbook;
import com.tajos.studio.excel.ExcelFilesFilter;
import com.tajos.studio.excel.ExcelHandler;
import com.tajos.studio.graphics.RoundedShapePath;
import com.tajos.studio.interfaces.TextStyles;
import com.tajos.studio.js.adapter.JavascriptInterfaceAdapter;
import com.tajos.studio.dialogs.FilterDialog;
import com.tajos.studio.dialogs.ImportingProgressDialog;
import com.tajos.studio.dialogs.NotificationPopup;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.table.Formulator;
import com.tajos.studio.table.TableCellEditor;
import com.tajos.studio.table.TableColumnHeader;
import com.tajos.studio.table.TableDefaultCellRenderer;
import com.tajos.studio.table.TableRowHeader;
import com.tajos.studio.table.TableModel;
import com.tajos.studio.util.ComponentResizer;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.json.simple.JSONObject;

/**
 *
 * @author Rene Tajos Jr.
 */
public class WorkBookActivity extends javax.swing.JFrame {
    
    private final String TAG = "WorkbookActivity: ";
    private boolean isFromDeletion = false; // this is used to know if the publishing came from deletion of workbooks and sheets
    private FilterDialog.OnFilteringListener mFilteringListener;
    private String mLastSpecialOperator;
    private MenuManager.SheetTxFieldListener mSheetTxFieldListener;
    private final String workbookIDTAG = "Workspace code: ";
    
    private final Color disabledColor = new Color(187,187,187);
    
    private final Color tableEnabledColor = new Color(227,219,242);
    private final Border tableEnabledBorder = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(227, 219, 242));
    private final Border tableDisabledBorder = javax.swing.BorderFactory.createLineBorder(disabledColor);
    
    private MenuManager mMenuManager;
    private StateManager mStateManager; // this will handle the stacking of undo/redo states
    
    private WorkbookManager mWorkbookManager;
    
    private final UserProfileDialog mUserProfilePopupMenu;
    private NotificationPopup notificationPopup;
    
    private JSONObject userInfo = new JSONObject();
    
    private final List<Component> ribbonGroup = new ArrayList<>();
    
    private MenuManager.OnDeletionListener mMenuDeletionListener;
    private ComponentResizer mWindowResizer;
    
    public static int prevWindowState = Frame.NORMAL;
    
    public enum PublishType {
        ONE_SHEET, ONE_WORKBOOK, ALL_WORKBOOKS
    }
    
    /**
     * Creates new form WorkBookActivity
     **/
    public WorkBookActivity() {
        setCursor(Cursor.getDefaultCursor());
        initComponents();
        GradeUtils.centerFrame(this);
        
        _initListeners();
        
        mUserProfilePopupMenu = new UserProfileDialog();
        _backgroundTask();
    }
    
    private void _backgroundTask() {
        Timer timer = new Timer();
        GradeApp.executor().execute(() -> {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        updateData();
                        
                        if (DBManager.getInstance().getUserData() == null)
                            return;
                        
                        if (DBManager.getInstance().getUserData().isEmpty())
                            return;
                        
                        String workbookId = DBManager.getInstance().getUserData().get("workspace_id").toString();
                        
                        File saveFile = new File(GradeApp.getDefaultSaveDirectory()  + "/"+ workbookId +".dat");
                        if (!saveFile.exists()) {
                            GradeUtils.writeFile("[]", saveFile.toURI(), false);
                        }
                        
                        _initLogic();
                        mainPanel.setVisible(true);
                        loadingPane.setVisible(false);
                        root.remove(loadingPane);
                        timer.cancel();
                    } catch (IOException ex) {
                        GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 100);
        });
    }
    
    public void disableTable() {
        tableScrollPane.setVisible(true);
        
        table.disableRibbon(true);
        table.disableTable();
        initTable(null);
        saveBtn.setBackground(disabledColor);
        publishBtn.setBackground(disabledColor);
        importBtn.setBackground(disabledColor);

        table.setGridColor(new Color(187,187,187, 50));
        tableScrollPane.setBorder(tableDisabledBorder);
        TableColumnHeader colHeader = (TableColumnHeader)tableScrollPane.getColumnHeader().getComponent(0);
        TableRowHeader rowHeader = (TableRowHeader)tableScrollPane.getRowHeader().getComponent(0);
        colHeader.disable();
        rowHeader.disable();
    }
    
    public void enableTable(boolean isTableEmpty) {
        saveBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        publishBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        importBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        
        table.disableRibbon(false);
        table.enableTable();

        table.setGridColor(tableEnabledColor);
        tableScrollPane.setBorder(tableEnabledBorder);
        table.updateTable();
    }
    
    private void _initListeners() {
        table.addTablePressedListener(new TajosTable.OnTablePressedListener() {
            @Override
            public void onPressed(int row, int col) {
                _hideUserProfilePopup();
            }
            @Override
            public void onDragging(int[] row, int[] col) {}
            @Override
            public void onReleased() {}
        });
        // JavascriptInterface listener
        DBManager.getInstance().getJavaScriptInterface()
            .addJavascriptInterfaceListener(new JavascriptInterfaceAdapter() {
                @Override
                public void onDataChanged(String data) {
                    updateData();
                }

                @Override
                public void onPublished() {
                    _onPublished();
                }

                @Override
                public void isSignedOut() {
                    setVisible(false);
                    dispose();
                }

                });
        // end
        // sheet txfield listener
        mSheetTxFieldListener = new MenuManager.SheetTxFieldListener() {
            @Override
            public void onEnterKeyPressed() {
                updateSheetTitle();
            }

            @Override
            public void onFocusLost() {
                updateSheetTitle();
            }
        };
        
        mMenuDeletionListener = (invoker, invokerType) -> {
            // on delete success
            isFromDeletion = true;
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            save();
            
            if (invokerType == TajosMenuTextField.TxtFieldType.WORKBOOK) {
                String workbookName = invoker.getText();
                try {
                    // delete the workbook
                    mWorkbookManager.publish(PublishType.ONE_WORKBOOK,
                            workbookName, null);
                } catch (JsonProcessingException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                }
            } else { // otherwise, delete a sheet
                try {
                    _publish(PublishType.ONE_WORKBOOK);
                } catch (JsonProcessingException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                }
            }
        };
        
        table.setTxStyleKeysListener(new TajosTable.OnTextStyleKeysListener() {
            @Override
            public void onBoldKeyPressed() {
                _bold();
            }

            @Override
            public void onItalicKeyPressed() {
                _italic();
            }

            @Override
            public void onUnderlineKeyPressed() {
                _underline();
            }
        });
        
        table.setSaveKeyListener(() -> {
            // on save key pressed
            save();
        });
        
        mFilteringListener = (filteredData) -> {
            // on Filtering complete
            if (filteredData.isEmpty()) {
                JOptionPane.showMessageDialog(new JFrame(), 
                        "Nothing has been found, unable to filter.");
                return;
            }
            
            table.setFilteredData(filteredData);
            filterImg.setImageResource(new ImageIcon(WorkBookActivity.class.getResource("/icons/filter-filled.png")));
            
            table.revalidate();
            table.repaint();
        };
    }
    
    private void _initLogic() throws IOException {
        setBackground(new Color(0,0,0,0));
        mWindowResizer = new ComponentResizer();
        mWindowResizer.registerComponent(this);
        mWindowResizer.setSnapSize(new Dimension(3, 3));
        mWindowResizer.setMaximumSize(getMaximumSize());
        mWindowResizer.setMinimumSize(new Dimension(getPreferredSize().width/2+200, getPreferredSize().height/2));
        tableScrollPane.attachRootFrameResizer(mWindowResizer);
        
        _initWorkspace();
        
        // always resize a resizable text so it would display properly on screen
        sheetTitleLbl.resize();
        
        mStateManager = new StateManager();
        // region: init menu manager
        mMenuManager = new MenuManager(menuPanel, mStateManager, this); // initialize the menu manager 
        mMenuManager.setPopupMenu(TxFieldMenuPopup);
        mMenuManager.setDeletionListener(mMenuDeletionListener);
        mMenuManager.setSheetTxFieldListener(mSheetTxFieldListener);
        // region end
        
        int width = contentRootPanel.getWidth();
        int height = contentRootPanel.getHeight();
        workbkRootPane.setBounds(workbkRootPane.getX(), workbkRootPane.getY(), width, height);
        
        tableScrollPane.attachTable(table);
        
        sheetTitleLbl.setText("Open A Table");
        sheetTitleLbl.resizeWithPreferredSize();
        
        // region: group all of the ribbon's components
        ribbonGroup.add(bold);
        ribbonGroup.add(italic);
        ribbonGroup.add(underline);
        ribbonGroup.add(font_color);
        ribbonGroup.add(fontOpenColor);
        ribbonGroup.add(fill);
        ribbonGroup.add(fillOpenColor);
        ribbonGroup.add(left_align);
        ribbonGroup.add(center_align);
        ribbonGroup.add(right_align);
        ribbonGroup.add(formula_dropdown);
        ribbonGroup.add(filterImg);
        table.linkRibbonComponents(ribbonGroup);
        // region end
        
        if (!_openSaveFiles()) {
            mMenuManager.initialize();
            disableTable();
        }
        table.updateTable();
    }
    
    public final void initTable(List<List<Object>> data) {
        // region: init table
        tableScrollPane.resetViewPosition();
        table.setAutoCreateColumnsFromModel(true);
        
        RowsAndColumnsCalculator m = new TajosTable().new RowsAndColumnsCalculator(tableRootPane);
        if (table.getPopupMenu() == null)
            table.setPopupMenu(cellPopupMenu);
        
        table.setModel(new TableModel(this, table, data,
                m.getCalculatedRows(), m.getCalculatedCols()));
        
        tableScrollPane.setColumnHeaderView(new TableColumnHeader(table, tableScrollPane, data == null));
        tableScrollPane.setRowHeaderView(new TableRowHeader(table, tableScrollPane, data == null));
        
        table.setInitialized(true);
        
        updateUI();
        // region end
    }
    
    private boolean _openSaveFiles() throws IOException  {
        boolean hasSaveFiles = false;
        String workbookId = DBManager.getInstance().getUserData().get("workspace_id").toString();
        List<Workbook> workbooks = mWorkbookManager.readSaveFile(GradeApp.getDefaultSaveDirectory() + "/"+ workbookId +".dat");
        
        boolean isFirstInit = true;
        if (!workbooks.isEmpty()) {
            for (Workbook workbook : workbooks) {
                String workbookName = workbook.getWorkbookName();
                
                TajosMenuTextField wrkbkComponent = mMenuManager.addNewWorkbook(true);
                wrkbkComponent.setForeground(GradeUtils.Colors.darkBlueColor);
                wrkbkComponent.setText(workbookName);
                
                List<Sheet> sheets = workbook.getSheets();
                for (Sheet sheet : sheets) {
                    String sheetName = sheet.getSheetName();
                    
                    TajosMenuTextField sheetComponent = mMenuManager.addNewSheet(-1, true, wrkbkComponent);
                    sheetComponent.setText(sheetName);
                    sheetComponent.setForeground(GradeUtils.Colors.darkBlueColor);
                    sheetComponent.setIsSavedState(sheet.getIsOnSaveState());
                    sheetComponent.putData(sheet.getData(), sheet.getCellsDimensions(), sheet.getOddCells(),
                            sheet.getTextStyles(), sheet.getSelectedAll());
                    sheetComponent.setFormulas(sheet.getFormulas());
                    
                    if (isFirstInit) {
                        mWorkbookManager.openTable(sheetComponent);
                        sheetComponent.getGroup().setSelected(sheetComponent, true, false);
                        isFirstInit = false;
                    }
                }
                mMenuManager.addNewSheet(-1, false, wrkbkComponent);
            }
            mMenuManager.addNewWorkbook(false);
            hasSaveFiles = true;
        }
        
        return hasSaveFiles;
    }
    
    /**
     * This will be called by the Java-script interface every time
     * the data in the database gets updated.
     * Updates the data and pass the data to the components who needs it.
     * This method should not be called in any other class except the
     * Java-script interface class and this activity.
     */
    public void updateData() {
        if (DBManager.getInstance().getUserData() == null)
            return;
        
        if (DBManager.getInstance().getUserData().isEmpty())
            return;
        
        setCursor(Cursor.getDefaultCursor());
        String workspaceId = DBManager.getInstance().getUserData().get("workspace_id").toString();
        userInfo = (JSONObject) DBManager.getInstance().getUserData().get("info");
        
        Object nameObj = userInfo.get("name");
        
        if (nameObj != null)
            userNameLbl.setText(nameObj.toString());
        userNameLbl.resizeWithPreferredSize();
        workbookCode.setText(workbookIDTAG + workspaceId);
        
        // region: update the user's profile pic if there's any
        String profPicLink = userInfo.get("photo_url").toString();
        if (!profPicLink.isEmpty()) {
            userProfImg.setImageURL(profPicLink);
            userProfImg.setImageLoadListener(() -> {
                // on image loaded
                updateUI();
            });
        }
        // region end
    }
    
    public void updateSheetTitle() {
        if (!sheetTitleLbl.isVisible())
            sheetTitleLbl.setVisible(true);
        
        String title = table.getInvoker().getOriginalText() != null? 
                table.getInvoker().getOriginalText() : table.getInvoker().getText();
        
        sheetTitleLbl.setText(title);
        sheetTitleLbl.resizeWithPreferredSize();
    }
    
    private void _initWorkspace() {
        mWorkbookManager = new WorkbookManager(tableScrollPane, table, this);
    }
    
    private void _hideUserProfilePopup() {
        if (mUserProfilePopupMenu.isVisible()) {
            mUserProfilePopupMenu.setVisible(false);
        }
    }
    
    public void save() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (!table.isInitialized())
            return;
        
        initialSaveToInvoker();
        table.getInvoker().setIsSavedState(true);
        mWorkbookManager.setWorkbookGroup(mMenuManager.getWorkbookGroup());
        try {
            mWorkbookManager.saveData(() -> {
                // on saving complete
                setCursor(Cursor.getDefaultCursor());
                notificationPopup = new NotificationPopup("Saved!");
                notificationPopup.show(this);
            });
        } catch (URISyntaxException | IOException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public void initialSaveToInvoker() {
        // region: get only the max occupied cell to save
        TableModel model = (TableModel) table.getModel();
        List<List<Object>> _data = model.getData();
        List<List<Object>> _absData = new ArrayList<>();
        List<List<CellsDimension>> _cellsWidthAndHeight = new ArrayList<>();
        
        for (int row=0; row<model.getMaxOccupiedCellRow(); row++) {
            List<Object> rowData = new ArrayList<>();
            List<CellsDimension> cellsDimensions = new ArrayList<>();
            int rowHeight = table.getRowHeight(row);
            
            for (int col=0; col<model.getMaxOccupiedCellCol(); col++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(col);
                CellsDimension cellsDimension = new CellsDimension();
                cellsDimension.setCellHeight(rowHeight);
                cellsDimension.setCellWidth(tableColumn.getPreferredWidth());
                
                cellsDimensions.add(cellsDimension);
                rowData.add(_data.get(row).get(col));
            }
            
            _cellsWidthAndHeight.add(cellsDimensions);
            _absData.add(rowData);
        }
        // region end
        // saves the data to the invoker of the table
        table.getInvoker().putData(_absData, _cellsWidthAndHeight, table.getOddCells(), 
            TableDefaultCellRenderer.getCellTextStyle(), TableDefaultCellRenderer.getSelectedAll());
    }
    
    private void updateCellBackgroundColor() {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        Color color = fill.getColorImage();

        CellTextStyle.SelectedAll all = TableDefaultCellRenderer.getSelectedAll();
        
        List<Integer> selectAllCol = new ArrayList<>();
        List<Integer> selectAllRow = new ArrayList<>();
        for (int row : rows) {
            for (int col : cols) {
                if (TableDefaultCellRenderer.getCellTextStyle().get(row) != null) {
                    CellTextStyle style = TableDefaultCellRenderer.getCellTextStyle().get(row).get(col);
                    style = style != null ? style : new CellTextStyle();
                    
                    style.setBackgroundColor(color);
                    table.getRenderer().putCellStyles(row, col, style);
                } else {
                    CellTextStyle style = new CellTextStyle();
                    style.setBackgroundColor(color);
                    
                    table.getRenderer().putCellStyles(row, col, style);
                }
                
                if (rows.length == table.getRowCount()) {
                    if (!selectAllCol.contains(col))
                        selectAllCol.add(col);
                }
                if (cols.length == table.getColumnCount()) {
                    if (!selectAllRow.contains(row))
                        selectAllRow.add(row);
                }
            }
        }
        
        for (int col : selectAllCol) {
            all.addSelectedAllCol(col, color);
        }
        for (int row : selectAllRow) {
            all.addSelectedAllRow(row, color);
        }
    }
    
    private void updateCellForegroundColor() {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        Color color = font_color.getColorImage();
        
        for (int row : rows) {
            for (int col : cols) {
                if (TableDefaultCellRenderer.getCellTextStyle().get(row) != null) {
                    CellTextStyle style = TableDefaultCellRenderer.getCellTextStyle().get(row).get(col);
                    style = style != null ? style : new CellTextStyle();
                    
                    style.setFontColor(color);
                    table.getRenderer().putCellStyles(row, col, style);
                } else {
                    CellTextStyle style = new CellTextStyle();
                    style.setFontColor(color);
                    
                    table.getRenderer().putCellStyles(row, col, style);
                }
            }
        }
    }
    
    private void styleText(TextStyles textStyle) {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        
        for (int row : rows) {
            for (int col : cols) {
                CellTextStyle cellStyle;
                if (TableDefaultCellRenderer.getCellTextStyle().get(row) != null) {
                    cellStyle = TableDefaultCellRenderer.getCellTextStyle().get(row).get(col);
                    cellStyle = cellStyle != null ? cellStyle : new CellTextStyle();
                    
                    List<TextStyles> textStyles = cellStyle.getStyles();
                    if (textStyles.isEmpty()) {
                        textStyles.add(textStyle);
                    } else {
                        if (!textStyles.contains(textStyle))
                            textStyles.add(textStyle);
                        else { // if the list already contains the textStyle, we will remove it in the list, to have toggle on/off effect.
                            textStyles.remove(textStyle);
                        }
                    }
                    
                    if (TableDefaultCellRenderer.isHorizontalAlignmentStyle(textStyle)) {
                        cellStyle.setHorizontalAlignment(textStyle);
                    }
                    
                    if (TableDefaultCellRenderer.isVerticalAlignmentStyle(textStyle)) {
                        cellStyle.setVerticalAlignment(textStyle);
                    }
                    
                    cellStyle.setStyles(textStyles);
                    table.getRenderer().putCellStyles(row, col, cellStyle);
                } else {
                    Map<Integer, CellTextStyle> colMap = new HashMap<>();
                    cellStyle = new CellTextStyle();
                    
                    if (!TableDefaultCellRenderer.isHorizontalAlignmentStyle(textStyle) &&
                        !TableDefaultCellRenderer.isVerticalAlignmentStyle(textStyle)) 
                    {
                        List<TextStyles> textStyles = new ArrayList<>();
                        textStyles.add(textStyle);
                        cellStyle.setStyles(textStyles);
                    } else if (TableDefaultCellRenderer.isHorizontalAlignmentStyle(textStyle)) {
                        cellStyle.setHorizontalAlignment(textStyle);
                    } else {
                        cellStyle.setVerticalAlignment(textStyle);
                    }
                    
                    colMap.put(col, cellStyle);
                    table.getRenderer().putCellStyles(row, col, cellStyle);
                }
            }
        }
    }
    
    private void _onPublished() {
        // we don't need to notify the user if the publishing was came
        // from the deletion of workbooks or sheets.
        if (isFromDeletion) {
            isFromDeletion = false;
            return;
        }
        
        notificationPopup = new NotificationPopup("Published!");
        notificationPopup.show(this);
    }
    
    private void _publish(PublishType type) throws JsonProcessingException {
        switch (type) {
            case ONE_SHEET -> {
                TajosMenuTextField sheetComp = table.getInvoker();
                TajosMenuTextField workbookComp = table.getInvoker().getSheetMaster();
                
                Map<String, List<List<Object>>> sheet = new HashMap<>();
                sheet.put(sheetComp.getText(), sheetComp.getData());
                
                mWorkbookManager.publish(PublishType.ONE_SHEET, workbookComp.getText(), sheet);
            }
            case ONE_WORKBOOK -> {
                TajosMenuTextField workbookComp = table.getInvoker().getSheetMaster();
                TajosMenuTextField sheetComp = table.getInvoker();
                
                Map<String, List<List<Object>>> sheetMap = new HashMap<>();
                
                List<Component> sheets = sheetComp.getWorkbookSheetsGroup().getElements();
                for (Component sheet : sheets) {
                    TajosMenuTextField sheetTx = (TajosMenuTextField) sheet;
                    
                    if (sheetTx.getText().equals(MenuManager.SHEET_STR))
                        continue;
                    
                    sheetMap.put(sheetTx.getText(), sheetTx.getData());
                }
                
                mWorkbookManager.publish(PublishType.ONE_WORKBOOK, workbookComp.getText(), sheetMap);
            }
            case ALL_WORKBOOKS -> {
                Map<String, Map<String, List<List<Object>>>> workbookMap = new HashMap<>();
                
                for (Component workbookComponent : mMenuManager.getWorkbookGroup().getElements()) {
                    String workBookName = ((TajosMenuTextField)workbookComponent).getText();
                    
                    if (workBookName.equals(MenuManager.WORKBOOK_STR))
                        continue;
                    
                    Map<String, List<List<Object>>> sheetMap = new HashMap<>();
                    for (Component sheet : ((TajosMenuTextField)workbookComponent).getWorkbookSheetsGroup().getElements()) {
                        TajosMenuTextField sheetTx = (TajosMenuTextField) sheet;

                        if (sheetTx.getText().equals(MenuManager.SHEET_STR))
                            continue;

                        sheetMap.put(sheetTx.getText(), sheetTx.getData());
                    }
                    workbookMap.put(workBookName, sheetMap);
                }
                
                mWorkbookManager.publishAllWorkbooks(workbookMap);
            }

        }
    }
    
    private void updateUI() {
        revalidate();
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TxFieldMenuPopup = new javax.swing.JPopupMenu() {
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,getWidth(), getHeight());
            }
        };
        renameItem = new javax.swing.JMenuItem();
        copyItem = new javax.swing.JMenuItem();
        pasteItem = new javax.swing.JMenuItem();
        deleteItem = new javax.swing.JMenuItem();
        cellPopupMenu = new javax.swing.JPopupMenu() {
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,getWidth(), getHeight());
            }
        };
        copyCell = new javax.swing.JMenuItem();
        cutCell = new javax.swing.JMenuItem();
        pasteOptionCell = new javax.swing.JMenu();
        pasteTextOnly = new javax.swing.JMenuItem();
        formulaPaste = new javax.swing.JMenuItem();
        deleteCell = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(227,219,242));
                g.fillRect(15, getHeight()/2, getWidth()-15, 1);
            }
        };
        shitCellDown = new javax.swing.JMenuItem();
        shiftRowDown = new javax.swing.JMenuItem();
        shiftCellRight = new javax.swing.JMenuItem();
        shiftColRight = new javax.swing.JMenuItem();
        publishPopupMenu = new javax.swing.JPopupMenu() {
            private RoundedShapePath.Corners mCorners;
            private int radius = 30, x, y;
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, publishPopupMenu.getWidth(), publishPopupMenu.getHeight());
            }
        };
        publishOnlySheet = new javax.swing.JMenuItem();
        sep1 = new javax.swing.JPopupMenu.Separator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(227,219,242));
                g.fillRect(15, getHeight()/2, getWidth()-30, 1);
            }
        };
        publishThisWorkbook = new javax.swing.JMenuItem();
        sep2 = new javax.swing.JPopupMenu.Separator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(227,219,242));
                g.fillRect(15, getHeight()/2, getWidth()-30, 1);
            }
        };
        publishAllWorkbooks = new javax.swing.JMenuItem();
        formulaPopup = new javax.swing.JPopupMenu() {
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,getWidth(), getHeight());
            }
        };
        sum = new javax.swing.JMenuItem();
        average = new javax.swing.JMenuItem();
        root = new javax.swing.JPanel();
        loadingPane = new javax.swing.JDesktopPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tajosJLabel7 = new com.tajos.studio.components.TajosJLabel();
        mainPanel = new com.tajos.studio.components.BackgroundPanel();
        toolbar = new com.tajos.studio.graphics.RoundedPanel();
        subToolbar = new com.tajos.studio.graphics.RoundedPanel();
        userProfilePanel = new javax.swing.JPanel();
        userNameLbl = new com.tajos.studio.components.TajosJLabel();
        userProfImg = new com.tajos.studio.components.ImageViewer();
        jPanel1 = new javax.swing.JPanel();
        minimizedButton1 = new com.tajos.studio.components.MinimizedButton();
        maximizedButton2 = new com.tajos.studio.components.MaximizedButton();
        closeButton2 = new com.tajos.studio.components.CloseButton();
        contentRootPanel = new javax.swing.JPanel();
        workbkRootPane = new javax.swing.JDesktopPane();
        ribbonPanel = new com.tajos.studio.graphics.RoundedPanel();
        bold = new com.tajos.studio.components.ImageViewer();
        italic = new com.tajos.studio.components.ImageViewer();
        underline = new com.tajos.studio.components.ImageViewer();
        jSeparator3 = new javax.swing.JSeparator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(235,228,249));
                g.fillRect(0, 0,
                    jSeparator3.getPreferredSize().width,
                    jSeparator3.getPreferredSize().height);
            }
        };
        fillPanel = new com.tajos.studio.graphics.RoundedPanel();
        fill = new com.tajos.studio.components.ImageViewer();
        fillOpenColor = new com.tajos.studio.graphics.RoundedPanel();
        fontPanel = new com.tajos.studio.graphics.RoundedPanel();
        font_color = new com.tajos.studio.components.ImageViewer();
        fontOpenColor = new com.tajos.studio.graphics.RoundedPanel();
        jSeparator4 = new javax.swing.JSeparator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(235,228,249));
                g.fillRect(0, 0,
                    jSeparator3.getPreferredSize().width,
                    jSeparator3.getPreferredSize().height);
            }
        };
        left_align = new com.tajos.studio.components.ImageViewer();
        center_align = new com.tajos.studio.components.ImageViewer();
        jSeparator6 = new javax.swing.JSeparator() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(235,228,249));
                g.fillRect(0, 0,
                    jSeparator3.getPreferredSize().width,
                    jSeparator3.getPreferredSize().height);
            }
        };
        filterPanel = new javax.swing.JPanel();
        filterImg = new com.tajos.studio.components.ImageViewer();
        right_align = new com.tajos.studio.components.ImageViewer();
        formula = new com.tajos.studio.components.ImageViewer();
        formula_dropdown = new com.tajos.studio.components.ImageViewer();
        saveBtn = new com.tajos.studio.graphics.RoundedPanel();
        saveLbl = new com.tajos.studio.components.TajosJLabel();
        menuRootPanel = new com.tajos.studio.graphics.RoundedPanel();
        menuScrollPane = new com.tajos.studio.components.TajosScrollPane();
        menuPanel = new com.tajos.studio.graphics.RoundedPanel();
        wrkbkLabel = new com.tajos.studio.components.TajosJLabel();
        wrkbookSeparator = new javax.swing.JSeparator();
        tableRootPane = new javax.swing.JPanel();
        tableScrollPane = new com.tajos.studio.components.TajosScrollPane();
        table = new com.tajos.studio.components.TajosTable();
        sheetTitlePanel = new javax.swing.JPanel();
        sheetTitleLbl = new com.tajos.studio.components.TajosJLabel();
        publishBtn = new com.tajos.studio.graphics.RoundedPanel();
        publishLbl = new com.tajos.studio.components.TajosJLabel();
        workbookCode = new javax.swing.JTextPane();
        importBtn = new com.tajos.studio.graphics.RoundedPanel();
        importLbl = new com.tajos.studio.components.TajosJLabel();

        TxFieldMenuPopup.setBackground(new java.awt.Color(255, 255, 255));
        TxFieldMenuPopup.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(213, 204, 240), null));

        renameItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        renameItem.setBackground(new java.awt.Color(153, 255, 0));
        renameItem.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        renameItem.setForeground(new java.awt.Color(22, 28, 72));
        renameItem.setMnemonic('R');
        renameItem.setText("Rename");
        TxFieldMenuPopup.add(renameItem);

        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        copyItem.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        copyItem.setForeground(new java.awt.Color(22, 28, 72));
        copyItem.setText("Copy");
        TxFieldMenuPopup.add(copyItem);

        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        pasteItem.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        pasteItem.setForeground(new java.awt.Color(22, 28, 72));
        pasteItem.setText("Paste");
        TxFieldMenuPopup.add(pasteItem);

        deleteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, java.awt.event.InputEvent.ALT_DOWN_MASK));
        deleteItem.setBackground(new java.awt.Color(204, 0, 51));
        deleteItem.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        deleteItem.setForeground(new java.awt.Color(22, 28, 72));
        deleteItem.setText("Delete");
        deleteItem.setToolTipText("You cannot undo this operation");
        TxFieldMenuPopup.add(deleteItem);

        cellPopupMenu.setBackground(new java.awt.Color(255, 255, 255));
        cellPopupMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(213, 204, 240), null));

        copyCell.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        copyCell.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        copyCell.setForeground(new java.awt.Color(22, 28, 72));
        copyCell.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/copy2.png"))); // NOI18N
        copyCell.setText("Copy");
        cellPopupMenu.add(copyCell);

        cutCell.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        cutCell.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        cutCell.setForeground(new java.awt.Color(22, 28, 72));
        cutCell.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cut.png"))); // NOI18N
        cutCell.setText("Cut");
        cellPopupMenu.add(cutCell);

        pasteOptionCell.setForeground(new java.awt.Color(22, 28, 72));
        pasteOptionCell.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/paste.png"))); // NOI18N
        pasteOptionCell.setText("Paste Options");
        pasteOptionCell.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N

        pasteTextOnly.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        pasteTextOnly.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        pasteTextOnly.setForeground(new java.awt.Color(22, 28, 72));
        pasteTextOnly.setText("Cell Content Only");
        pasteTextOnly.setToolTipText("");
        pasteOptionCell.add(pasteTextOnly);

        formulaPaste.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        formulaPaste.setForeground(new java.awt.Color(22, 28, 72));
        formulaPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/formula.png"))); // NOI18N
        formulaPaste.setText("Formulas");
        pasteOptionCell.add(formulaPaste);

        cellPopupMenu.add(pasteOptionCell);

        deleteCell.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteCell.setBackground(new java.awt.Color(204, 0, 51));
        deleteCell.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        deleteCell.setForeground(new java.awt.Color(22, 28, 72));
        deleteCell.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete2.png"))); // NOI18N
        deleteCell.setText("Delete");
        deleteCell.setToolTipText("You cannot undo this operation");
        cellPopupMenu.add(deleteCell);
        cellPopupMenu.add(jSeparator2);

        shitCellDown.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        shitCellDown.setForeground(new java.awt.Color(22, 28, 72));
        shitCellDown.setText("Shift Cell Down");
        cellPopupMenu.add(shitCellDown);

        shiftRowDown.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        shiftRowDown.setForeground(new java.awt.Color(22, 28, 72));
        shiftRowDown.setText("Shift Entire Row Down");
        cellPopupMenu.add(shiftRowDown);

        shiftCellRight.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        shiftCellRight.setForeground(new java.awt.Color(22, 28, 72));
        shiftCellRight.setText("Shift Cell Right");
        cellPopupMenu.add(shiftCellRight);

        shiftColRight.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        shiftColRight.setForeground(new java.awt.Color(22, 28, 72));
        shiftColRight.setText("Shift Entire Column Right");
        cellPopupMenu.add(shiftColRight);

        publishPopupMenu.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(22, 28, 72), 1, true));

        publishOnlySheet.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        publishOnlySheet.setForeground(new java.awt.Color(22, 28, 72));
        publishOnlySheet.setText("Publish");
        publishOnlySheet.setToolTipText("Publish only this table.");
        publishOnlySheet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publishOnlySheetActionPerformed(evt);
            }
        });
        publishPopupMenu.add(publishOnlySheet);
        publishPopupMenu.add(sep1);

        publishThisWorkbook.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        publishThisWorkbook.setForeground(new java.awt.Color(22, 28, 72));
        publishThisWorkbook.setText("Publish");
        publishThisWorkbook.setToolTipText("Publish all the tables in this workbook.");
        publishThisWorkbook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publishThisWorkbookActionPerformed(evt);
            }
        });
        publishPopupMenu.add(publishThisWorkbook);
        publishPopupMenu.add(sep2);

        publishAllWorkbooks.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        publishAllWorkbooks.setForeground(new java.awt.Color(22, 28, 72));
        publishAllWorkbooks.setText("Publish All Workbooks");
        publishAllWorkbooks.setToolTipText("Publish all the workbooks in the hierarchy");
        publishAllWorkbooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publishAllWorkbooksActionPerformed(evt);
            }
        });
        publishPopupMenu.add(publishAllWorkbooks);

        formulaPopup.setBackground(new java.awt.Color(255, 255, 255));
        formulaPopup.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(213, 204, 240), null));

        sum.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        sum.setForeground(new java.awt.Color(22, 28, 72));
        sum.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sum.png"))); // NOI18N
        sum.setText("SUM");
        sum.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sumMousePressed(evt);
            }
        });
        formulaPopup.add(sum);

        average.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        average.setForeground(new java.awt.Color(22, 28, 72));
        average.setText("AVERAGE");
        average.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                averageMousePressed(evt);
            }
        });
        formulaPopup.add(average);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Graede");
        setBackground(new java.awt.Color(0, 204, 204));
        setForeground(new java.awt.Color(0, 255, 153));
        setUndecorated(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        root.setLayout(new javax.swing.OverlayLayout(root));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72), 2));
        jPanel2.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/loading.gif"))); // NOI18N
        jPanel2.add(jLabel2);

        tajosJLabel7.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel7.setText("Preparing your workspace...");
        tajosJLabel7.setDefaultFont(12);
        jPanel2.add(tajosJLabel7);

        loadingPane.setLayer(jPanel2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout loadingPaneLayout = new javax.swing.GroupLayout(loadingPane);
        loadingPane.setLayout(loadingPaneLayout);
        loadingPaneLayout.setHorizontalGroup(
            loadingPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        loadingPaneLayout.setVerticalGroup(
            loadingPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        root.add(loadingPane);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72), 2));
        mainPanel.setVisible(false);
        mainPanel.setCornersRadius(new int[] {30, 30, 0, 0});

        toolbar.setCornersRadius(new int[] {30, 30, 30, 30});
        toolbar.setTransparent(true);
        toolbar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                toolbarMouseDragged(evt);
            }
        });
        toolbar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                toolbarMousePressed(evt);
            }
        });

        subToolbar.setBackground(new java.awt.Color(247, 245, 251));
        subToolbar.setOpaque(false);
        subToolbar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                subToolbarMouseDragged(evt);
            }
        });
        subToolbar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                subToolbarMousePressed(evt);
            }
        });
        subToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 5, 0));

        userProfilePanel.setBackground(new java.awt.Color(255, 255, 255));
        userProfilePanel.setOpaque(false);
        userProfilePanel.setPreferredSize(new java.awt.Dimension(80, 20));
        userProfilePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                userProfilePanelMousePressed(evt);
            }
        });
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout1 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout1.setAdjustableSize(true);
        gravityLayout1.setHorizontalGap(0);
        gravityLayout1.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        userProfilePanel.setLayout(gravityLayout1);

        userNameLbl.setForeground(new java.awt.Color(22, 28, 72));
        userNameLbl.setText("Graede App");
        userNameLbl.setDefaultFont(11);
        userNameLbl.setPreferredSize(new java.awt.Dimension(75, 16));
        userProfilePanel.add(userNameLbl);

        userProfImg.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/res/grading-logo.png"))); // NOI18N
        userProfImg.setImageShape(com.tajos.studio.components.ImageViewer.ImageShape.Circle);
        userProfImg.setImageType(com.tajos.studio.components.ImageViewer.ImageType.COVER);
        userProfImg.setPreferredSize(new java.awt.Dimension(20, 20));
        userProfilePanel.add(userProfImg);

        subToolbar.add(userProfilePanel);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(30, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        subToolbar.add(jPanel1);

        minimizedButton1.attachFrame(this);
        subToolbar.add(minimizedButton1);

        maximizedButton2.attachFrame(this);
        subToolbar.add(maximizedButton2);

        closeButton2.setBackground(new java.awt.Color(51, 0, 255));
        closeButton2.setText("closeButton2");
        subToolbar.add(closeButton2);

        javax.swing.GroupLayout toolbarLayout = new javax.swing.GroupLayout(toolbar);
        toolbar.setLayout(toolbarLayout);
        toolbarLayout.setHorizontalGroup(
            toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(subToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        toolbarLayout.setVerticalGroup(
            toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(subToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        contentRootPanel.setBackground(new java.awt.Color(255, 255, 255));

        workbkRootPane.setBackground(new java.awt.Color(255, 255, 255));

        ribbonPanel.setPreferredSize(new java.awt.Dimension(420, 25));
        ribbonPanel.setRadius(0);
        ribbonPanel.setTransparent(true);
        ribbonPanel.setLayout(null);

        bold.setForeground(new java.awt.Color(22, 28, 72));
        bold.setToolTipText("Bold the cell.");
        bold.setColorImage(new java.awt.Color(22, 28, 72));
        bold.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/bold.png"))); // NOI18N
        bold.setPreferredSize(new java.awt.Dimension(12, 12));
        bold.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                boldMousePressed(evt);
            }
        });
        ribbonPanel.add(bold);
        bold.setBounds(2, 8, 12, 12);

        italic.setToolTipText("Italize the cell.");
        italic.setColorImage(new java.awt.Color(22, 28, 72));
        italic.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/italic.png"))); // NOI18N
        italic.setPreferredSize(new java.awt.Dimension(12, 12));
        italic.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                italicMousePressed(evt);
            }
        });
        ribbonPanel.add(italic);
        italic.setBounds(34, 8, 12, 12);

        underline.setToolTipText("Underline the cell.");
        underline.setColorImage(new java.awt.Color(22, 28, 72));
        underline.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/underline.png"))); // NOI18N
        underline.setPreferredSize(new java.awt.Dimension(12, 12));
        underline.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                underlineMousePressed(evt);
            }
        });
        ribbonPanel.add(underline);
        underline.setBounds(66, 8, 12, 12);

        jSeparator3.setForeground(new java.awt.Color(227, 219, 242));
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setPreferredSize(new java.awt.Dimension(1, 10));
        ribbonPanel.add(jSeparator3);
        jSeparator3.setBounds(98, 9, 1, 10);

        fillPanel.setBackground(new java.awt.Color(204, 204, 204));
        fillPanel.setPreferredSize(new java.awt.Dimension(20, 13));
        fillPanel.setRadius(5);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout13 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout13.setPadding(3);
        gravityLayout13.setAdjustableSize(true);
        fillPanel.setLayout(gravityLayout13);

        fill.setToolTipText("Fill the cell with the chosen color.");
        fill.setColorImage(new java.awt.Color(22, 28, 72));
        fill.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/fill-color.png"))); // NOI18N
        fill.setImageType(com.tajos.studio.components.ImageViewer.ImageType.FILL);
        fill.setPreferredSize(new java.awt.Dimension(15, 13));
        fill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fillMousePressed(evt);
            }
        });

        javax.swing.GroupLayout fillLayout = new javax.swing.GroupLayout(fill);
        fill.setLayout(fillLayout);
        fillLayout.setHorizontalGroup(
            fillLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        fillLayout.setVerticalGroup(
            fillLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        fillPanel.add(fill);

        fillOpenColor.setBackground(new java.awt.Color(22, 28, 72));
        fillOpenColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72)));
        fillOpenColor.setToolTipText("Select a fill color.");
        fillOpenColor.setBorderColor(null);
        fillOpenColor.setPreferredSize(new java.awt.Dimension(8, 13));
        fillOpenColor.setRadius(0);
        fillOpenColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fillOpenColorMousePressed(evt);
            }
        });
        fillOpenColor.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());
        fillPanel.add(fillOpenColor);

        ribbonPanel.add(fillPanel);
        fillPanel.setBounds(119, 5, 20, 13);

        fontPanel.setBackground(new java.awt.Color(204, 204, 204));
        fontPanel.setOpaque(false);
        fontPanel.setPreferredSize(new java.awt.Dimension(20, 13));
        fontPanel.setRadius(5);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout14 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout14.setPadding(3);
        gravityLayout14.setAdjustableSize(true);
        fontPanel.setLayout(gravityLayout14);

        font_color.setToolTipText("Sets the font color.");
        font_color.setColorImage(new java.awt.Color(255, 0, 51));
        font_color.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/font-color.png"))); // NOI18N
        font_color.setImageType(com.tajos.studio.components.ImageViewer.ImageType.COVER);
        font_color.setPreferredSize(new java.awt.Dimension(12, 12));
        font_color.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                font_colorMousePressed(evt);
            }
        });
        fontPanel.add(font_color);

        fontOpenColor.setBackground(new java.awt.Color(255, 0, 51));
        fontOpenColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72)));
        fontOpenColor.setToolTipText("Select a font color.");
        fontOpenColor.setPreferredSize(new java.awt.Dimension(8, 13));
        fontOpenColor.setRadius(0);
        fontOpenColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fontOpenColorMousePressed(evt);
            }
        });
        fontOpenColor.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());
        fontPanel.add(fontOpenColor);

        ribbonPanel.add(fontPanel);
        fontPanel.setBounds(165, 5, 20, 13);

        jSeparator4.setForeground(new java.awt.Color(227, 219, 242));
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setPreferredSize(new java.awt.Dimension(1, 10));
        ribbonPanel.add(jSeparator4);
        jSeparator4.setBounds(208, 9, 1, 10);

        left_align.setToolTipText("Left align");
        left_align.setColorImage(new java.awt.Color(22, 28, 72));
        left_align.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/left-align.png"))); // NOI18N
        left_align.setPreferredSize(new java.awt.Dimension(12, 12));
        left_align.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                left_alignMousePressed(evt);
            }
        });
        ribbonPanel.add(left_align);
        left_align.setBounds(229, 8, 12, 12);

        center_align.setToolTipText("Center align.");
        center_align.setColorImage(new java.awt.Color(22, 28, 72));
        center_align.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/center-align.png"))); // NOI18N
        center_align.setPreferredSize(new java.awt.Dimension(12, 12));
        center_align.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                center_alignMousePressed(evt);
            }
        });
        ribbonPanel.add(center_align);
        center_align.setBounds(261, 8, 12, 12);

        jSeparator6.setForeground(new java.awt.Color(227, 219, 242));
        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setPreferredSize(new java.awt.Dimension(1, 10));
        ribbonPanel.add(jSeparator6);
        jSeparator6.setBounds(325, 9, 1, 10);

        filterPanel.setToolTipText("Filter");
        filterPanel.setOpaque(false);
        filterPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                filterPanelMousePressed(evt);
            }
        });
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout3 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout3.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout3.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        filterPanel.setLayout(gravityLayout3);

        filterImg.setToolTipText("Filter");
        filterImg.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/filter-unfilled.png"))); // NOI18N
        filterImg.setPreferredSize(new java.awt.Dimension(12, 12));
        filterImg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                filterImgMousePressed(evt);
            }
        });

        javax.swing.GroupLayout filterImgLayout = new javax.swing.GroupLayout(filterImg);
        filterImg.setLayout(filterImgLayout);
        filterImgLayout.setHorizontalGroup(
            filterImgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        filterImgLayout.setVerticalGroup(
            filterImgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        filterPanel.add(filterImg);

        ribbonPanel.add(filterPanel);
        filterPanel.setBounds(340, 0, 30, 30);

        right_align.setToolTipText("Right align");
        right_align.setColorImage(new java.awt.Color(22, 28, 72));
        right_align.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/right-align.png"))); // NOI18N
        right_align.setPreferredSize(new java.awt.Dimension(12, 12));
        right_align.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                right_alignMousePressed(evt);
            }
        });
        ribbonPanel.add(right_align);
        right_align.setBounds(293, 8, 12, 12);

        formula.setToolTipText("Formula");
        formula.setColorImage(new java.awt.Color(22, 28, 72));
        formula.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/formula.png"))); // NOI18N
        formula.setPreferredSize(new java.awt.Dimension(12, 12));
        formula.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formulaMousePressed(evt);
            }
        });
        ribbonPanel.add(formula);
        formula.setBounds(380, 8, 20, 12);

        formula_dropdown.setToolTipText("");
        formula_dropdown.setColorImage(new java.awt.Color(22, 28, 72));
        formula_dropdown.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/dropdown.png"))); // NOI18N
        formula_dropdown.setImageType(com.tajos.studio.components.ImageViewer.ImageType.FILL);
        formula_dropdown.setPreferredSize(new java.awt.Dimension(12, 12));
        formula_dropdown.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formula_dropdownMousePressed(evt);
            }
        });
        ribbonPanel.add(formula_dropdown);
        formula_dropdown.setBounds(400, 10, 10, 10);

        saveBtn.setBackground(new java.awt.Color(247, 245, 251));
        saveBtn.setToolTipText("Saves all the workbooks,\nand it's corresponding tables.");
        saveBtn.setRadius(10);
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtnMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveBtnMousePressed(evt);
            }
        });
        saveBtn.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        saveLbl.setForeground(new java.awt.Color(22, 28, 72));
        saveLbl.setText("Save");
        saveLbl.setDefaultFont(10);
        saveBtn.add(saveLbl);

        menuRootPanel.setBackground(new java.awt.Color(247, 245, 251));
        menuRootPanel.setCornersRadius(new int[] {0, 30, 30, 0});

        menuScrollPane.setBorder(null);
        menuScrollPane.getViewport().setBackground(new Color(247,245,251));
        menuScrollPane.setEnableScrollBarHiding(true);

        menuPanel.setBackground(new java.awt.Color(247, 245, 251));
        menuPanel.setBorderEnabled(false);
        menuPanel.setPreferredSize(new java.awt.Dimension(0, 100));
        com.tajos.studio.layoutmanagers.ListViewLayout listViewLayout1 = new com.tajos.studio.layoutmanagers.ListViewLayout();
        listViewLayout1.setFixHeight(false);
        listViewLayout1.setFixWidth(false);
        menuPanel.setLayout(listViewLayout1);
        menuScrollPane.setViewportView(menuPanel);

        wrkbkLabel.setBackground(new java.awt.Color(247, 245, 251));
        wrkbkLabel.setForeground(new java.awt.Color(22, 28, 72));
        wrkbkLabel.setText("Workbooks");
        wrkbkLabel.setBoldDefaultFont(14);
        wrkbkLabel.setOpaque(true);

        wrkbookSeparator.setBackground(new Color(0,0,0,0));
        wrkbookSeparator.setForeground(new java.awt.Color(227, 219, 242));
        wrkbookSeparator.setPreferredSize(new java.awt.Dimension(5, 3));

        javax.swing.GroupLayout menuRootPanelLayout = new javax.swing.GroupLayout(menuRootPanel);
        menuRootPanel.setLayout(menuRootPanelLayout);
        menuRootPanelLayout.setHorizontalGroup(
            menuRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, menuRootPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(wrkbkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(menuRootPanelLayout.createSequentialGroup()
                .addComponent(menuScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(menuRootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wrkbookSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        menuRootPanelLayout.setVerticalGroup(
            menuRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, menuRootPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(wrkbkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(wrkbookSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(menuScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableRootPane.setBackground(new java.awt.Color(255, 255, 255));

        tableScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(227, 219, 242)));
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tableScrollPaneMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tableScrollPaneMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableScrollPaneMousePressed(evt);
            }
        });

        table.setBackground(new java.awt.Color(255, 255, 255));
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        table.setGridColor(new java.awt.Color(227, 219, 242));
        table.setRowHeight(23);
        tableScrollPane.setViewportView(table);

        javax.swing.GroupLayout tableRootPaneLayout = new javax.swing.GroupLayout(tableRootPane);
        tableRootPane.setLayout(tableRootPaneLayout);
        tableRootPaneLayout.setHorizontalGroup(
            tableRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableRootPaneLayout.createSequentialGroup()
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        tableRootPaneLayout.setVerticalGroup(
            tableRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableRootPaneLayout.createSequentialGroup()
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                .addContainerGap())
        );

        sheetTitlePanel.setBackground(new java.awt.Color(255, 102, 153));
        sheetTitlePanel.setOpaque(false);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout11 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout11.setAdjustableSize(true);
        gravityLayout11.setLeftPadding(30);
        sheetTitlePanel.setLayout(gravityLayout11);

        sheetTitleLbl.setForeground(new java.awt.Color(22, 28, 72));
        sheetTitleLbl.setText("Hi!");
        sheetTitleLbl.setDefaultFont(18);
        sheetTitleLbl.setPreferredSize(new java.awt.Dimension(140, 23));
        sheetTitlePanel.add(sheetTitleLbl);

        publishBtn.setBackground(new java.awt.Color(247, 245, 251));
        publishBtn.setToolTipText("Publish your work so students can see their grades.");
        publishBtn.setRadius(10);
        publishBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                publishBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                publishBtnMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                publishBtnMousePressed(evt);
            }
        });
        publishBtn.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        publishLbl.setForeground(new java.awt.Color(22, 28, 72));
        publishLbl.setText("Publish");
        publishLbl.setDefaultFont(10);
        publishBtn.add(publishLbl);

        workbookCode.setEditable(false);
        workbookCode.setBackground(new java.awt.Color(255, 255, 255));
        workbookCode.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 10)); // NOI18N
        workbookCode.setForeground(new java.awt.Color(22, 28, 72));
        workbookCode.setText("Workspace Code:");
        workbookCode.setToolTipText("Click to copy workspace code");
        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_RIGHT);
        workbookCode.setParagraphAttributes(attribs, false);
        workbookCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                workbookCodeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                workbookCodeMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                workbookCodeMousePressed(evt);
            }
        });

        importBtn.setBackground(new java.awt.Color(247, 245, 251));
        importBtn.setToolTipText("Import Excel Workbook");
        importBtn.setRadius(10);
        importBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                importBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                importBtnMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                importBtnMousePressed(evt);
            }
        });
        importBtn.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        importLbl.setForeground(new java.awt.Color(22, 28, 72));
        importLbl.setText("Import");
        importLbl.setDefaultFont(10);
        importBtn.add(importLbl);

        workbkRootPane.setLayer(ribbonPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(saveBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(menuRootPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(tableRootPane, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(sheetTitlePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(publishBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(workbookCode, javax.swing.JLayeredPane.DEFAULT_LAYER);
        workbkRootPane.setLayer(importBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout workbkRootPaneLayout = new javax.swing.GroupLayout(workbkRootPane);
        workbkRootPane.setLayout(workbkRootPaneLayout);
        workbkRootPaneLayout.setHorizontalGroup(
            workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workbkRootPaneLayout.createSequentialGroup()
                .addComponent(menuRootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableRootPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, workbkRootPaneLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ribbonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(93, 93, 93)
                        .addComponent(importBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(publishBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(workbkRootPaneLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(sheetTitlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 495, Short.MAX_VALUE)
                        .addComponent(workbookCode, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        workbkRootPaneLayout.setVerticalGroup(
            workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(workbkRootPaneLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(menuRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(workbkRootPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(publishBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(importBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ribbonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(workbkRootPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sheetTitlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workbookCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tableRootPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout contentRootPanelLayout = new javax.swing.GroupLayout(contentRootPanel);
        contentRootPanel.setLayout(contentRootPanelLayout);
        contentRootPanelLayout.setHorizontalGroup(
            contentRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workbkRootPane)
        );
        contentRootPanelLayout.setVerticalGroup(
            contentRootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workbkRootPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(contentRootPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(contentRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        root.add(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void subToolbarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_subToolbarMouseDragged
        final int x = evt.getXOnScreen()-mouseX;
        final int y = evt.getYOnScreen()-mouseY;
        this.setLocation(x, y);
        
        
    }//GEN-LAST:event_subToolbarMouseDragged
    private int mouseX, mouseY;
    private void subToolbarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_subToolbarMousePressed
        _hideUserProfilePopup();
        mouseX = evt.getX(); mouseY = evt.getY();
    }//GEN-LAST:event_subToolbarMousePressed
    private boolean selectAllCells = false;
    private void tableScrollPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableScrollPaneMousePressed
        if (table.isDisable())
            return;
        
        int rowHeaderWidth = tableScrollPane.getRowHeader().getWidth();
        int colHeaderHeight = tableScrollPane.getColumnHeader().getHeight();
        
        // upper left of the scrollpane
        if (!(evt.getX() > rowHeaderWidth) && !(evt.getY() > colHeaderHeight)) {
            selectAllCells = !selectAllCells;
            table.selectAllCells(selectAllCells, false);
            table.revalidate();
            table.repaint();
        }
    }//GEN-LAST:event_tableScrollPaneMousePressed

    private void toolbarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMousePressed
        _hideUserProfilePopup();
        mouseX = evt.getX(); mouseY = evt.getY();
    }//GEN-LAST:event_toolbarMousePressed

    private void toolbarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseDragged
        final int x = evt.getXOnScreen()-mouseX;
        final int y = evt.getYOnScreen()-mouseY;
        this.setLocation(x, y); 
    }//GEN-LAST:event_toolbarMouseDragged

    private void tableScrollPaneMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableScrollPaneMouseEntered
        if (table.isDisable())
            return;

        int rowHeaderWidth = tableScrollPane.getRowHeader().getWidth();
        int colHeaderHeight = tableScrollPane.getColumnHeader().getHeight();
        
        // upper left of the scrollpane
        if (!(evt.getX() > rowHeaderWidth) && !(evt.getY() > colHeaderHeight)) {
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
        }
    }//GEN-LAST:event_tableScrollPaneMouseEntered

    private void tableScrollPaneMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableScrollPaneMouseExited
        if (table.isDisable())
            return;
        
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_tableScrollPaneMouseExited

    private void userProfilePanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userProfilePanelMousePressed
        if (mUserProfilePopupMenu.isShowing())
            return;
        
        int x = getWidth() - mUserProfilePopupMenu.getWidth();
        mUserProfilePopupMenu.show(this, x, subToolbar.getHeight());
    }//GEN-LAST:event_userProfilePanelMousePressed

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        _hideUserProfilePopup();
    }//GEN-LAST:event_formMousePressed

    private void saveBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMousePressed
        if (table.isDisable() || table.isEditing())
            return;
        
        save();
    }//GEN-LAST:event_saveBtnMousePressed

    private void saveBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseEntered
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBackground(GradeUtils.Colors.darkBlueColor);
        saveLbl.setForeground(Color.WHITE);
        updateUI();
    }//GEN-LAST:event_saveBtnMouseEntered

    private void saveBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseExited
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(Cursor.getDefaultCursor());
        saveBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        saveLbl.setForeground(GradeUtils.Colors.darkBlueColor);
        
        updateUI();
    }//GEN-LAST:event_saveBtnMouseExited

    private void fontOpenColorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fontOpenColorMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }

        Color newColor = JColorChooser.showDialog(
            new JFrame(),
            "Colors",
            Color.WHITE);

        if (newColor != null) {
            font_color.setColorImage(newColor);
            fontOpenColor.setBackground(newColor);
            table.getInvoker().setIsSavedState(false);
            table.fireCellDataChanged();
            updateCellForegroundColor();
        }

        table.updateTable();
    }//GEN-LAST:event_fontOpenColorMousePressed

    private void font_colorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_font_colorMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        updateCellForegroundColor();
        table.updateTable();
    }//GEN-LAST:event_font_colorMousePressed

    private void fillOpenColorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fillOpenColorMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }

        Color newColor = JColorChooser.showDialog(
            new JFrame(),
            "Colors",
            Color.WHITE);

        if (newColor != null) {
            fill.setColorImage(newColor);
            fillOpenColor.setBackground(newColor);
            table.getInvoker().setIsSavedState(false);
            table.fireCellDataChanged();
            updateCellBackgroundColor();
        }
        table.updateTable();
    }//GEN-LAST:event_fillOpenColorMousePressed

    private void fillMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fillMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged(); // always fire the data changed to save this current state.
        updateCellBackgroundColor();
        table.updateTable();
    }//GEN-LAST:event_fillMousePressed

    private void underlineMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_underlineMousePressed
        _underline();
    }//GEN-LAST:event_underlineMousePressed

    private void italicMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_italicMousePressed
        _italic();
    }//GEN-LAST:event_italicMousePressed

    private void boldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boldMousePressed
        _bold();
    }//GEN-LAST:event_boldMousePressed
    
    private void _bold() {
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        styleText(TextStyles.BOLD);
        table.updateTable();
    }
    
    private void _italic() {
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        styleText(TextStyles.ITALIC);
        table.updateTable();
    }
    
    private void _underline() {
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        styleText(TextStyles.UNDERLINED);
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        table.updateTable();
    }
    
    private void left_alignMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_left_alignMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        styleText(TextStyles.LEFT_ALIGN);
        table.updateTable();
    }//GEN-LAST:event_left_alignMousePressed

    private void formula_dropdownMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formula_dropdownMousePressed
        formulaPopup.show(formula_dropdown, 0, formula_dropdown.getSize().height);
    }//GEN-LAST:event_formula_dropdownMousePressed

    private void center_alignMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_center_alignMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        styleText(TextStyles.CENTER_ALIGN);
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        table.updateTable();
    }//GEN-LAST:event_center_alignMousePressed
    
    private void publishBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_publishBtnMouseEntered
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        publishBtn.setBackground(GradeUtils.Colors.darkBlueColor);
        publishLbl.setForeground(Color.WHITE);
        updateUI();
    }//GEN-LAST:event_publishBtnMouseEntered

    private void publishBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_publishBtnMouseExited
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(Cursor.getDefaultCursor());
        publishBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        publishLbl.setForeground(GradeUtils.Colors.darkBlueColor);
        updateUI();
    }//GEN-LAST:event_publishBtnMouseExited

    private void publishBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_publishBtnMousePressed
        if (table.isDisable() || table.isEditing())
            return;
        
        publishOnlySheet.setText("Publish " + table.getInvoker().getText());
        publishThisWorkbook.setText("Publish " + table.getInvoker().getSheetMaster().getText());
        publishPopupMenu.setMaximumSize(new Dimension(publishPopupMenu.getPreferredSize().width, publishPopupMenu.getPreferredSize().height));
        publishPopupMenu.show(publishBtn, 0, publishBtn.getHeight());
    }//GEN-LAST:event_publishBtnMousePressed

    private void publishOnlySheetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishOnlySheetActionPerformed
        if (!table.getInvoker().isSaved()) {
            JOptionPane.showMessageDialog(new JFrame(), "Oops! You need to save this table first before publishing.");
            return;
        }
        
        try {
            _publish(PublishType.ONE_SHEET);
        } catch (JsonProcessingException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }//GEN-LAST:event_publishOnlySheetActionPerformed

    private void publishThisWorkbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishThisWorkbookActionPerformed
        if (!table.getInvoker().isSaved()) {
            JOptionPane.showMessageDialog(new JFrame(), "Oops! You need to save this table first before publishing.");
            return;
        }
        
        try {
            _publish(PublishType.ONE_WORKBOOK);
        } catch (JsonProcessingException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }//GEN-LAST:event_publishThisWorkbookActionPerformed

    private void publishAllWorkbooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishAllWorkbooksActionPerformed
        if (!table.getInvoker().isSaved()) {
            JOptionPane.showMessageDialog(new JFrame(), "Oops! You need to save this table first before publishing.");
            return;
        }
        
        try {
            _publish(PublishType.ALL_WORKBOOKS);
        } catch (JsonProcessingException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }//GEN-LAST:event_publishAllWorkbooksActionPerformed

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                tableScrollPane.fireStateChanged();
                timer.cancel();
            }
        };
        timer.schedule(task, 50);
    }//GEN-LAST:event_formWindowStateChanged

    private void workbookCodeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workbookCodeMousePressed
        String workbkCode = workbookCode.getText().split(":")[1];
        StringSelection stringSelection = new StringSelection(workbkCode);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        
        notificationPopup = new NotificationPopup("Workspace Code Copied!");
        notificationPopup.show(this);
    }//GEN-LAST:event_workbookCodeMousePressed

    private void workbookCodeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workbookCodeMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_workbookCodeMouseEntered

    private void workbookCodeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_workbookCodeMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_workbookCodeMouseExited

    private void importBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importBtnMouseEntered
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        importBtn.setBackground(GradeUtils.Colors.darkBlueColor);
        importLbl.setForeground(Color.WHITE);
        updateUI();
    }//GEN-LAST:event_importBtnMouseEntered

    private void importBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importBtnMouseExited
        if (table.isDisable() || table.isEditing())
            return;
        
        setCursor(Cursor.getDefaultCursor());
        importBtn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        importLbl.setForeground(GradeUtils.Colors.darkBlueColor);
        updateUI();
    }//GEN-LAST:event_importBtnMouseExited

    private void importBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importBtnMousePressed
       if (table.isDisable() || table.isEditing())
           return;
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new ExcelFilesFilter());
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            File file = fc.getSelectedFile();
            String extension = file.getName().split("\\.")[1];
            ExcelHandler.ExtensionType extType = extension.equals("xlsx") ?
                   ExcelHandler.ExtensionType.XLSX : ExcelHandler.ExtensionType.XLS;
            
            ImportingProgressDialog importingDialog = new ImportingProgressDialog(this, true, 
                    new ExcelHandler(file).setExcelFileExtension(extType));
            
            importingDialog.setOnImportingListener((data, success) -> {
                // on importing finish.
                if (!success) {
                    importingDialog.setVisible(false);
                    importingDialog.dispose();
                    return;
                }
                
                boolean isFirstInit = true;
                for (Map.Entry<String, Map<String, List<List<Object>>>> entry : data.entrySet()) {
                    String workbookName = entry.getKey();
                    Map<String, List<List<Object>>> sheets = entry.getValue();
                    // add the workbook to the menu
                    MenuTxFieldGroup grp = mMenuManager.getWorkbookGroup();
                    List<Component> listOfComps = grp.getElements();
                    TajosMenuTextField lastWorkbook = (TajosMenuTextField) listOfComps.get(listOfComps.size()-1);
                    lastWorkbook.setText(workbookName);
                    lastWorkbook.setForeground(GradeUtils.Colors.darkBlueColor);
                    // end
                    for (Map.Entry<String, List<List<Object>>> entry2 : sheets.entrySet()) {
                        String sheetName = entry2.getKey();
                        List<List<Object>> cellsData = entry2.getValue();
                        // add the sheet to the menu
                        TajosMenuTextField sheetComp = mMenuManager.addNewSheet(-1, true, lastWorkbook);
                        sheetComp.setText(sheetName);
                        sheetComp.setForeground(GradeUtils.Colors.darkBlueColor);
                        sheetComp.putData(cellsData, null,
                                null, new HashMap<>(), null);

                        if (isFirstInit) {
                            mWorkbookManager.openTable(sheetComp);
                            sheetComp.getGroup().setSelected(sheetComp, true, false);
                            isFirstInit = false;
                        }
                        // end
                    }

                    mMenuManager.addNewSheet(-1, true, lastWorkbook);
                    mMenuManager.addNewWorkbook(true);
                }
                importingDialog.setVisible(false);
                importingDialog.dispose();
            });
            
            importingDialog.setVisible(true);
        }
    }//GEN-LAST:event_importBtnMousePressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Graede");
    }//GEN-LAST:event_formWindowActivated

    private void filterPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterPanelMousePressed
        _filter();
    }//GEN-LAST:event_filterPanelMousePressed

    private void filterImgMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterImgMousePressed
        _filter();
    }//GEN-LAST:event_filterImgMousePressed

    private void right_alignMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_right_alignMousePressed
        if (table.isEditing() || table.isDisable()) {
            return;
        }
        
        table.getInvoker().setIsSavedState(false);
        table.fireCellDataChanged();
        styleText(TextStyles.RIGHT_ALIGN);
        table.updateTable();
    }//GEN-LAST:event_right_alignMousePressed

    private void formulaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formulaMousePressed
        if (table.isDisable()) {
            return;
        }
        
        if (mLastSpecialOperator == null)
            mLastSpecialOperator = "SUM()";
        
        _addSpecialOperator(mLastSpecialOperator);
    }//GEN-LAST:event_formulaMousePressed

    private void sumMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sumMousePressed
        _addSpecialOperator("SUM()");
        
    }//GEN-LAST:event_sumMousePressed

    private void averageMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_averageMousePressed
        _addSpecialOperator("AVERAGE()");
    }//GEN-LAST:event_averageMousePressed
    
    private void _addSpecialOperator(String op) {
        if (table.isDisable())
            return;
        
        if (!table.isEditing()) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            
            boolean success = table.editCellAt(row, col);
            if (success) {
                table.changeSelection(row, col, 
                        false, false);
            }
        }
        
        mLastSpecialOperator = op;
        TableCellEditor editor = (TableCellEditor) table.getDefaultEditor(Object.class);
        String editorTx = editor.getEditorRenderer().getText();
        
        // if the editor is empty just concat the special op directly
        if (!(editorTx.length() > 0)) {
            editorTx = editorTx.concat("=").concat(op);
            editor.getEditorRenderer().setText(editorTx);
            return;
        }
        
        if (Formulator.isAnOperator(editorTx.charAt(editorTx.length()-1))) {
            editorTx = editorTx.concat(op);
            editor.getEditorRenderer().setText(editorTx);
        } else {
            int lastOperatorIndex = 0;
            for (int i=editorTx.length()-1; i > -1; i--) {
                if (Formulator.isAnOperator(editorTx.charAt(i))) {
                    lastOperatorIndex = i;
                    break;
                }
            }
            StringBuilder b = new StringBuilder();
            b.append(editorTx, 0, ++lastOperatorIndex);
            b.append(op);
            
            editor.getEditorRenderer().setText(b.toString());
        }
    }
    
    private void _filter() {
        if (table.isDisable() || table.isEditing())
            return;
        
        final int [] selectedCols = table.getSelectedColumns();
        final int [] selectedRows = table.getSelectedRows();
        
        if (!table.getFilterData().isEmpty()) {
            
            table.getFilterData().clear();
            filterImg.setImageResource(new ImageIcon(WorkBookActivity.class.getResource("/icons/filter-unfilled.png")));
            
            filterPanel.revalidate();
            filterPanel.repaint();
            table.revalidate();
            table.repaint();
            return;
        }
        
        if (selectedRows.length != table.getRowCount()) {
            JOptionPane.showMessageDialog(new JFrame(), 
                    "Please select a column to filter");
            
            return;
        } else if (selectedCols.length != 1) {
            JOptionPane.showMessageDialog(new JFrame(), 
                    "Please select only one column to filter");
            
            return;
        }
        
        new FilterDialog(this, true, selectedCols[0], 
                table.getTableData(), mFilteringListener)
                .setVisible(true);
    }
    
    int t = 0;    int prevX = -1;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu TxFieldMenuPopup;
    private javax.swing.JMenuItem average;
    private com.tajos.studio.components.ImageViewer bold;
    private javax.swing.JPopupMenu cellPopupMenu;
    private com.tajos.studio.components.ImageViewer center_align;
    private com.tajos.studio.components.CloseButton closeButton2;
    private javax.swing.JPanel contentRootPanel;
    private javax.swing.JMenuItem copyCell;
    private javax.swing.JMenuItem copyItem;
    private javax.swing.JMenuItem cutCell;
    private javax.swing.JMenuItem deleteCell;
    private javax.swing.JMenuItem deleteItem;
    private com.tajos.studio.components.ImageViewer fill;
    private com.tajos.studio.graphics.RoundedPanel fillOpenColor;
    private com.tajos.studio.graphics.RoundedPanel fillPanel;
    private com.tajos.studio.components.ImageViewer filterImg;
    private javax.swing.JPanel filterPanel;
    private com.tajos.studio.graphics.RoundedPanel fontOpenColor;
    private com.tajos.studio.graphics.RoundedPanel fontPanel;
    private com.tajos.studio.components.ImageViewer font_color;
    private com.tajos.studio.components.ImageViewer formula;
    private javax.swing.JMenuItem formulaPaste;
    private javax.swing.JPopupMenu formulaPopup;
    private com.tajos.studio.components.ImageViewer formula_dropdown;
    private com.tajos.studio.graphics.RoundedPanel importBtn;
    private com.tajos.studio.components.TajosJLabel importLbl;
    private com.tajos.studio.components.ImageViewer italic;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator6;
    private com.tajos.studio.components.ImageViewer left_align;
    private javax.swing.JDesktopPane loadingPane;
    private com.tajos.studio.components.BackgroundPanel mainPanel;
    private com.tajos.studio.components.MaximizedButton maximizedButton2;
    private com.tajos.studio.graphics.RoundedPanel menuPanel;
    private com.tajos.studio.graphics.RoundedPanel menuRootPanel;
    private com.tajos.studio.components.TajosScrollPane menuScrollPane;
    private com.tajos.studio.components.MinimizedButton minimizedButton1;
    private javax.swing.JMenuItem pasteItem;
    private javax.swing.JMenu pasteOptionCell;
    private javax.swing.JMenuItem pasteTextOnly;
    private javax.swing.JMenuItem publishAllWorkbooks;
    private com.tajos.studio.graphics.RoundedPanel publishBtn;
    private com.tajos.studio.components.TajosJLabel publishLbl;
    private javax.swing.JMenuItem publishOnlySheet;
    private javax.swing.JPopupMenu publishPopupMenu;
    private javax.swing.JMenuItem publishThisWorkbook;
    private javax.swing.JMenuItem renameItem;
    private com.tajos.studio.graphics.RoundedPanel ribbonPanel;
    private com.tajos.studio.components.ImageViewer right_align;
    private javax.swing.JPanel root;
    private com.tajos.studio.graphics.RoundedPanel saveBtn;
    private com.tajos.studio.components.TajosJLabel saveLbl;
    private javax.swing.JPopupMenu.Separator sep1;
    private javax.swing.JPopupMenu.Separator sep2;
    private com.tajos.studio.components.TajosJLabel sheetTitleLbl;
    private javax.swing.JPanel sheetTitlePanel;
    private javax.swing.JMenuItem shiftCellRight;
    private javax.swing.JMenuItem shiftColRight;
    private javax.swing.JMenuItem shiftRowDown;
    private javax.swing.JMenuItem shitCellDown;
    private com.tajos.studio.graphics.RoundedPanel subToolbar;
    private javax.swing.JMenuItem sum;
    private com.tajos.studio.components.TajosTable table;
    private javax.swing.JPanel tableRootPane;
    private com.tajos.studio.components.TajosScrollPane tableScrollPane;
    private com.tajos.studio.components.TajosJLabel tajosJLabel7;
    private com.tajos.studio.graphics.RoundedPanel toolbar;
    private com.tajos.studio.components.ImageViewer underline;
    private com.tajos.studio.components.TajosJLabel userNameLbl;
    private com.tajos.studio.components.ImageViewer userProfImg;
    private javax.swing.JPanel userProfilePanel;
    private javax.swing.JDesktopPane workbkRootPane;
    private javax.swing.JTextPane workbookCode;
    private com.tajos.studio.components.TajosJLabel wrkbkLabel;
    private javax.swing.JSeparator wrkbookSeparator;
    // End of variables declaration//GEN-END:variables
}