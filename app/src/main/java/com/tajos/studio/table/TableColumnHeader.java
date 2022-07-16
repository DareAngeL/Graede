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
import javax.swing.table.TableColumn;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TableColumnHeader extends JComponent implements ChangeListener, TableModelListener, TableSelectionListener, TableModel.ColumnListener {

    private int mViewportPos = 0;
    private final int FINAL_MIN_COL_WIDTH = 75;
    private int MIN_COL_WIDTH = 75;
    
    private Color disableColor = new Color(187,187,187, 80);
    
    private Map<Integer, Integer> mSelectedCols = new HashMap<>();
    private Map<Integer, Integer> mResizePoints = new HashMap<>();
    private final TajosTable mTable;
    private int mCols;
    
    private final Color mGridColor;
    private Color mFontColor = GradeUtils.Colors.darkBlueColor;
    private final Color mSelectedRowColor = GradeUtils.Colors.semiCreamWhiteBlueColor;
    private final Color mBgColor = new Color(247,245,251);
    private Font mFont = GradeUtils.getDefaultFont(12);
    
    private final JScrollPane mTableScrollPane;
    
    private char[] initialColumnNames = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
        'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    
    private final Map<Integer, String> columnNames = new HashMap<>();
    private final Map<String, Integer> namesColumn = new HashMap<>();
    
    private boolean mEnabled = true;
    private int ID_MIN_WIDTH;
    private final boolean mIsTableEmpty;
    
    public TableColumnHeader(TajosTable table, JScrollPane tableScrollPane, boolean isTableEmpty) {
        mTable = table;
        mTableScrollPane = tableScrollPane;
        mIsTableEmpty = isTableEmpty;
        mCols = mTable.getColumnCount();
        mGridColor = mTable.getGridColor();
        mTable.addColumnSelectionListener(this);
        
        mTableScrollPane.getViewport().addChangeListener(this);
        
        TableModel model = (TableModel) mTable.getModel();
        model.addTableModelListener( this );
        model.addColumnListener(this);
        _updateColumnSize();
        
        setPreferredSize(new Dimension(WIDTH, 30));
        setOpaque(true);
        setBackground(Color.WHITE);
        
        addMouseListener(inputAdapter);
        addMouseMotionListener(inputAdapter);
        
        mTable.revalidate();
        mTable.repaint();
    }
    
    private final MouseInputAdapter inputAdapter = new MouseInputAdapter() {
        private int mouseX = -1;
        private int colToResize;

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!mEnabled)
                return;
            
            Point p = e.getPoint();
            
            if (mResizePoints.containsKey((int)p.getX())) {
                GradeUtils.setCustomCursor(TableColumnHeader.this, GradeUtils.resizeHorizontalCursorStr);
                return;
            }
            
            GradeUtils.setCustomCursor(TableColumnHeader.this, GradeUtils.downArrowIconName);
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!mEnabled || mTable.isEditing())
                return;
            
            if (SwingUtilities.isRightMouseButton(e))
                return;
            
            if (getCursor() == GradeUtils.getCustomCursor(GradeUtils.downArrowIconName)) {
                _selectColumns(e);
                return;
            }
            
            int x = e.getX();
            if (mouseX != -1) {
                int elapse = x - mouseX;
                int oldColWidth = mTable.getInitialColWidth(colToResize)[1];
                int w = colToResize == 0 ? ID_MIN_WIDTH + 10 : MIN_COL_WIDTH;
                int colWidth = Math.max(w, oldColWidth + elapse);
                mTable.setInitialColWidth(colToResize, colWidth);   
            }
            mouseX = x;
            
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!mEnabled)
                return;
            
            mouseX = -1;
            startCol = -1; 
            endCol = -1;
            
            int colWidth = mTable.getInitialColWidth(colToResize)[1];
            mTable.getColumnModel().getColumn(colToResize).setPreferredWidth(colWidth);
            mTable.clearInitialColWidth();
            
            mTable.revalidate();
            mTable.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!mEnabled || mTable.isEditing())
                return;
            
            mTable.setSelectedHeader(TajosTable.HeaderType.COLUMN_H);
            
            if (getCursor() != GradeUtils.getCustomCursor(GradeUtils.downArrowIconName)) {
                mouseX = e.getX();
                // region: get the columns point to resize
                final Point p = e.getPoint();
                p.x += mViewportPos;
                colToResize = mTable.columnAtPoint(_getColumnPoint(p, 9));
                return;
                // region end
            }
            
            // region: selects all cells on this column index
            if (SwingUtilities.isLeftMouseButton(e))
                _selectColumns(e);
            //region end
        }
        /**
         * Locate the column point to resize
         * @param p The event Point
         * @param i The number difference of where to locate the col
         */
        private Point _getColumnPoint(Point p, int i) {
            if (!mResizePoints.containsKey(p.x -= i)) {
                p.x -= i;
                return p;
            }
            return _getColumnPoint(p, i-1);
        }
    };

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        int x = -1 - mViewportPos;
        _updateResizePoints(x);
        columnNames.clear(); // always clear the column names first
        // loop in every cols to draw column header
        for (int col=0; col<mTable.getColumnModel().getColumnCount(); col++) {
            int colWidth;
            if (mTable.getInitialColWidth(-1) == null) {
                colWidth = mTable.getColumnModel().getColumn(col).getWidth();
                //GradeUtils.log("null");
            } else if (mTable.getInitialColWidth(col)[0] == col) {
                colWidth = mTable.getInitialColWidth(-1)[1];
            } else {
                colWidth = mTable.getColumnModel().getColumn(col).getWidth();
            }
            if (col != mCols-1 && col == 0) { // draw the column at index 0
                if (mSelectedCols.containsKey(col)) {
                    g2d.setColor(mSelectedRowColor);
                } else
                    g2d.setColor(mBgColor);
                
                if (!mEnabled) {
                    g2d.setColor(disableColor);
                }
                
                g2d.fillRect(x, 0, colWidth, getPreferredSize().height-1);
                g2d.setColor(mGridColor);
                if (!mEnabled)
                    g2d.setColor(disableColor);
                
                g2d.drawRect(x+1, 0, colWidth, getPreferredSize().height-1);
                
                if (mSelectedCols.containsKey(col)) {
                    // region: draw the line in the bottom
                    BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
                    g2d.setStroke(stroke);
                    g2d.setColor(GradeUtils.Colors.darkBlueColor);
                    g2d.drawLine(x+1, getHeight()-2, x+colWidth, getHeight()-2);
                    // region end
                }
                
                // region: reset the stroke thickness
                BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
                g2d.setStroke(stroke);
                // region end

                _drawText(g2d, x, col, colWidth);
                x+= colWidth;
                continue;
            }
            // draw the rest of the columns
            if (mSelectedCols.containsKey(col))
                g2d.setColor(mSelectedRowColor);
            else
                g2d.setColor(mBgColor);
            
            if (!mEnabled) {
                g2d.setColor(disableColor);
            }
            
            g2d.fillRect(x, 0, colWidth, getPreferredSize().height-1);
            g2d.setColor(mGridColor);
            if (!mEnabled)
                g2d.setColor(disableColor);
            
            g2d.drawRect(x, 0, colWidth, getPreferredSize().height-1);
            
            if (mSelectedCols.containsKey(col)) {
                // region: draw the line in the bottom
                BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
                g2d.setStroke(stroke);
                g2d.setColor(GradeUtils.Colors.darkBlueColor);
                g2d.drawLine(x+2, getHeight()-2, x+colWidth, getHeight()-2);
                // region end
            }
            
            // region: reset the stroke thickness
            BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g2d.setStroke(stroke);
            // region end
            
            _drawText(g2d, x, col, colWidth);
            
            x += colWidth;
        }
        
        // region: fill the extra area
        g2d.setColor(mBgColor);
        g2d.fillRect(x+1, 0, x, getPreferredSize().height);
        // region end
        
        g2d.dispose();
    }
    
    private void _drawText(Graphics2D g2d, int x, int col, int colWidth) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(mFontColor);
        g2d.setFont(mFont);
        
        String colName = _getColumnName(col, 1);
        columnNames.put(col, colName);
        namesColumn.put(colName, col);

        FontMetrics fm = getFontMetrics(mFont);
        Rectangle2D textRect = fm.getStringBounds(colName, g2d);

        int textX = x + (colWidth - (int)textRect.getWidth()) / 2;
        int diffY = ((getPreferredSize().height-1) - (int)textRect.getHeight()) / 2;
        int textY = ((getPreferredSize().height-1) - diffY);
        
        if (!mEnabled) {
            g2d.setColor(disableColor);
        }

        g2d.drawString(colName, textX, textY - 1);
    }
    
    private void _updateColumnSize() {
        // region: determine the preferred width of every column on this column header
        String colHugeLastName = _getHugeColumnName(mCols);
        Rectangle2D rect = _getTextRect(colHugeLastName);
        
        Rectangle2D emailTxRect = _getTextRect("School ID");
        ID_MIN_WIDTH = (int)emailTxRect.getWidth();
        
        int colWidth = rect.getWidth() > MIN_COL_WIDTH || MIN_COL_WIDTH > FINAL_MIN_COL_WIDTH ? 
                (int)rect.getWidth() + 15 : MIN_COL_WIDTH;
        
        MIN_COL_WIDTH = colWidth;
        // region end
        if (mIsTableEmpty) {
            normalizedColumnsWidth(emailTxRect, colWidth);
        } else {
            TajosMenuTextField tableInvoker = mTable.getInvoker();
            List<List<CellsDimension>> cellsDimension = tableInvoker.getCellsDimension();
            for (int i=0; i<mTable.getColumnCount(); i++) {
                TableColumn tableColumn = mTable.getColumnModel().getColumn(i);
                
                if (cellsDimension == null) {
                    normalizedColumnsWidth(emailTxRect, colWidth);
                    continue;
                }
                
                if (i >= cellsDimension.get(0).size()) {
                    tableColumn.setPreferredWidth(colWidth);
                    continue;
                }
                
                int width = cellsDimension.get(0).get(i).getCellWidth();

                tableColumn.setPreferredWidth(width);
            }
        }
    }
    
    private void normalizedColumnsWidth(Rectangle2D emailTxRect, int colWidth) {
        for (int i=0; i<mTable.getColumnCount(); i++) {
            TableColumn tableColumn = mTable.getColumnModel().getColumn(i);
            if (i == 0) {
                tableColumn.setPreferredWidth((int)emailTxRect.getWidth() + 10);
                continue;
            }

            tableColumn.setPreferredWidth(colWidth);
        }
    }
    
    @Override
    public void disable() {
        mEnabled = false;
        invalidate();
        repaint();
    }
    
    @Override
    public void enable() {
        mEnabled = true;
        invalidate();
        repaint();
    }
    
    private int startCol = -1, endCol;
    private void _selectColumns(MouseEvent e) {
        Point p = e.getPoint();
        p.x += mViewportPos;
        int col = mTable.columnAtPoint(p);
        
        mTable.setRowSelectionInterval(0, mTable.getRowCount()-1);
        
        if (startCol == -1) {
            startCol = col;
            endCol = col;
            
            _select();
        }
        
        if (col != endCol) {
            endCol = col;
            
            _select();
        }
        
        mTable.setColumnSelectionInterval(startCol, endCol<0? 0 : endCol);
        
        mTable.revalidate();
        mTable.repaint();
    }
    
    private void _select() {
        JViewport rhViewport = mTableScrollPane.getRowHeader();
        TableRowHeader rh = (TableRowHeader) rhViewport.getComponent(0);
        // region: select all rows
        Map<Integer, Integer> selectedRows = new HashMap<>();
        for (int row : mTable.getSelectedRows())
            selectedRows.put(row, row);
        // region end
        mSelectedCols.clear();
        // region: select selected columns
        int start  = startCol > endCol ? endCol : startCol;
        int end = startCol > endCol ? startCol : endCol;
        for (int i=start; i<end+1; i++)
            mSelectedCols.put(i, i);
        
        rh.onRowsSelected(selectedRows);
        onColumnsSelected(mSelectedCols);
        // region end
    }
    
    /**
     * This will find the appropriate column name for a column header
     * @param col The column where we want to have a column name
     * @param batch Batch means, from A to Z -> it is one batch, so every recursive is a batch
     * e.g: if there are three recursive calls on this method, it is already 3 batch, and if we
     * have {@code column} indexed at 0, we will have the column name like this -> AAA
     * explanation:
     *          ^The letter is A because what sitting on index 0 is the letter A
     *          ^The letter was tripled because we have 3 batch or 3 recursive calls on the method
     * @return
     */
    private String _getColumnName(int col, int batch) {
        if (col > initialColumnNames.length-1) {
            return _getColumnName((col - (initialColumnNames.length-1)) - 1, batch+1);
        }
        
        String str = "";
        for (int i=0; i<batch; i++) {
            str = str.concat(String.valueOf(initialColumnNames[col]));
        }
        return str;
    }
    
    private String _getHugeColumnName(int cols) {
        int batch = cols / initialColumnNames.length;
        int rem = cols % initialColumnNames.length;
        String hugeStr = "";
        for (int i=1; i<=batch; i++) {
            for (int j=0; j<initialColumnNames.length; j++) {
                String str = "";
                for (int k=0; k<=i; k++)
                    str = str.concat(String.valueOf(initialColumnNames[j]));
                // if first index
                if (hugeStr == null) {
                    hugeStr = str;
                    continue;
                }
                // else
                Rectangle2D hugeStrRect = _getTextRect(hugeStr);
                Rectangle2D strRect = _getTextRect(str);
                
                hugeStr = strRect.getWidth() > hugeStrRect.getWidth() ? 
                        str : hugeStr;
            }
        }
        
        for (int i=0; i<rem; i++) {
            String str = "";
            for (int j=0; j<=batch+1; j++)
                str = str.concat(String.valueOf(initialColumnNames[i]));
            
            Rectangle2D hugeStrRect = _getTextRect(hugeStr);
            Rectangle2D strRect = _getTextRect(str);

            hugeStr = strRect.getWidth() > hugeStrRect.getWidth() ? 
                    str : hugeStr;
        }
        
        return hugeStr;
    }
    
    private Rectangle2D _getTextRect(String str) {
        return getFontMetrics(mFont).getStringBounds(str, getGraphics());
    }
    
    /**
     * Update the column resize points
     */
    private void _updateResizePoints(int x) {
        if (!mResizePoints.isEmpty())
            mResizePoints.clear();
            
        int nexPoint = x;
        for (int i=0; i<mTable.getColumnCount(); i++) {
            final int colWidth = mTable.getColumnModel().getColumn(i).getWidth();
            int resizePointMinThreshold = nexPoint + colWidth - 4;
            int resizePointMaxThreshold = nexPoint + colWidth + 4;
            
            for (int j=resizePointMinThreshold; j<=resizePointMaxThreshold; j++)
                mResizePoints.put(j, j);
            
            nexPoint += colWidth;
        }
    }
    
    public int getMinimumColumnWidth() {
        return MIN_COL_WIDTH;
    }

    /**
     * This will return the column name of which the provided column index
     * parameter was mapped to.
     * @param index The index position of the column
     * @return Returns the column name.
     */
    public String getColumnNameAt(int index) {
        if (index >= columnNames.size()) {
            return null;
        }
        
        return columnNames.get(index);
    }
    
    /**
     * This will return the column index of which the provided colName 
     * parameter was mapped to.
     * @param colName the column name
     * @return Returns the index of the provided column name, if the provided
     * column name does not exist in the Map then return -1.
     */
    public int getColumnAtName(String colName) {
        if (!namesColumn.containsKey(colName))
            return -1;
        
        return namesColumn.get(colName);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JViewport viewport = (JViewport) e.getSource();
        mViewportPos = viewport.getViewPosition().x;
        revalidate();
        repaint();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        revalidate();
        mCols = mTable.getModel().getColumnCount();
    }
    
    @Override
    public void OnColumnAdded() {
        mCols = mTable.getModel().getColumnCount();
        _updateColumnSize();
    }
    
    @Override
    public void OnColumnRemoved() {
        _updateColumnSize();
    }

    @Override
    public void onRowsSelected(Map<Integer, Integer> selectedRows) {}

    @Override
    public void onColumnsSelected(Map<Integer, Integer> selectedCols) {
        mSelectedCols = selectedCols;
        
        revalidate();
        repaint();
    }
    
    @Override
    public void selectAllColumns(boolean bool) {
        if (bool) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i=0; i<mCols; i++) {
                map.put(i, i);
            }

            mSelectedCols = map;
            
            revalidate();
            repaint();
            return;
        }
        
        mSelectedCols.clear();
        
        revalidate();
        repaint();
    }

    @Override
    public void selectAllRows(boolean bool) {}
}