package com.hotel.view.panels;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PropertyDAO;
import com.hotel.dao.UserDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.model.Booking;
import com.hotel.model.Property;
import com.hotel.model.User;
import com.hotel.model.Review;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Panel for generating and viewing reports
 */
public class ReportPanel extends JPanel {
    
    private BookingDAO bookingDAO;
    private PropertyDAO propertyDAO;
    private UserDAO userDAO;
    private PaymentDAO paymentDAO;
    private ReviewDAO reviewDAO;
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeComboBox;
    
    /**
     * Constructor for the ReportPanel
     */
    public ReportPanel() {
        bookingDAO = new BookingDAO();
        propertyDAO = new PropertyDAO();
        userDAO = new UserDAO();
        paymentDAO = new PaymentDAO();
        reviewDAO = new ReviewDAO();
        
        initComponents();
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
        
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create report selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        reportTypeComboBox = new JComboBox<>(new String[]{
                "Booking Summary",
                "Revenue by Property",
                "Top Rated Properties",
                "Occupancy Rate",
                "User Activity"
        });
        
        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        
        selectionPanel.add(new JLabel("Report Type:"));
        selectionPanel.add(reportTypeComboBox);
        selectionPanel.add(generateButton);
        
        // Create table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        // Create export panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> exportReport());
        exportPanel.add(exportButton);
        
        // Add components to the panel
        add(headerPanel, BorderLayout.NORTH);
        add(selectionPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(exportPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Generate a report based on the selected type
     */
    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        if (reportType == null) {
            return;
        }
        
        switch (reportType) {
            case "Booking Summary":
                generateBookingSummaryReport();
                break;
            case "Revenue by Property":
                generateRevenueByPropertyReport();
                break;
            case "Top Rated Properties":
                generateTopRatedPropertiesReport();
                break;
            case "Occupancy Rate":
                generateOccupancyRateReport();
                break;
            case "User Activity":
                generateUserActivityReport();
                break;
        }
    }
    
    /**
     * Generate a booking summary report
     */
    private void generateBookingSummaryReport() {
        // Set up table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        tableModel.addColumn("Booking ID");
        tableModel.addColumn("Property");
        tableModel.addColumn("Renter");
        tableModel.addColumn("Check-in");
        tableModel.addColumn("Check-out");
        tableModel.addColumn("Nights");
        tableModel.addColumn("Total Price");
        tableModel.addColumn("Status");
        
        // Get all bookings
        List<Booking> bookings = bookingDAO.getAllBookings();
        
        // Add bookings to the table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Booking booking : bookings) {
            Property property = propertyDAO.getPropertyById(booking.getPropertyId());
            User renter = userDAO.getUserById(booking.getRenterId());
            
            String propertyName = (property != null) ? property.getLocation() : "Unknown";
            String renterName = (renter != null) ? renter.getName() : "Unknown";
            
            // Calculate number of nights
            long diffInMillies = Math.abs(booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime());
            long nights = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            Object[] rowData = {
                booking.getBookingId(),
                propertyName,
                renterName,
                dateFormat.format(booking.getCheckInDate()),
                dateFormat.format(booking.getCheckOutDate()),
                nights,
                booking.getTotalPrice(),
                booking.getBookingStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Generate a revenue by property report
     */
    private void generateRevenueByPropertyReport() {
        // Set up table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        tableModel.addColumn("Property ID");
        tableModel.addColumn("Property");
        tableModel.addColumn("Host");
        tableModel.addColumn("Type");
        tableModel.addColumn("Total Bookings");
        tableModel.addColumn("Total Revenue");
        
        // Get all properties
        List<Property> properties = propertyDAO.getAllProperties();
        
        // Calculate revenue for each property
        for (Property property : properties) {
            List<Booking> bookings = bookingDAO.getBookingsByPropertyId(property.getPropertyId());
            
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Booking booking : bookings) {
                if (booking.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                    totalRevenue = totalRevenue.add(booking.getTotalPrice());
                }
            }
            
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                property.getLocation(),
                hostName,
                property.getPropertyType(),
                bookings.size(),
                totalRevenue
            };
            
            tableModel.addRow(rowData);
        }
        
        // Sort by total revenue (descending)
        sortTableByColumn(5, false);
    }
    
    /**
     * Generate a top rated properties report
     */
    private void generateTopRatedPropertiesReport() {
        // Set up table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        tableModel.addColumn("Property ID");
        tableModel.addColumn("Property");
        tableModel.addColumn("Host");
        tableModel.addColumn("Type");
        tableModel.addColumn("Average Rating");
        tableModel.addColumn("Number of Reviews");
        
        // Get all properties
        List<Property> properties = propertyDAO.getAllProperties();
        
        // Calculate average rating for each property
        for (Property property : properties) {
            double avgRating = reviewDAO.getAverageRatingForProperty(property.getPropertyId());
            List<Review> reviews = reviewDAO.getReviewsByPropertyId(property.getPropertyId());
            
            if (reviews.isEmpty()) {
                continue; // Skip properties with no reviews
            }
            
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                property.getLocation(),
                hostName,
                property.getPropertyType(),
                String.format("%.1f", avgRating),
                reviews.size()
            };
            
            tableModel.addRow(rowData);
        }
        
        // Sort by average rating (descending)
        sortTableByColumn(4, false);
    }
    
    /**
     * Generate an occupancy rate report
     */
    private void generateOccupancyRateReport() {
        // Set up table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        tableModel.addColumn("Property ID");
        tableModel.addColumn("Property");
        tableModel.addColumn("Host");
        tableModel.addColumn("Type");
        tableModel.addColumn("Days Booked");
        tableModel.addColumn("Occupancy Rate (%)");
        tableModel.addColumn("Revenue per Available Day");
        
        // Get all properties
        List<Property> properties = propertyDAO.getAllProperties();
        
        // Calculate occupancy rate for each property
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int daysInYear = 365;
        // Check if it's a leap year (divisible by 4, but not by 100 unless also divisible by 400)
        if ((currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0)) {
            daysInYear = 366;
        }
        
        for (Property property : properties) {
            List<Booking> bookings = bookingDAO.getBookingsByPropertyId(property.getPropertyId());
            
            int totalDaysBooked = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Booking booking : bookings) {
                if (booking.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
                    // Calculate days booked
                    long diffInMillies = Math.abs(booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime());
                    int days = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    totalDaysBooked += days;
                    
                    // Add to total revenue
                    totalRevenue = totalRevenue.add(booking.getTotalPrice());
                }
            }
            
            // Calculate occupancy rate
            double occupancyRate = (double) totalDaysBooked / daysInYear * 100;
            
            // Calculate revenue per available day
            BigDecimal revenuePerDay = BigDecimal.ZERO;
            if (daysInYear > 0) {
                revenuePerDay = totalRevenue.divide(BigDecimal.valueOf(daysInYear), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                property.getLocation(),
                hostName,
                property.getPropertyType(),
                totalDaysBooked,
                String.format("%.2f", occupancyRate),
                revenuePerDay
            };
            
            tableModel.addRow(rowData);
        }
        
        // Sort by occupancy rate (descending)
        sortTableByColumn(5, false);
    }
    
    /**
     * Generate a user activity report
     */
    private void generateUserActivityReport() {
        // Set up table model
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        tableModel.addColumn("User ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Email");
        tableModel.addColumn("Type");
        tableModel.addColumn("Registration Date");
        tableModel.addColumn("Properties");
        tableModel.addColumn("Bookings");
        tableModel.addColumn("Reviews");
        
        // Get all users
        List<User> users = userDAO.getAllUsers();
        
        // Calculate activity for each user
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (User user : users) {
            int propertyCount = 0;
            int bookingCount = 0;
            int reviewCount = 0;
            
            // Count properties (for hosts)
            if (user.getUserType() == User.UserType.HOST) {
                List<Property> properties = propertyDAO.getPropertiesByHostId(user.getUserId());
                propertyCount = properties.size();
            } else if (user.getUserType() == User.UserType.RENTER) {
                List<Booking> bookings = bookingDAO.getBookingsByRenterId(user.getUserId());
                bookingCount = bookings.size();
            }
            
            // Count reviews
            List<Review> reviews = reviewDAO.getReviewsByUserId(user.getUserId());
            reviewCount = reviews.size();
            
            Object[] rowData = {
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                dateFormat.format(user.getDateOfRegistration()),
                propertyCount,
                bookingCount,
                reviewCount
            };
            
            tableModel.addRow(rowData);
        }
        
        // Sort by user type
        sortTableByColumn(3, true);
    }
    
    /**
     * Sort the table by a specific column
     * @param column The column index to sort by
     * @param ascending Whether to sort in ascending order
     */
    private void sortTableByColumn(int column, boolean ascending) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        reportTable.setRowSorter(sorter);
        
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(column, ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING));
        
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }
    
    /**
     * Export the current report to a CSV file
     */
    private void exportReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("report.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write header
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
                
                // Write data
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        writer.write(value != null ? value.toString() : "");
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.newLine();
                }
                
                JOptionPane.showMessageDialog(this, "Report exported successfully to " + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 