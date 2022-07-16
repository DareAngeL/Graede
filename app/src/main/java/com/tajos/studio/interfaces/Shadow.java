package com.tajos.studio.interfaces;

import java.awt.Color;

/**
 *
 * @author Rene Tajos Jr.
 */
public abstract interface Shadow {
    
    public float shadowOpacity = 0.5f;
    public ShadowType defaultShadowType = ShadowType.CENTER;
    public int defaultShadowSize = 6;
    public Color defaultShadowColor = Color.BLACK;
    
    public enum ShadowType {
        CENTER, TOP_RIGHT, TOP_LEFT, BOT_RIGHT, BOT_LEFT, BOT, TOP, LEFT, RIGHT
    }
    
    public abstract ShadowType getShadowType();
    
    public abstract void setShadowType(ShadowType shadowType);
    
    public abstract int getShadowSize();
    
    public abstract void setShadowSize(int shadowSize);
    
    public abstract float getShadowOpacity();
    
    public abstract Color getShadowColor();
    
    public abstract void setShadowColor(Color shadowColor);
    
    public abstract void setCoordinates(ShadowType shadowType, int shadowSize, int size);
}
