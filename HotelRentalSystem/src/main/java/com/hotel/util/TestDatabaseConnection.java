package com.hotel.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple program to test the database connection
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            // Try to load the MySQL JDBC driver explicitly
            try {
                Class.forName("com.mysql.jdbc.Driver");
                System.out.println("MySQL JDBC Driver loaded successfully!");
            } catch (ClassNotFoundException e) {
                System.out.println("MySQL JDBC Driver not found!");
                System.out.println("Error: " + e.getMessage());
                
                // Try the new driver class name
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    System.out.println("MySQL CJ JDBC Driver loaded successfully!");
                } catch (ClassNotFoundException e2) {
                    System.out.println("MySQL CJ JDBC Driver not found!");
                    System.out.println("Error: " + e2.getMessage());
                }
                
                return;
            }
            
            // Try to get a database connection
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            
            // Close the connection
            conn.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 