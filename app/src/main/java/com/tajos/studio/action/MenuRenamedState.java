package com.tajos.studio.action;

import com.tajos.studio.interfaces.State;
import com.tajos.studio.components.TajosMenuTextField;

/**
 *
 * @author Rene Tajos Jr.
 */
public class MenuRenamedState implements State {
    
    private final String mTxString;
    private String mRedoTxString;
    private final TajosMenuTextField mTxField;
    
    public MenuRenamedState(TajosMenuTextField tx) {
        mTxField = tx;
        mTxString = tx.getText();
    }

    @Override
    public void undo() {
        mRedoTxString = mTxField.getText();
        mTxField.setText(mTxString);
        
        mTxField.repaint();
        mTxField.revalidate();
    }

    @Override
    public void redo() {
        mTxField.setText(mRedoTxString);
        
        mTxField.repaint();
        mTxField.revalidate();
    }
    
}
