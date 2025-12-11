package com.snakegame.ui;

import com.snakegame.firebase.FirestoreService;
import com.snakegame.models.Difficulty;
import com.snakegame.models.Score;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private FirestoreService firestoreService;
    private JComboBox<String> difficultySelector;
    private JPanel leaderboardContent;
    
    // Modern UI Colors
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color ACCENT_GOLD = new Color(255, 215, 0);
    private final Color ACCENT_SILVER = new Color(192, 192, 192);
    private final Color ACCENT_BRONZE = new Color(205, 127, 50);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170);
    
    public LeaderboardPanel(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
        
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        
        initComponents();
        loadLeaderboard(Difficulty.EASY);
    }
    
    private void initComponents() {
        // Header card
        JPanel headerCard = createHeaderCard();
        add(headerCard, BorderLayout.NORTH);
        
        // Leaderboard content with proper sizing
        leaderboardContent = new JPanel();
        leaderboardContent.setOpaque(false);
        leaderboardContent.setLayout(new BoxLayout(leaderboardContent, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(leaderboardContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Custom scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        JPanel contentWrapper = createModernCard(620, 550);
        contentWrapper.setLayout(new BorderLayout());
        contentWrapper.add(scrollPane);
        
        add(contentWrapper, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderCard() {
        JPanel card = createModernCard(620, 80);
        card.setLayout(new BorderLayout(15, 0));
        
        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("🏆 LEADERBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Top players worldwide");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        titlePanel.add(subtitleLabel);
        
        // Difficulty selector
        String[] difficulties = {"Easy", "Medium", "Hard", "Global"};
        difficultySelector = new JComboBox<>(difficulties);
        difficultySelector.setFont(new Font("Segoe UI", Font.BOLD, 12));
        difficultySelector.setPreferredSize(new Dimension(110, 35));
        difficultySelector.setFocusable(false);
        customizeComboBox(difficultySelector);
        
        difficultySelector.addActionListener(e -> {
            String selected = (String) difficultySelector.getSelectedItem();
            if ("Global".equals(selected)) {
                loadGlobalLeaderboard();
            } else {
                try {
                    Difficulty diff = Difficulty.valueOf(selected.toUpperCase());
                    loadLeaderboard(diff);
                } catch (Exception ex) {
                    displayError("Invalid difficulty selected");
                }
            }
        });
        
        card.add(titlePanel, BorderLayout.WEST);
        card.add(difficultySelector, BorderLayout.EAST);
        
        return card;
    }
    
    private void customizeComboBox(JComboBox<String> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setBorder(new EmptyBorder(6, 10, 6, 10));
                
                if (isSelected) {
                    label.setBackground(ACCENT_BLUE);
                    label.setForeground(DARK_BG);
                } else {
                    label.setBackground(CARD_BG);
                    label.setForeground(TEXT_PRIMARY);
                }
                
                return label;
            }
        });
    }
    
    /**
     * Public method to refresh the leaderboard
     * Called when user returns from a game
     */
    public void refreshLeaderboard() {
        String selected = (String) difficultySelector.getSelectedItem();
        if ("Global".equals(selected)) {
            loadGlobalLeaderboard();
        } else {
            try {
                Difficulty diff = Difficulty.valueOf(selected.toUpperCase());
                loadLeaderboard(diff);
            } catch (Exception e) {
                displayError("Failed to refresh leaderboard");
            }
        }
    }
    
    private void loadLeaderboard(Difficulty difficulty) {
        leaderboardContent.removeAll();
        
        JPanel loadingPanel = new JPanel();
        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        
        JLabel loadingLabel = new JLabel("⏳ Loading leaderboard...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(TEXT_SECONDARY);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loadingPanel.add(Box.createVerticalStrut(50));
        loadingPanel.add(loadingLabel);
        loadingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leaderboardContent.add(loadingPanel);
        leaderboardContent.revalidate();
        leaderboardContent.repaint();
        
        new SwingWorker<List<Score>, Void>() {
            @Override
            protected List<Score> doInBackground() throws Exception {
                try {
                    return firestoreService.getLeaderboard(difficulty, 20);
                } catch (Exception e) {
                    System.err.println("Error loading leaderboard: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Score> scores = get();
                    if (scores != null) {
                        displayLeaderboard(scores);
                    } else {
                        displayError("No data available");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to display leaderboard: " + e.getMessage());
                    e.printStackTrace();
                    displayError("Failed to load leaderboard");
                }
            }
        }.execute();
    }
    
    private void loadGlobalLeaderboard() {
        leaderboardContent.removeAll();
        
        JPanel loadingPanel = new JPanel();
        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        
        JLabel loadingLabel = new JLabel("⏳ Loading global leaderboard...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(TEXT_SECONDARY);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loadingPanel.add(Box.createVerticalStrut(50));
        loadingPanel.add(loadingLabel);
        loadingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leaderboardContent.add(loadingPanel);
        leaderboardContent.revalidate();
        leaderboardContent.repaint();
        
        new SwingWorker<List<Score>, Void>() {
            @Override
            protected List<Score> doInBackground() throws Exception {
                try {
                    return firestoreService.getGlobalLeaderboard(20);
                } catch (Exception e) {
                    System.err.println("Error loading global leaderboard: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Score> scores = get();
                    if (scores != null) {
                        displayLeaderboard(scores);
                    } else {
                        displayError("No data available");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to display global leaderboard: " + e.getMessage());
                    e.printStackTrace();
                    displayError("Failed to load global leaderboard");
                }
            }
        }.execute();
    }
    
    private void displayLeaderboard(List<Score> scores) {
        leaderboardContent.removeAll();
        
        if (scores == null || scores.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            
            JLabel emptyLabel = new JLabel("🎮 No scores yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            emptyLabel.setForeground(TEXT_PRIMARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel subLabel = new JLabel("Be the first to play!");
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subLabel.setForeground(TEXT_SECONDARY);
            subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            emptyPanel.add(Box.createVerticalStrut(50));
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(subLabel);
            emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            leaderboardContent.add(emptyPanel);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                Score score = scores.get(i);
                JPanel entryPanel = createLeaderboardEntry(score, i + 1);
                entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                leaderboardContent.add(entryPanel);
                if (i < scores.size() - 1) {
                    leaderboardContent.add(Box.createVerticalStrut(8));
                }
            }
        }
        
        leaderboardContent.revalidate();
        leaderboardContent.repaint();
    }
    
    private JPanel createLeaderboardEntry(Score score, int rank) {
        JPanel entry = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                Color bgColor = new Color(30, 30, 45);
                if (rank == 1) bgColor = new Color(40, 35, 30); // Gold tint
                else if (rank == 2) bgColor = new Color(35, 35, 40); // Silver tint
                else if (rank == 3) bgColor = new Color(38, 30, 30); // Bronze tint
                
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border for top 3
                if (rank <= 3) {
                    Color borderColor = rank == 1 ? ACCENT_GOLD : 
                                      rank == 2 ? ACCENT_SILVER : ACCENT_BRONZE;
                    g2d.setColor(borderColor);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }
            }
        };
        
        entry.setOpaque(false);
        entry.setLayout(new BorderLayout(12, 0));
        entry.setPreferredSize(new Dimension(560, 55));
        entry.setMaximumSize(new Dimension(560, 55));
        entry.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        // Rank
        JLabel rankLabel = new JLabel();
        if (rank <= 3) {
            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : "🥉";
            rankLabel.setText(medal);
            rankLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        } else {
            rankLabel.setText("#" + rank);
            rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            rankLabel.setForeground(TEXT_SECONDARY);
        }
        rankLabel.setPreferredSize(new Dimension(45, 40));
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Player info
        JPanel playerPanel = new JPanel();
        playerPanel.setOpaque(false);
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        
        JLabel nameLabel = new JLabel(score.getUserName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);
        playerPanel.add(nameLabel);
        
        JLabel difficultyLabel = new JLabel(score.getDifficulty().getDisplayName());
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        difficultyLabel.setForeground(score.getDifficulty().getColor());
        playerPanel.add(difficultyLabel);
        
        // Score
        JLabel scoreLabel = new JLabel(String.valueOf(score.getScore()));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreLabel.setForeground(ACCENT_GREEN);
        scoreLabel.setPreferredSize(new Dimension(90, 40));
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        entry.add(rankLabel, BorderLayout.WEST);
        entry.add(playerPanel, BorderLayout.CENTER);
        entry.add(scoreLabel, BorderLayout.EAST);
        
        return entry;
    }
    
    private void displayError(String message) {
        leaderboardContent.removeAll();
        
        JPanel errorPanel = new JPanel();
        errorPanel.setOpaque(false);
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        
        JLabel errorLabel = new JLabel("❌ " + message);
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        errorLabel.setForeground(new Color(255, 100, 100));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel retryLabel = new JLabel("Please check your connection");
        retryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        retryLabel.setForeground(TEXT_SECONDARY);
        retryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        errorPanel.add(Box.createVerticalStrut(50));
        errorPanel.add(errorLabel);
        errorPanel.add(Box.createVerticalStrut(8));
        errorPanel.add(retryLabel);
        errorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leaderboardContent.add(errorPanel);
        
        leaderboardContent.revalidate();
        leaderboardContent.repaint();
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
                
                // Card background
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
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        return card;
    }
    
    // Custom ScrollBar UI
    private class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(100, 150, 255, 100);
            this.trackColor = new Color(30, 30, 45);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 4, thumbBounds.height, 6, 6);
        }
    }
}