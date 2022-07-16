package com.tajos.studio.action;

import com.tajos.studio.components.TajosTable;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.table.TableCellData;
import com.tajos.studio.table.TableDefaultCellRenderer;
import com.tajos.studio.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is for saving state that comes from shifting cells,
 * This is use to know if the maximum column count before shifting
 * is less than the maximum column count after shifting,
 * we need to know it ,so when the user performs undo operation after shifting,
 * we can easily locate the cell indices of the shifted cells after shifting, 
 * and performs erasing its content to have a "go back to it's previous state" effect.
 * @author Rene Tajos Jr
 */
public class TableShiftState extends TableCellsState {

    private final String TAG;

    private final int maxCount;
    protected List<List<TableCellData>> _redoData;
    
    public TableShiftState(String TAG, TajosTable table, List<List<TableCellData>> undoData, int maxCount) {
        super(table, undoData);
        this.maxCount = maxCount;
        this.TAG = TAG;
    }

    @Override
    public void undo() {
        super.undo();
        TableModel model = (TableModel) table.getModel();
        _redoData = new ArrayList<>();

        List<TableCellData> colData = undoSaveStateData.get(0);
        performUndo(colData, model);
    }

    @Override
    public void redo() {
        super.redo();
        TableModel model = (TableModel) table.getModel();
        
        if (_redoData == null)
            return;
        
        for (List<TableCellData> colData : _redoData) {
            for (TableCellData data : colData) {
                Object val = data.getContent();
                CellTextStyle styles = data.getCellTextStyle();
                int row = data.getRow();
                int col = data.getCol();
                
                model.setValueAt(val, row, col);
                
                if (styles != null)
                    TajosTable.instance().getRenderer().putCellStyles(row, col, styles);
                else
                    TableDefaultCellRenderer.removeCellStyle(row, col);
            }
        }
    }
    
    private void performUndo(List<TableCellData> colData, TableModel model) {
        int newMaxCount = TAG.equals("SHIFT-COL") ?
                table.getColumnCount() : table.getRowCount();
        
        if (newMaxCount > maxCount) {

            int a = TAG.equals("SHIFT-COL") ?
                    colData.get(0).getRow() : colData.get(0).getCol();
            
            int max = TAG.equals("SHIFT-COL") ?
                    undoSaveStateData.get(undoSaveStateData.size()-1).get(0).getRow() :
                    colData.get(colData.size()-1).getCol();

            for (; a<=max; a++) {
                List<TableCellData> colRedoData = new ArrayList<>();
                for (int i=maxCount; i<newMaxCount; i++) {
                    if ("SHIFT-COL".equals(TAG)) {
                        _saveCurrentState(model, colRedoData, a, i); // saves the cell before modifying it for performing redo later
                        model.setValueAt(null, a, i);
                        TableDefaultCellRenderer.removeCellStyle(a, i);
                    } else {
                        _saveCurrentState(model, colRedoData, i, a); // saves the cell before modifying it for performing redo later
                        model.setValueAt(null, i, a);
                        TableDefaultCellRenderer.removeCellStyle(i, a);
                    }
                }
                _redoData.add(colRedoData);
            }
        }
    }
}