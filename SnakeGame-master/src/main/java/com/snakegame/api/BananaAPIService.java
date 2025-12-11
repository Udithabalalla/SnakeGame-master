package com.snakegame.api;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BananaAPIService {
    private static final String API_URL = "https://marcconrad.com/uob/banana/api.php";
    
    public static class Question {
        private String questionImageUrl;
        private int solution;
        
        public Question(String questionImageUrl, int solution) {
            this.questionImageUrl = questionImageUrl;
            this.solution = solution;
        }
        
        public String getQuestionImageUrl() {
            return questionImageUrl;
        }
        
        public int getSolution() {
            return solution;
        }
    }
    
    /**
     * Fetch a new question from the Banana API
     * @return Question object with image URL and solution
     */
    public Question fetchQuestion() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String questionUrl = jsonResponse.getString("question");
                int solution = jsonResponse.getInt("solution");
                
                System.out.println("✅ Banana API question fetched successfully");
                return new Question(questionUrl, solution);
                
            } else {
                System.err.println("❌ Banana API returned code: " + responseCode);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching Banana API question: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}