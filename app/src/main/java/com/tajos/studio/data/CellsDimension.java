package com.tajos.studio.data;

/**
 *
 * @author Rene Tajos Jr.
 */
public class CellsDimension {

    private int cellHeight;
    private int cellWidth;
    
    public CellsDimension() {
        cellHeight = 0;
        cellWidth = 0;
    }
    
    public CellsDimension(int w, int h) {
        cellWidth = w; cellHeight = h;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    
}
