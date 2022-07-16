package com.tajos.studio.components;

import com.tajos.studio.components.TajosTable.RowsAndColumnsCalculator;
import com.tajos.studio.table.TableModel;
import com.tajos.studio.util.ComponentResizer;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TajosScrollPane extends JScrollPane implements ChangeListener {

    private final int SCROLL_BAR_ALPHA_ROLLOVER = 100;
    private final int SCROLL_BAR_ALPHA = 50;
    private final int SB_SIZE = 10;
    private final Color THUMB_COLOR = GradeUtils.Colors.darkBlueColor;
    private static int THUMB_SIZE = 5;
    private Color mOldBackgroundColor;
    private Color mTrackColor;
    private JScrollBar mVerticalScrollBar;
    private JScrollBar mHorizontalScrollBar;
    
    private boolean isMouseExitedDetected = false;
    private boolean mEnableSBarHiding = false;
    private TajosTable mTable;
    
    private boolean isDragging;
    private int mAdjustmentValueX = -1;
    private int mAdjustmentValueY = -1;
    private ComponentResizer mComponentResizer;
    
    //private MouseState mMouseState;
    
    /**
     * @Enum types of mouse state;
     */
    private enum MouseState {
        ENTERED, EXITED
    }
    
    /**
     * @MouseListener detects mouse inputs
     */
    private final MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            isDragging = false;
            mAdjustmentValueY = -1;
            mAdjustmentValueX = -1;

            if (isMouseExitedDetected && mEnableSBarHiding) {
                setMouseState(MouseState.EXITED);
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            if (!mEnableSBarHiding)
                return;
            
            isMouseExitedDetected = false;
            setMouseState(MouseState.ENTERED);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            if (!mEnableSBarHiding)
                return;
            
            isMouseExitedDetected = true;
            if (isDragging)
                return;

            setMouseState(MouseState.EXITED);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (mTable == null)
                return;
            
            if (isVerticalScrollOnBottom() && !isDragging && e.getPreciseWheelRotation() > 0) {
                 mTable.addRow();
            }
            
            if (isHorizontalScrollOnRight() && !isDragging && e.getPreciseWheelRotation() > 0) {
                _addColumn();
            }
            
            if (isHorizontalScrollOnRight() && !isDragging) {
                _addColumn();
            }
            
            if (e.getPreciseWheelRotation() < 0) {
                _updateRemovingRow(false);
                _updateRemovingColumn(false);
            }
        }
    };
    
    int adjustmentValue = -1;
    private final AdjustmentListener verticalSbarListener = new AdjustmentListener() {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (mTable == null)
                return;
            
            int value = e.getValue();
            if (adjustmentValue != -1) {
                // moved upwards
                if (value < adjustmentValue && isDragging) {
                    _updateRemovingRow(false);
                }
            }
            
            adjustmentValue = value;
        }
    };
    
    /**
     * @MotionListener Detects when the user is scrolling the scroll bar
     */
    
    private final MouseMotionAdapter mouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            isDragging = true;
            
            if (mTable == null)
                return;
            
            // region: dragging vertical thumb
            int y = e.getY();
            if (mAdjustmentValueY != -1) {
                if (mAdjustmentValueY > y) {
                    _updateRemovingRow(false);
                }
            }
            mAdjustmentValueY = y;
            // region end
            //region: draggin horizontal thumb
            int x = e.getX();
            if (mAdjustmentValueX != -1) {
                if (mAdjustmentValueX > x) {
                    _updateRemovingColumn(false);
                }
            }
            mAdjustmentValueX = x;
            // region end
        }
    };
    
    private void _addColumn() {
        int row = -1;
        int col = -1;
        String editingTx = "";
        // get the row and column index and cell text
        if (mTable.isEditing()) {
            row = mTable.getEditingRow();
            col = mTable.getEditingColumn();
            JTextPane editor = (JTextPane) mTable.getEditorComponent();
            editingTx = editor.getText();
        }
        // end
        mTable.addColumn();
        if (row != -1 && col != -1) {
            boolean success = mTable.editCellAt(row, col);
            if (success) {
                mTable.changeSelection(row, col, 
                        false, false);

                JTextPane editor = (JTextPane) mTable.getEditorComponent();
                editor.setText(editingTx);
            }
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        _updateRowsAndColumns();
    }
    
    public void fireStateChanged() {
        _updateRowsAndColumns();
    }
    
    private void _updateRowsAndColumns() {
        if (mTable != null && !getHorizontalScrollBar().isVisible()) {
            _updateColumns();
        }
        if (mTable != null && !getVerticalScrollBar().isVisible()) {
            _updateRows();
        }
    }
    
    public void attachRootFrameResizer(ComponentResizer resizer) {
        mComponentResizer = resizer;
        
        _initComponentResizerListener();
    }
    
    private void _updateRemovingRow(boolean isMouseReleased) {
        int prefRows = mTable.getPreferredRowCount() > mTable.getMaxOccupiedCells()[0] ?
                        mTable.getPreferredRowCount() : mTable.getMaxOccupiedCells()[0];
        
        // if the current row count is bigger than the pref cell row count
        if (mTable.getPreciseRowCount() > prefRows) {
            mTable.removeLastRow();
            if (isMouseReleased) {
                _updateRemovingRow(isMouseReleased);
            }
        }
    }
    
    private void _updateRemovingColumn(boolean isMouseReleased) {
        int prefCols = mTable.getPreferredColumnCount() > mTable.getMaxOccupiedCells()[1] ?
                        mTable.getPreferredColumnCount() : mTable.getMaxOccupiedCells()[1] + 3;
        
        if (mTable.getColumnCount() > prefCols) {
            // refocus the cell editing, if it was on editing mode
            int row = -1;
            int col = -1;
            String editingTx = "";
            // get the row and column index and cell text
            if (mTable.isEditing()) {
                row = mTable.getEditingRow();
                col = mTable.getEditingColumn();
                JTextPane editor = (JTextPane) mTable.getEditorComponent();
                editingTx = editor.getText();
            }
            // end
            
            mTable.removeLastColumn();
            if (isMouseReleased) {
                _updateRemovingColumn(isMouseReleased);
            }
            
            if (row != -1 && col != -1) {
                boolean success = mTable.editCellAt(row, col);
                if (success) {
                    mTable.changeSelection(row, col, 
                            false, false);

                    JTextPane editor = (JTextPane) mTable.getEditorComponent();
                    editor.setText(editingTx);
                }
            }
            // end
        }
    }
    
    private void _updateColumns() {
        if (!(mTable.getModel() instanceof TableModel))
            return;
        
        // region: update columns
        RowsAndColumnsCalculator m = new TajosTable().new RowsAndColumnsCalculator(this);
        int diffCols = m.getCalculatedCols() < mTable.getMaxOccupiedCells()[1] ? 
                mTable.getMaxOccupiedCells()[1]-mTable.getColumnCount() : m.getCalculatedCols() - mTable.getColumnCount();
        
        if (diffCols > 0) {
            for (int i=0; i<diffCols; i++) {
                mTable.addColumn();
            }
        } else {
            for (int i=0; i<Math.abs(diffCols); i++) {
                mTable.removeLastColumn();
            }
        }
        mTable.setPreferredColumnCount(m.getCalculatedCols());
        // region end
    }
    
    private void _updateRows() {
        if (!(mTable.getModel() instanceof TableModel))
            return;
        
        // region: update rows
        RowsAndColumnsCalculator m = new TajosTable().new RowsAndColumnsCalculator(this);
        int diffRows = m.getCalculatedRows() < mTable.getMaxOccupiedCells()[0] ?
                mTable.getMaxOccupiedCells()[0]-mTable.getRowCount() : m.getCalculatedRows()- mTable.getRowCount();
        if (diffRows > 0) {
            for (int i=0; i<diffRows; i++) {
                mTable.addRow();
            }
        } else {
            for (int i=0; i<Math.abs(diffRows); i++) {
                mTable.removeLastRow();
            }
        }
        mTable.setPreferredRowCount(m.getCalculatedRows());
        // region end
    }
    
    public void resetViewPosition() {
        JViewport port = getViewport();
        port.setViewPosition(new Point(0, 0));
    }
    
    /**
     * The default constructor
     */
    public TajosScrollPane() {
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        getViewport().addChangeListener(this);
        mVerticalScrollBar = new JScrollBar();
        mHorizontalScrollBar = new JScrollBar();
        
        mVerticalScrollBar.setUnitIncrement(16);
        mHorizontalScrollBar.setUnitIncrement(20);
        
        mVerticalScrollBar.setOrientation(JScrollBar.VERTICAL);
        mHorizontalScrollBar.setOrientation(JScrollBar.HORIZONTAL);
        mVerticalScrollBar.addMouseListener(mouseListener);
        mVerticalScrollBar.addMouseMotionListener(mouseMotionListener);
        mVerticalScrollBar.addAdjustmentListener(verticalSbarListener);
        
        mHorizontalScrollBar.addMouseListener(mouseListener);
        mHorizontalScrollBar.addMouseMotionListener(mouseMotionListener);
        mHorizontalScrollBar.setUI(new ModernScrollBarUI(this));
        mVerticalScrollBar.setUI(new ModernScrollBarUI(this));
        setVerticalScrollBar(mVerticalScrollBar);
        setHorizontalScrollBar(mHorizontalScrollBar);
    }

    public TajosScrollPane(Component view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public TajosScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, vsbPolicy, hsbPolicy);
    }

    public TajosScrollPane(Component view, int vsbPolicy, int hsbPolicy) {

        setBorder(null);

        // Set ScrollBar UI
        JScrollBar _verticalScrollBar = getVerticalScrollBar();
        _verticalScrollBar.setOpaque(false);
        _verticalScrollBar.setUI(new ModernScrollBarUI(this));

        JScrollBar _horizontalScrollBar = getHorizontalScrollBar();
        _horizontalScrollBar.setOpaque(false);
        _horizontalScrollBar.setUI(new ModernScrollBarUI(this));

        setLayout(new ScrollPaneLayout() {
            private static final long serialVersionUID = 5740408979909014146L;

            @Override
            public void layoutContainer(Container parent) {
                Rectangle availR = ((JScrollPane) parent).getBounds();
                availR.x = availR.y = 0;

                // viewport
                Insets insets = parent.getInsets();
                availR.x = insets.left;
                availR.y = insets.top;
                availR.width -= insets.left + insets.right;
                availR.height -= insets.top + insets.bottom;
                if (viewport != null) {
                    viewport.setBounds(availR);
                }

                boolean vsbNeeded = isVerticalScrollBarfNecessary();
                boolean hsbNeeded = isHorizontalScrollBarNecessary();

                // vertical scroll bar
                Rectangle vsbR = new Rectangle();
                vsbR.width = SB_SIZE;
                vsbR.height = availR.height - (hsbNeeded ? vsbR.width : 0);
                vsbR.x = availR.x + availR.width - vsbR.width;
                vsbR.y = availR.y;
                if (vsb != null) {
                    vsb.setBounds(vsbR);
                }

                // horizontal scroll bar
                Rectangle hsbR = new Rectangle();
                hsbR.height = SB_SIZE;
                hsbR.width = availR.width - (vsbNeeded ? hsbR.height : 0);
                hsbR.x = availR.x;
                hsbR.y = availR.y + availR.height - hsbR.height;
                if (hsb != null) {
                    hsb.setBounds(hsbR);
                }
            }
        });

        // Layering
        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);

        viewport.setView(view);
    }
    
    private void _initComponentResizerListener() {
        if (mComponentResizer == null)
            return;
        
        mComponentResizer.setMouseReleasedListener(() -> {
            // on mouse released
            _updateColumns();
            _updateRows();
        });
    }

    public void attachTable(TajosTable table) {
        mTable = table;
    }
    
    public boolean isVerticalScrollOnBottom() {
        JScrollBar s = getVerticalScrollBar();
        return s.getValue() + s.getVisibleAmount() == s.getMaximum();
    }
    
    public boolean isHorizontalScrollOnRight() {
        JScrollBar s = getHorizontalScrollBar();
        return s.getValue() + s.getVisibleAmount() == s.getMaximum();
    }    
    /**
     * 
     * @return the thumb size of this scroll pane
     */
    public static int getThumbTrackSize() {
        return THUMB_SIZE + 4;
    }
    
    /**
     * 
     * @return {@code true} if the vertical scroll bar is necessary, {@code false} if its not necessary
     */
    private boolean isVerticalScrollBarfNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getHeight() > viewRect.getHeight();
    }

    /**
     * 
     * @return {@code true} if the horizontal scroll bar is necessary, {@code false} if its not necessary
     */
    private boolean isHorizontalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getWidth() > viewRect.getWidth();
    }
    
    /**
     * @description sets the color of the track
     * @param color the possible color for the track
     */
    public void setTrackColor(Color color) {
        mTrackColor = color;
    }
    
    /**
     * @description sets the transparency of this scroll pane
     * @param isTransparent {@code true} if transparent, else {@code false} if not transparent
     */
    public void setTransparent(boolean isTransparent) {
        if (isTransparent) {
            mOldBackgroundColor = getViewport().getBackground();
            getViewport().setBackground(GradeUtils.Colors.transparent);
            return;
        }
        
        getViewport().setBackground(mOldBackgroundColor!=null?mOldBackgroundColor:Color.BLACK);
    }
    
    public void setEnableScrollBarHiding(boolean b) {
        mEnableSBarHiding = b;
    }
    
    /**
     * @description sets the mouse state on this scroll pane
     * @param state the {@Enum type} mouse state
     */
    private void setMouseState(MouseState state) {
        if (state == MouseState.ENTERED) {
            ModernScrollBarUI sBarUI = (ModernScrollBarUI) mVerticalScrollBar.getUI();
            sBarUI.hideThumb(false);
            revalidate();
        } else {
            ModernScrollBarUI sBarUI = (ModernScrollBarUI) mVerticalScrollBar.getUI();
            sBarUI.hideThumb(true);
            revalidate();
        }
    }

    /**
     * @Class extending the {@BasicScrollBarUI} and overrides all necessary methods
     */
    private class ModernScrollBarUI extends BasicScrollBarUI {

        private final JScrollPane sp;
        private boolean mHideThumb = false;

        public ModernScrollBarUI(TajosScrollPane sp) {
            this.sp = sp;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }
        
        public void hideThumb(boolean isHide) {
            mHideThumb = isHide;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            final Color parentBgColor = c.getParent().getBackground();
            
            Graphics2D g2D = (Graphics2D) g;
            g2D.setColor(mTrackColor!=null?mTrackColor:parentBgColor);

            int orientation = scrollbar.getOrientation();
            int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE + 4 : trackBounds.width;
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ? trackBounds.height : THUMB_SIZE + 4;
            height = Math.max(height, THUMB_SIZE);

            trackBounds.width = width;
            trackBounds.height = height;
            
            if (orientation == JScrollBar.VERTICAL) {
                mVerticalScrollBar.setPreferredSize(new Dimension(width, height));
            } else {
                mHorizontalScrollBar.setPreferredSize(new Dimension(width, height));
            }
            
            g2D.fillRect(0, 0, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
            int orientation = scrollbar.getOrientation();
            int x = orientation == JScrollBar.VERTICAL ? thumbBounds.x + 2 : thumbBounds.x;
            int y = orientation == JScrollBar.HORIZONTAL ? thumbBounds.y + 2 : thumbBounds.y; 
            
            int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE : thumbBounds.width;
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ? thumbBounds.height : THUMB_SIZE;
            height = Math.max(height, THUMB_SIZE);
            
            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setColor(new Color(THUMB_COLOR.getRed(), THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
            
            if (orientation == JScrollBar.VERTICAL) {
                if (mHideThumb) {
                    graphics2D.fillRoundRect(x, y, 0, height, 5, 5);
                } else {
                    graphics2D.fillRoundRect(x, y, width, height, 5, 5);
                }
            } else {
                if (mHideThumb) {
                    graphics2D.fillRoundRect(x, y, width, 0, 5, 5);
                } else {
                    graphics2D.fillRoundRect(x, y, width, height, 5, 5);
                }
            }
            
            graphics2D.dispose();
        }

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            sp.repaint();
        }
        
        public void setThumbSize(int size) {
            THUMB_SIZE = size;
        }

        /**
         * Invisible Buttons, to hide scroll bar buttons
         */
        private class InvisibleScrollBarButton extends JButton {

            private static final long serialVersionUID = 1552427919226628689L;

            private InvisibleScrollBarButton() {
                setOpaque(false);
                setFocusable(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
    
    /**
     * @NOTSUPPORTED
     * @param bg 
     */
    @Override
    public void setBackground(Color bg) {}
}
