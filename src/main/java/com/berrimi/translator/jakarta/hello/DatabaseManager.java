package com.berrimi.translator.jakarta.hello;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
  // Database in WildFly's standalone/data directory
  private static final String DB_PATH = System.getProperty("jboss.server.data.dir", ".") + "/translator.db";
  private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
  private static Connection connection;

  static {
    System.out.println("===========================================");
    System.out.println("Database location: " + DB_PATH);
    System.out.println("===========================================");
  }

  static {
    try {
      // Load SQLite JDBC driver
      Class.forName("org.sqlite.JDBC");
      // Initialize database and create tables
      initializeDatabase();
    } catch (ClassNotFoundException e) {
      System.err.println("SQLite JDBC driver not found: " + e.getMessage());
    }
  }

  /**
   * Get database connection
   */
  public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      connection = DriverManager.getConnection(DB_URL);
      // Enable foreign keys
      try (Statement stmt = connection.createStatement()) {
        stmt.execute("PRAGMA foreign_keys = ON;");
      }
    }
    return connection;
  }

  /**
   * Initialize database and create tables if they don't exist
   */
  private static void initializeDatabase() {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      // Create users table
      String createUsersTable = """
          CREATE TABLE IF NOT EXISTS users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT UNIQUE NOT NULL,
              password TEXT NOT NULL,
              email TEXT NOT NULL,
              phone TEXT NOT NULL,
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
          );
          """;

      // Create translation_history table
      String createHistoryTable = """
          CREATE TABLE IF NOT EXISTS translation_history (
              id TEXT PRIMARY KEY,
              username TEXT NOT NULL,
              original_text TEXT NOT NULL,
              translated_text TEXT NOT NULL,
              target_lang TEXT NOT NULL,
              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
          );
          """;

      // Create index on username for faster queries
      String createUsernameIndex = """
          CREATE INDEX IF NOT EXISTS idx_history_username
          ON translation_history(username);
          """;

      // Create index on timestamp for sorting
      String createTimestampIndex = """
          CREATE INDEX IF NOT EXISTS idx_history_timestamp
          ON translation_history(timestamp DESC);
          """;

      stmt.execute(createUsersTable);
      stmt.execute(createHistoryTable);
      stmt.execute(createUsernameIndex);
      stmt.execute(createTimestampIndex);

      System.out.println("Database initialized successfully");

    } catch (SQLException e) {
      System.err.println("Error initializing database: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Close database connection
   */
  public static void closeConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.println("Database connection closed");
      }
    } catch (SQLException e) {
      System.err.println("Error closing database connection: " + e.getMessage());
    }
  }
}
