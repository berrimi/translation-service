package com.berrimi.translator.jakarta.hello;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Application lifecycle listener to manage database connection
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("Application started - Database initialized");
    // Database is already initialized in DatabaseManager static block
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("Application shutting down - Closing database connection");
    DatabaseManager.closeConnection();
  }
}
