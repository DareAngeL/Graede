package com.tajos.studio.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tajos.studio.table.CellTextStyle;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr.
 */
@JsonIgnoreProperties
public class Sheet {
    
    private List<List<Object>> data;
    private List<List<CellsDimension>> cellsDimensions;
    
    private Map<Integer, Map<Integer, Rectangle>> oddCells;
    private Map<Integer, Map<Integer, CellTextStyle>> textStyles;
    private Map<Integer, Map<Integer, String>> formulas;
    private CellTextStyle.SelectedAll selectedAll;
    
    private String sheetName;
    
    private boolean isOnSaveState;

    public Map<Integer, Map<Integer, String>> getFormulas() {
        return formulas;
    }

    public void setFormulas(Map<Integer, Map<Integer, String>> formulas) {
        this.formulas = formulas;
    }
    
     public List<List<CellsDimension>> getCellsDimensions() {
        return cellsDimensions;
    }

    public void setCellsDimensions(List<List<CellsDimension>> cellsDimensions) {
        this.cellsDimensions = cellsDimensions;
    }
    
    public void setIsOnSaveState(boolean bool) {
        isOnSaveState = bool;
    }
    
    public boolean getIsOnSaveState() {
        return isOnSaveState;
    }
    
    public void setSheetName(String name) {
        sheetName = name;
    }
    
    public String getSheetName() {
        return sheetName;
    }
    
    public List<List<Object>> getData() {
        return this.data;
    }
    
    public Map<Integer, Map<Integer, Rectangle>> getOddCells() {
        return this.oddCells;
    }
    
    public Map<Integer, Map<Integer, CellTextStyle>> getTextStyles() {
        return this.textStyles;
    }
    
    public CellTextStyle.SelectedAll getSelectedAll() {
        return this.selectedAll;
    }
    
    public void putData(List<List<Object>> data) {
        this.data = data;
    }
    
    public void putOddCellsData(Map<Integer, Map<Integer, Rectangle>> data) {
        oddCells = data;
    }
    
    public void putCellStylesData(Map<Integer, Map<Integer, CellTextStyle>> data) {
        textStyles = data;
    }
    
    public void putSelectAllData(CellTextStyle.SelectedAll data) {
        selectedAll = data;
    }
}