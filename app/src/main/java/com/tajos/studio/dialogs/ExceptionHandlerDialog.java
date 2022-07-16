package com.tajos.studio.dialogs;

import com.tajos.studio.activities.WorkBookActivity;
import com.tajos.studio.util.GradeUtils;
import javax.swing.ImageIcon;

/**
 *
 * @author Rene Tajos Jr
 */
public class ExceptionHandlerDialog extends javax.swing.JDialog {

    /**
     * Creates new form ExceptionHandlerDialog
     * @param parent
     * @param modal
     */
    public ExceptionHandlerDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        GradeUtils.centerFrame(this);
    }
    
    public ExceptionHandlerDialog setTextSize(int size) {
        txPane.setFont(new java.awt.Font("Nirmala UI Semilight", 0, size));
        
        return this;
    }
    
    public ExceptionHandlerDialog setExceptionMessage(String msg) {
        txPane.setText(msg);
        
        return this;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        txPane.setEditable(false);
        txPane.setBackground(new java.awt.Color(255, 255, 255));
        txPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        txPane.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        txPane.setForeground(new java.awt.Color(204, 0, 0));
        jScrollPane1.setViewportView(txPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        final ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Something went wrong");
    }//GEN-LAST:event_formWindowActivated

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane txPane;
    // End of variables declaration//GEN-END:variables
}