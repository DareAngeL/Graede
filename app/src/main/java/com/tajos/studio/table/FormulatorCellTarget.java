package com.tajos.studio.table;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rene Tajos Jr
 */
public class FormulatorCellTarget {

    private int col;
    private int row;
    
    private Color borderColor;
    private int [] rowsRange = new int[] {-1, -1}; // 0 -> starting row, 1 -> ending row
    private int [] colsRange = new int[] {-1, -1}; // 0 -> starting col, 1 -> ending col
    
    public FormulatorCellTarget() {}
    
    public FormulatorCellTarget(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public void setRange(int [] rowsRange, int [] colsRange) {
        this.rowsRange = rowsRange;
        this.colsRange = colsRange;
    }
    
    public List<int[]> getRanges() {
        List<int[]> lst = new ArrayList<>();
        lst.add(rowsRange);
        lst.add(colsRange);
        
        return lst;
    }

    public int getCol() {
        return col;
    }

    public void setColumn(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
    
    public boolean isRange() {
        return rowsRange[0] != -1;
    }
}
