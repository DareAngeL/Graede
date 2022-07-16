package com.tajos.studio.components;

import com.tajos.studio.util.GradeUtils;
import javax.swing.JTextArea;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TTextArea extends JTextArea {
    
    public void setDefaultFont(int size) {
        setFont(GradeUtils.getDefaultFont(size));
    }
}