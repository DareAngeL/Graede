package com.tajos.studio.table;

import com.tajos.studio.components.TajosMenuTextField;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.data.CellsDimension;
import com.tajos.studio.interfaces.TableSelectionListener;
import com.tajos.studio.util.GradeUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Initialize this class after setting the table's model
 * @author Rene Tajos Jr.
 */
public class TableRowHeader extends JComponent implements ChangeListener, TableModelListener, TableSelectionListener {

    private TajosTable mTable;
    private final JScrollPane mScrollPane;
    private int mRows; // [0] => row counts, [1] => row default height
    private int mViewportPos = 0;
    private final Color mGridColor;
    private Color mFontColor = GradeUtils.Colors.darkBlueColor;
    private final Color mSelectedRowColor = GradeUtils.Colors.semiCreamWhiteBlueColor;
    private final Color mBgColor = new Color(247,245,251);
    private Font mFont = GradeUtils.getDefaultFont(12);
    
    private Map<Integer, Integer> mRowsSelected = new HashMap<>();
    private Map<Integer, Integer> resizePoints = new HashMap<>();
    
    private boolean isResizing = false;
    private boolean mEnabled = true;
    private Color disableColor = new Color(187,187,187, 80);
    
    private final MouseInputAdapter inputAdapter = new MouseInputAdapter() {
        private int mMouseY = -1;
        private final int MIN_ROW_HEIGHT = 23;
        private int mRowToResize;

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!mEnabled)
                return;
            
            Point p = e.getPoint();
            
            if (resizePoints.containsKey((int)p.getY())) {
                GradeUtils.setCustomCursor(TableRowHeader.this, GradeUtils.resizeVerticalCursorStr);
                return;
            }
            
            GradeUtils.setCustomCursor(TableRowHeader.this, GradeUtils.rightArrowIconName);
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!mEnabled)
                return;
            
            if (SwingUtilities.isRightMouseButton(e))
                return;
            
            if (getCursor() == GradeUtils.getCustomCursor(GradeUtils.rightArrowIconName)) {
                _selectRows(e);
                return;
            }
                
            int y = e.getY();
            if (mMouseY != -1) {
                int elapsed = y - mMouseY;
                
                int oldRowHeight = mTable.getInitialRowHeight(mRowToResize)[1];
                int rowHeight = Math.max(MIN_ROW_HEIGHT, oldRowHeight + elapsed);
                mTable.setInitialRowHeight(mRowToResize, rowHeight);
            }
            mMouseY = y;
            isResizing = true;
            
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!mEnabled)
                return;
            
            mMouseY = -1;
            startRow = -1;
            endRow = -1;
            
            isResizing = false;
            mTable.setRowHeight(mRowToResize, mTable.getInitialRowHeight(mRowToResize)[1]);
            mTable.clearInitialRowHeight();
            
            mTable.revalidate();
            mTable.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!mEnabled)
                return;
            
            mTable.setSelectedHeader(TajosTable.HeaderType.ROW_H);
            
            final Point p = e.getPoint();
            
            if (getCursor() != GradeUtils.getCustomCursor(GradeUtils.rightArrowIconName)) {
                mMouseY = e.getY();
                // region: get the row point to resize
                p.y += mViewportPos;
                mRowToResize = mTable.rowAtPoint(_getRowPoint(p, 5));
                return;
                // region end
            }
            
            _selectRows(e);
            // region: selects all cells on this column index
           /* p.y += mViewportPos;
            int row = mTable.rowAtPoint(p);
            mTable.setColumnSelectionInterval(0, mTable.getColumnCount()-1);
            mTable.setRowSelectionInterval(row, row);
            mRowsSelected.clear();
            mRowsSelected.put(row, row);
            
            JViewport vport = mScrollPane.getColumnHeader();
            TableColumnHeader colHeader = (TableColumnHeader) vport.getComponent(0);
            
            Map<Integer, Integer> selectedCols = new HashMap<>();
            for (int col : mTable.getSelectedColumns())
                selectedCols.put(col, col);
                
            colHeader.onColumnsSelected(selectedCols);
            onRowsSelected(mRowsSelected);
            mTable.revalidate();
            mTable.repaint();*/
            //region end
        }
        /**
         * Locate the row point to resize
         * @param p The event Point
         * @param i The number difference of where to locate the row
         */
        private Point _getRowPoint(Point p, int i) {
            if (!resizePoints.containsKey(p.y -= i)) {
                p.y -= i;
                return p;
            }
            return _getRowPoint(p, i-1);
        }
    };
    private final boolean mIsTableEmpty;
    
    public TableRowHeader(TajosTable table, JScrollPane scrollPane, boolean isTableEmpty) {
        mTable = table;
        mRows = table.getRowCount();
        mScrollPane = scrollPane;
        mIsTableEmpty = isTableEmpty;
        mGridColor = mTable.getGridColor();
        mTable.addRowSelectionListener(this);
        
        JViewport v = (JViewport) mTable.getParent();
        v.addChangeListener(this);
        
        mTable.getModel().addTableModelListener( this );
        
        Rectangle2D rect = getFontMetrics(mFont).getStringBounds(String.valueOf(mRows), getGraphics());
        setPreferredSize(new Dimension((int)rect.getWidth() + 15, HEIGHT)); // plus 15, so we can have paddings
        setOpaque(true);
        setBackground(Color.WHITE);
        setOpaque(true);
        
        _initSavedRowHeights();
        
        addMouseListener(inputAdapter);
        addMouseMotionListener(inputAdapter);
        
        _updateResizePoints(-1);
    }
    
    private void _initSavedRowHeights() {
        if (!mIsTableEmpty) {
            TajosMenuTextField tableInvoker = mTable.getInvoker();
            List<List<CellsDimension>> cellsDimensions = tableInvoker.getCellsDimension();
            if (cellsDimensions == null)
                return;
            
            int row = 0;
            for (List<CellsDimension> rowDimension : cellsDimensions) {
                mTable.setRowHeight(row, rowDimension.get(0).getCellHeight());
                row++;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int y = -1 - mViewportPos;
        _updateResizePoints(y);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getPreferredSize().width-1, getPreferredSize().height);
        // loop in every rows to draw row header
        for (int row=0; row<mRows; row++) {
            int rowHeight;
            if (mTable.getInitialRowHeight(-1) == null)
                rowHeight = mTable.getRowHeight(row);
            else if (mTable.getInitialRowHeight(row)[0] == row)
                rowHeight = mTable.getInitialRowHeight(-1)[1];
            else
                rowHeight = mTable.getRowHeight(row);
            
            if (row != mRows-1 && row == 0) { // draw the column at index 0
                if (isRowSelected(row)) {
                    g2d.setColor(mSelectedRowColor);
                } else
                    g2d.setColor(mBgColor);
                
                if (!mEnabled)
                    g2d.setColor(disableColor);
                
                g2d.fillRect(0, y+1, getPreferredSize().width-1, rowHeight-1);
                g2d.setColor(mGridColor);
                if (!mEnabled)
                    g2d.setColor(disableColor);
                
                g2d.drawRect(0, y+1, getPreferredSize().width-1, rowHeight-1);
                if (isRowSelected(row)) {
                    BasicStroke stroke = GradeUtils.getBasicSQStroke(2);
                    g2d.setColor(GradeUtils.Colors.darkBlueColor);
                    g2d.setStroke(stroke);
                    g2d.drawLine(getPreferredSize().width-1, y+1, getPreferredSize().width-1, (y+1)+(rowHeight));
                }
                // region: reset the stroke thickness
                BasicStroke stroke = GradeUtils.getBasicSQStroke(1);
                g2d.setStroke(stroke);
                // region end
                _drawText(g2d, y, row, rowHeight);
                y += rowHeight;
                continue;
            }
            // draw the rest of the columns
            if (isRowSelected(row))
                g2d.setColor(mSelectedRowColor);
            else
                g2d.setColor(mBgColor);
            
            if (!mEnabled)
                    g2d.setColor(disableColor);
            
            g2d.fillRect(0, y, getPreferredSize().width-1, rowHeight);
            g2d.setColor(mGridColor);
            
            if (!mEnabled)
                g2d.setColor(disableColor);
            
            g2d.drawRect(0, y, getPreferredSize().width-1, rowHeight);
            if (isRowSelected(row)) {
                BasicStroke stroke = GradeUtils.getBasicSQStroke(2);
                g2d.setColor(GradeUtils.Colors.darkBlueColor);
                g2d.setStroke(stroke);
                g2d.drawLine(getPreferredSize().width-1, y+1, getPreferredSize().width-1, (y+1)+(rowHeight));
            }
            
            // region: reset the stroke thickness
            BasicStroke stroke = GradeUtils.getBasicSQStroke(1);
            g2d.setStroke(stroke);
            // region end
            
            _drawText(g2d, y, row, rowHeight);
            
            y += rowHeight;
        }
        
        // region: fill the extra area
        g2d.setColor(mBgColor);
        g2d.fillRect(0, y+1, getPreferredSize().width, y);
        // region end
        
        g2d.dispose();
    }
    
    private void _drawText(Graphics2D g2d, int y, int row, int rowHeight) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(mFontColor);
        g2d.setFont(mFont);
        String str = String.valueOf(row+1);

        FontMetrics fm = getFontMetrics(mFont);
        Rectangle2D textRect = fm.getStringBounds(str, g2d);

        int textX = ((getPreferredSize().width-1) - (int)textRect.getWidth()) / 2;
        int diffY = (rowHeight - (int)textRect.getHeight()) / 2;
        int textY = y + (rowHeight - diffY);
        
        if (!mEnabled)
            g2d.setColor(disableColor);

        g2d.drawString(str, textX, textY - 1);
    }

    @Override
    public void disable() {
        mEnabled = false;
    }

    @Override
    public void enable() {
        mEnabled = true;
    }
    
    private boolean isRowSelected(int row) {
        return mRowsSelected.containsKey(row);
    }
    
    private int startRow = -1, endRow;
    private void _selectRows(MouseEvent e) {
        Point p = e.getPoint();
        p.y += mViewportPos;
        int row = mTable.rowAtPoint(p);
        
        mTable.setColumnSelectionInterval(0, mTable.getColumnCount()-1);
        
        if (startRow == -1) {
            startRow = row;
            endRow = row;
            
            _select();
        }
        
        if (row != endRow) {
            endRow = row;
            
            _select();
        }
        
        mTable.setRowSelectionInterval(startRow, endRow);
        mTable.revalidate();
        mTable.repaint();
    }
    
    private void _select() {
        JViewport vport = mScrollPane.getColumnHeader();
        TableColumnHeader colHeader = (TableColumnHeader) vport.getComponent(0);
        // region: select all columns
        Map<Integer, Integer> selectedCols = new HashMap<>();
        for (int col : mTable.getSelectedColumns())
            selectedCols.put(col, col);
        // region end
        mRowsSelected.clear();
        // region: select selected rows
        int start  = startRow > endRow ? endRow : startRow;
        int end = startRow > endRow ? startRow : endRow;
        for (int i=start; i<end+1; i++)
            mRowsSelected.put(i, i);

        colHeader.onColumnsSelected(selectedCols);
        onRowsSelected(mRowsSelected);
        // region end
    }

    /**
     * Update the row resize points
     */
    private void _updateResizePoints(int y) {
        if (isResizing)
            return;
        
        if (!resizePoints.isEmpty())
            resizePoints.clear();
            
        int nexPoint = y;
        for (int i=0; i<mTable.getRowCount(); i++) {
            int resizePointMinThreshold = nexPoint + mTable.getRowHeight(i) - 4;
            int resizePointMaxThreshold = nexPoint + mTable.getRowHeight(i) + 4;
            
            for (int j=resizePointMinThreshold; j<=resizePointMaxThreshold; j++)
                resizePoints.put(j, j);
            
            nexPoint += mTable.getRowHeight(i);
        }
    }
    
    public TableSelectionListener getSelectionListener() {
        return this;
    }
    
    public int getRowHeaderViewport() {
        return mViewportPos;
    }

    @Override
    public void setForeground(Color fg) {
        mFontColor = fg;
    }

    /**
     * Implement the ChangeListener
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
        //  Keep the scrolling of the row table in sync with main table
        JViewport viewport = (JViewport) e.getSource();
        mViewportPos = viewport.getViewPosition().y;
        
        revalidate();
        repaint();
    }

    /**
    *  Implement the TableModelListener
    */
    @Override
    public void tableChanged(TableModelEvent e)
    {
        revalidate();
        mRows = mTable.getModel().getRowCount();
    }
    
    @Override
    public void onRowsSelected(Map<Integer, Integer> selectedRows) {
        mRowsSelected = selectedRows;
        
        revalidate();
        repaint();
    }
    
    @Override
    public void selectAllRows(boolean bool) {
        if (bool) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i=0; i<mRows; i++) {
                map.put(i, i);
            }

            mRowsSelected = map;

            revalidate();
            repaint();
            return;
        }
        
        mRowsSelected.clear();
        
        revalidate();
        repaint();
    }

    @Override
    public void onColumnsSelected(Map<Integer, Integer> selectedCols) {}

    @Override
    public void selectAllColumns(boolean bool) {}
}