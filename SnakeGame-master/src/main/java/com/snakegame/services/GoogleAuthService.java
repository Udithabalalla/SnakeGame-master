package com.snakegame.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.snakegame.models.User;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthService {
    private static final String CLIENT_SECRET_FILE = "src/main/resources/client_secret.json";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    
    private final GoogleIdTokenVerifier verifier;
    private final AuthenticationService authService;
    
    public GoogleAuthService(AuthenticationService authService) {
        this.authService = authService;
        
        // Initialize verifier (you'll need your Google Client ID)
        this.verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
            .setAudience(Collections.singletonList("YOUR_CLIENT_ID.apps.googleusercontent.com"))
            .build();
    }
    
    /**
     * Initiates Google OAuth flow and returns authenticated user
     */
    public User signInWithGoogle() throws Exception {
        try {
            // Load client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, 
                new FileReader(CLIENT_SECRET_FILE)
            );
            
            // Set up authorization flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, 
                JSON_FACTORY, 
                clientSecrets,
                Collections.singletonList("email profile openid")
            ).setAccessType("offline").build();
            
            // Start local server to receive callback
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();
            
            // Authorize and get credential
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");
            
            // Get ID token
            String idTokenString = credential.getAccessToken();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String googleId = payload.getSubject();
                
                // Check if user exists
                User existingUser = authService.getUserByEmail(email);
                
                if (existingUser != null) {
                    // User exists, update avatar if needed
                    if (pictureUrl != null && !pictureUrl.equals(existingUser.getAvatarUrl())) {
                        authService.updateAvatar(existingUser.getUserId(), pictureUrl);
                        existingUser.setAvatarUrl(pictureUrl);
                    }
                    return existingUser;
                } else {
                    // Create new user with Google info
                    return authService.registerWithGoogle(email, name, pictureUrl, googleId);
                }
            } else {
                throw new Exception("Invalid ID token");
            }
            
        } catch (IOException | GeneralSecurityException e) {
            throw new Exception("Google Sign-In failed: " + e.getMessage());
        }
    }
    
    /**
     * Verify Google ID token (alternative lightweight method)
     */
    public User verifyGoogleToken(String idTokenString) throws Exception {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String googleId = payload.getSubject();
                
                User existingUser = authService.getUserByEmail(email);
                
                if (existingUser != null) {
                    return existingUser;
                } else {
                    return authService.registerWithGoogle(email, name, pictureUrl, googleId);
                }
            } else {
                throw new Exception("Invalid ID token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new Exception("Token verification failed: " + e.getMessage());
        }
    }
}
