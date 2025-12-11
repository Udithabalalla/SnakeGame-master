package com.snakegame.ui;

import com.snakegame.auth.AuthenticationService;
import com.snakegame.auth.GoogleAuthService;
import com.snakegame.models.User;
import com.snakegame.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton googleSignInButton;
    private GoogleAuthService googleAuthService;
    private JFrame parentFrame;
    
    public LoginPanel(JFrame parentFrame, AuthenticationService authService) {
        this.parentFrame = parentFrame;
        this.googleAuthService = new GoogleAuthService(authService);
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Snake Game - Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(usernameLabel, gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);
        
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(loginButton, gbc);
        
        // Add Google Sign-In button after the regular login button
        googleSignInButton = new JButton("Sign in with Google");
        googleSignInButton.setFont(new Font("Arial", Font.BOLD, 16));
        googleSignInButton.setBackground(new Color(66, 133, 244)); // Google blue
        googleSignInButton.setForeground(Color.WHITE);
        googleSignInButton.setFocusPainted(false);
        googleSignInButton.setBorderPainted(false);
        googleSignInButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        googleSignInButton.addActionListener(e -> handleGoogleSignIn());
        
        // Add to layout (adjust positioning as needed)
        gbc.gridy = 4; // Adjust based on your current layout
        gbc.insets = new Insets(10, 50, 10, 50);
        add(googleSignInButton, gbc);
        
        // Action listener for regular login
        loginButton.addActionListener(e -> handleLogin());
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        // Perform login logic here (e.g., call to authService)
        // On success:
        // User user = ...;
        // SessionManager.getInstance().login(user);
        // Navigate to main menu
    }
    
    private void handleGoogleSignIn() {
        googleSignInButton.setEnabled(false);
        googleSignInButton.setText("Signing in...");
        
        // Run in background thread
        new Thread(() -> {
            try {
                User user = googleAuthService.signInWithGoogle();
                
                SwingUtilities.invokeLater(() -> {
                    SessionManager.getInstance().login(user);
                    JOptionPane.showMessageDialog(
                        this,
                        "Welcome " + user.getUsername() + "!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Navigate to main menu
                    parentFrame.getContentPane().removeAll();
                    parentFrame.add(new MainMenuPanel(parentFrame));
                    parentFrame.revalidate();
                    parentFrame.repaint();
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        this,
                        "Google Sign-In failed: " + ex.getMessage(),
                        "Authentication Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    googleSignInButton.setEnabled(true);
                    googleSignInButton.setText("Sign in with Google");
                });
            }
        }).start();
    }
}