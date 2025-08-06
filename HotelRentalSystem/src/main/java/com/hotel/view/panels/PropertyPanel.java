package com.hotel.view.panels;

import com.hotel.dao.PropertyDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.Property;
import com.hotel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel for managing properties
 */
public class PropertyPanel extends JPanel {
    
    private PropertyDAO propertyDAO;
    private UserDAO userDAO;
    
    private JTable propertyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    
    /**
     * Constructor for the PropertyPanel
     */
    public PropertyPanel() {
        propertyDAO = new PropertyDAO();
        userDAO = new UserDAO();
        
        initComponents();
        loadProperties();
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
        
        JLabel titleLabel = new JLabel("Properties Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(41, 128, 185));
        
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchProperties());
        
        filterComboBox = new JComboBox<>(new String[]{"All Properties", "Available Only", "Apartments", "Houses", "Villas"});
        filterComboBox.addActionListener(e -> filterProperties());
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(filterComboBox);
        
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "Host", "Type", "Location", "Price/Night", "Max Guests", "Available"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) { // Available column
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        
        propertyTable = new JTable(tableModel);
        propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propertyTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(propertyTable);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("Add Property");
        JButton editButton = new JButton("Edit Property");
        JButton deleteButton = new JButton("Delete Property");
        JButton viewButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addProperty());
        editButton.addActionListener(e -> editProperty());
        deleteButton.addActionListener(e -> deleteProperty());
        viewButton.addActionListener(e -> viewPropertyDetails());
        refreshButton.addActionListener(e -> loadProperties());
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(viewButton);
        buttonsPanel.add(refreshButton);
        
        // Add components to the panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load properties from the database and display them in the table
     */
    private void loadProperties() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Get all properties
        List<Property> properties = propertyDAO.getAllProperties();
        
        // Add properties to the table
        for (Property property : properties) {
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                hostName,
                property.getPropertyType(),
                property.getLocation(),
                property.getPricePerNight(),
                property.getMaxGuests(),
                property.isAvailabilityStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Search properties based on the search field
     */
    private void searchProperties() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadProperties();
            return;
        }
        
        // Clear the table
        tableModel.setRowCount(0);
        
        // Search properties by location
        List<Property> properties = propertyDAO.searchPropertiesByLocation(searchTerm);
        
        // Add properties to the table
        for (Property property : properties) {
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                hostName,
                property.getPropertyType(),
                property.getLocation(),
                property.getPricePerNight(),
                property.getMaxGuests(),
                property.isAvailabilityStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Filter properties based on the selected filter
     */
    private void filterProperties() {
        String filter = (String) filterComboBox.getSelectedItem();
        
        // Clear the table
        tableModel.setRowCount(0);
        
        List<Property> properties;
        
        if ("Available Only".equals(filter)) {
            properties = propertyDAO.getAvailableProperties();
        } else if ("Apartments".equals(filter)) {
            properties = propertyDAO.getAllProperties();
            properties.removeIf(p -> p.getPropertyType() != Property.PropertyType.APARTMENT);
        } else if ("Houses".equals(filter)) {
            properties = propertyDAO.getAllProperties();
            properties.removeIf(p -> p.getPropertyType() != Property.PropertyType.HOUSE);
        } else if ("Villas".equals(filter)) {
            properties = propertyDAO.getAllProperties();
            properties.removeIf(p -> p.getPropertyType() != Property.PropertyType.VILLA);
        } else {
            properties = propertyDAO.getAllProperties();
        }
        
        // Add properties to the table
        for (Property property : properties) {
            User host = userDAO.getUserById(property.getHostId());
            String hostName = (host != null) ? host.getName() : "Unknown";
            
            Object[] rowData = {
                property.getPropertyId(),
                hostName,
                property.getPropertyType(),
                property.getLocation(),
                property.getPricePerNight(),
                property.getMaxGuests(),
                property.isAvailabilityStatus()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Add a new property
     */
    private void addProperty() {
        // Create a dialog for adding a property
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Property", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all hosts for the host selection
        List<User> hosts = userDAO.getAllHosts();
        String[] hostNames = new String[hosts.size()];
        int[] hostIds = new int[hosts.size()];
        
        for (int i = 0; i < hosts.size(); i++) {
            User host = hosts.get(i);
            hostNames[i] = host.getName();
            hostIds[i] = host.getUserId();
        }
        
        JComboBox<String> hostComboBox = new JComboBox<>(hostNames);
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"apartment", "house", "villa"});
        JTextField locationField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField descriptionField = new JTextField();
        JCheckBox availabilityCheckBox = new JCheckBox();
        JTextField maxGuestsField = new JTextField();
        
        formPanel.add(new JLabel("Host:"));
        formPanel.add(hostComboBox);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Price per Night:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availabilityCheckBox);
        formPanel.add(new JLabel("Max Guests:"));
        formPanel.add(maxGuestsField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int hostIndex = hostComboBox.getSelectedIndex();
                if (hostIndex == -1 || hostIndex >= hostIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a host!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int hostId = hostIds[hostIndex];
                String type = (String) typeComboBox.getSelectedItem();
                String location = locationField.getText();
                String priceText = priceField.getText();
                String description = descriptionField.getText();
                boolean availability = availabilityCheckBox.isSelected();
                String maxGuestsText = maxGuestsField.getText();
                
                if (location.isEmpty() || priceText.isEmpty() || maxGuestsText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Location, Price, and Max Guests are required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                BigDecimal price;
                int maxGuests;
                
                try {
                    price = new BigDecimal(priceText);
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException("Price must be positive");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Price must be a positive number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    maxGuests = Integer.parseInt(maxGuestsText);
                    if (maxGuests <= 0) {
                        throw new NumberFormatException("Max guests must be positive");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Max Guests must be a positive integer!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Property property = new Property();
                property.setHostId(hostId);
                property.setPropertyType(Property.PropertyType.fromString(type));
                property.setLocation(location);
                property.setPricePerNight(price);
                property.setDescription(description);
                property.setAvailabilityStatus(availability);
                property.setMaxGuests(maxGuests);
                
                boolean success = propertyDAO.addProperty(property);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Property added successfully!");
                    dialog.dispose();
                    loadProperties();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add property!", "Error", JOptionPane.ERROR_MESSAGE);
                }
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
     * Edit the selected property
     */
    private void editProperty() {
        int selectedRow = propertyTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a property to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int propertyId = (int) propertyTable.getValueAt(selectedRow, 0);
        Property property = propertyDAO.getPropertyById(propertyId);
        
        if (property == null) {
            JOptionPane.showMessageDialog(this, "Property not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a dialog for editing the property
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Property", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all hosts for the host selection
        List<User> hosts = userDAO.getAllHosts();
        String[] hostNames = new String[hosts.size()];
        int[] hostIds = new int[hosts.size()];
        int selectedHostIndex = 0;
        
        for (int i = 0; i < hosts.size(); i++) {
            User host = hosts.get(i);
            hostNames[i] = host.getName();
            hostIds[i] = host.getUserId();
            
            if (host.getUserId() == property.getHostId()) {
                selectedHostIndex = i;
            }
        }
        
        JComboBox<String> hostComboBox = new JComboBox<>(hostNames);
        hostComboBox.setSelectedIndex(selectedHostIndex);
        
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"apartment", "house", "villa"});
        typeComboBox.setSelectedItem(property.getPropertyType().getValue());
        
        JTextField locationField = new JTextField(property.getLocation());
        JTextField priceField = new JTextField(property.getPricePerNight().toString());
        JTextField descriptionField = new JTextField(property.getDescription());
        JCheckBox availabilityCheckBox = new JCheckBox();
        availabilityCheckBox.setSelected(property.isAvailabilityStatus());
        JTextField maxGuestsField = new JTextField(String.valueOf(property.getMaxGuests()));
        
        formPanel.add(new JLabel("Host:"));
        formPanel.add(hostComboBox);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Price per Night:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availabilityCheckBox);
        formPanel.add(new JLabel("Max Guests:"));
        formPanel.add(maxGuestsField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int hostIndex = hostComboBox.getSelectedIndex();
                if (hostIndex == -1 || hostIndex >= hostIds.length) {
                    JOptionPane.showMessageDialog(dialog, "Please select a host!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int hostId = hostIds[hostIndex];
                String type = (String) typeComboBox.getSelectedItem();
                String location = locationField.getText();
                String priceText = priceField.getText();
                String description = descriptionField.getText();
                boolean availability = availabilityCheckBox.isSelected();
                String maxGuestsText = maxGuestsField.getText();
                
                if (location.isEmpty() || priceText.isEmpty() || maxGuestsText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Location, Price, and Max Guests are required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                BigDecimal price;
                int maxGuests;
                
                try {
                    price = new BigDecimal(priceText);
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException("Price must be positive");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Price must be a positive number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    maxGuests = Integer.parseInt(maxGuestsText);
                    if (maxGuests <= 0) {
                        throw new NumberFormatException("Max guests must be positive");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Max Guests must be a positive integer!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                property.setHostId(hostId);
                property.setPropertyType(Property.PropertyType.fromString(type));
                property.setLocation(location);
                property.setPricePerNight(price);
                property.setDescription(description);
                property.setAvailabilityStatus(availability);
                property.setMaxGuests(maxGuests);
                
                boolean success = propertyDAO.updateProperty(property);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Property updated successfully!");
                    dialog.dispose();
                    loadProperties();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update property!", "Error", JOptionPane.ERROR_MESSAGE);
                }
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
     * Delete the selected property
     */
    private void deleteProperty() {
        int selectedRow = propertyTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a property to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int propertyId = (int) propertyTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this property? This will also delete all associated bookings and reviews.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = propertyDAO.deleteProperty(propertyId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Property deleted successfully!");
                loadProperties();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete property!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * View details of the selected property
     */
    private void viewPropertyDetails() {
        int selectedRow = propertyTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a property to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int propertyId = (int) propertyTable.getValueAt(selectedRow, 0);
        Property property = propertyDAO.getPropertyById(propertyId);
        
        if (property == null) {
            JOptionPane.showMessageDialog(this, "Property not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User host = userDAO.getUserById(property.getHostId());
        String hostName = (host != null) ? host.getName() : "Unknown";
        
        StringBuilder details = new StringBuilder();
        details.append("Property ID: ").append(property.getPropertyId()).append("\n");
        details.append("Host: ").append(hostName).append("\n");
        details.append("Type: ").append(property.getPropertyType()).append("\n");
        details.append("Location: ").append(property.getLocation()).append("\n");
        details.append("Price per Night: $").append(property.getPricePerNight()).append("\n");
        details.append("Description: ").append(property.getDescription()).append("\n");
        details.append("Available: ").append(property.isAvailabilityStatus() ? "Yes" : "No").append("\n");
        details.append("Max Guests: ").append(property.getMaxGuests());
        
        JOptionPane.showMessageDialog(this, details.toString(), "Property Details", JOptionPane.INFORMATION_MESSAGE);
    }
} 