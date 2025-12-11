package com.snakegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements KeyListener, ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int GRID_SIZE = 20;

    private Snake snake;
    private Food food;
    private GameEngine gameEngine;
    private Timer gameTimer;
    private Direction nextDirection;
    private boolean gamePaused;
    private BufferedImage buffer;
    private ScoreRepository scoreRepo;
    private int speedLevel;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Initialize game components
        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        food = new Food(WIDTH, HEIGHT, GRID_SIZE);
        gameEngine = new GameEngine(snake, food, WIDTH, HEIGHT, GRID_SIZE);
        nextDirection = Direction.RIGHT;
        gamePaused = false;
        scoreRepo = new ScoreRepository();
        speedLevel = 150;

        // Setup double buffering
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Game loop timer (every 150ms)
        gameTimer = new Timer(150, this);
        gameTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw grid
        drawGrid(g2d);

        // Draw food
        drawFood(g2d);

        // Draw snake
        drawSnake(g2d);

        // Draw UI
        drawUI(g2d);

        // Draw pause message
        if (gamePaused) {
            drawPauseMessage(g2d);
        }

        g2d.dispose();
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 30));
        g2d.setStroke(new BasicStroke(1));

        for (int x = 0; x <= WIDTH; x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y <= HEIGHT; y += GRID_SIZE) {
            g2d.drawLine(0, y, WIDTH, y);
        }
    }

    private void drawSnake(Graphics2D g2d) {
        java.util.List<Point> body = snake.getBody();

        // Head - bright green
        g2d.setColor(Color.GREEN);
        Point head = body.get(0);
        g2d.fillRect(head.x, head.y, GRID_SIZE - 2, GRID_SIZE - 2);
        g2d.setColor(new Color(100, 255, 100));
        g2d.drawRect(head.x, head.y, GRID_SIZE - 2, GRID_SIZE - 2);

        // Body - darker green
        g2d.setColor(new Color(0, 180, 0));
        for (int i = 1; i < body.size(); i++) {
            Point segment = body.get(i);
            g2d.fillRect(segment.x, segment.y, GRID_SIZE - 2, GRID_SIZE - 2);
        }
    }

    private void drawFood(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        Point foodPos = food.getPosition();
        g2d.fillOval(foodPos.x + 2, foodPos.y + 2, GRID_SIZE - 4, GRID_SIZE - 4);
        g2d.setColor(new Color(255, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(foodPos.x + 2, foodPos.y + 2, GRID_SIZE - 4, GRID_SIZE - 4);
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + gameEngine.getScore(), 10, 30);
        g2d.drawString("Length: " + snake.getBody().size(), 10, 60);
        g2d.drawString("Speed: " + gameEngine.getDifficulty(), WIDTH - 150, 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("SPACE: Pause | R: Restart", 10, HEIGHT - 10);
    }

    private void drawPauseMessage(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        String pauseText = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(pauseText)) / 2;
        int y = (HEIGHT + fm.getAscent()) / 2;
        g2d.drawString(pauseText, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gamePaused) {
            // Update game state
            snake.move(nextDirection);

            // Check collisions
            if (gameEngine.checkCollisions()) {
                handleGameOver();
                return;
            }

            // Check food consumption
            if (gameEngine.checkFoodCollision()) {
                snake.grow();
                food.respawn();
                gameEngine.addScore(10);
                gameEngine.increaseDifficulty();
            }

            if (gameEngine.getDifficulty() == 2) {
                gameTimer.setDelay(120);
            } else if (gameEngine.getDifficulty() == 3) {
                gameTimer.setDelay(90);
            }
        }

        repaint();
    }

    private void handleGameOver() {
        gameTimer.stop();
        int option = JOptionPane.showConfirmDialog(
                this,
                "Game Over!\nFinal Score: " + gameEngine.getScore() + "\n\nPlay again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
        scoreRepo.saveScore("Player", gameEngine.getScore());
    }

    private void resetGame() {
        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        food = new Food(WIDTH, HEIGHT, GRID_SIZE);
        gameEngine = new GameEngine(snake, food, WIDTH, HEIGHT, GRID_SIZE);
        nextDirection = Direction.RIGHT;
        gamePaused = false;
        gameTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Direction controls
        if (key == KeyEvent.VK_UP && nextDirection != Direction.DOWN) {
            nextDirection = Direction.UP;
        } else if (key == KeyEvent.VK_DOWN && nextDirection != Direction.UP) {
            nextDirection = Direction.DOWN;
        } else if (key == KeyEvent.VK_LEFT && nextDirection != Direction.RIGHT) {
            nextDirection = Direction.LEFT;
        } else if (key == KeyEvent.VK_RIGHT && nextDirection != Direction.LEFT) {
            nextDirection = Direction.RIGHT;
        }

        // Game controls
        if (key == KeyEvent.VK_SPACE) {
            gamePaused = !gamePaused;
        } else if (key == KeyEvent.VK_R) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}


}