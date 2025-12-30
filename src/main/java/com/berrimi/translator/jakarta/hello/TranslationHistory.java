package com.berrimi.translator.jakarta.hello;

import java.util.Date;

public class TranslationHistory {
    private String id;
    private String username;
    private String originalText;
    private String translatedText;
    private String targetLang;
    private Date timestamp;
    
    public TranslationHistory() {
    }
    
    public TranslationHistory(String id, String username, String originalText, 
                             String translatedText, String targetLang) {
        this.id = id;
        this.username = username;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.targetLang = targetLang;
        this.timestamp = new Date();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public String getTranslatedText() {
        return translatedText;
    }
    
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    
    public String getTargetLang() {
        return targetLang;
    }
    
    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}