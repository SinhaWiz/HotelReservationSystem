package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/rentalplatform";
    private static final String USER = "root";
    private static final String PASSWORD = "sinhawiz123"; // Password from earlier in the conversation
    
    private static Connection connection = null;
    
    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Try the older driver class name first
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    System.out.println("Using legacy MySQL driver");
                } catch (ClassNotFoundException e) {
                    // If that fails, try the newer driver class name
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        System.out.println("Using MySQL CJ driver");
                    } catch (ClassNotFoundException e2) {
                        throw new SQLException("MySQL JDBC Driver not found. Make sure the MySQL connector JAR is in the classpath.", e2);
                    }
                }
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
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