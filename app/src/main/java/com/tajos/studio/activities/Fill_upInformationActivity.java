package com.tajos.studio.activities;

import com.tajos.studio.net.DBManager;
import com.tajos.studio.graphics.RoundedPanel;
import com.tajos.studio.interfaces.KeyBinds;
import com.tajos.studio.util.GradeUtils;
import java.awt.Color;
import java.awt.Cursor;
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
import org.json.JSONObject;

/**
 *
 * @author Rene Tajos Jr.
 */
public class Fill_upInformationActivity extends javax.swing.JFrame {

    private TxFieldHandler firstNameTxFldHandler;
    private TxFieldHandler lastNameTxFldHandler;
    private TxFieldHandler suffixTxFldHandler;
    private TxFieldHandler schoolNameTxFldHandler;
    private TxFieldHandler customTxFldHandler;
    private TxFieldHandler contactNumHandler;
    private TxFieldHandler mosTxFldHandler;
    private TxFieldHandler daysTxFldHandler;
    private TxFieldHandler yearTxFldHandler;
    
    private final List<TxFieldHandler> constrainstsGrp = new ArrayList<>();
    private ErrorPopupWindow errPopupWindow;

    private static Fill_upInformationActivity instance;
    public static Fill_upInformationActivity getInstance() {
        return instance;
    }

    /**
     * Creates new form Fill_upInformationActivity
     */
    public Fill_upInformationActivity() {
        initComponents();
        init();
        instance = this;
        GradeUtils.centerFrame(this);
    }
    
    private void init() {
        errPopupWindow = new ErrorPopupWindow("Fix errors first!");
        
        firstNameTxFldHandler = new TxFieldHandler(firstNameTxFld).handle();
        lastNameTxFldHandler = new TxFieldHandler(lastNameTxFld).handle();
        suffixTxFldHandler = new TxFieldHandler(suffixTxFld).handle();
        suffixTxFldHandler.setPriority(TxFieldHandler.PriorityType.OPTIONAL);
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
        constrainstsGrp.add(lastNameTxFldHandler);
        constrainstsGrp.add(suffixTxFldHandler);
        constrainstsGrp.add(schoolNameTxFldHandler);
        constrainstsGrp.add(customTxFldHandler);
        
        mosTxFld.requestFocusInWindow();
        mosTxFld.setCaretPosition(0);
        
        firstNameTxFld.requestFocusInWindow();
        firstNameTxFld.setCaretPosition(0);
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
            Fill_upInformationActivity.this.revalidate();
            Fill_upInformationActivity.this.repaint();
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
    
    private void updateUI() {
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        toolbar = new javax.swing.JPanel();
        minimizedButton1 = new com.tajos.studio.components.MinimizedButton();
        closeButton1 = new com.tajos.studio.components.CloseButton();
        jPanel1 = new javax.swing.JPanel();
        tajosJLabel1 = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel2 = new com.tajos.studio.components.TajosJLabel();
        contentPanel = new javax.swing.JPanel();
        RequiredLblRootPanel = new javax.swing.JPanel();
        tajosJLabel10 = new com.tajos.studio.components.TajosJLabel();
        NamesRootPanel = new javax.swing.JPanel();
        firstNamePanel = new com.tajos.studio.graphics.RoundedPanel();
        firstNameTxFld = new javax.swing.JTextField();
        lastNamePanel = new com.tajos.studio.graphics.RoundedPanel();
        lastNameTxFld = new javax.swing.JTextField();
        suffixPanel = new com.tajos.studio.graphics.RoundedPanel();
        suffixTxFld = new javax.swing.JTextField();
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
        jPanel2 = new javax.swing.JPanel();
        tajosJLabel6 = new com.tajos.studio.components.TajosJLabel();
        termsLbl = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel7 = new com.tajos.studio.components.TajosJLabel();
        jPanel3 = new javax.swing.JPanel();
        tajosJLabel8 = new com.tajos.studio.components.TajosJLabel();
        dataPolicyLbl = new com.tajos.studio.components.TajosJLabel();
        tajosJLabel9 = new com.tajos.studio.components.TajosJLabel();
        doneBtnPanel = new com.tajos.studio.graphics.RoundedPanel();
        tajosJLabel5 = new com.tajos.studio.components.TajosJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(22, 28, 72)));

        toolbar.setBackground(new java.awt.Color(247, 245, 251));
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
        toolbar.add(minimizedButton1);

        closeButton1.setText("closeButton1");
        toolbar.add(closeButton1);

        jPanel1.setBackground(new java.awt.Color(247, 245, 251));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout1 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout1.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout1.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        gravityLayout1.setLeftPadding(20);
        jPanel1.setLayout(gravityLayout1);

        tajosJLabel1.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel1.setText("Tell us about you");
        tajosJLabel1.setBoldDefaultFont(14);
        jPanel1.add(tajosJLabel1);

        tajosJLabel2.setForeground(new java.awt.Color(102, 102, 102));
        tajosJLabel2.setText("Fill-up the information below.");
        tajosJLabel2.setDefaultFont(11);
        jPanel1.add(tajosJLabel2);

        contentPanel.setBackground(new java.awt.Color(255, 255, 255));
        contentPanel.setLayout(null);

        RequiredLblRootPanel.setOpaque(false);
        RequiredLblRootPanel.setPreferredSize(new java.awt.Dimension(400, 10));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout23 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout23.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout23.setHorizontalGap(2);
        gravityLayout23.setLeftPadding(5);
        RequiredLblRootPanel.setLayout(gravityLayout23);

        tajosJLabel10.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel10.setText("Field having asterisk(*) is required.");
        tajosJLabel10.setDefaultFont(8);
        RequiredLblRootPanel.add(tajosJLabel10);

        contentPanel.add(RequiredLblRootPanel);
        RequiredLblRootPanel.setBounds(179, 40, 400, 10);

        NamesRootPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        NamesRootPanel.setOpaque(false);
        NamesRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout3 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout3.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout3.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout3.setTopPadding(2);
        gravityLayout3.setAdjustableSize(false);
        gravityLayout3.setLeftPadding(3);
        gravityLayout3.setHorizontalGap(5);
        NamesRootPanel.setLayout(gravityLayout3);

        firstNamePanel.setBackground(new java.awt.Color(255, 255, 255));
        firstNamePanel.setBorderColor(new java.awt.Color(153, 153, 153));
        firstNamePanel.setBorderEnabled(true);
        firstNamePanel.setPreferredSize(new java.awt.Dimension(150, 25));
        firstNamePanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout4 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout4.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout4.setAdjustableSize(true);
        gravityLayout4.setLeftPadding(8);
        gravityLayout4.setTopPadding(2);
        gravityLayout4.setBottomPadding(2);
        gravityLayout4.setRightPadding(2);
        firstNamePanel.setLayout(gravityLayout4);

        firstNameTxFld.setBackground(new java.awt.Color(255, 255, 255));
        firstNameTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        firstNameTxFld.setForeground(new java.awt.Color(153, 153, 153));
        firstNameTxFld.setText("First Name*");
        firstNameTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        firstNameTxFld.setPreferredSize(new java.awt.Dimension(144, 22));
        firstNamePanel.add(firstNameTxFld);

        NamesRootPanel.add(firstNamePanel);

        lastNamePanel.setBackground(new java.awt.Color(255, 255, 255));
        lastNamePanel.setBorderColor(new java.awt.Color(153, 153, 153));
        lastNamePanel.setBorderEnabled(true);
        lastNamePanel.setPreferredSize(new java.awt.Dimension(150, 25));
        lastNamePanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout5 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout5.setAdjustableSize(true);
        gravityLayout5.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout5.setLeftPadding(8);
        gravityLayout5.setRightPadding(2);
        gravityLayout5.setTopPadding(2);
        gravityLayout5.setBottomPadding(2);
        lastNamePanel.setLayout(gravityLayout5);

        lastNameTxFld.setBackground(new java.awt.Color(255, 255, 255));
        lastNameTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        lastNameTxFld.setForeground(new java.awt.Color(153, 153, 153));
        lastNameTxFld.setText("Last Name*");
        lastNameTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        lastNameTxFld.setPreferredSize(new java.awt.Dimension(144, 22));
        lastNamePanel.add(lastNameTxFld);

        NamesRootPanel.add(lastNamePanel);

        suffixPanel.setBackground(new java.awt.Color(255, 255, 255));
        suffixPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        suffixPanel.setBorderEnabled(true);
        suffixPanel.setPreferredSize(new java.awt.Dimension(40, 25));
        suffixPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout6 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout6.setAdjustableSize(true);
        gravityLayout6.setBottomPadding(2);
        gravityLayout6.setLeftPadding(8);
        gravityLayout6.setRightPadding(2);
        gravityLayout6.setTopPadding(2);
        gravityLayout6.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        suffixPanel.setLayout(gravityLayout6);

        suffixTxFld.setBackground(new java.awt.Color(255, 255, 255));
        suffixTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        suffixTxFld.setForeground(new java.awt.Color(153, 153, 153));
        suffixTxFld.setText("Jr./Sr./II/III");
        suffixTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        suffixTxFld.setPreferredSize(new java.awt.Dimension(69, 22));
        suffixPanel.add(suffixTxFld);

        NamesRootPanel.add(suffixPanel);

        contentPanel.add(NamesRootPanel);
        NamesRootPanel.setBounds(179, 55, 400, 30);

        ContactNumberRootPanel.setOpaque(false);
        ContactNumberRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout10 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout10.setAdjustableSize(false);
        gravityLayout10.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout10.setHorizontalGap(5);
        gravityLayout10.setLeftPadding(3);
        gravityLayout10.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout10.setTopPadding(2);
        ContactNumberRootPanel.setLayout(gravityLayout10);

        contactNumberPanel.setBackground(new java.awt.Color(255, 255, 255));
        contactNumberPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        contactNumberPanel.setBorderEnabled(true);
        contactNumberPanel.setPreferredSize(new java.awt.Dimension(150, 25));
        contactNumberPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout7 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout7.setAdjustableSize(true);
        gravityLayout7.setBottomPadding(2);
        gravityLayout7.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout7.setLeftPadding(8);
        gravityLayout7.setRightPadding(2);
        gravityLayout7.setTopPadding(2);
        contactNumberPanel.setLayout(gravityLayout7);

        contactNumberTxFld.setBackground(new java.awt.Color(255, 255, 255));
        contactNumberTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        contactNumberTxFld.setForeground(new java.awt.Color(153, 153, 153));
        contactNumberTxFld.setText("Contact Number (Optional)");
        contactNumberTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        contactNumberTxFld.setPreferredSize(new java.awt.Dimension(387, 22));
        contactNumberPanel.add(contactNumberTxFld);

        ContactNumberRootPanel.add(contactNumberPanel);

        contentPanel.add(ContactNumberRootPanel);
        ContactNumberRootPanel.setBounds(179, 90, 400, 30);

        SchoolNameRootPanel.setOpaque(false);
        SchoolNameRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout12 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout12.setAdjustableSize(false);
        gravityLayout12.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout12.setHorizontalGap(5);
        gravityLayout12.setLeftPadding(3);
        gravityLayout12.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout12.setTopPadding(2);
        SchoolNameRootPanel.setLayout(gravityLayout12);

        schoolNamePanel.setBackground(new java.awt.Color(255, 255, 255));
        schoolNamePanel.setBorderColor(new java.awt.Color(153, 153, 153));
        schoolNamePanel.setBorderEnabled(true);
        schoolNamePanel.setPreferredSize(new java.awt.Dimension(150, 25));
        schoolNamePanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout11 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout11.setAdjustableSize(true);
        gravityLayout11.setBottomPadding(2);
        gravityLayout11.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout11.setLeftPadding(8);
        gravityLayout11.setRightPadding(2);
        gravityLayout11.setTopPadding(2);
        schoolNamePanel.setLayout(gravityLayout11);

        schoolNameTxFld.setBackground(new java.awt.Color(255, 255, 255));
        schoolNameTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        schoolNameTxFld.setForeground(new java.awt.Color(153, 153, 153));
        schoolNameTxFld.setText("School Name*");
        schoolNameTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        schoolNameTxFld.setPreferredSize(new java.awt.Dimension(387, 22));
        schoolNamePanel.add(schoolNameTxFld);

        SchoolNameRootPanel.add(schoolNamePanel);

        contentPanel.add(SchoolNameRootPanel);
        SchoolNameRootPanel.setBounds(179, 125, 400, 30);

        space1.setOpaque(false);
        space1.setPreferredSize(new java.awt.Dimension(400, 10));
        space1.setLayout(null);
        contentPanel.add(space1);
        space1.setBounds(179, 160, 400, 10);

        bdayLblRootPanel.setOpaque(false);
        bdayLblRootPanel.setPreferredSize(new java.awt.Dimension(400, 20));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout15 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout15.setAdjustableSize(false);
        gravityLayout15.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout15.setHorizontalGap(5);
        gravityLayout15.setLeftPadding(3);
        gravityLayout15.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout15.setTopPadding(2);
        bdayLblRootPanel.setLayout(gravityLayout15);

        tajosJLabel3.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel3.setText("Birthday (Optional)");
        tajosJLabel3.setDefaultFont(10);
        bdayLblRootPanel.add(tajosJLabel3);

        contentPanel.add(bdayLblRootPanel);
        bdayLblRootPanel.setBounds(179, 175, 400, 20);

        bdayRootPanel.setOpaque(false);
        bdayRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        bdayRootPanel.setLayout(null);

        mosPanel.setBackground(new java.awt.Color(255, 255, 255));
        mosPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        mosPanel.setBorderEnabled(true);
        mosPanel.setPreferredSize(new java.awt.Dimension(120, 25));
        mosPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout13 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout13.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout13.setLeftPadding(5);
        gravityLayout13.setRightPadding(2);
        gravityLayout13.setTopPadding(2);
        gravityLayout13.setBottomPadding(2);
        gravityLayout13.setAdjustableSize(false);
        mosPanel.setLayout(gravityLayout13);

        mosTxFld.setBackground(new java.awt.Color(255, 255, 255));
        mosTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        mosTxFld.setForeground(new java.awt.Color(153, 153, 153));
        mosTxFld.setText("mm (month)");
        mosTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mosTxFld.setPreferredSize(new java.awt.Dimension(100, 22));
        mosPanel.add(mosTxFld);

        bdayRootPanel.add(mosPanel);
        mosPanel.setBounds(6, 0, 120, 25);

        daysPanel.setBackground(new java.awt.Color(255, 255, 255));
        daysPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        daysPanel.setBorderEnabled(true);
        daysPanel.setPreferredSize(new java.awt.Dimension(150, 25));
        daysPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout18 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout18.setAdjustableSize(true);
        gravityLayout18.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout18.setBottomPadding(2);
        gravityLayout18.setLeftPadding(8);
        gravityLayout18.setRightPadding(2);
        gravityLayout18.setTopPadding(2);
        daysPanel.setLayout(gravityLayout18);

        daysTxFld.setBackground(new java.awt.Color(255, 255, 255));
        daysTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        daysTxFld.setForeground(new java.awt.Color(153, 153, 153));
        daysTxFld.setText("dd (day)");
        daysTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        daysTxFld.setPreferredSize(new java.awt.Dimension(119, 22));
        daysPanel.add(daysTxFld);

        bdayRootPanel.add(daysPanel);
        daysPanel.setBounds(131, 0, 150, 25);

        yearPanel.setBackground(new java.awt.Color(255, 255, 255));
        yearPanel.setBorderColor(new java.awt.Color(153, 153, 153));
        yearPanel.setBorderEnabled(true);
        yearPanel.setPreferredSize(new java.awt.Dimension(40, 25));
        yearPanel.setRadius(10);
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout19 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout19.setAdjustableSize(true);
        gravityLayout19.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout19.setBottomPadding(2);
        gravityLayout19.setLeftPadding(8);
        gravityLayout19.setRightPadding(2);
        gravityLayout19.setTopPadding(2);
        yearPanel.setLayout(gravityLayout19);

        yearTxFld.setBackground(new java.awt.Color(255, 255, 255));
        yearTxFld.setFont(new java.awt.Font("Nirmala UI Semilight", 0, 12)); // NOI18N
        yearTxFld.setForeground(new java.awt.Color(153, 153, 153));
        yearTxFld.setText("yyyy (year)");
        yearTxFld.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        yearTxFld.setPreferredSize(new java.awt.Dimension(119, 22));
        yearPanel.add(yearTxFld);

        bdayRootPanel.add(yearPanel);
        yearPanel.setBounds(265, 0, 40, 25);

        contentPanel.add(bdayRootPanel);
        bdayRootPanel.setBounds(179, 200, 400, 30);

        bdayLblRootPanel1.setOpaque(false);
        bdayLblRootPanel1.setPreferredSize(new java.awt.Dimension(400, 20));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout22 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout22.setAdjustableSize(false);
        gravityLayout22.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout22.setHorizontalGap(5);
        gravityLayout22.setLeftPadding(3);
        gravityLayout22.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.HORIZONTAL);
        gravityLayout22.setTopPadding(2);
        bdayLblRootPanel1.setLayout(gravityLayout22);

        tajosJLabel4.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel4.setText("Gender*");
        tajosJLabel4.setDefaultFont(10);
        bdayLblRootPanel1.add(tajosJLabel4);

        contentPanel.add(bdayLblRootPanel1);
        bdayLblRootPanel1.setBounds(179, 235, 400, 20);

        GenderRadioBtnRootPanel.setOpaque(false);
        GenderRadioBtnRootPanel.setPreferredSize(new java.awt.Dimension(400, 30));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout8 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout8.setHorizontalGap(30);
        GenderRadioBtnRootPanel.setLayout(gravityLayout8);

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
        GenderRadioBtnRootPanel.setBounds(179, 260, 400, 30);

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
        customPanel.setBounds(181, 295, 400, 25);

        termsAndPolicyRoot.setOpaque(false);
        termsAndPolicyRoot.setPreferredSize(new java.awt.Dimension(400, 25));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout14 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout14.setOrientation(com.tajos.studio.layoutmanagers.GravityLayout.Orientation.VERTICAL);
        termsAndPolicyRoot.setLayout(gravityLayout14);

        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 10));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout9 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout9.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout9.setLeftPadding(5);
        gravityLayout9.setHorizontalGap(2);
        jPanel2.setLayout(gravityLayout9);

        tajosJLabel6.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel6.setText("By clicking Done, you agree to our");
        tajosJLabel6.setDefaultFont(8);
        jPanel2.add(tajosJLabel6);

        termsLbl.setForeground(new java.awt.Color(22, 28, 72));
        termsLbl.setText("<html><body><p><u> Terms.</u></p></body></html>");
        termsLbl.setDefaultFont(8);
        termsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                termsLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                termsLblMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                termsLblMousePressed(evt);
            }
        });
        jPanel2.add(termsLbl);

        tajosJLabel7.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel7.setText(" You learn how we collect, use and share your data ");
        tajosJLabel7.setDefaultFont(8);
        jPanel2.add(tajosJLabel7);

        termsAndPolicyRoot.add(jPanel2);

        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(400, 10));
        com.tajos.studio.layoutmanagers.GravityLayout gravityLayout16 = new com.tajos.studio.layoutmanagers.GravityLayout();
        gravityLayout16.setGravity(com.tajos.studio.layoutmanagers.GravityLayout.Gravity.LEFT);
        gravityLayout16.setHorizontalGap(2);
        gravityLayout16.setLeftPadding(5);
        jPanel3.setLayout(gravityLayout16);

        tajosJLabel8.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel8.setText("in our");
        tajosJLabel8.setDefaultFont(8);
        jPanel3.add(tajosJLabel8);

        dataPolicyLbl.setForeground(new java.awt.Color(22, 28, 72));
        dataPolicyLbl.setText("<html><body><p><u>Data Policy.</u></p></body></html>");
        dataPolicyLbl.setDefaultFont(8);
        dataPolicyLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dataPolicyLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dataPolicyLblMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dataPolicyLblMousePressed(evt);
            }
        });
        jPanel3.add(dataPolicyLbl);

        tajosJLabel9.setForeground(new java.awt.Color(22, 28, 72));
        tajosJLabel9.setText(" You may receive an email from us and can opt out any time.");
        tajosJLabel9.setDefaultFont(8);
        jPanel3.add(tajosJLabel9);

        termsAndPolicyRoot.add(jPanel3);

        contentPanel.add(termsAndPolicyRoot);
        termsAndPolicyRoot.setBounds(179, 332, 400, 25);

        doneBtnPanel.setBackground(new java.awt.Color(76, 96, 204));
        doneBtnPanel.setPreferredSize(new java.awt.Dimension(200, 30));
        doneBtnPanel.setRadius(10);
        doneBtnPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                doneBtnPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                doneBtnPanelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                doneBtnPanelMousePressed(evt);
            }
        });
        doneBtnPanel.setLayout(new com.tajos.studio.layoutmanagers.GravityLayout());

        tajosJLabel5.setForeground(new java.awt.Color(255, 255, 255));
        tajosJLabel5.setText("Done");
        tajosJLabel5.setDefaultFont(14);
        doneBtnPanel.add(tajosJLabel5);

        contentPanel.add(doneBtnPanel);
        doneBtnPanel.setBounds(279, 359, 200, 30);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void toolbarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseDragged
        int x = evt.getXOnScreen() - mX;
        int y = evt.getYOnScreen() - mY;
        setLocation(x, y);
    }//GEN-LAST:event_toolbarMouseDragged
    private int mX, mY;
    private void toolbarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMousePressed
        mX = evt.getX(); mY = evt.getY();
    }//GEN-LAST:event_toolbarMousePressed

    private void customBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customBtnActionPerformed
        if (customBtn.isSelected()) {
            customPanel.setVisible(true);
            updateUI();
        }
    }//GEN-LAST:event_customBtnActionPerformed
    private final Color toolbarMouseEnteredColor = new Color(230,226,238);
    private final Color toolbarMouseExitedColor = new Color(247,245,251);
    private void toolbarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseEntered
        toolbar.setBackground(toolbarMouseEnteredColor);
    }//GEN-LAST:event_toolbarMouseEntered

    private void toolbarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolbarMouseExited
        toolbar.setBackground(toolbarMouseExitedColor);
    }//GEN-LAST:event_toolbarMouseExited

    private void maleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maleBtnActionPerformed
        if (customPanel.isVisible()) {
            customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 0));
            customPanel.setVisible(false);
            
            updateUI();
        }
    }//GEN-LAST:event_maleBtnActionPerformed

    private void femaleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_femaleBtnActionPerformed
        if (customPanel.isVisible()) {
            customTxFld.setPreferredSize(new Dimension(customTxFld.getPreferredSize().width, 0));
            customPanel.setVisible(false);
            
            updateUI();
        }
    }//GEN-LAST:event_femaleBtnActionPerformed

    private void doneBtnPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_doneBtnPanelMouseEntered
        doneBtnPanel.setBackground(GradeUtils.Colors.hoverBtnColor);
        updateUI();
    }//GEN-LAST:event_doneBtnPanelMouseEntered

    private void doneBtnPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_doneBtnPanelMouseExited
        doneBtnPanel.setBackground(GradeUtils.Colors.buttonColor);
        updateUI();
    }//GEN-LAST:event_doneBtnPanelMouseExited

    private void doneBtnPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_doneBtnPanelMousePressed
        boolean error = false;
        for (TxFieldHandler handler : constrainstsGrp) {
            if (handler.isOnError()) {
                error = true;
            }
        }
        
        if (error) {
            errPopupWindow.show(doneBtnPanel, 0, doneBtnPanel.getHeight());
            updateUI();
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(new Frame(), 
                        "Are you sure your information is correct?", 
                        "Confirmation Dialog", JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION)
            return;
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        // region: map the user's information and sent it to the server
        Map<String, Object> userInfos = new HashMap<>();
        String name = GradeUtils.removeStringSpaces(firstNameTxFldHandler.getAbsoluteText());
        String last = GradeUtils.removeStringSpaces(lastNameTxFldHandler.getAbsoluteText());
        String suff = GradeUtils.removeStringSpaces(suffixTxFldHandler.getAbsoluteText());
        // bday
        String bday = "";
        if (!"".equals(mosTxFldHandler.getAbsoluteText()) && 
            !"".equals(daysTxFldHandler.getAbsoluteText()) &&
            !"".equals(yearTxFldHandler.getAbsoluteText()))
            bday = mosTxFldHandler.getAbsoluteText() + "/" + daysTxFldHandler.getAbsoluteText()+ "/" + yearTxFldHandler.getAbsoluteText();
        // name
        String displayName = name + " " + last + (suff.isEmpty()?"":" "+suff);
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
        
        userInfos.put("name", displayName);
        userInfos.put("bday", bday);
        userInfos.put("gender", gender[0]);
        userInfos.put("contact", contactNumHandler.getAbsoluteText());
        userInfos.put("school_name", schoolNameTxFldHandler.getAbsoluteText());
        DBManager.getInstance().updateUserInfoWithSignIn(new JSONObject(userInfos).toString());
        // region end
    }//GEN-LAST:event_doneBtnPanelMousePressed
    
    private void termsLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_termsLblMousePressed
        new TermsAndConditionActivity().setVisible(true);
    }//GEN-LAST:event_termsLblMousePressed

    private void dataPolicyLblMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataPolicyLblMousePressed
        new DataPolicyActivity().setVisible(true);
    }//GEN-LAST:event_dataPolicyLblMousePressed

    private void termsLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_termsLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_termsLblMouseEntered

    private void termsLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_termsLblMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_termsLblMouseExited

    private void dataPolicyLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataPolicyLblMouseEntered
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_dataPolicyLblMouseEntered

    private void dataPolicyLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataPolicyLblMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_dataPolicyLblMouseExited

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        ImageIcon icon = new ImageIcon(WorkBookActivity.class.getResource("/res/grading-logo.png"));
        setIconImage(icon.getImage());
        setTitle("Graede Fill-up Information");
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
    private com.tajos.studio.graphics.RoundedPanel contactNumberPanel;
    private javax.swing.JTextField contactNumberTxFld;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JRadioButton customBtn;
    private com.tajos.studio.graphics.RoundedPanel customPanel;
    private javax.swing.JTextField customTxFld;
    private com.tajos.studio.components.TajosJLabel dataPolicyLbl;
    private com.tajos.studio.graphics.RoundedPanel daysPanel;
    private javax.swing.JTextField daysTxFld;
    private com.tajos.studio.graphics.RoundedPanel doneBtnPanel;
    private javax.swing.JRadioButton femaleBtn;
    private com.tajos.studio.graphics.RoundedPanel firstNamePanel;
    private javax.swing.JTextField firstNameTxFld;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private com.tajos.studio.graphics.RoundedPanel lastNamePanel;
    private javax.swing.JTextField lastNameTxFld;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JRadioButton maleBtn;
    private com.tajos.studio.components.MinimizedButton minimizedButton1;
    private com.tajos.studio.graphics.RoundedPanel mosPanel;
    private javax.swing.JTextField mosTxFld;
    private com.tajos.studio.graphics.RoundedPanel schoolNamePanel;
    private javax.swing.JTextField schoolNameTxFld;
    private javax.swing.JPanel space1;
    private com.tajos.studio.graphics.RoundedPanel suffixPanel;
    private javax.swing.JTextField suffixTxFld;
    private com.tajos.studio.components.TajosJLabel tajosJLabel1;
    private com.tajos.studio.components.TajosJLabel tajosJLabel10;
    private com.tajos.studio.components.TajosJLabel tajosJLabel2;
    private com.tajos.studio.components.TajosJLabel tajosJLabel3;
    private com.tajos.studio.components.TajosJLabel tajosJLabel4;
    private com.tajos.studio.components.TajosJLabel tajosJLabel5;
    private com.tajos.studio.components.TajosJLabel tajosJLabel6;
    private com.tajos.studio.components.TajosJLabel tajosJLabel7;
    private com.tajos.studio.components.TajosJLabel tajosJLabel8;
    private com.tajos.studio.components.TajosJLabel tajosJLabel9;
    private javax.swing.JPanel termsAndPolicyRoot;
    private com.tajos.studio.components.TajosJLabel termsLbl;
    private javax.swing.JPanel toolbar;
    private com.tajos.studio.graphics.RoundedPanel yearPanel;
    private javax.swing.JTextField yearTxFld;
    // End of variables declaration//GEN-END:variables
}
