package com.tajos.studio;

import com.tajos.studio.net.DBManager;
import com.tajos.studio.activities.SignupActivity;
import com.tajos.studio.activities.SplashActivity;
import com.tajos.studio.dialogs.ExceptionHandlerDialog;
import com.tajos.studio.util.GradeUtils;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GradeApp {
    
    private static String secretDirectory;
    private static String defaultSaveDirectory;
    private static String webDirectory;
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    private static boolean isFirstInit = false;
    private static GradeApp instance;
    
    public GradeApp() {
        instance = this;
    }
    
    public static ExecutorService executor() {
        return executor;
    }
    
    public static GradeApp instance() {
        if (instance == null)
            instance = new GradeApp();
        
        return instance;
    }
    
    public void init() {
        DBManager.getInstance().initDB();
        
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            String str;
            if (t.getName().equals("JavaFX Application Thread") &&
                e.getMessage().toLowerCase().contains("syntaxerror")) {
                str = "Please check your cells content. Quotations (\"|') are not allowed";
                
                new ExceptionHandlerDialog(new JFrame(), true)
                    .setExceptionMessage(str)
                    .setTextSize(16)
                    .setVisible(true);
                
                return;
            }
            
            str = e.getMessage();
            for (StackTraceElement element : e.getStackTrace()) {
                str = str.concat(element.toString().concat("\n"));
            }
            
            new ExceptionHandlerDialog(new JFrame(), true)
                    .setExceptionMessage(str)
                    .setTextSize(12)
                    .setVisible(true);
        });
    }
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        initGradeAppFolder();
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            GradeUtils.showErrorDialog(ex.getMessage(), "Something went wrong");
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            if (isFirstInit) {
                new SplashActivity().setVisible(true);
            } else {
                GradeApp.instance().init();
                new SignupActivity().setVisible(true);
            }
        });
    }

    private static void initGradeAppFolder() throws IOException, URISyntaxException {
        secretDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory().getPath() + "\\Graede";
        defaultSaveDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory().getPath() + "\\DareAngeL Studio\\Saves";
        webDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory().getPath() + "\\DareAngeL Studio\\Web";
        
        File secretDir = new File(secretDirectory);
        File defaultDir = new File(defaultSaveDirectory);
        File webDir = new File(webDirectory);
        if (!secretDir.exists()){
            secretDir.mkdirs();
        }
        
        if (!defaultDir.exists()) {
            isFirstInit = true;
            defaultDir.mkdirs();
        }
        
        if (!webDir.exists()) {
            webDir.mkdirs();
        }
    }
    
    public static String getDefaultSaveDirectory() {
        return defaultSaveDirectory;
    }
    
    public static String getBasePath() {
        return System.getProperty("user.dir");
    }
    
    public static String getSecretDirectory() {
        return secretDirectory;
    }

    public static String getWebDirectory() {
        return webDirectory;
    }
}