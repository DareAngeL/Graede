package com.tajos.studio.util;

import com.tajos.studio.data.Sheet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rene Tajos Jr.
 */
public class GradeUtils {
    
    public static final int defaultFontSize = 12;
    public static final String resizeHorizontalCursorStr = "resize-h.png";
    public static final String resizeVerticalCursorStr = "resize-v.png";
    public static final String downArrowIconName = "down-arrow.png";
    public static final String rightArrowIconName = "right-arrow.png";
    public static final String selectAllIconName = "select-all.png";
    private static Random generator = null;
    
    private static final Map<String, Cursor> customCursors = new HashMap<>();
    
    public static void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(new JFrame(), message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static Color getRandomColour() {
        //  Same ideas as getRandomRBG, just returns a Color.
        if (generator == null) {
            generator = new Random();
        }

        float h = generator.nextFloat();
        if (h > 0.1f && h < 0.25f) {
            h -= 0.15f; // More reds, no yellows.
        }

        return Color.getHSBColor(h, 1.0F, 0.9F);
    }
    
    public static boolean isScaleChanged(final PropertyChangeEvent ev) {
        return isScaleChanged(ev.getPropertyName(), ev.getOldValue(),
                              ev.getNewValue());
    }
    
    public static boolean isScaleChanged(final String name,
                                         final Object oldValue,
                                         final Object newValue) {
        if (oldValue == newValue || !"graphicsConfiguration".equals(name)) {
            return false;
        }
        var newGC = (GraphicsConfiguration) oldValue;
        var oldGC = (GraphicsConfiguration) newValue;
        var newTx = newGC != null ? newGC.getDefaultTransform() : null;
        var oldTx = oldGC != null ? oldGC.getDefaultTransform() : null;
        return !Objects.equals(newTx, oldTx);
    }
    
    public static LinkedHashMap<String, Sheet> reverseMap(Map<String, Sheet> arr) {
        NavigableMap<String, Sheet> map = new TreeMap<>(arr);
        LinkedHashMap<String,Sheet> reverseMap = new LinkedHashMap<>();
        
        NavigableSet<String> keySet = map.navigableKeySet();
        Iterator<String> iterator = keySet.descendingIterator();
        
        String key;
        while(iterator.hasNext()) {
            key = iterator.next();
            reverseMap.put(key,map.get(key));
        }
        log(reverseMap);
        return reverseMap;
    }
    
    public static List<String> reverseMapKeysString(List<String> arr) {
        Collections.reverse(arr);
        return arr;
    }
    
    public static Color blend(Color c0, Color c1) {
        double totalAlpha = c0.getAlpha() + c1.getAlpha();
        double weight0 = c0.getAlpha() / totalAlpha;
        double weight1 = c1.getAlpha() / totalAlpha;

        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = Math.max(c0.getAlpha(), c1.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }
    
    public static Color getContrastColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return (yiq >= 128) ? Colors.darkBlueColor : Color.WHITE;
    }
    
    public static String removeStringSpaces(String str) {
        return str.replaceAll(" ", "");
    }
    
    public static String charsToString(char [] chars) {
        return String.copyValueOf(chars);
    }
    
    public static void centerFrame(Window frame) {
        // region: position the frame in the center of the screen
        final int scrnWidth = GradeUtils.getUserScreenSize().width;
        final int scrnHeight = GradeUtils.getUserScreenSize().height;
        final int x = (scrnWidth - frame.getPreferredSize().width) / 2;
        final int y = (scrnHeight - frame.getPreferredSize().height) / 2;
        frame.setBounds(x, y, frame.getPreferredSize().width, frame.getPreferredSize().height);
        // end region
    }
    
    public static void writeFile(String text, URI location, boolean append) throws IOException {
        File file = new File(location);
        try (FileWriter writer = new FileWriter(file, append)) {
            writer.write(text);
        }
    }
    
    public static BasicStroke getBasicSQStroke(int strokeThickness) {
        return new BasicStroke(strokeThickness, BasicStroke.CAP_SQUARE, BasicStroke.CAP_SQUARE);
    }
     
    public static int showWrkbkSheetDeletionDialog(String wkrbkSheetTitle) {
        return JOptionPane.showConfirmDialog(new Frame(),   
                        "Are you sure you want to delete this " + wkrbkSheetTitle + "\nYou cannot undo this action after deleting it.", 
                        "Delete", JOptionPane.WARNING_MESSAGE);
    }
    
    public static void setCustomCursor(Component comp, String imageName) {
        if (customCursors.containsKey(imageName)) {
            comp.setCursor(customCursors.get(imageName));
            return;
        }
        
        int y;
        if (imageName.equals(resizeHorizontalCursorStr))
            y = 5;
        else
            y = 10;
        
        try
        {
            Cursor c = Toolkit
                .getDefaultToolkit()
                .createCustomCursor(
                       new ImageIcon(GradeUtils.class.getResource("/icons/" + imageName)).getImage(),
                       new Point(10, y),
                       "My cursor"
                );
            comp.setCursor(c);
            customCursors.put(imageName, c);
            
        } catch(HeadlessException | IndexOutOfBoundsException ex){
            showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public static void setPrecisionCursor(Component comp) {
        if (customCursors.containsKey("precision-cursor.png")) {
            comp.setCursor(customCursors.get("precision-cursor.png"));
            return;
        }
        
        try
        {
            Cursor c = Toolkit
                .getDefaultToolkit()
                .createCustomCursor(
                       new ImageIcon(GradeUtils.class.getResource("/icons/precision-cursor.png")).getImage(),
                       new Point(15, 15),
                       "Precision"
                );
            
            comp.setCursor(c);
            customCursors.put("precision-cursor.png", c);
            
        } catch(HeadlessException | IndexOutOfBoundsException ex){
            showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public static Cursor getCustomCursor(String imgName) {
        if (customCursors.containsKey(imgName)) {
            return customCursors.get(imgName);
        }
        
        return null;
    }
    
    public static BufferedImage getImageResource(String filePathName) {
        try {
            BufferedImage bimg = ImageIO.read(GradeUtils.class.getResource(filePathName));
            return bimg;
        } catch (IOException ex) {
            showErrorDialog(ex.getMessage(), "Something went wrong");
        }
        
        return null;
    }
    
    public static BufferedImage convertIconToBufferedImage(Icon icon) {
        BufferedImage mBufImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mBufImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        
        return mBufImg;
    }
    
    public static BufferedImage colorImage(BufferedImage img, Color c) {
        int width = img.getWidth();
        int height = img.getHeight();
        WritableRaster raster = img.getRaster();

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int[] pixels = raster.getPixel(xx, yy, (int[]) null);
                pixels[0] = c.getRed();
                pixels[1] = c.getGreen();
                pixels[2] = c.getBlue();
                raster.setPixel(xx, yy, pixels);
            }
        }
        return img;
    }
    
    public static Rectangle2D getTextRect(FontMetrics metrics, Graphics g, String text) {
        return metrics.getStringBounds(text, g);
    }
    
    public static Font getDefaultFont(int fontSize) {
        return getFont("Montserrat-Regular.ttf", Font.TRUETYPE_FONT, fontSize);
    }
    
    public static Font getBoldDefaultFont(int fontSize) {
        return getFont("Montserrat-Bold.ttf", Font.TRUETYPE_FONT, fontSize);
    }
    
    private static Font getFont(String fontName, int fontType, float fontSize) {
        Font font = null;
        
        try (InputStream in = GradeUtils.class.getClassLoader().getResourceAsStream("fonts/" + fontName)) {
            final Font f = Font.createFont(fontType, in);
            font = f.deriveFont(fontSize);
        } catch (FontFormatException | IOException ex) {
            showErrorDialog(ex.getMessage(), "Something went wrong");
        }
        
        return font;
    }
    
    public static String getSharedLibrary(String name, String ext) {
        File temp;
        try (InputStream in = GradeUtils.class.getClassLoader().getResourceAsStream("bin/"+name)) {
            byte[] buffer = new byte[1024];
            int read = -1;
            temp = new File(new File(System.getProperty("java.io.tmpdir")), name);
            FileOutputStream fos = new FileOutputStream(temp);
            while((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }   
            
            fos.close();
            in.close();
            return temp.getAbsolutePath();
        } catch (IOException ex) {
            showErrorDialog(ex.getMessage(), "Something went wrong");
        }
        
        return null;
    }
    
    public static void log(Object obj) {
        System.out.println("Log: " + obj);
    }
    
    public static void err(Object err) {
        System.err.println(err);
    }
    
    public static Dimension getUserScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    public static class Colors {
        public static final Color creamyWhiteBlueColor = Color.decode("#F7F5FB");
        public static final Color semiCreamWhiteBlueColor = Color.decode("#9A9EC4");
        public static final Color darkBlueColor = Color.decode("#161C48");
        public static final Color highlightColor = Color.decode("#D8DCF4");
        public static final Color pinkColor = Color.decode("#FFCCFF");
        public static final Color transparent = new Color(0,0,0,0);
        public static final Color gridColor = new Color(227,219,242);
        public static final Color buttonColor = new Color(76,96,204);
        public static final Color hoverBtnColor = new Color(104,122,218);
    }
}