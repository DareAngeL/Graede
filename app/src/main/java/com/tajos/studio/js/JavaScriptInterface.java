package com.tajos.studio.js;

import com.tajos.studio.net.DBManager;
import com.tajos.studio.activities.SignupActivity;
import com.tajos.studio.activities.Fill_upInformationActivity;
import com.tajos.studio.activities.LoginActivity;
import com.tajos.studio.activities.WorkBookActivity;
import com.tajos.studio.interfaces.JavascriptInterfaceListener;
import com.tajos.studio.js.adapter.JavascriptInterfaceAdapter.Properties;
import com.tajos.studio.net.GoogleProvider;
import com.tajos.studio.util.GradeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.swing.JFrame;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JavaScript Interface
 * @author Rene Tajos Jr.
 */
public class JavaScriptInterface {
    
    public static final String name = "GradeAppJava";
    private final List<JavascriptInterfaceListener> listeners = new ArrayList<>();
    private String mProvider;
    
    public JavaScriptInterface() {}

    public void addJavascriptInterfaceListener(JavascriptInterfaceListener listener) {
        if (listeners.contains(listener))
            return;
        
        listeners.add(listener);
    }
    
    private void fireListeners(int property, @Nullable String data) {
        switch (property) {
            case Properties.DATA_CHANGED -> {
                for (JavascriptInterfaceListener listener : listeners) {
                    listener.onDataChanged(data);
                }
            }
            case Properties.PUBLISHED -> {
                for (JavascriptInterfaceListener listener : listeners) {
                    listener.onPublished();
                }
            }
            case Properties.SIGNED_OUT -> {
                for (JavascriptInterfaceListener listener : listeners) {
                    listener.isSignedOut();
                }
            }
            case Properties.SEND_RESET_PASS_SUCCESS -> {
                for (JavascriptInterfaceListener listener : listeners) {
                    listener.onSendEmailResetPasswordSuccess();
                }
            }
            case Properties.SEND_RESET_PASS_FAILED -> {
                for (JavascriptInterfaceListener listener : listeners) {
                    listener.onSendEmailResetPasswordFailed(data);
                }
            }
        }
    }
    
    /**
     * 
     * @return the {@Firebase config} of this application
     */
    public String getFirebaseConfig() {
        Map<String, Object> conf = new HashMap<>();
        conf.put("apiKey", "AIzaSyA3Xc9kZJc7yA_TjGaDiGv2TdNF5XZpfdc");
        conf.put("authDomain", "grading-system-408f5.firebaseapp.com");
        conf.put("databaseURL", "https://grading-system-408f5-default-rtdb.asia-southeast1.firebasedatabase.app");
        conf.put("projectId", "grading-system-408f5");
        conf.put("storageBucket", "grading-system-408f5.appspot.com");
        conf.put("messagingSenderId", "57540000972");
        conf.put("appId", "1:57540000972:web:d111065a4341138f325669");
        conf.put("measurementId", "G-3QR0QVZYRZ");
        
        return new JSONObject(conf).toJSONString();
    }
    
    public void onDataChanged(String data) {
        try {
            JSONParser parser = new JSONParser();
            
            JSONObject obj = (JSONObject) parser.parse(data);
            DBManager.getInstance().setUserData(obj);
            fireListeners(Properties.DATA_CHANGED, obj.toJSONString());
            
        } catch (ParseException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    public void log(String str) {}
    
    public void err(String str) {}
    
    public void onSendEmailResetPasswordSuccess() {
        fireListeners(Properties.SEND_RESET_PASS_SUCCESS, null);
    }
    
    public void onSendEmailResetPasswordFailed(String reason) {
        fireListeners(Properties.SEND_RESET_PASS_FAILED, reason);
    }
    
    public void onPublished() {
        fireListeners(Properties.PUBLISHED, null);
    }
    
    public void onSendingVerification() {
        SignupActivity.getInstance().showEmailVerificationScreen();
    }
    
    /**
     * If the java-script client will call this method, it means , the sign up is from firebase email and password,
     * We need to redirect the user to fill-up information about him/her if this is the case.
     */
    public void onEmailVerified() {
        SignupActivity.getInstance().stopEmailVerificationTimer();
        SignupActivity.getInstance().setVisible(false);
        new Fill_upInformationActivity().setVisible(true);
    }
    
    public void onLoginError(String err) {
        LoginActivity.getInstance().showLoginError(err);
    }
    
    public void onCreateUserError(String err) {
        SignupActivity.getInstance().showCreateUserError(err);
    }
    
    public void isSignedIn(String provider) {
        mProvider = provider;
        JFrame authActivityInstance = SignupActivity.getInstance();
        if (authActivityInstance != null && authActivityInstance.isVisible()) {
            authActivityInstance.setVisible(false);
            authActivityInstance.dispose();
            authActivityInstance = null;
        }
        
        JFrame fillUpActivityInstance = Fill_upInformationActivity.getInstance();
        if (fillUpActivityInstance != null && fillUpActivityInstance.isVisible()) {
            fillUpActivityInstance.setVisible(false);
            fillUpActivityInstance.dispose();
            fillUpActivityInstance = null;
        }
        
        JFrame loginActivityInstance = LoginActivity.getInstance();
        if (loginActivityInstance != null && loginActivityInstance.isVisible()) {
            loginActivityInstance.setVisible(false);
            loginActivityInstance.dispose();
            loginActivityInstance = null;
        }
        
        new WorkBookActivity().setVisible(true);
        
        if (provider.equals("google.com"))
            GoogleProvider.getInstance().stop();
    }
    
    public void isSignedOut(String provider) {
        
        if (DBManager.getInstance().getUserData() != null)
            DBManager.getInstance().clearUserData();
        
        fireListeners(Properties.SIGNED_OUT, null);
        
        if (!SignupActivity.getInstance().isVisible())
            SignupActivity.getInstance().setVisible(true);
        
        SignupActivity.getInstance().showSignupActivity();
        
        if (provider.equals("google.com")) {
            GoogleProvider.getInstance().signOutGoogle();
        }
    }

    public String getProvider() {
        return mProvider;
    }
}