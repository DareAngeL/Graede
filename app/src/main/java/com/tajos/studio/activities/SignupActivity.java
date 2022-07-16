package com.tajos.studio.activities;

import com.tajos.studio.net.DBManager;
import com.tajos.studio.components.TajosJLabel;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.interfaces.Shadow;
import com.tajos.studio.layoutmanagers.GravityLayout;
import com.tajos.studio.net.ThirdPartyAuthentication;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

/**
 *
 * @author Rene Tajos Jr.
 */
public class SignupActivity extends javax.swing.JFrame {

    private final String mEmailHint = "example@gmail.com";

    private final Color mFocusedBorderColor = new Color(0,0,191);
    private final Color mUnFocusedBorderColor = new Color(153,153,153);

    private final Color mFocusedTxFieldColor = new Color(22,28,72);
    private final Color mUnFocusedTxFieldColor = new Color(153,153,153);
    private final DBManager mDBManager;

    private ErrorPopupWindow mErrEmail;
    private ErrorPopupWindow mErrPass;
    private ErrorPopupWindow mErrCreateUser;
    
    private List<ErrorPopupWindow> mPopupWindowGrp = new ArrayList<>();
    
    private Timer mEmailVerificationTimer;
    private boolean isEmailVerificationTimerRunning = false;
    private WaitingPopupWindow mWaitingPopup;
    
    private static SignupActivity instance;
    public static SignupActivity getInstance() {
        if (instance == null)
            instance = new SignupActivity();
        
        return instance;
    }

    /**
     * Creates new form SignupActivity
     */
    public SignupActivity() {
        instance = this;
        mDBManager = DBManager.getInstance();
        setCursor(Cursor.getDefaultCursor());
        initComponents();
        _init();
    }

    private void _init() {
        GradeUtils.centerFrame(this);
        
        mErrEmail = new ErrorPopupWindow("Invalid Email!");
        mErrPass = new ErrorPopupWindow("Invalid Password!");
        // region: group all the error popup window
        mPopupWindowGrp.add(mErrEmail);
        mPopupWindowGrp.add(mErrPass);
        mPopupWindowGrp.add(mErrCreateUser);
        // region end
    }

    public void showSignupActivity() {
        if (isEmailVerificationTimerRunning)
            stopEmailVerificationTimer();

        loadingScreen.setVisible(false);
        waitingEmailVerificationPanel.setVisible(false);
        signUpMainPanel.setVisible(true);
    }

    private void restartUI() {
        revalidate();
        repaint();
    }

    private boolean isEmailValid(String email) {
        String[] constraints = { ".com", "@" };
        
        return email.contains(constraints[0]) && email.contains(constraints[1]);
    }
    
    private boolean isPasswordValid() {
       return passwordField.getPassword().length >= 6;
    }
    
    public void showEmailVerificationScreen() {
        signUpMainPanel.setVisible(false);
        signInMainPanel.setVisible(false);
        loadingScreen.setVisible(false);
        waitingEmailVerificationPanel.setVisible(true);
        
        setCursor(Cursor.getDefaultCursor());
        if (mWaitingPopup != null && mWaitingPopup.isVisible())
            mWaitingPopup.hide();
        
        // start the timer: this will check if the user already verify his/her email
        mEmailVerificationTimer = new Timer();
        new Thread(() -> {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    mDBManager.reloadUser();
                }
            };
            mEmailVerificationTimer.scheduleAtFixedRate(task, 0, 5000);
            isEmailVerificationTimerRunning = true;
        }).start();
    }
    
    public void showCreateUserError(String error) {
        setCursor(Cursor.getDefaultCursor());
        mWaitingPopup.hide();
        
        error = error.replace("Firebase:", ""); // remove the unnecessary word Firebase
        
        if (mErrCreateUser == null)
            mErrCreateUser = new ErrorPopupWindow(error);
        
        JOptionPane.showMessageDialog(new JFrame(), error, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void stopEmailVerificationTimer() {
        mEmailVerificationTimer.cancel();
    }
    
    private void hidePopupWindows() {
        for (ErrorPopupWindow w : mPopupWindowGrp) {
            if (w != null && w.isVisible())
                w.hide();
        }
        
        if (mWaitingPopup != null && mWaitingPopup.isVisible())
            mWaitingPopup.hide();
    }

    @Override
    public void setVisible(boolean b) {
        if (mErrEmail != null && mErrEmail.isVisible()) {
            mErrEmail.hide();
        }
        if (mErrPass != null && mErrPass.isVisible()) {
            mErrPass.hide();
        }
        if (mErrCreateUser != null && mErrCreateUser.isVisible()) {
            mErrCreateUser.hide();
        }
        if (mWaitingPopup != null && mWaitingPopup.isVisible()) {
            mWaitingPopup.hide();
        }
        
        super.setVisible(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        root = new javax.swing.JPanel();
        toolbar = new javax.swing.JPanel();
        minimizedButton1 = new com.tajos.studio.components.MinimizedButton();
        closeButton1 = new com.tajos.studio.components.CloseButton();
        contentRoot = new javax.swing.JPanel();
        signUpMainPanel = new javax.swing.JPanel();
        rootPanel = new com.tajos.studio.components.BackgroundPanel();
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
        createBtn = new com.tajos.studio.graphics.RoundedPanel();
        tajosJLabel5 = new com.tajos.studio.components.TajosJLabel();
        jPanel2 = new javax.swing.JPanel();
        tajosJLabel6 = new com.tajos.studio.components.TajosJLabel();
        jSeparator1 = new javax.swing.JSeparator();
        googleLogoPanel = new com.tajos.studio.graphics.RoundedPanel();
        googleLogo = new com.tajos.studio.components.ImageViewer();
        jPanel3 = new javax.swing.JPanel();
        imageViewer2 = new com.tajos.studio.components.ImageViewer();
        loadingScreen = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        tajosJLabel7 = new com.tajos.studio.components.TajosJLabel();
        jLabel2 = new javax.swing.JLabel();
        signInMainPanel = new javax.swing.JPanel();
        waitingEmailVerificationPanel = new javax.swing.JPanel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        tajosJLabel8 = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel9 = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel10 = new com.tajos.studio.components.TajosJLabel();
        jLabel3 = new javax.swing.JLabel();
        tajosJLabel11 = new com.tajos.studio.components.TajosJLabel();
        switchAccntLbl = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
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

        contentRoot.setBackground(new java.awt.Color(255, 255, 255));
        contentRoot.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        contentRoot.setLayout(new javax.swing.OverlayLayout(contentRoot));

        signUpMainPanel.setVisible(false);

        rootPanel.setBackground(new java.awt.Color(255, 255, 255));
        rootPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        rootPanel.setOpaque(false);
        rootPanel.setLayout(null);

        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(260, 80));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        tajosJLabel1.setForeground(new java.awt.Color(153, 153, 153));
        tajosJLabel1.setText("Already have an account?");
        tajosJLabel1.setDefaultFont(10);
        jPanel4.add(tajosJLabel1);

        signInLbl.setForeground(new java.awt.Color(76, 96, 204));
        signInLbl.setText("Sign in");
        signInLbl.setFont(new java.awt.Font("Nirmala UI Semilight", 3, 10)); // NOI18N
        signInLbl.setPreferredSize(new java.awt.Dimension(35, 14));
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

        rootPanel.add(jPanel4);
        jPanel4.setBounds(248, 3, 260, 80);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(260, 50));
        jPanel1.setLayout(new com.tajos.studio.layoutmanagers.ListViewLayout());

        signupLbl.setForeground(new java.awt.Color(22, 28, 72));
        signupLbl.setText("Sign up");
        signupLbl.setBoldDefaultFont(16);
        jPanel1.add(signupLbl);

        makeAccntLbl.setForeground(new java.awt.Color(153, 153, 153));
        makeAccntLbl.setText("Please make an account.");
        makeAccntLbl.setDefaultFont(10);
        jPanel1.add(makeAccntLbl);

        rootPanel.add(jPanel1);
        jPanel1.setBounds(248, 91, 260, 50);

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

        rootPanel.add(emailMainPanel);
        emailMainPanel.setBounds(248, 134, 260, 50);

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

        rootPanel.add(passwordMainPanel);
        passwordMainPanel.setBounds(248, 194, 260, 50);

        createBtn.setBackground(new java.awt.Color(76, 96, 204));
        createBtn.setPreferredSize(new java.awt.Dimension(260, 30));
        createBtn.setRadius(10);
        createBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                createBtnMousePressed(evt);
            }
        });
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout2 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout2.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout2.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout2.setVerticalGap(0);
        createBtn.setLayout(gravityLayout2);

        tajosJLabel5.setForeground(new java.awt.Color(255, 255, 255));
        tajosJLabel5.setText("Create Account");
        tajosJLabel5.setDefaultFont(14);
        createBtn.add(tajosJLabel5);

        rootPanel.add(createBtn);
        createBtn.setBounds(248, 254, 260, 30);

        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(260, 30));
        jPanel2.setLayout(null);

        tajosJLabel6.setBackground(new java.awt.Color(255, 255, 255));
        tajosJLabel6.setForeground(new java.awt.Color(153, 153, 153));
        tajosJLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel6.setText(" Or sign up with");
        tajosJLabel6.setDefaultFont(9);
        tajosJLabel6.setOpaque(true);
        jPanel2.add(tajosJLabel6);
        tajosJLabel6.setBounds(90, 15, 80, 12);

        jSeparator1.setBackground(new java.awt.Color(215, 215, 215));
        jSeparator1.setForeground(new java.awt.Color(215, 215, 215));
        jPanel2.add(jSeparator1);
        jSeparator1.setBounds(10, 20, 240, 10);

        rootPanel.add(jPanel2);
        jPanel2.setBounds(248, 292, 260, 30);

        googleLogoPanel.setBackground(new java.awt.Color(255, 255, 255));
        googleLogoPanel.setBorderColor(new java.awt.Color(76, 96, 204));
        googleLogoPanel.setBorderEnabled(true);
        googleLogoPanel.setPreferredSize(new java.awt.Dimension(30, 30));
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
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout5 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout5.setPadding(5);
        gravityLayout5.setAdjustableSize(true);
        gravityLayout5.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout5.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        googleLogoPanel.setLayout(gravityLayout5);

        googleLogo.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/icons/google-logo.png"))); // NOI18N
        googleLogo.setPreferredSize(new java.awt.Dimension(20, 20));
        googleLogoPanel.add(googleLogo);

        rootPanel.add(googleLogoPanel);
        googleLogoPanel.setBounds(363, 330, 30, 30);

        jPanel3.setOpaque(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        rootPanel.add(jPanel3);
        jPanel3.setBounds(328, 368, 100, 100);

        imageViewer2.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/res/design1.png"))); // NOI18N
        rootPanel.add(imageViewer2);
        imageViewer2.setBounds(0, 240, 220, 230);

        javax.swing.GroupLayout signUpMainPanelLayout = new javax.swing.GroupLayout(signUpMainPanel);
        signUpMainPanel.setLayout(signUpMainPanelLayout);
        signUpMainPanelLayout.setHorizontalGroup(
            signUpMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rootPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE)
        );
        signUpMainPanelLayout.setVerticalGroup(
            signUpMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rootPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
        );

        contentRoot.add(signUpMainPanel);

        loadingScreen.setBackground(new java.awt.Color(255, 255, 255));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout3 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout3.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.CENTER);
        gravityLayout3.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        loadingScreen.setLayout(gravityLayout3);

        jLayeredPane1.setPreferredSize(new java.awt.Dimension(200, 101));

        tajosJLabel7.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel7.setText("Connecting to Graede...");
        tajosJLabel7.setDefaultFont(12);
        jLayeredPane1.add(tajosJLabel7);
        tajosJLabel7.setBounds(30, 70, 150, 15);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/loading.gif"))); // NOI18N
        jLayeredPane1.add(jLabel2);
        jLabel2.setBounds(50, 0, 100, 100);

        loadingScreen.add(jLayeredPane1);

        contentRoot.add(loadingScreen);

        signInMainPanel.setOpaque(false);

        javax.swing.GroupLayout signInMainPanelLayout = new javax.swing.GroupLayout(signInMainPanel);
        signInMainPanel.setLayout(signInMainPanelLayout);
        signInMainPanelLayout.setHorizontalGroup(
            signInMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );
        signInMainPanelLayout.setVerticalGroup(
            signInMainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 471, Short.MAX_VALUE)
        );

        contentRoot.add(signInMainPanel);

        waitingEmailVerificationPanel.setVisible(false);
        waitingEmailVerificationPanel.setBackground(new java.awt.Color(255, 255, 255));
        waitingEmailVerificationPanel.setLayout(null);

        jLayeredPane2.setPreferredSize(new java.awt.Dimension(400, 135));

        tajosJLabel8.setForeground(new java.awt.Color(102, 102, 102));
        tajosJLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel8.setText("This is for us to make sure that you are the owner of the email");
        tajosJLabel8.setDefaultFont(10);
        jLayeredPane2.add(tajosJLabel8);
        tajosJLabel8.setBounds(40, 120, 340, 13);

        tajosJLabel9.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel9.setText("Waiting for email verification.");
        tajosJLabel9.setDefaultFont(12);
        jLayeredPane2.add(tajosJLabel9);
        tajosJLabel9.setBounds(100, 70, 220, 15);

        tajosJLabel10.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tajosJLabel10.setText("Please see your email/spam inbox to verify.");
        tajosJLabel10.setDefaultFont(12);
        jLayeredPane2.add(tajosJLabel10);
        tajosJLabel10.setBounds(80, 90, 260, 15);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/loading.gif"))); // NOI18N
        jLayeredPane2.add(jLabel3);
        jLabel3.setBounds(150, 0, 100, 100);

        tajosJLabel11.setForeground(new java.awt.Color(102, 102, 102));
        tajosJLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel11.setText("Note: You can't use this app if you don't verify your email");
        tajosJLabel11.setDefaultFont(10);
        jLayeredPane2.add(tajosJLabel11);
        tajosJLabel11.setBounds(70, 110, 290, 13);

        waitingEmailVerificationPanel.add(jLayeredPane2);
        jLayeredPane2.setBounds(178, 168, 400, 135);

        switchAccntLbl.setForeground(new java.awt.Color(22, 28, 72));
        switchAccntLbl.setText("<html><body><u>Switch Account</u>");
        switchAccntLbl.setDefaultFont(10);
        switchAccntLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                switchAccntLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                switchAccntLblMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                switchAccntLblMousePressed(evt);
            }
        });
        waitingEmailVerificationPanel.add(switchAccntLbl);
        switchAccntLbl.setBounds(660, 20, 80, 16);

        contentRoot.add(waitingEmailVerificationPanel);

        javax.swing.GroupLayout rootLayout = new javax.swing.GroupLayout(root);
        root.setLayout(rootLayout);
        rootLayout.setHorizontalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        rootLayout.setVerticalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contentRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(root, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void emailTxFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_emailTxFieldFocusLost
        emailPanel.setBorderColor(mUnFocusedBorderColor);
        
        if (emailTxField.getText().isBlank())
            emailTxField.setText(mEmailHint);
        
        if (emailTxField.getText().equals(mEmailHint))
            emailTxField.setForeground(mUnFocusedTxFieldColor);
        
        if (!isEmailValid(emailTxField.getText())) {
            try {
                mErrEmail.show(emailPanel, 0,0);
            } catch (Exception ex) {}
        }
        
        restartUI();
    }//GEN-LAST:event_emailTxFieldFocusLost

    private void passwordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFieldFocusLost
        passwordPanel.setBorderColor(mUnFocusedBorderColor);
        passwordField.setForeground(mUnFocusedTxFieldColor);
        
        if (!isPasswordValid()) {
            try {
                mErrPass.show(passwordPanel, 0,0);
            } catch (Exception ex) {}
        }
        
        restartUI();
    }//GEN-LAST:event_passwordFieldFocusLost

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

    private void googleLogoPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_googleLogoPanelMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleLogoPanel.setBackground(new Color(229,229,229));
        
        restartUI();
    }//GEN-LAST:event_googleLogoPanelMouseEntered
    /**
     * Toggle on/off password visibility
     * @param evt 
     */
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

    private void createBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createBtnMousePressed
        if (mWaitingPopup == null)
            mWaitingPopup = new WaitingPopupWindow("Sending Verification Link");
        
        if (isEmailValid(emailTxField.getText()) && isPasswordValid() && !emailTxField.getText().equals(mEmailHint)) {
            String email = emailTxField.getText();
            String pass = GradeUtils.charsToString(passwordField.getPassword());
            
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            mWaitingPopup.show(evt.getXOnScreen(), evt.getYOnScreen() + 10);
            mDBManager.signUpUserWithEmailAndPassword(email, pass);
            
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
    }//GEN-LAST:event_createBtnMousePressed

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        if (mWaitingPopup != null)
            mWaitingPopup.move(evt.getXOnScreen(), evt.getYOnScreen() + 10);
    }//GEN-LAST:event_formMouseMoved

    private void switchAccntLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_switchAccntLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_switchAccntLblMouseEntered

    private void switchAccntLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_switchAccntLblMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_switchAccntLblMouseExited

    private void switchAccntLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_switchAccntLblMousePressed
        mDBManager.signOutUser();
    }//GEN-LAST:event_switchAccntLblMousePressed
    private int mX, mY;
    private void toolbarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMousePressed
        mX = evt.getX(); mY = evt.getY();
        hidePopupWindows();
    }//GEN-LAST:event_toolbarMousePressed

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

    private void signInLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLbl.setText("<html><body><u>Sign in</u>");
    }//GEN-LAST:event_signInLblMouseEntered

    private void signInLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMouseExited
        setCursor(Cursor.getDefaultCursor());
        signInLbl.setText("Sign in");
    }//GEN-LAST:event_signInLblMouseExited

    private void signInLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLblMousePressed
        setVisible(false);
        dispose();
        
        new LoginActivity().setVisible(true);
    }//GEN-LAST:event_signInLblMousePressed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Graede Sign-up");
    }//GEN-LAST:event_formWindowActivated

    private void passwordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFieldFocusGained
        passwordPanel.setBorderColor(mFocusedBorderColor);
        passwordField.setForeground(mFocusedTxFieldColor);
        
        
        if (mErrCreateUser != null && mErrCreateUser.isVisible()) {
            mErrCreateUser.hide();
        }
        
        if (mErrPass != null && mErrPass.isVisible()) {
            mErrPass.hide();
        }
        
        restartUI();
    }//GEN-LAST:event_passwordFieldFocusGained

    private void emailTxFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_emailTxFieldFocusGained
        emailPanel.setBorderColor(mFocusedBorderColor);
        emailTxField.setForeground(mFocusedTxFieldColor);

        if (mErrCreateUser != null && mErrCreateUser.isVisible()) {
            mErrCreateUser.hide();
        }

        if (mErrEmail != null && mErrEmail.isVisible()) {
            mErrEmail.hide();
        }

        if (emailTxField.getText().equals(mEmailHint))
            emailTxField.setText("");

        restartUI();
    }//GEN-LAST:event_emailTxFieldFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.tajos.studio.components.CloseButton closeButton1;
    private javax.swing.JPanel contentRoot;
    private com.tajos.studio.graphics.RoundedPanel createBtn;
    private com.tajos.studio.components.TajosJLabel emailLbl;
    private javax.swing.JPanel emailMainPanel;
    private com.tajos.studio.graphics.RoundedPanel emailPanel;
    private javax.swing.JTextField emailTxField;
    private com.tajos.studio.components.ImageViewer googleLogo;
    private com.tajos.studio.graphics.RoundedPanel googleLogoPanel;
    private com.tajos.studio.components.ImageViewer imageViewer1;
    private com.tajos.studio.components.ImageViewer imageViewer2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel loadingScreen;
    private com.tajos.studio.components.TajosJLabel makeAccntLbl;
    private com.tajos.studio.components.MinimizedButton minimizedButton1;
    private com.tajos.studio.components.TajosJLabel passLbl;
    private javax.swing.JPasswordField passwordField;
    private com.tajos.studio.components.ImageViewer passwordImg;
    private javax.swing.JPanel passwordMainPanel;
    private com.tajos.studio.graphics.RoundedPanel passwordPanel;
    private javax.swing.JPanel root;
    private com.tajos.studio.components.BackgroundPanel rootPanel;
    private com.tajos.studio.components.TajosJLabel signInLbl;
    private javax.swing.JPanel signInMainPanel;
    private javax.swing.JPanel signUpMainPanel;
    private com.tajos.studio.components.TajosJLabel signupLbl;
    private com.tajos.studio.components.TajosJLabel switchAccntLbl;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel10;
    private com.tajos.studio.components.TajosJLabel tajosJLabel11;
    private com.tajos.studio.components.TajosJLabel tajosJLabel5;
    private com.tajos.studio.components.TajosJLabel tajosJLabel6;
    private com.tajos.studio.components.TajosJLabel tajosJLabel7;
    private com.tajos.studio.components.TajosJLabel tajosJLabel8;
    private com.tajos.studio.components.TajosJLabel tajosJLabel9;
    private javax.swing.JPanel toolbar;
    private javax.swing.JPanel waitingEmailVerificationPanel;
    // End of variables declaration//GEN-END:variables
}

class ErrorPopupWindow {

    private final JWindow mWindow = new JWindow();
    private Point p;
    private TajosJLabel lbl;
    private RoundedPanel _root;

    public ErrorPopupWindow() {
        init("");
    }

    public ErrorPopupWindow(String errTxt) {
        init(errTxt);
    }

    private void init(String errTxt) {
        lbl = new TajosJLabel();
        lbl.setText(errTxt);
        lbl.setDefaultFont(10);
        lbl.setForeground(Color.RED);

        Rectangle2D errTxtRect = GradeUtils.getTextRect(
            lbl.getFontMetrics(lbl.getFont()), lbl.getGraphics(), errTxt);

        mWindow.setBackground(GradeUtils.Colors.transparent);
        mWindow.setSize((int)errTxtRect.getWidth() + 10, 20);
        _root = new RoundedPanel();
        _root.setRadius(10);
        _root.setBorderEnabled(true);
        _root.setBorderColor(Color.RED);
        _root.setShadowEnabled(true);
        _root.setShadowType(Shadow.ShadowType.BOT);
        _root.setShadowSize(2);
        _root.setBackground(Color.WHITE);
        _root.setSize((int)errTxtRect.getWidth() + 10, 20);
        GravityLayout layout = new GravityLayout();
        layout.setGravity(GravityLayout.Gravity.LEFT);
        layout.setLeftPadding(mWindow.getSize().width/2 - (int)errTxtRect.getWidth()/2);
        layout.setTopPadding(mWindow.getSize().height/2 - (int)errTxtRect.getHeight()/2 - 2);
        _root.setLayout(layout);

        _root.add(lbl);
        mWindow.add(_root);
    }

    public void show(Component invoker, int x, int y) {
        p = invoker.getLocationOnScreen();
        mWindow.setLocation((int)p.getX() + x, (int)p.getY() + y);
        mWindow.setVisible(true);
    }

    public void setError(String err) {
        lbl.setText(err);
        // region: update window size
        Rectangle2D errTxtRect = GradeUtils.getTextRect(
            lbl.getFontMetrics(lbl.getFont()), lbl.getGraphics(), err);
        mWindow.setSize((int)errTxtRect.getWidth() + 10, 20);
        _root.setSize((int)errTxtRect.getWidth() + 10, 20);
        GravityLayout layout = new GravityLayout();
        layout.setGravity(GravityLayout.Gravity.LEFT);
        layout.setLeftPadding(mWindow.getSize().width/2 - (int)errTxtRect.getWidth()/2);
        layout.setTopPadding(mWindow.getSize().height/2 - (int)errTxtRect.getHeight()/2 - 2);
        _root.setLayout(layout);
        // region end
    }

    public void hide() {
        mWindow.setVisible(false);
        mWindow.dispose();
    }

    public boolean isVisible() {
        return mWindow.isVisible();
    }
}

class WaitingPopupWindow {

    private final JWindow mWindow = new JWindow();
    private Point p;

    public WaitingPopupWindow(String txt) {
        TajosJLabel lbl = new TajosJLabel();
        lbl.setText(txt);
        lbl.setDefaultFont(10);
        lbl.setForeground(GradeUtils.Colors.darkBlueColor);

        Rectangle2D errTxtRect = GradeUtils.getTextRect(
            lbl.getFontMetrics(lbl.getFont()), lbl.getGraphics(), txt);

        mWindow.setBackground(GradeUtils.Colors.transparent);
        mWindow.setSize((int)errTxtRect.getWidth() + 10, 20);
        RoundedPanel _root = new RoundedPanel();
        _root.setRadius(10);
        _root.setBorderEnabled(true);
        _root.setBorderColor(GradeUtils.Colors.darkBlueColor);
        _root.setBackground(Color.WHITE);
        _root.setSize((int)errTxtRect.getWidth() + 10, 20);
        GravityLayout layout = new GravityLayout();
        layout.setGravity(GravityLayout.Gravity.LEFT);
        layout.setLeftPadding(mWindow.getSize().width/2 - (int)errTxtRect.getWidth()/2);
        layout.setTopPadding(mWindow.getSize().height/2 - (int)errTxtRect.getHeight()/2 - 2);
        _root.setLayout(layout);

        _root.add(lbl);
        mWindow.add(_root);
    }

    public void show(int x, int y) {
        mWindow.setLocation(x, y);
        mWindow.setVisible(true);
    }

    public void move(int x, int y) {
        mWindow.setLocation(x, y);
    }

    public void hide() {
        mWindow.setVisible(false);
        mWindow.dispose();
    }

    public boolean isVisible() {
        return mWindow.isVisible();
    }
}