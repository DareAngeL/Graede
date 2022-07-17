package com.tajos.studio.table;

import com.tajos.studio.data.FilteredData;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.interfaces.TextStyles;
import com.tajos.studio.dialogs.FormulasCopyDialog;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TableModel extends AbstractTableModel {
    private int mCols;
    private final JScrollPane mScrollPane;
    private int preferredRows;
    private int preferredCols;
    
    private List<List<Object>> mData = new ArrayList<>();
    private List<FilteredData> mFilteredData = new ArrayList<>();
    
    // cell that has formulas
    //                row           column, formula
    private Map<Integer, Map<Integer, String>> formulas = new HashMap<>();
    private final Formulator formulator = new Formulator();
    
    List<OccupiedCells> mOccupiedCells = new ArrayList<>();
    private final TajosTable mTable;
    
    private ColumnListener columnListener;
    
    public interface ColumnListener {
        void OnColumnAdded();
        void OnColumnRemoved();
    }

    public TableModel(JFrame frame, TajosTable table, List<List<Object>> data, 
        int rows, int cols) 
    {
        mTable = table;
        JViewport port = (JViewport) table.getParent();
        mScrollPane = (JScrollPane) port.getParent();
        
        if (data != null && !data.isEmpty()) {
            rows = rows > data.size() ? rows : data.size();
            // finds the biggest column size of data
            int biggestCols = 0;
            for (List<Object> data1 : data) {
                biggestCols = data1.size() > biggestCols ? data1.size() : biggestCols;
            }
            // end
            // determine if the default calculated cols is bigger than
            // the data's biggest cols
            mCols = cols > biggestCols ? cols : biggestCols;
            // end
        } else {
            mCols = cols;
        }
        
        preferredCols = mCols;
        preferredRows = rows;
        
        _initRows(data, rows);
        if (table.getInvoker() != null) {
            formulas.clear();
            FormulasCopyDialog[] cp = {null};
            cp[0] = new FormulasCopyDialog(
                frame, 
                true, 
                    table, 
                (_formulas) -> 
                // on copying done
            {
                SwingUtilities.invokeLater(() -> {
                    this.formulas = _formulas;
                    cp[0].setVisible(false); 
                });
            });
            cp[0].setVisible(true);
        }
    }
    
    public void setData(List<List<Object>> data) {
        mData = data;
        
        fireTableDataChanged();
    }
    
    public void setFilteredData(List<FilteredData> filterData) {
        mFilteredData = filterData;
        fireTableDataChanged();
    }

    public List<FilteredData> getFilteredData() {
        return mFilteredData;
    }
    
    public boolean isOnFilteredMode() {
        return !mFilteredData.isEmpty();
    }

    public List<List<Object>> getData() {
        return mData;
    }

    public Map<Integer, Map<Integer, String>> getFormulas() {
        return formulas;
    }

    public void setFormulas(Map<Integer, Map<Integer, String>> formulas) {
        this.formulas = formulas;
    }

    public void addFormula(int row, int col, String formula) {
        final Map<Integer, String> prev = formulas.get(row);
        final Map<Integer, String> colFormula = new HashMap<>();
        if (prev != null) {
            for (Map.Entry<Integer, String> entry : prev.entrySet()) {
                final int colEntry = entry.getKey();
                final String colEntryFormula = entry.getValue();
                
                colFormula.put(colEntry, colEntryFormula);
            }
        }
        
        colFormula.put(col, formula);
        GradeUtils.log(colFormula.size());
        formulas.put(row, colFormula);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !(rowIndex == 0 && columnIndex == 0) ||
               !(rowIndex == 0 && columnIndex == 1);
    }

    /**
     * Initialize the data
     * @param rows - Value of how many rows should be added
     */
    private void _initRows(List<List<Object>> data, int rows) {
        OccupiedCells.Manager.resetIndex();
        mData.clear();
        for (int j=0; j<rows; j++) {
            final List<Object> lst = new ArrayList<>();
            
            for (int i=0; i<mCols; i++) {
                
                if (((j==0 && i==0) || (j==0 && i==1)))
                {
                    if (i==0) {
                        lst.add("School ID");
                        _updateOccupiedCells("School ID", j, i);
                        continue;
                    }
                    lst.add("Name");
                    _updateOccupiedCells("School ID", j, i);
                    continue;
                }
                
                if (data != null && !data.isEmpty() &&
                    j < data.size() && i < data.get(j).size())
                {
                    Object value = data.get(j).get(i); // j -> row, i -> column
                    lst.add(value);
                    _updateOccupiedCells(value, j, i);
                    continue;
                }
                lst.add(null);
            }

            mData.add(lst);
        }
    }

    public void addRow() {
        _insertRow("ADD", getRowCount()-1);
    }

    public void addColumn() {
        _insertColumn("ADD", -1);
    }
    
    public void removeColumnContent(int column) {
        --mCols;
        for (int row=0; row<getRowCount(); row++) {
            if (column >= mData.get(row).size())
                continue;
                
            mData.get(row).remove(column);
        }
        
        TableColumn tableColumn = mTable.getColumnModel().getColumn(column);
        mTable.removeColumn(tableColumn);
        columnListener.OnColumnRemoved();
    }

    public void removeRowContent(int row) {
        mData.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    public void shiftCellLeft(int row, int _column) {
        int max = getMaxOccupiedCellCol() == 1 ? mTable.getColumnCount() :
                getMaxOccupiedCellCol();
        
        _delete("COL", _column, row, max);
    }
    
    public void shiftColumnLeft(int row, int column) {
        _deleteColumn(row, column);
    }
    
    public void shiftCellUp(int delCount, int column) {
        int max = getMaxOccupiedCellRow() == 1 ? mTable.getRowCount() :
                getMaxOccupiedCellRow();
        
        _delete("ROW", delCount, column, max);
    }
    
    public void shiftRowUp(int row, int col) {
        _deleteRow(row, col);
    }
    
    /**
     * Shift the single cell to the right
     * @param row
     * @param _col 
     */
    public void shiftCellRight(int row, int _col) {
        int max = getMaxOccupiedCellCol() == 1 ? mTable.getColumnCount() :
                getMaxOccupiedCellCol();
        
        _shift("COL", _col, row, max, mTable.getColumnCount());
    }
    
    /**
     * Shift the single cell down
     * @param _row
     * @param col 
     */
    public void shiftCellDown(int _row, int col) {
        int max = getMaxOccupiedCellRow() == 1 ? mTable.getRowCount() :
                getMaxOccupiedCellRow();
        
        _shift("ROW", _row, col, max, mTable.getRowCount());
    }
    
    /**
     * Shift the entire row down
     * @param _row 
     */
    public void shiftRowDown(int _row) {
        int max = getMaxOccupiedCellRow() == 1 ? mTable.getRowCount() :
                getMaxOccupiedCellRow();
        
        for (int col=_row==0?2:0; col<mTable.getColumnCount(); col++) {
            _shift("ROW", _row, col, max,
                    mTable.getRowCount());
        }
        
        _shiftSelectedAllRowToBottom();
    }
    
    /**
     * Shift the entire column right
     * @param col 
     */
    public void shiftColumnRight(int col) {
        if (col == 0 || col == 1) // dont shift the non-editable cell
            return;
        
        int max = getMaxOccupiedCellCol() == 1 ? mTable.getColumnCount() :
                getMaxOccupiedCellCol();
        
        for (int row=0; row<mTable.getRowCount(); row++) {
            _shift("COL", col, row, max, 
                    mTable.getColumnCount());
        }
        
        _shiftSelectedAllColToRight(col);
    }
    
    public void setPreferredRowCount(int c) {
        preferredRows = c;
    }
    
    public int getPreferredRowCount() {
        return preferredRows;
    }
    
    public void setPreferredColCount(int c) {
        preferredCols = c;
    }
    
    public int getPreferredColCount() {
        return preferredCols;
    }
    
    public int getMaxOccupiedCellRow() {
        return OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells)[0];
    }
    
    public int getMaxOccupiedCellCol() {
        return OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells)[1];
    }
    
    public boolean isIDExist(Object value) {
        Stream<List<Object>> found = mData.stream().parallel().filter(cols -> {
            if (cols.get(0) == null)
                return false;
            
            final String v1 = cols.get(0).toString();
            final String v2 = value.toString();
            
            return v1.toLowerCase().equals(v2.toLowerCase());
        });
        
        return found.count() > 0;
    }
    
    public boolean isNameExist(Object value) {
        Stream<List<Object>> found = mData.stream().parallel().filter(cols -> {
            if (cols.get(1) == null)
                return false;
            
            final String v1 = cols.get(1).toString();
            final String v2 = value.toString();
            
            return v1.toLowerCase().equals(v2.toLowerCase());
        });
        
        return found.count() > 0;
    }

    @Override
    public int getRowCount() {
        return mData.size();
    }

    @Override
    public int getColumnCount() {
        return mCols;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // region: On Filtering MODE
        if (!mFilteredData.isEmpty()) {
            if (rowIndex != 0) {
                if (rowIndex-1 < mFilteredData.size() && 
                    columnIndex < mFilteredData.get(rowIndex-1).getColumns().size()) 
                {
                    final FilteredData filterRowData = mFilteredData.get(rowIndex-1);
                    filterRowData.getColumns().set(columnIndex, value);

                    mFilteredData.set(rowIndex-1, filterRowData);
                    mData.set(filterRowData.getOriginalRow(), filterRowData.getColumns());
                    
                    _recheckOccupiedCells(0);
                    fireTableCellUpdated(rowIndex, columnIndex);
                } else {
                    final FilteredData filterRowData = mFilteredData.get(rowIndex-1);
                    filterRowData.setColumns(mData.get(filterRowData.getOriginalRow()));
                    filterRowData.getColumns().set(columnIndex, value);
                    
                    mFilteredData.set(rowIndex-1, filterRowData);
                    
                    mData.set(filterRowData.getOriginalRow(), filterRowData.getColumns());
                    _recheckOccupiedCells(0);
                }

                return;
            }
        }
        // region end
        
        if (rowIndex >= mData.size()) {
            return;
        }
        
        // region: evaluate if the value is a formula
        if (Formulator.isAnExpression((String)value)) {
            formulator.setExpression((String)value);
            formulator.calculate(mTable, 
                (TableColumnHeader)mScrollPane.getColumnHeader().getComponent(0));
            
            addFormula(rowIndex, columnIndex, value.toString());
            value = formulator.getResult();
        } else {
            // removes the formula if there's any, if this is not an expression.
            if (formulas.containsKey(rowIndex) &&
                formulas.get(rowIndex).containsKey(columnIndex))
            {
                formulas.get(rowIndex).remove(columnIndex);
                if (formulas.get(rowIndex).isEmpty()) {
                    formulas.remove(rowIndex);
                }
            }
        }
        // region end
        
        List<Object> rowData = mData.get(rowIndex);
        if (columnIndex >= rowData.size()) {
            rowData = _fixTableData(rowData, columnIndex+1 - rowData.size());
            rowData.set(columnIndex, value);
        } else
            rowData.set(columnIndex, value);
        
        mData.set(rowIndex, rowData);
        
        _updateOccupiedCells(value, rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    public void setValueUndo(Object value, int rowIndex, int columnIndex) {
        if (rowIndex >= mData.size() || 
            columnIndex >= mData.get(rowIndex).size())
                return;
        
        final List<Object> rowData = mData.get(rowIndex);
        if (columnIndex >= mData.get(rowIndex).size())
            rowData.add(value);
        else
            rowData.set(columnIndex, value);
        
        mData.set(rowIndex, rowData);
        
        _updateOccupiedCells(value, rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!mFilteredData.isEmpty()) {
            if (rowIndex != 0) {
                if (rowIndex-1 < mFilteredData.size() && 
                    columnIndex < mFilteredData.get(rowIndex-1).getColumns().size()) 
                {
                    return mFilteredData.get(rowIndex-1).getColumns().get(columnIndex);
                } else {
                    return null;
                }
            }
        }
        
        Object obj = null;
        try {
            obj = mData.get(rowIndex).get(columnIndex);
        } catch (Exception e) {
            return obj;
        }
        
        return obj;
    }
    
    private List<Object> _fixTableData(final List<Object> rowData, final int diff) {
        for (int i=0; i<diff; i++)
            rowData.add(null);
        
        return rowData;
    }
    
    /**
     * Updates the occupied cells
     * @param value
     * @param rowIndex
     * @param columnIndex 
     */
    private void _updateOccupiedCells(Object value, int rowIndex, int columnIndex) {
        
        if (value != null && !value.equals("")) 
        {
            mOccupiedCells.add(new OccupiedCells(rowIndex, columnIndex));
            int[] maxOccupiedCell = OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells);
            mTable.setMaxOccupiedCells(maxOccupiedCell);
            
        } else if (value != null && value.equals("") && !mOccupiedCells.isEmpty()) {
            
            int index = OccupiedCells.Manager.getOccupiedCellIndex(mOccupiedCells, rowIndex, columnIndex);
            
            if (index == -1)
                return;
            
            mOccupiedCells.remove(index);
            OccupiedCells.Manager.updateIndexes(mOccupiedCells);
            OccupiedCells.Manager.decrementIndex();
            
            int[] maxOccupiedCell = OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells);
            mTable.setMaxOccupiedCells(maxOccupiedCell);
        } else if (value == null && !mOccupiedCells.isEmpty()) {
            // check first if this cell is already exist in occupied list
            int index = OccupiedCells.Manager.getOccupiedCellIndex(mOccupiedCells, rowIndex, columnIndex);
            if (index == -1)
                return;
            
            mOccupiedCells.remove(index);
            OccupiedCells.Manager.updateIndexes(mOccupiedCells);
            OccupiedCells.Manager.decrementIndex();
            
            int[] maxOccupiedCell = OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells);
            mTable.setMaxOccupiedCells(maxOccupiedCell);
        }
    }
    
    /**
     * A method that will handle all the shifting for single cell
     * @param _a - The column or row to be shift
     * @param b - The column or row index
     * @param _max - The maximum row or column occupied cells in the table
     * @param count - The total visible row or column count
     */
    private void _shift(String TAG, int _a, int b, int _max, int count) {
        boolean reachedEnd = false;
        
        if (_max == count) {
            if (TAG.equals("ROW"))
                addRow();
            else
                addColumn();
        }
        
        TableCellData cellData = null;
        for (int i=_a; i<_max; i++) {
            if (i == _a && i != _max-1) {
                Object value = TAG.equals("ROW") ? 
                        getValueAt(i, b) :  // for row
                        getValueAt(b, i);   // for column
                
                if (TAG.equals("ROW")) {
                    cellData = _getCellData(TAG, value, i, b);
                    setValueAt(null, i, b);
                    TableDefaultCellRenderer.removeCellStyle(i, b);
                } else {
                    cellData = _getCellData(TAG, value, b, i);
                    setValueAt(null, b, i);
                    TableDefaultCellRenderer.removeCellStyle(b, i);
                }
                
                continue;
            }
            
            if (i == _max-1 && !reachedEnd) {
                reachedEnd = true;
                ++_max;
            }
            
            Object prevValue = TAG.equals("ROW") ? 
                               getValueAt(i, b) : // for row
                               getValueAt(b, i);  // for column

            TableCellData prevCellData;
            if (TAG.equals("ROW")) {
                prevCellData = _getCellData(TAG, prevValue, i, b);
                if (cellData == null) {
                    setValueAt(null, i, b);
                    TableDefaultCellRenderer.removeCellStyle(i, b);
                } else {
                    setValueAt(cellData.getContent(), i, b);
                    mTable.getRenderer().putCellStyles(i, b, cellData.getCellTextStyle());
                }
            } else {
                prevCellData = _getCellData(TAG, prevValue, b, i);
                if (cellData == null) {
                    setValueAt(null, b, i);
                    TableDefaultCellRenderer.removeCellStyle(b, i);
                } else {
                    setValueAt(cellData.getContent(), b, i);
                    mTable.getRenderer().putCellStyles(b, i, cellData.getCellTextStyle());
                }
            }
            
            cellData = i == _max-1 ? cellData : prevCellData;
        }
    }
    
    /**
     * A method that will handle all the deletion of the cells, either from the column or from the row
     */
    private void _delete(String TAG, int toShift, int b, int max) {
        for (int i=toShift; i<max; i++) {
            Object value = TAG.equals("COL") ? getValueAt(b, i+1) : getValueAt(i+1, b);
            
            if (TAG.equals("ROW")) {
                if (i == max-1) {
                    setValueAt(null, i, b);
                    continue;
                }
                
                final TableCellData cellData = _getCellData(TAG, value, i+1, b);
                setValueAt(value, i, b);
                mTable.getRenderer().putCellStyles(i, b, cellData.getCellTextStyle());
                TableDefaultCellRenderer.removeCellStyle(i+1, b);
            } else {
                if (i == max-1) {
                    setValueAt(null, b, i);
                    continue;
                }
                
                final TableCellData cellData = _getCellData(TAG, value, b, i+1);
                setValueAt(value, b, i);
                mTable.getRenderer().putCellStyles(b, i, cellData.getCellTextStyle());
                TableDefaultCellRenderer.removeCellStyle(b, i+1);
            }
        }
    }
    
    private void _deleteRow(int row, int col) {
        mData.remove(row);
        TableDefaultCellRenderer.removeCellStyle(row, col);
        fireTableRowsDeleted(row, row);
    }
    
    private void _deleteColumn(int row, int col) {
        for (int i=0; i<mData.size(); i++) {
            if (i == 0 && (col == 0 || col == 1))
                continue;
            
            mData.get(i).remove(col);
            TableDefaultCellRenderer.removeCellStyle(i, col);
        }
        
        fireTableDataChanged();
    }
    
    private void _shiftSelectedAllColToRight(int _col) {
        CellTextStyle.SelectedAll all = TableDefaultCellRenderer.getSelectedAll();
        
        Map<Integer, Color> newSelectedAllCol = new HashMap<>();
        for (Map.Entry<Integer, Color> entry : all.getSelectedAllCols().entrySet()) {
            int col = entry.getKey();
            Color color = entry.getValue();
            
            if (col < _col) {
                newSelectedAllCol.put(col, color);
                continue;
            }
            
            newSelectedAllCol.put(col+1, color);
        }
        all.setSelectedAllCols(newSelectedAllCol);
    }
    
    private void _shiftSelectedAllRowToBottom() {
        CellTextStyle.SelectedAll all = TableDefaultCellRenderer.getSelectedAll();
        
        Map<Integer, Color> newSelectedAllRow = new HashMap<>();
        for (Map.Entry<Integer, Color> entry : all.getSelectedAllRow().entrySet()) {
            int row = entry.getKey();
            Color color = entry.getValue();
            
            newSelectedAllRow.put(row+1, color);
        }
        all.setSelectedAllRows(newSelectedAllRow);
    }
    
    private TableCellData _getCellData(String TAG, Object value, int row, int col) {
        TableCellData cellData;
        Map<Integer, Map<Integer, CellTextStyle>> stylesMap = TableDefaultCellRenderer.getCellTextStyle();
        CellTextStyle style = null;
        if (stylesMap != null && (stylesMap.get(row) != null && stylesMap.get(row).get(col) != null)) {
            style = _copyCellStyles(TAG, stylesMap, row, col);
        }
        cellData = new TableCellData(value, style, row, col);
        
        return cellData;
    }
    
    private CellTextStyle _copyCellStyles(String TAG, Map<Integer, Map<Integer, CellTextStyle>> stylesMap, int row, int col) {
        CellTextStyle styleOrig = stylesMap.get(row).get(col);
        //region: copy the styles
        CellTextStyle style = new CellTextStyle();
        style.setFontColor(styleOrig.getFontColor());
        style.setBackgroundColor(styleOrig.getBgColor());
        style.setHorizontalAlignment(styleOrig.getHorizontalAlignment());

        List<TextStyles> origStyles = styleOrig.getStyles();
        List<TextStyles> copyOrigStyles = new ArrayList<>();
        for (TextStyles origStyle : origStyles) {
            copyOrigStyles.add(origStyle);
        }
        style.setStyles(copyOrigStyles);
        //region end
        // region: copy the selected all
        CellTextStyle.SelectedAll selectedAllOrig = TableDefaultCellRenderer.getSelectedAll();
        if (selectedAllOrig != null) {
            CellTextStyle.SelectedAll selectedAll = new CellTextStyle.SelectedAll();
            if ("ROW".equals(TAG)) {
                selectedAll.addSelectedAllRow(row+1, selectedAllOrig.getColorInRow(row));
                selectedAll.addSelectedAllCol(col, selectedAllOrig.getColorInCol(col));
            } else {
                selectedAll.addSelectedAllRow(row, selectedAllOrig.getColorInRow(row));
                selectedAll.addSelectedAllCol(col+1, selectedAllOrig.getColorInCol(col));
            }
            
            style.putSelectedAllList(selectedAll);
        }
        //region end
        return style;
    }
    
    /**
     * A method that will handle all the inserting of rows
     * @param TAG
     * @param row 
     */
    private void _insertRow(String TAG, int row) {
        List<Object> cols = new ArrayList<>();
        for (int i=0; i<mCols; i++)
            cols.add(null);
        
        if (TAG.equals("INS")) {
            mData.add(row, cols);
            fireTableRowsInserted(row, row);
        } else {
            mData.add(cols);
            fireTableRowsInserted(row, row);
        }
    }
    
    /**
     * A method that will handle all the inserting of columns
     * @param TAG
     * @param col
     */
    private void _insertColumn(String TAG, int col) {
        if (mTable.getAutoCreateColumnsFromModel()) {
            mTable.setAutoCreateColumnsFromModel(false);
        }
        
        ++mCols;
        for (int row=0; row<getRowCount(); row++) {
            if (TAG.equals("ADD"))
                mData.get(row).add(null);
            else {
                mData.get(row).add(col, null);
            }
        }
        
        int index = TAG.equals("ADD") ? getColumnCount()-1 : col;
        TableColumn tableColumn = new TableColumn(index);
        mTable.addColumn(tableColumn);
        
        columnListener.OnColumnAdded();
//        fireTableDataChanged();
        
        mTable.setAutoCreateColumnsFromModel(false);
    }
    
    /**
     * Recheck and update the occupied cells
     * @param _row - The starting row to check the cell
     */
    private void _recheckOccupiedCells(int _row) {
        int maxRow = getMaxOccupiedCellRow()+1;
        for (int row=_row; row<maxRow; row++) {
            
            for (int col=0; col<getColumnCount(); col++) {
                // region: set its previous cell value to null to erase it on the occupied cell list
                int index = OccupiedCells.Manager.getOccupiedCellIndex(mOccupiedCells, row, col);
                if (index != -1) {
                    _updateOccupiedCells(null, row, col);
                }
                // region end
                
                Object rowCellData = getValueAt(row, col);
                if (rowCellData != null && !rowCellData.equals("")) {
                    _updateOccupiedCells(rowCellData, row, col);
                }
            }
        }
        
        // region: recheck occupied cells
        for (Iterator<OccupiedCells> it = mOccupiedCells.iterator(); it.hasNext();) {
            OccupiedCells occ = it.next();
            int row = occ.getRow();
            int col = occ.getColumn();
            
            Object value = getValueAt(row, col);
            if (value == null || value.equals("")) {
                int index = OccupiedCells.Manager.getOccupiedCellIndex(mOccupiedCells, row, col);
            
                if (index == -1)
                    return;

                it.remove();
            }
        }
        
        OccupiedCells.Manager.updateIndexes(mOccupiedCells);
        OccupiedCells.Manager.decrementIndex();

        int[] maxOccupiedCell = OccupiedCells.Manager.getMaxOccupiedCell(mOccupiedCells);
        mTable.setMaxOccupiedCells(maxOccupiedCell);
        // region end 
   }
    
    public void addColumnListener(ColumnListener listner) {
        columnListener = listner;
    }
}