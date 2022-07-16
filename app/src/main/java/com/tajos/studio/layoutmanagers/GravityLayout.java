package com.tajos.studio.layoutmanagers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/**
 *
 * @author Rene Tajos Jr.
 */
public class GravityLayout implements LayoutManager {

    private Gravity mGravity = Gravity.CENTER;
    private int mHGap;
    private boolean isStickyHeight;
    private int mVGap;
    private Orientation mOrientation = Orientation.HORIZONTAL;
    private boolean mIsAdjustable = false;
    private int mLeftPadding;
    private int mRightPadding;
    private int mTopPadding;
    private int mBottomPadding;

    public enum Gravity {
        LEFT, RIGHT, CENTER, TOP, BOTTOM
    }

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    @Override
    public void addLayoutComponent(String string, Component cmpnt) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(30, 30);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(0, 0);
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            Component[] components = parent.getComponents();
            
            if (mOrientation == Orientation.HORIZONTAL) {
                // region: identify the total rect of all components in horizontal orientation
                Rectangle totalRect = new Rectangle();
                int biggestHeight = -1, lastStep = 0;
                
                for (Component comp : components) {
                    int width = comp.getPreferredSize().width;
                    biggestHeight = comp.getPreferredSize().height > biggestHeight ? comp.getPreferredSize().height : biggestHeight;
                    totalRect.setSize((int)totalRect.getSize().width+width+(lastStep!=components.length-1?mHGap:0), biggestHeight);
                    lastStep++;
                }
                if (mIsAdjustable)
                    // always adjust the parent width and height to the total rect of all the childrens
                    _adjustParentSize(parent, totalRect);
                    // region end
                
                _setRectLocation(totalRect, parent, insets);
                
                int x = totalRect.x;
                for (Component component : components) {
                    int y = _getYLocation(parent, component, insets);
                    int width = component.getPreferredSize().width;
                    int height = isStickyHeight ? parent.getHeight() - mVGap * 2 : component.getPreferredSize().height;
                    component.setBounds(x, y, width, height);
                    x += width + mHGap;
                }
            } else if (mOrientation == Orientation.VERTICAL) {
                // region: identify the total rect of all components in VERTICAL orientation
                Rectangle totalRect = new Rectangle();
                int biggestWidth = -1, lastStep = 0;
                for (Component comp : components) { 
                    int height = comp.getPreferredSize().height;
                    biggestWidth = comp.getPreferredSize().width > biggestWidth ? comp.getPreferredSize().width : biggestWidth;
                    totalRect.setSize(biggestWidth, (int)totalRect.height+height+(lastStep!=components.length-1?mVGap:0));
                    lastStep++;
                }
                if (mIsAdjustable)
                    // always adjust the parent width and height to the total rect of all the childrens
                    _adjustParentSize(parent, totalRect);
                    // region end
                _setRectLocation(totalRect, parent, insets);
                
                int y = totalRect.y;
                for (Component component : components) {
                    int x = _getXLocation(parent, component, insets);
                    int height = component.getPreferredSize().height;
                    int width = component.getPreferredSize().width;
                    component.setBounds(x, y, width, height);
                    y += height + mVGap;
                }
            }
        }
    }
    
    private void _setRectLocation(Rectangle totalRect, Component parent, Insets insets) {
        switch (mGravity) {
            case CENTER -> {
                int rectX = ((parent.getWidth() / 2) - (totalRect.getSize().width / 2));
                int rectY = ((parent.getHeight() / 2) - (totalRect.getSize().height / 2));
                totalRect.setLocation(rectX, rectY);    
            }
            case LEFT -> {
                int rectY = ((parent.getHeight() / 2) - (totalRect.getSize().height / 2));
                totalRect.setLocation(insets.left + mLeftPadding, rectY);
            }
            default -> throw new AssertionError();
        }
    }
    
    private int _getXLocation(Component parent, Component children, Insets insets) {
        int x = 0;
        switch (mGravity) {
            case CENTER -> {
                x = (parent.getWidth() / 2) - (children.getPreferredSize().width / 2);
            }
            case LEFT -> {
                x = insets.left + mLeftPadding;
            }
        }
        
        return x;
    }
    
    private int _getYLocation(Component parent, Component children, Insets insets) {
        int y;
        switch (mGravity) {
            case CENTER -> {
                y = isStickyHeight ? insets.top + mTopPadding : ((parent.getHeight() / 2) - (children.getPreferredSize().height / 2));
            }
            case LEFT -> {
                y = insets.top + mTopPadding;
            }
            default -> throw new AssertionError();
        }
        
        return y;
    }
    
    private void _adjustParentSize(Component parent, Rectangle totalRect) {
        parent.setPreferredSize(new Dimension(totalRect.width + mLeftPadding + mRightPadding, totalRect.height + mTopPadding + mBottomPadding));
        parent.setSize(totalRect.width + mLeftPadding + mRightPadding, totalRect.height + mTopPadding + mBottomPadding);
    }
    
    public void setGravity(Gravity gravity) {
        mGravity = gravity;
    }
    
    public void setPadding(int pad) {
        mLeftPadding = pad;
        mRightPadding = pad;
        mTopPadding = pad;
        mBottomPadding = pad;
    }
    
    public void setLeftPadding(int pad) {
        mLeftPadding = pad;
    }
    
    public void setRightPadding(int pad) {
        mRightPadding = pad;
    }
    
    public void setTopPadding(int pad) {
        mTopPadding = pad;
    }
    
    public void setBottomPadding(int pad) {
        mBottomPadding = pad;
    }
    
    public void setStickyHeight(boolean isSticky) {
        isStickyHeight = isSticky;
    }
    
    public void setOrientation(Orientation or) {
        mOrientation = or;
    }
    
    public void setHorizontalGap(int hgap) {
        mHGap = hgap;
    }
    
    public void setVerticalGap(int vgap) {
        mVGap = vgap;
    }
    
    public void setAdjustableSize(boolean bool) {
        mIsAdjustable = bool;
    }
}
