package com.snakegame;

import java.sql.*;

public class ScoreRepository {
    private Connection conn;

    public ScoreRepository() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:scores.db");
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scores (" +
                "id INTEGER PRIMARY KEY, " +
                "player TEXT, " +
                "score INTEGER, " +
                "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        conn.createStatement().execute(sql);
    }

    public void saveScore(String player, int score) {
        try {
            String sql = "INSERT INTO scores (player, score) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, player);
            stmt.setInt(2, score);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}