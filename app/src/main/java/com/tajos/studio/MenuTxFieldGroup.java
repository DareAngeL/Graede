package com.tajos.studio;

import com.tajos.studio.interfaces.MenuTxFieldModel;
import com.tajos.studio.components.TajosMenuTextField;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonModel;

/**
 * @Description This is a copy of JDK's Built in ButtonGroup customized for {@TajosMenuTextField component}
 * @author Rene Tajos Jr.
 */
public class MenuTxFieldGroup implements Serializable {
    /**
     * The list of components participating in this group.
     */
    protected List<Component> components = new ArrayList<>();

    /**
     * The current selection.
     */
    MenuTxFieldModel selection = null;

    /**
     * Creates a new <code>MenuTxFieldGroup</code>.
     */
    public MenuTxFieldGroup() {}

    /**
     * Adds the component to the group.
     * @Customized JTextField Component
     * @param b the TajosMenuTextField Component to be added
     */
    public void add(TajosMenuTextField b) {
        if(b == null) {
            return;
        }
        components.add(b);

        if (b.isSelected()) {
            if (selection == null) {
                selection = b.getModel();
            } else {
                b.setSelected(false);
            }
        }

        b.getModel().setGroup(this);
    }
    
    public void addAll(List<Component> lst) {
        components.removeAll(components);
        components.addAll(lst);
        
        for (Component c : components) {
            if (((TajosMenuTextField)c).isSelected()) {
                selection = ((TajosMenuTextField)c).getModel();
            }
        }
    }
    
    public boolean isNameUnique(String name) {
        for (Component c : components) {
            TajosMenuTextField tx = (TajosMenuTextField) c;
            if (tx.getText().equals(name)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean contains(Component c) {
        return components.contains(c);
    }

    /**
     * Removes the component from the group.
     * @param b the component to be removed
     */
    public void remove(TajosMenuTextField b) {
        if(b == null) {
            return;
        }
        
        components.remove(b);
        if(b.getModel() == selection) {
            selection = null;
        }
    }

    /**
     * Clears the selection such that none of the components
     * in the <code>MenuTxFieldGroup</code> are selected.
     */
    public void clearSelection() {
        if (selection != null) {
            MenuTxFieldModel oldSelection = selection;
            selection = null;
            oldSelection.setSelected(false);
        }
    }

    /**
     * Returns all the components that are participating in
     * this group.
     * @return a <code>List</code> of the components in this group
     */
    public List<Component> getElements() {
        return components;
    }
    
    public List<Component> copyElements() {
        final List<Component> lst = new ArrayList<>();
        lst.addAll(components);
        
        return lst;
    }

    /**
     * Returns the model of the selected component.
     * @return the selected component model
     */
    public MenuTxFieldModel getSelection() {
        return selection;
    }

    /**
     * Sets the selected value for the <code>MenuTxFieldModel</code>.Only one component in the group may be selected at a time.
     * @param m the <code>MenuTxFieldModel</code>
     * @param b <code>true</code> if this component is to be
     *   selected, otherwise <code>false</code>
     * @param isEdit either <code>true</code> or <code>false</code> if this component is set to be edited.
     */
    public void setSelected(MenuTxFieldModel m, boolean b, boolean isEdit) {
        
        if (b && m != null && m != selection) {
            MenuTxFieldModel oldSelection = selection;
            selection = m;
            if (oldSelection != null) {
                oldSelection.setEditingMode(false);
                oldSelection.setSelected(false);
            }
            
            if (isEdit)
                m.setEditingMode(isEdit);
            m.setSelected(true);
            return;
        }
        
        if (m != null) {
            m.setEditingMode(isEdit);
            m.setSelected(true);
        }
    }
    
    /**
     * Returns whether a {@code MenuTxFieldModel} is selected.
     *
     * @param m an instance of {@code MenuTxFieldModel}
     * @return {@code true} if the component is selected,
     *   otherwise returns {@code false}
     */
    public boolean isSelected(ButtonModel m) {
        return (m == selection);
    }

    /**
     * Returns the number of components in the group.
     * @return the components count
     */
    public int size() {
        if (components == null) {
            return 0;
        } else {
            return components.size();
        }
    }

}
