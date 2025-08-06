package com.hotel.view.panels;

import com.hotel.service.HotelManagementService;
import com.hotel.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Panel for reports and analytics
 */
public class ReportsPanel extends JPanel implements RefreshablePanel {
    
    private HotelManagementService hotelService;
    
    // Report selection components
    private JComboBox<String> reportTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton generateReportButton;
    private JButton exportReportButton;
    
    // Results display
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;
    private JTextArea summaryArea;
    
    // Quick stats panel
    private JLabel totalRevenueLabel;
    private JLabel totalBookingsLabel;
    private JLabel avgOccupancyLabel;
    private JLabel vipRevenueLabel;
    
    public ReportsPanel(HotelManagementService hotelService) {
        this.hotelService = hotelService;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeComponents() {
        // Report selection components
        reportTypeCombo = new JComboBox<>(new String[]{
            "Revenue Report",
            "Booking Summary",
            "Customer Analysis",
            "VIP Member Report",
            "Room Utilization",
            "Occupancy Trends",
            "VIP Eligible Customers"
        });
        
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        generateReportButton = new JButton("Generate Report");
        exportReportButton = new JButton("Export to CSV");
        
        // Set default date range (current month)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDateField.setText(dateFormat.format(cal.getTime()));
        
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        endDateField.setText(dateFormat.format(cal.getTime()));
        
        // Results table
        tableModel = new DefaultTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 300));
        
        // Summary area
        summaryArea = new JTextArea(8, 40);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        summaryArea.setBackground(new Color(248, 248, 248));
        summaryArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Style buttons
        generateReportButton.setBackground(new Color(70, 130, 180));
        generateReportButton.setForeground(Color.WHITE);
        exportReportButton.setBackground(new Color(34, 139, 34));
        exportReportButton.setForeground(Color.WHITE);
        
        // Quick stats labels
        totalRevenueLabel = new JLabel("$0.00");
        totalBookingsLabel = new JLabel("0");
        avgOccupancyLabel = new JLabel("0.0%");
        vipRevenueLabel = new JLabel("$0.00");
        
        // Style statistics labels
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        totalRevenueLabel.setFont(statsFont);
        totalBookingsLabel.setFont(statsFont);
        avgOccupancyLabel.setFont(statsFont);
        vipRevenueLabel.setFont(statsFont);
        
        totalRevenueLabel.setForeground(new Color(34, 139, 34));
        totalBookingsLabel.setForeground(new Color(70, 130, 180));
        avgOccupancyLabel.setForeground(new Color(255, 140, 0));
        vipRevenueLabel.setForeground(new Color(255, 215, 0));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Report selection and quick stats
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Results
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Summary
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Quick stats panel
        JPanel statsPanel = createQuickStatsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Report selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Report Generation"));
        
        selectionPanel.add(new JLabel("Report Type:"));
        selectionPanel.add(reportTypeCombo);
        selectionPanel.add(Box.createHorizontalStrut(10));
        selectionPanel.add(new JLabel("From:"));
        selectionPanel.add(startDateField);
        selectionPanel.add(new JLabel("To:"));
        selectionPanel.add(endDateField);
        selectionPanel.add(Box.createHorizontalStrut(10));
        selectionPanel.add(generateReportButton);
        selectionPanel.add(exportReportButton);
        
        panel.add(selectionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Statistics"));
        
        panel.add(createStatCard("Total Revenue", totalRevenueLabel, new Color(34, 139, 34)));
        panel.add(createStatCard("Total Bookings", totalBookingsLabel, new Color(70, 130, 180)));
        panel.add(createStatCard("Avg Occupancy", avgOccupancyLabel, new Color(255, 140, 0)));
        panel.add(createStatCard("VIP Revenue", vipRevenueLabel, new Color(255, 215, 0)));
        
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
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Report Results"));
        
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Report Summary"));
        
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        summaryScrollPane.setPreferredSize(new Dimension(0, 150));
        panel.add(summaryScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        generateReportButton.addActionListener(e -> generateReport());
        exportReportButton.addActionListener(e -> exportReport());
        
        // Enable export button only when there's data
        exportReportButton.setEnabled(false);
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String startDateStr = startDateField.getText().trim();
        String endDateStr = endDateField.getText().trim();
        
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both start and end dates.");
            return;
        }
        
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            
            if (endDate.before(startDate)) {
                JOptionPane.showMessageDialog(this, "End date must be after start date.");
                return;
            }
            
            // Clear previous results
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            summaryArea.setText("");
            
            // Generate report based on type
            switch (reportType) {
                case "Revenue Report":
                    generateRevenueReport(startDate, endDate);
                    break;
                case "Booking Summary":
                    generateBookingSummaryReport(startDate, endDate);
                    break;
                case "Customer Analysis":
                    generateCustomerAnalysisReport();
                    break;
                case "VIP Member Report":
                    generateVIPMemberReport();
                    break;
                case "Room Utilization":
                    generateRoomUtilizationReport(startDate, endDate);
                    break;
                case "Occupancy Trends":
                    generateOccupancyTrendsReport(startDate, endDate);
                    break;
                case "VIP Eligible Customers":
                    generateVIPEligibleReport();
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Report type not implemented yet.");
            }
            
            exportReportButton.setEnabled(tableModel.getRowCount() > 0);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + e.getMessage(),
                "Report Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateRevenueReport(Date startDate, Date endDate) throws Exception {
        List<Booking> bookings = hotelService.getBookingsByDateRange(startDate, endDate);
        
        // Set up table columns
        String[] columns = {"Booking ID", "Customer", "Room", "Check-In", "Check-Out", "Amount", "Status"};
        tableModel.setColumnIdentifiers(columns);
        
        double totalRevenue = 0.0;
        double vipRevenue = 0.0;
        int completedBookings = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Booking booking : bookings) {
            Object[] row = {
                booking.getBookingId(),
                booking.getCustomer() != null ? booking.getCustomer().getFullName() : "Unknown",
                booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "N/A",
                dateFormat.format(booking.getCheckInDate()),
                dateFormat.format(booking.getCheckOutDate()),
                String.format("$%.2f", booking.getTotalAmount()),
                booking.getBookingStatusString()
            };
            tableModel.addRow(row);
            
            if (booking.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
                totalRevenue += booking.getTotalAmount();
                completedBookings++;
                
                // Check if customer is VIP
                try {
                    VIPMember vipMember = hotelService.getCustomerVIPStatus(booking.getCustomerId());
                    if (vipMember != null && vipMember.isValidMembership()) {
                        vipRevenue += booking.getTotalAmount();
                    }
                } catch (Exception e) {
                    // Ignore VIP check errors
                }
            }
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("REVENUE REPORT SUMMARY\\n");
        summary.append("======================\\n\\n");
        summary.append(String.format("Report Period: %s to %s\\n", 
            new SimpleDateFormat("MMM dd, yyyy").format(startDate),
            new SimpleDateFormat("MMM dd, yyyy").format(endDate)));
        summary.append(String.format("Total Bookings: %d\\n", bookings.size()));
        summary.append(String.format("Completed Bookings: %d\\n", completedBookings));
        summary.append(String.format("Total Revenue: $%.2f\\n", totalRevenue));
        summary.append(String.format("VIP Revenue: $%.2f (%.1f%%)\\n", 
            vipRevenue, totalRevenue > 0 ? (vipRevenue / totalRevenue * 100) : 0));
        summary.append(String.format("Average Booking Value: $%.2f\\n", 
            completedBookings > 0 ? (totalRevenue / completedBookings) : 0));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateBookingSummaryReport(Date startDate, Date endDate) throws Exception {
        List<Booking> bookings = hotelService.getBookingsByDateRange(startDate, endDate);
        
        // Set up table columns
        String[] columns = {"Status", "Count", "Percentage", "Total Amount"};
        tableModel.setColumnIdentifiers(columns);
        
        // Count bookings by status
        int confirmed = 0, checkedIn = 0, checkedOut = 0, cancelled = 0, noShow = 0;
        double confirmedAmount = 0, checkedInAmount = 0, checkedOutAmount = 0;
        
        for (Booking booking : bookings) {
            switch (booking.getBookingStatus()) {
                case CONFIRMED:
                    confirmed++;
                    confirmedAmount += booking.getTotalAmount();
                    break;
                case CHECKED_IN:
                    checkedIn++;
                    checkedInAmount += booking.getTotalAmount();
                    break;
                case CHECKED_OUT:
                    checkedOut++;
                    checkedOutAmount += booking.getTotalAmount();
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
                case NO_SHOW:
                    noShow++;
                    break;
            }
        }
        
        int total = bookings.size();
        
        // Add rows to table
        if (confirmed > 0) {
            tableModel.addRow(new Object[]{
                "CONFIRMED", confirmed, String.format("%.1f%%", (double) confirmed / total * 100),
                String.format("$%.2f", confirmedAmount)
            });
        }
        if (checkedIn > 0) {
            tableModel.addRow(new Object[]{
                "CHECKED_IN", checkedIn, String.format("%.1f%%", (double) checkedIn / total * 100),
                String.format("$%.2f", checkedInAmount)
            });
        }
        if (checkedOut > 0) {
            tableModel.addRow(new Object[]{
                "CHECKED_OUT", checkedOut, String.format("%.1f%%", (double) checkedOut / total * 100),
                String.format("$%.2f", checkedOutAmount)
            });
        }
        if (cancelled > 0) {
            tableModel.addRow(new Object[]{
                "CANCELLED", cancelled, String.format("%.1f%%", (double) cancelled / total * 100),
                "$0.00"
            });
        }
        if (noShow > 0) {
            tableModel.addRow(new Object[]{
                "NO_SHOW", noShow, String.format("%.1f%%", (double) noShow / total * 100),
                "$0.00"
            });
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("BOOKING SUMMARY REPORT\\n");
        summary.append("======================\\n\\n");
        summary.append(String.format("Report Period: %s to %s\\n", 
            new SimpleDateFormat("MMM dd, yyyy").format(startDate),
            new SimpleDateFormat("MMM dd, yyyy").format(endDate)));
        summary.append(String.format("Total Bookings: %d\\n\\n", total));
        summary.append("Booking Status Breakdown:\\n");
        summary.append(String.format("- Confirmed: %d (%.1f%%)\\n", confirmed, (double) confirmed / total * 100));
        summary.append(String.format("- Checked In: %d (%.1f%%)\\n", checkedIn, (double) checkedIn / total * 100));
        summary.append(String.format("- Checked Out: %d (%.1f%%)\\n", checkedOut, (double) checkedOut / total * 100));
        summary.append(String.format("- Cancelled: %d (%.1f%%)\\n", cancelled, (double) cancelled / total * 100));
        summary.append(String.format("- No Show: %d (%.1f%%)\\n", noShow, (double) noShow / total * 100));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateCustomerAnalysisReport() throws Exception {
        List<Customer> customers = hotelService.getAllCustomers();
        
        // Set up table columns
        String[] columns = {"Customer Name", "Email", "Total Spent", "Loyalty Points", "Bookings", "VIP Status"};
        tableModel.setColumnIdentifiers(columns);
        
        double totalSpent = 0.0;
        int totalLoyaltyPoints = 0;
        int vipCustomers = 0;
        
        for (Customer customer : customers) {
            // Check VIP status
            String vipStatus = "Regular";
            try {
                VIPMember vipMember = hotelService.getCustomerVIPStatus(customer.getCustomerId());
                if (vipMember != null && vipMember.isValidMembership()) {
                    vipStatus = "VIP (" + vipMember.getMembershipLevelString() + ")";
                    vipCustomers++;
                }
            } catch (Exception e) {
                // Ignore VIP status check errors
            }
            
            // Get booking count
            int bookingCount = 0;
            try {
                List<Booking> customerBookings = hotelService.getCustomerBookingHistory(customer.getCustomerId());
                bookingCount = customerBookings.size();
            } catch (Exception e) {
                // Ignore booking count errors
            }
            
            Object[] row = {
                customer.getFullName(),
                customer.getEmail(),
                String.format("$%.2f", customer.getTotalSpent()),
                customer.getLoyaltyPoints(),
                bookingCount,
                vipStatus
            };
            tableModel.addRow(row);
            
            totalSpent += customer.getTotalSpent();
            totalLoyaltyPoints += customer.getLoyaltyPoints();
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("CUSTOMER ANALYSIS REPORT\\n");
        summary.append("========================\\n\\n");
        summary.append(String.format("Total Customers: %d\\n", customers.size()));
        summary.append(String.format("VIP Customers: %d (%.1f%%)\\n", vipCustomers, 
            customers.size() > 0 ? (double) vipCustomers / customers.size() * 100 : 0));
        summary.append(String.format("Total Customer Spending: $%.2f\\n", totalSpent));
        summary.append(String.format("Average Spending per Customer: $%.2f\\n", 
            customers.size() > 0 ? totalSpent / customers.size() : 0));
        summary.append(String.format("Total Loyalty Points: %d\\n", totalLoyaltyPoints));
        summary.append(String.format("Average Loyalty Points: %.1f\\n", 
            customers.size() > 0 ? (double) totalLoyaltyPoints / customers.size() : 0));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateVIPMemberReport() throws Exception {
        List<VIPMember> vipMembers = hotelService.getVIPMembersDetailed(null);
        
        // Set up table columns
        String[] columns = {"VIP ID", "Customer Name", "Level", "Discount %", "Total Spent", "Start Date", "Status"};
        tableModel.setColumnIdentifiers(columns);
        
        int goldMembers = 0, platinumMembers = 0, diamondMembers = 0;
        double totalVIPSpending = 0.0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (VIPMember vipMember : vipMembers) {
            Object[] row = {
                vipMember.getVipId(),
                vipMember.getCustomer() != null ? vipMember.getCustomer().getFullName() : "Unknown",
                vipMember.getMembershipLevelString(),
                String.format("%.1f%%", vipMember.getDiscountPercentage()),
                vipMember.getCustomer() != null ? 
                    String.format("$%.2f", vipMember.getCustomer().getTotalSpent()) : "$0.00",
                vipMember.getMembershipStartDate() != null ? 
                    dateFormat.format(vipMember.getMembershipStartDate()) : "N/A",
                vipMember.isActive() ? "Active" : "Inactive"
            };
            tableModel.addRow(row);
            
            if (vipMember.isActive()) {
                switch (vipMember.getMembershipLevel()) {
                    case GOLD:
                        goldMembers++;
                        break;
                    case PLATINUM:
                        platinumMembers++;
                        break;
                    case DIAMOND:
                        diamondMembers++;
                        break;
                }
                
                if (vipMember.getCustomer() != null) {
                    totalVIPSpending += vipMember.getCustomer().getTotalSpent();
                }
            }
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("VIP MEMBER REPORT\\n");
        summary.append("=================\\n\\n");
        summary.append(String.format("Total VIP Members: %d\\n", vipMembers.size()));
        summary.append(String.format("Active VIP Members: %d\\n", goldMembers + platinumMembers + diamondMembers));
        summary.append("\\nMembership Level Breakdown:\\n");
        summary.append(String.format("- Gold Members: %d\\n", goldMembers));
        summary.append(String.format("- Platinum Members: %d\\n", platinumMembers));
        summary.append(String.format("- Diamond Members: %d\\n", diamondMembers));
        summary.append(String.format("\\nTotal VIP Spending: $%.2f\\n", totalVIPSpending));
        summary.append(String.format("Average VIP Spending: $%.2f\\n", 
            vipMembers.size() > 0 ? totalVIPSpending / vipMembers.size() : 0));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateRoomUtilizationReport(Date startDate, Date endDate) throws Exception {
        List<Object[]> utilizationStats = hotelService.getRoomUtilizationStats(startDate, endDate);
        
        // Set up table columns
        String[] columns = {"Room Type", "Total Rooms", "Occupied Days", "Available Days", "Utilization %"};
        tableModel.setColumnIdentifiers(columns);
        
        double totalUtilization = 0.0;
        int roomTypeCount = 0;
        
        for (Object[] stat : utilizationStats) {
            tableModel.addRow(stat);
            if (stat.length > 4 && stat[4] != null) {
                try {
                    double utilization = Double.parseDouble(stat[4].toString().replace("%", ""));
                    totalUtilization += utilization;
                    roomTypeCount++;
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("ROOM UTILIZATION REPORT\\n");
        summary.append("=======================\\n\\n");
        summary.append(String.format("Report Period: %s to %s\\n", 
            new SimpleDateFormat("MMM dd, yyyy").format(startDate),
            new SimpleDateFormat("MMM dd, yyyy").format(endDate)));
        summary.append(String.format("Room Types Analyzed: %d\\n", roomTypeCount));
        summary.append(String.format("Average Utilization: %.1f%%\\n", 
            roomTypeCount > 0 ? totalUtilization / roomTypeCount : 0));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateOccupancyTrendsReport(Date startDate, Date endDate) throws Exception {
        // Calculate occupancy rate for the period
        double occupancyRate = hotelService.getRoomOccupancyRate(startDate, endDate);
        
        // Get room statistics
        List<Room> allRooms = hotelService.getAllRooms();
        List<Room> availableRooms = hotelService.getRoomsByStatus(Room.RoomStatus.AVAILABLE);
        List<Room> occupiedRooms = hotelService.getRoomsByStatus(Room.RoomStatus.OCCUPIED);
        
        // Set up table columns
        String[] columns = {"Metric", "Value", "Percentage"};
        tableModel.setColumnIdentifiers(columns);
        
        int totalRooms = allRooms.size();
        int available = availableRooms.size();
        int occupied = occupiedRooms.size();
        
        tableModel.addRow(new Object[]{
            "Total Rooms", totalRooms, "100.0%"
        });
        tableModel.addRow(new Object[]{
            "Available Rooms", available, 
            String.format("%.1f%%", totalRooms > 0 ? (double) available / totalRooms * 100 : 0)
        });
        tableModel.addRow(new Object[]{
            "Occupied Rooms", occupied, 
            String.format("%.1f%%", totalRooms > 0 ? (double) occupied / totalRooms * 100 : 0)
        });
        tableModel.addRow(new Object[]{
            "Period Occupancy Rate", String.format("%.1f%%", occupancyRate), 
            String.format("%.1f%%", occupancyRate)
        });
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("OCCUPANCY TRENDS REPORT\\n");
        summary.append("=======================\\n\\n");
        summary.append(String.format("Report Period: %s to %s\\n", 
            new SimpleDateFormat("MMM dd, yyyy").format(startDate),
            new SimpleDateFormat("MMM dd, yyyy").format(endDate)));
        summary.append(String.format("Current Occupancy Rate: %.1f%%\\n", occupancyRate));
        summary.append(String.format("Available Rooms: %d out of %d\\n", available, totalRooms));
        summary.append(String.format("Occupied Rooms: %d out of %d\\n", occupied, totalRooms));
        
        summaryArea.setText(summary.toString());
    }
    
    private void generateVIPEligibleReport() throws Exception {
        List<Customer> eligibleCustomers = hotelService.getVIPEligibleCustomers();
        
        // Set up table columns
        String[] columns = {"Customer Name", "Email", "Total Spent", "Loyalty Points", "Recommended Level"};
        tableModel.setColumnIdentifiers(columns);
        
        double totalEligibleSpending = 0.0;
        
        for (Customer customer : eligibleCustomers) {
            // Determine recommended VIP level based on spending
            String recommendedLevel = "GOLD";
            if (customer.getTotalSpent() >= 15000) {
                recommendedLevel = "DIAMOND";
            } else if (customer.getTotalSpent() >= 10000) {
                recommendedLevel = "PLATINUM";
            }
            
            Object[] row = {
                customer.getFullName(),
                customer.getEmail(),
                String.format("$%.2f", customer.getTotalSpent()),
                customer.getLoyaltyPoints(),
                recommendedLevel
            };
            tableModel.addRow(row);
            
            totalEligibleSpending += customer.getTotalSpent();
        }
        
        // Generate summary
        StringBuilder summary = new StringBuilder();
        summary.append("VIP ELIGIBLE CUSTOMERS REPORT\\n");
        summary.append("=============================\\n\\n");
        summary.append(String.format("Eligible Customers: %d\\n", eligibleCustomers.size()));
        summary.append(String.format("Total Eligible Spending: $%.2f\\n", totalEligibleSpending));
        summary.append(String.format("Average Eligible Spending: $%.2f\\n", 
            eligibleCustomers.size() > 0 ? totalEligibleSpending / eligibleCustomers.size() : 0));
        summary.append("\\nRecommended Actions:\\n");
        summary.append("- Contact eligible customers for VIP promotion\\n");
        summary.append("- Offer personalized VIP benefits\\n");
        summary.append("- Schedule VIP enrollment campaigns\\n");
        
        summaryArea.setText(summary.toString());
    }
    
    private void exportReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new java.io.File("hotel_report.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                java.io.PrintWriter writer = new java.io.PrintWriter(fileToSave);
                
                // Write headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
                
                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        writer.print(value != null ? value.toString() : "");
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
                
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Report exported successfully to: " + fileToSave.getAbsolutePath(),
                    "Export Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting report: " + e.getMessage(),
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Update quick statistics
                updateQuickStatistics();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing reports data: " + e.getMessage(),
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void updateQuickStatistics() {
        try {
            // Calculate statistics for current month
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startOfMonth = cal.getTime();
            Date endOfMonth = new Date();
            
            List<Booking> monthlyBookings = hotelService.getBookingsByDateRange(startOfMonth, endOfMonth);
            
            double totalRevenue = 0.0;
            double vipRevenue = 0.0;
            int totalBookings = monthlyBookings.size();
            
            for (Booking booking : monthlyBookings) {
                if (booking.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
                    totalRevenue += booking.getTotalAmount();
                    
                    // Check if customer is VIP
                    try {
                        VIPMember vipMember = hotelService.getCustomerVIPStatus(booking.getCustomerId());
                        if (vipMember != null && vipMember.isValidMembership()) {
                            vipRevenue += booking.getTotalAmount();
                        }
                    } catch (Exception e) {
                        // Ignore VIP check errors
                    }
                }
            }
            
            double occupancyRate = hotelService.getRoomOccupancyRate(startOfMonth, endOfMonth);
            
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
            totalBookingsLabel.setText(String.valueOf(totalBookings));
            avgOccupancyLabel.setText(String.format("%.1f%%", occupancyRate));
            vipRevenueLabel.setText(String.format("$%.2f", vipRevenue));
            
        } catch (Exception e) {
            // Set default values on error
            totalRevenueLabel.setText("$0.00");
            totalBookingsLabel.setText("0");
            avgOccupancyLabel.setText("0.0%");
            vipRevenueLabel.setText("$0.00");
        }
    }
}

