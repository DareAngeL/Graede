package com.tajos.studio.components;

import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rene Tajos Jr.
 */
public class MaximizedButton extends JButton {

    private JFrame frame;

    private Image mIcon;

    public MaximizedButton() {
        setPreferredSize(new Dimension(17, 17));
        setBorder(null);
        setBackground(new Color(0,0,0,0));
        setOpaque(false);
        setFocusPainted(false);
        setText("");
        addMouseListener(new ButtonMouseListener());
        
        try {
            BufferedImage buffImg = ImageIO.read(CloseButton.class.getResource("/icons/maximized.png"));
            
            mIcon = buffImg.getScaledInstance(16, 16, BufferedImage.SCALE_SMOOTH);
            setIcon(new ImageIcon(mIcon));
        } catch (IOException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public void attachFrame(JFrame frame) {
        this.frame = frame;
    }
    
    /**
     * @description listens for mouse inputs
     */
    private class ButtonMouseListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (frame.getExtendedState() != JFrame.NORMAL) {
                frame.setExtendedState(JFrame.NORMAL);
                return;
            }
            
            GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
            frame.setMaximizedBounds(env.getMaximumWindowBounds());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
}
