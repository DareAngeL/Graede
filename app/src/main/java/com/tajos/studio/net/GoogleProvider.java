package com.tajos.studio.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tajos.studio.GradeApp;
import com.tajos.studio.util.GradeUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rene Tajos Jr.
 */
public class GoogleProvider {

    private static GoogleProvider instance;
    
    private HttpServer server;
    private static String mStateToken = "";
    private static String mCodeVerifier = "";
    private static DBManager mDBManager;
    
    public static GoogleProvider getInstance() {
        if (instance == null)
            instance = new GoogleProvider(DBManager.getInstance());
        
        return instance;
    }
    private boolean isOnline = false;
    
    public GoogleProvider(DBManager dBManager) {
        mDBManager = dBManager;
        instance = this;
    }
    
    public void create() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 5000), 0);
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.createContext("/sign_in", new THttpHandler());
        server.setExecutor(executor);
    }
    
    public void start() {
        server.start();
        isOnline = true;
    }
    
    public void stop() {
        isOnline = false;
        server.stop(5);
        instance = null;
    }

    public void setStateToken(String token) {
        mStateToken = token;
    }
    
    public void setCodeVerifier(String code) {
        mCodeVerifier = code;
    }

    public boolean isOnline() {
        return isOnline;
    }
    
    public void signOutGoogle() {
        try {
            String access_token = getAccessTokenFromFile();
            
            if (access_token.isEmpty()) {
                
                return;
            }   
            
            String params = "token=" + access_token;
            URL url = new URL("https://oauth2.googleapis.com/revoke");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params);
            writer.flush();
            
            conn.getInputStream();
        } catch (MalformedURLException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        } catch (IOException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }
    }
    
    private String getAccessTokenFromFile() throws FileNotFoundException, IOException {
        InputStream reader=new FileInputStream(GradeApp.getSecretDirectory() + "\\cfg.properties");
        Properties p=new Properties();  
        p.load(reader);
        
        return p.get("access_token").toString();
    }

    private static class THttpHandler implements HttpHandler {
        
        public THttpHandler() {}

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String code = null;
                Map<String, Object> params = getRequestParameters(exchange);
                
                // region: execute the block if the connection is GET
                if ("GET".equals(exchange.getRequestMethod())) {
                    // region: shows an error if request is invalid
                    if (!isCredibleRequest(params.get("state").toString())) {
                        JOptionPane.showMessageDialog(new JFrame(), 
                                "Can't connect!. The request is invalid!", 
                                "Unknown Request Error", JOptionPane.ERROR_MESSAGE);
                        
                        handleResponse(null, "");
                        return; // we will not continue if the request is invalid!
                    }
                    // else, if the request is valid  we should now clear the state token
                    mStateToken = null;
                    code = params.get("code").toString();
                }
                // region end
                // region: decode the code
                if (code != null) {
                    code = URLDecoder.decode(code, StandardCharsets.UTF_8.name());
                    code = code.replaceAll("\\s", "+");
                }
                // region end
                handleResponse(exchange, code);
            } catch (URISyntaxException ex) {
                GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
            }
        }
        
        private void handleResponse(HttpExchange exchange, String code) throws IOException, URISyntaxException {
            File file = null;
            JsonObject tokens = null;
            InputStream in;
            
            if (exchange == null || code == null) {
                in = getClass().getResourceAsStream("/web-files/google_login_failed.html");
            } else {
                tokens = getAccessTokens(code);
                signIn(tokens.get("id_token").getAsString());
                in = getClass().getResourceAsStream("/web-files/google_login_success.html");
            }
            
            assert exchange != null;
            try (OutputStream outStream = exchange.getResponseBody()) {
                assert file != null;
                exchange.sendResponseHeaders(200, in.available());
                _copyInputStreamtoOutputStream(in, outStream);
                
            }
            
            // region: store the tokens
            // dissect the tokens
            String config = "";
            assert tokens != null;
            for (Map.Entry<String, JsonElement> entry : tokens.entrySet()) {
                String key = entry.getKey();
                JsonElement element = entry.getValue();
                
                config += key+"="+element.getAsString()+"\n";
            }
            // end
            File fileRoot = new File(GradeApp.getSecretDirectory());
            File writeFile = new File(GradeApp.getSecretDirectory() + "\\cfg.properties");
            Path path = Path.of(writeFile.toURI()).toAbsolutePath().normalize();
            Path fileRootPath = Path.of(fileRoot.toURI());
            assert tokens != null;
            GradeUtils.writeFile(config, path.toUri(), false);
            java.nio.file.Files.setAttribute(fileRootPath, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            // region end
        }
        
        private void _copyInputStreamtoOutputStream(InputStream in, OutputStream out) throws IOException {
            
            try (out) {
                int length;
                byte[] bytes = new byte[1024];
                
                // copy data from input stream to output stream
                while ((length = in.read(bytes)) != -1) {
                    out.write(bytes, 0, length);
                }
                
                out.flush();
            }
        }

        private Map getRequestParameters(HttpExchange exchange) {
            Map<String, Object> paramMap = new HashMap<>();
            
            String[] params = exchange.getRequestURI().toString()
                    .split("\\?")[1]
                    .split("&");
            
            for (String str : params) {
                String[] s = str.split("=");
                paramMap.put(s[0], s[1]);
            }
            
            return paramMap;
        }
        
        /**
         * Get the the access tokens
         * @param code
         * @return
         * @throws MalformedURLException
         * @throws IOException 
         */
        private JsonObject getAccessTokens(String code) throws MalformedURLException, IOException {
            String urlParameters = 
                      "code="+code
                    + "&client_id="+ThirdPartyAuthentication.getClientID()
                    + "&client_secret="+ThirdPartyAuthentication.getClientSecret()
                    + "&redirect_uri="+ThirdPartyAuthentication.getRedirectUri()
                    + "&code_verifier="+mCodeVerifier
                    + "&grant_type=authorization_code";
            // region: POST the parameters
            URL url = new URL("https://oauth2.googleapis.com/token");
            URLConnection urlConn = url.openConnection();
            urlConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(urlParameters);
            writer.flush();
            // region end
            // region: get the return value from POST
            String line, outputString = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                outputString += line;
            }
            // region end
            return JsonParser.parseString(outputString).getAsJsonObject();
        }
        
        private void signIn(String idToken) {
            mDBManager.signInUserWithGoogle(idToken);
        }
        
        private boolean isCredibleRequest(String token) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
            token = URLDecoder.decode(token, StandardCharsets.UTF_8.name());
            token = token.replaceAll("\\s", "+");
            
            return mStateToken.equals(token);
        }
    }
}