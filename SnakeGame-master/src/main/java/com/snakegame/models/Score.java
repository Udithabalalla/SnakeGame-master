package com.snakegame.models;

import java.io.Serializable;
import java.util.Date;

public class Score implements Serializable, Comparable<Score> {
    private String scoreId;
    private String userId;
    private String userName;
    private String avatarUrl;
    private int score;
    private Difficulty difficulty;
    private Date timestamp;
    
    public Score() {}
    
    public Score(String userId, String userName, String avatarUrl, int score, Difficulty difficulty) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.score = score;
        this.difficulty = difficulty;
        this.timestamp = new Date();
    }
    
    // Getters and setters
    public String getScoreId() { return scoreId; }
    public void setScoreId(String scoreId) { this.scoreId = scoreId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    @Override
    public int compareTo(Score other) {
        return Integer.compare(other.score, this.score);
    }
}