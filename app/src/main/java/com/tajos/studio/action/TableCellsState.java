    package com.tajos.studio.action;

import com.tajos.studio.interfaces.State;
import com.tajos.studio.table.TableCellData;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.table.CellTextStyle;
import com.tajos.studio.table.TableDefaultCellRenderer;
import com.tajos.studio.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr
 */
public class TableCellsState implements State {
    
    protected final List<List<TableCellData>> undoSaveStateData;
    private List<List<TableCellData>> redoData;
    protected TajosTable table;
    
    public TableCellsState(TajosTable table, List<List<TableCellData>> undoData) 
    {
        undoSaveStateData = undoData;
        this.table = table;
    }

    @Override
    public void undo() {
        TableModel model = (TableModel) table.getModel();
        redoData = new ArrayList<>();

        for (List<TableCellData> colData : undoSaveStateData) {
            List<TableCellData> colRedoData = new ArrayList<>();

            for (TableCellData data : colData) {
                Object val = data.getContent();
                CellTextStyle styles = data.getCellTextStyle();
                int row = data.getRow();
                int col = data.getCol();
                // saves the current state for the redo later
                _saveCurrentState(model, colRedoData, row, col);

                model.setValueUndo(val, row, col);

                if (styles != null) {
                    TajosTable.instance().getRenderer().putCellStyles(row, col, styles);
                } else 
                    TableDefaultCellRenderer.removeCellStyle(row, col);
            }
            redoData.add(colRedoData);
        }
    }

    @Override
    public void redo() {
        TableModel model = (TableModel) table.getModel();

        if (redoData == null)
            return;

        for (List<TableCellData> colData : redoData) {
            for (TableCellData data : colData) {
                Object val = data.getContent();
                CellTextStyle styles = data.getCellTextStyle();
                int row = data.getRow();
                int col = data.getCol();

                model.setValueUndo(val, row, col);

                if (styles != null)
                    TajosTable.instance().getRenderer().putCellStyles(row, col, styles);
                else
                    TableDefaultCellRenderer.removeCellStyle(row, col);
            }
        }
    }
    
    protected void _saveCurrentState(TableModel model, List<TableCellData> colsData, int row, int col) {
        if (row >= model.getRowCount() || col >= model.getColumnCount())
            return;
        
        Object val = model.getValueAt(row, col);
        Map<Integer, Map<Integer, CellTextStyle>> stylesMap = TableDefaultCellRenderer.getCellTextStyle();

        CellTextStyle style = null;
        if (stylesMap != null && (stylesMap.get(row) != null && stylesMap.get(row).get(col) != null)) {
            style = stylesMap.get(row).get(col);
        }

        TableCellData cellData = new TableCellData(val, style, row, col);
        colsData.add(cellData);
    }
}
