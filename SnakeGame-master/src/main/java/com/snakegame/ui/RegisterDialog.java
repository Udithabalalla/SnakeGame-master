package com.snakegame.ui;

import com.snakegame.firebase.AuthService;
import com.snakegame.firebase.FirestoreService;
import com.snakegame.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterDialog extends JDialog {
    private AuthService authService;
    private FirestoreService firestoreService;
    private boolean registrationSuccessful = false;
    
    // Modern UI Colors
    private final Color DARK_BG = new Color(15, 15, 25);
    private final Color CARD_BG = new Color(25, 25, 40);
    private final Color ACCENT_GREEN = new Color(0, 255, 150);
    private final Color ACCENT_BLUE = new Color(100, 150, 255);
    private final Color TEXT_PRIMARY = new Color(240, 240, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170);
    private final Color INPUT_BG = new Color(30, 30, 45);
    
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public RegisterDialog(JFrame parent, AuthService authService, FirestoreService firestoreService) {
        super(parent, "Create Account", true);
        this.authService = authService;
        this.firestoreService = firestoreService;
        
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(8, 8, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Background gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, CARD_BG,
                    0, getHeight(), new Color(30, 30, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 30, 30);
                
                // Border
                g2d.setColor(ACCENT_BLUE);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(1, 1, getWidth() - 10, getHeight() - 10, 30, 30);
            }
        };
        
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("✨ CREATE ACCOUNT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton closeButton = createCloseButton();
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        headerPanel.setMaximumSize(new Dimension(400, 40));
        mainPanel.add(headerPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel subtitleLabel = new JLabel("Join the Snake Game community!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Username
        mainPanel.add(createFieldLabel("Username *"));
        mainPanel.add(Box.createVerticalStrut(8));
        usernameField = createModernTextField("Choose a username");
        mainPanel.add(usernameField);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Email
        mainPanel.add(createFieldLabel("Email (Optional)"));
        mainPanel.add(Box.createVerticalStrut(8));
        emailField = createModernTextField("your.email@example.com");
        mainPanel.add(emailField);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Password
        mainPanel.add(createFieldLabel("Password *"));
        mainPanel.add(Box.createVerticalStrut(8));
        passwordField = createModernPasswordField("Min 6 characters");
        mainPanel.add(passwordField);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Confirm Password
        mainPanel.add(createFieldLabel("Confirm Password *"));
        mainPanel.add(Box.createVerticalStrut(8));
        confirmPasswordField = createModernPasswordField("Re-enter password");
        mainPanel.add(confirmPasswordField);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setMaximumSize(new Dimension(400, 50));
        
        registerButton = createModernButton("CREATE ACCOUNT", ACCENT_GREEN);
        registerButton.setPreferredSize(new Dimension(200, 50));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);
        
        cancelButton = createModernButton("CANCEL", new Color(100, 100, 120));
        cancelButton.setPreferredSize(new Dimension(120, 50));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        
        add(mainPanel);
        
        // Enter key support
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleRegister();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        confirmPasswordField.addKeyListener(enterKeyListener);
    }
    
    private JButton createCloseButton() {
        JButton button = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 100, 100));
                } else {
                    g2d.setColor(TEXT_SECONDARY);
                }
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        button.setPreferredSize(new Dimension(35, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> dispose());
        
        return button;
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
                
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(new EmptyBorder(10, 15, 10, 15));
        field.setMaximumSize(new Dimension(400, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
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
                
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(new EmptyBorder(10, 15, 10, 15));
        field.setMaximumSize(new Dimension(400, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEchoChar('●');
        
        return field;
    }
    
    private JButton createModernButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(accentColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(accentColor.brighter());
                } else {
                    g2d.setColor(accentColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(DARK_BG);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (username.isEmpty() || username.equals("Choose a username")) {
            showStatus("Please enter a username", new Color(255, 100, 100));
            return;
        }
        
        if (username.length() < 3) {
            showStatus("Username must be at least 3 characters", new Color(255, 100, 100));
            return;
        }
        
        if (password.isEmpty()) {
            showStatus("Please enter a password", new Color(255, 100, 100));
            return;
        }
        
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters", new Color(255, 100, 100));
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords don't match", new Color(255, 100, 100));
            return;
        }
        
        if (email.equals("your.email@example.com")) {
            email = "";
        }
        
        registerButton.setEnabled(false);
        showStatus("Creating account...", ACCENT_BLUE);
        
        String finalEmail = email;
        
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.register(username, finalEmail, password);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showStatus("✓ Account created successfully!", ACCENT_GREEN);
                        registrationSuccessful = true;
                        Timer timer = new Timer(1000, e -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus("✗ Username already exists", new Color(255, 100, 100));
                        registerButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    showStatus("✗ Error: " + e.getMessage(), new Color(255, 100, 100));
                    registerButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
}