package com.tajos.studio.table;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TableCellData {
    
    private Object content;
    private CellTextStyle cellTextStyle;
    private int row;
    private int col;
    
    public TableCellData(Object content, CellTextStyle style, int row, int col) {
        this.content = content;
        this.cellTextStyle = style;
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public CellTextStyle getCellTextStyle() {
        return cellTextStyle;
    }

    public void setCellTextStyle(CellTextStyle cellTextStyle) {
        this.cellTextStyle = cellTextStyle;
    }
}
