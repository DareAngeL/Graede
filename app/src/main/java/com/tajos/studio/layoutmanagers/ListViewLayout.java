package com.tajos.studio.layoutmanagers;

import com.tajos.studio.components.TajosJLabel;
import com.tajos.studio.components.TajosMenuTextField;
import com.tajos.studio.graphics.RoundedPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr.
 */
public class ListViewLayout implements LayoutManager {

    private boolean mFixWidth;
    private boolean mFixHeight;
    
    private enum Margin {
        TOP, LEFT, RIGHT, BOTTOM
    }
    
    private enum SupportedComponent {
        T_TEXTFIELD, T_LABEL, T_ROUNDED_PANEL
    }
    
    /**
     * @Description the default constructor
     */
    public ListViewLayout() {
        mFixWidth = false;
        mFixHeight = false;
    }
    
    /**
     * @Description a constructor that determines if this layout size is fixed or not
     * @param fixWidth the fixed width
     * @param fixHeight the fixed height
     */
    public ListViewLayout(boolean fixWidth, boolean fixHeight) {
        mFixWidth = fixWidth;
        mFixHeight = fixHeight;
    }

    /**
     * @Description determines the preferred layout size
     * @param parent the parent where this {@LayoutManager manager} will be attached to;
     * @return the calculated preferredSize;
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (w < d.width) {
                    w = d.width;
                }
                
                h += d.getHeight();
            }
            
            return new Dimension(insets.left + insets.right + w, insets.top + insets.bottom + h);
        }
    }

    /**
     * @Description determines the preferred minimum layout size
     * @param parent the parent where this {@LayoutManager manager} will be attached to;
     * @return the calculated preferred minimum layout size;
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            
            int w = 0;
            int h = 0;
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (w < d.width) {
                    w = d.width;
                }
                if (h < d.height) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + w,
                                 insets.top + insets.bottom + h);
        }
    }

    /**
     * This will layout all the {@code childrens} of the {@code parent}
     * where this {@code manager} will be attached to;
     * @param parent the parent where this {@code manager} will be attached to;
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            
            int nextYPosition = insets.top;
            int x, y;
            
            for (int i=0; i<ncomponents; i++) {
                Component children = parent.getComponent(i);
                // custom component TajosJLabel and TajosMenuTextField is the only supported component that has margins implementation for now
                // so, we need to check the instance of children before layouting it with its margins.
                if (children instanceof TajosJLabel || children instanceof TajosMenuTextField || children instanceof RoundedPanel) {
                    
                    Map<Enum, Integer> margins;
                    if (children instanceof TajosJLabel) {
                        margins = getMappedMargins(children, SupportedComponent.T_LABEL);
                    } else if (children instanceof RoundedPanel) {
                        margins = getMappedMargins(children, SupportedComponent.T_ROUNDED_PANEL);
                    } else {
                        ((TajosMenuTextField)children).setPosition(i);
                        margins = getMappedMargins(children, SupportedComponent.T_TEXTFIELD);
                    }
                    
                    int childrenWidthWMargins = insets.left + margins.get(Margin.LEFT) + children.getPreferredSize().width +
                                        margins.get(Margin.RIGHT) + insets.right;
                    // always adjust the parent width if one of its children's width is bigger than the parent's width.
                    if (!mFixWidth && childrenWidthWMargins >= parent.getWidth()) {
                        parent.setPreferredSize(new Dimension(childrenWidthWMargins, parent.getHeight()));
                        parent.setSize(childrenWidthWMargins, parent.getHeight());
                    }
                    
                    if (i == 0) {
                        x = insets.left + margins.get(Margin.LEFT);
                        y = nextYPosition + margins.get(Margin.TOP);
                        nextYPosition = insets.top + margins.get(Margin.TOP) + children.getPreferredSize().height + margins.get(Margin.BOTTOM) + 1;
                    } else {
                        x = insets.left + margins.get(Margin.LEFT);
                        y = nextYPosition + margins.get(Margin.TOP);
                        nextYPosition = nextYPosition + margins.get(Margin.TOP) + children.getPreferredSize().height + margins.get(Margin.BOTTOM) + 1;
                    }
                    
                    if (childrenWidthWMargins >= parent.getWidth()) {
                        children.setBounds(x, y, parent.getWidth()-margins.get(Margin.LEFT), children.getPreferredSize().height);
                        continue;
                    }
                    
                    if (children instanceof RoundedPanel rPanel)
                        rPanel.setPosition(i);
                    
                    children.setBounds(x, y, children.getPreferredSize().width, children.getPreferredSize().height);
                    continue;
                }
                
                x = children.getBounds().x;
                y = nextYPosition;
                nextYPosition = nextYPosition + children.getPreferredSize().height + 1;
                children.setBounds(x, y, children.getPreferredSize().width, children.getPreferredSize().height);
            }
            // adjust the parent height
            if (!mFixHeight && nextYPosition >= parent.getHeight()) {
                parent.setPreferredSize(new Dimension(parent.getWidth(), nextYPosition));
                parent.setSize(parent.getWidth(), nextYPosition);
            } else if (!mFixHeight && nextYPosition < parent.getHeight()) {
                parent.setPreferredSize(new Dimension(parent.getWidth(), nextYPosition));
                parent.setSize(parent.getWidth(), nextYPosition);
            }
            
            parent.revalidate();
            parent.repaint();
        }
    }
    
    /**
     * @Description this will get all the margins of the {@SupportedComponent childrens} 
     * @param comp the {@SupportedComponent component} where we will get the margins
     * @param sComp the {@Enum type} which determines if the component is supported or not;
     * @return all the margins that was mapped;
     */
    private Map<Enum, Integer> getMappedMargins(Component comp, SupportedComponent sComp) {
        switch (sComp) {
            case T_LABEL -> {
                TajosJLabel tjLabel = (TajosJLabel) comp;
                Map<Enum, Integer> margins = new HashMap<>();
                margins.put(Margin.TOP, tjLabel.getMarginTop());
                margins.put(Margin.LEFT, tjLabel.getMarginLeft());
                margins.put(Margin.RIGHT, tjLabel.getMarginRight());
                margins.put(Margin.BOTTOM, tjLabel.getMarginBottom());
                
                return margins;
            }
            case T_ROUNDED_PANEL -> {
                RoundedPanel panel = (RoundedPanel) comp;
                Map<Enum, Integer> margins = new HashMap<>();
                margins.put(Margin.TOP, panel.getMarginTop());
                margins.put(Margin.LEFT, panel.getMarginLeft());
                margins.put(Margin.RIGHT, panel.getMarginRight());
                margins.put(Margin.BOTTOM, panel.getMarginBottom());
                
                return margins;
            }
            default -> {
                TajosMenuTextField txtField = (TajosMenuTextField) comp;
                Map<Enum, Integer> margins = new HashMap<>();
                margins.put(Margin.TOP, txtField.getMarginTop());
                margins.put(Margin.LEFT, txtField.getMarginLeft());
                margins.put(Margin.RIGHT, txtField.getMarginRight());
                margins.put(Margin.BOTTOM, txtField.getMarginBottom());
                    
                return margins;
            }
        }
    }
    
    public void setFixWidth(boolean bool) {
        mFixWidth = bool;
    }
    
    public void setFixHeight(boolean bool) {
        mFixHeight = bool;
    }
    
     /**
     * @NOTSUPPORTED
     * @param string
     * @param cmpnt 
     */
    @Override
    public void addLayoutComponent(String string, Component cmpnt) {}

    /**
     * @NOTSUPPORTED
     * @param comp 
     */
    @Override
    public void removeLayoutComponent(Component comp) {}
}
