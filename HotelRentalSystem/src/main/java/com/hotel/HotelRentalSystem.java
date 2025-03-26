package com.hotel;

import com.hotel.view.MainFrame;
import com.hotel.util.DatabaseConnection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main class for the Hotel Rental System application
 */
public class HotelRentalSystem {
    
    /**
     * Main method to start the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Test database connection
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            
            // Launch the GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                    "Error connecting to database: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 