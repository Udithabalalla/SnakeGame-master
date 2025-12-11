package com.snakegame;

import java.awt.*;
import java.util.*;

public class Snake {
    private java.util.List<Point> body;
    private int gridSize;
    private Direction direction;

    public Snake(int startX, int startY, int gridSize) {
        body = new ArrayList<>();
        this.gridSize = gridSize;
        this.direction = Direction.RIGHT;

        // Initialize snake with 3 segments
        body.add(new Point(startX, startY));
        body.add(new Point(startX - gridSize, startY));
        body.add(new Point(startX - 2 * gridSize, startY));
    }

    public void move() {
        Point head = body.get(0);
        Point newHead = new Point(
                head.x + direction.dx * gridSize,
                head.y + direction.dy * gridSize
        );

        body.add(0, newHead);
        body.remove(body.size() - 1); // Remove tail
    }

    public void grow() {
        Point tail = body.get(body.size() - 1);
        body.add(new Point(tail.x, tail.y));
    }

    public Point getHead() {
        return body.get(0);
    }

    public java.util.List<Point> getBody() {
        return new ArrayList<>(body);
    }

    public boolean checkSelfCollision() {
        Point head = body.get(0);
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void draw(Graphics2D g, Color color) {
        for (int i = 0; i < body.size(); i++) {
            Point segment = body.get(i);
            
            if (i == 0) {
                // Head - brighter
                g.setColor(color.brighter());
            } else {
                g.setColor(color);
            }
            
            g.fillRoundRect(segment.x + 1, segment.y + 1, 
                          gridSize - 2, gridSize - 2, 5, 5);
            
            // Add border
            g.setColor(color.darker());
            g.drawRoundRect(segment.x + 1, segment.y + 1, 
                          gridSize - 2, gridSize - 2, 5, 5);
        }
    }
}