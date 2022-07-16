package com.tajos.studio.components;

import java.awt.Color;
import javax.swing.JTextPane;

/**
 *
 * @author maste
 */
public class TTextPane extends JTextPane {

    private Color mForeGround;

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        mForeGround = fg;
    }

    @Override
    public Color getForeground() {
        return mForeGround;
    }
}
