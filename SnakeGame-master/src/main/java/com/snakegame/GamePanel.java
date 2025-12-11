package com.snakegame;

import com.snakegame.api.BananaAPIService;
import com.snakegame.firebase.FirestoreService;
import com.snakegame.models.Difficulty;
import com.snakegame.models.Score;
import com.snakegame.models.User;
import com.snakegame.ui.QuestionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GamePanel extends JPanel {
    private static final String CONFIG_PATH = "config.json";
    
    private final int GRID_SIZE;
    private final int DELAY;
    private final int WIDTH;
    private final int HEIGHT;
    
    private final Color backgroundColor;
    private final Color snakeColor;
    private final Color foodColor;
    private final Color gridColor;
    
    // Modern UI Colors
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170);
    
    // Firebase fields
    private final User currentUser;
    private final Difficulty difficulty;
    private final FirestoreService firestoreService;
    private boolean scoreSaved = false;
    
    private Snake snake;
    private Food food;
    private GameEngine gameEngine;
    private Timer timer;
    private boolean paused = false;
    private boolean running = true;
    
    // Animation
    private float pulseAnimation = 0f;
    private Timer animationTimer;
    
    // Special food variables
    private boolean specialFoodActive = false;
    private int specialFoodX;
    private int specialFoodY;
    private final Color SPECIAL_FOOD_COLOR = new Color(255, 215, 0); // Gold color
    private BananaAPIService bananaAPI;
    private int specialFoodChance = 80; // 80% chance to spawn special food
    
    // Grid dimensions
    private int GRID_WIDTH;
    private int GRID_HEIGHT;
    private int CELL_SIZE;
    
    // Current score
    private int score = 0;

    // Countdown fields
    private boolean countdownActive = false;
    private int countdownValue = 3;
    private Timer countdownTimer;

    public GamePanel(User currentUser, Difficulty difficulty, FirestoreService firestoreService) {
        this.currentUser = currentUser;
        this.difficulty = difficulty;
        this.firestoreService = firestoreService;
        
        // Initialize config
        int gridSize = 25;
        int delay = 150;
        int width = 900;
        int height = 650;
        Color bgColor = DARK_BG;
        Color sColor = ACCENT_GREEN;
        Color fColor = new Color(255, 100, 100);
        Color gColor = new Color(30, 30, 45);
        
        try {
            ConfigLoader config = new ConfigLoader(CONFIG_PATH);
            gridSize = config.getGridSize();
            int baseSpeed = difficulty.getSpeed();
            delay = 1000 / baseSpeed;
            width = config.getScreenWidth();
            height = config.getScreenHeight();
            
            bgColor = DARK_BG;
            sColor = ACCENT_GREEN;
            fColor = new Color(255, 100, 100);
            gColor = new Color(30, 30, 45);
            
            System.out.println("✓ Modern UI loaded");
        } catch (Exception e) {
            delay = 1000 / difficulty.getSpeed();
        }
        
        this.GRID_SIZE = gridSize;
        this.CELL_SIZE = gridSize;
        this.DELAY = delay;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.GRID_WIDTH = width / gridSize;
        this.GRID_HEIGHT = height / gridSize;
        this.backgroundColor = bgColor;
        this.snakeColor = sColor;
        this.foodColor = fColor;
        this.gridColor = gColor;
        
        this.bananaAPI = new BananaAPIService();
        
        initializePanel();
    }
    
    private void initializePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(backgroundColor);
        setFocusable(true);
        requestFocusInWindow();

        initGame();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        timer = new Timer(DELAY, e -> {
            if (!paused && running && !countdownActive) {
                gameEngine.update(snake, food);
                checkCollisions();
                repaint();
                
                if (gameEngine.isGameOver() && !scoreSaved) {
                    running = false;
                    saveScore();
                }
            }
        });
        timer.start();
        
        // Animation timer for smooth effects
        animationTimer = new Timer(50, e -> {
            pulseAnimation += 0.1f;
            if (pulseAnimation > Math.PI * 2) {
                pulseAnimation = 0f;
            }
            repaint();
        });
        animationTimer.start();
    }
    
    private void saveScore() {
        scoreSaved = true;
        int baseScore = gameEngine.getScore();
        int finalScore = difficulty.calculateScore(baseScore);
        
        Score scoreObj = new Score();
        scoreObj.setUserId(currentUser.getUserId());
        scoreObj.setUserName(currentUser.getUsername());
        scoreObj.setAvatarUrl(currentUser.getAvatarUrl());
        scoreObj.setScore(finalScore);
        scoreObj.setDifficulty(difficulty);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (firestoreService != null) {
                    firestoreService.saveScore(scoreObj);
                }
                return null;
            }
            
            @Override
            protected void done() {
                System.out.println("✅ Score saved: " + finalScore);
            }
        }.execute();
    }

    private void initGame() {
        int centerX = (WIDTH / GRID_SIZE) / 2 * GRID_SIZE;
        int centerY = (HEIGHT / GRID_SIZE) / 2 * GRID_SIZE;
        snake = new Snake(centerX, centerY, GRID_SIZE);
        food = new Food(WIDTH, HEIGHT, GRID_SIZE);
        gameEngine = new GameEngine(WIDTH, HEIGHT, GRID_SIZE);
        paused = false;
        running = true;
        scoreSaved = false;
        score = 0;
        specialFoodActive = false;
        spawnFood();
    }

    private void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_SPACE) {
            if (paused) {
                // Start countdown when unpausing
                startCountdown();
            } else {
                // Pause immediately
                paused = true;
            }
            return;
        }
        
        if (key == KeyEvent.VK_R && gameEngine.isGameOver()) {
            initGame();
            timer.restart();
            return;
        }

        if (!paused && running && !countdownActive) {
            Direction currentDir = snake.getDirection();
            switch (key) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (currentDir != Direction.DOWN) {
                        snake.setDirection(Direction.UP);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (currentDir != Direction.UP) {
                        snake.setDirection(Direction.DOWN);
                    }
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (currentDir != Direction.RIGHT) {
                        snake.setDirection(Direction.LEFT);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (currentDir != Direction.LEFT) {
                        snake.setDirection(Direction.RIGHT);
                    }
                    break;
            }
        }
    }

    private void spawnFood() {
        Random rand = new Random();
        
        // Only spawn normal food if special food is already active
        if (specialFoodActive) {
            System.out.println("🍎 Special food already active, spawning normal food");
            food.spawn();
            return;
        }
        
        // Decide if this should be special food (80% chance)
        int roll = rand.nextInt(100);
        System.out.println("🎲 Food spawn roll: " + roll + " (threshold: " + specialFoodChance + ")");
        
        if (roll < specialFoodChance) {
            System.out.println("✅ Rolling for special food... SUCCESS!");
            spawnSpecialFood();
        } else {
            System.out.println("🍎 Rolling for special food... FAILED, spawning normal food");
            food.spawn();
        }
    }

    private void spawnSpecialFood() {
        Random rand = new Random();
        boolean validPosition = false;
        int attempts = 0;
        int maxAttempts = 100;
        
        while (!validPosition && attempts < maxAttempts) {
            attempts++;
            specialFoodX = rand.nextInt(GRID_WIDTH);
            specialFoodY = rand.nextInt(GRID_HEIGHT);
            
            validPosition = true;
            
            // Check against snake body
            for (Point segment : snake.getBody()) {
                if (segment.x / GRID_SIZE == specialFoodX && segment.y / GRID_SIZE == specialFoodY) {
                    validPosition = false;
                    break;
                }
            }
            
            // Also check it doesn't overlap with normal food
            if (validPosition) {
                Point foodPos = food.getPosition();
                if (foodPos.x / GRID_SIZE == specialFoodX && foodPos.y / GRID_SIZE == specialFoodY) {
                    validPosition = false;
                }
            }
        }
        
        if (validPosition) {
            specialFoodActive = true;
            System.out.println("🍌 *** SPECIAL FOOD SPAWNED at (" + specialFoodX + ", " + specialFoodY + ") after " + attempts + " attempts ***");
            System.out.println("🍌 Special food will remain until collected!");
        } else {
            System.err.println("⚠️ Failed to spawn special food after " + maxAttempts + " attempts, spawning normal food instead");
            specialFoodActive = false;
            food.spawn();
        }
    }

    private void checkCollisions() {
        java.util.List<Point> body = snake.getBody();
        if (body.isEmpty()) return;
        
        Point head = body.get(0);
        int headGridX = head.x / GRID_SIZE;
        int headGridY = head.y / GRID_SIZE;
        
        // Check special food collision first (priority)
        if (specialFoodActive && headGridX == specialFoodX && headGridY == specialFoodY) {
            System.out.println("🎉 *** SPECIAL FOOD COLLECTED! ***");
            handleSpecialFoodCollision();
            return; // Don't check normal food
        }
        
        // Normal food collision
        Point foodPos = food.getPosition();
        int foodGridX = foodPos.x / GRID_SIZE;
        int foodGridY = foodPos.y / GRID_SIZE;
        
        if (headGridX == foodGridX && headGridY == foodGridY) {
            System.out.println("🍎 Normal food collected");
            
            // Get points per food from difficulty
            int points = 10; // Default
            switch (difficulty) {
                case EASY:
                    points = 10;
                    break;
                case MEDIUM:
                    points = 15;
                    break;
                case HARD:
                    points = 20;
                    break;
            }
            
            score += points;
            gameEngine.addScore(points);
            snake.grow();
            
            // IMPORTANT: Spawn new food after eating
            spawnFood();
            System.out.println("📊 Score: " + score + " (+" + points + " points)");
        }
    }

    private void startCountdown() {
        countdownActive = true;
        countdownValue = 3;
        paused = true; // Keep paused during countdown
        
        System.out.println("⏱️ Starting countdown...");
        
        countdownTimer = new Timer(1000, e -> {
            countdownValue--;
            repaint();
            
            if (countdownValue <= 0) {
                countdownTimer.stop();
                countdownActive = false;
                paused = false;
                System.out.println("🎮 GO! Game resumed");
            } else {
                System.out.println("⏱️ " + countdownValue);
            }
        });
        
        countdownTimer.start();
    }

    private void handleSpecialFoodCollision() {
        specialFoodActive = false;
        paused = true;
        timer.stop();
        
        System.out.println("🍌 Special food collected! Fetching question...");
        
        // Fetch question in background
        SwingWorker<BananaAPIService.Question, Void> worker = new SwingWorker<>() {
            @Override
            protected BananaAPIService.Question doInBackground() {
                return bananaAPI.fetchQuestion();
            }
            
            @Override
            protected void done() {
                try {
                    BananaAPIService.Question question = get();
                    
                    if (question != null) {
                        // Show question dialog
                        Window window = SwingUtilities.getWindowAncestor(GamePanel.this);
                        JFrame frame = window instanceof JFrame ? (JFrame) window : null;
                        
                        QuestionDialog dialog = new QuestionDialog(frame, question);
                        dialog.setVisible(true);
                        
                        // Check if answered correctly
                        if (dialog.isAnsweredCorrectly()) {
                            score += 10;
                            gameEngine.addScore(10);
                            System.out.println("✅ Correct answer! +10 bonus points");
                        } else {
                            System.out.println("❌ Wrong answer, no bonus");
                        }
                    } else {
                        System.err.println("❌ Failed to fetch question, continuing game");
                    }
                    
                    // Start countdown before resuming
                    timer.start();
                    spawnFood();
                    startCountdown(); // Add countdown here!
                    
                } catch (Exception e) {
                    System.err.println("❌ Error showing question: " + e.getMessage());
                    e.printStackTrace();
                    timer.start();
                    spawnFood();
                    startCountdown(); // Add countdown here too!
                }
            }
        };
        
        worker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        drawModernBackground(g2d);
        
        if (running) {
            // Draw grid
            drawModernGrid(g2d);
            
            // Draw normal food
            drawModernFood(g2d);
            
            // Draw special food if active
            if (specialFoodActive) {
                drawSpecialFood(g2d);
            }
            
            // Draw snake
            drawModernSnake(g2d);
            
            // Draw HUD
            drawModernHUD(g2d);
            
            // Draw countdown overlay if active
            if (countdownActive) {
                drawCountdown(g2d);
            }
            // Draw pause overlay if paused (but not during countdown)
            else if (paused) {
                drawModernPauseScreen(g2d);
            }
            
        } else if (gameEngine.isGameOver()) {
            drawModernGameOver(g2d);
        }
    }
    
    private void drawSpecialFood(Graphics2D g) {
        // Pulsing effect
        float pulse = (float) (0.8 + 0.2 * Math.sin(System.currentTimeMillis() / 200.0));
        
        int size = (int)(CELL_SIZE * 1.2); // Slightly larger
        int offset = (CELL_SIZE - size) / 2;
        
        // Glow effect (multiple layers for stronger glow)
        for (int i = 3; i > 0; i--) {
            g.setColor(new Color(255, 215, 0, 20 * i));
            g.fillOval(
                specialFoodX * CELL_SIZE + offset - (i * 5),
                specialFoodY * CELL_SIZE + offset - (i * 5),
                size + (i * 10), size + (i * 10)
            );
        }
        
        // Main food with pulsing
        g.setColor(new Color(
            (int)(SPECIAL_FOOD_COLOR.getRed() * pulse),
            (int)(SPECIAL_FOOD_COLOR.getGreen() * pulse),
            (int)(SPECIAL_FOOD_COLOR.getBlue() * pulse)
        ));
        g.fillOval(
            specialFoodX * CELL_SIZE + offset,
            specialFoodY * CELL_SIZE + offset,
            size, size
        );
        
        // Draw banana emoji
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, CELL_SIZE / 2));
        g.drawString("🍌", 
            specialFoodX * CELL_SIZE + CELL_SIZE / 4,
            specialFoodY * CELL_SIZE + CELL_SIZE * 3 / 4
        );
    }

    private void drawModernBackground(Graphics2D g) {
        // Gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, DARK_BG,
            WIDTH, HEIGHT, new Color(20, 20, 35)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Subtle pattern
        g.setColor(new Color(255, 255, 255, 3));
        for (int i = 0; i < WIDTH; i += 100) {
            for (int j = 0; j < HEIGHT; j += 100) {
                g.fillOval(i, j, 2, 2);
            }
        }
    }

    private void drawModernGrid(Graphics2D g) {
        g.setColor(gridColor);
        g.setStroke(new BasicStroke(1f));
        
        // Draw subtle grid
        for (int i = GRID_SIZE; i < WIDTH; i += GRID_SIZE) {
            g.drawLine(i, 0, i, HEIGHT);
        }
        for (int i = GRID_SIZE; i < HEIGHT; i += GRID_SIZE) {
            g.drawLine(0, i, WIDTH, i);
        }
    }

    private void drawModernFood(Graphics2D g) {
        Point foodPos = food.getPosition();
        
        // Pulsing glow effect
        float pulse = (float) (0.8f + 0.2f * Math.sin(pulseAnimation));
        int glowSize = (int) (GRID_SIZE * pulse);
        int glowOffset = (GRID_SIZE - glowSize) / 2;
        
        // Outer glow
        g.setColor(new Color(255, 100, 100, 30));
        g.fillOval(
            foodPos.x + glowOffset - 5, 
            foodPos.y + glowOffset - 5, 
            glowSize + 10, 
            glowSize + 10
        );
        
        // Main food
        GradientPaint foodGradient = new GradientPaint(
            foodPos.x, foodPos.y, new Color(255, 120, 120),
            foodPos.x + GRID_SIZE, foodPos.y + GRID_SIZE, new Color(255, 50, 50)
        );
        g.setPaint(foodGradient);
        g.fillRoundRect(
            foodPos.x + 2, 
            foodPos.y + 2, 
            GRID_SIZE - 4, 
            GRID_SIZE - 4, 
            GRID_SIZE / 2, 
            GRID_SIZE / 2
        );
        
        // Highlight
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(
            foodPos.x + GRID_SIZE / 3, 
            foodPos.y + GRID_SIZE / 4, 
            GRID_SIZE / 4, 
            GRID_SIZE / 4
        );
    }

    private void drawModernSnake(Graphics2D g) {
        java.util.List<Point> body = snake.getBody();
        
        for (int i = 0; i < body.size(); i++) {
            Point segment = body.get(i);
            
            if (i == 0) {
                // Head with gradient
                GradientPaint headGradient = new GradientPaint(
                    segment.x, segment.y, ACCENT_GREEN.brighter(),
                    segment.x + GRID_SIZE, segment.y + GRID_SIZE, ACCENT_GREEN
                );
                g.setPaint(headGradient);
                g.fillRoundRect(
                    segment.x + 1, 
                    segment.y + 1, 
                    GRID_SIZE - 2, 
                    GRID_SIZE - 2, 
                    12, 
                    12
                );
                
                // Eyes
                g.setColor(DARK_BG);
                Direction dir = snake.getDirection();
                int eyeOffset = GRID_SIZE / 4;
                int eyeSize = GRID_SIZE / 5;
                
                if (dir == Direction.UP) {
                    g.fillOval(segment.x + eyeOffset, segment.y + eyeOffset, eyeSize, eyeSize);
                    g.fillOval(segment.x + GRID_SIZE - eyeOffset - eyeSize, segment.y + eyeOffset, eyeSize, eyeSize);
                } else if (dir == Direction.DOWN) {
                    g.fillOval(segment.x + eyeOffset, segment.y + GRID_SIZE - eyeOffset - eyeSize, eyeSize, eyeSize);
                    g.fillOval(segment.x + GRID_SIZE - eyeOffset - eyeSize, segment.y + GRID_SIZE - eyeOffset - eyeSize, eyeSize, eyeSize);
                } else if (dir == Direction.LEFT) {
                    g.fillOval(segment.x + eyeOffset, segment.y + eyeOffset, eyeSize, eyeSize);
                    g.fillOval(segment.x + eyeOffset, segment.y + GRID_SIZE - eyeOffset - eyeSize, eyeSize, eyeSize);
                } else {
                    g.fillOval(segment.x + GRID_SIZE - eyeOffset - eyeSize, segment.y + eyeOffset, eyeSize, eyeSize);
                    g.fillOval(segment.x + GRID_SIZE - eyeOffset - eyeSize, segment.y + GRID_SIZE - eyeOffset - eyeSize, eyeSize, eyeSize);
                }
                
                // Head glow
                g.setColor(new Color(0, 255, 150, 20));
                g.fillRoundRect(
                    segment.x - 2, 
                    segment.y - 2, 
                    GRID_SIZE + 4, 
                    GRID_SIZE + 4, 
                    14, 
                    14
                );
            } else {
                // Body segments with fade effect
                float alpha = 1.0f - (i * 0.02f);
                if (alpha < 0.5f) alpha = 0.5f;
                
                Color bodyColor = new Color(
                    ACCENT_GREEN.getRed(),
                    ACCENT_GREEN.getGreen(),
                    ACCENT_GREEN.getBlue(),
                    (int) (255 * alpha)
                );
                
                g.setColor(bodyColor);
                g.fillRoundRect(
                    segment.x + 2, 
                    segment.y + 2, 
                    GRID_SIZE - 4, 
                    GRID_SIZE - 4, 
                    10, 
                    10
                );
                
                // Inner highlight
                g.setColor(new Color(255, 255, 255, (int) (30 * alpha)));
                g.fillRoundRect(
                    segment.x + 4, 
                    segment.y + 4, 
                    GRID_SIZE - 8, 
                    GRID_SIZE - 8, 
                    8, 
                    8
                );
            }
        }
    }

    private void drawModernHUD(Graphics2D g) {
        // HUD background card
        g.setColor(new Color(CARD_BG.getRed(), CARD_BG.getGreen(), CARD_BG.getBlue(), 200));
        g.fillRoundRect(15, 15, 280, 120, 20, 20);
        
        // Border glow
        g.setColor(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 100));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(15, 15, 280, 120, 20, 20);
        
        int baseScore = gameEngine.getScore();
        int displayScore = difficulty.calculateScore(baseScore);
        
        // Player name
        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g.drawString("👤 " + currentUser.getUsername(), 30, 40);
        
        // Score with icon
        g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        g.setColor(ACCENT_GREEN);
        g.drawString("" + displayScore, 30, 80);
        
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(TEXT_SECONDARY);
        g.drawString("SCORE", 30, 95);
        
        // Difficulty badge
        Color diffColor = difficulty.getColor();
        g.setColor(new Color(diffColor.getRed(), diffColor.getGreen(), diffColor.getBlue(), 150));
        g.fillRoundRect(30, 100, 100, 25, 12, 12);
        
        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        String diffText = difficulty.getDisplayName().toUpperCase() + " × " + difficulty.getScoreMultiplier();
        g.drawString(diffText, 40, 117);
        
        // Controls hint (bottom right)
        g.setColor(new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), TEXT_SECONDARY.getBlue(), 150));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("SPACE: Pause  •  R: Restart  •  🍌 = +10 pts", WIDTH - 280, HEIGHT - 15);
    }

    private void drawModernPauseScreen(Graphics2D g) {
        // Blur effect background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Pause card
        int cardWidth = 400;
        int cardHeight = 250;
        int cardX = (WIDTH - cardWidth) / 2;
        int cardY = (HEIGHT - cardHeight) / 2;
        
        // Card shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(cardX + 5, cardY + 5, cardWidth, cardHeight, 30, 30);
        
        // Card background
        GradientPaint cardGradient = new GradientPaint(
            cardX, cardY, new Color(30, 30, 50),
            cardX, cardY + cardHeight, new Color(40, 40, 60)
        );
        g.setPaint(cardGradient);
        g.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 30, 30);
        
        // Card border
        g.setColor(ACCENT_BLUE);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 30, 30);
        
        // Pause icon
        g.setColor(ACCENT_BLUE);
        int iconSize = 60;
        int iconX = cardX + (cardWidth - iconSize) / 2;
        int iconY = cardY + 40;
        g.fillRoundRect(iconX, iconY, 18, iconSize, 8, 8);
        g.fillRoundRect(iconX + 32, iconY, 18, iconSize, 8, 8);
        
        // Title
        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 42));
        String pauseText = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int textX = cardX + (cardWidth - fm.stringWidth(pauseText)) / 2;
        g.drawString(pauseText, textX, cardY + 150);
        
        // Subtitle
        g.setColor(TEXT_SECONDARY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        String subtitle = "Press SPACE to continue";
        textX = cardX + (cardWidth - g.getFontMetrics().stringWidth(subtitle)) / 2;
        g.drawString(subtitle, textX, cardY + 190);
    }

    private void drawModernGameOver(Graphics2D g) {
        int baseScore = gameEngine.getScore();
        int finalScore = difficulty.calculateScore(baseScore);
        
        // Dark overlay
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Game over card
        int cardWidth = 500;
        int cardHeight = 400;
        int cardX = (WIDTH - cardWidth) / 2;
        int cardY = (HEIGHT - cardHeight) / 2;
        
        // Card shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(cardX + 8, cardY + 8, cardWidth, cardHeight, 35, 35);
        
        // Card background
        GradientPaint cardGradient = new GradientPaint(
            cardX, cardY, new Color(35, 25, 40),
            cardX, cardY + cardHeight, new Color(25, 15, 30)
        );
        g.setPaint(cardGradient);
        g.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 35, 35);
        
        // Card border
        g.setColor(new Color(255, 100, 100));
        g.setStroke(new BasicStroke(4f));
        g.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 35, 35);
        
        // Skull icon (game over)
        g.setColor(new Color(255, 100, 100, 150));
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        String icon = "💀";
        FontMetrics fm = g.getFontMetrics();
        int iconX = cardX + (cardWidth - fm.stringWidth(icon)) / 2;
        g.drawString(icon, iconX, cardY + 90);
        
        // Game Over text
        g.setColor(new Color(255, 100, 100));
        g.setFont(new Font("Segoe UI", Font.BOLD, 52));
        String gameOverText = "GAME OVER";
        fm = g.getFontMetrics();
        int textX = cardX + (cardWidth - fm.stringWidth(gameOverText)) / 2;
        g.drawString(gameOverText, textX, cardY + 160);
        
        // Score section
        g.setColor(CARD_BG);
        g.fillRoundRect(cardX + 50, cardY + 190, cardWidth - 100, 90, 20, 20);
        
        g.setColor(ACCENT_GREEN);
        g.setFont(new Font("Segoe UI", Font.BOLD, 48));
        String scoreText = String.valueOf(finalScore);
        fm = g.getFontMetrics();
        textX = cardX + (cardWidth - fm.stringWidth(scoreText)) / 2;
        g.drawString(scoreText, textX, cardY + 240);
        
        g.setColor(TEXT_SECONDARY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        String scoreLabel = "FINAL SCORE";
        textX = cardX + (cardWidth - g.getFontMetrics().stringWidth(scoreLabel)) / 2;
        g.drawString(scoreLabel, textX, cardY + 265);
        
        // Multiplier info
        g.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        String multiplier = "(" + baseScore + " × " + difficulty.getScoreMultiplier() + ")";
        textX = cardX + (cardWidth - g.getFontMetrics().stringWidth(multiplier)) / 2;
        g.drawString(multiplier, textX, cardY + 285);
        
        // Action buttons
        g.setColor(TEXT_SECONDARY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        String closeText = "Close window to return";
        textX = cardX + (cardWidth - g.getFontMetrics().stringWidth(closeText)) / 2;
        g.drawString(closeText, textX, cardY + 330);
        
        g.setColor(ACCENT_BLUE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String restartText = "Press R to play again";
        textX = cardX + (cardWidth - g.getFontMetrics().stringWidth(restartText)) / 2;
        g.drawString(restartText, textX, cardY + 360);
    }

    private void drawCountdown(Graphics2D g) {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Countdown card
        int cardWidth = 300;
        int cardHeight = 300;
        int cardX = (WIDTH - cardWidth) / 2;
        int cardY = (HEIGHT - cardHeight) / 2;
        
        // Card shadow
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(cardX + 6, cardY + 6, cardWidth, cardHeight, 40, 40);
        
        // Card background with gradient
        GradientPaint cardGradient = new GradientPaint(
            cardX, cardY, new Color(30, 30, 50),
            cardX, cardY + cardHeight, new Color(40, 40, 65)
        );
        g.setPaint(cardGradient);
        g.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 40, 40);
        
        // Pulsing border
        float pulse = (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 200.0));
        g.setColor(new Color(
            (int)(ACCENT_GREEN.getRed() * pulse),
            (int)(ACCENT_GREEN.getGreen() * pulse),
            (int)(ACCENT_GREEN.getBlue() * pulse)
        ));
        g.setStroke(new BasicStroke(4f));
        g.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 40, 40);
        
        // Draw countdown number or "GO!"
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (countdownValue > 0) {
            // Draw number
            g.setFont(new Font("Segoe UI", Font.BOLD, 150));
            g.setColor(ACCENT_GREEN);
            
            String countText = String.valueOf(countdownValue);
            FontMetrics fm = g.getFontMetrics();
            int textX = cardX + (cardWidth - fm.stringWidth(countText)) / 2;
            int textY = cardY + (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
            
            // Glow effect
            g.setColor(new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 50));
            for (int i = 10; i > 0; i--) {
                g.drawString(countText, textX - i, textY - i);
                g.drawString(countText, textX + i, textY + i);
            }
            
            // Main number
            g.setColor(ACCENT_GREEN);
            g.drawString(countText, textX, textY);
            
        } else {
            // Draw "GO!"
            g.setFont(new Font("Segoe UI", Font.BOLD, 100));
            g.setColor(ACCENT_GREEN);
            
            String goText = "GO!";
            FontMetrics fm = g.getFontMetrics();
            int textX = cardX + (cardWidth - fm.stringWidth(goText)) / 2;
            int textY = cardY + (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
            
            // Glow effect
            g.setColor(new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 80));
            for (int i = 15; i > 0; i--) {
                g.drawString(goText, textX - i/2, textY - i/2);
                g.drawString(goText, textX + i/2, textY + i/2);
            }
            
            // Main text
            g.setColor(ACCENT_GREEN);
            g.drawString(goText, textX, textY);
            
            // Particles effect around "GO!"
            Random rand = new Random();
            g.setColor(new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 150));
            for (int i = 0; i < 20; i++) {
                int px = cardX + cardWidth/2 + rand.nextInt(200) - 100;
                int py = cardY + cardHeight/2 + rand.nextInt(200) - 100;
                g.fillOval(px, py, 4, 4);
            }
        }
        
        // Subtitle
        g.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g.setColor(TEXT_SECONDARY);
        String subtitle = "Get ready...";
        FontMetrics fm = g.getFontMetrics();
        int subtitleX = cardX + (cardWidth - fm.stringWidth(subtitle)) / 2;
        g.drawString(subtitle, subtitleX, cardY + cardHeight - 40);
    }
}