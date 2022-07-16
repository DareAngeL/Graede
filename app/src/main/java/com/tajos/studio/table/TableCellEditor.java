package com.tajos.studio.table;

import com.tajos.studio.GradeApp;
import com.tajos.studio.WorkbookManager;
import com.tajos.studio.components.TajosScrollPane;
import com.tajos.studio.components.TajosTable;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.interfaces.TextStyles;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TableCellEditor extends DefaultCellEditor {

    private TajosScrollPane mScrollPane = null;
    
    private int ENTER_KEY_PRESSED = 0;
    private int ESC_KEY_PRESSED = 0;
    private int ON_NAVIGATING = 0;
    
    private int currentCol = -1;

    JTextPane editor;
    JLabel schoolIDCellRenderer = new JLabel("School ID");
    private DefaultStyledDocument doc = null;
    private TajosTable mTable = null;
    
    private boolean isOnFormulatingMode = false;
    
    List<FormulatorCellTarget> targets = new ArrayList<>();
    
    final Style defaultStyle = StyleContext.
        getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
    
    private final DocumentListener docListener = new DocumentListener() {
        TableColumnHeader colHeader;
        @Override
        public void insertUpdate(DocumentEvent e) {
            WorkbookManager.instance().updateSaveState(false);
            mTable.disableRibbon(true);
            // region: toggle true/false if the cell editor is on formulating mode
            // and update the formulator cell targets;
            SwingUtilities.invokeLater(() -> {
                if (Formulator.isAnExpression(editor.getText())) {
                    isOnFormulatingMode = true;
                    
                    if (colHeader == null)
                        colHeader = 
                            (TableColumnHeader) mScrollPane.getColumnHeader().getComponent(0);
                    
                    targets = Formulator.getFormulatorCellTargets(
                        editor.getText(), colHeader, targets);
                }
                mTable.revalidate();
                mTable.repaint();
            });
            // region end
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            // region: toggle true/false if the cell editor is on formulating mode
            // and update the formulator cell targets;
            SwingUtilities.invokeLater(() -> {
                if (colHeader == null)
                    colHeader = 
                        (TableColumnHeader) mScrollPane.getColumnHeader().getComponent(0);
                    
                targets = Formulator.getFormulatorCellTargets(
                    editor.getText(), colHeader, targets);
                
                mTable.revalidate();
                mTable.repaint();
            });
            // region end
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            mTable.revalidate();
            mTable.repaint();
        }
    };
    
    private final TajosTable.OnTablePressedListener tablePressedListener = 
        new TajosTable.OnTablePressedListener() {
        @Override
        public void onDragging(int[] row, int[] col) {
            _onTableDragging(row, col);
        }

        @Override
        public void onPressed(int row, int col) {
            initFormulatorCellTargets(row, col, null, null);
        }

        @Override
        public void onReleased() {
            mRowsRange[0] = -1; mRowsRange[1] = -1;
            mColsRange[0] = -1; mColsRange[1] = -1;
        }
    };
    
    private final KeyBinds keyListener = new KeyBinds() {
        @Override
        public void keyPressed(KeyEvent e) {
            // adds new line when alt+enter is press
            if (ALT_ENTER(e)) {
                StringBuilder str = new StringBuilder(editor.getText());
                str.append("\n");
                editor.setText(str.toString());
                return;
            }
            // stops cell editing when key enter is press
            if (ENTER(e)) {
                ENTER_KEY_PRESSED = 1;
                stopCellEditing();
                mTable.disableRibbon(false);
                ENTER_KEY_PRESSED = 0;
            }
            // stops cell editing when key escape is press
            if (ESCAPE(e)) {
                ESC_KEY_PRESSED = 1;
                stopCellEditing();
                mTable.disableRibbon(false);
                ESC_KEY_PRESSED = 0;
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    };

    public TableCellEditor(TajosTable table) {
        super(new JTextPane());
        mScrollPane = null;
        mTable = table;
        mTable.addTablePressedListener(tablePressedListener);
        // region: init textpane editor
        StyleContext sc = new StyleContext();
        doc = new DefaultStyledDocument(sc);
        doc.addDocumentListener(docListener);
        editor = new JTextPane(doc);
        // region end
        editor.setBackground(Color.WHITE);
        editor.setFont(GradeUtils.getDefaultFont(12));
        editor.setForeground(GradeUtils.Colors.darkBlueColor);
        editor.addKeyListener(keyListener);
    }
    
    private int [] mRowsRange = {-1,-1}, mColsRange = {-1,-1};
    
    private void _onTableDragging(int[] rows, int[] cols) {
        int[] rowsRange = new int[2], colsRange = new int[2];
        rowsRange[0] = rows[0]<rows[rows.length-1]?rows[0]:rows[rows.length-1];
        rowsRange[1] = rows[rows.length-1]>rows[0]?rows[rows.length-1]:rows[0];
        
        colsRange[0] = cols[0]<cols[cols.length-1]?cols[0]:cols[cols.length-1];
        colsRange[1] = cols[cols.length-1]>cols[0]?cols[cols.length-1]:cols[0];
        
        if (isOnFormulatingMode) {
            if (rowsRange[0] != rowsRange[1] || colsRange[0] != colsRange[1]) {
                if (rowsRange[1] != mRowsRange[1] || colsRange[1] != mColsRange[1] ||
                    rowsRange[0] != mRowsRange[0] || colsRange[0] != mColsRange[0]) 
                {
                    mRowsRange = rowsRange; 
                    mColsRange = colsRange;
                    initFormulatorCellTargets(-1, -1, rowsRange, colsRange);
                }
            }
        }
    }
    
    public void initFormulatorCellTargets(int row, int col,
            int[] rowsRange, int[] colsRange) 
    {
        if (mScrollPane == null) {
            JViewport port = (JViewport) mTable.getParent();
            mScrollPane = (TajosScrollPane) port.getParent();
        }
        
        boolean isRange = rowsRange != null;
        
        if (mTable.isEditing() && isOnFormulatingMode) {
            
            if (isRange) {
                _rangeFormula(rowsRange, colsRange);
                return;
            }
            
            _defaultFormula(row, col);
        }
    }
    
    private void _rangeFormula(int [] rowsRange, int [] colsRange) {
        GradeApp.executor().execute(() -> {
            TableColumnHeader colHeader = 
                    (TableColumnHeader) mScrollPane.getColumnHeader().getComponent(0);
            
            String startColName = colHeader.getColumnNameAt(colsRange[0])
                            .concat(String.valueOf(rowsRange[0]+1));
            String endColName = colHeader.getColumnNameAt(colsRange[1])
                            .concat(String.valueOf(rowsRange[1]+1));

            String editorTx = editor.getText();
            
            final List<Object> lastOpIndex = _getLastOperatorIndex(editorTx);
            int lastOperatorIndex = (int) lastOpIndex.get(0);
            boolean isLastSpecialOperator = (boolean) lastOpIndex.get(1);

            StringBuilder newTxt = new StringBuilder();
            newTxt.append(editor.getText(), 0, ++lastOperatorIndex);
            if ((editorTx.length() > lastOperatorIndex &&
                editorTx.charAt(lastOperatorIndex) == '(') ||
                isLastSpecialOperator)
            {
                newTxt.append("(");
            }

            newTxt.append(startColName);
            newTxt.append(":");
            newTxt.append(endColName);

            if (editorTx.charAt(editorTx.length()-1) == ')' || isLastSpecialOperator) {
                newTxt.append(")");
            }

            editor.setText(newTxt.toString());
            targets = Formulator.getFormulatorCellTargets(
                editor.getText(), colHeader, targets);
            
            int cPIndex = newTxt.toString().indexOf(")", 
                    newTxt.toString().length()-1);
            editor.setCaretPosition(cPIndex==-1?editor.getText().length():cPIndex);
        });
    }
    
    private void _defaultFormula(int row, int col) {
        GradeApp.executor().execute(() -> {
            TableColumnHeader colHeader = 
                    (TableColumnHeader) mScrollPane.getColumnHeader().getComponent(0);
            String colName = colHeader.getColumnNameAt(col)
                    .concat(String.valueOf(row+1));

            char[] editorLastChar = new char[1];
            // get the last character of the editor
            editor.getText().getChars(editor.getText().length()-1,
                editor.getText().length(), editorLastChar, 0);
            // if the last character is an operator, then concatenate the colName
            if (Formulator.isAnOperator(editorLastChar[0])) {
                editor.setText(editor.getText().concat(colName));
                targets = Formulator.getFormulatorCellTargets(
                    editor.getText(), colHeader, targets);
                
                editor.setCaretPosition(editor.getText().length());
            } else {
                // otherwise, insert the colName.
                String editorTx = editor.getText();
                
                final List<Object> lastOpIndex = _getLastOperatorIndex(editorTx);
                int lastOperatorIndex = (int) lastOpIndex.get(0);
                boolean isLastSpecialOperator = (boolean) lastOpIndex.get(1);

                // construct the new text
                StringBuilder newTxt = new StringBuilder();
                newTxt.append(editorTx, 0, ++lastOperatorIndex);

                if ((editorTx.length() > lastOperatorIndex &&
                    editorTx.charAt(lastOperatorIndex) == '(') ||
                    isLastSpecialOperator)
                {
                    newTxt.append("(");
                }

                newTxt.append(colName);

                if (editorTx.charAt(editorTx.length()-1) == ')' || isLastSpecialOperator) {
                    newTxt.append(")");
                }
                // end
                editor.setText(newTxt.toString());
                // region: get all the target cells
                targets = Formulator.getFormulatorCellTargets(
                        editor.getText(), colHeader, targets);
                // region end
                // region: position the caret
                int cPIndex = newTxt.toString().indexOf(")", 
                    newTxt.toString().length()-1);
                editor.setCaretPosition(cPIndex==-1?editor.getText().length():cPIndex);
                // region end
            }
        });
    }
    
    /**
     * Get the index of the last operator.
     * @param editorTx
     * @return Returns a list of the index of the last operator and a boolean
     * of whether the last operator is special operator or not.
     */
    private List<Object> _getLastOperatorIndex(String editorTx) {
        int lastOperatorIndex = 0;
        boolean isLastSpecOp = false;
        final List<Object> arr = new ArrayList<>();
        
        for (int i=editorTx.length()-1; i > -1; i--) {
            StringBuilder b = new StringBuilder();
            b.append(editorTx, i, editorTx.length());
            if (b.toString().contains("SUM")) {
                lastOperatorIndex = i + 2;
                isLastSpecOp = true;
                break;
            } else if (b.toString().contains("AVERAGE")) {
                lastOperatorIndex = i + 6;
                isLastSpecOp = true;
                break;
            }

            if (Formulator.isAnOperator(editorTx.charAt(i))) {
                lastOperatorIndex = i;
                break;
            }
        }
        arr.add(lastOperatorIndex);
        arr.add(isLastSpecOp);
        
        return arr;
    }

    public void setTargets(List<FormulatorCellTarget> targets) {
        this.targets = targets;
    }

    public List<FormulatorCellTarget> getTargets() {
        return targets;
    }

    public boolean isOnFormulatingMode() {
        return isOnFormulatingMode;
    }

    @Override
    public boolean stopCellEditing() {
        if (Formulator.isAnExpression(editor.getText()) &&
            ENTER_KEY_PRESSED == 0 && ESC_KEY_PRESSED == 0 &&
            ON_NAVIGATING == 0)
                return false;
        
        final Object[] errResult = hasErrors();
        if (errResult != null) {
            final boolean hasError = (boolean) errResult[0];
            final String errMsg = (String) errResult[1];

            if (hasError) {
                JOptionPane.showMessageDialog(new JFrame(),
                        errMsg, "Oops",
                        JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }
        
        boolean stop = super.stopCellEditing();
        resetEditor();
        GradeUtils.log("stop");
        return stop;
    }
    
    public void resetEditor() {
        isOnFormulatingMode = false;
        editor.setText("");
        currentCol = -1;
        targets.clear();
    }
    
    public Object [] hasErrors() {
        // we will only need to execute this if-block if the editing is coming
        // from the school ID and Name columns
        if (currentCol == 0 || currentCol == 1) {
            final boolean isIDExist = mTable.isIDExist(editor.getText().trim());

            if (currentCol == 0 && isIDExist)
                return new Object[] {true, "School ID already exist."};
            
            final boolean isNameExist = mTable.isNameExist(editor.getText().trim());
            if (currentCol == 1 && isNameExist)
                return new Object[] {true, "Name already exist."};
        }
        // end
        
        return null;
    }

    public JTextPane getEditorRenderer() {
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getText();
    }
    
    public void setBold() {
        _setStyleText(TextStyles.BOLD);
    }
    
    public void setItalic() {
        _setStyleText(TextStyles.ITALIC);
    }
    
    public void setUnderline() {
        _setStyleText(TextStyles.UNDERLINED);
    }
    
    public void setLeftAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_LEFT);
    }
    
    public void setRightAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_RIGHT);
    }
    
    public void setCenterAlign() {
        setHorizontalAlignment(StyleConstants.ALIGN_CENTER);
    }
    
    public void removeAttributes() {
        style.removeAttributes(style);
        paragraphAttributeSet.removeAttributes(paragraphAttributeSet);
        editor.setCharacterAttributes(style, true);
        editor.setParagraphAttributes(paragraphAttributeSet, false);
        editor.setForeground(GradeUtils.Colors.darkBlueColor);
        
        setLeftAlign();
    }
    
    static SimpleAttributeSet style = new SimpleAttributeSet();
    private void _setStyleText(TextStyles textStyle) {
        switch (textStyle) {
            case BOLD -> {
                StyleConstants.setBold(style, true);
            }
            case ITALIC -> {
                StyleConstants.setItalic(style, true);
            }
            case UNDERLINED -> {
                StyleConstants.setUnderline(style, true);
            }
        }
        editor.setCharacterAttributes(style, true);
    }
    
    SimpleAttributeSet paragraphAttributeSet = new SimpleAttributeSet();
    private void setHorizontalAlignment(int constant) {
        StyleConstants.setAlignment(paragraphAttributeSet, constant);
        editor.setParagraphAttributes(paragraphAttributeSet, false);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable jtable, Object value, boolean isSelected, int row, int column) {
        if (row == 0 && column == 0) {
            return schoolIDCellRenderer;
        }
        
        if (!mTable.getFormulas().isEmpty() &&
            mTable.getFormulas().get(row) != null &&
            mTable.getFormulas().get(row).get(column) != null)
        {
            value = mTable.getFormulas().get(row).get(column);
        }
        
        editor.setText((String) value);
        currentCol = column;
        return editor;
    }
    
    public void restartEditor() {
        MutableAttributeSet mas = editor.getInputAttributes();
        mas.removeAttributes(mas);
        
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
    }
}