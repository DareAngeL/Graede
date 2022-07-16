package com.tajos.studio.table;

import com.tajos.studio.components.TajosTable;
import com.tajos.studio.util.GradeUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.core.animation.timing.interpolators.LinearInterpolator;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

/**
 *
 * @author Rene Tajos jr.
 */
public class TableUI extends BasicTableUI {
    
    private double fraction = 0;
    
    private Animator cellXYAnimator;
    private Animator cellFormulatorBorderAnimator;
    
    private Rectangle prevSelectedCellRect = new Rectangle(0, 0, 0, 0);
    private Rectangle nexSelectedCellRect = new Rectangle(0, 0, 0, 0);
    
    private void _initAnimator() {
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(Animator source, double fraction) {
                TableUI.this.fraction = fraction;
                
                if (fraction > 0 && !cellFormulatorBorderAnimator.isRunning()) {
                    table.revalidate();
                    table.repaint();
                }
                
                if (table.isEditing())
                    paintFormulatorBorder((Graphics2D)table.getGraphics());
                else
                    selection((Graphics2D)table.getGraphics());
            }
        };
        
        TimingSource src = new SwingTimerTimingSource();
        cellXYAnimator = new Animator.Builder(src)
            .addTarget(target)
            .setInterpolator(LinearInterpolator.getInstance())
            .setDuration(60, TimeUnit.MILLISECONDS)
            .build();
        
        TimingSource src2 = new SwingTimerTimingSource();
        cellFormulatorBorderAnimator = new Animator.Builder(src2)
            .addTarget(target)
            .setInterpolator(LinearInterpolator.getInstance())
            .setDuration(5000, TimeUnit.MILLISECONDS)
            .setRepeatCount(Animator.INFINITE)
            .setRepeatBehavior(Animator.RepeatBehavior.REVERSE)
            .setEndBehavior(Animator.EndBehavior.RESET)
            .build();
        
        src2.init();
        src.init();
    }
    
    private void _animateCellXY() {
        if (!cellXYAnimator.isRunning()) {
            cellXYAnimator.start();
        }
    }
    
    private void _animateCellFormulatorBorder() {
        if (!cellFormulatorBorderAnimator.isRunning()) {
            cellFormulatorBorderAnimator.start();
        }
    }
    
    private void _stopCellFormulatorAnimation() {
        if (cellFormulatorBorderAnimator.isRunning())
            cellFormulatorBorderAnimator.stop();
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Graphics2D g2D = (Graphics2D) g;
        if (cellXYAnimator == null)
            _initAnimator();
        
        // region: draw the odd cells if there's any
        paintOddCells((Graphics2D)g);
        // region end
        
        if (!table.isEditing()) {
            _stopCellFormulatorAnimation();
            selection(g2D);
        } else {
            paintCellEditor(g2D);
            paintFormulatorBorder(g2D);
        }
        
        // region: paint the resizing indicator when the user resize the row or column
        paintCellResizingIndicator(g);
        // region end
        
        g2D.dispose();
    }
    
    public void paintSchoolIDError(List<int[]> cellError) {
        
    }
    
    public void paintOddCells(Graphics2D g) {
        int selectedRow = table.getSelectedRow();
        int selectedCol = table.getSelectedColumn();
        
        final Map<Integer, Map<Integer, Rectangle>> oddCells = ((TajosTable)table).getOddCells();

        if (!oddCells.isEmpty()) {
            for (Map.Entry<Integer, Map<Integer, Rectangle>> row : oddCells.entrySet()) {
                final int _row = row.getKey();
                if (_row > table.getRowCount()-1) {
                    oddCells.remove(_row);
                    break;
                }

                for (Map.Entry<Integer, Rectangle> col : oddCells.get(_row).entrySet()) {
                    final int _col = col.getKey();
                    // dont draw the cell renderer if this cell is being edited
                    if (table.isEditing() && selectedRow == _row && selectedCol == _col)
                        continue;
                    
                    if (_col > table.getColumnCount()-1) {
                        oddCells.remove(_row);
                        break;
                    }

                    final TableCellRenderer tableCell = table.getCellRenderer(_row, _col);
                    final Component renderer = table.prepareRenderer(tableCell, _row, _col);
                    final Rectangle cellRect = getComponentRect(renderer, _row, _col);
                    
                    TableDefaultCellRenderer label = (TableDefaultCellRenderer) renderer;
                    if (label.getText().isEmpty())
                        continue;

                    Container container = new Container();
                    container.setBackground(Color.WHITE);
                    rendererPane.paintComponent(g, renderer, container, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
                }
            }
        }
    }    
    /**
     * Paint the indicator when the user resize the cell column or row
     * @param g - The graphics
     */
    private void paintCellResizingIndicator(Graphics g) {
        // region: draw the initial row resizing line indicator
        final TajosTable ttable = (TajosTable) table;
        if (ttable.getInitialRowHeight(-1) != null) {
            int row = ttable.getInitialRowHeight(-1)[0];
            Rectangle rowRect = ttable.getCellRect(row, 0, true);
            rowRect.height = ttable.getInitialRowHeight(-1)[1];
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor( GradeUtils.Colors.semiCreamWhiteBlueColor);
            g2d.setStroke(GradeUtils.getBasicSQStroke(2));
            
            int height = (rowRect.y + rowRect.height) - rowRect.y;
            int width = ttable.getWidth();
            g2d.drawRect(0 , rowRect.y, width, height);
            
            g2d.dispose();
        }
        // region end
        // region: draw the initial col resizing line indicator
        if (ttable.getInitialColWidth(-1) != null) {
            int col = ttable.getInitialColWidth(-1)[0];
            Rectangle colRect = ttable.getCellRect(0, col, true);
            colRect.width = ttable.getInitialColWidth(-1)[1];
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor( GradeUtils.Colors.semiCreamWhiteBlueColor);
            g2d.setStroke(GradeUtils.getBasicSQStroke(2));
            
            int width = (colRect.x + colRect.width) - colRect.x;
            int height = ttable.getHeight();
            g2d.drawRect(colRect.x, 0, width, height);
            
            g2d.dispose();
        }
        // region end
    }
    
    /**
     * Paints the cell editor.
     * @param g2d - the graphics use to paint
     */
    private void paintCellEditor(Graphics2D g2d) {
        int cellRow = table.getSelectedRow();
        int cellCol = table.getSelectedColumn();
        final Component editor = table.getEditorComponent();
        
        paintSelectedCell(g2d, null,
            getComponentRect(editor, cellRow, cellCol));
    }
    
    private void paintFormulatorBorder(Graphics2D g2d) {
        TableCellEditor editor = (TableCellEditor) table.getCellEditor();
        
        if (!editor.isOnFormulatingMode()) {
            return;
        }
        
        List<FormulatorCellTarget> targets = editor.getTargets();
        
        int i = 0;
        for (FormulatorCellTarget target : targets) {
            int row = target.getRow();
            int col = target.getCol();
            Color color = target.getBorderColor();
            
            // don't draw if the selected row and column is the same as the
            // editing row and column.
            if (row == getSelectedRows()[0] &&
                col == getSelectedColumns()[0])
            {
                return;
            }
            // draw the range if the target is a range
            if (target.isRange()) {
                int[] rowsRange = target.getRanges().get(0);
                int[] colsRange = target.getRanges().get(1);
                Rectangle selectionRangeRect = _getCellRect(rowsRange, colsRange);
                paintBrokenLineRect(g2d, selectionRangeRect, color, i==targets.size()-1);
                
                _animateCellFormulatorBorder();
                i++;
                continue;
            }
            // end
            Rectangle cellRect = table.getCellRect(row, col, true);
            paintBrokenLineRect(g2d, cellRect, color, i==targets.size()-1);
            
            _animateCellFormulatorBorder();
            i++;
        }
    }
    
    private void paintBrokenLineRect(Graphics2D g2d, Rectangle cellRect, Color color, boolean selected) {
        // region: draw rect
        BasicStroke stroke1 = GradeUtils.getBasicSQStroke(3);
        g2d.setStroke(stroke1);
        g2d.setColor(color);
        g2d.drawRect(cellRect.x, cellRect.y, cellRect.width-1, cellRect.height);
        // region end

        if (selected) {
            // region: draw broken line
            BasicStroke stroke2 = new BasicStroke(3f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_BEVEL, 1f,
                    new float[]{0f, 10f}, (float)(fraction * 100f));

            g2d.setStroke(stroke2);
            g2d.setColor(new Color(227,219,242));
            g2d.drawRect(cellRect.x, cellRect.y, cellRect.width-1, cellRect.height);
            // region end
        }
    }
    
    /**
    * @param color - the selection border color; null if the default color will
    * be used, otherwise, use the color provided by this parameter.
    */
    private void paintSelectedCell(Graphics2D g2d, Color color, Rectangle cellRect) {
        BasicStroke stroke = GradeUtils.getBasicSQStroke(3);
        g2d.setStroke(stroke);
        g2d.setColor(color==null?GradeUtils.Colors.darkBlueColor:color);

        // region: draw rect
        g2d.drawRect(cellRect.x, cellRect.y, cellRect.width-1, cellRect.height);
        // region end
        BasicStroke smallStroke = GradeUtils.getBasicSQStroke(1);
        g2d.setStroke(smallStroke);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(cellRect.x+1, cellRect.y+1, cellRect.width-3, cellRect.height-2);
        // region: fill 
        g2d.setColor(new Color(247,245,251, 100));
        g2d.fillRect(cellRect.x, cellRect.y, cellRect.width-1, cellRect.height);
        // region end
    }
    
    /**
     * Paints a selected cell border if there's any selected cell
     * @param g2d - the graphics use to paint
     */
    private void selection(Graphics2D g2d) {
        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();
        
        if (!(selectedRows.length > 0) || !(selectedCols.length > 0))
            return;
        
        Rectangle selectionRect = _getCellRect(selectedRows, selectedCols);
        
        if (nexSelectedCellRect.x != selectionRect.x ||
            nexSelectedCellRect.y != selectionRect.y ||
            nexSelectedCellRect.getWidth() != selectionRect.getWidth() ||
            nexSelectedCellRect.getHeight() != selectionRect.getHeight())
        {
            if (((TajosTable)table).isDragging()) {
                prevSelectedCellRect = selectionRect;
                paintSelectedCell(g2d, null, selectionRect);
                return;
            }
            
            nexSelectedCellRect = selectionRect;
            _animateCellXY();
        } else {
            paintSelectionWithAnimation(g2d, nexSelectedCellRect);
        }
    }
    
    private Rectangle _getCellRect(int[] rows, int[] cols) {
        int[] firstCellIndices = {rows[0], cols[0]};
        int[] lastCellIndices = {rows[rows.length-1], cols[cols.length-1]};
        
        Rectangle firstRect = table.getCellRect(firstCellIndices[0], firstCellIndices[1], true);
        Rectangle lastRect = table.getCellRect(lastCellIndices[0], lastCellIndices[1], true);
        int width = lastRect.x != firstRect.x ? (lastRect.x - firstRect.x) + lastRect.width : firstRect.width;
        int height = lastRect.y != firstRect.y ? (lastRect.y - firstRect.y) + lastRect.height : firstRect.height;
        
        return new Rectangle(firstRect.x-1, firstRect.y-1, width, height);
    }
    
    private void paintSelectionWithAnimation(Graphics2D g2d, Rectangle nexRect) {
        Rectangle rect = new Rectangle(nexRect);
        
        if ((prevSelectedCellRect.x != rect.x || prevSelectedCellRect.y != rect.y) &&
            prevSelectedCellRect.x != 0 && prevSelectedCellRect.y != 0)
        {
            int moveX = rect.x - prevSelectedCellRect.x;
            int moveY = rect.y - prevSelectedCellRect.y;
            rect.x = (int)(fraction * moveX) + prevSelectedCellRect.x;
            rect.y = (int)(fraction * moveY) + prevSelectedCellRect.y;
        }
        
        if (prevSelectedCellRect.width != rect.width ||
            prevSelectedCellRect.height != rect.height)
        {
            int addWidth = rect.width - prevSelectedCellRect.width;
            int addHeight = rect.height - prevSelectedCellRect.height;
            rect.width = (int)(fraction * addWidth) + prevSelectedCellRect.width;
            rect.height = (int)(fraction * addHeight) + prevSelectedCellRect.height;
        }
        
        prevSelectedCellRect = rect;
        paintSelectedCell(g2d, null, rect);
    }
    
    private Rectangle getComponentRect(Component comp, int row, int col) {
        final Rectangle defaultCellRect = table.getCellRect(row, col, true);
        final Component editor = comp;
        
        if (row == 0 && col == 0)
            return defaultCellRect;
        
        Insets ins = ((JTextComponent)editor).getInsets();
        
        int editorWidth = ins.left + editor.getPreferredSize().width;
        int editorHeight = ins.top + getContentHeight(editorWidth, ((JTextComponent)editor).getText());
        
        boolean hasOddCell = false;
        // region: calculate the new width of the component
        int rW = defaultCellRect.width / editorWidth;
        if (rW < 1 && defaultCellRect.width != 0) {
            int add = editorWidth / defaultCellRect.width;
            
            for (int i=0; i<add; i++) {
                Rectangle nexColRect = table.getCellRect(row, col+(i+1), true);
                int totalRectWidth = defaultCellRect.width + nexColRect.width;
                int r = totalRectWidth / editorWidth;
                
                if (!(r < 1)) {
                    defaultCellRect.width = totalRectWidth;
                    break;
                }
                
                defaultCellRect.width += nexColRect.width;
            }
            hasOddCell = true;
        }
        // region end
        // region: calculate the new height of the component
        int rH = defaultCellRect.height / editorHeight;
        if (rH < 1 && defaultCellRect.height != 0) {
            int add = editorHeight / defaultCellRect.height;
            
            for (int i=0; i<add; i++) {
                Rectangle nexRowRect = table.getCellRect(row+(i+1), col, true);
                int totalRectHeight = defaultCellRect.height + nexRowRect.height;
                int r = totalRectHeight / editorHeight;
                
                if (!(r < 1)) {
                    defaultCellRect.height = totalRectHeight;
                    break;
                }
                
                defaultCellRect.height += nexRowRect.height;
            }
            hasOddCell = true;
        }
        // region end
        defaultCellRect.width -= 1;
        defaultCellRect.height -= 1;
        editor.setBounds(defaultCellRect);
        
        if (hasOddCell)
            ((TajosTable)table).storeOddCells(row, col, defaultCellRect);
        
        return defaultCellRect;
    }
    
    private int getContentHeight(int width, String content) {
        JEditorPane dummyEditorPane = new JEditorPane();
        dummyEditorPane.setSize(width, Short.MAX_VALUE);
        dummyEditorPane.setText(content);
        return dummyEditorPane.getPreferredSize().height;
    }
    
    private int [] getSelectedColumns() {
        return table.getSelectedColumns();
    }
    
    private int [] getSelectedRows() {
        return table.getSelectedRows();
    }
}