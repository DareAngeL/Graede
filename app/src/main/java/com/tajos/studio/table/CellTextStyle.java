package com.tajos.studio.table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import com.tajos.studio.interfaces.TextStyles;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr.
 */
public class CellTextStyle implements Serializable {
    
    private Color fontColor;
    private Color bgColor;
    private List<TextStyles> styles = new ArrayList<>(); // list of styles : bold, italic, underlined
    private List<TextStyles> horizontlAlignment = new ArrayList<>(); //TextStyles.LEFT_ALIGN;
    private List<TextStyles> verticalAlignment = new ArrayList<>();//TextStyles.TOP_ALIGN;
    
    private SelectedAll selectedAllList;
    
    private int row;
    private int col;
    private int columnWidth;
    
    public CellTextStyle() {
        horizontlAlignment.add(TextStyles.LEFT_ALIGN);
        verticalAlignment.add(TextStyles.TOP_ALIGN);
    }
    
    public CellTextStyle(CellTextStyle copyStyle) {
        if (copyStyle == null) {
            horizontlAlignment.add(TextStyles.LEFT_ALIGN);
            verticalAlignment.add(TextStyles.TOP_ALIGN);
            return;
        }
        
        fontColor = copyStyle.fontColor;
        bgColor = copyStyle.bgColor;
        styles = copyStyle.styles;
        horizontlAlignment = copyStyle.horizontlAlignment;
        verticalAlignment = copyStyle.verticalAlignment;
    }
    
    public void setColumnWidth(int width) {
        columnWidth = width;
    }
    
    public int getColumnWidth() {
        return columnWidth;
    }
    
    public void putSelectedAllList(SelectedAll selectedAll) {
        selectedAllList = selectedAll;
    }
    
    public SelectedAll getSelectedAllList() {
        return selectedAllList;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setFontColor(Color color) {
        fontColor = color;
    }
    
    public void setBackgroundColor(Color color) {
        bgColor = color;
    }
    
    public void setStyles(List<TextStyles> _styles) {
        styles = _styles;
    }
    
    public void setHorizontalAlignment(TextStyles alignment) {
        horizontlAlignment.removeAll(horizontlAlignment);
        horizontlAlignment.add(alignment);
    }
    
    public void setVerticalAlignment(TextStyles alignment) {
        verticalAlignment.removeAll(verticalAlignment);
        verticalAlignment.add(alignment);
    }
    
    public Color getFontColor() {
        return fontColor;
    }
    
    public Color getBgColor() {
        return bgColor;
    }
    
    public List<TextStyles> getStyles() {
        return styles;
    }
    
    public TextStyles getHorizontalAlignment() {
        return this.horizontlAlignment.get(0);
    }
    
    public TextStyles getVerticalAlignment() {
        return this.verticalAlignment.get(0);
    }
    
    public static class SelectedAll {
        
        @JsonProperty("selectedallrow")
        private Map<Integer, Color> selectedAllRows = new HashMap<>();
        @JsonProperty("selectedallcols")
        private Map<Integer, Color> selectedAllCols = new HashMap<>();
        
        @JsonIgnore
        public int getSelectedAllRowSize() {
            return selectedAllRows.size();
        }
        @JsonIgnore
        public int getSelectedAllColSize() {
            return selectedAllCols.size();
        }
        @JsonIgnore
        public Color getColorInRow(int row) {
            return selectedAllRows.get(row);
        }
        @JsonIgnore
        public Color getColorInCol(int col) {
            return selectedAllCols.get(col);
        }
        @JsonIgnore
        public void removeSelectedAllRow(int row) {
            if (!selectedAllRows.containsKey(row))
                return;
            
            selectedAllRows.remove(row);
        }
        @JsonIgnore
        public void removeSelectedAllCol(int col) {
            if (!selectedAllCols.containsKey(col))
                return;
            
            selectedAllCols.remove(col);
        }

        public void addSelectedAllRow(int row, Color color) {
            if (color == Color.WHITE) {
                if (selectedAllRows.containsKey(row)) {
                    selectedAllRows.remove(row);
                }
                return;
            }
            selectedAllRows.put(row, color);
        }
        
        public Map<Integer, Color> getSelectedAllRow() {
            return selectedAllRows;
        }
        
        public Map<Integer, Color> getSelectedAllCols() {
            return selectedAllCols;
        }
        
        public void setSelectedAllCols(Map<Integer, Color> col) {
            selectedAllCols = col;
        }
        
        public void setSelectedAllRows(Map<Integer, Color> row) {
            selectedAllRows = row;
        }

        public void addSelectedAllCol(int col, Color color) {
            if (color == Color.WHITE) {
                if (selectedAllCols.containsKey(col)) {
                    selectedAllCols.remove(col);
                }
                return;
            }
            
            selectedAllCols.put(col, color);
        }
        @JsonIgnore
        public boolean isOnSelectedAllRowBlock(int row) {
            if (selectedAllRows.isEmpty())
                return false;
            
            return selectedAllRows.containsKey(row);
        }
        @JsonIgnore
        public boolean isOnSelectedAllColBlock(int col) {
            if (selectedAllCols.isEmpty())
                return false;
            
            return selectedAllCols.containsKey(col);
        }
    }
}
