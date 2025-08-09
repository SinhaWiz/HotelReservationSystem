package com.hotel.view.panels;

import com.hotel.service.HotelManagementService;
import com.hotel.model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Dashboard panel showing system overview and key statistics
 */
public class DashboardPanel extends JPanel implements RefreshablePanel {
    
    private HotelManagementService hotelService;
    
    // Statistics labels
    private JLabel totalCustomersLabel;
    private JLabel totalVIPMembersLabel;
    private JLabel totalRoomsLabel;
    private JLabel availableRoomsLabel;
    private JLabel occupiedRoomsLabel;
    private JLabel currentReservationsLabel;
    private JLabel occupancyRateLabel;
    
    // Quick action buttons
    private JButton newBookingButton;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton refreshButton;
    
    // Recent activity area
    private JTextArea recentActivityArea;
    private JScrollPane activityScrollPane;
    
    public DashboardPanel(HotelManagementService hotelService) {
        this.hotelService = hotelService;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeComponents() {
        // Statistics labels
        totalCustomersLabel = new JLabel("0");
        totalVIPMembersLabel = new JLabel("0");
        totalRoomsLabel = new JLabel("0");
        availableRoomsLabel = new JLabel("0");
        occupiedRoomsLabel = new JLabel("0");
        currentReservationsLabel = new JLabel("0");
        occupancyRateLabel = new JLabel("0.0%");
        
        // Style statistics labels
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
        totalCustomersLabel.setFont(statsFont);
        totalVIPMembersLabel.setFont(statsFont);
        totalRoomsLabel.setFont(statsFont);
        availableRoomsLabel.setFont(statsFont);
        occupiedRoomsLabel.setFont(statsFont);
        currentReservationsLabel.setFont(statsFont);
        occupancyRateLabel.setFont(statsFont);
        
        totalCustomersLabel.setForeground(new Color(70, 130, 180));
        totalVIPMembersLabel.setForeground(new Color(255, 215, 0));
        totalRoomsLabel.setForeground(new Color(60, 179, 113));
        availableRoomsLabel.setForeground(new Color(34, 139, 34));
        occupiedRoomsLabel.setForeground(new Color(220, 20, 60));
        currentReservationsLabel.setForeground(new Color(255, 140, 0));
        occupancyRateLabel.setForeground(new Color(138, 43, 226));
        
        // Quick action buttons
        newBookingButton = new JButton("New Booking");
        checkInButton = new JButton("Check In");
        checkOutButton = new JButton("Check Out");
        refreshButton = new JButton("Refresh Dashboard");
        
        // Style buttons
        Dimension buttonSize = new Dimension(150, 40);
        newBookingButton.setPreferredSize(buttonSize);
        checkInButton.setPreferredSize(buttonSize);
        checkOutButton.setPreferredSize(buttonSize);
        refreshButton.setPreferredSize(buttonSize);
        
        // Recent activity area
        recentActivityArea = new JTextArea(10, 40);
        recentActivityArea.setEditable(false);
        recentActivityArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        recentActivityArea.setBackground(new Color(248, 248, 248));
        activityScrollPane = new JScrollPane(recentActivityArea);
        activityScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Welcome and date
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Statistics and quick actions
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        centerPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel actionsPanel = createQuickActionsPanel();
        centerPanel.add(actionsPanel, BorderLayout.EAST);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Recent activity
        JPanel bottomPanel = createRecentActivityPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("Hotel Management System Dashboard");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy - HH:mm:ss");
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);
        
        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(dateLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "System Statistics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        
        // Create stat cards
        panel.add(createStatCard("Total Customers", totalCustomersLabel, new Color(70, 130, 180)));
        panel.add(createStatCard("VIP Members", totalVIPMembersLabel, new Color(255, 215, 0)));
        panel.add(createStatCard("Total Rooms", totalRoomsLabel, new Color(60, 179, 113)));
        panel.add(createStatCard("Available Rooms", availableRoomsLabel, new Color(34, 139, 34)));
        panel.add(createStatCard("Occupied Rooms", occupiedRoomsLabel, new Color(220, 20, 60)));
        panel.add(createStatCard("Current Reservations", currentReservationsLabel, new Color(255, 140, 0)));
        panel.add(createStatCard("Occupancy Rate", occupancyRateLabel, new Color(138, 43, 226)));
        panel.add(createStatCard("System Status", new JLabel("Online"), new Color(34, 139, 34)));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Quick Actions",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        
        panel.add(newBookingButton);
        panel.add(checkInButton);
        panel.add(checkOutButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Recent Activity",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        
        panel.add(activityScrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 200));
        
        return panel;
    }
    
    private void setupEventHandlers() {
        newBookingButton.addActionListener(e -> {
            // Open new booking dialog
            NewBookingDialog dialog = new NewBookingDialog((JFrame) SwingUtilities.getWindowAncestor(this), hotelService);
            dialog.setVisible(true);
            if (dialog.isBookingCreated()) {
                refreshData();
                addActivity("New booking created for customer: " + dialog.getCustomerName());
            }
        });
        
        checkInButton.addActionListener(e -> {
            // Open check-in dialog
            String bookingId = JOptionPane.showInputDialog(this, "Enter Booking ID for Check-In:");
            if (bookingId != null && !bookingId.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(bookingId.trim());
                    boolean success = hotelService.checkInCustomer(id);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Customer checked in successfully!");
                        refreshData();
                        addActivity("Customer checked in - Booking ID: " + id);
                    } else {
                        JOptionPane.showMessageDialog(this, "Check-in failed. Please verify booking ID and status.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid booking ID format.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error during check-in: " + ex.getMessage());
                }
            }
        });
        
        checkOutButton.addActionListener(e -> {
            // Open check-out dialog
            String bookingId = JOptionPane.showInputDialog(this, "Enter Booking ID for Check-Out:");
            if (bookingId != null && !bookingId.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(bookingId.trim());
                    boolean success = hotelService.checkOutCustomer(id);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Customer checked out successfully!");
                        refreshData();
                        addActivity("Customer checked out - Booking ID: " + id);
                    } else {
                        JOptionPane.showMessageDialog(this, "Check-out failed. Please verify booking ID and status.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid booking ID format.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error during check-out: " + ex.getMessage());
                }
            }
        });
        
        refreshButton.addActionListener(e -> {
            refreshData();
            addActivity("Dashboard refreshed");
        });
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Get system statistics
                Object[] stats = hotelService.getSystemStatistics();
                
                totalCustomersLabel.setText(String.valueOf(stats[0]));
                totalVIPMembersLabel.setText(String.valueOf(stats[1]));
                totalRoomsLabel.setText(String.valueOf(stats[2]));
                availableRoomsLabel.setText(String.valueOf(stats[3]));
                occupiedRoomsLabel.setText(String.valueOf(stats[4]));
                currentReservationsLabel.setText(String.valueOf(stats[5]));
                
                // Calculate occupancy rate
                int totalRooms = (Integer) stats[2];
                int occupiedRooms = (Integer) stats[4];
                double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
                occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
                
                // Update recent activity with current reservations
                updateRecentActivity();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing dashboard: " + e.getMessage(),
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void updateRecentActivity() {
        try {
            StringBuilder activity = new StringBuilder();
            activity.append("=== CURRENT RESERVATIONS ===\\n");
            
            List<Booking> reservations = hotelService.getCurrentReservations();
            
            if (reservations.isEmpty()) {
                activity.append("No current reservations.\\n");
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                
                for (Booking booking : reservations) {
                    activity.append(String.format("Booking #%d - %s\\n", 
                        booking.getBookingId(),
                        booking.getCustomer() != null ? booking.getCustomer().getFullName() : "Unknown Customer"));
                    
                    activity.append(String.format("  Room: %s | Check-in: %s | Check-out: %s\\n",
                        booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "N/A",
                        dateFormat.format(booking.getCheckInDate()),
                        dateFormat.format(booking.getCheckOutDate())));
                    
                    activity.append(String.format("  Status: %s | Amount: $%.2f\\n",
                        booking.getBookingStatusString(),
                        booking.getTotalAmount()));
                    
                    activity.append("\\n");
                }
            }
            
            activity.append("\\n=== SYSTEM STATUS ===\\n");
            activity.append("Last Updated: ").append(new SimpleDateFormat("HH:mm:ss").format(new Date())).append("\\n");
            activity.append("Database: Connected\\n");
            activity.append("System: Online\\n");
            
            recentActivityArea.setText(activity.toString());
            recentActivityArea.setCaretPosition(0);
            
        } catch (Exception e) {
            recentActivityArea.setText("Error loading recent activity: " + e.getMessage());
        }
    }
    
    private void addActivity(String message) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timestamp = timeFormat.format(new Date());
        String activityLine = "[" + timestamp + "] " + message + "\\n";
        
        recentActivityArea.insert(activityLine, 0);
    }
}

/**
 * Dialog for creating new bookings from dashboard
 */
class NewBookingDialog extends JDialog {
    private HotelManagementService hotelService;
    private boolean bookingCreated = false;
    private String customerName = "";
    
    private JTextField customerIdField;
    private JTextField roomIdField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JTextArea specialRequestsArea;
    
    public NewBookingDialog(JFrame parent, HotelManagementService hotelService) {
        super(parent, "New Booking", true);
        this.hotelService = hotelService;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Customer ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        customerIdField = new JTextField(15);
        formPanel.add(customerIdField, gbc);
        
        // Room ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Room ID:"), gbc);
        gbc.gridx = 1;
        roomIdField = new JTextField(15);
        formPanel.add(roomIdField, gbc);
        
        // Check-in date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Check-in (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        checkInField = new JTextField(15);
        formPanel.add(checkInField, gbc);
        
        // Check-out date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Check-out (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        checkOutField = new JTextField(15);
        formPanel.add(checkOutField, gbc);
        
        // Special requests
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Special Requests:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        specialRequestsArea = new JTextArea(3, 15);
        JScrollPane scrollPane = new JScrollPane(specialRequestsArea);
        formPanel.add(scrollPane, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create Booking");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> createBooking());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createBooking() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText().trim());
            int roomId = Integer.parseInt(roomIdField.getText().trim());
            
            // Parse dates (simple format YYYY-MM-DD)
            String[] checkInParts = checkInField.getText().trim().split("-");
            String[] checkOutParts = checkOutField.getText().trim().split("-");
            
            Calendar checkInCal = Calendar.getInstance();
            checkInCal.set(Integer.parseInt(checkInParts[0]), 
                          Integer.parseInt(checkInParts[1]) - 1, 
                          Integer.parseInt(checkInParts[2]));
            
            Calendar checkOutCal = Calendar.getInstance();
            checkOutCal.set(Integer.parseInt(checkOutParts[0]), 
                           Integer.parseInt(checkOutParts[1]) - 1, 
                           Integer.parseInt(checkOutParts[2]));
            
            Date checkInDate = checkInCal.getTime();
            Date checkOutDate = checkOutCal.getTime();
            String specialRequests = specialRequestsArea.getText().trim();
            
            Booking booking = hotelService.createBooking(customerId, roomId, checkInDate, checkOutDate, specialRequests);
            
            if (booking != null) {
                bookingCreated = true;
                if (booking.getCustomer() != null) {
                    customerName = booking.getCustomer().getFullName();
                }
                JOptionPane.showMessageDialog(this, 
                    "Booking created successfully!\\nBooking ID: " + booking.getBookingId(),
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error creating booking: " + e.getMessage(),
                "Booking Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isBookingCreated() {
        return bookingCreated;
    }
    
    public String getCustomerName() {
        return customerName;
    }
}

