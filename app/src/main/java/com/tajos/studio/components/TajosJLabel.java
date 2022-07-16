package com.tajos.studio.components;

import com.tajos.studio.interfaces.Margins;
import com.tajos.studio.layoutmanagers.GravityLayout;
import com.tajos.studio.util.GradeUtils;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JLabel;

/**
 *
 * @author Rene Tajos Jr.
 */
public class TajosJLabel extends JLabel implements Margins {

    private int mTopMargin = 0;
    private int mLeftMargin = 0;
    private int mRightMargin = 0;
    private int mBtmMargin = 0;
    
    public SupportedComponent supportedComponentName = SupportedComponent.T_LABEL;
    
    public TajosJLabel() {
        super();
    }
    
    /**
     * @Description set the {@Deafult font} for this component
     * @param size the {@Font size} for this component
     */
    public void setDefaultFont(int size) {
        setFont(GradeUtils.getDefaultFont(size));
    }
    
    /**
     * @Description set the {@Bold font} for this component
     * @param size the {@Font size} for this component
     */
    public void setBoldDefaultFont(int size) {
        setFont(GradeUtils.getBoldDefaultFont(size));
    }
    
    /**
    * {@code Warning} This will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @param margins The margins for the {@code TOP} {@code LEFT} {@code RIGHT} {@code BOTTOM} sides
    */
    @Override
    public void setMargins(int margins) {
        mTopMargin = margins;
        mLeftMargin = margins;
        mRightMargin = margins;
        mBtmMargin = margins;
    }
    /**
    * {@code Warning} This will only work when the parent of this component have {@LayoutManager ListViewLayout}.
     * @return Return all the Margins of this component
    */
    @Override
    public int [] getAllMargins() {
        return new int[] {mTopMargin, mLeftMargin, mRightMargin, mBtmMargin};
    }
    
    @Override
    public void setMarginTop(int margin) {
        mTopMargin = margin;
    }
    
    @Override
    public void setMarginLeft(int margin) {
        mLeftMargin = margin;
    }
    
    @Override
    public void setMarginRight(int margin) {
        mRightMargin = margin;
    }
    
    @Override
    public void setMarginBottom(int margin) {
        mBtmMargin = margin;
    }
    
     @Override
    public int getMarginTop() {
        return mTopMargin;
    }

    @Override
    public int getMarginLeft() {
        return mLeftMargin;
    }

    @Override
    public int getMarginRight() {
        return mRightMargin;
    }

    @Override
    public int getMarginBottom() {
        return mBtmMargin;
    }
    
    /**
    * @Description this will resize the {@TajosJLabel label} according to its text's width and height
    * @Warning this will only work when the parent of this component don't have {@LayoutManager} attached to it.
    */
    public void resize() {
        if (getFont() == null) // if theres no available font, just return
            return;
        
        FontMetrics fntMetrics = getFontMetrics(getFont());
        int newWidth = (int) fntMetrics.getStringBounds(getText(), getGraphics()).getWidth() + 20; // adds 20 for extra room
        int newHeight = (int) fntMetrics.getStringBounds(getText(), getGraphics()).getHeight();
        this.setBounds(this.getX(), this.getY(), newWidth, newHeight);
    }
    
    public void resizeWithPreferredSize() {
        if (getFont() == null)
            return;
        
        FontMetrics fntMetrics = getFontMetrics(getFont());
        int newWidth = (int) fntMetrics.getStringBounds(getText(), getGraphics()).getWidth() + 20; // adds 20 for extra room
        int newHeight = (int) fntMetrics.getStringBounds(getText(), getGraphics()).getHeight();
        this.setPreferredSize(new Dimension(newWidth, newHeight));
        
        GravityLayout gravityLayout = new GravityLayout();
        gravityLayout.setAdjustableSize(true);
        getParent().setLayout(gravityLayout);
    }
}