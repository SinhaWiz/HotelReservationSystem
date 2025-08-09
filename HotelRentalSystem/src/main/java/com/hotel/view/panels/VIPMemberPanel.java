package com.hotel.view.panels;

import com.hotel.service.HotelManagementService;
import com.hotel.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing VIP members
 */
public class VIPMemberPanel extends JPanel implements RefreshablePanel {
    
    private HotelManagementService hotelService;
    
    // Table components
    private JTable vipMembersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JScrollPane tableScrollPane;
    
    // Filter components
    private JComboBox<String> levelFilterCombo;
    private JButton filterButton;
    private JButton clearFilterButton;
    
    // Action buttons
    private JButton viewDetailsButton;
    private JButton updateMemberButton;
    private JButton deactivateButton;
    private JButton processRenewalsButton;
    private JButton refreshButton;
    
    // Statistics panel
    private JLabel totalVIPLabel;
    private JLabel goldMembersLabel;
    private JLabel platinumMembersLabel;
    private JLabel diamondMembersLabel;
    
    private static final String[] COLUMN_NAMES = {
        "VIP ID", "Customer Name", "Email", "Phone", "Membership Level", 
        "Discount %", "Total Spent", "Loyalty Points", "Start Date", "End Date", "Status"
    };
    
    public VIPMemberPanel(HotelManagementService hotelService) {
        this.hotelService = hotelService;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeComponents() {
        // Table setup
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        vipMembersTable = new JTable(tableModel);
        vipMembersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vipMembersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        vipMembersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        vipMembersTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // VIP ID
        vipMembersTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Customer Name
        vipMembersTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        vipMembersTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Phone
        vipMembersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Membership Level
        vipMembersTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Discount %
        vipMembersTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total Spent
        vipMembersTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Loyalty Points
        vipMembersTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Start Date
        vipMembersTable.getColumnModel().getColumn(9).setPreferredWidth(100); // End Date
        vipMembersTable.getColumnModel().getColumn(10).setPreferredWidth(80); // Status
        
        // Table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        vipMembersTable.setRowSorter(tableSorter);
        
        tableScrollPane = new JScrollPane(vipMembersTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));
        
        // Filter components
        levelFilterCombo = new JComboBox<>(new String[]{
            "All Levels", "GOLD", "PLATINUM", "DIAMOND"
        });
        filterButton = new JButton("Filter");
        clearFilterButton = new JButton("Clear Filter");
        
        // Action buttons
        viewDetailsButton = new JButton("View Details");
        updateMemberButton = new JButton("Update Member");
        deactivateButton = new JButton("Deactivate");
        processRenewalsButton = new JButton("Process Renewals");
        refreshButton = new JButton("Refresh");
        
        // Style buttons
        processRenewalsButton.setBackground(new Color(70, 130, 180));
        processRenewalsButton.setForeground(Color.WHITE);
        deactivateButton.setBackground(new Color(220, 20, 60));
        deactivateButton.setForeground(Color.WHITE);
        
        // Statistics labels
        totalVIPLabel = new JLabel("0");
        goldMembersLabel = new JLabel("0");
        platinumMembersLabel = new JLabel("0");
        diamondMembersLabel = new JLabel("0");
        
        // Style statistics labels
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        totalVIPLabel.setFont(statsFont);
        goldMembersLabel.setFont(statsFont);
        platinumMembersLabel.setFont(statsFont);
        diamondMembersLabel.setFont(statsFont);
        
        totalVIPLabel.setForeground(new Color(70, 130, 180));
        goldMembersLabel.setForeground(new Color(255, 215, 0));
        platinumMembersLabel.setForeground(new Color(192, 192, 192));
        diamondMembersLabel.setForeground(new Color(185, 242, 255));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Statistics and filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter VIP Members"));
        
        filterPanel.add(new JLabel("Membership Level:"));
        filterPanel.add(levelFilterCombo);
        filterPanel.add(filterButton);
        filterPanel.add(clearFilterButton);
        
        panel.add(filterPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("VIP Member Statistics"));
        
        panel.add(createStatCard("Total VIP Members", totalVIPLabel, new Color(70, 130, 180)));
        panel.add(createStatCard("Gold Members", goldMembersLabel, new Color(255, 215, 0)));
        panel.add(createStatCard("Platinum Members", platinumMembersLabel, new Color(192, 192, 192)));
        panel.add(createStatCard("Diamond Members", diamondMembersLabel, new Color(185, 242, 255)));
        
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
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));
        
        panel.add(viewDetailsButton);
        panel.add(updateMemberButton);
        panel.add(deactivateButton);
        panel.add(processRenewalsButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Filter functionality
        filterButton.addActionListener(e -> applyFilter());
        clearFilterButton.addActionListener(e -> clearFilter());
        
        // Action buttons
        viewDetailsButton.addActionListener(e -> viewMemberDetails());
        updateMemberButton.addActionListener(e -> updateMember());
        deactivateButton.addActionListener(e -> deactivateMember());
        processRenewalsButton.addActionListener(e -> processRenewals());
        refreshButton.addActionListener(e -> refreshData());
        
        // Table selection
        vipMembersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void applyFilter() {
        String selectedLevel = (String) levelFilterCombo.getSelectedItem();
        if ("All Levels".equals(selectedLevel)) {
            refreshData();
        } else {
            try {
                List<VIPMember> vipMembers = hotelService.getVIPMembersDetailed(selectedLevel);
                populateTable(vipMembers);
                updateStatistics(vipMembers);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error applying filter: " + e.getMessage(),
                    "Filter Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearFilter() {
        levelFilterCombo.setSelectedIndex(0);
        refreshData();
    }
    
    private void viewMemberDetails() {
        int selectedRow = vipMembersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a VIP member to view details.");
            return;
        }
        
        int modelRow = vipMembersTable.convertRowIndexToModel(selectedRow);
        int vipId = (Integer) tableModel.getValueAt(modelRow, 0);
        
        try {
            // Get VIP member details using the DAO
            VIPMember vipMember = null;
            List<VIPMember> allMembers = hotelService.getAllVIPMembers();
            for (VIPMember member : allMembers) {
                if (member.getVipId() == vipId) {
                    vipMember = member;
                    break;
                }
            }
            
            if (vipMember != null) {
                VIPMemberDetailsDialog dialog = new VIPMemberDetailsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), vipMember, hotelService);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "VIP member not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading VIP member details: " + e.getMessage(),
                "Details Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateMember() {
        int selectedRow = vipMembersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a VIP member to update.");
            return;
        }
        
        int modelRow = vipMembersTable.convertRowIndexToModel(selectedRow);
        int vipId = (Integer) tableModel.getValueAt(modelRow, 0);
        String memberName = (String) tableModel.getValueAt(modelRow, 1);
        
        // Show update dialog
        VIPMemberUpdateDialog dialog = new VIPMemberUpdateDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), hotelService, vipId, memberName);
        dialog.setVisible(true);
        
        if (dialog.isMemberUpdated()) {
            refreshData();
        }
    }
    
    private void deactivateMember() {
        int selectedRow = vipMembersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a VIP member to deactivate.");
            return;
        }
        
        int modelRow = vipMembersTable.convertRowIndexToModel(selectedRow);
        int vipId = (Integer) tableModel.getValueAt(modelRow, 0);
        String memberName = (String) tableModel.getValueAt(modelRow, 1);
        
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to deactivate VIP membership for " + memberName + "?\\n" +
            "This action cannot be undone.",
            "Deactivate VIP Membership",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean success = hotelService.deactivateVIPMember(vipId);
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "VIP membership deactivated successfully for " + memberName,
                        "Deactivation Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to deactivate VIP membership.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deactivating VIP membership: " + e.getMessage(),
                    "Deactivation Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processRenewals() {
        int option = JOptionPane.showConfirmDialog(this,
            "This will process VIP membership renewals for all eligible members.\\n" +
            "Members who no longer meet spending requirements will be expired.\\n\\n" +
            "Do you want to continue?",
            "Process VIP Renewals",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                // Show progress dialog
                JDialog progressDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                   "Processing VIP Renewals", true);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(this);
                progressDialog.add(new JLabel("Processing VIP renewals, please wait...", JLabel.CENTER));
                
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        hotelService.processVIPRenewals();
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            get(); // Check for exceptions
                            JOptionPane.showMessageDialog(VIPMemberPanel.this, 
                                "VIP renewals processed successfully!",
                                "Renewals Complete", 
                                JOptionPane.INFORMATION_MESSAGE);
                            refreshData();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(VIPMemberPanel.this, 
                                "Error processing VIP renewals: " + e.getCause().getMessage(),
                                "Renewal Error", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                
                worker.execute();
                progressDialog.setVisible(true);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error starting VIP renewal process: " + e.getMessage(),
                    "Renewal Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = vipMembersTable.getSelectedRow() != -1;
        viewDetailsButton.setEnabled(hasSelection);
        updateMemberButton.setEnabled(hasSelection);
        deactivateButton.setEnabled(hasSelection);
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<VIPMember> vipMembers = hotelService.getVIPMembersDetailed(null);
                populateTable(vipMembers);
                updateStatistics(vipMembers);
                updateButtonStates();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing VIP member data: " + e.getMessage(),
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void populateTable(List<VIPMember> vipMembers) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (VIPMember vipMember : vipMembers) {
            Object[] row = new Object[]{
                vipMember.getVipId(),
                vipMember.getCustomer() != null ? vipMember.getCustomer().getFullName() : "Unknown",
                vipMember.getCustomer() != null ? vipMember.getCustomer().getEmail() : "N/A",
                vipMember.getCustomer() != null ? vipMember.getCustomer().getPhone() : "N/A",
                vipMember.getMembershipLevelString(),
                String.format("%.1f%%", vipMember.getDiscountPercentage()),
                vipMember.getCustomer() != null ? 
                    String.format("$%.2f", vipMember.getCustomer().getTotalSpent()) : "$0.00",
                vipMember.getCustomer() != null ? 
                    vipMember.getCustomer().getLoyaltyPoints() : 0,
                vipMember.getMembershipStartDate() != null ? 
                    dateFormat.format(vipMember.getMembershipStartDate()) : "N/A",
                vipMember.getMembershipEndDate() != null ? 
                    dateFormat.format(vipMember.getMembershipEndDate()) : "Lifetime",
                vipMember.isActive() ? "Active" : "Inactive"
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStatistics(List<VIPMember> vipMembers) {
        int totalVIP = vipMembers.size();
        int goldCount = 0;
        int platinumCount = 0;
        int diamondCount = 0;
        
        for (VIPMember member : vipMembers) {
            if (member.isActive()) {
                switch (member.getMembershipLevel()) {
                    case GOLD:
                        goldCount++;
                        break;
                    case PLATINUM:
                        platinumCount++;
                        break;
                    case DIAMOND:
                        diamondCount++;
                        break;
                }
            }
        }
        
        totalVIPLabel.setText(String.valueOf(totalVIP));
        goldMembersLabel.setText(String.valueOf(goldCount));
        platinumMembersLabel.setText(String.valueOf(platinumCount));
        diamondMembersLabel.setText(String.valueOf(diamondCount));
    }
}

/**
 * Dialog for updating VIP member information
 */
class VIPMemberUpdateDialog extends JDialog {
    private HotelManagementService hotelService;
    private int vipId;
    private boolean memberUpdated = false;
    
    private JComboBox<String> levelCombo;
    private JTextField discountField;
    private JTextField endDateField;
    private JTextArea benefitsArea;
    
    public VIPMemberUpdateDialog(JFrame parent, HotelManagementService hotelService, 
                               int vipId, String memberName) {
        super(parent, "Update VIP Member - " + memberName, true);
        this.hotelService = hotelService;
        this.vipId = vipId;
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
        
        // Membership Level
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Membership Level:"), gbc);
        gbc.gridx = 1;
        levelCombo = new JComboBox<>(new String[]{"GOLD", "PLATINUM", "DIAMOND"});
        formPanel.add(levelCombo, gbc);
        
        // Discount Percentage
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Discount Percentage:"), gbc);
        gbc.gridx = 1;
        discountField = new JTextField(15);
        formPanel.add(discountField, gbc);
        
        // End Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        endDateField = new JTextField(15);
        endDateField.setToolTipText("Leave empty for lifetime membership");
        formPanel.add(endDateField, gbc);
        
        // Benefits
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Benefits:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        benefitsArea = new JTextArea(5, 15);
        JScrollPane benefitsScrollPane = new JScrollPane(benefitsArea);
        formPanel.add(benefitsScrollPane, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update Member");
        JButton cancelButton = new JButton("Cancel");
        
        updateButton.addActionListener(e -> updateMember());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default values
        setDefaultValues();
    }
    
    private void setDefaultValues() {
        // Set default discount based on level
        levelCombo.addActionListener(e -> {
            String selectedLevel = (String) levelCombo.getSelectedItem();
            VIPMember.MembershipLevel level = VIPMember.MembershipLevel.valueOf(selectedLevel);
            discountField.setText(String.valueOf(VIPMember.getDefaultDiscountForLevel(level)));
            benefitsArea.setText(VIPMember.getDefaultBenefitsForLevel(level));
        });
        
        // Trigger initial setup
        levelCombo.setSelectedIndex(0);
        levelCombo.getActionListeners()[0].actionPerformed(null);
    }
    
    private void updateMember() {
        try {
            // Create VIPMember object with updated values
            VIPMember vipMember = new VIPMember();
            vipMember.setVipId(vipId);
            vipMember.setMembershipLevelFromString((String) levelCombo.getSelectedItem());
            vipMember.setDiscountPercentage(Double.parseDouble(discountField.getText().trim()));
            vipMember.setBenefits(benefitsArea.getText().trim());
            
            // Parse end date if provided
            if (!endDateField.getText().trim().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date endDate = dateFormat.parse(endDateField.getText().trim());
                vipMember.setMembershipEndDate(endDate);
            }
            
            boolean success = hotelService.updateVIPMember(vipMember);
            if (success) {
                memberUpdated = true;
                JOptionPane.showMessageDialog(this, "VIP member updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update VIP member.");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating VIP member: " + e.getMessage(),
                "Update Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isMemberUpdated() {
        return memberUpdated;
    }
}

/**
 * Dialog for displaying detailed VIP member information
 */
class VIPMemberDetailsDialog extends JDialog {
    private VIPMember vipMember;
    private HotelManagementService hotelService;
    
    public VIPMemberDetailsDialog(JFrame parent, VIPMember vipMember, HotelManagementService hotelService) {
        super(parent, "VIP Member Details - " + 
              (vipMember.getCustomer() != null ? vipMember.getCustomer().getFullName() : "Unknown"), true);
        this.vipMember = vipMember;
        this.hotelService = hotelService;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        
        int row = 0;
        
        // VIP Information
        addDetailRow(detailsPanel, gbc, row++, "VIP ID:", String.valueOf(vipMember.getVipId()));
        addDetailRow(detailsPanel, gbc, row++, "Membership Level:", vipMember.getMembershipLevelString());
        addDetailRow(detailsPanel, gbc, row++, "Discount Percentage:", vipMember.getFormattedDiscountPercentage());
        addDetailRow(detailsPanel, gbc, row++, "Status:", vipMember.isActive() ? "Active" : "Inactive");
        
        if (vipMember.getMembershipStartDate() != null) {
            addDetailRow(detailsPanel, gbc, row++, "Start Date:", dateFormat.format(vipMember.getMembershipStartDate()));
        }
        
        if (vipMember.getMembershipEndDate() != null) {
            addDetailRow(detailsPanel, gbc, row++, "End Date:", dateFormat.format(vipMember.getMembershipEndDate()));
        } else {
            addDetailRow(detailsPanel, gbc, row++, "End Date:", "Lifetime");
        }
        
        // Customer Information
        if (vipMember.getCustomer() != null) {
            Customer customer = vipMember.getCustomer();
            addSectionHeader(detailsPanel, gbc, row++, "Customer Information");
            addDetailRow(detailsPanel, gbc, row++, "Name:", customer.getFullName());
            addDetailRow(detailsPanel, gbc, row++, "Email:", customer.getEmail());
            addDetailRow(detailsPanel, gbc, row++, "Phone:", customer.getPhone());
            addDetailRow(detailsPanel, gbc, row++, "Total Spent:", String.format("$%.2f", customer.getTotalSpent()));
            addDetailRow(detailsPanel, gbc, row++, "Loyalty Points:", String.valueOf(customer.getLoyaltyPoints()));
        }
        
        // Benefits
        if (vipMember.getBenefits() != null && !vipMember.getBenefits().trim().isEmpty()) {
            addSectionHeader(detailsPanel, gbc, row++, "VIP Benefits");
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
            JTextArea benefitsArea = new JTextArea(vipMember.getBenefits(), 4, 30);
            benefitsArea.setEditable(false);
            benefitsArea.setBackground(getBackground());
            benefitsArea.setBorder(BorderFactory.createLoweredBevelBorder());
            detailsPanel.add(new JScrollPane(benefitsArea), gbc);
            gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        }
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String header) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerLabel.setForeground(new Color(70, 130, 180));
        panel.add(headerLabel, gbc);
        gbc.gridwidth = 1;
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        panel.add(new JLabel(value != null ? value : "N/A"), gbc);
    }
}

