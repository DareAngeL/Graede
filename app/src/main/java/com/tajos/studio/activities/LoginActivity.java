package com.tajos.studio.activities;

import com.tajos.studio.dialogs.SendEmailResetPassDialog;
import com.tajos.studio.net.DBManager;
import com.tajos.studio.net.ThirdPartyAuthentication;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author maste
 */
public class LoginActivity extends javax.swing.JFrame {
    
    private final String mEmailHint = "example@gmail.com";
    private final Color mFocusedBorderColor = new Color(0,0,191);
    private final Color mUnFocusedBorderColor = new Color(153,153,153);
    
    private final Color mFocusedTxFieldColor = new Color(22,28,72);
    private final Color mUnFocusedTxFieldColor = new Color(153,153,153);
    
    private ErrorPopupWindow mErrEmail;
    private ErrorPopupWindow mErrPass;
    private ErrorPopupWindow mErrLoginUser;
    
    private WaitingPopupWindow mWaitingPopup;
    
    private final List<ErrorPopupWindow> mPopupWindowGrp = new ArrayList<>();
    private final DBManager mDBManager;
    
    private static LoginActivity instance;
    public static LoginActivity getInstance() {
        return instance;
    }

    @Override
    public void setVisible(boolean b) {
        if (mErrEmail != null && mErrEmail.isVisible()) {
            mErrEmail.hide();
        }
        if (mErrPass != null && mErrPass.isVisible()) {
            mErrPass.hide();
        }
        if (mErrLoginUser != null && mErrLoginUser.isVisible()) {
            mErrLoginUser.hide();
        }
        if (mWaitingPopup != null && mWaitingPopup.isVisible()) {
            mWaitingPopup.hide();
        }
        
        if (!b)
            passwordField.setText("");
        
        super.setVisible(b);
    }
    
    /**
     * Creates new form LoginActivity
     */
    public LoginActivity() {
        setCursor(Cursor.getDefaultCursor());
        initComponents();
        _init();
        mDBManager = DBManager.getInstance();
        
        revalidate();
        repaint();
    }
    
    private void _init() {
        GradeUtils.centerFrame(this);
        instance = this;
        
        mErrEmail = new ErrorPopupWindow("Invalid Email!");
        mErrPass = new ErrorPopupWindow("Invalid Password!");
        // region: group all the error popup window
        mPopupWindowGrp.add(mErrEmail);
        mPopupWindowGrp.add(mErrPass);
        mPopupWindowGrp.add(mErrLoginUser);
        // region end
    }
    
    public void showLoginError(String error) {
        setCursor(Cursor.getDefaultCursor());
        
        if (mWaitingPopup != null && mWaitingPopup.isVisible())
            mWaitingPopup.hide();
        
        final StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append(error, error.indexOf("(")+1, error.length()-2);
        error = errorBuilder.toString().split("/")[1].replace("-", " ");
        
        if (mErrLoginUser == null)
            mErrLoginUser = new ErrorPopupWindow(error);
        
        mErrLoginUser.setError(error);
        mErrLoginUser.show(loginBtn, 0, loginBtn.getHeight());
    }
    
    private boolean isEmailValid(String email) {
        String[] constraints = { ".com", "@" };
        
        return email.contains(constraints[0]) && email.contains(constraints[1]);
    }
    
    private boolean isPasswordValid() {
       return passwordField.getPassword().length >= 6;
    }
    
    private void hidePopupWindows() {
        for (ErrorPopupWindow w : mPopupWindowGrp) {
            if (w != null && w.isVisible())
                w.hide();
        }
        
        if (mWaitingPopup != null && mWaitingPopup.isVisible())
            mWaitingPopup.hide();
    }
    
    private void restartUI() {
        revalidate();
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        root = new com.tajos.studio.components.BackgroundPanel();
        toolbar = new javax.swing.JPanel();
        minimizedButton1 = new com.tajos.studio.components.MinimizedButton();
        closeButton1 = new com.tajos.studio.components.CloseButton();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        loginRootPanel = new com.tajos.studio.components.BackgroundPanel();
        jPanel4 = new javax.swing.JPanel();
        tajosJLabel1 = new com.tajos.studio.components.TajosJLabel();
        signInLbl = new com.tajos.studio.components.TajosJLabel();
        jPanel1 = new javax.swing.JPanel();
        signupLbl = new com.tajos.studio.components.TajosJLabel();
        makeAccntLbl = new com.tajos.studio.components.TajosJLabel();
        emailMainPanel = new javax.swing.JPanel();
        emailLbl = new com.tajos.studio.components.TajosJLabel();
        emailPanel = new com.tajos.studio.graphics.RoundedPanel();
        emailTxField = new javax.swing.JTextField();
        imageViewer1 = new com.tajos.studio.components.ImageViewer();
        passwordMainPanel = new javax.swing.JPanel();
        passLbl = new com.tajos.studio.components.TajosJLabel();
        passwordPanel = new com.tajos.studio.graphics.RoundedPanel();
        passwordImg = new com.tajos.studio.components.ImageViewer();
        passwordField = new javax.swing.JPasswordField();
        loginBtn = new com.tajos.studio.graphics.RoundedPanel();
        tajosJLabel5 = new com.tajos.studio.components.TajosJLabel();
        jPanel2 = new javax.swing.JPanel();
        tajosJLabel6 = new com.tajos.studio.components.TajosJLabel();
        jSeparator1 = new javax.swing.JSeparator();
        googleLogoPanel = new com.tajos.studio.graphics.RoundedPanel();
        googleLogo = new com.tajos.studio.components.ImageViewer();
        imageViewer2 = new com.tajos.studio.components.ImageViewer();
        forgotPassLbl = new com.tajos.studio.components.TajosJLabel();
        resetPasswordPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        resetLbl = new com.tajos.studio.components.TajosJLabel();
        makeAccntLbl1 = new com.tajos.studio.components.TajosJLabel();
        passwordPanel1 = new com.tajos.studio.graphics.RoundedPanel();
        passwordImg1 = new com.tajos.studio.components.ImageViewer();
        passwordField1 = new javax.swing.JPasswordField();
        jPanel6 = new javax.swing.JPanel();
        makeAccntLbl2 = new com.tajos.studio.components.TajosJLabel();
        passwordPanel2 = new com.tajos.studio.graphics.RoundedPanel();
        passwordImg2 = new com.tajos.studio.components.ImageViewer();
        passwordField2 = new javax.swing.JPasswordField();
        loginBtn1 = new com.tajos.studio.graphics.RoundedPanel();
        tajosJLabel7 = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        root.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72)));

        toolbar.setBackground(new java.awt.Color(255, 255, 255));
        toolbar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                toolbarMouseDragged(evt);
            }
        });
        toolbar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                toolbarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                toolbarMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                toolbarMousePressed(evt);
            }
        });
        toolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));

        minimizedButton1.attachFrame(this);
        toolbar.add(minimizedButton1);

        closeButton1.setText("closeButton1");
        toolbar.add(closeButton1);

        loginRootPanel.setBackground(new java.awt.Color(255, 255, 255));
        loginRootPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        loginRootPanel.setOpaque(false);
        loginRootPanel.setLayout(null);

        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(260, 80));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        tajosJLabel1.setForeground(new java.awt.Color(153, 153, 153));
        tajosJLabel1.setText("You don't have an account?");
        tajosJLabel1.setDefaultFont(10);
        jPanel4.add(tajosJLabel1);

        signInLbl.setForeground(new java.awt.Color(76, 96, 204));
        signInLbl.setText("Sign up");
        signInLbl.setFont(new java.awt.Font("Nirmala UI Semilight", 3, 10)); // NOI18N
        signInLbl.setPreferredSize(new java.awt.Dimension(40, 14));
        signInLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                signInLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                signInLblMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                signInLblMousePressed(evt);
            }
        });
        jPanel4.add(signInLbl);

        loginRootPanel.add(jPanel4);
        jPanel4.setBounds(249, 4, 260, 80);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(260, 50));
        jPanel1.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        signupLbl.setForeground(new java.awt.Color(22, 28, 72));
        signupLbl.setText("Log-in");
        signupLbl.setBoldDefaultFont(16);
        jPanel1.add(signupLbl);

        makeAccntLbl.setForeground(new java.awt.Color(153, 153, 153));
        makeAccntLbl.setText("Please log-in your account.");
        makeAccntLbl.setDefaultFont(10);
        jPanel1.add(makeAccntLbl);

        loginRootPanel.add(jPanel1);
        jPanel1.setBounds(249, 92, 260, 50);

        emailMainPanel.setOpaque(false);
        emailMainPanel.setPreferredSize(new java.awt.Dimension(260, 50));
        emailMainPanel.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        emailLbl.setForeground(new java.awt.Color(22, 28, 72));
        emailLbl.setText("E-mail");
        emailLbl.setDefaultFont(12);
        emailMainPanel.add(emailLbl);

        emailPanel.setBackground(new java.awt.Color(255, 255, 255));
        emailPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        emailPanel.setBorderEnabled(true);
        emailPanel.setMarginTop(5);
        emailPanel.setPreferredSize(new java.awt.Dimension(260, 30));
        emailPanel.setRadius(10);
        emailPanel.setLayout(null);

        emailTxField.setBackground(new java.awt.Color(255, 255, 255));
        emailTxField.setForeground(new java.awt.Color(153, 153, 153));
        emailTxField.setText("example@gmail.com");
        emailTxField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        emailTxField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                emailTxFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                emailTxFieldFocusLost(evt);
            }
        });
        emailPanel.add(emailTxField);
        emailTxField.setBounds(10, 5, 210, 18);

        imageViewer1.setColorImage(new java.awt.Color(153, 153, 153));
        imageViewer1.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/at.png"))); // NOI18N
        emailPanel.add(imageViewer1);
        imageViewer1.setBounds(230, 7, 15, 15);

        emailMainPanel.add(emailPanel);

        loginRootPanel.add(emailMainPanel);
        emailMainPanel.setBounds(249, 135, 260, 50);

        passwordMainPanel.setOpaque(false);
        passwordMainPanel.setPreferredSize(new java.awt.Dimension(260, 50));
        passwordMainPanel.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        passLbl.setForeground(new java.awt.Color(22, 28, 72));
        passLbl.setText("Password  (6+ characters)");
        passLbl.setDefaultFont(12);
        passwordMainPanel.add(passLbl);

        passwordPanel.setBackground(new java.awt.Color(255, 255, 255));
        passwordPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        passwordPanel.setBorderEnabled(true);
        passwordPanel.setMarginTop(5);
        passwordPanel.setPreferredSize(new java.awt.Dimension(260, 30));
        passwordPanel.setRadius(10);
        passwordPanel.setLayout(null);

        passwordImg.setColorImage(new java.awt.Color(153, 153, 153));
        passwordImg.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/lock.png"))); // NOI18N
        passwordImg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                passwordImgMousePressed(evt);
            }
        });
        passwordPanel.add(passwordImg);
        passwordImg.setBounds(230, 7, 15, 15);

        passwordField.setBackground(new java.awt.Color(255, 255, 255));
        passwordField.setForeground(new java.awt.Color(153, 153, 153));
        passwordField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        passwordField.setFocusCycleRoot(true);
        passwordField.setFocusTraversalPolicyProvider(true);
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordFieldFocusLost(evt);
            }
        });
        passwordPanel.add(passwordField);
        passwordField.setBounds(10, 4, 210, 18);
        defaultEcho = passwordField.getEchoChar();

        passwordMainPanel.add(passwordPanel);

        loginRootPanel.add(passwordMainPanel);
        passwordMainPanel.setBounds(249, 195, 260, 50);

        loginBtn.setBackground(new java.awt.Color(76, 96, 204));
        loginBtn.setPreferredSize(new java.awt.Dimension(260, 30));
        loginBtn.setRadius(10);
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                loginBtnMousePressed(evt);
            }
        });
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout6 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout6.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout6.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout6.setVerticalGap(0);
        loginBtn.setLayout(gravityLayout6);

        tajosJLabel5.setForeground(new java.awt.Color(255, 255, 255));
        tajosJLabel5.setText("Login");
        tajosJLabel5.setDefaultFont(14);
        loginBtn.add(tajosJLabel5);

        loginRootPanel.add(loginBtn);
        loginBtn.setBounds(250, 290, 260, 30);

        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(260, 30));
        jPanel2.setLayout(null);

        tajosJLabel6.setBackground(new java.awt.Color(255, 255, 255));
        tajosJLabel6.setForeground(new java.awt.Color(153, 153, 153));
        tajosJLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel6.setText(" Or Log-in with");
        tajosJLabel6.setDefaultFont(9);
        tajosJLabel6.setOpaque(true);
        jPanel2.add(tajosJLabel6);
        tajosJLabel6.setBounds(90, 15, 80, 12);

        jSeparator1.setBackground(new java.awt.Color(215, 215, 215));
        jSeparator1.setForeground(new java.awt.Color(215, 215, 215));
        jPanel2.add(jSeparator1);
        jSeparator1.setBounds(10, 20, 240, 10);

        loginRootPanel.add(jPanel2);
        jPanel2.setBounds(250, 330, 260, 30);

        googleLogoPanel.setBackground(new java.awt.Color(255, 255, 255));
        googleLogoPanel.setBorderColor(new java.awt.Color(76, 96, 204));
        googleLogoPanel.setBorderEnabled(true);
        googleLogoPanel.setRadius(15);
        googleLogoPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                googleLogoPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                googleLogoPanelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                googleLogoPanelMousePressed(evt);
            }
        });
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout7 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout7.setAdjustableSize(true);
        gravityLayout7.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout7.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout7.setPadding(5);
        googleLogoPanel.setLayout(gravityLayout7);

        googleLogo.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/google-logo.png"))); // NOI18N
        googleLogo.setPreferredSize(new java.awt.Dimension(20, 20));
        googleLogoPanel.add(googleLogo);

        loginRootPanel.add(googleLogoPanel);
        googleLogoPanel.setBounds(360, 370, 30, 30);

        imageViewer2.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/res/design2.png"))); // NOI18N
        loginRootPanel.add(imageViewer2);
        imageViewer2.setBounds(440, 80, 320, 300);

        forgotPassLbl.setForeground(new java.awt.Color(76, 96, 204));
        forgotPassLbl.setText("I forgot my password.");
        forgotPassLbl.setDefaultFont(9);
        forgotPassLbl.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 10)); // NOI18N
        forgotPassLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPassLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPassLblMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                forgotPassLblMousePressed(evt);
            }
        });
        loginRootPanel.add(forgotPassLbl);
        forgotPassLbl.setBounds(250, 250, 100, 14);

        resetPasswordPanel.setBackground(new java.awt.Color(255, 255, 255));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout4 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout4.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout4.setVerticalGap(5);
        resetPasswordPanel.setLayout(gravityLayout4);
        resetPasswordPanel.setVisible(false);

        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(260, 50));
        jPanel5.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        resetLbl.setForeground(new java.awt.Color(22, 28, 72));
        resetLbl.setText("Reset Password");
        resetLbl.setBoldDefaultFont(16);
        jPanel5.add(resetLbl);

        makeAccntLbl1.setForeground(new java.awt.Color(153, 153, 153));
        makeAccntLbl1.setText("Enter your new password.");
        makeAccntLbl1.setDefaultFont(10);
        jPanel5.add(makeAccntLbl1);

        resetPasswordPanel.add(jPanel5);

        passwordPanel1.setBackground(new java.awt.Color(255, 255, 255));
        passwordPanel1.setBorderColor(new java.awt.Color(153, 153, 153));
        passwordPanel1.setBorderEnabled(true);
        passwordPanel1.setMarginTop(5);
        passwordPanel1.setPreferredSize(new java.awt.Dimension(260, 30));
        passwordPanel1.setRadius(10);
        passwordPanel1.setLayout(null);

        passwordImg1.setColorImage(new java.awt.Color(153, 153, 153));
        passwordImg1.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/lock.png"))); // NOI18N
        passwordPanel1.add(passwordImg1);
        passwordImg1.setBounds(230, 7, 15, 15);

        passwordField1.setBackground(new java.awt.Color(255, 255, 255));
        passwordField1.setForeground(new java.awt.Color(153, 153, 153));
        passwordField1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        passwordPanel1.add(passwordField1);
        passwordField1.setBounds(10, 4, 210, 18);
        defaultEcho = passwordField.getEchoChar();

        resetPasswordPanel.add(passwordPanel1);

        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(260, 50));
        jPanel6.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        makeAccntLbl2.setForeground(new java.awt.Color(153, 153, 153));
        makeAccntLbl2.setText("Re-enter your new password.");
        makeAccntLbl2.setDefaultFont(10);
        jPanel6.add(makeAccntLbl2);

        resetPasswordPanel.add(jPanel6);

        passwordPanel2.setBackground(new java.awt.Color(255, 255, 255));
        passwordPanel2.setBorderColor(new java.awt.Color(153, 153, 153));
        passwordPanel2.setBorderEnabled(true);
        passwordPanel2.setMarginTop(5);
        passwordPanel2.setPreferredSize(new java.awt.Dimension(260, 30));
        passwordPanel2.setRadius(10);
        passwordPanel2.setLayout(null);

        passwordImg2.setColorImage(new java.awt.Color(153, 153, 153));
        passwordImg2.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/lock.png"))); // NOI18N
        passwordPanel2.add(passwordImg2);
        passwordImg2.setBounds(230, 7, 15, 15);

        passwordField2.setBackground(new java.awt.Color(255, 255, 255));
        passwordField2.setForeground(new java.awt.Color(153, 153, 153));
        passwordField2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        passwordPanel2.add(passwordField2);
        passwordField2.setBounds(10, 4, 210, 18);
        defaultEcho = passwordField.getEchoChar();

        resetPasswordPanel.add(passwordPanel2);

        loginBtn1.setBackground(new java.awt.Color(76, 96, 204));
        loginBtn1.setPreferredSize(new java.awt.Dimension(260, 30));
        loginBtn1.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout5 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout5.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout5.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout5.setVerticalGap(0);
        loginBtn1.setLayout(gravityLayout5);

        tajosJLabel7.setForeground(new java.awt.Color(255, 255, 255));
        tajosJLabel7.setText("Change Password");
        tajosJLabel7.setDefaultFont(14);
        loginBtn1.add(tajosJLabel7);

        resetPasswordPanel.add(loginBtn1);

        jDesktopPane1.setLayer(loginRootPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(resetPasswordPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(resetPasswordPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginRootPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
            .addGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(resetPasswordPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout rootLayout = new javax.swing.GroupLayout(root);
        root.setLayout(rootLayout);
        rootLayout.setHorizontalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
            .addComponent(jDesktopPane1)
        );
        rootLayout.setVerticalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jDesktopPane1))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void toolbarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseDragged
        int x = evt.getXOnScreen() - mX;
        int y = evt.getYOnScreen() - mY;
        setLocation(x, y);
    }//GEN-LAST:event_toolbarMouseDragged

    private void toolbarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseEntered
        toolbar.setBackground(GradeUtils.Colors.creamyWhiteBlueColor);
    }//GEN-LAST:event_toolbarMouseEntered

    private void toolbarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseExited
        toolbar.setBackground(Color.WHITE);
    }//GEN-LAST:event_toolbarMouseExited
    int mX, mY;
    private void toolbarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMousePressed
        mX = evt.getX(); mY = evt.getY();
        hidePopupWindows();
    }//GEN-LAST:event_toolbarMousePressed

    private void signInLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLbl.setText("<html><body><u>Sign up</u>");
    }//GEN-LAST:event_signInLblMouseEntered

    private void signInLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMouseExited
        setCursor(Cursor.getDefaultCursor());
        signInLbl.setText("Sign up");
    }//GEN-LAST:event_signInLblMouseExited

    private void emailTxFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_emailTxFieldFocusLost
        emailPanel.setBorderColor(mUnFocusedBorderColor);

        if (emailTxField.getText().isBlank())
            emailTxField.setText(mEmailHint);

        if (emailTxField.getText().equals(mEmailHint)) {
            emailTxField.setForeground(mUnFocusedTxFieldColor);
        }

        if (!isEmailValid(emailTxField.getText())) {
            try {
                mErrEmail.show(emailPanel, 0,0);
            } catch (Exception ex) {
                GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
            }
        }

        restartUI();
    }//GEN-LAST:event_emailTxFieldFocusLost

    private boolean isVisible = false;
    private char defaultEcho;
    private void passwordImgMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_passwordImgMousePressed
        if (!isVisible) {
            passwordImg.setImageResource(new ImageIcon(getClass().getResource("/icons/unlocked.png")));
            passwordField.setEchoChar('\u0000');
            isVisible = true;
            return;
        }

        passwordImg.setImageResource(new ImageIcon(getClass().getResource("/icons/lock.png")));
        passwordField.setEchoChar(defaultEcho);
        isVisible = false;
    }//GEN-LAST:event_passwordImgMousePressed

    private void passwordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFieldFocusLost
        passwordPanel.setBorderColor(mUnFocusedBorderColor);
        passwordField.setForeground(mUnFocusedTxFieldColor);

        if (!isPasswordValid()) {
            try {
                mErrPass.show(passwordPanel, 0,0);
            } catch (Exception ex) {
                GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
            }
        }
        
        restartUI();
    }//GEN-LAST:event_passwordFieldFocusLost

    private void loginBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginBtnMousePressed
        if (mWaitingPopup == null)
            mWaitingPopup = new WaitingPopupWindow("Verifying Your Account...");

        if (isEmailValid(emailTxField.getText()) && isPasswordValid() && !emailTxField.getText().equals(mEmailHint)) {
            String email = emailTxField.getText();
            String pass = GradeUtils.charsToString(passwordField.getPassword());

            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            mWaitingPopup.show(evt.getXOnScreen(), evt.getYOnScreen() + 10);
            mDBManager.signInUserWithEmailAndPassword(email, pass);

            email = null;
            pass = null;
        } else {
            if (!isPasswordValid() && !mErrPass.isVisible()) {
                mErrPass.show(passwordPanel,0,0);
            }

            if (!isEmailValid(emailTxField.getText()) && !mErrEmail.isVisible()) {
                mErrEmail.show(emailPanel,0,0);
            }

            if (emailTxField.getText().equals(mEmailHint) && !mErrEmail.isVisible()) {
                mErrEmail.show(emailPanel,0,0);
            }
        }
    }//GEN-LAST:event_loginBtnMousePressed

    private void googleLogoPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_googleLogoPanelMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleLogoPanel.setBackground(new Color(229,229,229));

        restartUI();
    }//GEN-LAST:event_googleLogoPanelMouseEntered

    private void googleLogoPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_googleLogoPanelMouseExited
        setCursor(Cursor.getDefaultCursor());
        googleLogoPanel.setBackground(Color.WHITE);

        restartUI();
    }//GEN-LAST:event_googleLogoPanelMouseExited

    private void googleLogoPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_googleLogoPanelMousePressed
        try {
            ThirdPartyAuthentication.googleLogin(mDBManager);
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }//GEN-LAST:event_googleLogoPanelMousePressed

    private void signInLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMousePressed
        setVisible(false);
        dispose();
        
        SignupActivity act = new SignupActivity();
        act.setVisible(true);
        act.showSignupActivity();
    }//GEN-LAST:event_signInLblMousePressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Graede Login");
    }//GEN-LAST:event_formWindowActivated

    private void passwordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFieldFocusGained
        passwordPanel.setBorderColor(mFocusedBorderColor);
        passwordField.setForeground(mFocusedTxFieldColor);

        if (mErrLoginUser != null && mErrLoginUser.isVisible()) {
            mErrLoginUser.hide();
        }

        if (mErrPass != null && mErrPass.isVisible()) {
            mErrPass.hide();
        }

        restartUI();
    }//GEN-LAST:event_passwordFieldFocusGained

    private void emailTxFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_emailTxFieldFocusGained
        emailPanel.setBorderColor(mFocusedBorderColor);
        emailTxField.setForeground(mFocusedTxFieldColor);

        if (mErrLoginUser != null && mErrLoginUser.isVisible()) {
            mErrLoginUser.hide();
        }

        if (mErrEmail != null && mErrEmail.isVisible()) {
            mErrEmail.hide();
        }

        if (emailTxField.getText().equals(mEmailHint))
            emailTxField.setText("");

        restartUI();
    }//GEN-LAST:event_emailTxFieldFocusGained

    private void forgotPassLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPassLblMousePressed
        new SendEmailResetPassDialog(this, true).setVisible(true);
    }//GEN-LAST:event_forgotPassLblMousePressed

    private void forgotPassLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPassLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPassLbl.setText("<html><body><u>I forgot my password.</u>");
    }//GEN-LAST:event_forgotPassLblMouseEntered

    private void forgotPassLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPassLblMouseExited
        setCursor(Cursor.getDefaultCursor());
        forgotPassLbl.setText("I forgot my password.");
    }//GEN-LAST:event_forgotPassLblMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.tajos.studio.components.CloseButton closeButton1;
    private com.tajos.studio.components.TajosJLabel emailLbl;
    private javax.swing.JPanel emailMainPanel;
    private com.tajos.studio.graphics.RoundedPanel emailPanel;
    private javax.swing.JTextField emailTxField;
    private com.tajos.studio.components.TajosJLabel forgotPassLbl;
    private com.tajos.studio.components.ImageViewer googleLogo;
    private com.tajos.studio.graphics.RoundedPanel googleLogoPanel;
    private com.tajos.studio.components.ImageViewer imageViewer1;
    private com.tajos.studio.components.ImageViewer imageViewer2;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private com.tajos.studio.graphics.RoundedPanel loginBtn;
    private com.tajos.studio.graphics.RoundedPanel loginBtn1;
    private com.tajos.studio.components.BackgroundPanel loginRootPanel;
    private com.tajos.studio.components.TajosJLabel makeAccntLbl;
    private com.tajos.studio.components.TajosJLabel makeAccntLbl1;
    private com.tajos.studio.components.TajosJLabel makeAccntLbl2;
    private com.tajos.studio.components.MinimizedButton minimizedButton1;
    private com.tajos.studio.components.TajosJLabel passLbl;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JPasswordField passwordField1;
    private javax.swing.JPasswordField passwordField2;
    private com.tajos.studio.components.ImageViewer passwordImg;
    private com.tajos.studio.components.ImageViewer passwordImg1;
    private com.tajos.studio.components.ImageViewer passwordImg2;
    private javax.swing.JPanel passwordMainPanel;
    private com.tajos.studio.graphics.RoundedPanel passwordPanel;
    private com.tajos.studio.graphics.RoundedPanel passwordPanel1;
    private com.tajos.studio.graphics.RoundedPanel passwordPanel2;
    private com.tajos.studio.components.TajosJLabel resetLbl;
    private javax.swing.JPanel resetPasswordPanel;
    private com.tajos.studio.components.BackgroundPanel root;
    private com.tajos.studio.components.TajosJLabel signInLbl;
    private com.tajos.studio.components.TajosJLabel signupLbl;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel5;
    private com.tajos.studio.components.TajosJLabel tajosJLabel6;
    private com.tajos.studio.components.TajosJLabel tajosJLabel7;
    private javax.swing.JPanel toolbar;
    // End of variables declaration//GEN-END:variables
}
