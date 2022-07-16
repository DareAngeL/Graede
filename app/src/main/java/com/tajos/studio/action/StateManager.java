package com.tajos.studio.action;

import com.tajos.studio.interfaces.State;

public class StateManager {
    
    protected Node currentIndex = null;
    protected Node parentNode = new Node();
    
    public StateManager() {
        currentIndex = parentNode;
    }
    
    public StateManager(StateManager manager) {
        this();
        currentIndex = manager.currentIndex;
    }
    
    public void clear() {
        currentIndex = parentNode;
    }
    
    public boolean canUndo() {
        return currentIndex != parentNode;
    }
    
    public boolean canRedo() {
        return currentIndex.right != null;
    }
    
    public void undo(){
        if (!canUndo())
            return;
        
        currentIndex.state.undo();
        moveLeft();
    }
    
    public void redo() {
        if (!canRedo())
            return;

        moveRight();
        currentIndex.state.redo();
    }
    
    protected void moveLeft() {
        if (currentIndex.left == null) {
            throw new IllegalStateException("Internal index set to null.");
        }

        currentIndex = currentIndex.left;
    }
    
    protected void moveRight(){

        if (currentIndex.right == null) {
            throw new IllegalStateException("Internal index set to null.");
        }

        currentIndex = currentIndex.right;
    }
    
    public void addNewState(State state) {
        Node node = new Node(state);
        currentIndex.right = node;
        node.left = currentIndex;
        currentIndex = node;
    }
    
    public class Node {
        public Node left = null;
        public Node right = null;
        
        protected final State state;
        
        public Node(State c){
            state = c;
        }
        
        public Node(){
            state = null;
        }
    }
}
