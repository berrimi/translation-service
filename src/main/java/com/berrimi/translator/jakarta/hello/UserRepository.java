package com.berrimi.translator.jakarta.hello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UserRepository {

  /**
   * Register a new user
   */
  public static boolean register(User user) {
    String sql = "INSERT INTO users (username, password, email, phone) VALUES (?, ?, ?, ?)";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, hashPassword(user.getPassword())); // Hash password
      pstmt.setString(3, user.getEmail());
      pstmt.setString(4, user.getPhone());

      pstmt.executeUpdate();
      return true;

    } catch (SQLException e) {
      // Username already exists (UNIQUE constraint violation)
      if (e.getMessage().contains("UNIQUE constraint failed")) {
        return false;
      }
      System.err.println("Error registering user: " + e.getMessage());
      return false;
    }
  }

  /**
   * Login user
   */
  public static boolean login(String username, String password) {
    String sql = "SELECT password FROM users WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        String storedHash = rs.getString("password");
        String inputHash = hashPassword(password);
        return storedHash.equals(inputHash);
      }
      return false;

    } catch (SQLException e) {
      System.err.println("Error logging in: " + e.getMessage());
      return false;
    }
  }

  /**
   * Get user by username
   */
  public static User getUser(String username) {
    String sql = "SELECT username, email, phone FROM users WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        User user = new User();
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        return user;
      }
      return null;

    } catch (SQLException e) {
      System.err.println("Error getting user: " + e.getMessage());
      return null;
    }
  }

  /**
   * Check if user exists
   */
  public static boolean userExists(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();
      return rs.next();

    } catch (SQLException e) {
      System.err.println("Error checking user existence: " + e.getMessage());
      return false;
    }
  }

  /**
   * Update user information (email and phone)
   */
  public static boolean updateUser(String username, String email, String phone) {
    String sql = "UPDATE users SET email = ?, phone = ? WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      pstmt.setString(2, phone);
      pstmt.setString(3, username);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      System.err.println("Error updating user: " + e.getMessage());
      return false;
    }
  }

  /**
   * Update user password
   */
  public static boolean updatePassword(String username, String oldPassword, String newPassword) {
    // First verify old password
    if (!login(username, oldPassword)) {
      return false;
    }

    String sql = "UPDATE users SET password = ? WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, hashPassword(newPassword));
      pstmt.setString(2, username);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      System.err.println("Error updating password: " + e.getMessage());
      return false;
    }
  }

  /**
   * Delete user
   */
  public static boolean deleteUser(String username) {
    String sql = "DELETE FROM users WHERE username = ?";

    try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      System.err.println("Error deleting user: " + e.getMessage());
      return false;
    }
  }

  /**
   * Hash password using SHA-256
   */
  private static String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(password.getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Error hashing password: " + e.getMessage());
      // Fallback to plain text (not recommended for production)
      return password;
    }
  }
}
