package com.tajos.studio.graphics;

import com.tajos.studio.interfaces.Shadow;
import com.tajos.studio.interfaces.Margins;
import com.tajos.studio.util.GradeUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Rene Tajos Jr.
 */
public class RoundedPanel extends JPanel implements Shadow, Margins {

    private boolean hasShadow = false;
    private int mUpperLeftRadius;
    private int mUpperRightRadius;
    private int mBottomLeftRadius;
    private int mBottomRightRadius;
    
    private ShadowType mShadowType;
    private RoundedShapePath.Create  mShapePath;
    private int mShadowSize = -1;
    private Color mShadowColor;
    private Color mOldBackgroundColor;

    private boolean isShadowEnabled = false;
    private int x, y;
    private boolean isBorderEnabled = false;
    private Color mBorderColor = Color.BLACK; // default border color
    private float mBorderSize = 1f; // 1 is the default border size.
    private RoundedShapePath.Corners mCorners;
    private int position;
    private Color mBgColor;
    
    private int mTopMargin = 0;
    private int mLeftMargin = 0;
    private int mRightMargin = 0;
    private int mBtmMargin = 0;
    
    /**
     * @constructor
     */
    public RoundedPanel() {}
    
    /**
     * @param radius the corner radius for this component
     * @constructor
     */
    public RoundedPanel(int radius) {
        _init(radius, radius, radius, radius);
        
        isShadowEnabled = false;
    }

    /**
     * @param radius the corner radius for this component
     * @param shadowEnabled {@code true} {@code false}, determines if the shadow should be enabled or not;
     * @constructor
     */
    public RoundedPanel(int radius, boolean shadowEnabled) {
        _init(radius, radius, radius, radius);
        isShadowEnabled = shadowEnabled;
    }
    
    /**
     * @param topLeftRadius the top left corner radius for this component
     * @param topRightRadius the top right corner radius for this component
     * @param bottomLeftRadius the bottom left corner radius for this component
     * @param bottomRightRadius the bottom right corner radius for this component
     * @constructor
     */
    public RoundedPanel(int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius) {
        _init(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius);
        isShadowEnabled = false;
    }
    
    /**
     * @param topLeftRadius the top left corner radius for this component
     * @param topRightRadius the top right corner radius for this component
     * @param bottomLeftRadius the bottom left corner radius for this component
     * @param bottomRightRadius the bottom right corner radius for this component
     * @param shadowEnabled {@code true} {@code false}, determines if the shadow should be enabled or not;
     * @constructor
     */
    public RoundedPanel(int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius, boolean shadowEnabled) {
        _init(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius);
        isShadowEnabled = shadowEnabled;
    }

    /**
     * @description initialize this component
     */
    private void _init(int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius) {
        mUpperLeftRadius = topLeftRadius;
        mUpperRightRadius = topRightRadius;
        mBottomLeftRadius = bottomLeftRadius;
        mBottomRightRadius = bottomRightRadius;
    }
    
    /**
     * @description paints the component
     * @param g the graphics used for this component
     */
     @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set corners
        mCorners = new RoundedShapePath.Corners(
            mUpperLeftRadius, mUpperRightRadius, mBottomLeftRadius, mBottomRightRadius);
        
        // region: draw the shape
        int shadowSize;
        ShadowType shadowType;
        Color shadowColor;
        
        if (!isShadowEnabled) {
            shadowSize = 0;
            shadowType = null;
            shadowColor = null;
        } else {
            shadowSize = mShadowSize == -1 ? defaultShadowSize : getShadowSize();
            shadowType = mShadowType == null ? defaultShadowType : getShadowType();
            shadowColor = mShadowColor == null ? defaultShadowColor : getShadowColor();
        }
        
        // region: drawing the button and the shadow
        int size = shadowSize * 2;
        x = 0;
        y = 0;
        int width = getWidth() - size;
        int height = getHeight() - size;
        
        if (width == 0)
            return;
        
        mShapePath = new RoundedShapePath.Create(0, 0, width, height, mCorners);
        
        // set the x and y coordinates of the this panel
        setCoordinates(shadowType, shadowSize, size);
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgG2D = img.createGraphics();
        imgG2D.setColor(mBgColor);
        imgG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        imgG2D.fill(mShapePath);
        // Draw Borders
        if (isBorderEnabled) {
            imgG2D.setColor(mBorderColor);
            BasicStroke stroke = new BasicStroke(mBorderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            imgG2D.setStroke(stroke);
            imgG2D.draw(new RoundedShapePath.Create(0, 0, width-mBorderSize, height-mBorderSize, mCorners));
        }
        imgG2D.dispose();
        
        //  Create Shadow renderer
        ShadowRenderer render = new ShadowRenderer(shadowSize, shadowOpacity, shadowColor);
        // draw the shadow
        if (isShadowEnabled && shadowSize != 0 && !hasShadow) {
            g2D.drawImage(render.createShadow(img), 0, 0, null);
            hasShadow = true;
        }
        // draw the component
        g2D.drawImage(img, x, y, null);
        // region end
    }

    public boolean isHasShadow() {
        return hasShadow;
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        mBgColor = bg;
    }
    
    /**
     * @description sets the {@index position} of this component
     * @Warning This will be only useful if the {@LayoutManager manager} is listview layout
     * @param pos the {@index position} for this component
     */
    public void setPosition(int pos) {
        position = pos;
    }
    
    /**
     * 
     * @return the {@index position} for this component
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * 
     * @return the {@Shape} of this component
     */
    public RoundedShapePath.Create getShapePath() {
        return mShapePath;
    }
    
    /**
     * 
     * @return all the radius of the corners
     */
    public RoundedShapePath.Corners getCorners() {
        return mCorners;
    }
    
    /**
     * @description sets the transparency of this component
     * @param isTransparent {@code true} if transparent, {@code false} if not transparent
     */
    public void setTransparent(boolean isTransparent) {
        if (isTransparent) {
            mOldBackgroundColor = getBackground();
            setBackground(GradeUtils.Colors.transparent);
        } else {
            if (mOldBackgroundColor == null) {
                setBackground(new Color(0,0,0,1));
                return;
            }
            
            setBackground(mOldBackgroundColor);
        }
    }
    
    /**
     * @description sets the border size of this component
     * @param size the possible border size of this component
     */
    public void setBorderSize(float size) {
        mBorderSize = size;
    }
    
    /**
     * @description determines if the border of this component will be enabled or not
     * @param bool {@code true} if the border will be set to be enabled, else
     * {@code false} if this will be set to be not enabled
     */
    public void setBorderEnabled(boolean bool) {
        isBorderEnabled = bool;
    }
    
    /**
     * @description sets the color of the border for this component
     * @param color the possible color of the border
     */
    public void setBorderColor(Color color) {
        mBorderColor = color;
        
       revalidate();
       repaint();
    }
    
    /**
     * @description sets the radius for all the corners of this component
     * @param radius the possible {@int radius} of this component
     */
    public void setRadius(int radius) {
        mUpperLeftRadius = radius;
        mUpperRightRadius = radius;
        mBottomLeftRadius = radius;
        mBottomRightRadius = radius;
    }
    
    /**
     * @description sets the radius for each corners of this component
     * @param radius the {@integerArray radius} for this component
     */
    public void setCornersRadius(int[] radius) {
        mUpperLeftRadius = radius[0];
        mUpperRightRadius = radius[1];
        mBottomLeftRadius = radius[2];
        mBottomRightRadius = radius[3];
    }
    
    public void setShadowEnabled(boolean enabled) {
        isShadowEnabled = enabled;
    }

    /**
     * 
     * @return what shadow type this component have;
     */
    @Override
    public ShadowType getShadowType() {
        return isShadowEnabled ? mShadowType : defaultShadowType;
    }

    /**
     * @description sets the shadow type of this component
     * @param shadowType the shadow type
     */
    @Override
    public void setShadowType(ShadowType shadowType) {
        mShadowType = shadowType;
    }

    /**
     * 
     * @return the shadow size;
     */
    @Override
    public int getShadowSize() {
        return isShadowEnabled ? mShadowSize : defaultShadowSize;
    }

    /**
     * @description sets the shadow size;
     * @param shadowSize the possible shadow {@int size};
     */
    @Override
    public void setShadowSize(int shadowSize) {
        mShadowSize = shadowSize;
    }

    /**
     * 
     * @return the opacity of the shadow, of this component
     */
    @Override
    public float getShadowOpacity() {
        return shadowOpacity;
    }

    /**
     * 
     * @return the shadow color;
     */
    @Override
    public Color getShadowColor() {
        return isShadowEnabled ? mShadowColor : defaultShadowColor;
    }

    /**
     * @description sets the shadow color for this component
     * @param shadowColor the possible shadow color
     */
    @Override
    public void setShadowColor(Color shadowColor) {
        mShadowColor = shadowColor;
    }

    /**
     * @description sets the {@coordinates x and y} of this component relative to its {@ShadowType shadowtype}
     * @param shadowType the shadow type of this component
     * @param shadowSize the shadow size of this component
     * @param size the doubled shadow size of this component {@code shadowSize x 2}
     */
    @Override
    public void setCoordinates(ShadowType shadowType, int shadowSize, int size) {
        if (null == shadowType) {
            //  Center
            x = shadowSize;
            y = shadowSize;
        } else switch (shadowType) {
            case TOP -> {
                x = shadowSize;
                y = size;
            }
            case BOT -> {
                x = shadowSize;
                y = 0;
            }
            case TOP_LEFT -> {
                x = size;
                y = size;
            }
            case TOP_RIGHT -> {
                x = 0;
                y = size;
            }
            case BOT_LEFT -> {
                x = size;
                y = 0;
            }
            case BOT_RIGHT -> {
                x = 0;
                y = 0;
            }
            case LEFT -> {
                x = size;
                y = shadowSize;
            }
            case RIGHT -> {
                x = 0;
                y = shadowSize;
            }
            default -> {
                    x = shadowSize;
                    y = shadowSize;
            }
        }
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
}