package com.hotel.view;

import com.hotel.view.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main frame of the Hotel Rental System application
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    public MainFrame() {
        initComponents();
    }
    
    /**
     * Initialize the components of the frame
     */
    private void initComponents() {
        // Set frame properties
        setTitle("Hotel Rental System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels for each tab
        DashboardPanel dashboardPanel = new DashboardPanel();
        PropertyPanel propertyPanel = new PropertyPanel();
        BookingPanel bookingPanel = new BookingPanel();
        UserPanel userPanel = new UserPanel();
        ReportPanel reportPanel = new ReportPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "View system overview");
        tabbedPane.addTab("Properties", new ImageIcon(), propertyPanel, "Manage properties");
        tabbedPane.addTab("Bookings", new ImageIcon(), bookingPanel, "Manage bookings");
        tabbedPane.addTab("Users", new ImageIcon(), userPanel, "Manage users");
        tabbedPane.addTab("Reports", new ImageIcon(), reportPanel, "View reports");
        
        // Add tabbed pane to frame
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        // Create menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
    }
    
    /**
     * Create the menu bar for the frame
     * @return The created menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Show the about dialog
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Hotel Rental System\nVersion 1.0\n\nA system for managing hotel rentals.",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
} 