package com.tajos.studio.table;

import com.tajos.studio.components.TajosTable;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.tajos.studio.interfaces.TextStyles;
import javax.swing.text.StyleContext;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TableDefaultCellRenderer extends JTextPane implements TableCellRenderer {
    private final TajosTable mTable;
    private final Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    
    private static Map<Integer, Map<Integer, CellTextStyle>> mCellStyles = new HashMap<>();
    private static CellTextStyle.SelectedAll mSelectedAll = new CellTextStyle.SelectedAll();
    
    public TableDefaultCellRenderer(TajosTable table) {
        mTable = table;
        // region: init textpane editor
        setForeground(GradeUtils.Colors.darkBlueColor);
        setOpaque(true);
        setFont(GradeUtils.getDefaultFont(12));
        setEditable(false);
    }
    
    public static void setSelectedAll(CellTextStyle.SelectedAll all) {
        if (all == null) {
            mSelectedAll = new CellTextStyle.SelectedAll();
            return;
        }
        
        mSelectedAll = all;
    }
    
    public static void removeSelectedAllCol(int col) {
        if (mSelectedAll.getSelectedAllCols().isEmpty() ||
            !mSelectedAll.getSelectedAllCols().containsKey(col))
                return;
        
        mSelectedAll.getSelectedAllCols().remove(col);
    }
    
    public static CellTextStyle.SelectedAll getSelectedAll() {
        return mSelectedAll;
    }
    
    public void setCellStyles(Map<Integer, Map<Integer, CellTextStyle>> styles) {
        if (styles ==  null) {
            mCellStyles.clear();
            return;
        }
        
        mCellStyles = styles;
    }
    
    public void putCellStyles(int _row, int _col, CellTextStyle _style) {
        Map<Integer, CellTextStyle> existingCol = mCellStyles.get(_row);
        
        Map<Integer, CellTextStyle> colMap = new HashMap<>();
        if (existingCol != null) {
            // region: put the existing stylized renderer first
            for (Map.Entry<Integer, CellTextStyle> entry : existingCol.entrySet()) {
                final int col = entry.getKey();
                final CellTextStyle style = entry.getValue();
                
                colMap.put(col, style);
            }
            // region end
        }
        // set the column width and row height for this cell
        // then add the new one
        colMap.put(_col, _style);
        
        mCellStyles.put(_row, colMap);
    }
    
    public static void removeCellStyle(int _row, int _col) {
        if (mCellStyles == null || mCellStyles.get(_row) == null)
            return;
        
        if (!mCellStyles.get(_row).isEmpty() && _col != -1) {
            mCellStyles.get(_row).remove(_col);
            return;
        }
        
        mCellStyles.remove(_row);
    }
    
    public static Map<Integer, Map<Integer, CellTextStyle>> getCellTextStyle() {
        return mCellStyles;
    }
    
    public static void clearCellStyles() {
        mCellStyles.clear();
    }
    
    public static boolean isHorizontalAlignmentStyle(TextStyles style) {
        switch (style) {
            case LEFT_ALIGN -> {
                return true;
            }
            case CENTER_ALIGN -> {
                return true;
            }
            case RIGHT_ALIGN -> {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isVerticalAlignmentStyle(TextStyles style) {
        switch (style) {
            case TOP_ALIGN -> {
                return true;
            }
            case MIDDLE_ALIGN -> {
                return true;
            }
            case BOTTOM_ALIGN -> {
                return true;
            }
        }
        return false;
    }
    
    private void setBold() {
        _setStyleText(TextStyles.BOLD);
    }
    
    private void setItalic() {
        _setStyleText(TextStyles.ITALIC);
    }
    
    private void setUnderline() {
        _setStyleText(TextStyles.UNDERLINED);
    }
    
    private void setLeftAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_LEFT);
    }
    
    private void setRightAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_RIGHT);
    }
    
    private void setCenterAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_CENTER);
    }
    
    private void _setStyleText(TextStyles textStyle) {
        switch (textStyle) {
            case BOLD -> {
                StyledDocument doc = getStyledDocument();
                Style style = addStyle("bold", null);
                StyleConstants.setBold(style, true);

                doc.setCharacterAttributes(0, doc.getLength(), style, false);
            }
            case ITALIC -> {
                StyledDocument doc = getStyledDocument();
                Style style = addStyle("italic", null);
                StyleConstants.setItalic(style, true);

                doc.setCharacterAttributes(0, doc.getLength(), style, false);
            }
            case UNDERLINED -> {
                StyledDocument doc = getStyledDocument();
                Style style = addStyle("underlined", null);
                StyleConstants.setUnderline(style, true);

                doc.setCharacterAttributes(0, doc.getLength(), style, false);
            }
            default -> throw new AssertionError();
        }
    }
    
    private void setHorizontalAlignment(int constant) {
        StyledDocument doc = getStyledDocument();
        Style style = addStyle(String.valueOf(constant), null);
        StyleConstants.setAlignment(style, constant);
        doc.setParagraphAttributes(0, doc.getLength(), style, false);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        CellTextStyle cellStyle = _getCellTextStyle(row, column);
        
        setText((String) value);
        
        renderStyles(cellStyle);
        renderForeground(cellStyle, row, column);
        renderBackground(cellStyle, row);
        renderHorizontalAlignment(cellStyle);
        
        return this;
    }
    
    private void renderHorizontalAlignment(CellTextStyle _cellStyle) {
        if (_cellStyle == null) {
            setLeftAlign();
            return;
        }
        
        switch (_cellStyle.getHorizontalAlignment()) {
            case LEFT_ALIGN -> {
                setLeftAlign();
            }
            case CENTER_ALIGN -> {
                setCenterAlign();
            }
            case RIGHT_ALIGN -> {
                setRightAlign();
            }
        }
    }
    
    private void renderStyles(CellTextStyle _cellStyle) {
        if (_cellStyle == null) {
            restartCell();
            return;
        }
        
        if (_cellStyle.getStyles().isEmpty()) {
            restartCell();
        }
        
        for (TextStyles style : _cellStyle.getStyles()) {
            switch (style) {
                case BOLD -> {
                    setBold();
                }
                case ITALIC -> {
                    setItalic();
                }
                case UNDERLINED -> {
                    setUnderline();
                }
            }
        }
    }
    
    private void renderForeground(CellTextStyle _cellStyle, int row, int col) {
        if (_cellStyle == null)
            return;
        
        setFontColor(_cellStyle.getFontColor(), row, col);
    }
    
    private void setFontColor(Color color, int row, int col) {
        if (color == null)
            return;
        
        if (row == 0 && col == 0) {
            setForeground(color);
            return;
        }
        
        StyledDocument doc = getStyledDocument();
        Style attribSet = addStyle("", null);
        StyleConstants.setForeground(attribSet, color);
        
        doc.setCharacterAttributes(0, doc.getLength(), attribSet, false);
    }
    
    private void renderBackground(CellTextStyle _cellStyle, int row) {
        if (!mTable.getFilterData().isEmpty() && 
            row > mTable.getFilterData().size()) 
        {
            setBackground(GradeUtils.Colors.semiCreamWhiteBlueColor);
            return;
        }
        
        if (_cellStyle == null) {
            setBackground(Color.WHITE);
            
            return;
        }
        
        if (_cellStyle.getBgColor() == null)
            setBackground(Color.WHITE);
        
        if (_cellStyle.getBgColor() != null) {
            setBackground(_cellStyle.getBgColor());
        }
    }
    
    private CellTextStyle _getCellTextStyle(int row, int col) {
        if (mCellStyles.get(row) == null) {
            return null;
        }
        
        CellTextStyle style = mCellStyles.get(row).get(col);
        if (style == null) {
            return null;
        }
        
        return mCellStyles.get(row).get(col);
    }
    
    private void restartCell() {
        StyledDocument doc = getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
    }
    
   /**
     * Overridden for performance reasons.
     * @since 1.5
     */
    @Override
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * @param r
     */
    @Override
    public void repaint(Rectangle r) {}

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}