package com.snakegame.firebase;

import com.snakegame.models.User;

import java.util.UUID;

public class AuthenticationService {
    private final FirestoreService firestoreService;
    
    public AuthenticationService() {
        this.firestoreService = new FirestoreService();
    }
    
    /**
     * Register a new user via Google Sign-In
     */
    public User registerWithGoogle(String email, String name, String avatarUrl, String googleId) throws Exception {
        // Check if user already exists
        User existingUser = getUserByEmail(email);
        if (existingUser != null) {
            return existingUser;
        }
        
        // Generate unique username from email or name
        String username = generateUsernameFromEmail(email, name);
        
        // Create user with Google info
        User newUser = new User();
        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(hashPassword(googleId)); // Use Google ID as password placeholder
        newUser.setAvatarUrl(avatarUrl != null ? avatarUrl : "default_avatar.png");
        
        firestoreService.createUser(newUser);
        
        return newUser;
    }
    
    /**
     * Helper method to generate username from email
     */
    private String generateUsernameFromEmail(String email, String name) throws Exception {
        String baseUsername;
        
        if (name != null && !name.isEmpty()) {
            baseUsername = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        } else {
            baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        }
        
        // Ensure username is unique
        String username = baseUsername;
        int counter = 1;
        
        while (getUserByUsername(username) != null) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) throws Exception {
        return firestoreService.getUserByEmail(email);
    }
    
    // ...existing methods...
}