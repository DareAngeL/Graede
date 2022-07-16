package com.tajos.studio.dialogs;

import com.tajos.studio.excel.ExcelHandler;
import com.tajos.studio.util.GradeUtils;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rene Tajos Jr
 */
public class ImportingProgressDialog extends javax.swing.JDialog {

    private OnImportingListener listener;
    public interface OnImportingListener {
        void onImportingFinished(Map<String, Map<String, List<List<Object>>>> data, boolean success);
    }
    
    /**
     * Creates new form ImportingPopup
     * @param parent
     * @param modal
     * @param excelHandler
     */
    public ImportingProgressDialog(java.awt.Frame parent, boolean modal, ExcelHandler excelHandler) {
        super(parent, modal);
        initComponents();
        GradeUtils.centerFrame(this);
        
        _initImporting(excelHandler);
    }
    
    private void _initImporting(ExcelHandler handler) {
        handler.handle((data, success) -> {
            // on handling finish
            listener.onImportingFinished(data, success);
        });
    }
    
    public void setOnImportingListener(OnImportingListener listener) {
        this.listener = listener;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tajosJLabel1 = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel2 = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout1 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout1.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        jPanel1.setLayout(gravityLayout1);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/importing90x90.gif"))); // NOI18N
        jPanel1.add(jLabel1);

        tajosJLabel1.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel1.setText("Importing Excel Data...");
        tajosJLabel1.setDefaultFont(12);
        jPanel1.add(tajosJLabel1);

        tajosJLabel2.setForeground(new java.awt.Color(62, 71, 142));
        tajosJLabel2.setText("Please wait.");
        tajosJLabel2.setDefaultFont(10);
        jPanel1.add(tajosJLabel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        requestFocusInWindow();
    }//GEN-LAST:event_formWindowLostFocus

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel2;
    // End of variables declaration//GEN-END:variables
}
