package com.tajos.studio.interfaces;

/**
 *
 * @author Rene Tajos Jr
 */
public interface JavascriptInterfaceListener {
    
    void onDataChanged(String data);
    void onPublished();
    void isSignedOut();
    void onSendEmailResetPasswordSuccess();
    void onSendEmailResetPasswordFailed(String reason);
}
    