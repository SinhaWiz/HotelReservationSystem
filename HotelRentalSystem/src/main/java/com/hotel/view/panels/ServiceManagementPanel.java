package com.hotel.view.panels;

import com.hotel.model.*;
import com.hotel.service.EnhancedHotelManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing room services and service usage
 */
public class ServiceManagementPanel extends JPanel {
    
    private EnhancedHotelManagementService hotelService;
    private JTable servicesTable;
    private JTable usageTable;
    private DefaultTableModel servicesTableModel;
    private DefaultTableModel usageTableModel;
    private JTextField serviceNameField;
    private JTextArea serviceDescriptionArea;
    private JComboBox<RoomService.ServiceCategory> categoryComboBox;
    private JTextField basePriceField;
    private JTextField searchField;
    private JComboBox<String> customerComboBox;
    private JComboBox<String> bookingComboBox;
    private JComboBox<String> serviceComboBox;
    private JSpinner quantitySpinner;
    
    public ServiceManagementPanel() {
        this.hotelService = new EnhancedHotelManagementService();
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        loadServicesData();
    }
    
    private void initializeComponents() {
        // Services table
        String[] serviceColumns = {"ID", "Service Name", "Category", "Base Price", "Active", "Created Date"};
        servicesTableModel = new DefaultTableModel(serviceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        servicesTable = new JTable(servicesTableModel);
        servicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Usage table
        String[] usageColumns = {"Usage ID", "Customer", "Service", "Quantity", "Unit Price", "Total Cost", "Date", "Complimentary"};
        usageTableModel = new DefaultTableModel(usageColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usageTable = new JTable(usageTableModel);
        usageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Service form fields
        serviceNameField = new JTextField(20);
        serviceDescriptionArea = new JTextArea(3, 20);
        serviceDescriptionArea.setLineWrap(true);
        serviceDescriptionArea.setWrapStyleWord(true);
        categoryComboBox = new JComboBox<>(RoomService.ServiceCategory.values());
        basePriceField = new JTextField(10);
        
        // Search and filter fields
        searchField = new JTextField(15);
        customerComboBox = new JComboBox<>();
        bookingComboBox = new JComboBox<>();
        serviceComboBox = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Services Management Tab
        JPanel servicesPanel = createServicesManagementPanel();
        tabbedPane.addTab("Room Services", servicesPanel);
        
        // Service Usage Tab
        JPanel usagePanel = createServiceUsagePanel();
        tabbedPane.addTab("Service Usage", usagePanel);
        
        // Service Reports Tab
        JPanel reportsPanel = createServiceReportsPanel();
        tabbedPane.addTab("Service Reports", reportsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createServicesManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with search and add service
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchServices());
        topPanel.add(searchButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadServicesData());
        topPanel.add(refreshButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with services table
        JScrollPane servicesScrollPane = new JScrollPane(servicesTable);
        servicesScrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(servicesScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with service form
        JPanel formPanel = createServiceFormPanel();
        panel.add(formPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createServiceFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Service Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Service Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(serviceNameField, gbc);
        
        // Category
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(categoryComboBox, gbc);
        
        // Base Price
        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Base Price:"), gbc);
        gbc.gridx = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(basePriceField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(serviceDescriptionArea), gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add Service");
        addButton.addActionListener(e -> addService());
        buttonPanel.add(addButton);
        
        JButton updateButton = new JButton("Update Service");
        updateButton.addActionListener(e -> updateService());
        buttonPanel.add(updateButton);
        
        JButton deleteButton = new JButton("Delete Service");
        deleteButton.addActionListener(e -> deleteService());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearServiceForm());
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createServiceUsagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with usage controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Customer:"));
        topPanel.add(customerComboBox);
        topPanel.add(new JLabel("Booking:"));
        topPanel.add(bookingComboBox);
        topPanel.add(new JLabel("Service:"));
        topPanel.add(serviceComboBox);
        topPanel.add(new JLabel("Quantity:"));
        topPanel.add(quantitySpinner);
        
        JButton addUsageButton = new JButton("Add Service Usage");
        addUsageButton.addActionListener(e -> addServiceUsage());
        topPanel.add(addUsageButton);
        
        JButton refreshUsageButton = new JButton("Refresh");
        refreshUsageButton.addActionListener(e -> loadUsageData());
        topPanel.add(refreshUsageButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with usage table
        JScrollPane usageScrollPane = new JScrollPane(usageTable);
        usageScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(usageScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with usage summary
        JPanel summaryPanel = createUsageSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUsageSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Usage Summary"));
        
        JButton customerSummaryButton = new JButton("Customer Service Summary");
        customerSummaryButton.addActionListener(e -> showCustomerServiceSummary());
        panel.add(customerSummaryButton);
        
        JButton popularServicesButton = new JButton("Popular Services");
        popularServicesButton.addActionListener(e -> showPopularServices());
        panel.add(popularServicesButton);
        
        return panel;
    }
    
    private JPanel createServiceReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea reportArea = new JTextArea(20, 60);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateReportButton = new JButton("Generate Service Report");
        generateReportButton.addActionListener(e -> {
            try {
                String report = generateServiceReport();
                reportArea.setText(report);
            } catch (SQLException ex) {
                showError("Error generating report: " + ex.getMessage());
            }
        });
        buttonPanel.add(generateReportButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void attachEventListeners() {
        // Service table selection listener
        servicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedServiceToForm();
            }
        });
        
        // Customer selection listener
        customerComboBox.addActionListener(e -> loadCustomerBookings());
    }
    
    private void loadServicesData() {
        try {
            List<RoomService> services = hotelService.getAllRoomServices();
            servicesTableModel.setRowCount(0);
            
            for (RoomService service : services) {
                Object[] row = {
                    service.getServiceId(),
                    service.getServiceName(),
                    service.getServiceCategory(),
                    service.getFormattedPrice(),
                    service.isActive() ? "Yes" : "No",
                    service.getCreatedDate()
                };
                servicesTableModel.addRow(row);
            }
            
            // Load service combo box
            serviceComboBox.removeAllItems();
            for (RoomService service : hotelService.getActiveRoomServices()) {
                serviceComboBox.addItem(service.getServiceId() + " - " + service.getServiceName());
            }
            
        } catch (SQLException e) {
            showError("Error loading services: " + e.getMessage());
        }
    }
    
    private void loadUsageData() {
        try {
            // Load all service usage (you might want to filter this)
            usageTableModel.setRowCount(0);
            
            // Load customers for combo box
            customerComboBox.removeAllItems();
            List<Customer> customers = hotelService.getAllCustomers();
            for (Customer customer : customers) {
                customerComboBox.addItem(customer.getCustomerId() + " - " + customer.getFullName());
            }
            
        } catch (SQLException e) {
            showError("Error loading usage data: " + e.getMessage());
        }
    }
    
    private void loadCustomerBookings() {
        try {
            String selectedCustomer = (String) customerComboBox.getSelectedItem();
            if (selectedCustomer != null) {
                int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
                
                bookingComboBox.removeAllItems();
                List<Booking> bookings = hotelService.getCustomerBookings(customerId);
                for (Booking booking : bookings) {
                    if ("CONFIRMED".equals(booking.getBookingStatus()) || 
                        "CHECKED_IN".equals(booking.getBookingStatus())) {
                        bookingComboBox.addItem(booking.getBookingId() + " - Room " + booking.getRoomId());
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error loading customer bookings: " + e.getMessage());
        }
    }
    
    private void searchServices() {
        try {
            String searchTerm = searchField.getText().trim();
            List<RoomService> services;
            
            if (searchTerm.isEmpty()) {
                services = hotelService.getAllRoomServices();
            } else {
                services = hotelService.searchRoomServices(searchTerm);
            }
            
            servicesTableModel.setRowCount(0);
            for (RoomService service : services) {
                Object[] row = {
                    service.getServiceId(),
                    service.getServiceName(),
                    service.getServiceCategory(),
                    service.getFormattedPrice(),
                    service.isActive() ? "Yes" : "No",
                    service.getCreatedDate()
                };
                servicesTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error searching services: " + e.getMessage());
        }
    }
    
    private void addService() {
        try {
            if (validateServiceForm()) {
                RoomService service = new RoomService();
                service.setServiceName(serviceNameField.getText().trim());
                service.setServiceDescription(serviceDescriptionArea.getText().trim());
                service.setServiceCategory((RoomService.ServiceCategory) categoryComboBox.getSelectedItem());
                service.setBasePrice(Double.parseDouble(basePriceField.getText().trim()));
                
                hotelService.createRoomService(service);
                showSuccess("Service added successfully!");
                clearServiceForm();
                loadServicesData();
            }
        } catch (SQLException e) {
            showError("Error adding service: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter a valid price");
        }
    }
    
    private void updateService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a service to update");
            return;
        }
        
        try {
            if (validateServiceForm()) {
                int serviceId = (Integer) servicesTableModel.getValueAt(selectedRow, 0);
                RoomService service = hotelService.getRoomService(serviceId);
                
                if (service != null) {
                    service.setServiceName(serviceNameField.getText().trim());
                    service.setServiceDescription(serviceDescriptionArea.getText().trim());
                    service.setServiceCategory((RoomService.ServiceCategory) categoryComboBox.getSelectedItem());
                    service.setBasePrice(Double.parseDouble(basePriceField.getText().trim()));
                    
                    hotelService.updateRoomService(service);
                    showSuccess("Service updated successfully!");
                    clearServiceForm();
                    loadServicesData();
                }
            }
        } catch (SQLException e) {
            showError("Error updating service: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter a valid price");
        }
    }
    
    private void deleteService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a service to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this service?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int serviceId = (Integer) servicesTableModel.getValueAt(selectedRow, 0);
                RoomService service = hotelService.getRoomService(serviceId);
                
                if (service != null) {
                    service.setActive(false);
                    hotelService.updateRoomService(service);
                    showSuccess("Service deleted successfully!");
                    clearServiceForm();
                    loadServicesData();
                }
            } catch (SQLException e) {
                showError("Error deleting service: " + e.getMessage());
            }
        }
    }
    
    private void addServiceUsage() {
        try {
            String selectedCustomer = (String) customerComboBox.getSelectedItem();
            String selectedBooking = (String) bookingComboBox.getSelectedItem();
            String selectedService = (String) serviceComboBox.getSelectedItem();
            
            if (selectedCustomer == null || selectedBooking == null || selectedService == null) {
                showError("Please select customer, booking, and service");
                return;
            }
            
            int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            long bookingId = Long.parseLong(selectedBooking.split(" - ")[0]);
            int serviceId = Integer.parseInt(selectedService.split(" - ")[0]);
            int quantity = (Integer) quantitySpinner.getValue();
            
            long usageId = hotelService.addServiceUsage(bookingId, customerId, serviceId, quantity);
            showSuccess("Service usage added successfully! Usage ID: " + usageId);
            
            // Refresh usage data for the selected customer
            loadCustomerServiceUsage(customerId);
            
        } catch (SQLException e) {
            showError("Error adding service usage: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid selection format");
        }
    }
    
    private void loadCustomerServiceUsage(int customerId) {
        try {
            List<ServiceUsage> usageList = hotelService.getCustomerServiceUsage(customerId);
            usageTableModel.setRowCount(0);
            
            for (ServiceUsage usage : usageList) {
                Object[] row = {
                    usage.getUsageId(),
                    usage.getCustomerName(),
                    usage.getServiceName(),
                    usage.getQuantity(),
                    usage.getFormattedUnitPrice(),
                    usage.getFormattedTotalCost(),
                    usage.getUsageDate(),
                    usage.getComplimentaryStatus()
                };
                usageTableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error loading customer service usage: " + e.getMessage());
        }
    }
    
    private void showCustomerServiceSummary() {
        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        if (selectedCustomer == null) {
            showError("Please select a customer");
            return;
        }
        
        try {
            int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            List<ServiceUsage> summary = hotelService.getCustomerServiceSummary(customerId);
            double totalCost = hotelService.calculateCustomerServiceTotal(customerId, null);
            
            StringBuilder report = new StringBuilder();
            report.append("Customer Service Summary\n");
            report.append("========================\n\n");
            report.append("Customer: ").append(selectedCustomer).append("\n");
            report.append("Total Service Cost: $").append(String.format("%.2f", totalCost)).append("\n\n");
            
            for (ServiceUsage usage : summary) {
                report.append(usage.getNotes()).append("\n");
            }
            
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Customer Service Summary", 
                                        JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            showError("Error generating customer summary: " + e.getMessage());
        }
    }
    
    private void showPopularServices() {
        try {
            List<ServiceUsage> popularServices = hotelService.getMostPopularServices(10);
            
            StringBuilder report = new StringBuilder();
            report.append("Most Popular Services\n");
            report.append("====================\n\n");
            
            int rank = 1;
            for (ServiceUsage service : popularServices) {
                report.append(rank++).append(". ").append(service.getNotes()).append("\n");
                report.append("   Total Revenue: ").append(service.getFormattedTotalCost()).append("\n\n");
            }
            
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Popular Services Report", 
                                        JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            showError("Error generating popular services report: " + e.getMessage());
        }
    }
    
    private String generateServiceReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("SERVICE MANAGEMENT REPORT\n");
        report.append("========================\n");
        report.append("Generated on: ").append(new java.util.Date()).append("\n\n");
        
        // Service statistics
        List<RoomService> allServices = hotelService.getAllRoomServices();
        List<RoomService> activeServices = hotelService.getActiveRoomServices();
        
        report.append("SERVICE STATISTICS:\n");
        report.append("- Total Services: ").append(allServices.size()).append("\n");
        report.append("- Active Services: ").append(activeServices.size()).append("\n");
        report.append("- Inactive Services: ").append(allServices.size() - activeServices.size()).append("\n\n");
        
        // Service categories
        report.append("SERVICES BY CATEGORY:\n");
        for (RoomService.ServiceCategory category : RoomService.ServiceCategory.values()) {
            List<RoomService> categoryServices = hotelService.getServicesByCategory(category);
            report.append("- ").append(category.name()).append(": ").append(categoryServices.size()).append("\n");
        }
        
        report.append("\n");
        
        // Popular services
        report.append("TOP 5 POPULAR SERVICES:\n");
        List<ServiceUsage> popularServices = hotelService.getMostPopularServices(5);
        int rank = 1;
        for (ServiceUsage service : popularServices) {
            report.append(rank++).append(". ").append(service.getNotes()).append("\n");
        }
        
        return report.toString();
    }
    
    private void loadSelectedServiceToForm() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                int serviceId = (Integer) servicesTableModel.getValueAt(selectedRow, 0);
                RoomService service = hotelService.getRoomService(serviceId);
                
                if (service != null) {
                    serviceNameField.setText(service.getServiceName());
                    serviceDescriptionArea.setText(service.getServiceDescription());
                    categoryComboBox.setSelectedItem(service.getServiceCategory());
                    basePriceField.setText(String.valueOf(service.getBasePrice()));
                }
            } catch (SQLException e) {
                showError("Error loading service details: " + e.getMessage());
            }
        }
    }
    
    private boolean validateServiceForm() {
        if (serviceNameField.getText().trim().isEmpty()) {
            showError("Please enter service name");
            return false;
        }
        
        try {
            double price = Double.parseDouble(basePriceField.getText().trim());
            if (price < 0) {
                showError("Price cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid price");
            return false;
        }
        
        return true;
    }
    
    private void clearServiceForm() {
        serviceNameField.setText("");
        serviceDescriptionArea.setText("");
        categoryComboBox.setSelectedIndex(0);
        basePriceField.setText("");
        servicesTable.clearSelection();
    }
    
    public void refreshData() {
        loadServicesData();
        loadUsageData();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

