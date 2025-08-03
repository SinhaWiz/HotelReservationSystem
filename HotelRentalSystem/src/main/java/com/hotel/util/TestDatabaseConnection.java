package com.hotel.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility class to test database connection
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("Testing Oracle Database Connection...");
        
        try {
            // Try to load the Oracle JDBC driver explicitly
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                System.out.println("Oracle JDBC Driver loaded successfully!");
            } catch (ClassNotFoundException e) {
                System.out.println("Oracle JDBC Driver not found!");
                e.printStackTrace();
                return;
            }
            
            // Test the connection
            Connection connection = DatabaseConnection.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection successful!");
                
                // Get database metadata
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Database: " + metaData.getDatabaseProductName());
                System.out.println("Version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver: " + metaData.getDriverName());
                System.out.println("Driver Version: " + metaData.getDriverVersion());
                
                connection.close();
                System.out.println("Connection closed successfully!");
            } else {
                System.out.println("Failed to establish database connection!");
            }
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 