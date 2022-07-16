package com.tajos.studio.table;

import javax.swing.*;
import java.beans.*;
import java.util.Map;

/*
 *  This class listens for changes made to the data in the table via the
 *  TableCellEditor. When editing is started, the value of the cell is saved
 *  When editing is stopped the new value is saved. When the oold and new
 *  values are different, then the provided Action is invoked.
 *
 *  The source of the Action is a TableCellListener instance.
 */
public class TableCellListener implements PropertyChangeListener, Runnable
{
    private final JTable table;

    private int row;
    private int column;
    private Object oldValue;
    private TableCellData prevData;
    private Object newValue;

    private OnEditingCellListener onEditingCellListener;
    public interface OnEditingCellListener {
        void onEditing();
    }

    public void setOnEditingCellListener(OnEditingCellListener onEditingCellListener) {
        this.onEditingCellListener = onEditingCellListener;
    }

    /**
     *  Create a TableCellListener.
     *
     *  @param table   the table to be monitored for data changes
     * @param lstener
     */
    public TableCellListener(JTable table, OnEditingCellListener lstener)
    {
            this.table = table;
            this.onEditingCellListener = lstener;
    }

    /**
     *  Create a TableCellListener with a copy of all the data relevant to
     *  the change of data for a given cell.
     *
     *  @param row  the row of the changed cell
     *  @param column  the column of the changed cell
     *  @param oldValue  the old data of the changed cell
     *  @param newValue  the new data of the changed cell
     */
    private TableCellListener(JTable table, int row, int column, Object oldValue, Object newValue)
    {
            this.table = table;
            this.row = row;
            this.column = column;
            this.oldValue = oldValue;
            this.newValue = newValue;
    }

    public TableCellData getPrevData() {
        return prevData;
    }

    public void setPrevData(TableCellData prevData) {
        this.prevData = prevData;
    }

    /**
     *  Get the column that was last edited
     *
     *  @return the column that was edited
     */
    public int getColumn()
    {
            return column;
    }

    /**
     *  Get the new value in the cell
     *
     *  @return the new value in the cell
     */
    public Object getNewValue()
    {
            return newValue;
    }

    /**
     *  Get the old value of the cell
     *
     *  @return the old value of the cell
     */
    public Object getOldValue()
    {
            return oldValue;
    }

    /**
     *  Get the row that was last edited
     *
     *  @return the row that was edited
     */
    public int getRow()
    {
            return row;
    }

    /**
     *  Get the table of the cell that was changed
     *
     *  @return the table of the cell that was changed
     */
    public JTable getTable()
    {
        return table;
    }
//
//  Implement the PropertyChangeListener interface
//
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        //  A cell has started/stopped editing

        if ("tableCellEditor".equals(e.getPropertyName()))
        {
            if (table.isEditing())
                processEditingStarted();
        }
    }

    /*
     *  Save information of the cell about to be edited
     */
    private void processEditingStarted()
    {
        //  The invokeLater is necessary because the editing row and editing
        //  column of the table have not been set when the "tableCellEditor"
        //  PropertyChangeEvent is fired.
        //  This results in the "run" method being invoked
        onEditingCellListener.onEditing();
        SwingUtilities.invokeLater( this );
    }
    /*
     *  See above.
     */
    @Override
    public void run()
    {   
        row = table.convertRowIndexToModel( table.getEditingRow() );
        column = table.convertColumnIndexToModel( table.getEditingColumn() );
        oldValue = table.getModel().getValueAt(row, column);
        newValue = null;

        Map<Integer, Map<Integer, CellTextStyle>> cellTextStyles = TableDefaultCellRenderer.getCellTextStyle();
        CellTextStyle cellStyle = null;
        if (cellTextStyles.get(row) != null &&
            cellTextStyles.get(row).get(column) != null) 
        {
            cellStyle = cellTextStyles.get(row).get(column);
        }

        prevData = new TableCellData(oldValue,
                cellStyle, row, column);
    }
}
