package com.berrimi.translator.jakarta.hello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

  private static final int MAX_HISTORY_PER_USER = 50;

  /**
   * Add a translation to user's history
   */
  public static boolean addTranslation(String username, TranslationHistory history) {
    String sql = """
        INSERT INTO translation_history
        (id, username, original_text, translated_text, target_lang, timestamp)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, history.getId());
      pstmt.setString(2, username);
      pstmt.setString(3, history.getOriginalText());
      pstmt.setString(4, history.getTranslatedText());
      pstmt.setString(5, history.getTargetLang());
      pstmt.setTimestamp(6, new Timestamp(history.getTimestamp().getTime()));

      pstmt.executeUpdate();

      // Clean up old history entries if exceeded max
      cleanupOldHistory(username);

      return true;

    } catch (SQLException e) {
      System.err.println("Error adding translation history: " + e.getMessage());
      return false;
    }
  }

  /**
   * Get user's translation history (most recent first)
   */
  public static List<TranslationHistory> getHistory(String username) {
    List<TranslationHistory> historyList = new ArrayList<>();
    String sql = """
        SELECT id, username, original_text, translated_text, target_lang, timestamp
        FROM translation_history
        WHERE username = ?
        ORDER BY timestamp DESC
        LIMIT ?
        """;

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.setInt(2, MAX_HISTORY_PER_USER);

      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        TranslationHistory history = new TranslationHistory();
        history.setId(rs.getString("id"));
        history.setUsername(rs.getString("username"));
        history.setOriginalText(rs.getString("original_text"));
        history.setTranslatedText(rs.getString("translated_text"));
        history.setTargetLang(rs.getString("target_lang"));
        history.setTimestamp(rs.getTimestamp("timestamp"));

        historyList.add(history);
      }

    } catch (SQLException e) {
      System.err.println("Error getting translation history: " + e.getMessage());
    }

    return historyList;
  }

  /**
   * Get a specific translation by ID
   */
  public static TranslationHistory getTranslationById(String id) {
    String sql = """
        SELECT id, username, original_text, translated_text, target_lang, timestamp
        FROM translation_history
        WHERE id = ?
        """;

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, id);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        TranslationHistory history = new TranslationHistory();
        history.setId(rs.getString("id"));
        history.setUsername(rs.getString("username"));
        history.setOriginalText(rs.getString("original_text"));
        history.setTranslatedText(rs.getString("translated_text"));
        history.setTargetLang(rs.getString("target_lang"));
        history.setTimestamp(rs.getTimestamp("timestamp"));
        return history;
      }

    } catch (SQLException e) {
      System.err.println("Error getting translation by ID: " + e.getMessage());
    }

    return null;
  }

  /**
   * Clear user's history
   */
  public static boolean clearHistory(String username) {
    String sql = "DELETE FROM translation_history WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.executeUpdate();
      return true;

    } catch (SQLException e) {
      System.err.println("Error clearing history: " + e.getMessage());
      return false;
    }
  }

  /**
   * Delete a specific translation by ID
   */
  public static boolean deleteTranslation(String id, String username) {
    String sql = "DELETE FROM translation_history WHERE id = ? AND username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, id);
      pstmt.setString(2, username);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      System.err.println("Error deleting translation: " + e.getMessage());
      return false;
    }
  }

  /**
   * Get count of translations for a user
   */
  public static int getHistoryCount(String username) {
    String sql = "SELECT COUNT(*) as count FROM translation_history WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return rs.getInt("count");
      }

    } catch (SQLException e) {
      System.err.println("Error getting history count: " + e.getMessage());
    }

    return 0;
  }

  /**
   * Search translations by text
   */
  public static List<TranslationHistory> searchHistory(String username, String searchText) {
    List<TranslationHistory> historyList = new ArrayList<>();
    String sql = """
        SELECT id, username, original_text, translated_text, target_lang, timestamp
        FROM translation_history
        WHERE username = ? AND (original_text LIKE ? OR translated_text LIKE ?)
        ORDER BY timestamp DESC
        LIMIT ?
        """;

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      String searchPattern = "%" + searchText + "%";
      pstmt.setString(1, username);
      pstmt.setString(2, searchPattern);
      pstmt.setString(3, searchPattern);
      pstmt.setInt(4, MAX_HISTORY_PER_USER);

      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        TranslationHistory history = new TranslationHistory();
        history.setId(rs.getString("id"));
        history.setUsername(rs.getString("username"));
        history.setOriginalText(rs.getString("original_text"));
        history.setTranslatedText(rs.getString("translated_text"));
        history.setTargetLang(rs.getString("target_lang"));
        history.setTimestamp(rs.getTimestamp("timestamp"));

        historyList.add(history);
      }

    } catch (SQLException e) {
      System.err.println("Error searching history: " + e.getMessage());
    }

    return historyList;
  }

  /**
   * Clean up old history entries if exceeded max
   */
  private static void cleanupOldHistory(String username) {
    String sql = """
        DELETE FROM translation_history
        WHERE id IN (
            SELECT id FROM translation_history
            WHERE username = ?
            ORDER BY timestamp DESC
            LIMIT -1 OFFSET ?
        )
        """;

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.setInt(2, MAX_HISTORY_PER_USER);
      pstmt.executeUpdate();

    } catch (SQLException e) {
      System.err.println("Error cleaning up old history: " + e.getMessage());
    }
  }
}
