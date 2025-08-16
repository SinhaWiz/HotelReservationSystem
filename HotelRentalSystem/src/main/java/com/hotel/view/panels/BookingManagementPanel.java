package com.hotel.view.panels;

import com.hotel.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing hotel bookings
 */
public class BookingManagementPanel extends JPanel implements RefreshablePanel {
    
    private EnhancedHotelManagementService hotelService;

    // Table components
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JScrollPane tableScrollPane;
    
    // Search and filter components
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JButton searchButton;
    private JButton clearFilterButton;
    
    // Action buttons
    private JButton newBookingButton;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton cancelBookingButton;
    private JButton viewDetailsButton;
    private JButton refreshButton;
    
    // Date range components
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton dateFilterButton;
    
    private static final String[] COLUMN_NAMES = {
        "Booking ID", "Customer Name", "Room Number", "Room Type", 
        "Check-In Date", "Check-Out Date", "Total Amount", "Status", 
        "Payment Status", "Special Requests"
    };
    
    public BookingManagementPanel(EnhancedHotelManagementService hotelService) {
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
        
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bookingsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Booking ID
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Customer Name
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Room Number
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Room Type
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Check-In Date
        bookingsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Check-Out Date
        bookingsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total Amount
        bookingsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        bookingsTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Payment Status
        bookingsTable.getColumnModel().getColumn(9).setPreferredWidth(200); // Special Requests
        
        // Table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        bookingsTable.setRowSorter(tableSorter);
        
        tableScrollPane = new JScrollPane(bookingsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));
        
        // Search and filter components
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by customer name, room number, or booking ID");
        
        statusFilterCombo = new JComboBox<>(new String[]{
            "All Statuses", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED", "NO_SHOW"
        });
        
        searchButton = new JButton("Search");
        clearFilterButton = new JButton("Clear Filters");
        
        // Date range components
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        dateFilterButton = new JButton("Filter by Date");
        
        // Set default date range (current month)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDateField.setText(dateFormat.format(cal.getTime()));
        
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        endDateField.setText(dateFormat.format(cal.getTime()));
        
        // Action buttons
        newBookingButton = new JButton("New Booking");
        checkInButton = new JButton("Check In");
        checkOutButton = new JButton("Check Out");
        cancelBookingButton = new JButton("Cancel Booking");
        viewDetailsButton = new JButton("View Details");
        refreshButton = new JButton("Refresh");
        
        // Style buttons
        newBookingButton.setBackground(new Color(34, 139, 34));
        newBookingButton.setForeground(Color.WHITE);
        checkInButton.setBackground(new Color(70, 130, 180));
        checkInButton.setForeground(Color.WHITE);
        checkOutButton.setBackground(new Color(255, 140, 0));
        checkOutButton.setForeground(Color.WHITE);
        cancelBookingButton.setBackground(new Color(220, 20, 60));
        cancelBookingButton.setForeground(Color.WHITE);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Search and filters
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
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilterCombo);
        
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(clearFilterButton);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Date range panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(BorderFactory.createTitledBorder("Date Range Filter"));
        
        datePanel.add(new JLabel("From:"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("To:"));
        datePanel.add(endDateField);
        datePanel.add(dateFilterButton);
        datePanel.add(new JLabel("(Format: YYYY-MM-DD)"));
        
        panel.add(datePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));
        
        panel.add(newBookingButton);
        panel.add(checkInButton);
        panel.add(checkOutButton);
        panel.add(cancelBookingButton);
        panel.add(viewDetailsButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        
        // Filter functionality
        statusFilterCombo.addActionListener(e -> applyStatusFilter());
        clearFilterButton.addActionListener(e -> clearFilters());
        dateFilterButton.addActionListener(e -> applyDateFilter());
        
        // Action buttons
        newBookingButton.addActionListener(e -> showNewBookingDialog());
        checkInButton.addActionListener(e -> performCheckIn());
        checkOutButton.addActionListener(e -> performCheckOut());
        cancelBookingButton.addActionListener(e -> cancelBooking());
        viewDetailsButton.addActionListener(e -> viewBookingDetails());
        refreshButton.addActionListener(e -> refreshData());
        
        // Table selection
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter("(?i)" + searchText);
            tableSorter.setRowFilter(filter);
        }
    }
    
    private void applyStatusFilter() {
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        if ("All Statuses".equals(selectedStatus)) {
            // If search is active, keep search filter
            if (!searchField.getText().trim().isEmpty()) {
                performSearch();
            } else {
                tableSorter.setRowFilter(null);
            }
        } else {
            RowFilter<DefaultTableModel, Object> statusFilter = RowFilter.regexFilter(selectedStatus, 7); // Status column
            
            // Combine with search filter if active
            if (!searchField.getText().trim().isEmpty()) {
                RowFilter<DefaultTableModel, Object> searchFilter = RowFilter.regexFilter("(?i)" + searchField.getText().trim());
                RowFilter<DefaultTableModel, Object> combinedFilter = RowFilter.andFilter(java.util.Arrays.asList(searchFilter, statusFilter));
                tableSorter.setRowFilter(combinedFilter);
            } else {
                tableSorter.setRowFilter(statusFilter);
            }
        }
    }
    
    private void applyDateFilter() {
        try {
            String startDateStr = startDateField.getText().trim();
            String endDateStr = endDateField.getText().trim();
            
            if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both start and end dates.");
                return;
            }
            
            // Parse dates and filter bookings
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            
            List<Booking> bookings = hotelService.getBookingsByDateRange(startDate, endDate);
            populateTable(bookings);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error applying date filter: " + e.getMessage(),
                "Date Filter Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFilters() {
        searchField.setText("");
        statusFilterCombo.setSelectedIndex(0);
        tableSorter.setRowFilter(null);
        refreshData();
    }
    
    private void showNewBookingDialog() {
        NewBookingDialog dialog = new NewBookingDialog((JFrame) SwingUtilities.getWindowAncestor(this), hotelService);
        dialog.setVisible(true);
        if (dialog.isBookingCreated()) {
            refreshData();
        }
    }
    
    private void performCheckIn() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to check in.");
            return;
        }
        
        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        int bookingId = (Integer) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        
        if (!"CONFIRMED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only confirmed bookings can be checked in.");
            return;
        }
        
        try {
            boolean success = hotelService.checkInCustomer(bookingId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Customer checked in successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Check-in failed. Please try again.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error during check-in: " + e.getMessage(),
                "Check-In Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performCheckOut() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to check out.");
            return;
        }
        
        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        int bookingId = (Integer) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        
        if (!"CHECKED_IN".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only checked-in bookings can be checked out.");
            return;
        }
        
        try {
            boolean success = hotelService.checkOutCustomer(bookingId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Customer checked out successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Check-out failed. Please try again.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error during check-out: " + e.getMessage(),
                "Check-Out Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }
        
        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        int bookingId = (Integer) tableModel.getValueAt(modelRow, 0);
        String customerName = (String) tableModel.getValueAt(modelRow, 1);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        
        if ("CHECKED_OUT".equals(status) || "CANCELLED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This booking cannot be cancelled.");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking #" + bookingId + " for " + customerName + "?",
            "Cancel Booking Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean success = hotelService.cancelBooking(bookingId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Booking cancellation failed. Please try again.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error cancelling booking: " + e.getMessage(),
                    "Cancellation Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewBookingDetails() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to view details.");
            return;
        }
        
        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        int bookingId = (Integer) tableModel.getValueAt(modelRow, 0);
        
        try {
            Booking booking = hotelService.getBookingById(bookingId);
            if (booking != null) {
                BookingDetailsDialog dialog = new BookingDetailsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), booking);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Booking details not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading booking details: " + e.getMessage(),
                "Details Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = bookingsTable.getSelectedRow() != -1;
        checkInButton.setEnabled(hasSelection);
        checkOutButton.setEnabled(hasSelection);
        cancelBookingButton.setEnabled(hasSelection);
        viewDetailsButton.setEnabled(hasSelection);
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Booking> bookings = hotelService.getCurrentReservations();
                populateTable(bookings);
                updateButtonStates();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing booking data: " + e.getMessage(),
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void populateTable(List<Booking> bookings) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Booking booking : bookings) {
            Object[] row = new Object[]{
                booking.getBookingId(),
                booking.getCustomer() != null ? booking.getCustomer().getFullName() : "Unknown",
                booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "N/A",
                booking.getRoom() != null && booking.getRoom().getRoomType() != null ? 
                    booking.getRoom().getRoomType().getTypeName() : "N/A",
                dateFormat.format(booking.getCheckInDate()),
                dateFormat.format(booking.getCheckOutDate()),
                String.format("$%.2f", booking.getTotalAmount()),
                booking.getBookingStatusString(),
                booking.getPaymentStatusString(),
                booking.getSpecialRequests() != null ? booking.getSpecialRequests() : ""
            };
            tableModel.addRow(row);
        }
    }
}

/**
 * Dialog for displaying detailed booking information
 */
class BookingDetailsDialog extends JDialog {
    private Booking booking;
    
    public BookingDetailsDialog(JFrame parent, Booking booking) {
        super(parent, "Booking Details - #" + booking.getBookingId(), true);
        this.booking = booking;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        
        int row = 0;
        
        // Booking Information
        addDetailRow(detailsPanel, gbc, row++, "Booking ID:", String.valueOf(booking.getBookingId()));
        addDetailRow(detailsPanel, gbc, row++, "Booking Date:", 
            booking.getBookingDate() != null ? dateFormat.format(booking.getBookingDate()) : "N/A");
        addDetailRow(detailsPanel, gbc, row++, "Status:", booking.getBookingStatusString());
        addDetailRow(detailsPanel, gbc, row++, "Payment Status:", booking.getPaymentStatusString());
        
        // Customer Information
        if (booking.getCustomer() != null) {
            Customer customer = booking.getCustomer();
            addSectionHeader(detailsPanel, gbc, row++, "Customer Information");
            addDetailRow(detailsPanel, gbc, row++, "Name:", customer.getFullName());
            addDetailRow(detailsPanel, gbc, row++, "Email:", customer.getEmail());
            addDetailRow(detailsPanel, gbc, row++, "Phone:", customer.getPhone());
        }
        
        // Room Information
        if (booking.getRoom() != null) {
            Room room = booking.getRoom();
            addSectionHeader(detailsPanel, gbc, row++, "Room Information");
            addDetailRow(detailsPanel, gbc, row++, "Room Number:", room.getRoomNumber());
            if (room.getRoomType() != null) {
                addDetailRow(detailsPanel, gbc, row++, "Room Type:", room.getRoomType().getTypeName());
                addDetailRow(detailsPanel, gbc, row++, "Base Price:", String.format("$%.2f", room.getRoomType().getBasePrice()));
            }
        }
        
        // Stay Information
        addSectionHeader(detailsPanel, gbc, row++, "Stay Information");
        addDetailRow(detailsPanel, gbc, row++, "Check-In Date:", dateFormat.format(booking.getCheckInDate()));
        addDetailRow(detailsPanel, gbc, row++, "Check-Out Date:", dateFormat.format(booking.getCheckOutDate()));
        addDetailRow(detailsPanel, gbc, row++, "Number of Nights:", String.valueOf(booking.getNumberOfNights()));
        
        if (booking.getActualCheckIn() != null) {
            addDetailRow(detailsPanel, gbc, row++, "Actual Check-In:", 
                dateFormat.format(booking.getActualCheckIn()) + " " + timeFormat.format(booking.getActualCheckIn()));
        }
        
        if (booking.getActualCheckOut() != null) {
            addDetailRow(detailsPanel, gbc, row++, "Actual Check-Out:", 
                dateFormat.format(booking.getActualCheckOut()) + " " + timeFormat.format(booking.getActualCheckOut()));
        }
        
        // Financial Information
        addSectionHeader(detailsPanel, gbc, row++, "Financial Information");
        addDetailRow(detailsPanel, gbc, row++, "Total Amount:", String.format("$%.2f", booking.getTotalAmount()));
        addDetailRow(detailsPanel, gbc, row++, "Discount Applied:", String.format("$%.2f", booking.getDiscountApplied()));
        addDetailRow(detailsPanel, gbc, row++, "Extra Charges:", String.format("$%.2f", booking.getExtraCharges()));
        addDetailRow(detailsPanel, gbc, row++, "Final Amount:", String.format("$%.2f", booking.getFinalAmount()));
        
        // Special Requests
        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty()) {
            addSectionHeader(detailsPanel, gbc, row++, "Special Requests");
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
            JTextArea requestsArea = new JTextArea(booking.getSpecialRequests(), 3, 30);
            requestsArea.setEditable(false);
            requestsArea.setBackground(getBackground());
            requestsArea.setBorder(BorderFactory.createLoweredBevelBorder());
            detailsPanel.add(new JScrollPane(requestsArea), gbc);
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
