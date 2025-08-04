package com.hotel.view.panels;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PropertyDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.Booking;
import com.hotel.model.Property;
import com.hotel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Panel for managing bookings
 */
public class BookingPanel extends JPanel {
    
    private BookingDAO bookingDAO;
    private PropertyDAO propertyDAO;
    private UserDAO userDAO;
    
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterComboBox;
    
    /**
     * Constructor for the BookingPanel
     */
    public BookingPanel() {
        bookingDAO = new BookingDAO();
        propertyDAO = new PropertyDAO();
        userDAO = new UserDAO();
        
        initComponents();
        loadBookings();
    }
    
    /**
     * Initialize the components of the panel
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        
        JLabel titleLabel = new JLabel("Bookings Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(new Color(41, 128, 185));
        
        statusFilterComboBox = new JComboBox<>(new String[]{"All Bookings", "Confirmed", "Pending", "Cancelled"});
        statusFilterComboBox.addActionListener(e -> filterBookings());
        
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilterComboBox);
        
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "Property", "Renter", "Check-in", "Check-out", "Total Price", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("Add Booking");
        JButton editButton = new JButton("Edit Booking");
        JButton cancelBookingButton = new JButton("Cancel Booking");
        JButton viewButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addBooking());
        editButton.addActionListener(e -> editBooking());
        cancelBookingButton.addActionListener(e -> cancelBooking());
        viewButton.addActionListener(e -> viewBookingDetails());
        refreshButton.addActionListener(e -> loadBookings());
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(cancelBookingButton);
        buttonsPanel.add(viewButton);
        buttonsPanel.add(refreshButton);
        
        // Add components to the panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load bookings from the database and display them in the table
     */
    private void loadBookings() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Get all bookings
        List<Booking> bookings = bookingDAO.getAllBookings();
        
        // Add bookings to the table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Booking booking : bookings) {
            Property property = propertyDAO.getPropertyById(booking.getPropertyId());
            User renter = userDAO.getUserById(booking.getRenterId());
            
            String propertyName = (property != null) ? property.getLocation() : "Unknown";
            String renterName = (renter != null) ? renter.getName() : "Unknown";
            
            Object[] rowData = {
                booking.getBookingId(),
                propertyName,
                renterName,
                dateFormat.format(booking.getCheckInDate()),
                dateFormat.format(booking.getCheckOutDate()),
                booking.getTotalPrice(),
                booking.getBookingStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Filter bookings based on the selected status
     */
    private void filterBookings() {
        String filter = (String) statusFilterComboBox.getSelectedItem();
        
        // Clear the table
        tableModel.setRowCount(0);
        
        List<Booking> bookings;
        
        if ("Confirmed".equals(filter)) {
            bookings = bookingDAO.getBookingsByStatus(Booking.BookingStatus.CONFIRMED);
        } else if ("Pending".equals(filter)) {
            bookings = bookingDAO.getBookingsByStatus(Booking.BookingStatus.PENDING);
        } else if ("Cancelled".equals(filter)) {
            bookings = bookingDAO.getBookingsByStatus(Booking.BookingStatus.CANCELLED);
        } else {
            bookings = bookingDAO.getAllBookings();
        }
        
        // Add bookings to the table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Booking booking : bookings) {
            Property property = propertyDAO.getPropertyById(booking.getPropertyId());
            User renter = userDAO.getUserById(booking.getRenterId());
            
            String propertyName = (property != null) ? property.getLocation() : "Unknown";
            String renterName = (renter != null) ? renter.getName() : "Unknown";
            
            Object[] rowData = {
                booking.getBookingId(),
                propertyName,
                renterName,
                dateFormat.format(booking.getCheckInDate()),
                dateFormat.format(booking.getCheckOutDate()),
                booking.getTotalPrice(),
                booking.getBookingStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Add a new booking
     */
    private void addBooking() {
        // Create a dialog for adding a booking
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Booking", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all properties for the property selection
        List<Property> properties = propertyDAO.getAvailableProperties();
        String[] propertyNames = new String[properties.size()];
        int[] propertyIds = new int[properties.size()];
        BigDecimal[] propertyPrices = new BigDecimal[properties.size()];
        
        for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);
            propertyNames[i] = property.getLocation() + " (" + property.getPropertyType() + ")";
            propertyIds[i] = property.getPropertyId();
            propertyPrices[i] = property.getPricePerNight();
        }
        
        // Get all renters for the renter selection
        List<User> renters = userDAO.getAllRenters();
        String[] renterNames = new String[renters.size()];
        int[] renterIds = new int[renters.size()];
        
        for (int i = 0; i < renters.size(); i++) {
            User renter = renters.get(i);
            renterNames[i] = renter.getName();
            renterIds[i] = renter.getUserId();
        }
        
        JComboBox<String> propertyComboBox = new JComboBox<>(propertyNames);
        JComboBox<String> renterComboBox = new JComboBox<>(renterNames);
        JTextField checkInField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField checkOutField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField totalPriceField = new JTextField();
        totalPriceField.setEditable(false);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"confirmed", "pending"});
        
        // Add listeners to update total price when dates or property changes
        propertyComboBox.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        checkInField.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        checkOutField.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        
        formPanel.add(new JLabel("Property:"));
        formPanel.add(propertyComboBox);
        formPanel.add(new JLabel("Renter:"));
        formPanel.add(renterComboBox);
        formPanel.add(new JLabel("Check-in Date (yyyy-MM-dd):"));
        formPanel.add(checkInField);
        formPanel.add(new JLabel("Check-out Date (yyyy-MM-dd):"));
        formPanel.add(checkOutField);
        formPanel.add(new JLabel("Total Price:"));
        formPanel.add(totalPriceField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int propertyIndex = propertyComboBox.getSelectedIndex();
                int renterIndex = renterComboBox.getSelectedIndex();
                
                if (propertyIndex == -1 || propertyIndex >= propertyIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a property!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (renterIndex == -1 || renterIndex >= renterIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a renter!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int propertyId = propertyIds[propertyIndex];
                int renterId = renterIds[renterIndex];
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date checkInDate = dateFormat.parse(checkInField.getText());
                Date checkOutDate = dateFormat.parse(checkOutField.getText());
                
                if (checkInDate.after(checkOutDate)) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date must be before check-out date!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if property is available for the selected dates
                if (!bookingDAO.isPropertyAvailable(propertyId, new java.sql.Date(checkInDate.getTime()), new java.sql.Date(checkOutDate.getTime()))) {
                    JOptionPane.showMessageDialog(dialog, "Property is not available for the selected dates!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String status = (String) statusComboBox.getSelectedItem();
                
                // Calculate total price
                BigDecimal pricePerNight = propertyPrices[propertyIndex];
                long diffInMillies = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(diffInDays));
                
                Booking booking = new Booking();
                booking.setPropertyId(propertyId);
                booking.setRenterId(renterId);
                booking.setCheckInDate(checkInDate);
                booking.setCheckOutDate(checkOutDate);
                booking.setTotalPrice(totalPrice);
                booking.setBookingStatus(Booking.BookingStatus.fromString(status));
                
                boolean success = bookingDAO.addBooking(booking);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Booking added successfully!");
                    dialog.dispose();
                    loadBookings();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add booking!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format! Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * Calculate the total price based on the selected property and dates
     */
    private void calculateTotalPrice(JComboBox<String> propertyComboBox, BigDecimal[] propertyPrices, 
                                    JTextField checkInField, JTextField checkOutField, JTextField totalPriceField) {
        try {
            int propertyIndex = propertyComboBox.getSelectedIndex();
            if (propertyIndex == -1 || propertyIndex >= propertyPrices.length) {
                return;
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date checkInDate = dateFormat.parse(checkInField.getText());
            Date checkOutDate = dateFormat.parse(checkOutField.getText());
            
            if (checkInDate.after(checkOutDate)) {
                totalPriceField.setText("Invalid dates");
                return;
            }
            
            BigDecimal pricePerNight = propertyPrices[propertyIndex];
            long diffInMillies = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(diffInDays));
            
            totalPriceField.setText(totalPrice.toString());
        } catch (ParseException e) {
            totalPriceField.setText("Invalid dates");
        }
    }
    
    /**
     * Edit the selected booking
     */
    private void editBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (int) bookingTable.getValueAt(selectedRow, 0);
        Booking booking = bookingDAO.getBookingById(bookingId);
        
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a dialog for editing the booking
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Booking", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all properties for the property selection
        List<Property> properties = propertyDAO.getAllProperties();
        String[] propertyNames = new String[properties.size()];
        int[] propertyIds = new int[properties.size()];
        BigDecimal[] propertyPrices = new BigDecimal[properties.size()];
        int selectedPropertyIndex = 0;
        
        for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);
            propertyNames[i] = property.getLocation() + " (" + property.getPropertyType() + ")";
            propertyIds[i] = property.getPropertyId();
            propertyPrices[i] = property.getPricePerNight();
            
            if (property.getPropertyId() == booking.getPropertyId()) {
                selectedPropertyIndex = i;
            }
        }
        
        // Get all renters for the renter selection
        List<User> renters = userDAO.getAllRenters();
        String[] renterNames = new String[renters.size()];
        int[] renterIds = new int[renters.size()];
        int selectedRenterIndex = 0;
        
        for (int i = 0; i < renters.size(); i++) {
            User renter = renters.get(i);
            renterNames[i] = renter.getName();
            renterIds[i] = renter.getUserId();
            
            if (renter.getUserId() == booking.getRenterId()) {
                selectedRenterIndex = i;
            }
        }
        
        JComboBox<String> propertyComboBox = new JComboBox<>(propertyNames);
        propertyComboBox.setSelectedIndex(selectedPropertyIndex);
        
        JComboBox<String> renterComboBox = new JComboBox<>(renterNames);
        renterComboBox.setSelectedIndex(selectedRenterIndex);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JTextField checkInField = new JTextField(dateFormat.format(booking.getCheckInDate()));
        JTextField checkOutField = new JTextField(dateFormat.format(booking.getCheckOutDate()));
        JTextField totalPriceField = new JTextField(booking.getTotalPrice().toString());
        totalPriceField.setEditable(false);
        
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"confirmed", "pending", "cancelled"});
        statusComboBox.setSelectedItem(booking.getBookingStatus().getValue());
        
        // Add listeners to update total price when dates or property changes
        propertyComboBox.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        checkInField.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        checkOutField.addActionListener(e -> calculateTotalPrice(propertyComboBox, propertyPrices, checkInField, checkOutField, totalPriceField));
        
        formPanel.add(new JLabel("Property:"));
        formPanel.add(propertyComboBox);
        formPanel.add(new JLabel("Renter:"));
        formPanel.add(renterComboBox);
        formPanel.add(new JLabel("Check-in Date (yyyy-MM-dd):"));
        formPanel.add(checkInField);
        formPanel.add(new JLabel("Check-out Date (yyyy-MM-dd):"));
        formPanel.add(checkOutField);
        formPanel.add(new JLabel("Total Price:"));
        formPanel.add(totalPriceField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int propertyIndex = propertyComboBox.getSelectedIndex();
                int renterIndex = renterComboBox.getSelectedIndex();
                
                if (propertyIndex == -1 || propertyIndex >= propertyIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a property!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (renterIndex == -1 || renterIndex >= renterIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a renter!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int propertyId = propertyIds[propertyIndex];
                int renterId = renterIds[renterIndex];
                
                Date checkInDate = dateFormat.parse(checkInField.getText());
                Date checkOutDate = dateFormat.parse(checkOutField.getText());
                
                if (checkInDate.after(checkOutDate)) {
                    JOptionPane.showMessageDialog(dialog, "Check-in date must be before check-out date!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String status = (String) statusComboBox.getSelectedItem();
                
                // Calculate total price
                BigDecimal pricePerNight = propertyPrices[propertyIndex];
                long diffInMillies = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(diffInDays));
                
                booking.setPropertyId(propertyId);
                booking.setRenterId(renterId);
                booking.setCheckInDate(checkInDate);
                booking.setCheckOutDate(checkOutDate);
                booking.setTotalPrice(totalPrice);
                booking.setBookingStatus(Booking.BookingStatus.fromString(status));
                
                boolean success = bookingDAO.updateBooking(booking);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Booking updated successfully!");
                    dialog.dispose();
                    loadBookings();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update booking!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format! Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * Cancel the selected booking
     */
    private void cancelBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (int) bookingTable.getValueAt(selectedRow, 0);
        Booking booking = bookingDAO.getBookingById(bookingId);
        
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            
            boolean success = bookingDAO.updateBooking(booking);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * View details of the selected booking
     */
    private void viewBookingDetails() {
        int selectedRow = bookingTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (int) bookingTable.getValueAt(selectedRow, 0);
        Booking booking = bookingDAO.getBookingById(bookingId);
        
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Property property = propertyDAO.getPropertyById(booking.getPropertyId());
        User renter = userDAO.getUserById(booking.getRenterId());
        
        String propertyName = (property != null) ? property.getLocation() + " (" + property.getPropertyType() + ")" : "Unknown";
        String renterName = (renter != null) ? renter.getName() : "Unknown";
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        StringBuilder details = new StringBuilder();
        details.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        details.append("Property: ").append(propertyName).append("\n");
        details.append("Renter: ").append(renterName).append("\n");
        details.append("Check-in Date: ").append(dateFormat.format(booking.getCheckInDate())).append("\n");
        details.append("Check-out Date: ").append(dateFormat.format(booking.getCheckOutDate())).append("\n");
        details.append("Total Price: $").append(booking.getTotalPrice()).append("\n");
        details.append("Status: ").append(booking.getBookingStatus());
        
        JOptionPane.showMessageDialog(this, details.toString(), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }
} 