package com.snakegame;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        setTitle("Retro Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame());
    }
}