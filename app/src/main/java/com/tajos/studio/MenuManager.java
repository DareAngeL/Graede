package com.tajos.studio;

import com.tajos.studio.layoutmanagers.ListViewLayout;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.interfaces.MenuTxFieldModel;
import com.tajos.studio.action.MenuRenamedState;
import com.tajos.studio.action.StateManager;
import com.tajos.studio.activities.WorkBookActivity;
import com.tajos.studio.components.TajosMenuTextField;
import com.tajos.studio.components.TajosScrollPane;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 *
 * @author Rene Tajos Jr.
 */
public class MenuManager implements KeyBinds {
    
    private final Color mBgColor = new Color(247,245,251);
    private final Color mTextColor = new Color(150,155,191);
    
    public static String WORKBOOK_STR = "Add Workbook...";
    public static String SHEET_STR = "Add Table...";
    
    public static TajosMenuTextField mPrevSelectedSheet;
    
    private RoundedPanel mMenuPanel;
    private int mWorkBookCount = 0;
    private int mSheetCount = 0;
    private int mUntitledCount = 0;
    
    private boolean isFirstInit = true;
    private JPopupMenu mPopupMenu;
    private final MenuTxFieldGroup mSheetGroup; // the group of all sheets lbl from all workbooks lbl
    private final WorkBookActivity mWorkbookActivity;
    private final MenuTxFieldGroup mWorkbookGroup; // the group of all workbooks lbl
    private MenuTxFieldGroup mModelGroup;
    private TajosMenuTextField mInvoker;
    private TajosMenuTextField mCopyInvoker;
    
    private final StateManager mStateManager;
    
    private final MouseAdapter menuPanelMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            mMenuPanel.requestFocusInWindow();
            mMenuPanel.setBorderEnabled(true);
            mMenuPanel.setBorderSize(2);
            mMenuPanel.setBorderColor(mTextColor);
            
            mMenuPanel.repaint();
            mMenuPanel.revalidate();
        }
    };
    
    private final FocusAdapter focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
           mMenuPanel.setBorderEnabled(false);
            
            mMenuPanel.repaint();
            mMenuPanel.revalidate();
        }
    };
    
    private OnDeletionListener deletionListener;
    public interface OnDeletionListener {
        void onDeleteSuccess(TajosMenuTextField invoker, TajosMenuTextField.TxtFieldType invokerType);
    }
    private SheetTxFieldListener sheetTxFieldListener;
    public interface SheetTxFieldListener {
        void onEnterKeyPressed();
        void onFocusLost();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // if ctrl + z is clicked, undo the current state!
        if (CTRL_Z(e)) {
            mStateManager.undo();
        } else if (CTRL_Y(e)) { // else redo!
            mStateManager.redo();
        }
    }
    
    public MenuManager(RoundedPanel menuPanel, StateManager stateManager, WorkBookActivity wrkbkActivity) {
        mMenuPanel = menuPanel;
        mStateManager = stateManager;
        mWorkbookActivity = wrkbkActivity;
        
        mMenuPanel.addMouseListener(menuPanelMouseListener);
        mMenuPanel.addKeyListener(this);
        mMenuPanel.addFocusListener(focusListener);
        
        mSheetGroup = new MenuTxFieldGroup();
        mWorkbookGroup = new MenuTxFieldGroup();
    }
    
    /**
     * @Description initialize this {@MenuManager manager} for the first time
     */
    public void initialize() {
        addNewWorkbook(false);
    }
    
    /**
     * @Description this will show the {@Popup menu} for the menuPanel.
     * @param c the component of where the popup should be shown
     * @param x the {@coordinate x} for the popup menu to be shown
     * @param y the {@coordinate y} for the popup menu to be shown
     */
    public void showPopupMenu(MenuTxFieldModel c, int x, int y) {
        mModelGroup = c.getGroup();
        mInvoker = (TajosMenuTextField) c;
        mPopupMenu.show((Component)c, x, y);
    }
    
    /**
     * This will be called every time the user wants to add a new workbook
     * @param isAddCount {@code true} if there's a new workbook added, {@code false} if there's none.
     * @return 
     */
    public TajosMenuTextField addNewWorkbook(boolean isAddCount) {
        final TajosMenuTextField wrkbk = new TajosMenuTextField();
        
        if (isAddCount)
            ++mWorkBookCount;
        
        wrkbk.attachMenuManager(this);
        wrkbk.setPreferredSize(new Dimension(140-TajosScrollPane.getThumbTrackSize(), 18));
        wrkbk.setText(WORKBOOK_STR);
        wrkbk.setBorder(null);
        wrkbk.setEditable(false);
        wrkbk.setBackground(mBgColor);
        wrkbk.setForeground(mTextColor);
        wrkbk.setDefaultFont(12);
        wrkbk.setMarginTop(10);
        wrkbk.setMarginBottom(10);
        wrkbk.setMarginLeft(20);
        wrkbk.setType(TajosMenuTextField.TxtFieldType.WORKBOOK);
        wrkbk.setWorkbookSheetsGroup(wrkbk.new WorkbookSheetsGroup(mMenuPanel));
        
        JSeparator separator = null;
        if (!isFirstInit) {
            separator = new JSeparator(JSeparator.HORIZONTAL);
            separator.setForeground(new Color(213,204,240));
            separator.setBackground(new Color(0,0,0,0));
            final int separatorWidth = mMenuPanel.getWidth() - TajosScrollPane.getThumbTrackSize() * 2;
            separator.setPreferredSize(new Dimension(separatorWidth, 5));
            separator.setLocation(TajosScrollPane.getThumbTrackSize(), separator.getY());
        }
        
        isFirstInit = false;
        
        if (separator != null)
            mMenuPanel.add(separator);
        
        mWorkbookGroup.add(wrkbk);
        mMenuPanel.add(wrkbk);
        
        mMenuPanel.updateUI();
        mMenuPanel.revalidate();
        
        return wrkbk;
    }
    
    /**
     * This will be called every time the user wants to add a new sheet
     * @param pos the {@code index position} of where the sheet should be added. -1 if you want to add it in the last position
     * @param isAddCount {@code true} if there's a new sheet added, {@code false} if there's none.
     * @param sheetMaster The Workbook instance of where the new sheet should be under.
     * @return 
     */
    public TajosMenuTextField addNewSheet(int pos, boolean isAddCount, TajosMenuTextField sheetMaster) {
        final RoundedPanel textFldPanel = new RoundedPanel();
        
        textFldPanel.setCornersRadius(new int[] {0, 30, 0, 30});
        textFldPanel.setPreferredSize(new Dimension(140, 16));
        textFldPanel.setBackground(mBgColor);
        textFldPanel.setLayout(new ListViewLayout(true, false));

        final TajosMenuTextField sheet = new TajosMenuTextField();
        
         if (isAddCount)
            ++mSheetCount;
        
        sheet.attachMenuManager(this);
        sheet.setSheetTypeListener(sheetTxFieldListener);
        sheet.setForeground(mTextColor);
        sheet.setBackground(mBgColor);
        sheet.setEditable(false);
        sheet.setBorder(null);
        sheet.setText(SHEET_STR);
        sheet.setDefaultFont(11);
        sheet.setMarginBottom(5);
        sheet.setMarginLeft(40);
        sheet.setMarginTop(5);
        sheet.setPreferredSize(new Dimension(140-40-5, 16));
        sheet.setType(TajosMenuTextField.TxtFieldType.SHEET);
        sheet.setSheetMaster(sheetMaster);
        sheetMaster.getWorkbookSheetsGroup().add(sheet);
        sheet.setWorkbookSheetsGroup(sheetMaster.getWorkbookSheetsGroup());

        mSheetGroup.add(sheet);
        textFldPanel.add(sheet);
        mMenuPanel.add(textFldPanel, pos);
        
        mMenuPanel.updateUI();
        mMenuPanel.revalidate();
        
        return sheet;
    }
    
    /**
     * Set the {@code JPopup menu} of menuPanel for this {@code MenuManager manager} to handle
     * @param popupMenu the {@code JPopupMenu menu} of menuPanel
     */
    public void setPopupMenu(JPopupMenu popupMenu) {
        mPopupMenu = popupMenu;
        List<Component> menuItems = Arrays.asList(mPopupMenu.getComponents());
        
        for (Iterator<Component> it = menuItems.iterator(); it.hasNext();) {
            JMenuItem menuItem = (JMenuItem) it.next();
            
            menuItem.addActionListener((ActionEvent e) -> {
                // when "Rename" menuItem is clicked
                switch (menuItem.getText()) {
                    case "Rename" -> {
                        mStateManager.addNewState(new MenuRenamedState(mInvoker));
                        mModelGroup.setSelected(mModelGroup.getSelection(), true, true);
                    }
                    case "Copy" -> {
                        copy(mInvoker);
                    }
                    case "Paste" -> {
                        paste(mInvoker);
                    }
                    default -> {
                        final int result = GradeUtils.showWrkbkSheetDeletionDialog(mInvoker.getText());
                        if (result == JOptionPane.OK_OPTION) {
                            if (mInvoker.getType() == TajosMenuTextField.TxtFieldType.SHEET) {
                                deleteSheet(mInvoker);
                                refreshDeletedItems(mInvoker, mModelGroup,
                                    TajosMenuTextField.TxtFieldType.SHEET);
                                    
                            } else {
                                deleteWorkbook(mInvoker);
                                refreshDeletedItems(mInvoker, mModelGroup,
                                    TajosMenuTextField.TxtFieldType.WORKBOOK);
                            }
                        }
                    }
                }
            });
        }
    }
    
    public void copy(TajosMenuTextField _invoker) {
        mCopyInvoker = _invoker;
    }
    
    public void paste(TajosMenuTextField _invoker) {
        if (mCopyInvoker == null)
            return;
        
        if (_invoker.getType() != TajosMenuTextField.TxtFieldType.WORKBOOK)
            mModelGroup = _invoker.getGroup();
        // region: algorithm -> find the last sheet in the group of this 
        // invoker and place the copied sheet on that position.
        // execute this if block if the type is SHEET
        if (mCopyInvoker.getType() == TajosMenuTextField.TxtFieldType.SHEET) {
            for (Component sheet : _invoker.getWorkbookSheetsGroup().getElements()) {
                TajosMenuTextField tx = (TajosMenuTextField) sheet;

                if (tx.getText().equals(SHEET_STR)) {
                    tx.setText(mCopyInvoker.getText() + "Copy");
                    tx.setForeground(GradeUtils.Colors.darkBlueColor);
                    tx.putData(mCopyInvoker.getData(), mCopyInvoker.getCellsDimension(), mCopyInvoker.getOddCellsData(), 
                        mCopyInvoker.getCellStylesData(), mCopyInvoker.getSelectedAllData());
                    RoundedPanel parent = (RoundedPanel) tx.getParent();
                    int pos = parent.getPosition()+1;
                    addNewSheet(pos, true, tx.getSheetMaster());

                    if (_invoker.getType() == TajosMenuTextField.TxtFieldType.WORKBOOK)
                        mModelGroup = tx.getGroup();

                    WorkbookManager.instance().openTable(tx);
                    mModelGroup.setSelected(tx.getModel(), true, false);
                    break;
                }
            }
            return;
        }
        // region end
        // otherwise, if type WORKBOOK
        for (Component c : mCopyInvoker.getGroup().getElements()) {
            TajosMenuTextField txWorkbook = (TajosMenuTextField) c;
            
            if (txWorkbook.getText().equals(WORKBOOK_STR)) {
                txWorkbook.setText(mCopyInvoker.getText() + "Copy");
                txWorkbook.setForeground(GradeUtils.Colors.darkBlueColor);

                int copiedWrkbkSheetsCount = mCopyInvoker.getWorkbookSheetsGroup().getElements().size();
                List<Component> copiedElements = mCopyInvoker.getWorkbookSheetsGroup().getElements();
                
                for (int i=0; i<copiedWrkbkSheetsCount; i++) {
                    TajosMenuTextField copiedSheet = (TajosMenuTextField) copiedElements.get(i);
                    if (copiedSheet.getText().equals(SHEET_STR))
                        continue;
                    
                    TajosMenuTextField newCopiedSheet = addNewSheet(-1, true, txWorkbook);

                    newCopiedSheet.setText(copiedSheet.getText() + "Copy");
                    newCopiedSheet.setForeground(GradeUtils.Colors.darkBlueColor);
                    newCopiedSheet.putData(copiedSheet.getData(), 
                    copiedSheet.getCellsDimension(), copiedSheet.getOddCellsData(), 
                        copiedSheet.getCellStylesData(), copiedSheet.getSelectedAllData());
                }
                
                addNewSheet(-1, true, txWorkbook);
            }
        }
        addNewWorkbook(false);
    }
    
    /**
     * 
     * @return the {@code JPopup Menu} from this manager.
     */
    public JPopupMenu getPopupMenu() {
        return mPopupMenu;
    }
    
    public void deleteSheet(TajosMenuTextField c) {
        c.getWorkbookSheetsGroup().remove(c);
        mMenuPanel.remove(c.getParent());
        --mSheetCount;
        
        if (mModelGroup.getSelection() == null) {
            WorkbookManager.instance().blankTable(c);
            mWorkbookActivity.disableTable();
        }
    }
    
    /**
     * Deletes a workbook including its sheets and JSeparator.
     * @param c The component to be deleted.
     */
    public void deleteWorkbook(TajosMenuTextField c) {
        c.getWorkbookSheetsGroup().removeAllSheets();
        mMenuPanel.remove(c);
        mMenuPanel.remove(c.getPosition()); // removes the Jseparator also

        WorkbookManager.instance().blankTable(c);
        mWorkbookActivity.disableTable();
    }
    
    public void refreshDeletedItems(TajosMenuTextField invoker, MenuTxFieldGroup modelGrp, 
                                    TajosMenuTextField.TxtFieldType type) 
    {
        modelGrp.remove((TajosMenuTextField)modelGrp.getSelection()); // removes this model from his group.
        
        mWorkBookCount = mWorkbookGroup.getElements().size() - 1; // minus 1 because we dont include the "Add Workbook" identifier
        mSheetCount = mSheetGroup.getElements().size() - 1; // minus 1 because we dont include the "Add Table" identifier
        mMenuPanel.repaint();
        mMenuPanel.revalidate();
        
        deletionListener.onDeleteSuccess(invoker, type);
    }

    public OnDeletionListener getDeletionListener() {
        return deletionListener;
    }

    public void setDeletionListener(OnDeletionListener deletionListener) {
        this.deletionListener = deletionListener;
    }

    public void setSheetTxFieldListener(SheetTxFieldListener enterKeyListener) {
        this.sheetTxFieldListener = enterKeyListener;
    }
    
    public MenuTxFieldGroup getWorkbookGroup() {
        return mWorkbookGroup;
    }
    
    public Color getDefaultForegroundColor() {
        return mTextColor;
    }
    
    /**
     * 
     * @return the {@RoundedPanel menuPanel}
     */
    public RoundedPanel getMenuPanel() {
        return mMenuPanel;
    }
    
    public StateManager getStateManager() {
        return mStateManager;
    }
    
    /**
     * 
     * @return all the untitled {@TajosMenuTextField Workbook} or {@TajosMenuTextField Sheets}
     */
    public int getUntitledCount() {
        mUntitledCount++;
        return mUntitledCount;
    }
    
    /**
     * 
     * @return all the counts for {@TajosMenuTextField Workbook}
     */
    public int getWorkBookCount() {
        return mWorkbookGroup.getElements().size();
    }
    
    /**
     * 
     * @return all the counts for {@TajosMenuTextField Sheets}
     */
    public int getSheetCount() {
        return mSheetCount;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

}