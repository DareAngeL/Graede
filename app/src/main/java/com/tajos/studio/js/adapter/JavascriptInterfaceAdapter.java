package com.tajos.studio.js.adapter;

import com.tajos.studio.interfaces.JavascriptInterfaceListener;

/**
 *
 * @author Rene Tajos Jr
 */
public abstract class JavascriptInterfaceAdapter implements JavascriptInterfaceListener {
    
    public static class Properties {
        public static final int DATA_CHANGED = 0;
        public static final int SIGNED_OUT = 1;
        public static final int SEND_RESET_PASS_FAILED = 2;
        public static final int SEND_RESET_PASS_SUCCESS = 3;
        public static final int PUBLISHED = 4;
    }

    @Override
    public void onDataChanged(String data) {}

    @Override
    public void isSignedOut() {}

    @Override
    public void onPublished() {}

    @Override
    public void onSendEmailResetPasswordFailed(String reason) {}

    @Override
    public void onSendEmailResetPasswordSuccess() {}
}
