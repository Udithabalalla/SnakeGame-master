package com.snakegame;

import com.snakegame.ui.LoginScreen;

import javax.swing.*;
import java.awt.*;

public class SnakeGame {
    public static void main(String[] args) {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Modern font rendering
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🐍 Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            
            // Start with login screen
            LoginScreen loginScreen = new LoginScreen(frame);
            frame.add(loginScreen);
            
            // Set initial size for login
            frame.setSize(600, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("╔═══════════════════════════════════╗");
            System.out.println("║      🐍 SNAKE GAME STARTED 🐍    ║");
            System.out.println("╚═══════════════════════════════════╝");
        });
    }
}