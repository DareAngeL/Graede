package com.tajos.studio.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rene Tajos Jr
 */
public class FilteredData {
    
    private final int originalRow;
    private List<Object> columns = new ArrayList<>();
    
    public FilteredData(int row, List<Object> columns) {
        this.originalRow = row;
        this.columns = columns;
    }

    public void setColumns(List<Object> columns) {
        this.columns = columns;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public int getOriginalRow() {
        return originalRow;
    }
}
