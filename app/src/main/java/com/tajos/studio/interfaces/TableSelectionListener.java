package com.tajos.studio.interfaces;

import java.util.Map;

/**
 *
 * @author Rene Tajos Jr.
 */
public interface TableSelectionListener {
    
    public void onRowsSelected(Map<Integer, Integer> selectedRows);
    public void selectAllRows(boolean bool);
    public void onColumnsSelected(Map<Integer, Integer> selectedCols);
    public void selectAllColumns(boolean bool);
}
