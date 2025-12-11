package com.snakegame;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;

public class ConfigLoader {
    private JSONObject config;
    
    public ConfigLoader(String configPath) throws Exception {
        try {
            // Try to load from resources
            InputStream is = getClass().getClassLoader().getResourceAsStream(configPath);
            if (is != null) {
                String content = new String(is.readAllBytes());
                // Remove JSON comments
                content = content.replaceAll("//.*", "");
                config = new JSONObject(content);
            } else {
                // Try file system
                String content = new String(Files.readAllBytes(Paths.get(configPath)));
                content = content.replaceAll("//.*", "");
                config = new JSONObject(content);
            }
        } catch (Exception e) {
            System.err.println("Failed to load config, using defaults");
            config = getDefaultConfig();
        }
    }
    
    private JSONObject getDefaultConfig() {
        JSONObject defaults = new JSONObject();
        defaults.put("gridSize", 20);
        defaults.put("screenWidth", 800);
        defaults.put("screenHeight", 600);
        defaults.put("backgroundColor", "#000000");
        defaults.put("snakeColor", "#00FF00");
        defaults.put("foodColor", "#FF0000");
        defaults.put("gridColor", "#282828");
        return defaults;
    }
    
    public int getGridSize() {
        return config.optInt("gridSize", 20);
    }
    
    public int getScreenWidth() {
        return config.optInt("screenWidth", 800);
    }
    
    public int getScreenHeight() {
        return config.optInt("screenHeight", 600);
    }
    
    public String getBackgroundColor() {
        return config.optString("backgroundColor", "#000000");
    }
    
    public String getSnakeColor() {
        return config.optString("snakeColor", "#00FF00");
    }
    
    public String getFoodColor() {
        return config.optString("foodColor", "#FF0000");
    }
    
    public String getGridColor() {
        return config.optString("gridColor", "#282828");
    }
    
    public static JSONObject loadConfig() throws Exception {
        String content = new String(Files.readAllBytes(
                Paths.get("config.json")));
        content = content.replaceAll("//.*", "");
        return new JSONObject(content);
    }

    public static void saveConfig(JSONObject config) throws Exception {
        Files.write(Paths.get("config.json"),
                config.toString(2).getBytes());
    }
}