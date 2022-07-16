package com.tajos.studio.interfaces;

import com.tajos.studio.MenuTxFieldGroup;
import java.awt.ItemSelectable;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;

/**
 * @Desription a copy of JDK's ButtonModel customized for {@TajosMenuTextField component}
 * @author Rene Tajos Jr.
 */
public interface MenuTxFieldModel extends ItemSelectable {
    /**
     * Indicates if the model has been selected. Only needed for {@TajosMenuTextField component}
     *
     * @return <code>true</code> if the model is selected
     */
    boolean isSelected();

    /**
     * Indicates if the model can be selected or triggered by
     * an input device, such as a mouse pointer.
     *
     * @return <code>true</code> if the model is enabled
     */
    boolean isEnabled();

    /**
     * Indicates if the model is pressed.
     *
     * @return <code>true</code> if the model is pressed
     */
    boolean isPressed();

    /**
     * Indicates that the mouse is over the model.
     *
     * @return <code>true</code> if the mouse is over the model
     */
    boolean isRollover();

    /**
     * Selects or deselects the model.
     *
     * @param b <code>true</code> selects the model,
     *          <code>false</code> deselects the model
     */
    public void setSelected(boolean b);
    
    public void setEditingMode(boolean b);

    /**
     * Enables or disables the model.
     *
     * @param b whether or not the model should be enabled
     * @see #isEnabled
     */
    public void setEnabled(boolean b);

    /**
     * Sets the model to pressed or unpressed.
     *
     * @param b whether or not the model should be pressed
     * @see #isPressed
     */
    public void setPressed(boolean b);

    /**
     * Sets or clears the model's rollover state
     *
     * @param b whether or not the model is in the rollover state
     * @see #isRollover
     */
    public void setRollover(boolean b);
    
    /**
     * Sets the action command string that gets sent as part of the
     * <code>ActionEvent</code> when the model is triggered.
     *
     * @param s the <code>String</code> that identifies the generated event
     * @see #getActionCommand
     * @see java.awt.event.ActionEvent#getActionCommand
     */
    public void setActionCommand(String s);

    /**
     * Returns the action command string for the model.
     *
     * @return the <code>String</code> that identifies the generated event
     * @see #setActionCommand
     */
    public String getActionCommand();

    /**
     * Identifies the group the model belongs to --
     * needed for TajosMenuTextField models, which are mutually
     * exclusive within their group.
     *
     * @param group the <code>MenuTxFieldGroup</code> the model belongs to
     */
    public void setGroup(MenuTxFieldGroup group);

    /**
     * Returns the group that the model belongs to.
     * Normally used with TajosMenuTextField models, which are mutually
     * exclusive within their group.
     *
     * @implSpec The default implementation of this method returns {@code null}.
     * Subclasses should return the group set by setGroup().
     *
     * @return the <code>MenuTxFieldGroup</code> that the model belongs to
     */
    default MenuTxFieldGroup getGroup() {
        return null;
    }

    /**
     * Adds an <code>ActionListener</code> to the model.
     *
     * @param l the listener to add
     */
    void addActionListener(ActionListener l);

    /**
     * Removes an <code>ActionListener</code> from the model.
     *
     * @param l the listener to remove
     */
    void removeActionListener(ActionListener l);

    /**
     * Adds an <code>ItemListener</code> to the model.
     *
     * @param l the listener to add
     */
    @Override
    void addItemListener(ItemListener l);

    /**
     * Removes an <code>ItemListener</code> from the model.
     *
     * @param l the listener to remove
     */
    @Override
    void removeItemListener(ItemListener l);

    /**
     * Adds a <code>ChangeListener</code> to the model.
     *
     * @param l the listener to add
     */
    void addChangeListener(ChangeListener l);

    /**
     * Removes a <code>ChangeListener</code> from the model.
     *
     * @param l the listener to remove
     */
    void removeChangeListener(ChangeListener l);
}
