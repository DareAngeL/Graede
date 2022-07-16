package com.tajos.studio;

import com.tajos.studio.net.DBManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tajos.studio.data.Sheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tajos.studio.activities.WorkBookActivity;
import com.tajos.studio.components.TajosMenuTextField;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.data.Workbook;
import com.tajos.studio.serializer.Serializer;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.table.TableCellEditor;
import com.tajos.studio.table.TableDefaultCellRenderer;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rene Tajos Jr.
 */
public class WorkbookManager {

    private final WorkBookActivity mWorkbookActivity;
    
    private final SimpleModule serializerModule = new SimpleModule();

    private final JComponent mTableSPane;
    private final TajosTable mTable;
    
    private MenuTxFieldGroup mWorkbookGroup;
    
    private static WorkbookManager instance;
    public static WorkbookManager instance() {
        return instance;
    }
    private TajosMenuTextField mActiveInvoker;
    
    public interface OnSavingListener {
        void onSavingComplete();
    }
    
    public WorkbookManager(JComponent tableSPane, TajosTable table, WorkBookActivity host) {
        serializerModule.addSerializer(Color.class, new Serializer.JSONColorSerializer());
        serializerModule.addDeserializer(Color.class, new Serializer.JSONColorDeserializer());
        serializerModule.addSerializer(Rectangle.class, new Serializer.JSONRectanlgeSerializer());
        serializerModule.addDeserializer(Rectangle.class, new Serializer.JSONRectangleDeserializer());
        
        mTableSPane = tableSPane;
        mTable = table;
        mWorkbookActivity = host;
        
        instance = this;
        
        init();
    }
    
    private void init() {
        mTableSPane.setVisible(false);
    }
    
    public void updateSaveState(boolean isSave) {
        mActiveInvoker.setIsSavedState(isSave);
    }
    
    public void blankTable(TajosMenuTextField invoker) {
        mTable.clearSaveStates();
        if (mTable.isDisable()) {
            mWorkbookActivity.enableTable(true);
        }   
        
        mActiveInvoker = invoker;
        mTable.setInvoker(invoker);
         mTable.getRenderer().setCellStyles(null);
//        TableDefaultCellRenderer.setCellStyles(null);
        TableDefaultCellRenderer.setSelectedAll(null);
        mTable.setOddCells(null);
        TableCellEditor editor = (TableCellEditor) mTable.getDefaultEditor(Object.class);
        editor.restartEditor();
        
        mTableSPane.setVisible(true);
        
        mWorkbookActivity.initTable(null);
        mWorkbookActivity.updateSheetTitle();
    }
    
    public TajosTable getTable() {
        return mTable;
    }
    
    public void openTable(TajosMenuTextField invoker) {
        mTable.clearSaveStates();
        if (mTable.isDisable()) {
            mWorkbookActivity.enableTable(false);
            mTable.disableRibbon(false);
        }
        
        mActiveInvoker = invoker;
        mTable.setInvoker(invoker);

        if (!mTableSPane.isVisible())
            mTableSPane.setVisible(true);

        if (invoker.getOddCellsData() != null) {
            mTable.setOddCells(invoker.getOddCellsData());
        } else {
            mTable.setOddCells(null);
        }

        if (invoker.getCellStylesData() != null) {
            Map<Integer, Map<Integer, CellTextStyle>> copied = copyCellTextStyle(invoker.getCellStylesData());
            mTable.getRenderer().setCellStyles(copied);
        } else {
            mTable.getRenderer().setCellStyles(null);
        }

        if (invoker.getSelectedAllData() != null) {
            TableDefaultCellRenderer.setSelectedAll(invoker.getSelectedAllData());
        } else {
            TableDefaultCellRenderer.setSelectedAll(null);
        }

        mWorkbookActivity.initTable(invoker.getData());
        mWorkbookActivity.updateSheetTitle();
        mTable.clearSaveStates();
    }
    
    private Map<Integer, Map<Integer, CellTextStyle>> copyCellTextStyle(Map<Integer, Map<Integer, CellTextStyle>> style) {
        Map<Integer, Map<Integer, CellTextStyle>> copyStyle = new HashMap<>();
        
        for (Map.Entry<Integer, Map<Integer, CellTextStyle>> entry : style.entrySet()) {
            int row = entry.getKey();
            Map<Integer, CellTextStyle> val = entry.getValue();
            Map<Integer, CellTextStyle> copyVal = new HashMap<>();
            
            for (Map.Entry<Integer, CellTextStyle> entry2 : val.entrySet()) {
                int col = entry2.getKey();
                CellTextStyle st = entry2.getValue();
                CellTextStyle copy = new CellTextStyle(st);
                
                copyVal.put(col, copy);
            }
            
            copyStyle.put(row, copyVal);
        }
        
        return copyStyle;
    }
    
    public void setWorkbookGroup(MenuTxFieldGroup grp) {
        mWorkbookGroup = grp;
    }
    
    public void saveData(OnSavingListener listener) throws URISyntaxException, IOException {
        if (mWorkbookGroup == null)
            return;
        
        // put the formulas to the invoker of the table
        mActiveInvoker.setFormulas(mTable.getFormulas());
        
        List<Workbook> workbooks = new ArrayList<>();
        List<Component> workbookLst = mWorkbookGroup.getElements();
        // region: List all the workbooks and sheets
        for (Component workbookComponent : workbookLst) {
            TajosMenuTextField _workbook = (TajosMenuTextField) workbookComponent;
            
            if (_workbook.getText().equals(MenuManager.WORKBOOK_STR))
                continue;
            
            Workbook workbook = new Workbook();
            workbook.setWorkbookName(_workbook.getText());
            
            List<Sheet> sheets = new ArrayList<>();
            List<Component> sheetsLst = _workbook.getWorkbookSheetsGroup().getElements();
            // region: List all the sheets and put it to workbook.
            for (Component sheetComponent : sheetsLst) {
                TajosMenuTextField _sheet = (TajosMenuTextField) sheetComponent;
                
                if (_sheet.getText().equals(MenuManager.SHEET_STR))
                   continue;

                Sheet sheet = new Sheet();
                sheet.setSheetName(_sheet.getText());
                sheet.putData(_sheet.getData());
                sheet.putOddCellsData(_sheet.getOddCellsData());
                sheet.putCellStylesData(_sheet.getCellStylesData());
                sheet.putSelectAllData(_sheet.getSelectedAllData());
                sheet.setCellsDimensions(_sheet.getCellsDimension());
                sheet.setIsOnSaveState(_sheet.isSaved());
                sheet.setFormulas(_sheet.getFormulas());
                sheets.add(sheet);
            }
            workbook.putSheets(sheets);
            workbooks.add(workbook);
            // region end
        }
        // region end
        String workbookId = DBManager.getInstance().getUserData().get("workspace_id").toString();
        File file = new File(GradeApp.getDefaultSaveDirectory() + "/" + workbookId + ".dat");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(serializerModule);
        
        mapper.writeValue(file, workbooks);
        mTable.getInvoker().setIsSavedState(true);
        listener.onSavingComplete();
    }
    
    public void publish(WorkBookActivity.PublishType type, String workbookName,
            Map<String, List<List<Object>>> data) throws JsonProcessingException
    {
        String jsonData = "";
        if (data != null) {
            ObjectMapper mapper = new ObjectMapper();
            jsonData = mapper.writeValueAsString(data);
        }
        
        if (jsonData.contains("'")) {
            JOptionPane.showMessageDialog(new JFrame(), 
                "Quotations are not allowed", 
                "Error", JOptionPane.WARNING_MESSAGE);
            
            return;
        }
        
        try {
            DBManager.getInstance().publish(type, workbookName, jsonData);
        } catch (Exception ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public void publishAllWorkbooks(Map<String, Map<String, List<List<Object>>>> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        
        String jsonData = mapper.writeValueAsString(data);
        DBManager.getInstance().publish(WorkBookActivity.PublishType.ALL_WORKBOOKS, null, jsonData);
    }
    
    public List<Workbook> readSaveFile(String _path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(serializerModule);
        File file = new File(_path);
        
        return mapper.readValue(file, new TypeReference<List<Workbook>>(){});
    }
}