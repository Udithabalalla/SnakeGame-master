package com.snakegame.models;

public enum Difficulty {
    EASY(1.0, "Easy", 5, new java.awt.Color(0, 200, 0)),
    MEDIUM(1.5, "Medium", 8, new java.awt.Color(255, 165, 0)),
    HARD(2.5, "Hard", 12, new java.awt.Color(255, 0, 0));
    
    private final double scoreMultiplier;
    private final String displayName;
    private final int speed;
    private final java.awt.Color color;
    
    Difficulty(double scoreMultiplier, String displayName, int speed, java.awt.Color color) {
        this.scoreMultiplier = scoreMultiplier;
        this.displayName = displayName;
        this.speed = speed;
        this.color = color;
    }
    
    public double getScoreMultiplier() { return scoreMultiplier; }
    public String getDisplayName() { return displayName; }
    public int getSpeed() { return speed; }
    public java.awt.Color getColor() { return color; }
    
    public int calculateScore(int baseScore) {
        return (int) (baseScore * scoreMultiplier);
    }

    public int getPointsPerFood() {
        switch (this) {
            case EASY:
                return 10;
            case MEDIUM:
                return 15;
            case HARD:
                return 20;
            default:
                return 10;
        }
    }
}