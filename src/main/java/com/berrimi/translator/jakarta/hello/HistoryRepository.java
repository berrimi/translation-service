package com.berrimi.translator.jakarta.hello;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HistoryRepository {
    // Store history for each user
    private static ConcurrentHashMap<String, List<TranslationHistory>> userHistory = 
            new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_PER_USER = 5;
    
    // Add a translation to user's history
    public static void addTranslation(String username, TranslationHistory history) {
        userHistory.computeIfAbsent(username, k -> new ArrayList<>());
        
        List<TranslationHistory> historyList = userHistory.get(username);
        
        // Add new history at the beginning (most recent first)
        historyList.add(0, history);
        
        // Keep only last 5 translations
        if (historyList.size() > MAX_HISTORY_PER_USER) {
            userHistory.put(username, 
                historyList.stream()
                    .limit(MAX_HISTORY_PER_USER)
                    .collect(Collectors.toList()));
        }
    }
    
    // Get user's translation history (most recent first)
    public static List<TranslationHistory> getHistory(String username) {
        return userHistory.getOrDefault(username, Collections.emptyList());
    }
    
    // Clear user's history
    public static void clearHistory(String username) {
        userHistory.remove(username);
    }
}