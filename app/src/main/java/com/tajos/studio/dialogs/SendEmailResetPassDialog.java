package com.tajos.studio.dialogs;

import com.tajos.studio.activities.WorkBookActivity;
import com.tajos.studio.js.adapter.JavascriptInterfaceAdapter;
import com.tajos.studio.net.DBManager;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rene Tajos Jr
 */
public class SendEmailResetPassDialog extends javax.swing.JDialog {

    private final DBManager mDBManager;

    /**
     * Creates new form FilterPopup
     * @param parent
     * @param modal
     */
    public SendEmailResetPassDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        
        mDBManager = DBManager.getInstance();
        initComponents();
        initListeners();
        
        GradeUtils.centerFrame(this);
    }
    
    private void initListeners() {
        mDBManager.getJavaScriptInterface().addJavascriptInterfaceListener(
            new JavascriptInterfaceAdapter() {
                @Override
                public void onSendEmailResetPasswordSuccess() {
                    setCursor(Cursor.getDefaultCursor());
                    
                    if (isGmail) {
                        final Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI("https://mail.google.com/"));
                            setVisible(false);
                            dispose();
                        } catch (URISyntaxException | IOException ex) {
                            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
                        }
                        
                        return;
                    }
                    
                    email_fillup_root.setVisible(false);
                    waiting_click_link_root.setVisible(true);
                }

                @Override
                public void onSendEmailResetPasswordFailed(String reason) {
                    setCursor(Cursor.getDefaultCursor());
                    
                    final StringBuilder errorBuilder = new StringBuilder();
                    errorBuilder.append(reason, reason.indexOf("(")+1, reason.length()-2);
                    reason = errorBuilder.toString().split("/")[1].replace("-", " ");
                    
                    JOptionPane.showMessageDialog(new JFrame(), reason, 
                        "Something went wrong",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        email_fillup_root = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        email_tx = new javax.swing.JTextField();
        tajosJLabel1 = new com.tajos.studio.components.TajosJLabel();
        reset_btn = new com.tajos.studio.graphics.RoundedPanel();
        reset_lbl = new com.tajos.studio.components.TajosJLabel();
        waiting_click_link_root = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        tajosJLabel2 = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel3 = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Send Email Verification");
        setBackground(new java.awt.Color(255, 255, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        email_fillup_root.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setOpaque(false);

        email_tx.setBackground(new java.awt.Color(255, 255, 255));
        email_tx.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        email_tx.setForeground(new java.awt.Color(22, 28, 72));
        email_tx.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(76, 96, 204)));

        tajosJLabel1.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel1.setText("Enter your email address:");
        tajosJLabel1.setDefaultFont(10);

        reset_btn.setBackground(new java.awt.Color(247, 245, 251));
        reset_btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        reset_btn.setBorderColor(new java.awt.Color(76, 96, 204));
        reset_btn.setBorderEnabled(true);
        reset_btn.setRadius(10);
        reset_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                reset_btnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                reset_btnMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                reset_btnMousePressed(evt);
            }
        });
        reset_btn.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        reset_lbl.setForeground(new java.awt.Color(22, 28, 72));
        reset_lbl.setText("Reset Password");
        reset_lbl.setDefaultFont(10);
        reset_btn.add(reset_lbl);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(email_tx, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                            .addComponent(tajosJLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(279, 279, 279)
                        .addComponent(reset_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(116, 116, 116))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(53, Short.MAX_VALUE)
                .addComponent(tajosJLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(email_tx, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(reset_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout email_fillup_rootLayout = new javax.swing.GroupLayout(email_fillup_root);
        email_fillup_root.setLayout(email_fillup_rootLayout);
        email_fillup_rootLayout.setHorizontalGroup(
            email_fillup_rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, email_fillup_rootLayout.createSequentialGroup()
                .addGap(0, 39, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        email_fillup_rootLayout.setVerticalGroup(
            email_fillup_rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        waiting_click_link_root.setBackground(new java.awt.Color(255, 255, 255));
        waiting_click_link_root.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());
        waiting_click_link_root.setVisible(false);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(300, 100));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout1 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout1.setAdjustableSize(true);
        gravityLayout1.setHorizontalGap(5);
        jPanel1.setLayout(gravityLayout1);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/waiting.gif"))); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(190, 170));
        jPanel1.add(jLabel1);

        jPanel2.setOpaque(false);

        tajosJLabel2.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel2.setText("Email verification sent, please verify yourself before resetting your password.");
        tajosJLabel2.setDefaultFont(12);

        tajosJLabel3.setForeground(new java.awt.Color(153, 153, 153));
        tajosJLabel3.setText("Please see your email's inbox/spam folder");
        tajosJLabel3.setDefaultFont(10);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tajosJLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(tajosJLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(tajosJLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tajosJLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2);

        waiting_click_link_root.add(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(email_fillup_root, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(waiting_click_link_root, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(email_fillup_root, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(waiting_click_link_root, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void reset_btnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reset_btnMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        reset_btn.setBackground(GradeUtils.Colors.buttonColor);
        reset_lbl.setForeground(Color.WHITE);
        
        _updateUI();
    }//GEN-LAST:event_reset_btnMouseEntered

    private void reset_btnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reset_btnMouseExited
        setCursor(Cursor.getDefaultCursor());
        reset_btn.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
        reset_lbl.setForeground(GradeUtils.Colors.darkBlueColor);
        
        _updateUI();
    }//GEN-LAST:event_reset_btnMouseExited

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
    }//GEN-LAST:event_formWindowActivated
    private boolean isGmail = false;
    private void reset_btnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reset_btnMousePressed

        String gmail = "gmail";
        if (email_tx.getText().contains(gmail)) {
            int result = JOptionPane.showConfirmDialog(this, """
                We are about to redirect you to the Gmail website

                Login to your Gmail account and click the link
                in the email that we send to verify that you are the owner
                of this email account. 
                See your spam folder if you can't find the email that we send 

                Please confirm if you want to reset your password.""");

            if (result == JOptionPane.OK_OPTION) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                isGmail = true;
                _sendEmailVerification();
            }
        } else {
            _sendEmailVerification();
        }
    }//GEN-LAST:event_reset_btnMousePressed

    private void _sendEmailVerification() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (!email_tx.getText().isEmpty() || !email_tx.getText().isBlank()) {
            mDBManager.sendResetPasswordEmail(email_tx.getText());
        } else
            JOptionPane.showMessageDialog(new JFrame(), 
                "Please enter correct email address", 
                "Unable to send verification link", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    private void _updateUI() {
        revalidate();
        repaint();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel email_fillup_root;
    private javax.swing.JTextField email_tx;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private com.tajos.studio.graphics.RoundedPanel reset_btn;
    private com.tajos.studio.components.TajosJLabel reset_lbl;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel2;
    private com.tajos.studio.components.TajosJLabel tajosJLabel3;
    private javax.swing.JPanel waiting_click_link_root;
    // End of variables declaration//GEN-END:variables
}
