package com.snakegame.ui;

import com.snakegame.firebase.AuthService;
import com.snakegame.firebase.FirestoreService;
import com.snakegame.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginScreen extends JPanel {
    private JFrame parentFrame;
    private AuthService authService;
    private FirestoreService firestoreService;
    
    // Modern UI Colors - matching game theme
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color ACCENT_PURPLE = new Color(150, 100, 255);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170);
    private final Color INPUT_BG = new Color(30, 30, 45);
    private final Color BUTTON_HOVER = new Color(120, 170, 255);
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    
    // Animation
    private float pulseAnimation = 0f;
    private Timer animationTimer;

    public LoginScreen(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.authService = new AuthService();
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
        // Main container
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new GridBagLayout());
        
        // Login card
        JPanel loginCard = createLoginCard();
        mainPanel.add(loginCard);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(8, 8, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Card background with gradient
                GradientPaint cardGradient = new GradientPaint(
                    0, 0, CARD_BG,
                    0, getHeight(), new Color(30, 30, 50)
                );
                g2d.setPaint(cardGradient);
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Glowing border
                g2d.setColor(ACCENT_BLUE);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, 30, 30);
                
                super.paintComponent(g);
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(450, 550));
        
        // Logo/Icon
        JLabel logoLabel = new JLabel("🐍");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLabel);
        
        card.add(Box.createVerticalStrut(10));
        
        // Title
        JLabel titleLabel = new JLabel("SNAKE GAME");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(5));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Welcome Back!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        
        card.add(Box.createVerticalStrut(40));
        
        // Username field
        card.add(createFieldLabel("Username"));
        card.add(Box.createVerticalStrut(8));
        usernameField = createModernTextField("Enter your username");
        card.add(usernameField);
        
        card.add(Box.createVerticalStrut(20));
        
        // Password field
        card.add(createFieldLabel("Password"));
        card.add(Box.createVerticalStrut(8));
        passwordField = createModernPasswordField("Enter your password");
        card.add(passwordField);
        
        card.add(Box.createVerticalStrut(30));
        
        // Login button
        loginButton = createModernButton("LOGIN", ACCENT_GREEN);
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);
        
        card.add(Box.createVerticalStrut(15));
        
        // Register button
        registerButton = createModernButton("CREATE ACCOUNT", ACCENT_BLUE);
        registerButton.addActionListener(e -> showRegisterDialog());
        card.add(registerButton);
        
        card.add(Box.createVerticalStrut(20));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        
        // Enter key support
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        
        return card;
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                if (isFocusOwner()) {
                    g2d.setColor(ACCENT_BLUE);
                    g2d.setStroke(new BasicStroke(2f));
                } else {
                    g2d.setColor(new Color(60, 60, 80));
                    g2d.setStroke(new BasicStroke(1f));
                }
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                
                super.paintComponent(g);
            }
        };
        
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(new EmptyBorder(12, 15, 12, 15));
        field.setMaximumSize(new Dimension(350, 45));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Placeholder
        field.setText(placeholder);
        field.setForeground(TEXT_SECONDARY);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                if (isFocusOwner()) {
                    g2d.setColor(ACCENT_BLUE);
                    g2d.setStroke(new BasicStroke(2f));
                } else {
                    g2d.setColor(new Color(60, 60, 80));
                    g2d.setStroke(new BasicStroke(1f));
                }
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                
                super.paintComponent(g);
            }
        };
        
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(new EmptyBorder(12, 15, 12, 15));
        field.setMaximumSize(new Dimension(350, 45));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setEchoChar('●');
        
        return field;
    }
    
    private JButton createModernButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                if (getModel().isPressed()) {
                    g2d.setColor(accentColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(accentColor.brighter());
                } else {
                    g2d.setColor(accentColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Text
                g2d.setColor(DARK_BG);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(DARK_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(350, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        return button;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || username.equals("Enter your username")) {
            showStatus("Please enter username", new Color(255, 100, 100));
            return;
        }
        
        if (password.isEmpty()) {
            showStatus("Please enter password", new Color(255, 100, 100));
            return;
        }
        
        loginButton.setEnabled(false);
        showStatus("Logging in...", ACCENT_BLUE);
        
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        showStatus("✓ Login successful!", ACCENT_GREEN);
                        // Smooth transition
                        Timer timer = new Timer(500, e -> openDashboard(user));
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus("✗ Invalid credentials", new Color(255, 100, 100));
                        loginButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    showStatus("✗ Error: " + e.getMessage(), new Color(255, 100, 100));
                    loginButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(parentFrame, authService, firestoreService);
        dialog.setVisible(true);
        
        if (dialog.isRegistrationSuccessful()) {
            showStatus("✓ Account created! Please login", ACCENT_GREEN);
        }
    }

    private void openDashboard(User user) {
        animationTimer.stop();
        parentFrame.getContentPane().removeAll();
        
        DashboardScreen dashboard = new DashboardScreen(parentFrame, user);
        parentFrame.add(dashboard);
        parentFrame.setSize(1200, 800);
        parentFrame.setLocationRelativeTo(null);
        
        parentFrame.revalidate();
        parentFrame.repaint();
    }
}