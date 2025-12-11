package com.snakegame.ui;

import com.snakegame.GamePanel;
import com.snakegame.firebase.FirestoreService;
import com.snakegame.models.Difficulty;
import com.snakegame.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class DashboardScreen extends JPanel {
    private JFrame parentFrame;
    private User currentUser;
    private FirestoreService firestoreService;
    private LeaderboardPanel leaderboardPanel;
    
    // Modern UI Colors - matching game theme
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color ACCENT_PURPLE = new Color(150, 100, 255);
    private final Color ACCENT_RED = new Color(255, 100, 100);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170);
    
    // Animation
    private float pulseAnimation = 0f;
    private Timer animationTimer;
    
    public DashboardScreen(JFrame parentFrame, User currentUser) {
        this.parentFrame = parentFrame;
        this.currentUser = currentUser;
        this.firestoreService = new FirestoreService();
        
        setLayout(new BorderLayout());
        setBackground(DARK_BG);
        
        initComponents();
        startAnimation();
    }
    
    private void startAnimation() {
        animationTimer = new Timer(50, e -> {
            pulseAnimation += 0.05f;
            if (pulseAnimation > Math.PI * 2) {
                pulseAnimation = 0f;
            }
            repaint();
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, DARK_BG,
            getWidth(), getHeight(), new Color(20, 20, 35)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Animated background pattern
        g2d.setColor(new Color(255, 255, 255, 3));
        for (int i = 0; i < getWidth(); i += 100) {
            for (int j = 0; j < getHeight(); j += 100) {
                float offset = (float) Math.sin(pulseAnimation + (i + j) * 0.01);
                int alpha = (int) (5 + 3 * offset);
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.fillOval(i, j, 3, 3);
            }
        }
    }
    
    private void initComponents() {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout(25, 0));
        mainPanel.setBorder(new EmptyBorder(20, 30, 30, 30));
        
        // Left side - User profile and quick play
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        JPanel profileCard = createProfileCard();
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(profileCard);
        
        leftPanel.add(Box.createVerticalStrut(15));
        
        JPanel quickPlayCard = createQuickPlayCard();
        quickPlayCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(quickPlayCard);
        
        leftPanel.add(Box.createVerticalStrut(15));
        
        JPanel statsCard = createStatsCard();
        statsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(statsCard);
        
        leftPanel.add(Box.createVerticalGlue()); // Push content to top
        
        // Right side - Leaderboard
        leaderboardPanel = new LeaderboardPanel(firestoreService);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(leaderboardPanel, BorderLayout.CENTER);
        
        // Add scroll pane for the main content
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 25, 40),
                    0, getHeight(), new Color(30, 30, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Bottom border
                g2d.setColor(ACCENT_BLUE);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        
        header.setOpaque(false);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        // Left side - Logo and title
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftSide.setOpaque(false);
        
        JLabel logoLabel = new JLabel("🐍");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        leftSide.add(logoLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("SNAKE GAME");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_GREEN);
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        titlePanel.add(subtitleLabel);
        
        leftSide.add(titlePanel);
        
        // Right side - Logout button
        JButton logoutButton = createModernButton("LOGOUT", ACCENT_RED, 110, 40);
        logoutButton.addActionListener(e -> handleLogout());
        
        header.add(leftSide, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createProfileCard() {
        JPanel card = createModernCard(450, 180);
        card.setLayout(new BorderLayout(15, 0));
        
        // Avatar section
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Avatar circle with glow
                int size = 100;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Glow effect
                float pulse = (float) (0.8f + 0.2f * Math.sin(pulseAnimation));
                int glowSize = (int) (size + 12 * pulse);
                int glowOffset = (size - glowSize) / 2;
                
                g2d.setColor(new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 30));
                g2d.fillOval(x + glowOffset, y + glowOffset, glowSize, glowSize);
                
                // Avatar background gradient
                GradientPaint avatarGradient = new GradientPaint(
                    x, y, ACCENT_GREEN,
                    x + size, y + size, ACCENT_BLUE
                );
                g2d.setPaint(avatarGradient);
                g2d.fillOval(x, y, size, size);
                
                // Border
                g2d.setColor(TEXT_PRIMARY);
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawOval(x, y, size, size);
                
                // User initial
                g2d.setColor(DARK_BG);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
                String initial = currentUser.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (size - fm.stringWidth(initial)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(initial, textX, textY);
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(120, 130));
        
        // Info section
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel usernameLabel = new JLabel(currentUser.getUsername());
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        usernameLabel.setForeground(TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(usernameLabel);
        
        infoPanel.add(Box.createVerticalStrut(8));
        
        String emailText = currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() 
            ? currentUser.getEmail() : "No email provided";
        JLabel emailLabel = new JLabel(emailText);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(emailLabel);
        
        infoPanel.add(Box.createVerticalStrut(18));
        
        // High score badge
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badgePanel.setOpaque(false);
        badgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel trophyLabel = new JLabel("🏆");
        trophyLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        badgePanel.add(trophyLabel);
        
        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        
        JLabel scoreLabel = new JLabel(String.valueOf(currentUser.getHighScore()));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        scoreLabel.setForeground(ACCENT_GREEN);
        scorePanel.add(scoreLabel);
        
        JLabel scoreLabelText = new JLabel("High Score");
        scoreLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        scoreLabelText.setForeground(TEXT_SECONDARY);
        scorePanel.add(scoreLabelText);
        
        badgePanel.add(scorePanel);
        infoPanel.add(badgePanel);
        
        card.add(avatarPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createQuickPlayCard() {
        JPanel card = createModernCard(450, 300);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("⚡ QUICK PLAY");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(4));
        
        JLabel subtitleLabel = new JLabel("Choose your difficulty");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitleLabel);
        
        card.add(Box.createVerticalStrut(20));
        
        // Difficulty buttons with modern styling (matching login)
        Difficulty[] difficulties = {Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD};
        
        for (int i = 0; i < difficulties.length; i++) {
            Difficulty diff = difficulties[i];
            JButton diffButton = createDifficultyButton(diff);
            diffButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            diffButton.addActionListener(e -> startGame(diff));
            card.add(diffButton);
            if (i < difficulties.length - 1) {
                card.add(Box.createVerticalStrut(12));
            }
        }
        
        return card;
    }
    
    private JButton createDifficultyButton(Difficulty difficulty) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with hover effect
                Color bgColor;
                if (getModel().isPressed()) {
                    bgColor = difficulty.getColor().darker();
                } else if (getModel().isRollover()) {
                    bgColor = difficulty.getColor().brighter();
                } else {
                    bgColor = difficulty.getColor();
                }
                
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Content
                g2d.setColor(DARK_BG);
                
                // Icon
                String icon = difficulty == Difficulty.EASY ? "🟢" : 
                             difficulty == Difficulty.MEDIUM ? "🟡" : "🔴";
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                g2d.drawString(icon, 18, 35);
                
                // Title
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 17));
                g2d.drawString(difficulty.getDisplayName().toUpperCase(), 55, 32);
                
                // Details
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String details = "Speed: " + difficulty.getSpeed() + " • Multiplier: " + difficulty.getScoreMultiplier() + "x";
                g2d.drawString(details, 55, 48);
            }
        };
        
        button.setPreferredSize(new Dimension(400, 65));
        button.setMaximumSize(new Dimension(400, 65));
        button.setMinimumSize(new Dimension(400, 65));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private JPanel createStatsCard() {
        JPanel card = createModernCard(450, 130);
        card.setLayout(new GridLayout(1, 2, 20, 0));
        
        // Games played
        JPanel gamesPanel = createStatPanel("🎮", String.valueOf(currentUser.getTotalGamesPlayed()), "Games Played");
        card.add(gamesPanel);
        
        // Member since
        JPanel memberPanel = createStatPanel("📅", "2024", "Member Since");
        card.add(memberPanel);
        
        return card;
    }
    
    private JPanel createStatPanel(String emoji, String value, String label) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(emojiLabel);
        
        panel.add(Box.createVerticalStrut(6));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(valueLabel);
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelText.setForeground(TEXT_SECONDARY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelText);
        
        return panel;
    }
    
    private JPanel createModernCard(int width, int height) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 70));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Card background gradient
                GradientPaint cardGradient = new GradientPaint(
                    0, 0, CARD_BG,
                    0, getHeight(), new Color(30, 30, 50)
                );
                g2d.setPaint(cardGradient);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Border
                g2d.setColor(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 6, getHeight() - 6, 20, 20);
            }
        };
        
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, height));
        card.setMaximumSize(new Dimension(width, height));
        card.setMinimumSize(new Dimension(width, height));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        return card;
    }
    
    private JButton createModernButton(String text, Color accentColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with hover effects (matching login screen)
                if (getModel().isPressed()) {
                    g2d.setColor(accentColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(accentColor.brighter());
                } else {
                    g2d.setColor(accentColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Text
                g2d.setColor(DARK_BG);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void startGame(Difficulty difficulty) {
        animationTimer.stop();
        
        JFrame gameFrame = new JFrame("🐍 Snake Game - " + difficulty.getDisplayName());
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setResizable(false);
        
        GamePanel gamePanel = new GamePanel(currentUser, difficulty, firestoreService);
        gameFrame.add(gamePanel);
        
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(parentFrame);
        gameFrame.setVisible(true);
        
        // Refresh dashboard when game window closes
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                startAnimation();
                refreshUserData();
            }
        });
    }
    
    private void refreshUserData() {
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return firestoreService.getUser(currentUser.getUserId());
            }
            
            @Override
            protected void done() {
                try {
                    User updatedUser = get();
                    if (updatedUser != null) {
                        currentUser = updatedUser;
                        leaderboardPanel.refreshLeaderboard();
                        refreshDashboard();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to refresh user data: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void refreshDashboard() {
        animationTimer.stop();
        parentFrame.getContentPane().removeAll();
        DashboardScreen newDashboard = new DashboardScreen(parentFrame, currentUser);
        parentFrame.add(newDashboard);
        parentFrame.revalidate();
        parentFrame.repaint();
    }
    
    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            animationTimer.stop();
            parentFrame.getContentPane().removeAll();
            parentFrame.setSize(600, 700);
            parentFrame.add(new LoginScreen(parentFrame));
            parentFrame.setLocationRelativeTo(null);
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }
}