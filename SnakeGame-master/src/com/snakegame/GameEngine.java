package com.snakegame;

import java.awt.*;

public class GameEngine {
    private Snake snake;
    private Food food;
    private int score;
    private int difficulty;
    private int width;
    private int height;
    private int gridSize;

    public GameEngine(Snake snake, Food food, int width, int height, int gridSize) {
        this.snake = snake;
        this.food = food;
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;
        this.score = 0;
        this.difficulty = 1;
    }

    public boolean checkCollisions() {
        Point head = snake.getHead();

        // Wall collision
        if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
            return true;
        }

        // Self collision
        return snake.checkSelfCollision();
    }

    public boolean checkFoodCollision() {
        Point head = snake.getHead();
        Point foodPos = food.getPosition();
        return head.equals(foodPos);
    }

    public void addScore(int points) {
        score += points;
    }

    public void increaseDifficulty() {
        if (score % 100 == 0 && score > 0) {
            difficulty++;
        }
    }

    public int getScore() {
        return score;
    }

    public int getDifficulty() {
        return difficulty;
    }
}