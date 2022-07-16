package com.tajos.studio.table;

/**
 *
 * @author Rene Tajos Jr.
 */
public class CellClipboard {
    
    private CellTextStyle cellTextStyle;
    private String content;
    private String formula;
    private int row;
    private int col;
    
    public CellClipboard() {}
    
    public CellClipboard(String _content) {
        content = _content;
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

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CellTextStyle getCellTextStyle() {
        return cellTextStyle;
    }

    public void setCellTextStyle(CellTextStyle cellTextStyle) {
        this.cellTextStyle = cellTextStyle;
    }
}
