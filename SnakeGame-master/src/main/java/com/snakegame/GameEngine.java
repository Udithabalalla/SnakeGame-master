package com.snakegame;

import java.awt.*;

public class GameEngine {
    private int score;
    private int width;
    private int height;
    private int gridSize;
    private boolean gameOver;

    public GameEngine(int width, int height, int gridSize) {
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;
        this.score = 0;
        this.gameOver = false;
    }

    public void update(Snake snake, Food food) {
        if (gameOver) return;

        snake.move();

        // Check food collision
        if (checkFoodCollision(snake, food)) {
            snake.grow();
            food.spawn();
            addScore(10);
        }

        // Check collisions
        if (checkCollisions(snake)) {
            gameOver = true;
        }
    }

    public boolean checkCollisions(Snake snake) {
        Point head = snake.getHead();

        // Wall collision
        if (head.x < 0 || head.x >= width || head.y < 0 || head.y >= height) {
            return true;
        }

        // Self collision
        return snake.checkSelfCollision();
    }

    public boolean checkFoodCollision(Snake snake, Food food) {
        Point head = snake.getHead();
        Point foodPos = food.getPosition();
        return head.equals(foodPos);
    }

    public void addScore(int points) {
        score += points;
    }

    public void incrementScore(int points) {
        this.score += points;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}