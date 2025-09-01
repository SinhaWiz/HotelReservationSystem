package com.hotel.view.panels;

import com.hotel.model.EnhancedHotelManagementService;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.view.panels.RefreshablePanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for managing hotel rooms
 */
public class RoomManagementPanel extends JPanel implements RefreshablePanel {
    private final EnhancedHotelManagementService hotelService;
    private JTable roomsTable;
    private DefaultTableModel tableModel;

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
    
    public RoomManagementPanel(EnhancedHotelManagementService hotelService) {
        this.hotelService = hotelService;
        this.roomsTable = new JTable();
        this.tableModel = new DefaultTableModel();
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
        TableRowSorter<DefaultTableModel> tableSorter = new TableRowSorter<>(tableModel);
        roomsTable.setRowSorter(tableSorter);
        
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
        
        // Style buttons (retain existing backgrounds where set later if any)
        updateStatusButton.setBackground(new Color(70, 130, 180));
        viewAvailabilityButton.setBackground(new Color(34, 139, 34));

        // Apply bold black font + foreground to all panel buttons
        Font boldFont = getFont().deriveFont(Font.BOLD);
        JButton[] buttons = {filterButton, clearFilterButton, updateStatusButton, viewAvailabilityButton, roomUtilizationButton, refreshButton};
        for (JButton b : buttons) {
            if (b != null) {
                b.setFont(boldFont);
                b.setForeground(Color.BLACK);
            }
        }

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
        JScrollPane tableScrollPane = new JScrollPane(roomsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));
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
        Room.RoomStatus[] statuses = Room.RoomStatus.values();
        String[] statusStrings = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusStrings[i] = statuses[i].toString();
        }

        String selectedStatus = (String) JOptionPane.showInputDialog(this,
            "Select new status for Room " + roomNumber + ":\nCurrent Status: " + currentStatus,
            "Update Room Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusStrings,
            currentStatus);
        
        if (selectedStatus != null && !selectedStatus.equals(currentStatus)) {
            try {
                Room.RoomStatus newStatus = Room.RoomStatus.valueOf(selectedStatus.toUpperCase().replace(' ', '_'));
                hotelService.updateRoomStatus(roomId, newStatus);
                refreshData();
                JOptionPane.showMessageDialog(this, 
                    "Room " + roomNumber + " status updated to " + selectedStatus,
                    "Status Updated", 
                    JOptionPane.INFORMATION_MESSAGE);
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
        SwingUtilities.invokeLater(this::refreshDataImpl);
    }

    private void refreshDataImpl() {
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
    }

    private void populateTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Room room : rooms) {
            RoomType type = room.getRoomType();
            Date cleanedDate = room.getLastCleaned();
            String amenities = room.getAmenities();
            String notes = room.getNotes();
            
            Object[] row = new Object[]{
                room.getRoomId(),
                room.getRoomNumber(),
                type != null ? type.getTypeName() : "N/A",
                type != null ? String.format("$%.2f", type.getBasePrice()) : "N/A",
                type != null ? type.getMaxOccupancy() : "N/A",
                room.getFloorNumber(),
                room.getRoomStatusString(),
                amenities != null ? amenities : "",
                cleanedDate != null ? dateFormat.format(cleanedDate) : "N/A",
                notes != null ? notes : ""
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
            Room.RoomStatus status = room.getStatus();
            if (status == Room.RoomStatus.AVAILABLE) {
                availableRooms++;
            } else if (status == Room.RoomStatus.OCCUPIED) {
                occupiedRooms++;
            } else if (status == Room.RoomStatus.MAINTENANCE || status == Room.RoomStatus.OUT_OF_ORDER) {
                maintenanceRooms++;
            }
        }
        
        double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
        
        totalRoomsLabel.setText(String.valueOf(totalRooms));
        availableRoomsLabel.setText(String.valueOf(availableRooms));
        occupiedRoomsLabel.setText(String.valueOf(occupiedRooms));
        maintenanceRoomsLabel.setText(String.valueOf(maintenanceRooms));
        occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
    }
    
    private void loadRoomTypes() {
        try {
            List<RoomType> roomTypes = hotelService.getAllRoomTypes();
            typeFilterCombo.removeAllItems();
            typeFilterCombo.addItem("All Types");
            for (RoomType type : roomTypes) {
                typeFilterCombo.addItem(type.getTypeName());
            }
        } catch (Exception e) {
            // Ignore errors loading room types
        }
    }

    /**
     * Dialog for checking room availability
     */
    private static class RoomAvailabilityDialog extends JDialog {
        private final EnhancedHotelManagementService hotelService;
        private final JTextField checkInField;
        private final JTextField checkOutField;
        private final DefaultTableModel tableModel;
    
        public RoomAvailabilityDialog(JFrame parent, EnhancedHotelManagementService hotelService) {
            super(parent, "Check Room Availability", true);
            this.hotelService = hotelService;
            checkInField = new JTextField(10);
            checkOutField = new JTextField(10);
            tableModel = new DefaultTableModel(new String[]{"Room Number", "Room Type", "Base Price", "Max Occupancy", "Amenities"}, 0);
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
            datePanel.add(checkInField);
            
            datePanel.add(new JLabel("Check-Out Date (YYYY-MM-DD):"));
            datePanel.add(checkOutField);
            
            JButton checkButton = new JButton("Check Availability");
            checkButton.addActionListener(e -> checkAvailability());
            datePanel.add(checkButton);
            
            add(datePanel, BorderLayout.NORTH);
            
            // Results table
            JTable availabilityTable = new JTable(tableModel);
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
    private static class RoomUtilizationDialog extends JDialog {
        private final EnhancedHotelManagementService hotelService;
        private final JTextField startDateField;
        private final JTextField endDateField;
        private final DefaultTableModel tableModel;
    
        public RoomUtilizationDialog(JFrame parent, EnhancedHotelManagementService hotelService) {
            super(parent, "Room Utilization Statistics", true);
            this.hotelService = hotelService;
            startDateField = new JTextField(10);
            endDateField = new JTextField(10);
            tableModel = new DefaultTableModel(new String[]{"Room Type", "Total Rooms", "Occupied Days", "Available Days", "Utilization %"}, 0);
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
            datePanel.add(startDateField);
            
            datePanel.add(new JLabel("End Date (YYYY-MM-DD):"));
            datePanel.add(endDateField);
            
            JButton generateButton = new JButton("Generate Report");
            generateButton.addActionListener(e -> generateUtilizationReport());
            datePanel.add(generateButton);
            
            add(datePanel, BorderLayout.NORTH);
            
            // Results table
            JTable utilizationTable = new JTable(tableModel);
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
                
                Map<RoomType, Double> utilizationStats = hotelService.getRoomUtilizationStats(startDate, endDate);
                
                tableModel.setRowCount(0);
                
                if (utilizationStats.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "No utilization data found for the selected period.",
                        "No Data", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    for (Map.Entry<RoomType, Double> entry : utilizationStats.entrySet()) {
                        RoomType roomType = entry.getKey();
                        Double utilization = entry.getValue();
                        
                        Object[] row = {
                            roomType.getTypeName(),
                            "N/A", // Total rooms not available in this data
                            "N/A", // Occupied days not available in this data
                            "N/A", // Available days not available in this data
                            String.format("%.1f%%", utilization != null ? utilization : 0.0)
                        };
                        tableModel.addRow(row);
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
}
