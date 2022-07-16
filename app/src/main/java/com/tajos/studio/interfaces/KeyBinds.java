package com.tajos.studio.interfaces;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 *
 * @author Rene Tajos Jr.
 */
public interface KeyBinds extends KeyListener {
    /**
     * Listens for CTRL + Z keys, this is for UNDO
     * @param e The key event
     * @return  {@code true} if the key binding is pressed, {@code false} otherwise
     */
    default public boolean CTRL_Z(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_Z) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    /**
     * Listens for CTRL + Y keys, this is for REDO
     * @param e The key event
     * @return {@code true} if the key binding is pressed, {@code false} otherwise
     */
    default public boolean CTRL_Y(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_Y) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    /**
     * Listens for CTRL + R keys.this is for RENAME
     * @param e The key event
     * @return {@code true} if the key binding is pressed, {@code false} otherwise
     */
    default public boolean CTRL_R(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_R) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_X(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_X) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean ALT_DEL(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_DELETE) && (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;
    }
    
    default public boolean ALT_ENTER(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_ENTER) && (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;
    }
    
    default public boolean DELETE(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_DELETE;
    }
    
    default public boolean CTRL_C(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_C) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_V(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_V) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_S(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_S) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_B(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_B) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_I(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_I) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean CTRL_U(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_U) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean ENTER(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_ENTER;
    }
    
    default public boolean doubleClickPerformed(MouseEvent evt) {
        return evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1;
    }
    
    default public boolean BACKSPACE(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_BACK_SPACE);
    }
    
    default public boolean SHIFT(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_SHIFT;
    }
    
    default public boolean TAB(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_TAB;
    }
    
    default public boolean ARROW_LEFT(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_LEFT;
    }
    
    default public boolean ARROW_RIGHT(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_RIGHT;
    }
    
    default public boolean ARROW_DOWN(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_DOWN;
    }
    
    default public boolean ARROW_UP(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_UP;
    }
    
    default public boolean CTRL_DOWN(KeyEvent e) {
        return (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean ALT_DOWN(KeyEvent e) {
        return (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;
    }
    
    default public boolean SHIFT_DOWN(KeyEvent e) {
        return (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
    }
    
    default public boolean isArrowKeysButton(KeyEvent e) {
        return ARROW_UP(e) || ARROW_DOWN(e) || ARROW_LEFT(e) || ARROW_RIGHT(e);
    }
    
    default public boolean CTR_A(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_A) && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
    }
    
    default public boolean isNotPrintable(KeyEvent e) {
        return e.getKeyChar() == '\uFFFF';
    } 
    
    default public boolean ESCAPE(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_ESCAPE;
    }
    
    default public boolean CTRL_SHIFT_DOWN(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_DOWN) && 
               (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 &&
               (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
    }
}
