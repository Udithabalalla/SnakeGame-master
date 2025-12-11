package com.snakegame;

import java.awt.*;
import java.util.Random;

public class Food {
    private Point position;
    private int width;
    private int height;
    private int gridSize;
    private Random random;

    public Food(int width, int height, int gridSize) {
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;
        this.random = new Random();
        respawn();
    }

    public void respawn() {
        int x = (random.nextInt(width / gridSize)) * gridSize;
        int y = (random.nextInt(height / gridSize)) * gridSize;
        position = new Point(x, y);
    }

    public Point getPosition() {
        return position;
    }
}