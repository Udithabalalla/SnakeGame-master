package com.snakegame;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private int difficulty;
    private LocalDateTime timestamp;

    public GameState(String playerName, int score, int difficulty) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getDifficulty() { return difficulty; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return playerName + " - Score: " + score + " (Difficulty: " + difficulty + ") - " + timestamp;
    }
}