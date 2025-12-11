package com.snakegame.firebase;

import com.snakegame.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class AuthService {
    private FirestoreService firestoreService;
    
    public AuthService() {
        this.firestoreService = new FirestoreService();
    }
    
    /**
     * Register a new user
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String email, String password) throws Exception {
        // Check if username already exists
        User existingUser = firestoreService.getUserByUsername(username);
        if (existingUser != null) {
            return false;
        }
        
        // Check if email exists (if provided)
        if (email != null && !email.isEmpty()) {
            User existingEmail = firestoreService.getUserByEmail(email);
            if (existingEmail != null) {
                throw new Exception("Email already registered");
            }
        }
        
        // Hash password
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        
        // Create user
        User newUser = new User();
        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordHash);
        newUser.setAvatarUrl("https://ui-avatars.com/api/?name=" + 
            username.replace(" ", "+") + "&background=random&size=128");
        newUser.setHighScore(0);
        newUser.setTotalGamesPlayed(0);
        
        firestoreService.createUser(newUser);
        return true;
    }
    
    /**
     * Login user
     * @return User object if successful, null otherwise
     */
    public User login(String username, String password) throws Exception {
        User user = firestoreService.getUserByUsername(username);
        
        if (user == null) {
            return null;
        }
        
        // Verify password
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return null;
        }
        
        // Update last login
        firestoreService.updateLastLogin(user.getUserId());
        
        return user;
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) throws Exception {
        User user = firestoreService.getUser(userId);
        
        if (user == null) {
            return false;
        }
        
        // Verify old password
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            return false;
        }
        
        // Hash new password
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        
        // Update password
        firestoreService.updatePassword(userId, newPasswordHash);
        return true;
    }
    
    /**
     * Reset password (requires email verification in production)
     */
    public boolean resetPassword(String email, String newPassword) throws Exception {
        User user = firestoreService.getUserByEmail(email);
        
        if (user == null) {
            return false;
        }
        
        // In production, send verification email before allowing reset
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        firestoreService.updatePassword(user.getUserId(), newPasswordHash);
        return true;
    }
    
    /**
     * Verify if username is available
     */
    public boolean isUsernameAvailable(String username) throws Exception {
        return firestoreService.getUserByUsername(username) == null;
    }
    
    /**
     * Verify if email is available
     */
    public boolean isEmailAvailable(String email) throws Exception {
        if (email == null || email.isEmpty()) {
            return true;
        }
        return firestoreService.getUserByEmail(email) == null;
    }
}