package com.tajos.studio.table;

import com.tajos.studio.components.TajosTable;
import com.tajos.studio.util.GradeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author Rene Tajos Jr
 */
public class Formulator {
    
    private final static char FORMULA_SIGN = '=';
    private final static char RANGE_SIGN = ':';
    
    private final String SUM = "SUM";
    private final String AVERAGE = "AVERAGE";
    
    private String mExpression;
    private String mResult;
    
    private static char [] operators = { '+', '-', '*', '/', '^' };
    
    public Formulator() {}
    
    public Formulator(String exp) {
        mExpression = exp;
    }

    public static char[] getOperators() {
        return operators;
    }
    
    public static boolean isAnOperator(char op) {
        for (char operator : operators) {
            if (operator == op) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isRangeSign(char _char) {
        return RANGE_SIGN == _char;
    }
    
    public static boolean isAnExpression(String str) {
        if (str == null)
            return false;
        
        if (!(str.length() > 0))
            return false;
        
        final char n = str.charAt(0);
        return n == FORMULA_SIGN;
    }
    
    public void setExpression(String exp) {
        mExpression = exp;
    }

    /**
     * <b> Calculate the cell's formula. </b>
     * <p></p>
     * If any of the declared variables has the value of -1, it means
     * that the value has error and thus, cannot be calculated
     * @param table
     * @param columnHeader
     */
    public void calculate(TajosTable table, TableColumnHeader columnHeader) {
        // region: calculate Special operation SUM()
        String expression = _calculateSpecialOperator(table, columnHeader, 
                mExpression, SUM);
        // region end
        if (expression == null)
            return;
        // region: calculate Special Operation AVERAGE()
        expression = _calculateSpecialOperator(table, columnHeader, 
                expression, AVERAGE);
        // region end
        if (expression == null)
            return;
        // region: calculate the non-special operators.
        String pattern = "([a-zA-Z]+)([0-9]+)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(mExpression);
        
        final Map<String, Double> variables = new HashMap<>();
        int i = 0;
        boolean error = false;
        while (matcher.find()) {
            String cellName = matcher.group();
            
            int[] row_col = getCellRowAndColumn(cellName, columnHeader);
            if (row_col == null) {
                error = true;
                break;
            }
            
            try {
                final String valueStr = (String)table.getValueAt(row_col[0], row_col[1]);
                final double value = Double.parseDouble(valueStr);
                
                variables.put(cellName, value);
            } catch (Exception e) {
                error = true;
                break;
            }
            
            i++;
        }
        
        if (error) {
            mResult = "!ERROR";
            return;
        }
        
        try {
            Expression e = new ExpressionBuilder(expression.replace("=", ""))
                .variables(variables.keySet())
                .build()
                .setVariables(variables);
        
            mResult = String.valueOf(e.evaluate());
            char c = mResult.charAt(mResult.length()-1);
            if (c == '0' && mResult.charAt(mResult.length()-2) == '.')
                mResult = mResult.replaceFirst("(\\.)([0])", "");
            
        } catch (Exception e) {
            mResult = "!ERROR";
        }
        // region end
    }
    
    /**
     * Calculate with the special operators.
     * @param table
     * @param columnHeader
     * @param expression
     * @param SpecialOperator
     * @return Returns the new expression.
     */
    private String _calculateSpecialOperator(
            TajosTable table, 
            TableColumnHeader columnHeader,
            String expression,
            String SpecialOperator
    )
    {
        int specOpIndex = mExpression.indexOf(SpecialOperator);
        double[] specOpResult = _getSpecialOperatorResult(table, columnHeader, 
                SpecialOperator, specOpIndex);
        
        // -2 means all the SPECIAL OPERATORS has been calculated or there's no SUM() at all.
        while (specOpResult[1] != -2) {
            if (specOpResult[0] == -1) {
                mResult = "!ERROR";
                return null;
            }
            // region: change the SPECIAL OPERATORS with it's value
            StringBuilder newTx = new StringBuilder();
            newTx.append(expression, 0, (int)specOpResult[1]);
            newTx.append(String.format("%.3f", specOpResult[0]));
            newTx.append(expression, expression.indexOf(")", (int)specOpResult[1])+1, 
                    expression.length());
            
            expression = newTx.toString();
            mExpression = expression;
            // region end
            
            specOpIndex = expression.indexOf(SpecialOperator);
            specOpResult = _getSpecialOperatorResult(table, columnHeader, 
                SpecialOperator, specOpIndex);
        }
        
        return expression;
    }
    
    private double [] _getSpecialOperatorResult(
            TajosTable table, 
            TableColumnHeader columnHeader,
            String SpecialOperator,
            int specOpIndex
    )
    {
        switch (SpecialOperator) {
            case SUM -> {
                return _SUM(table, columnHeader, specOpIndex);
            }
            case AVERAGE -> {
                return _AVERAGE(table, columnHeader, specOpIndex);
            }
        }
        
        return null;
    }
    
    /**
     * Calculates the sum of the expression that was given if the expression
     * has a SUM() special operation
     * @return Returns the sum result and the index of the word "SUM" <br>
     * Sum is at the position [0] <br>
     * Position of the word "SUM" is at the index [1].
     */
    @SuppressWarnings("UseSpecificCatch")
    private double[] _SUM(TajosTable table, TableColumnHeader columnHeader, int index) {
        double sum = 0;
        
        final List<int[]> rangeLst = _getRange(columnHeader, index, SUM);
        // if null, it means, can't find the SUM() special operation. Thus, consider
        // this to be done.
        if (rangeLst == null)
            return new double[] {-2, -2};
        // if empty, it means, there's an error while getting the start and end range.
        if (rangeLst.isEmpty()) {
            return new double[] {-1,-1};
        }
        
        int[] startRangeCellR_C = rangeLst.get(0); // starting range
        int[] endRangeCellR_C = rangeLst.get(1); // ending range
        
        try {
            for (int row=startRangeCellR_C[0]; row<endRangeCellR_C[0]+1; row++) {
                for (int col=startRangeCellR_C[1]; col<endRangeCellR_C[1]+1; col++) {
                    double value = Double.parseDouble((String) table.getValueAt(row, col));
                    sum += value;
                }
            }
        } catch (Exception e) {
            return new double[] {-1, -1};
        }
        
        return new double[] {sum, index};
    }
    
    @SuppressWarnings("UseSpecificCatch")
    private double[] _AVERAGE(TajosTable table, TableColumnHeader columnHeader, int index) {
        double average = 0;
        
        final List<int[]> rangeLst = _getRange(columnHeader, index, AVERAGE);
        // if null, it means, can't find the AVERAGE() special operation. Thus, consider
        // this to be done.
        if (rangeLst == null)
            return new double[] {-2, -2};
        // if empty, it means, there's an error while getting the start and end range.
        if (rangeLst.isEmpty())
            return new double[] {-1,-1};
        
        int[] startRangeCellR_C = rangeLst.get(0); // starting range
        int[] endRangeCellR_C = rangeLst.get(1); // ending range
        
        try {
            int valueCount = 0;
            for (int row=startRangeCellR_C[0]; row<endRangeCellR_C[0]+1; row++) {
                for (int col=startRangeCellR_C[1]; col<endRangeCellR_C[1]+1; col++) {
                    double value = Double.parseDouble((String) table.getValueAt(row, col));
                    average += value;
                    valueCount++;
                }
            }
            
            average /= valueCount;
        } catch (Exception e) {
            return new double[] {-1, -1};
        }
        
        return new double[] {average, index};
    }
    
    /**
     * Get the row and column of the range.
     * @param columnHeader The table column header
     * @param index The index position of the special operation in a string.
     * @param specOp The Special Operation.
     * @return <p>Returns the rows and columns of the starting range and end range.
     * Index at 0 is the starting range, index at 1 is the ending range.</p>
     * <p>Returns null if there's no existing specified Special Operation.</p>
     * <p>Returns empty range if the Special Operation format is incorrect. </p>
     */
    private List<int[]> _getRange(TableColumnHeader columnHeader, int index, String specOp) {
        final int specOpIndex = index;
        List<int[]> rangeList = new ArrayList<>();
        
        if (specOpIndex != -1) {
            int closeParenthesisIndex = mExpression.indexOf(")", specOpIndex+3);
            
            StringBuilder sumBuilder = new StringBuilder();
            sumBuilder.append(mExpression, specOpIndex, closeParenthesisIndex+1);
            
            String[] ranges = sumBuilder.toString().replaceAll(
                "("+specOp+"\\(|\\))", "")
                .split(":");
            
            if (!(ranges.length > 1) || ranges.length > 2) {
                return rangeList;
            }
            
            int[] startRangeCellR_C = getCellRowAndColumn(ranges[0], columnHeader);
            int[] endRangeCellR_C = getCellRowAndColumn(ranges[1], columnHeader);
            rangeList.add(startRangeCellR_C);
            rangeList.add(endRangeCellR_C);
        } else {
            rangeList = null;
        }
        
        return rangeList;
    }
    
    public String getResult() {
        return mResult;
    }

    public String getExpression() {
        return mExpression;
    }
    
    public static int [] getCellRowAndColumn(String cell, TableColumnHeader columnHeader) {
        
        int row, col;
        int[] rows_cols = {-1, -1};
        // try to parse to integer
        try {
            row = Integer.parseInt(cell.replaceAll("[a-zA-Z]", ""));
        } catch (NumberFormatException e) {
            // just return if an exception happen
            return null;
        }
        String colName = cell.replaceAll("[0-9]", "");
        if (colName.isEmpty() || colName.isBlank())
            return null;

        col = columnHeader.getColumnAtName(colName);
        if (col == -1)
            return null;
        
        rows_cols[0] = row-1;
        rows_cols[1] = col;
        
        return rows_cols;
    }
    
    public static boolean isOneRowFormula(String formula) {
        String pattern = "([a-zA-Z]+)([0-9]+)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(formula);
        
        int prevRow = -1;
        try {
            while (matcher.find()) {
                String match = matcher.group();
                int row = Integer.parseInt(match.replaceAll("([a-zA-Z])+", ""));
                if (prevRow != row && prevRow != -1)
                    return false;
                
                prevRow = row;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String changeCellRows(String formula, int to) {
        return formula.replaceAll("([0-9]+)", String.valueOf(to));
    }
    
    public static List<FormulatorCellTarget> getFormulatorCellTargets(String str, TableColumnHeader colHeader, 
            List<FormulatorCellTarget> _targets) 
    {
        String[] _targetsArr = str.replaceAll(
            "(\\(|\\)|=|SUM|AVERAGE)", "").split("(\\+|\\-|\\*|/|\\^)");
        
        List<FormulatorCellTarget> formulatorCellTargets = new ArrayList<>();
        
        int index = 0;
        for (String target : _targetsArr) {
            // if this target is a range execute this block
            if (target.contains(":")) {
                String[] _ts = target.split(":");
                
                int[] rowsRange = new int[]{-1,-1}, colsRange = new int[]{-1,-1};
                int i = 0;
                for (String _t : _ts) {
                    if (i >= 2)
                        break;
                    
                    int[] row_col = getCellRowAndColumn(_t, colHeader);
                    if (row_col == null)
                        break;
                    
                    rowsRange[i] = row_col[0];
                    colsRange[i] = row_col[1];
                    
                    i++;
                }
                
                if (rowsRange[0] == -1)
                    continue;
                
                FormulatorCellTarget rangeTarget = new FormulatorCellTarget();
                rangeTarget.setRange(rowsRange, colsRange);
                if (index < _targets.size()) {
                    rangeTarget.setBorderColor(_targets.get(index).getBorderColor());
                } else {
                    rangeTarget.setBorderColor(GradeUtils.getRandomColour());
                }
                
                formulatorCellTargets.add(rangeTarget);
                index++;
                continue;
            }
            // otherwise, execute the codes below
            int[] row_col = getCellRowAndColumn(target, colHeader);
            if (row_col == null)
                continue;
            
            FormulatorCellTarget t = new FormulatorCellTarget(
                    row_col[0], row_col[1]);
            
            if (index < _targets.size()) {
                t.setBorderColor(_targets.get(index).getBorderColor());
            } else {
                t.setBorderColor(GradeUtils.getRandomColour());
            }
            
            formulatorCellTargets.add(t);
            index++;
        }
        
        return formulatorCellTargets;
    }
}