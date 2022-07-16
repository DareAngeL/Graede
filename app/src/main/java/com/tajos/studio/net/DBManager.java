package com.tajos.studio.net;

import com.tajos.studio.js.JavaScriptInterface;
import com.tajos.studio.activities.WorkBookActivity;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;

/**
 *
 * @author Rene Tajos Jr.
 */
public class DBManager {

    private final JFXPanel mJfxPanel = new JFXPanel();
    private static JSObject mWin;
    private boolean isInitialized = false;
    
    private WebView webView;
    private WebEngine mWebEngine;
    private static DBManager instance;
    private final JavaScriptInterface js;
    
    private static JSONObject userData;
    
    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        
        return instance;
    }
    
    public DBManager() {
        js = new JavaScriptInterface();
    }
    
    public void initDB() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        Platform.runLater(() -> {
            webView = new WebView();
            mJfxPanel.setScene(new Scene(webView));
            String webRes = "https://grading-system-408f5.web.app";
            mWebEngine = webView.getEngine();
            mWebEngine.setJavaScriptEnabled(true);

            mWebEngine.getLoadWorker().stateProperty().addListener(
                     (ObservableValue<? extends State> ov, State oldState, State newState) -> {

                if (newState == State.SUCCEEDED) {
                    _openWindow();
                    initFirebase();
                }
            });
            
            mWebEngine.load(webRes);
        });
    }
    
    private void _openWindow() {
        mWin = (JSObject) mWebEngine.executeScript("window");
        mWin.setMember("GradeAppJava", js);
    }
    
    public void setUserData(JSONObject obj) {
        userData = obj;
    }
    
    public JavaScriptInterface getJavaScriptInterface() {
        return js;
    }
    
    public void clearUserData() {
        userData.clear();
    }
    
    public JSONObject getUserData() {
        return userData;
    }
    
    public void setInitialized(boolean bool) {
        isInitialized = bool;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    private void initFirebase() {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.initFirebase()");
        });
    }
    
    public void sendResetPasswordEmail(String email) {
        Platform.runLater(() -> {
            mWebEngine.executeScript(
            "main.sendEmailForResetPassword('"+email+"')");
        });
    }
    
    public void signInUserWithGoogle(String idToken) {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.signInUserWithGoogle('"+idToken+"')");
        });
    }
    
    public void signInUserWithEmailAndPassword(String email, String pass) {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.signIn('"+email+"','"+pass+"')");
        });
    }
    
    public void signUpUserWithEmailAndPassword(String email, String pass) {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.createUser('"+email+"','"+pass+"')");
        });
    }
    
    public void signOutUser() {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.signOutUser()");
        });
    }
    
    public void updateUserInfoWithSignIn(String str) {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.updateUserInfo('"+str+"')");
        });
    }
    
    public void updateUserInfo(String str) {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.updateWithoutSignIn('"+str+"')");
        });
    }
    
    public void reloadUser() {
        Platform.runLater(() -> {
            mWebEngine.executeScript("main.reloadUser()");
        });
    }
    
    public void publish(WorkBookActivity.PublishType type, String workbookName, String data) {
        switch (type) {
            case ONE_SHEET -> {
                Platform.runLater(() -> {
                    if (data.isEmpty()) {
                        mWebEngine.executeScript("main.publishSheet('"+workbookName+"', \"null\")");
                        return;
                    }
                    mWebEngine.executeScript("main.publishSheet('"+workbookName+"', '"+data+"')");
                });
            }
            case ONE_WORKBOOK -> {
                Platform.runLater(() -> {
                    if (data.isEmpty()) {
                        mWebEngine.executeScript("main.publishWorkbook('"+workbookName+"', \"null\")");
                        return;
                    }
                    mWebEngine.executeScript("main.publishWorkbook('"+workbookName+"', '"+data+"')");
                });
            }
            case ALL_WORKBOOKS -> {
                Platform.runLater(() -> {
                    mWebEngine.executeScript("main.publishAllWorkbooks('"+data+"')");
                });
            }
        }
    }
}