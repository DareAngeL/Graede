package com.tajos.studio.components;

import com.tajos.studio.util.GradeUtils;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

/**
 *
 * @author Rene Tajos Jr.
 */
public class ImageViewer extends JComponent {
    
    private BufferedImage mImage;
    private Icon mUnresizedImg = new ImageIcon(ImageViewer.class.getResource("/icons/user.png"));
    private ImageShape mImgShape;
    private ImageType mImgType = ImageType.FILL;
    
    private ImageLoadListener mImgLoadListener;
    private Color mColorMod;
    
    public interface ImageLoadListener {
        void onURLLoadComplete();
    }
    
    public enum ImageShape {
        Circle, Rectangle
    }
    
    public enum ImageType {
        FILL, COVER
    }
    
    public ImageViewer() {
        setPreferredSize(new Dimension(100,100));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        
        if (mUnresizedImg != null)
            drawImage(mUnresizedImg);
        
        if (mImage != null)
            g2D.drawImage(mImage, null, 0, 0);
    }
    
    private void drawImage(Icon icon) {
        int width = getWidth() > 0 ? getWidth() : getPreferredSize().width;
        int height = getHeight() > 0 ? getHeight() : getPreferredSize().height;
        
        int iwidth = width, iheight = height;
        // region: scale the image either if it's COVER type or FILL type
        if (mImgType == ImageType.COVER) {
            int[] size = coverImageType(icon);
            iwidth = size[0];
            iheight = size[1];
        }
        
        BufferedImage bimg = GradeUtils.convertIconToBufferedImage(icon);
        Image scaledImg = bimg.getScaledInstance(iwidth, iheight, BufferedImage.SCALE_SMOOTH);
        // region end
        BufferedImage maskShape = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = maskShape.createGraphics();
        _applyQualityRenderingHints(g2D);
        // region: identify the image shape
        if (mImgShape != null && mImgShape == ImageShape.Circle) {
            int diameter = Math.min(width, height);
            int x = (width / 2) - (diameter / 2);
            int y = (height / 2) - (diameter / 2);
            
            g2D.fillOval(x, y, diameter-1, diameter-1);
        } else if (mImgShape != null && mImgShape == ImageShape.Rectangle)
            g2D.fillRect(0, 0, width, height);
        // region end
        g2D.dispose();
        
        mImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2D = mImage.createGraphics();
        _applyQualityRenderingHints(g2D);
        
        g2D.setComposite(AlphaComposite.Src);
        int movX = (iwidth / 2 - width / 2);
        int movY = (iheight / 2 - height / 2);
        int x = movX == 0 ? 0 : -movX;
        int y = movY == 0 ? 0 : -movY;
        g2D.drawImage(scaledImg, x, y, null);
        // region: draw the image shape if it is set
        if (mImgShape != null) {
            g2D.setComposite(AlphaComposite.DstIn);
            g2D.drawImage(maskShape, 0, 0, null);
        }
        // region end
        // region: set its color modification if there's any
        if (mColorMod != null)
            mImage = GradeUtils.colorImage(mImage, mColorMod);
        // region end
        g2D.dispose();
    }
    
    public void setImageType(ImageType type) {
        mImgType = type;
    }
    
    public void setImageResource(Icon icon) {
        mUnresizedImg = icon;
        revalidate();
        repaint();
    }
    
    public void setImageURL(String _url) {
        // run in the background thread to avoid freezing 
        new SwingWorker<>() {
            @Override
            protected Image doInBackground() throws Exception {
                URL url = new URL(_url);
                Image img = ImageIO.read(url);
                return img;
            }

            @Override
            protected void done() {
                try {
                    mUnresizedImg = new ImageIcon((Image)get());
                    if (mImgLoadListener != null)
                        mImgLoadListener.onURLLoadComplete();
                } catch (InterruptedException | ExecutionException ex) {
                    GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                }
            }
        }.execute();
    }
    
    public void setImageShape(ImageShape shape) {
        if (shape == ImageShape.Circle) {
            mImgShape = shape;
            return;
        }
        
        mImgShape = null;
    }
    
    public void setImageLoadListener(ImageLoadListener lstner) {
        mImgLoadListener = lstner;
    }
    
    public void setColorImage(Color color) {
        mColorMod = color;
        
        revalidate();
        repaint();
    }
    
    public Color getColorImage() {
        return mColorMod;
    }
    
    private int [] coverImageType(Icon icon) {
        int cwidth = getWidth() > 0 ? getWidth() : getPreferredSize().width;
        int cheight = getHeight() > 0 ? getHeight() : getPreferredSize().height;
        
        int iwidth = icon.getIconWidth();
        int iheight = icon.getIconHeight();
        float rate;
        
        if (iwidth < iheight) {
            rate = cwidth / (float) iwidth;
            iheight = (int)(iheight * rate);
            iwidth = cwidth;
        } else {
            rate = cheight / (float) iheight;
            iwidth = (int)(iwidth * rate);
            iheight = cheight;
        }
        
        return new int[] {iwidth, iheight};
    }
    
    private void _applyQualityRenderingHints(Graphics2D g2d) {

//        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    }
}