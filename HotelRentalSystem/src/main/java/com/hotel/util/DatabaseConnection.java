package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    // Oracle connection parameters
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "rentalplatform";
    private static final String PASSWORD = "rentalplatform123";
    
    private static Connection connection = null;
    
    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load Oracle JDBC driver
                Class.forName("oracle.jdbc.driver.OracleDriver");
                System.out.println("Oracle JDBC Driver loaded successfully!");
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found. Make sure the Oracle JDBC driver JAR is in the classpath.", e);
            } catch (Exception e) {
                throw new SQLException("Failed to establish database connection: " + e.getMessage(), e);
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
} 