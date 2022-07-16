package com.tajos.studio.interfaces;

/**
 *
 * @author Rene Tajos Jr.
 */
public interface Margins {
    
    public enum SupportedComponent {
        T_LABEL, T_TEXTFIELD
    }
    
    public void setMargins(int margins);
    
    public int [] getAllMargins();
    
    public void setMarginTop(int margin);
    
    public int getMarginTop();
    
    public void setMarginLeft(int margin);
    
    public int getMarginLeft();
    
    public void setMarginRight(int margin);
    
    public int getMarginRight();
    
    public void setMarginBottom(int margin);
    
    public int getMarginBottom();
}
