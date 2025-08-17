package com.hotel.view;

import com.hotel.model.EnhancedHotelManagementService;
import com.hotel.util.DatabaseConnection;
import com.hotel.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Main application window for Hotel Management System
 */
public class HotelManagementApp extends JFrame {
    
    private EnhancedHotelManagementService hotelService;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    
    // Panels for different functionalities
    private DashboardPanel dashboardPanel;
    private CustomerManagementPanel customerPanel;
    private BookingManagementPanel bookingPanel;
    private RoomManagementPanel roomPanel;
    private VIPMemberPanel vipPanel;
    private ServiceManagementPanel servicePanel;
    private InvoiceManagementPanel invoicePanel;
    private ReportsPanel reportsPanel;
    
    public HotelManagementApp() {
        initializeServices();
        initializeGUI();
        setupEventHandlers();
    }
    
    private void initializeServices() {
        try {
            // Test database connection
            if (DatabaseConnection.testConnection()) {
                hotelService = new EnhancedHotelManagementService();
                System.out.println("Database connection established successfully");
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Failed to connect to database. Please check your Oracle database configuration.",
                    "Database Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error initializing application: " + e.getMessage(),
                "Initialization Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void initializeGUI() {
        setTitle("Hotel Management System - Oracle Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create main content
        createMainContent();
        
        // Create status bar
        createStatusBar();
        
        // Set application icon
        setIconImage(createAppIcon());
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        // Database Menu
        JMenu databaseMenu = new JMenu("Database");
        databaseMenu.setMnemonic('D');
        
        JMenuItem testConnectionItem = new JMenuItem("Test Connection");
        testConnectionItem.addActionListener(e -> testDatabaseConnection());
        databaseMenu.add(testConnectionItem);
        
        JMenuItem dbInfoItem = new JMenuItem("Database Info");
        dbInfoItem.addActionListener(e -> showDatabaseInfo());
        databaseMenu.add(dbInfoItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem vipRenewalItem = new JMenuItem("Process VIP Renewals");
        vipRenewalItem.addActionListener(e -> processVIPRenewals());
        toolsMenu.add(vipRenewalItem);
        
        JMenuItem refreshItem = new JMenuItem("Refresh All Data");
        refreshItem.setMnemonic('R');
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshAllData());
        toolsMenu.add(refreshItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(databaseMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        
        // Initialize panels
        dashboardPanel = new DashboardPanel(hotelService);
        customerPanel = new CustomerManagementPanel(hotelService);
        bookingPanel = new BookingManagementPanel(hotelService);
        roomPanel = new RoomManagementPanel(hotelService);
        vipPanel = new VIPMemberPanel(hotelService);
        servicePanel = new ServiceManagementPanel();
        invoicePanel = new InvoiceManagementPanel();
        reportsPanel = new ReportsPanel(hotelService);
        
        // Add tabs
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "System Overview");
        tabbedPane.addTab("Customers", new ImageIcon(), customerPanel, "Customer Management");
        tabbedPane.addTab("Bookings", new ImageIcon(), bookingPanel, "Booking Management");
        tabbedPane.addTab("Rooms", new ImageIcon(), roomPanel, "Room Management");
        tabbedPane.addTab("VIP Members", new ImageIcon(), vipPanel, "VIP Member Management");
        tabbedPane.addTab("Services", new ImageIcon(), servicePanel, "Room Service Management");
        tabbedPane.addTab("Invoices", new ImageIcon(), invoicePanel, "Invoice Management");
        tabbedPane.addTab("Reports", new ImageIcon(), reportsPanel, "Reports and Analytics");
        
        // Add tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String tabName = tabbedPane.getTitleAt(selectedIndex);
            updateStatus("Switched to " + tabName + " tab");
            
            // Refresh data when switching tabs
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent instanceof RefreshablePanel) {
                ((RefreshablePanel) selectedComponent).refreshData();
            }
        });
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        statusLabel = new JLabel("Hotel Management System Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        JLabel connectionLabel = new JLabel("Oracle DB Connected");
        connectionLabel.setForeground(Color.GREEN);
        connectionLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(connectionLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private Image createAppIcon() {
        // Create a simple hotel icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw building
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(8, 12, 16, 16);
        
        // Draw roof
        g2d.setColor(new Color(139, 69, 19));
        int[] xPoints = {6, 16, 26};
        int[] yPoints = {12, 4, 12};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Draw windows
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(10, 15, 3, 3);
        g2d.fillRect(19, 15, 3, 3);
        g2d.fillRect(10, 21, 3, 3);
        g2d.fillRect(19, 21, 3, 3);
        
        // Draw door
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(14, 22, 4, 6);
        
        g2d.dispose();
        return icon;
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void testDatabaseConnection() {
        try {
            boolean connected = DatabaseConnection.testConnection();
            if (connected) {
                JOptionPane.showMessageDialog(this,
                    "Database connection successful!",
                    "Connection Test",
                    JOptionPane.INFORMATION_MESSAGE);
                updateStatus("Database connection test successful");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Database connection failed!",
                    "Connection Test",
                    JOptionPane.ERROR_MESSAGE);
                updateStatus("Database connection test failed");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error testing connection: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showDatabaseInfo() {
        try {
            DatabaseConnection.printDatabaseInfo();
            JOptionPane.showMessageDialog(this,
                "Database information printed to console",
                "Database Info",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error getting database info: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void processVIPRenewals() {
        try {
            hotelService.promoteTopCustomersToVIP("System");
            JOptionPane.showMessageDialog(this,
                "Top customers promoted to VIP successfully!",
                "VIP Promotions",
                JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Top customers promoted to VIP");
            
            // Refresh VIP panel if it's visible
            if (tabbedPane.getSelectedComponent() == vipPanel) {
                vipPanel.refreshData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error promoting customers to VIP: " + e.getMessage(),
                "VIP Promotion Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshAllData() {
        try {
            // Refresh current panel
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent instanceof RefreshablePanel) {
                ((RefreshablePanel) selectedComponent).refreshData();
            }
            updateStatus("Data refreshed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error refreshing data: " + e.getMessage(),
                "Refresh Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAboutDialog() {
        String message = "Hotel Management System\\n" +
                        "Enhanced Version 2.0.0\\n\\n" +
                        "A comprehensive hotel management solution\\n" +
                        "with Oracle database backend and Java Swing frontend.\\n\\n" +
                        "Features:\\n" +
                        "• Customer Management\\n" +
                        "• Booking Management with Check-in/Check-out\\n" +
                        "• Room Management with Service Assignments\\n" +
                        "• VIP Member Management with Auto-Promotion\\n" +
                        "• Room Service Management\\n" +
                        "• Customer Blacklist Management\\n" +
                        "• Invoice Generation and Payment Tracking\\n" +
                        "• Automated Data Archiving (60 days)\\n" +
                        "• Late Checkout Extra Charges\\n" +
                        "• Customer Service Usage Tracking\\n" +
                        "• Comprehensive Financial Reporting\\n\\n" +
                        "Built with Oracle Database, Java, and Swing\\n" +
                        "Uses Oracle Functions, Procedures, Triggers, and Cursors";
        
        JOptionPane.showMessageDialog(this, message, "About Hotel Management System", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit the Hotel Management System?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                DatabaseConnection.closeAllConnections();
                updateStatus("Application shutting down...");
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
                System.exit(1);
            }
        }
    }
    
    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    public static void main(String[] args) {
        // Set system properties for better UI
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                new HotelManagementApp().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start application: " + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

