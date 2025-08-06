package com.hotel.view.panels;

import com.hotel.service.HotelManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Panel for managing hotel rooms
 */
public class RoomManagementPanel extends JPanel implements RefreshablePanel {
    
    private HotelManagementService hotelService;
    
    // Table components
    private JTable roomsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JScrollPane tableScrollPane;
    
    // Filter components
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JButton filterButton;
    private JButton clearFilterButton;
    
    // Action buttons
    private JButton updateStatusButton;
    private JButton viewAvailabilityButton;
    private JButton roomUtilizationButton;
    private JButton refreshButton;
    
    // Statistics panel
    private JLabel totalRoomsLabel;
    private JLabel availableRoomsLabel;
    private JLabel occupiedRoomsLabel;
    private JLabel maintenanceRoomsLabel;
    private JLabel occupancyRateLabel;
    
    private static final String[] COLUMN_NAMES = {
        "Room ID", "Room Number", "Room Type", "Base Price", "Max Occupancy", 
        "Floor", "Status", "Amenities", "Last Cleaned", "Notes"
    };
    
    public RoomManagementPanel(HotelManagementService hotelService) {
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
        
        roomsTable = new JTable(tableModel);
        roomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        roomsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        roomsTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Room ID
        roomsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Room Number
        roomsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Room Type
        roomsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Base Price
        roomsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Max Occupancy
        roomsTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Floor
        roomsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
        roomsTable.getColumnModel().getColumn(7).setPreferredWidth(200); // Amenities
        roomsTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Last Cleaned
        roomsTable.getColumnModel().getColumn(9).setPreferredWidth(150); // Notes
        
        // Table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        roomsTable.setRowSorter(tableSorter);
        
        tableScrollPane = new JScrollPane(roomsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));
        
        // Filter components
        statusFilterCombo = new JComboBox<>(new String[]{
            "All Statuses", "AVAILABLE", "OCCUPIED", "MAINTENANCE", "OUT_OF_ORDER"
        });
        
        typeFilterCombo = new JComboBox<>();
        typeFilterCombo.addItem("All Types");
        
        filterButton = new JButton("Apply Filter");
        clearFilterButton = new JButton("Clear Filter");
        
        // Action buttons
        updateStatusButton = new JButton("Update Status");
        viewAvailabilityButton = new JButton("Check Availability");
        roomUtilizationButton = new JButton("Room Utilization");
        refreshButton = new JButton("Refresh");
        
        // Style buttons
        updateStatusButton.setBackground(new Color(70, 130, 180));
        updateStatusButton.setForeground(Color.WHITE);
        viewAvailabilityButton.setBackground(new Color(34, 139, 34));
        viewAvailabilityButton.setForeground(Color.WHITE);
        
        // Statistics labels
        totalRoomsLabel = new JLabel("0");
        availableRoomsLabel = new JLabel("0");
        occupiedRoomsLabel = new JLabel("0");
        maintenanceRoomsLabel = new JLabel("0");
        occupancyRateLabel = new JLabel("0.0%");
        
        // Style statistics labels
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        totalRoomsLabel.setFont(statsFont);
        availableRoomsLabel.setFont(statsFont);
        occupiedRoomsLabel.setFont(statsFont);
        maintenanceRoomsLabel.setFont(statsFont);
        occupancyRateLabel.setFont(statsFont);
        
        totalRoomsLabel.setForeground(new Color(70, 130, 180));
        availableRoomsLabel.setForeground(new Color(34, 139, 34));
        occupiedRoomsLabel.setForeground(new Color(220, 20, 60));
        maintenanceRoomsLabel.setForeground(new Color(255, 140, 0));
        occupancyRateLabel.setForeground(new Color(138, 43, 226));
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
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Rooms"));
        
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(filterButton);
        filterPanel.add(clearFilterButton);
        
        panel.add(filterPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Room Statistics"));
        
        panel.add(createStatCard("Total Rooms", totalRoomsLabel, new Color(70, 130, 180)));
        panel.add(createStatCard("Available", availableRoomsLabel, new Color(34, 139, 34)));
        panel.add(createStatCard("Occupied", occupiedRoomsLabel, new Color(220, 20, 60)));
        panel.add(createStatCard("Maintenance", maintenanceRoomsLabel, new Color(255, 140, 0)));
        panel.add(createStatCard("Occupancy Rate", occupancyRateLabel, new Color(138, 43, 226)));
        
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
        
        panel.add(updateStatusButton);
        panel.add(viewAvailabilityButton);
        panel.add(roomUtilizationButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Filter functionality
        filterButton.addActionListener(e -> applyFilters());
        clearFilterButton.addActionListener(e -> clearFilters());
        
        // Action buttons
        updateStatusButton.addActionListener(e -> updateRoomStatus());
        viewAvailabilityButton.addActionListener(e -> checkRoomAvailability());
        roomUtilizationButton.addActionListener(e -> showRoomUtilization());
        refreshButton.addActionListener(e -> refreshData());
        
        // Table selection
        roomsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void applyFilters() {
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedType = (String) typeFilterCombo.getSelectedItem();
        
        try {
            List<Room> rooms;
            
            if ("All Statuses".equals(selectedStatus) && "All Types".equals(selectedType)) {
                rooms = hotelService.getAllRooms();
            } else if (!"All Statuses".equals(selectedStatus)) {
                Room.RoomStatus status = Room.RoomStatus.valueOf(selectedStatus);
                rooms = hotelService.getRoomsByStatus(status);
            } else {
                rooms = hotelService.getAllRooms();
                // Filter by type if needed (would require additional service method)
            }
            
            populateTable(rooms);
            updateStatistics(rooms);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error applying filters: " + e.getMessage(),
                "Filter Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFilters() {
        statusFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setSelectedIndex(0);
        refreshData();
    }
    
    private void updateRoomStatus() {
        int selectedRow = roomsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to update status.");
            return;
        }
        
        int modelRow = roomsTable.convertRowIndexToModel(selectedRow);
        int roomId = (Integer) tableModel.getValueAt(modelRow, 0);
        String roomNumber = (String) tableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 6);
        
        // Show status selection dialog
        String[] statuses = {"AVAILABLE", "OCCUPIED", "MAINTENANCE", "OUT_OF_ORDER"};
        String selectedStatus = (String) JOptionPane.showInputDialog(this,
            "Select new status for Room " + roomNumber + ":\\nCurrent Status: " + currentStatus,
            "Update Room Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            currentStatus);
        
        if (selectedStatus != null && !selectedStatus.equals(currentStatus)) {
            try {
                Room.RoomStatus newStatus = Room.RoomStatus.valueOf(selectedStatus);
                boolean success = hotelService.updateRoomStatus(roomId, newStatus);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Room " + roomNumber + " status updated to " + selectedStatus,
                        "Status Updated", 
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update room status.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error updating room status: " + e.getMessage(),
                    "Update Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void checkRoomAvailability() {
        RoomAvailabilityDialog dialog = new RoomAvailabilityDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), hotelService);
        dialog.setVisible(true);
    }
    
    private void showRoomUtilization() {
        RoomUtilizationDialog dialog = new RoomUtilizationDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), hotelService);
        dialog.setVisible(true);
    }
    
    private void updateButtonStates() {
        boolean hasSelection = roomsTable.getSelectedRow() != -1;
        updateStatusButton.setEnabled(hasSelection);
    }
    
    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Room> rooms = hotelService.getAllRooms();
                populateTable(rooms);
                updateStatistics(rooms);
                loadRoomTypes();
                updateButtonStates();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error refreshing room data: " + e.getMessage(),
                    "Refresh Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void loadRoomTypes() {
        try {
            List<RoomType> roomTypes = hotelService.getAllRoomTypes();
            typeFilterCombo.removeAllItems();
            typeFilterCombo.addItem("All Types");
            
            for (RoomType roomType : roomTypes) {
                typeFilterCombo.addItem(roomType.getTypeName());
            }
        } catch (Exception e) {
            // Ignore room type loading errors
        }
    }
    
    private void populateTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Room room : rooms) {
            Object[] row = new Object[]{
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A",
                room.getRoomType() != null ? String.format("$%.2f", room.getRoomType().getBasePrice()) : "N/A",
                room.getRoomType() != null ? room.getRoomType().getMaxOccupancy() : "N/A",
                room.getFloor(),
                room.getRoomStatusString(),
                room.getAmenities() != null ? room.getAmenities() : "",
                room.getLastCleaned() != null ? dateFormat.format(room.getLastCleaned()) : "N/A",
                room.getNotes() != null ? room.getNotes() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStatistics(List<Room> rooms) {
        int totalRooms = rooms.size();
        int availableRooms = 0;
        int occupiedRooms = 0;
        int maintenanceRooms = 0;
        
        for (Room room : rooms) {
            switch (room.getRoomStatus()) {
                case AVAILABLE:
                    availableRooms++;
                    break;
                case OCCUPIED:
                    occupiedRooms++;
                    break;
                case MAINTENANCE:
                case OUT_OF_ORDER:
                    maintenanceRooms++;
                    break;
            }
        }
        
        double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
        
        totalRoomsLabel.setText(String.valueOf(totalRooms));
        availableRoomsLabel.setText(String.valueOf(availableRooms));
        occupiedRoomsLabel.setText(String.valueOf(occupiedRooms));
        maintenanceRoomsLabel.setText(String.valueOf(maintenanceRooms));
        occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
    }
}

/**
 * Dialog for checking room availability
 */
class RoomAvailabilityDialog extends JDialog {
    private HotelManagementService hotelService;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JTable availabilityTable;
    private DefaultTableModel tableModel;
    
    public RoomAvailabilityDialog(JFrame parent, HotelManagementService hotelService) {
        super(parent, "Check Room Availability", true);
        this.hotelService = hotelService;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Date selection panel
        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Select Date Range"));
        
        datePanel.add(new JLabel("Check-In Date (YYYY-MM-DD):"));
        checkInField = new JTextField(10);
        datePanel.add(checkInField);
        
        datePanel.add(new JLabel("Check-Out Date (YYYY-MM-DD):"));
        checkOutField = new JTextField(10);
        datePanel.add(checkOutField);
        
        JButton checkButton = new JButton("Check Availability");
        checkButton.addActionListener(e -> checkAvailability());
        datePanel.add(checkButton);
        
        add(datePanel, BorderLayout.NORTH);
        
        // Results table
        String[] columnNames = {"Room Number", "Room Type", "Base Price", "Max Occupancy", "Amenities"};
        tableModel = new DefaultTableModel(columnNames, 0);
        availabilityTable = new JTable(tableModel);
        availabilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(availabilityTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default dates (today and tomorrow)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        checkInField.setText(dateFormat.format(cal.getTime()));
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        checkOutField.setText(dateFormat.format(cal.getTime()));
    }
    
    private void checkAvailability() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date checkInDate = dateFormat.parse(checkInField.getText().trim());
            Date checkOutDate = dateFormat.parse(checkOutField.getText().trim());
            
            if (checkOutDate.before(checkInDate) || checkOutDate.equals(checkInDate)) {
                JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.");
                return;
            }
            
            List<Room> availableRooms = hotelService.getAvailableRooms(checkInDate, checkOutDate);
            
            tableModel.setRowCount(0);
            
            if (availableRooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No rooms available for the selected dates.",
                    "No Availability", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Room room : availableRooms) {
                    Object[] row = {
                        room.getRoomNumber(),
                        room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A",
                        room.getRoomType() != null ? String.format("$%.2f", room.getRoomType().getBasePrice()) : "N/A",
                        room.getRoomType() != null ? room.getRoomType().getMaxOccupancy() : "N/A",
                        room.getAmenities() != null ? room.getAmenities() : ""
                    };
                    tableModel.addRow(row);
                }
                
                JOptionPane.showMessageDialog(this, 
                    availableRooms.size() + " rooms available for the selected dates.",
                    "Availability Results", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error checking availability: " + e.getMessage(),
                "Availability Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * Dialog for showing room utilization statistics
 */
class RoomUtilizationDialog extends JDialog {
    private HotelManagementService hotelService;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTable utilizationTable;
    private DefaultTableModel tableModel;
    
    public RoomUtilizationDialog(JFrame parent, HotelManagementService hotelService) {
        super(parent, "Room Utilization Statistics", true);
        this.hotelService = hotelService;
        initializeDialog();
    }
    
    private void initializeDialog() {
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Date selection panel
        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Select Date Range"));
        
        datePanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        startDateField = new JTextField(10);
        datePanel.add(startDateField);
        
        datePanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        endDateField = new JTextField(10);
        datePanel.add(endDateField);
        
        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateUtilizationReport());
        datePanel.add(generateButton);
        
        add(datePanel, BorderLayout.NORTH);
        
        // Results table
        String[] columnNames = {"Room Type", "Total Rooms", "Occupied Days", "Available Days", "Utilization %"};
        tableModel = new DefaultTableModel(columnNames, 0);
        utilizationTable = new JTable(tableModel);
        utilizationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(utilizationTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default dates (last 30 days)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        endDateField.setText(dateFormat.format(cal.getTime()));
        
        cal.add(Calendar.DAY_OF_MONTH, -30);
        startDateField.setText(dateFormat.format(cal.getTime()));
    }
    
    private void generateUtilizationReport() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateField.getText().trim());
            Date endDate = dateFormat.parse(endDateField.getText().trim());
            
            if (endDate.before(startDate)) {
                JOptionPane.showMessageDialog(this, "End date must be after start date.");
                return;
            }
            
            List<Object[]> utilizationStats = hotelService.getRoomUtilizationStats(startDate, endDate);
            
            tableModel.setRowCount(0);
            
            if (utilizationStats.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No utilization data found for the selected period.",
                    "No Data", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Object[] stat : utilizationStats) {
                    tableModel.addRow(stat);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating utilization report: " + e.getMessage(),
                "Report Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

