package com.tajos.studio.action;

import com.tajos.studio.interfaces.State;
import com.tajos.studio.table.TableModel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr
 */
public class FormulasState implements State {

    private final Map<Integer, Map<Integer, String>> undoFormulas = new HashMap<>();
    private final Map<Integer, Map<Integer, String>> redoFormulas = new HashMap<>();
    private final TableModel tableModel;
    
    public FormulasState(Map<Integer, Map<Integer, String>> formulas, TableModel model) {
        this.tableModel = model;
        _copy(formulas, undoFormulas);
    }
    
    private void _copy(
            Map<Integer, Map<Integer, String>> formulas, 
            Map<Integer, Map<Integer, String>> to) 
    {
        to.clear();
        
        for (Map.Entry<Integer, Map<Integer, String>> entry : formulas.entrySet()) {
            final int row = entry.getKey();
            final Map<Integer, String> colMap = new HashMap(entry.getValue());
            
            to.put(row, colMap);
        }
    }

    @Override
    public void undo() {
        _copy(tableModel.getFormulas(), redoFormulas);
        tableModel.setFormulas(undoFormulas);
    }
    
    @Override
    public void redo() {
        tableModel.setFormulas(redoFormulas);
    }
}