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
        spawn();
    }

    public void spawn() {
        int maxX = (width / gridSize) - 1;
        int maxY = (height / gridSize) - 1;

        int x = random.nextInt(maxX) * gridSize;
        int y = random.nextInt(maxY) * gridSize;

        position = new Point(x, y);
    }

    public Point getPosition() {
        return position;
    }

    public void draw(Graphics2D g, Color color) {
        g.setColor(color);
        g.fillOval(position.x + 2, position.y + 2,
                gridSize - 4, gridSize - 4);

        // Add shine effect
        g.setColor(color.brighter());
        g.fillOval(position.x + 4, position.y + 4,
                gridSize / 3, gridSize / 3);
    }
}