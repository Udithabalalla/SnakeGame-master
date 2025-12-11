package com.snakegame.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseConfig {
    private static FirebaseApp firebaseApp;
    private static boolean initialized = false;
    private static boolean available = false;
    
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            InputStream serviceAccount = null;
            
            // Try to load from project root first
            File rootFile = new File("serviceAccountKey.json");
            if (rootFile.exists()) {
                serviceAccount = new FileInputStream(rootFile);
                System.out.println("📄 Found Firebase credentials in project root");
            } else {
                // Try to load from resources
                serviceAccount = FirebaseConfig.class.getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");
                if (serviceAccount != null) {
                    System.out.println("📄 Found Firebase credentials in resources");
                }
            }
            
            if (serviceAccount == null) {
                System.err.println("⚠️  Firebase credentials not found!");
                System.err.println("⚠️  Place 'serviceAccountKey.json' in project root");
                System.err.println("⚠️  Running in offline mode...");
                initialized = true;
                available = false;
                return;
            }
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            firebaseApp = FirebaseApp.initializeApp(options);
            initialized = true;
            available = true;
            
            System.out.println("✅ Firebase initialized successfully!");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            System.err.println("⚠️  Running in offline mode...");
            initialized = true;
            available = false;
        }
    }
    
    public static FirebaseApp getApp() {
        if (!initialized) {
            initialize();
        }
        return firebaseApp;
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return available;
    }
}