package com.tajos.studio.activities;

import com.tajos.studio.net.DBManager;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.json.simple.JSONObject;

/**
 *
 * @author Rene Tajos Jr
 */
public class MyProfileActivity extends javax.swing.JFrame {
    
    private TxFieldHandler firstNameTxFldHandler;
    private TxFieldHandler schoolNameTxFldHandler;
    private TxFieldHandler customTxFldHandler;
    private TxFieldHandler contactNumHandler;
    private TxFieldHandler mosTxFldHandler;
    private TxFieldHandler daysTxFldHandler;
    private JSONObject userInfo;
    private TxFieldHandler yearTxFldHandler;
    
    private final List<TxFieldHandler> constrainstsGrp = new ArrayList<>();
    private ErrorPopupWindow errPopupWindow;

    /**
     * Creates new form MyProfileActivity
     */
    public MyProfileActivity() {
        initComponents();
        init();
        _initUserInfo();
        GradeUtils.centerFrame(this);
        
        updateUI();
    }
    
    private void init() {
        errPopupWindow = new ErrorPopupWindow("Fix errors first!");
        
        firstNameTxFldHandler = new TxFieldHandler(firstNameTxFld).handle();
        schoolNameTxFldHandler = new TxFieldHandler(schoolNameTxFld).handle();
        customTxFldHandler = new TxFieldHandler(customTxFld).handle();
        customTxFldHandler.linkToJRadioBtn(customBtn);
        
        contactNumHandler = new TxFieldHandler(contactNumberTxFld).handle();
        contactNumHandler.addConstraints("0123456789");
        contactNumHandler.setPriority(TxFieldHandler.PriorityType.OPTIONAL);
        
        mosTxFldHandler = new TxFieldHandler(mosTxFld).handle();
        mosTxFldHandler.addConstraints("0123456789");
        mosTxFldHandler.setMaxChar(2);
        mosTxFldHandler.setPriority(TxFieldHandler.PriorityType.OPTIONAL);
        
        daysTxFldHandler = new TxFieldHandler(daysTxFld).handle();
        daysTxFldHandler.addConstraints("0123456789");
        daysTxFldHandler.setMaxChar(2);
        daysTxFldHandler.setPriority(TxFieldHandler.PriorityType.OPTIONAL);
        
        yearTxFldHandler = new TxFieldHandler(yearTxFld).handle();
        yearTxFldHandler.addConstraints("0123456789");
        yearTxFldHandler.setMaxChar(4);
        yearTxFldHandler.setPriority(TxFieldHandler.PriorityType.OPTIONAL);
        
        constrainstsGrp.add(contactNumHandler);
        constrainstsGrp.add(mosTxFldHandler);
        constrainstsGrp.add(daysTxFldHandler);
        constrainstsGrp.add(yearTxFldHandler);
        constrainstsGrp.add(firstNameTxFldHandler);
        constrainstsGrp.add(schoolNameTxFldHandler);
        constrainstsGrp.add(customTxFldHandler);
        
        firstNameTxFld.requestFocusInWindow();
        firstNameTxFld.setCaretPosition(0);
    }
    
    private void _initUserInfo() {
        if (DBManager.getInstance().getUserData() == null)
            return;
        
        userInfo = (JSONObject) DBManager.getInstance().getUserData().get("info");
        if (userInfo == null)
            return;
        
        boolean isNameEmpty = userInfo.get("name").toString().isEmpty();
        if (!isNameEmpty) {
            firstNameTxFld.setText(userInfo.get("name").toString());
            firstNameTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
        }
        
        boolean isContactEmpty = userInfo.get("contact").toString().isEmpty();
        if (!isContactEmpty) {
            contactNumberTxFld.setText(userInfo.get("contact").toString());
            contactNumberTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
        }
        
        boolean isSchoolNameEmpty = userInfo.get("school_name").toString().isEmpty();
        if (!isSchoolNameEmpty) {
            schoolNameTxFld.setText(userInfo.get("school_name").toString());
            schoolNameTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
        }
        
        boolean isBdayEmpty = userInfo.get("bday").toString().isEmpty();
        if (!isBdayEmpty) {
            String bdayStr = userInfo.get("bday").toString();
            String[] bdayArr = bdayStr.split("/");
            mosTxFld.setText(bdayArr[0]);
            mosTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
            daysTxFld.setText(bdayArr[1]);
            daysTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
            yearTxFld.setText(bdayArr[2]);
            yearTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
        }
        
        boolean isGenderEmpty = userInfo.get("gender").toString().isEmpty();
        if (!isGenderEmpty) {
            switch (userInfo.get("gender").toString()) {
                case "Male" -> {
                    maleBtn.setSelected(true);
                }
                case "Female" -> {
                    femaleBtn.setSelected(true);
                }
                default -> {
                    customBtn.setSelected(true);
                    customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 22));
                    customPanel.setVisible(true);
                    customTxFld.setText(userInfo.get("gender").toString());
                    customTxFld.setForeground(GradeUtils.Colors.darkBlueColor);
                }
            }
        }
        
        root.setVisible(true);
        loadingPanel.setVisible(false);
    }
    
    private class TxFieldHandler implements KeyBinds {

        private final JTextField mTxFld;
        private final RoundedPanel parent;
        private final String mHint;
        
        private final Color filledTxColor = GradeUtils.Colors.darkBlueColor;
        private final Color nonFilledTxColor = new Color(153,153,153);
        private final Color selectedBorderColor = new Color(76,96,204);
        private final Color unselectedBorderColor = new Color(153,153,153);
        private String mConstraints;
        private int mMaxChar = -1;
        private PriorityType mPriorityType = PriorityType.REQUIRED;
        private JRadioButton mLinked;
        
        public enum PriorityType {
            OPTIONAL, REQUIRED
        }
        
        public TxFieldHandler(JTextField txFld) {
            mTxFld = txFld;
            mHint = txFld.getText();
            
            parent = (RoundedPanel) mTxFld.getParent();
        }
        
        public TxFieldHandler handle() {
            _initListeners();
            
            return this;
        }
        
        public TxFieldHandler addConstraints(String cons) {
            mConstraints = cons;
            return this;
        }
        
        public void linkToJRadioBtn(JRadioButton btn) {
            mLinked = btn;
        }
        
        public boolean isOnError() {
            if (mPriorityType == PriorityType.REQUIRED)  {
                if (mTxFld.getText().equals(mHint) && mLinked == null) {
                    handleError();
                    return true;
                } else if (mTxFld.getText().equals(mHint) && mLinked != null) {
                   if (mLinked.isSelected()) {
                       handleError();
                       return true;
                   }
                   parent.setBorderColor(unselectedBorderColor);
                   return false;
                }
            }
            
            return hasError();
        }
        
        public void setMaxChar(int max) {
            mMaxChar = max;
        }
        
        public String getAbsoluteText() {
            return mTxFld.getText().equals(mHint) ? "" : mTxFld.getText();
        }
        
        public void setPriority(PriorityType type) {
            mPriorityType = type;
        }
        
        private void handleError() {
            parent.setBorderColor(Color.RED);
        }
        
        private void _initListeners() {
            mTxFld.addKeyListener(this);
            mTxFld.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (mTxFld.getText().equals(mHint)) {
                        mTxFld.setCaretPosition(0);
                    }
                    
                    if (!mTxFld.getCaret().isVisible())
                        mTxFld.getCaret().setVisible(true);
                }
            });
            
            mTxFld.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    
                    if (errPopupWindow != null && errPopupWindow.isVisible())
                        errPopupWindow.hide();
                    
                    parent.setBorderColor(selectedBorderColor);
                    updateUI();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (hasError())
                        return;
                    
                    parent.setBorderColor(unselectedBorderColor);
                    updateUI();
                }
            });
        }
        
        private boolean hasError() {
            if (mConstraints != null) {
                // region: check if the text meets the added contraints
                for (char c : mTxFld.getText().toCharArray()) {
                    if (!mTxFld.getText().equals(mHint) && 
                        !mConstraints.contains(String.valueOf(c))) 
                    {
                        handleError();
                        updateUI();
                        return true;
                    }
                }
                // region end
            }
            
            return false;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (isNotPrintable(e)) { // if the key is not printable then just return
                if (isArrowKeysButton(e) && mTxFld.getText().equals(mHint))
                    mTxFld.getCaret().setVisible(false);
                
                return;
            }
            
            if (CTR_A(e))
                return;
            
            if (BACKSPACE(e) && mTxFld.getText().equals(mHint)) {
                mTxFld.setText(mHint);
                mTxFld.setForeground(nonFilledTxColor);
                mTxFld.setCaretPosition(0);
            }
            
            mTxFld.getCaret().setVisible(true);
            if (!BACKSPACE(e) && mTxFld.getText().equals(mHint)) {
                mTxFld.setForeground(filledTxColor);
                mTxFld.setText("");
            }
            
            if (BACKSPACE(e) && (mTxFld.getSelectedText() != null 
                    && mHint.contains(mTxFld.getSelectedText())))
            {
                mTxFld.setText(mHint);
                mTxFld.setCaretPosition(0);
                return;
            }
            
            if (BACKSPACE(e) && (mTxFld.getText().length() == 1 || 
                (mTxFld.getSelectedText() != null && mTxFld.getSelectedText().equals(mTxFld.getText()))))
            {
                mTxFld.setText(mHint);
                mTxFld.setForeground(nonFilledTxColor);
                mTxFld.setCaretPosition(0);
            }
        }
        
        private void updateUI() {
            MyProfileActivity.this.revalidate();
            MyProfileActivity.this.repaint();
        }
        
        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyReleased(KeyEvent e) {
            if (mMaxChar != -1 && !mTxFld.getText().equals(mHint) &&
                mTxFld.getText().length() > mMaxChar)
            {
                String tx = mTxFld.getText().substring(0, mMaxChar);
                mTxFld.setText(tx);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        loadingPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tajosJLabel7 = new com.tajos.studio.components.TajosJLabel();
        closeButton2 = new com.tajos.studio.components.CloseButton();
        root = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        closeButton1 = new com.tajos.studio.components.CloseButton();
        jPanel1 = new javax.swing.JPanel();
        tajosJLabel1 = new com.tajos.studio.components.TajosJLabel();
        imageViewer1 = new com.tajos.studio.components.ImageViewer();
        contentPanel = new javax.swing.JPanel();
        RequiredLblRootPanel = new javax.swing.JPanel();
        tajosJLabel10 = new com.tajos.studio.components.TajosJLabel();
        NamesRootPanel = new javax.swing.JPanel();
        firstNamePanel = new com.tajos.studio.graphics.RoundedPanel();
        firstNameTxFld = new javax.swing.JTextField();
        ContactNumberRootPanel = new javax.swing.JPanel();
        contactNumberPanel = new com.tajos.studio.graphics.RoundedPanel();
        contactNumberTxFld = new javax.swing.JTextField();
        SchoolNameRootPanel = new javax.swing.JPanel();
        schoolNamePanel = new com.tajos.studio.graphics.RoundedPanel();
        schoolNameTxFld = new javax.swing.JTextField();
        space1 = new javax.swing.JPanel();
        bdayLblRootPanel = new javax.swing.JPanel();
        tajosJLabel3 = new com.tajos.studio.components.TajosJLabel();
        bdayRootPanel = new javax.swing.JPanel();
        mosPanel = new com.tajos.studio.graphics.RoundedPanel();
        mosTxFld = new javax.swing.JTextField();
        daysPanel = new com.tajos.studio.graphics.RoundedPanel();
        daysTxFld = new javax.swing.JTextField();
        yearPanel = new com.tajos.studio.graphics.RoundedPanel();
        yearTxFld = new javax.swing.JTextField();
        bdayLblRootPanel1 = new javax.swing.JPanel();
        tajosJLabel4 = new com.tajos.studio.components.TajosJLabel();
        GenderRadioBtnRootPanel = new javax.swing.JPanel();
        femaleBtn = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        maleBtn = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JSeparator();
        customBtn = new javax.swing.JRadioButton();
        customPanel = new com.tajos.studio.graphics.RoundedPanel();
        customTxFld = new javax.swing.JTextField();
        termsAndPolicyRoot = new javax.swing.JPanel();
        saveBtn = new com.tajos.studio.graphics.RoundedPanel();
        saveLbl = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.OverlayLayout(getContentPane()));

        loadingPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadingPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72), 2));

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/loading.gif"))); // NOI18N
        jPanel3.add(jLabel2);

        tajosJLabel7.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tajosJLabel7.setText("Fetching Profile Information...");
        tajosJLabel7.setDefaultFont(12);
        jPanel3.add(tajosJLabel7);

        closeButton2.setText("closeButton2");
        closeButton2.setOnCloseButtonClickedListener(() -> {
            // on click
            setVisible(false);
            dispose();
        });

        javax.swing.GroupLayout loadingPanelLayout = new javax.swing.GroupLayout(loadingPanel);
        loadingPanel.setLayout(loadingPanelLayout);
        loadingPanelLayout.setHorizontalGroup(
            loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loadingPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        loadingPanelLayout.setVerticalGroup(
            loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(closeButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
        );

        getContentPane().add(loadingPanel);

        root.setBackground(new java.awt.Color(255, 255, 255));
        root.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72), 2));
        root.setVisible(false);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 10, 8));

        closeButton1.setText("closeButton1");
        closeButton1.setOnCloseButtonClickedListener(() -> {
            // on click
            setVisible(false);
            dispose();
        });
        jPanel2.add(closeButton1);

        jPanel1.setBackground(new java.awt.Color(247, 245, 251));

        tajosJLabel1.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel1.setText("My Profile");
        tajosJLabel1.setDefaultFont(12);
        tajosJLabel1.setMargins(20);

        imageViewer1.setImageResource(new javax.swing.ImageIcon(getClass().getResource("/res/workbk-design.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageViewer1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tajosJLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(tajosJLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(imageViewer1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        contentPanel.setBackground(new java.awt.Color(255, 255, 255));
        contentPanel.setLayout(null);

        RequiredLblRootPanel.setOpaque(false);
        RequiredLblRootPanel.setPreferredSize(new java.awt.Dimension(400, 10));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout22 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout22.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout22.setHorizontalGap(2);
        gravityLayout22.setLeftPadding(5);
        RequiredLblRootPanel.setLayout(gravityLayout22);

        tajosJLabel10.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel10.setText("Field having asterisk(*) is required.");
        tajosJLabel10.setDefaultFont(8);
        RequiredLblRootPanel.add(tajosJLabel10);

        contentPanel.add(RequiredLblRootPanel);
        RequiredLblRootPanel.setBounds(107, 48, 400, 10);

        NamesRootPanel.setOpaque(false);
        NamesRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout26 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout26.setAdjustableSize(false);
        gravityLayout26.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout26.setHorizontalGap(5);
        gravityLayout26.setLeftPadding(3);
        gravityLayout26.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout26.setTopPadding(2);
        NamesRootPanel.setLayout(gravityLayout26);

        firstNamePanel.setBackground(new java.awt.Color(255, 255, 255));
        firstNamePanel.setBorderColor(new java.awt.Color(153, 153, 153));
        firstNamePanel.setBorderEnabled(true);
        firstNamePanel.setPreferredSize(new java.awt.Dimension(400, 25));
        firstNamePanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout23 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout23.setAdjustableSize(true);
        gravityLayout23.setBottomPadding(2);
        gravityLayout23.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout23.setLeftPadding(8);
        gravityLayout23.setRightPadding(2);
        gravityLayout23.setTopPadding(2);
        firstNamePanel.setLayout(gravityLayout23);

        firstNameTxFld.setBackground(new java.awt.Color(255, 255, 255));
        firstNameTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        firstNameTxFld.setForeground(new java.awt.Color(153, 153, 153));
        firstNameTxFld.setText("Your Full Name*");
        firstNameTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        firstNameTxFld.setPreferredSize(new java.awt.Dimension(386, 22));
        firstNamePanel.add(firstNameTxFld);

        NamesRootPanel.add(firstNamePanel);

        contentPanel.add(NamesRootPanel);
        NamesRootPanel.setBounds(107, 63, 400, 30);

        ContactNumberRootPanel.setOpaque(false);
        ContactNumberRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout28 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout28.setAdjustableSize(false);
        gravityLayout28.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout28.setHorizontalGap(5);
        gravityLayout28.setLeftPadding(3);
        gravityLayout28.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout28.setTopPadding(2);
        ContactNumberRootPanel.setLayout(gravityLayout28);

        contactNumberPanel.setBackground(new java.awt.Color(255, 255, 255));
        contactNumberPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        contactNumberPanel.setBorderEnabled(true);
        contactNumberPanel.setPreferredSize(new java.awt.Dimension(150, 25));
        contactNumberPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout27 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout27.setAdjustableSize(true);
        gravityLayout27.setBottomPadding(2);
        gravityLayout27.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout27.setLeftPadding(8);
        gravityLayout27.setRightPadding(2);
        gravityLayout27.setTopPadding(2);
        contactNumberPanel.setLayout(gravityLayout27);

        contactNumberTxFld.setBackground(new java.awt.Color(255, 255, 255));
        contactNumberTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        contactNumberTxFld.setForeground(new java.awt.Color(153, 153, 153));
        contactNumberTxFld.setText("Contact Number (Optional)");
        contactNumberTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        contactNumberTxFld.setPreferredSize(new java.awt.Dimension(387, 22));
        contactNumberPanel.add(contactNumberTxFld);

        ContactNumberRootPanel.add(contactNumberPanel);

        contentPanel.add(ContactNumberRootPanel);
        ContactNumberRootPanel.setBounds(107, 98, 400, 30);

        SchoolNameRootPanel.setOpaque(false);
        SchoolNameRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout30 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout30.setAdjustableSize(false);
        gravityLayout30.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout30.setHorizontalGap(5);
        gravityLayout30.setLeftPadding(3);
        gravityLayout30.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout30.setTopPadding(2);
        SchoolNameRootPanel.setLayout(gravityLayout30);

        schoolNamePanel.setBackground(new java.awt.Color(255, 255, 255));
        schoolNamePanel.setBorderColor(new java.awt.Color(153, 153, 153));
        schoolNamePanel.setBorderEnabled(true);
        schoolNamePanel.setPreferredSize(new java.awt.Dimension(150, 25));
        schoolNamePanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout29 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout29.setAdjustableSize(true);
        gravityLayout29.setBottomPadding(2);
        gravityLayout29.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout29.setLeftPadding(8);
        gravityLayout29.setRightPadding(2);
        gravityLayout29.setTopPadding(2);
        schoolNamePanel.setLayout(gravityLayout29);

        schoolNameTxFld.setBackground(new java.awt.Color(255, 255, 255));
        schoolNameTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        schoolNameTxFld.setForeground(new java.awt.Color(153, 153, 153));
        schoolNameTxFld.setText("School Name*");
        schoolNameTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        schoolNameTxFld.setPreferredSize(new java.awt.Dimension(387, 22));
        schoolNamePanel.add(schoolNameTxFld);

        SchoolNameRootPanel.add(schoolNamePanel);

        contentPanel.add(SchoolNameRootPanel);
        SchoolNameRootPanel.setBounds(107, 133, 400, 30);

        space1.setOpaque(false);
        space1.setPreferredSize(new java.awt.Dimension(400, 10));
        space1.setLayout(null);
        contentPanel.add(space1);
        space1.setBounds(107, 168, 400, 10);

        bdayLblRootPanel.setOpaque(false);
        bdayLblRootPanel.setPreferredSize(new java.awt.Dimension(400, 20));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout31 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout31.setAdjustableSize(false);
        gravityLayout31.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout31.setHorizontalGap(5);
        gravityLayout31.setLeftPadding(3);
        gravityLayout31.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout31.setTopPadding(2);
        bdayLblRootPanel.setLayout(gravityLayout31);

        tajosJLabel3.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel3.setText("Birthday (Optional)");
        tajosJLabel3.setDefaultFont(10);
        bdayLblRootPanel.add(tajosJLabel3);

        contentPanel.add(bdayLblRootPanel);
        bdayLblRootPanel.setBounds(107, 183, 400, 20);

        bdayRootPanel.setOpaque(false);
        bdayRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        bdayRootPanel.setLayout(null);

        mosPanel.setBackground(new java.awt.Color(255, 255, 255));
        mosPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        mosPanel.setBorderEnabled(true);
        mosPanel.setPreferredSize(new java.awt.Dimension(120, 25));
        mosPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout32 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout32.setAdjustableSize(true);
        gravityLayout32.setBottomPadding(2);
        gravityLayout32.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout32.setLeftPadding(8);
        gravityLayout32.setRightPadding(2);
        gravityLayout32.setTopPadding(2);
        mosPanel.setLayout(gravityLayout32);

        mosTxFld.setBackground(new java.awt.Color(255, 255, 255));
        mosTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        mosTxFld.setForeground(new java.awt.Color(153, 153, 153));
        mosTxFld.setText("mm (month)");
        mosTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mosTxFld.setPreferredSize(new java.awt.Dimension(119, 22));
        mosPanel.add(mosTxFld);

        bdayRootPanel.add(mosPanel);
        mosPanel.setBounds(3, 2, 120, 25);

        daysPanel.setBackground(new java.awt.Color(255, 255, 255));
        daysPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        daysPanel.setBorderEnabled(true);
        daysPanel.setPreferredSize(new java.awt.Dimension(150, 25));
        daysPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout33 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout33.setAdjustableSize(true);
        gravityLayout33.setBottomPadding(2);
        gravityLayout33.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout33.setLeftPadding(8);
        gravityLayout33.setRightPadding(2);
        gravityLayout33.setTopPadding(2);
        daysPanel.setLayout(gravityLayout33);

        daysTxFld.setBackground(new java.awt.Color(255, 255, 255));
        daysTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        daysTxFld.setForeground(new java.awt.Color(153, 153, 153));
        daysTxFld.setText("dd (day)");
        daysTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        daysTxFld.setPreferredSize(new java.awt.Dimension(119, 22));
        daysPanel.add(daysTxFld);

        bdayRootPanel.add(daysPanel);
        daysPanel.setBounds(137, 2, 150, 25);

        yearPanel.setBackground(new java.awt.Color(255, 255, 255));
        yearPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        yearPanel.setBorderEnabled(true);
        yearPanel.setPreferredSize(new java.awt.Dimension(40, 25));
        yearPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout34 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout34.setAdjustableSize(true);
        gravityLayout34.setBottomPadding(2);
        gravityLayout34.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout34.setLeftPadding(8);
        gravityLayout34.setRightPadding(2);
        gravityLayout34.setTopPadding(2);
        yearPanel.setLayout(gravityLayout34);

        yearTxFld.setBackground(new java.awt.Color(255, 255, 255));
        yearTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        yearTxFld.setForeground(new java.awt.Color(153, 153, 153));
        yearTxFld.setText("yyyy (year)");
        yearTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        yearTxFld.setPreferredSize(new java.awt.Dimension(119, 22));
        yearPanel.add(yearTxFld);

        bdayRootPanel.add(yearPanel);
        yearPanel.setBounds(271, 2, 40, 25);

        contentPanel.add(bdayRootPanel);
        bdayRootPanel.setBounds(107, 208, 400, 30);

        bdayLblRootPanel1.setOpaque(false);
        bdayLblRootPanel1.setPreferredSize(new java.awt.Dimension(400, 20));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout36 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout36.setAdjustableSize(false);
        gravityLayout36.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout36.setHorizontalGap(5);
        gravityLayout36.setLeftPadding(3);
        gravityLayout36.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout36.setTopPadding(2);
        bdayLblRootPanel1.setLayout(gravityLayout36);

        tajosJLabel4.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel4.setText("Gender*");
        tajosJLabel4.setDefaultFont(10);
        bdayLblRootPanel1.add(tajosJLabel4);

        contentPanel.add(bdayLblRootPanel1);
        bdayLblRootPanel1.setBounds(107, 243, 400, 20);

        GenderRadioBtnRootPanel.setOpaque(false);
        GenderRadioBtnRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout37 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout37.setHorizontalGap(30);
        GenderRadioBtnRootPanel.setLayout(gravityLayout37);

        femaleBtn.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(femaleBtn);
        femaleBtn.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        femaleBtn.setForeground(new java.awt.Color(22, 28, 72));
        femaleBtn.setSelected(true);
        femaleBtn.setText("Female  ");
        femaleBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        femaleBtn.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        femaleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                femaleBtnActionPerformed(evt);
            }
        });
        GenderRadioBtnRootPanel.add(femaleBtn);

        jSeparator1.setBackground(new java.awt.Color(215, 215, 215));
        jSeparator1.setForeground(new java.awt.Color(215, 215, 215));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 20));
        GenderRadioBtnRootPanel.add(jSeparator1);

        maleBtn.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(maleBtn);
        maleBtn.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        maleBtn.setForeground(new java.awt.Color(22, 28, 72));
        maleBtn.setText("Male   ");
        maleBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        maleBtn.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        maleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maleBtnActionPerformed(evt);
            }
        });
        GenderRadioBtnRootPanel.add(maleBtn);

        jSeparator2.setBackground(new java.awt.Color(215, 215, 215));
        jSeparator2.setForeground(new java.awt.Color(215, 215, 215));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(2, 20));
        GenderRadioBtnRootPanel.add(jSeparator2);

        customBtn.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(customBtn);
        customBtn.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        customBtn.setForeground(new java.awt.Color(22, 28, 72));
        customBtn.setText("Custom  ");
        customBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        customBtn.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        customBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customBtnActionPerformed(evt);
            }
        });
        GenderRadioBtnRootPanel.add(customBtn);

        contentPanel.add(GenderRadioBtnRootPanel);
        GenderRadioBtnRootPanel.setBounds(107, 268, 400, 30);

        customPanel.setVisible(false);
        customPanel.setBackground(new java.awt.Color(255, 255, 255));
        customPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        customPanel.setBorderEnabled(true);
        customPanel.setPreferredSize(new java.awt.Dimension(400, 25));
        customPanel.setRadius(10);
        customPanel.setLayout(null);

        customTxFld.setBackground(new java.awt.Color(255, 255, 255));
        customTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        customTxFld.setForeground(new java.awt.Color(153, 153, 153));
        customTxFld.setText("Enter your Gender*");
        customTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        customTxFld.setPreferredSize(new java.awt.Dimension(387, 25));
        customPanel.add(customTxFld);
        customTxFld.setBounds(8, 2, 387, 20);

        contentPanel.add(customPanel);
        customPanel.setBounds(109, 303, 400, 25);

        termsAndPolicyRoot.setOpaque(false);
        termsAndPolicyRoot.setPreferredSize(new java.awt.Dimension(400, 25));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout41 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout41.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        termsAndPolicyRoot.setLayout(gravityLayout41);
        contentPanel.add(termsAndPolicyRoot);
        termsAndPolicyRoot.setBounds(107, 312, 400, 25);

        saveBtn.setBackground(new java.awt.Color(76, 96, 204));
        saveBtn.setPreferredSize(new java.awt.Dimension(200, 30));
        saveBtn.setRadius(10);
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtnMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveBtnMousePressed(evt);
            }
        });
        saveBtn.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        saveLbl.setForeground(new java.awt.Color(255, 255, 255));
        saveLbl.setText("Save");
        saveLbl.setDefaultFont(14);
        saveBtn.add(saveLbl);

        contentPanel.add(saveBtn);
        saveBtn.setBounds(207, 342, 200, 30);

        javax.swing.GroupLayout rootLayout = new javax.swing.GroupLayout(root);
        root.setLayout(rootLayout);
        rootLayout.setHorizontalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rootLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contentPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 659, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        rootLayout.setVerticalGroup(
            rootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(root);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void femaleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_femaleBtnActionPerformed
        if (customPanel.isVisible()) {
            customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 0));
            customPanel.setVisible(false);

            updateUI();
        }
    }//GEN-LAST:event_femaleBtnActionPerformed

    private void maleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maleBtnActionPerformed
        if (customPanel.isVisible()) {
            customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 0));
            customPanel.setVisible(false);

            updateUI();
        }
    }//GEN-LAST:event_maleBtnActionPerformed

    private void customBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customBtnActionPerformed
        if (customBtn.isSelected()) {
            customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 22));
            customPanel.setVisible(true);
        }
    }//GEN-LAST:event_customBtnActionPerformed

    private void saveBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseEntered
        saveBtn.setBackground(GradeUtils.Colors.hoverBtnColor);
        updateUI();
    }//GEN-LAST:event_saveBtnMouseEntered

    private void saveBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseExited
        saveBtn.setBackground(GradeUtils.Colors.buttonColor);
        updateUI();
    }//GEN-LAST:event_saveBtnMouseExited

    private void saveBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMousePressed
        boolean error = false;
        for (TxFieldHandler handler : constrainstsGrp) {
            if (handler.isOnError()) {
                error = true;
            }
        }

        if (error) {
            errPopupWindow.show(saveBtn, 0, saveBtn.getHeight());
            updateUI();
            return;
        }

        int result = JOptionPane.showConfirmDialog(new Frame(),
            "Are you sure your information is correct?",
            "Confirmation Dialog", JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.OK_OPTION)
            return;
        
        // region: map the user's information and sent it to the server
        Map<String, Object> userInfos = new HashMap<>();
        String name = firstNameTxFldHandler.getAbsoluteText();
        // bday
        String bday = "";
        if (!"".equals(mosTxFldHandler.getAbsoluteText()) &&
            !"".equals(daysTxFldHandler.getAbsoluteText()) &&
            !"".equals(yearTxFldHandler.getAbsoluteText()))
                bday = mosTxFldHandler.getAbsoluteText() + "/" + daysTxFldHandler.getAbsoluteText()+ "/" + yearTxFldHandler.getAbsoluteText();
        // gender
        String[] gender = {""};

        Enumeration<AbstractButton> enumeration = buttonGroup1.getElements();
        enumeration.asIterator().forEachRemaining(b -> {
            String s = GradeUtils.removeStringSpaces(b.getText());
            if (b.isSelected() && s.equals("Custom")) {
                gender[0] = customTxFldHandler.getAbsoluteText();
            } else if (b.isSelected()) {
                gender[0] = GradeUtils.removeStringSpaces(b.getText());
            }
        });

        userInfos.put("name", name);
        userInfos.put("bday", bday);
        userInfos.put("gender", gender[0]);
        userInfos.put("contact", contactNumHandler.getAbsoluteText());
        userInfos.put("school_name", schoolNameTxFldHandler.getAbsoluteText());
        DBManager.getInstance().updateUserInfo(new JSONObject(userInfos).toString());
        // region end
    }//GEN-LAST:event_saveBtnMousePressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Graede My Profile");
    }//GEN-LAST:event_formWindowActivated

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ContactNumberRootPanel;
    private javax.swing.JPanel GenderRadioBtnRootPanel;
    private javax.swing.JPanel NamesRootPanel;
    private javax.swing.JPanel RequiredLblRootPanel;
    private javax.swing.JPanel SchoolNameRootPanel;
    private javax.swing.JPanel bdayLblRootPanel;
    private javax.swing.JPanel bdayLblRootPanel1;
    private javax.swing.JPanel bdayRootPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.tajos.studio.components.CloseButton closeButton1;
    private com.tajos.studio.components.CloseButton closeButton2;
    private com.tajos.studio.graphics.RoundedPanel contactNumberPanel;
    private javax.swing.JTextField contactNumberTxFld;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JRadioButton customBtn;
    private com.tajos.studio.graphics.RoundedPanel customPanel;
    private javax.swing.JTextField customTxFld;
    private com.tajos.studio.graphics.RoundedPanel daysPanel;
    private javax.swing.JTextField daysTxFld;
    private javax.swing.JRadioButton femaleBtn;
    private com.tajos.studio.graphics.RoundedPanel firstNamePanel;
    private javax.swing.JTextField firstNameTxFld;
    private com.tajos.studio.components.ImageViewer imageViewer1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel loadingPanel;
    private javax.swing.JRadioButton maleBtn;
    private com.tajos.studio.graphics.RoundedPanel mosPanel;
    private javax.swing.JTextField mosTxFld;
    private javax.swing.JPanel root;
    private com.tajos.studio.graphics.RoundedPanel saveBtn;
    private com.tajos.studio.components.TajosJLabel saveLbl;
    private com.tajos.studio.graphics.RoundedPanel schoolNamePanel;
    private javax.swing.JTextField schoolNameTxFld;
    private javax.swing.JPanel space1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel10;
    private com.tajos.studio.components.TajosJLabel tajosJLabel3;
    private com.tajos.studio.components.TajosJLabel tajosJLabel4;
    private com.tajos.studio.components.TajosJLabel tajosJLabel7;
    private javax.swing.JPanel termsAndPolicyRoot;
    private com.tajos.studio.graphics.RoundedPanel yearPanel;
    private javax.swing.JTextField yearTxFld;
    // End of variables declaration//GEN-END:variables

    private void updateUI() {
        revalidate();
        repaint();
    }
}
