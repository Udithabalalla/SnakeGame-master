package com.snakegame.models;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String passwordHash; // BCrypt hashed password
    private String avatarUrl;
    private int highScore;
    private int totalGamesPlayed;
    private Date createdAt;
    private Date lastLogin;
    
    public User() {}
    
    public User(String userId, String username, String email, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = getDefaultAvatar(username);
        this.highScore = 0;
        this.totalGamesPlayed = 0;
        this.createdAt = new Date();
        this.lastLogin = new Date();
    }
    
    private String getDefaultAvatar(String username) {
        // Generate avatar URL from DiceBear API or UI Avatars
        return "https://ui-avatars.com/api/?name=" + username + 
               "&background=random&size=128&bold=true";
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
    
    public int getTotalGamesPlayed() { return totalGamesPlayed; }
    public void setTotalGamesPlayed(int totalGamesPlayed) { 
        this.totalGamesPlayed = totalGamesPlayed; 
    }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
}