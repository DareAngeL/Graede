package com.tajos.studio.net;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 * @author Rene Tajos Jr.
 */
public class ThirdPartyAuthentication {
    
    private static final String clientId = "57540000972-fd9gbs6tt7kpqkb4ekmd32mm9odo4qej.apps.googleusercontent.com";
    private static final String cliendSecret = "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";
    private static final String redirectUrl = "http://localhost:5000/sign_in";
    private static GoogleProvider googleResponseListener;
    
    /**
     * Logging in to google provider
     * @param dBManager we need this parameter for logging in to the database later
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static void googleLogin(DBManager dBManager) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        String state_token = generateStateToken();
        String codeVerifier = generateCodeVerifier();
        URL url = getAuthenticationURL(state_token, codeVerifier);
        // region: opens a local server to listen the response from google server
        if (googleResponseListener == null || !googleResponseListener.isOnline()) {
            googleResponseListener = new GoogleProvider(dBManager);
            googleResponseListener.create(); // create server at localhost and port, at 5000
            googleResponseListener.start(); // start the local server
        // region end
        }
        
        googleResponseListener.setCodeVerifier(codeVerifier);
        googleResponseListener.setStateToken(state_token);

        state_token = null;
        codeVerifier = null;
        openSystemBrowser(url);
    }
    
    public static String getClientID() {
        return clientId;
    }
    
    public static String getClientSecret() {
        return cliendSecret;
    }
    
    public static String getRedirectUri() {
        return redirectUrl;
    }
    
    private static void openSystemBrowser(URL url) throws URISyntaxException, IOException {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(url.toURI());
    }
    
    private static URL getAuthenticationURL(String token, String codeVerifier) throws NoSuchAlgorithmException, MalformedURLException {
        String code_challenge = codeVerifier;
        
        String parameter = "https://accounts.google.com/o/oauth2/v2/auth?"
                            + "client_id="+clientId+"&"
                            + "redirect_uri="+redirectUrl+"&"
                            + "scope=profile&"
                            + "response_type=code&"
                            + "code_challenge="+code_challenge+"&"
                            + "approval_prompt=force&"
                            + "access_type=offline&"
                            + "state="+token;
        
        return new URL(parameter);
    }
    
    private static String generateStateToken() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] data = new byte[16];
        random.nextBytes(data);

        // convert to Base64 string
        return Base64.getEncoder().encodeToString(data);
    }
    
    private static String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        random.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
    /*
    private static String generateCodeChallenge(String codeVerifier) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes("US-ASCII");
        MessageDigest digestMsg = MessageDigest.getInstance("SHA-256");
        digestMsg.update(bytes, 0, bytes.length);
        byte[] digest = digestMsg.digest();
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }*/
}