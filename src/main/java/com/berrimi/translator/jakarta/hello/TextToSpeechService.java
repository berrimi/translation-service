package com.berrimi.translator.jakarta.hello;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class TextToSpeechService {
    
    private static final String CREDENTIALS_FILE = "google-credentials.json";
    private static String googleApiKey;
    
    static {
        loadApiKey();
    }
    
    private static void loadApiKey() {
        try (InputStream input = TextToSpeechService.class.getClassLoader()
                .getResourceAsStream("translator.properties")) {
            
            if (input == null) {
                System.err.println("translator.properties not found in resources!");
                return;
            }
            
            Properties props = new Properties();
            props.load(input);
            googleApiKey = props.getProperty("GOOGLE_TTS_API_KEY");
            
            if (googleApiKey == null) {
                System.err.println("GOOGLE_TTS_API_KEY not found in properties!");
            }
            
        } catch (IOException e) {
            System.err.println("Error loading API key: " + e.getMessage());
        }
    }
    
    /**
     * Convert text to speech using Google TTS API
     * @param text The text to convert to speech
     * @param languageCode The language code (e.g., "en-US", "ar-AR", "fr-FR")
     * @return Base64 encoded audio bytes
     */
    public static String textToSpeech(String text, String languageCode) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Default to Arabic if language not specified
        if (languageCode == null || languageCode.trim().isEmpty()) {
            languageCode = "ar-AR";
        }
        
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();
            
            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();
            
            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)  // Using MP3 for better compatibility
                    .setSpeakingRate(1.0)  // Normal speed
                    .setPitch(0.0)  // Normal pitch
                    .setVolumeGainDb(0.0)  // Normal volume
                    .build();
            
            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                    input, voice, audioConfig);
            
            // Get the audio content from the response
            ByteString audioContents = response.getAudioContent();
            
            // Convert to base64 for easy transmission
            return Base64.getEncoder().encodeToString(audioContents.toByteArray());
            
        } catch (Exception e) {
            System.err.println("Error in text-to-speech conversion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Detect language and return appropriate TTS language code
     */
    public static String detectLanguageCode(String text) {
        // Simple language detection for common languages in your app
        if (text == null || text.trim().isEmpty()) {
            return "ar-AR"; // Default to Arabic
        }
        
        String lowerText = text.toLowerCase();
        
        // Check for Arabic characters (including Darija)
        if (containsArabic(text)) {
            return "ar-AR"; // Arabic
        } else if (containsFrenchKeywords(lowerText)) {
            return "fr-FR"; // French
        } else if (containsEnglishKeywords(lowerText)) {
            return "en-US"; // English
        } else if (containsSpanishKeywords(lowerText)) {
            return "es-ES"; // Spanish
        }
        
        // Default to Arabic
        return "ar-AR";
    }
    
    private static boolean containsArabic(String text) {
        // Arabic Unicode range
        return text.matches(".*[\\u0600-\\u06FF].*");
    }
    
    private static boolean containsFrenchKeywords(String text) {
        String[] frenchWords = {"bonjour", "merci", "oui", "non", "s'il vous plaît", "français"};
        for (String word : frenchWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean containsEnglishKeywords(String text) {
        String[] englishWords = {"hello", "thank you", "yes", "no", "please", "english"};
        for (String word : englishWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean containsSpanishKeywords(String text) {
        String[] spanishWords = {"hola", "gracias", "sí", "no", "por favor", "español"};
        for (String word : spanishWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get language code from language name
     */
    public static String getLanguageCode(String languageName) {
        if (languageName == null) {
            return "ar-AR";
        }
        
        switch (languageName.toLowerCase()) {
            case "darija":
            case "arabic":
            case "ar":
                return "ar-AR";
            case "english":
            case "en":
                return "en-US";
            case "french":
            case "fr":
                return "fr-FR";
            case "spanish":
            case "es":
                return "es-ES";
            case "amazigh":
            case "berber":
                return "ar-AR"; // Default to Arabic for Amazigh
            default:
                return "ar-AR";
        }
    }
}