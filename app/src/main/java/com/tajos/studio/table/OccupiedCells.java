package com.tajos.studio.table;

import java.util.List;

/**
 * This is a class for holding the value of cells which is not empty or null
 * @author Rene Tajos Jr.
 */
public class OccupiedCells {

    private final int mRow;
    private final int mCol;
    private int mIndex;
    
    private static int index = -1;
    
    public OccupiedCells(int row, int col) {
        ++index;
        mIndex = index;
        mRow = row;
        mCol = col;
    }
    
    public int getRow() {
        return mRow;
    }
    
    public int getColumn() {
        return mCol;
    }
    
    public int getIndex() {
        return mIndex;
    }
    
    public void setIndex(int i) {
        mIndex = i;
    }
    
    public static class Manager {
        
        public static void decrementIndex() {
            index--;
        }
        
        public static void resetIndex() {
            index = -1;
        }
        
        public static int [] getMaxOccupiedCell(List<OccupiedCells> lst) {
            int maxRow = -1, maxCol = -1;
            for (OccupiedCells m : lst) {
                if (m.getRow() >= maxRow)
                    maxRow = m.getRow() +1;
                
                if (m.getColumn() >= maxCol)
                    maxCol = m.getColumn() +1;
            }
            
            return new int[] {maxRow, maxCol};
        }
        
        public static int getOccupiedCellIndex(List<OccupiedCells> lst, int row, int col) {
            for (OccupiedCells m : lst) {
                if (m.getRow() == row && m.getColumn() == col) {
                    return m.getIndex();
                }
            }
            
            return -1;
        }
        
        public static void updateIndexes(List<OccupiedCells> lst) {
            
            int i = 0;
            for (OccupiedCells m : lst) {
                m.setIndex(i);
                i++;
            }
        }
    }
}