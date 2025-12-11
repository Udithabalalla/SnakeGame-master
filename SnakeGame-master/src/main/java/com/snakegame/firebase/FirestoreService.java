package com.snakegame.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.snakegame.models.Difficulty;
import com.snakegame.models.Score;
import com.snakegame.models.User;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class FirestoreService {
    private final Firestore db;
    private final boolean available;
    
    public FirestoreService() {
        FirebaseConfig.initialize();
        this.available = FirebaseConfig.isAvailable();
        
        if (available) {
            this.db = FirestoreClient.getFirestore(FirebaseConfig.getApp());
        } else {
            this.db = null;
            System.out.println("⚠️  FirestoreService running in offline mode");
        }
    }
    
    private void checkAvailability() throws Exception {
        if (!available) {
            throw new Exception("Firebase is not available. Running in offline mode.");
        }
    }
    
    // ===== USER OPERATIONS =====
    
    public void createUser(User user) throws Exception {
        checkAvailability();
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("passwordHash", user.getPasswordHash());
        userData.put("avatarUrl", user.getAvatarUrl());
        userData.put("highScore", 0);
        userData.put("totalGamesPlayed", 0);
        userData.put("createdAt", new Date());
        userData.put("lastLogin", new Date());
        
        db.collection("users").document(user.getUserId()).set(userData).get();
    }
    
    public User getUser(String userId) throws Exception {
        checkAvailability();
        
        DocumentSnapshot document = db.collection("users").document(userId).get().get();
        if (document.exists()) {
            return mapToUser(document);
        }
        return null;
    }
    
    public User getUserByUsername(String username) throws Exception {
        checkAvailability();
        
        Query query = db.collection("users")
            .whereEqualTo("username", username)
            .limit(1);
        
        QuerySnapshot querySnapshot = query.get().get();
        if (!querySnapshot.isEmpty()) {
            return mapToUser(querySnapshot.getDocuments().get(0));
        }
        return null;
    }
    
    public User getUserByEmail(String email) throws Exception {
        checkAvailability();
        
        if (email == null || email.isEmpty()) {
            return null;
        }
        
        Query query = db.collection("users")
            .whereEqualTo("email", email)
            .limit(1);
        
        QuerySnapshot querySnapshot = query.get().get();
        if (!querySnapshot.isEmpty()) {
            return mapToUser(querySnapshot.getDocuments().get(0));
        }
        return null;
    }
    
    private User mapToUser(DocumentSnapshot document) {
        User user = new User();
        user.setUserId(document.getId());
        user.setUsername(document.getString("username"));
        user.setEmail(document.getString("email"));
        user.setPasswordHash(document.getString("passwordHash"));
        user.setAvatarUrl(document.getString("avatarUrl"));
        user.setHighScore(document.getLong("highScore").intValue());
        user.setTotalGamesPlayed(document.getLong("totalGamesPlayed").intValue());
        user.setCreatedAt(document.getDate("createdAt"));
        user.setLastLogin(document.getDate("lastLogin"));
        return user;
    }
    
    public void updateLastLogin(String userId) throws Exception {
        checkAvailability();
        db.collection("users").document(userId)
            .update("lastLogin", new Date()).get();
    }
    
    public void updatePassword(String userId, String newPasswordHash) throws Exception {
        checkAvailability();
        db.collection("users").document(userId)
            .update("passwordHash", newPasswordHash).get();
    }
    
    public void updateAvatar(String userId, String avatarUrl) throws Exception {
        checkAvailability();
        db.collection("users").document(userId)
            .update("avatarUrl", avatarUrl).get();
    }
    
    public void updateHighScore(String userId, int newHighScore) throws Exception {
        checkAvailability();
        
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentSnapshot userDoc = userRef.get().get();
        
        if (userDoc.exists()) {
            int currentHighScore = userDoc.getLong("highScore").intValue();
            if (newHighScore > currentHighScore) {
                userRef.update("highScore", newHighScore).get();
            }
        }
    }
    
    // ===== SCORE OPERATIONS =====
    
    public void saveScore(Score score) throws Exception {
        if (!available) {
            System.out.println("⚠️  Offline mode: Score not saved to Firebase");
            System.out.println("📊 Local Score: " + score.getScore());
            return;
        }
        
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("userId", score.getUserId());
        scoreData.put("userName", score.getUserName());
        scoreData.put("avatarUrl", score.getAvatarUrl());
        scoreData.put("score", score.getScore());
        scoreData.put("difficulty", score.getDifficulty().name());
        scoreData.put("timestamp", new Date());
        
        ApiFuture<DocumentReference> future = db.collection("scores").add(scoreData);
        DocumentReference docRef = future.get();
        score.setScoreId(docRef.getId());
        
        // Update user stats
        DocumentReference userRef = db.collection("users").document(score.getUserId());
        userRef.update("totalGamesPlayed", FieldValue.increment(1)).get();
        
        // Update high score if needed
        updateHighScore(score.getUserId(), score.getScore());
    }
    
    public List<Score> getLeaderboard(Difficulty difficulty, int limit) throws Exception {
        checkAvailability();
        
        Query query = db.collection("scores")
            .whereEqualTo("difficulty", difficulty.name())
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        List<Score> leaderboard = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            leaderboard.add(mapToScore(document));
        }
        
        return leaderboard;
    }
    
    public List<Score> getGlobalLeaderboard(int limit) throws Exception {
        checkAvailability();
        
        Query query = db.collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit * 3);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        Map<String, Score> bestScores = new HashMap<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            Score score = mapToScore(document);
            String userId = score.getUserId();
            
            if (!bestScores.containsKey(userId) || 
                bestScores.get(userId).getScore() < score.getScore()) {
                bestScores.put(userId, score);
            }
        }
        
        List<Score> leaderboard = new ArrayList<>(bestScores.values());
        leaderboard.sort(Score::compareTo);
        
        return leaderboard.subList(0, Math.min(limit, leaderboard.size()));
    }
    
    public List<Score> getUserScores(String userId, int limit) throws Exception {
        checkAvailability();
        
        Query query = db.collection("scores")
            .whereEqualTo("userId", userId)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        List<Score> scores = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            scores.add(mapToScore(document));
        }
        
        return scores;
    }
    
    private Score mapToScore(DocumentSnapshot document) {
        Score score = new Score();
        score.setScoreId(document.getId());
        score.setUserId(document.getString("userId"));
        score.setUserName(document.getString("userName"));
        score.setAvatarUrl(document.getString("avatarUrl"));
        score.setScore(document.getLong("score").intValue());
        score.setDifficulty(Difficulty.valueOf(document.getString("difficulty")));
        score.setTimestamp(document.getDate("timestamp"));
        return score;
    }
    
    public int getUserRank(String userId, Difficulty difficulty) throws Exception {
        checkAvailability();
        
        Query userQuery = db.collection("scores")
            .whereEqualTo("userId", userId)
            .whereEqualTo("difficulty", difficulty.name())
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(1);
        
        QuerySnapshot userSnapshot = userQuery.get().get();
        if (userSnapshot.isEmpty()) {
            return -1;
        }
        
        int userScore = userSnapshot.getDocuments().get(0).getLong("score").intValue();
        
        Query higherScoresQuery = db.collection("scores")
            .whereEqualTo("difficulty", difficulty.name())
            .whereGreaterThan("score", userScore);
        
        QuerySnapshot higherScores = higherScoresQuery.get().get();
        
        Set<String> uniqueUsers = new HashSet<>();
        for (DocumentSnapshot doc : higherScores.getDocuments()) {
            uniqueUsers.add(doc.getString("userId"));
        }
        
        return uniqueUsers.size() + 1;
    }
    
    public boolean isAvailable() {
        return available;
    }
}