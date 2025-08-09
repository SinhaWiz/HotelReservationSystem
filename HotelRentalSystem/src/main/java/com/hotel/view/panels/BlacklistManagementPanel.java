package com.hotel.view.panels;

import com.hotel.model.*;
import com.hotel.service.EnhancedHotelManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing blacklisted customers
 */
public class BlacklistManagementPanel extends JPanel {
    
    private EnhancedHotelManagementService hotelService;
    private JTable blacklistTable;
    private DefaultTableModel blacklistTableModel;
    private JComboBox<String> customerComboBox;
    private JTextArea reasonTextArea;
    private JTextField blacklistedByField;
    private JSpinner expiryDateSpinner;
    private JCheckBox hasExpiryCheckBox;
    private JTextField searchField;
    private JLabel statisticsLabel;
    
    // Predefined blacklist reasons
    private final String[] BLACKLIST_REASONS = {
        "Property Damage",
        "Disruptive Behavior",
        "Non-Payment",
        "Fraudulent Activity",
        "Violation of Hotel Policy",
        "Threatening Behavior",
        "Excessive Complaints",
        "Other"
    };
    
    public BlacklistManagementPanel() {
        this.hotelService = new EnhancedHotelManagementService();
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        loadBlacklistData();
        updateStatistics();
    }
    
    private void initializeComponents() {
        // Blacklist table
        String[] columns = {"ID", "Customer Name", "Email", "Reason", "Blacklisted By", 
                           "Blacklist Date", "Expiry Date", "Status", "Notes"};
        blacklistTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        blacklistTable = new JTable(blacklistTableModel);
        blacklistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blacklistTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        blacklistTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        blacklistTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Customer Name
        blacklistTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Email
        blacklistTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Reason
        blacklistTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Blacklisted By
        blacklistTable.getColumnModel().getColumn(5).setPreferredWidth(120);  // Blacklist Date
        blacklistTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Expiry Date
        blacklistTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Status
        blacklistTable.getColumnModel().getColumn(8).setPreferredWidth(200);  // Notes
        
        // Form components
        customerComboBox = new JComboBox<>();
        customerComboBox.setPreferredSize(new Dimension(300, 25));
        
        reasonTextArea = new JTextArea(3, 30);
        reasonTextArea.setLineWrap(true);
        reasonTextArea.setWrapStyleWord(true);
        
        blacklistedByField = new JTextField(20);
        blacklistedByField.setText(System.getProperty("user.name", "Admin"));
        
        // Date spinner for expiry date
        expiryDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd");
        expiryDateSpinner.setEditor(dateEditor);
        expiryDateSpinner.setEnabled(false);
        
        hasExpiryCheckBox = new JCheckBox("Set Expiry Date");
        hasExpiryCheckBox.addActionListener(e -> expiryDateSpinner.setEnabled(hasExpiryCheckBox.isSelected()));
        
        searchField = new JTextField(20);
        statisticsLabel = new JLabel();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Blacklist Management Tab
        JPanel blacklistPanel = createBlacklistManagementPanel();
        tabbedPane.addTab("Blacklist Management", blacklistPanel);
        
        // Blacklist Reports Tab
        JPanel reportsPanel = createBlacklistReportsPanel();
        tabbedPane.addTab("Blacklist Reports", reportsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createBlacklistManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with search and statistics
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBlacklistedCustomers());
        searchPanel.add(searchButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadBlacklistData();
            updateStatistics();
        });
        searchPanel.add(refreshButton);
        
        JButton showActiveButton = new JButton("Show Active Only");
        showActiveButton.addActionListener(e -> loadActiveBlacklistData());
        searchPanel.add(showActiveButton);
        
        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> loadBlacklistData());
        searchPanel.add(showAllButton);
        
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(statisticsLabel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with blacklist table
        JScrollPane tableScrollPane = new JScrollPane(blacklistTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with blacklist form
        JPanel formPanel = createBlacklistFormPanel();
        panel.add(formPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBlacklistFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Blacklist Customer"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Customer selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(customerComboBox, gbc);
        
        // Quick reason buttons
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Quick Reasons:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel reasonButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (String reason : BLACKLIST_REASONS) {
            JButton reasonButton = new JButton(reason);
            reasonButton.setPreferredSize(new Dimension(120, 25));
            reasonButton.addActionListener(e -> reasonTextArea.setText(reason));
            reasonButtonPanel.add(reasonButton);
        }
        panel.add(reasonButtonPanel, gbc);
        
        // Reason text area
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(reasonTextArea), gbc);
        
        // Blacklisted by
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Blacklisted By:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(blacklistedByField, gbc);
        
        // Expiry date
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(hasExpiryCheckBox, gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(expiryDateSpinner, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton blacklistButton = new JButton("Add to Blacklist");
        blacklistButton.addActionListener(e -> blacklistCustomer());
        buttonPanel.add(blacklistButton);
        
        JButton removeButton = new JButton("Remove from Blacklist");
        removeButton.addActionListener(e -> removeFromBlacklist());
        buttonPanel.add(removeButton);
        
        JButton updateButton = new JButton("Update Blacklist Entry");
        updateButton.addActionListener(e -> updateBlacklistEntry());
        buttonPanel.add(updateButton);
        
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        JButton checkStatusButton = new JButton("Check Customer Status");
        checkStatusButton.addActionListener(e -> checkCustomerStatus());
        buttonPanel.add(checkStatusButton);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createBlacklistReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea reportArea = new JTextArea(20, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton generateReportButton = new JButton("Generate Blacklist Report");
        generateReportButton.addActionListener(e -> {
            try {
                String report = generateBlacklistReport();
                reportArea.setText(report);
            } catch (SQLException ex) {
                showError("Error generating report: " + ex.getMessage());
            }
        });
        buttonPanel.add(generateReportButton);
        
        JButton reasonStatsButton = new JButton("Reason Statistics");
        reasonStatsButton.addActionListener(e -> {
            try {
                String stats = generateReasonStatistics();
                reportArea.setText(stats);
            } catch (SQLException ex) {
                showError("Error generating statistics: " + ex.getMessage());
            }
        });
        buttonPanel.add(reasonStatsButton);
        
        JButton expiredEntriesButton = new JButton("Show Expired Entries");
        expiredEntriesButton.addActionListener(e -> showExpiredEntries());
        buttonPanel.add(expiredEntriesButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void attachEventListeners() {
        // Table selection listener
        blacklistTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedEntryToForm();
            }
        });
    }
    
    private void loadBlacklistData() {
        try {
            List<BlacklistedCustomer> blacklistedCustomers = hotelService.getAllBlacklistedCustomers();
            blacklistTableModel.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (BlacklistedCustomer blacklisted : blacklistedCustomers) {
                Object[] row = {
                    blacklisted.getBlacklistId(),
                    blacklisted.getCustomerName(),
                    blacklisted.getCustomerEmail(),
                    blacklisted.getBlacklistReason(),
                    blacklisted.getBlacklistedBy(),
                    dateFormat.format(blacklisted.getBlacklistDate()),
                    blacklisted.getExpiryDate() != null ? dateFormat.format(blacklisted.getExpiryDate()) : "Never",
                    blacklisted.isActive() ? "Active" : "Inactive",
                    blacklisted.getNotes()
                };
                blacklistTableModel.addRow(row);
            }
            
            // Load customers for combo box (non-blacklisted customers)
            loadCustomersComboBox();
            
        } catch (SQLException e) {
            showError("Error loading blacklist data: " + e.getMessage());
        }
    }
    
    private void loadActiveBlacklistData() {
        try {
            List<BlacklistedCustomer> blacklistedCustomers = hotelService.getActiveBlacklistedCustomers();
            blacklistTableModel.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (BlacklistedCustomer blacklisted : blacklistedCustomers) {
                Object[] row = {
                    blacklisted.getBlacklistId(),
                    blacklisted.getCustomerName(),
                    blacklisted.getCustomerEmail(),
                    blacklisted.getBlacklistReason(),
                    blacklisted.getBlacklistedBy(),
                    dateFormat.format(blacklisted.getBlacklistDate()),
                    blacklisted.getExpiryDate() != null ? dateFormat.format(blacklisted.getExpiryDate()) : "Never",
                    "Active",
                    blacklisted.getNotes()
                };
                blacklistTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error loading active blacklist data: " + e.getMessage());
        }
    }
    
    private void loadCustomersComboBox() {
        try {
            customerComboBox.removeAllItems();
            List<Customer> customers = hotelService.getAllCustomers();
            
            for (Customer customer : customers) {
                // Only add non-blacklisted customers
                if (!hotelService.isCustomerBlacklisted(customer.getCustomerId())) {
                    customerComboBox.addItem(customer.getCustomerId() + " - " + customer.getFullName() + 
                                           " (" + customer.getEmail() + ")");
                }
            }
            
        } catch (SQLException e) {
            showError("Error loading customers: " + e.getMessage());
        }
    }
    
    private void searchBlacklistedCustomers() {
        try {
            String searchTerm = searchField.getText().trim();
            List<BlacklistedCustomer> results;
            
            if (searchTerm.isEmpty()) {
                results = hotelService.getAllBlacklistedCustomers();
            } else {
                results = hotelService.searchBlacklistedCustomers(searchTerm);
            }
            
            blacklistTableModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (BlacklistedCustomer blacklisted : results) {
                Object[] row = {
                    blacklisted.getBlacklistId(),
                    blacklisted.getCustomerName(),
                    blacklisted.getCustomerEmail(),
                    blacklisted.getBlacklistReason(),
                    blacklisted.getBlacklistedBy(),
                    dateFormat.format(blacklisted.getBlacklistDate()),
                    blacklisted.getExpiryDate() != null ? dateFormat.format(blacklisted.getExpiryDate()) : "Never",
                    blacklisted.isActive() ? "Active" : "Inactive",
                    blacklisted.getNotes()
                };
                blacklistTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error searching blacklisted customers: " + e.getMessage());
        }
    }
    
    private void blacklistCustomer() {
        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        if (selectedCustomer == null) {
            showError("Please select a customer to blacklist");
            return;
        }
        
        String reason = reasonTextArea.getText().trim();
        if (reason.isEmpty()) {
            showError("Please enter a reason for blacklisting");
            return;
        }
        
        String blacklistedBy = blacklistedByField.getText().trim();
        if (blacklistedBy.isEmpty()) {
            showError("Please enter who is blacklisting the customer");
            return;
        }
        
        try {
            int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            
            // Check if customer is already blacklisted
            if (hotelService.isCustomerBlacklisted(customerId)) {
                showError("Customer is already blacklisted");
                return;
            }
            
            Date expiryDate = null;
            if (hasExpiryCheckBox.isSelected()) {
                expiryDate = (Date) expiryDateSpinner.getValue();
            }
            
            int blacklistId = hotelService.blacklistCustomer(customerId, reason, blacklistedBy, 
                                                           expiryDate != null ? new java.sql.Date(expiryDate.getTime()) : null);
            
            showSuccess("Customer blacklisted successfully! Blacklist ID: " + blacklistId);
            clearForm();
            loadBlacklistData();
            updateStatistics();
            
        } catch (SQLException e) {
            showError("Error blacklisting customer: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid customer selection");
        }
    }
    
    private void removeFromBlacklist() {
        int selectedRow = blacklistTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a blacklist entry to remove");
            return;
        }
        
        String status = (String) blacklistTableModel.getValueAt(selectedRow, 7);
        if (!"Active".equals(status)) {
            showError("This blacklist entry is already inactive");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove this customer from the blacklist?",
            "Confirm Removal", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Get customer ID from the customer name (you might need to adjust this)
                String customerInfo = (String) blacklistTableModel.getValueAt(selectedRow, 1);
                // This is a simplified approach - in a real system, you'd store customer ID in the table
                
                String removedBy = JOptionPane.showInputDialog(this, "Enter your name:", 
                                                             System.getProperty("user.name", "Admin"));
                if (removedBy != null && !removedBy.trim().isEmpty()) {
                    // For this example, we'll need to find the customer ID
                    // In a real implementation, you'd store it in the table model
                    showInfo("Please implement customer ID lookup for removal");
                    // hotelService.removeFromBlacklist(customerId, removedBy);
                    // showSuccess("Customer removed from blacklist successfully!");
                    // loadBlacklistData();
                    // updateStatistics();
                }
                
            } catch (Exception e) {
                showError("Error removing from blacklist: " + e.getMessage());
            }
        }
    }
    
    private void updateBlacklistEntry() {
        int selectedRow = blacklistTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a blacklist entry to update");
            return;
        }
        
        showInfo("Update functionality would be implemented here");
        // Implementation would involve updating the selected blacklist entry
    }
    
    private void checkCustomerStatus() {
        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        if (selectedCustomer == null) {
            showError("Please select a customer");
            return;
        }
        
        try {
            int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            boolean isBlacklisted = hotelService.isCustomerBlacklisted(customerId);
            
            String message = "Customer Status: " + (isBlacklisted ? "BLACKLISTED" : "CLEAR");
            if (isBlacklisted) {
                message += "\n\nThis customer is currently blacklisted and cannot make new bookings.";
            } else {
                message += "\n\nThis customer is in good standing and can make bookings.";
            }
            
            JOptionPane.showMessageDialog(this, message, "Customer Status", 
                                        isBlacklisted ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            showError("Error checking customer status: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid customer selection");
        }
    }
    
    private void updateStatistics() {
        try {
            int activeCount = hotelService.getActiveBlacklistCount();
            List<BlacklistedCustomer> allBlacklisted = hotelService.getAllBlacklistedCustomers();
            int totalCount = allBlacklisted.size();
            int inactiveCount = totalCount - activeCount;
            
            statisticsLabel.setText(String.format(
                "Blacklist Statistics - Total: %d | Active: %d | Inactive: %d", 
                totalCount, activeCount, inactiveCount));
            
        } catch (SQLException e) {
            statisticsLabel.setText("Error loading statistics");
        }
    }
    
    private String generateBlacklistReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("BLACKLIST MANAGEMENT REPORT\n");
        report.append("===========================\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        // Statistics
        int activeCount = hotelService.getActiveBlacklistCount();
        List<BlacklistedCustomer> allBlacklisted = hotelService.getAllBlacklistedCustomers();
        int totalCount = allBlacklisted.size();
        
        report.append("BLACKLIST STATISTICS:\n");
        report.append("- Total Blacklisted Customers: ").append(totalCount).append("\n");
        report.append("- Currently Active: ").append(activeCount).append("\n");
        report.append("- Inactive/Expired: ").append(totalCount - activeCount).append("\n\n");
        
        // Recent blacklist entries
        report.append("RECENT BLACKLIST ENTRIES (Last 10):\n");
        List<BlacklistedCustomer> recentEntries = hotelService.getAllBlacklistedCustomers();
        int count = 0;
        for (BlacklistedCustomer entry : recentEntries) {
            if (count >= 10) break;
            report.append("- ").append(entry.getCustomerName())
                  .append(" (").append(entry.getBlacklistReason()).append(")")
                  .append(" - ").append(new SimpleDateFormat("yyyy-MM-dd").format(entry.getBlacklistDate()))
                  .append("\n");
            count++;
        }
        
        return report.toString();
    }
    
    private String generateReasonStatistics() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("BLACKLIST REASON STATISTICS\n");
        report.append("===========================\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        List<String> reasonStats = hotelService.getBlacklistReasonStatistics();
        
        report.append("BLACKLIST REASONS BY FREQUENCY:\n");
        for (String stat : reasonStats) {
            report.append("- ").append(stat).append("\n");
        }
        
        return report.toString();
    }
    
    private void showExpiredEntries() {
        // This would show expired blacklist entries
        showInfo("Expired entries functionality would be implemented here");
    }
    
    private void loadSelectedEntryToForm() {
        int selectedRow = blacklistTable.getSelectedRow();
        if (selectedRow != -1) {
            String reason = (String) blacklistTableModel.getValueAt(selectedRow, 3);
            String blacklistedBy = (String) blacklistTableModel.getValueAt(selectedRow, 4);
            
            reasonTextArea.setText(reason);
            blacklistedByField.setText(blacklistedBy);
        }
    }
    
    private void clearForm() {
        customerComboBox.setSelectedIndex(-1);
        reasonTextArea.setText("");
        blacklistedByField.setText(System.getProperty("user.name", "Admin"));
        hasExpiryCheckBox.setSelected(false);
        expiryDateSpinner.setEnabled(false);
        blacklistTable.clearSelection();
    }
    
    public void refreshData() {
        loadBlacklistData();
        updateStatistics();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}

