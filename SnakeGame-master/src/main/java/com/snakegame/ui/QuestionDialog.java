package com.snakegame.ui;

import com.snakegame.api.BananaAPIService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionDialog extends JDialog {
    private boolean answeredCorrectly = false;
    private int userAnswer = -1;
    private final int correctAnswer;
    
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_RED = new Color(255, 100, 100);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(180, 180, 200);
    private final Color INPUT_BG = new Color(40, 40, 60);

    public QuestionDialog(JFrame parent, BananaAPIService.Question question) {
        super(parent, "🍌 Bonus Question!", true);
        this.correctAnswer = question.getSolution();
        
        setSize(550, 650);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        initComponents(question.getQuestionImageUrl());
    }
    
    private void initComponents(String imageUrl) {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(8, 8, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Background
                GradientPaint gradient = new GradientPaint(
                    0, 0, CARD_BG,
                    0, getHeight(), new Color(30, 30, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Border
                g2d.setColor(ACCENT_GREEN);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, 30, 30);
            }
        };
        
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Title
        JLabel titleLabel = new JLabel("🍌 BONUS QUESTION!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel subtitleLabel = new JLabel("Answer correctly for +10 bonus points!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_PRIMARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Question Image Panel
        JPanel imagePanel = new JPanel();
        imagePanel.setOpaque(false);
        imagePanel.setMaximumSize(new Dimension(450, 300));
        imagePanel.setPreferredSize(new Dimension(450, 300));
        
        JLabel imageLabel = new JLabel("Loading question...", SwingConstants.CENTER);
        imageLabel.setForeground(TEXT_PRIMARY);
        imagePanel.add(imageLabel);
        
        mainPanel.add(imagePanel);
        
        // Load image in background
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return ImageIO.read(new URL(imageUrl));
            }
            
            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(430, 280, Image.SCALE_SMOOTH));
                    imageLabel.setIcon(icon);
                    imageLabel.setText("");
                } catch (Exception e) {
                    imageLabel.setText("❌ Failed to load question image");
                    System.err.println("Error loading question image: " + e.getMessage());
                }
            }
        }.execute();
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Instructions
        JLabel instructionLabel = new JLabel("Select the correct answer:");
        instructionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        instructionLabel.setForeground(TEXT_PRIMARY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(instructionLabel);
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Generate 4 answer options (including the correct one)
        List<Integer> options = generateOptions(correctAnswer);
        
        System.out.println("🎯 Generated answer options: " + options);
        System.out.println("✅ Correct answer: " + correctAnswer);
        
        // Create answer buttons in a 2x2 grid
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        answersPanel.setOpaque(false);
        answersPanel.setMaximumSize(new Dimension(450, 180));
        
        for (int option : options) {
            JButton answerBtn = createAnswerButton(String.valueOf(option));
            answerBtn.addActionListener(e -> {
                userAnswer = option;
                answeredCorrectly = (userAnswer == correctAnswer);
                
                // Disable all buttons
                for (Component comp : answersPanel.getComponents()) {
                    comp.setEnabled(false);
                }
                
                // Visual feedback
                if (answeredCorrectly) {
                    answerBtn.setBackground(ACCENT_GREEN);
                    answerBtn.setForeground(DARK_BG);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(this,
                            "🎉 Correct! +10 Bonus Points!",
                            "Correct Answer!",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    });
                } else {
                    answerBtn.setBackground(ACCENT_RED);
                    answerBtn.setForeground(Color.WHITE);
                    
                    // Highlight correct answer
                    for (Component comp : answersPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            JButton btn = (JButton) comp;
                            if (btn.getText().equals(String.valueOf(correctAnswer))) {
                                btn.setBackground(ACCENT_GREEN);
                                btn.setForeground(DARK_BG);
                            }
                        }
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(this,
                            "❌ Wrong! The answer was: " + correctAnswer,
                            "Incorrect",
                            JOptionPane.ERROR_MESSAGE);
                        dispose();
                    });
                }
            });
            answersPanel.add(answerBtn);
        }
        
        mainPanel.add(answersPanel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Skip button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        JButton skipButton = createButton("SKIP", ACCENT_BLUE);
        skipButton.addActionListener(e -> {
            System.out.println("⏭️ User skipped the question");
            dispose();
        });
        buttonPanel.add(skipButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    /**
     * Generate 4 options including the correct answer
     * Uses smart algorithm to create realistic wrong answers
     */
    private List<Integer> generateOptions(int correctAnswer) {
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer);
        
        Random rand = new Random();
        
        // Strategy 1: Add numbers close to the correct answer (±1, ±2, ±3)
        List<Integer> closeNumbers = new ArrayList<>();
        for (int offset : new int[]{-3, -2, -1, 1, 2, 3}) {
            int candidate = correctAnswer + offset;
            if (candidate >= 0 && candidate <= 9 && candidate != correctAnswer) {
                closeNumbers.add(candidate);
            }
        }
        
        // Strategy 2: Add some random numbers as backup
        List<Integer> randomNumbers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            if (i != correctAnswer && !closeNumbers.contains(i)) {
                randomNumbers.add(i);
            }
        }
        
        // Shuffle both lists
        Collections.shuffle(closeNumbers);
        Collections.shuffle(randomNumbers);
        
        // Fill options: prioritize close numbers, then random
        while (options.size() < 4) {
            if (!closeNumbers.isEmpty()) {
                int candidate = closeNumbers.remove(0);
                if (!options.contains(candidate)) {
                    options.add(candidate);
                }
            } else if (!randomNumbers.isEmpty()) {
                int candidate = randomNumbers.remove(0);
                if (!options.contains(candidate)) {
                    options.add(candidate);
                }
            } else {
                // Fallback: generate truly random number
                int candidate = rand.nextInt(10);
                if (!options.contains(candidate)) {
                    options.add(candidate);
                }
            }
        }
        
        // Shuffle final options so correct answer position varies
        Collections.shuffle(options);
        
        return options;
    }
    
    private JButton createAnswerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 42));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(INPUT_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 80));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Rounded corners
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 2, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalBg = button.getBackground();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(60, 60, 90));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled() && 
                    button.getBackground() != ACCENT_GREEN && 
                    button.getBackground() != ACCENT_RED) {
                    button.setBackground(originalBg);
                }
            }
        });
        
        return button;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(DARK_BG);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    public boolean isAnsweredCorrectly() {
        return answeredCorrectly;
    }
}