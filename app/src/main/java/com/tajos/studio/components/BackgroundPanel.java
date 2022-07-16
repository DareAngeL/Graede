package com.tajos.studio.components;

import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 *
 * @author Rene Tajos jr.
 */
public class BackgroundPanel extends RoundedPanel {
    
    private BufferedImage mBufImg;
    private Image img;
    private Color mDefaultBgColor = Color.WHITE;
    
    private int imgX = -1, imgY = -1, imgWidth, imgHeight;
    private Position mPos;
    
    public BackgroundPanel() {}
    
    /**
     * @Enum the position of where the background image should be placed;
     */
    public enum Position {
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        TOP_LEFT,
        TOP_RIGHT,
        TOP,
        LEFT,
        RIGHT,
        BOTTOM
    }

    /**
     * @Description sets the background color of this component
     * @param bg the background color for this panel
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        mDefaultBgColor = bg;
    }
    
    /**
     * @Description sets the background image for this component;
     * @param icon the image/icon to be used for this component
     */
    public void setBackgroundDesign(Icon icon) {
        mBufImg = GradeUtils.convertIconToBufferedImage(icon);
        
        if (img != null) {
            setBackgroundImageSize(new Dimension(img.getWidth(this), img.getHeight(this)));
        }
    }
    
    /**
     * @Description sets the background image size for this component;
     * @param dimension the dimension or the width and height for the image;
     */
    public void setBackgroundImageSize(Dimension dimension) {
        if (mBufImg == null)
            return;
        
        imgWidth = dimension.width; imgHeight = dimension.height;
        img = mBufImg.getScaledInstance(imgWidth, imgHeight, BufferedImage.SCALE_SMOOTH);
    }
    
    /**
     * @Description sets the background image {@Coordinate position} on this component
     * @param pos the {@Enum type} position for the image;
     */
    public void setBackgroundImagePosition(Position pos) {
        mPos = pos;
        if (img == null)
            return;
        
        _initImagePositions(pos);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            if (mPos != null)
                _initImagePositions(mPos);
            
            g2d.setColor(mDefaultBgColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.drawImage(img, imgX!=-1?imgX:0, imgY!=-1?imgY:0, this);
            
        }
    }

    /**
     * @Description this will initialize all the positions for the background image
     * @param pos the {@Enum type} position for the image;
     */
    private void _initImagePositions(Position pos) {
        switch (pos) {
            case BOTTOM_RIGHT -> {
                imgX = getWidth() - imgWidth;
                imgY = getHeight() - imgHeight;
            }
            case TOP_RIGHT -> {
                imgX = getWidth() - imgWidth;
                imgY = 0;
            }
            default -> {
                imgX = 0; imgY = 0;
            }
        }
    }
}