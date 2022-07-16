package com.tajos.studio.components;

import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 *
 * @author Rene Tajos Jr.
 */
public class CloseButton extends JButton {
    
    private boolean isFocused = false;
    
    private OnCloseButtonClickedListener listener;
    public interface OnCloseButtonClickedListener {
        void onCloseBtnClick();
    }

    public CloseButton() {
        setPreferredSize(new Dimension(17, 17));
        setBorder(null);
        setBackground(new Color(0,0,0,0));
        setOpaque(false);
        setFocusPainted(false);
        setText("");
        addMouseListener(new ButtonMouseListener());
    }
    
    /**
     * @Description determines if this button is focused or not
     * @param b {@code true} if it is focused, else {@code false} if its not focused
     */
    public void setIsFocused(boolean b) {
        isFocused = b;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        _applyQualityRenderingHints(g2d);
        
        g2d.setColor(GradeUtils.Colors.darkBlueColor);
        g2d.fillOval(0 , 0, getWidth(), getHeight());
        // draw the "X" icon on this button
        if (isFocused) {
            // draw first line
            g2d.setColor(Color.WHITE);
            int startX = (getWidth() / 2) / 2 + 2, startY = (getHeight() / 2) / 2 + 2;
            int endX = startX + (startX / 2 + 2), endY = startY + (startY / 2 + 2) ;
            g2d.drawLine(startX, startY, endX, endY);
            // draw second line
            int startX2 = startX * 2 - 1, startY2 = startY;
            int endX2 = startX, endY2 = startY * 2 - 1;
            g2d.drawLine(startX2, startY2, endX2, endY2);
        }
        g2d.dispose();
    }
    
    public void setOnCloseButtonClickedListener(OnCloseButtonClickedListener listener) {
        this.listener = listener;
    }
    
    /**
     * @description listens for mouse input
     */
    private class ButtonMouseListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setIsFocused(true);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            setIsFocused(false);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (listener != null) {
                listener.onCloseBtnClick();
                return;
            }
            
            System.exit(0);
        }
    }
    
    private void _applyQualityRenderingHints(Graphics2D g2d) {

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    }
}
