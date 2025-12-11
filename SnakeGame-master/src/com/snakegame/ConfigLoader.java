package com.snakegame;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigLoader {
    public static JSONObject loadConfig() throws Exception {
        String content = new String(Files.readAllBytes(
                Paths.get("config.json")));
        return new JSONObject(content);
    }

    public static void saveConfig(JSONObject config) throws Exception {
        Files.write(Paths.get("config.json"),
                config.toString(2).getBytes());
    }
}