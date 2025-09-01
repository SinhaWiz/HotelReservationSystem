package com.hotel.view.panels;

import com.hotel.model.Booking;
import com.hotel.model.EnhancedHotelManagementService;

import com.hotel.model.Customer;
import com.hotel.model.VIPMember;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing hotel customers
 */
public class CustomerManagementPanel extends JPanel implements RefreshablePanel {

    private EnhancedHotelManagementService hotelService;

    // Table components
    private JTable customersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JScrollPane tableScrollPane;

    // Search components
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearSearchButton;

    // Action buttons
    private JButton addCustomerButton;
    private JButton editCustomerButton;
    private JButton viewDetailsButton;
    private JButton promoteToVIPButton;
    private JButton refreshButton;

    private static final String[] COLUMN_NAMES = {
            "Customer ID", "First Name", "Last Name", "Email", "Phone",
            "Total Spent", "Loyalty Points", "Registration Date", "VIP Status"
    };

    public CustomerManagementPanel(EnhancedHotelManagementService hotelService) {
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

        customersTable = new JTable(tableModel);
        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        customersTable.getTableHeader().setReorderingAllowed(false);

        // Set bold font for table
        Font boldFont = customersTable.getFont().deriveFont(Font.BOLD);
        customersTable.setFont(boldFont);
        customersTable.getTableHeader().setFont(boldFont);

        // Set column widths
        customersTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Customer ID
        customersTable.getColumnModel().getColumn(1).setPreferredWidth(100); // First Name
        customersTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Last Name
        customersTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Email
        customersTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Phone
        customersTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Total Spent
        customersTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Loyalty Points
        customersTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Registration Date
        customersTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // VIP Status

        // Table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        customersTable.setRowSorter(tableSorter);

        tableScrollPane = new JScrollPane(customersTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));

        // Search components
        searchField = new JTextField(25);
        searchField.setToolTipText("Search by name, email, or phone number");
        searchField.setFont(boldFont);
        searchButton = new JButton("Search");
        clearSearchButton = new JButton("Clear");

        // Action buttons
        addCustomerButton = new JButton("Add Customer");
        editCustomerButton = new JButton("Edit Customer");
        viewDetailsButton = new JButton("View Details");
        promoteToVIPButton = new JButton("Promote to VIP");
        refreshButton = new JButton("Refresh");

        // Set bold font for buttons
         boldFont = customersTable.getFont().deriveFont(Font.BOLD);
        addCustomerButton.setFont(boldFont);
        editCustomerButton.setFont(boldFont);
        viewDetailsButton.setFont(boldFont);
        promoteToVIPButton.setFont(boldFont);
        refreshButton.setFont(boldFont);
        searchButton.setFont(boldFont);
        clearSearchButton.setFont(boldFont);

        // Set black foreground for all buttons per requirement
        JButton[] buttons = {addCustomerButton, editCustomerButton, viewDetailsButton, promoteToVIPButton, refreshButton, searchButton, clearSearchButton};
        for (JButton b : buttons) {
            if (b != null) b.setForeground(Color.BLACK);
        }

        // Style buttons backgrounds (retain any existing specific styling)
        addCustomerButton.setBackground(Color.WHITE);
        promoteToVIPButton.setBackground(Color.WHITE);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel - Table
        add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel - Action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Search Customers"));

        // Set bold font for labels
        Font boldFont = getFont().deriveFont(Font.BOLD);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(boldFont);
        searchLabel.setForeground(Color.BLACK);

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(clearSearchButton);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));

        panel.add(addCustomerButton);
        panel.add(editCustomerButton);
        panel.add(viewDetailsButton);
        panel.add(promoteToVIPButton);
        panel.add(refreshButton);

        return panel;
    }

    private void setupEventHandlers() {
        // Search functionality
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        clearSearchButton.addActionListener(e -> clearSearch());

        // Action buttons
        addCustomerButton.addActionListener(e -> showAddCustomerDialog());
        editCustomerButton.addActionListener(e -> showEditCustomerDialog());
        viewDetailsButton.addActionListener(e -> viewCustomerDetails());
        promoteToVIPButton.addActionListener(e -> promoteToVIP());
        refreshButton.addActionListener(e -> refreshData());

        // Table selection
        customersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            refreshData();
            return;
        }

        try {
            List<Customer> customers = hotelService.searchCustomers(searchText);
            populateTable(customers);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error searching customers: " + e.getMessage(),
                    "Search Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSearch() {
        searchField.setText("");
        refreshData();
    }

    private void showAddCustomerDialog() {
        CustomerDialog dialog = new CustomerDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                hotelService, null);
        dialog.setVisible(true);
        if (dialog.isCustomerSaved()) {
            refreshData();
        }
    }

    private void showEditCustomerDialog() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
            return;
        }

        int modelRow = customersTable.convertRowIndexToModel(selectedRow);
        int customerId = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Customer customer = hotelService.findCustomerById(customerId);
            if (customer != null) {
                CustomerDialog dialog = new CustomerDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        hotelService, customer);
                dialog.setVisible(true);
                if (dialog.isCustomerSaved()) {
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading customer: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewCustomerDetails() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to view details.");
            return;
        }

        int modelRow = customersTable.convertRowIndexToModel(selectedRow);
        int customerId = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Customer customer = hotelService.findCustomerById(customerId);
            if (customer != null) {
                CustomerDetailsDialog dialog = new CustomerDetailsDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this), customer, hotelService);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading customer details: " + e.getMessage(),
                    "Details Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void promoteToVIP() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to promote to VIP.");
            return;
        }

        int modelRow = customersTable.convertRowIndexToModel(selectedRow);
        int customerId = (Integer) tableModel.getValueAt(modelRow, 0);
        String customerName = tableModel.getValueAt(modelRow, 1) + " " + tableModel.getValueAt(modelRow, 2);
        String vipStatus = (String) tableModel.getValueAt(modelRow, 8);

        if ("VIP".equals(vipStatus)) {
            JOptionPane.showMessageDialog(this, "Customer is already a VIP member.");
            return;
        }

        try {
            String eligibility = hotelService.checkVIPEligibility(customerId);

            if ("NOT_ELIGIBLE".equals(eligibility)) {
                JOptionPane.showMessageDialog(this,
                        "Customer does not meet VIP spending requirements.",
                        "VIP Promotion",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if ("ALREADY_VIP".equals(eligibility)) {
                JOptionPane.showMessageDialog(this, "Customer is already a VIP member.");
                return;
            }

            // Show VIP level selection dialog
            String[] levels = {"GOLD", "PLATINUM", "DIAMOND"};
            String selectedLevel = (String) JOptionPane.showInputDialog(this,
                    "Select VIP membership level for " + customerName + ":",
                    "VIP Promotion",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    levels,
                    eligibility);

            if (selectedLevel != null) {
                VIPMember.MembershipLevel level = VIPMember.MembershipLevel.valueOf(selectedLevel);
                VIPMember vipMember = hotelService.promoteToVIP(customerId, level);

                if (vipMember != null) {
                    JOptionPane.showMessageDialog(this,
                            customerName + " has been promoted to " + selectedLevel + " VIP member!",
                            "VIP Promotion Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "VIP promotion failed.");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error promoting customer to VIP: " + e.getMessage(),
                    "VIP Promotion Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = customersTable.getSelectedRow() != -1;
        editCustomerButton.setEnabled(hasSelection);
        viewDetailsButton.setEnabled(hasSelection);
        promoteToVIPButton.setEnabled(hasSelection);
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Customer> customers = hotelService.getAllCustomers();
                populateTable(customers);
                updateButtonStates();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error refreshing customer data: " + e.getMessage(),
                        "Refresh Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void populateTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

        for (Customer customer : customers) {
            // Check VIP status
            String vipStatus = "Regular";
            try {
                VIPMember vipMember = hotelService.getCustomerVIPStatus(customer.getCustomerId());
                if (vipMember != null && vipMember.isValidMembership()) {
                    vipStatus = "VIP";
                }
            } catch (Exception e) {
                // Ignore VIP status check errors
            }

            Object[] row = new Object[]{
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    String.format("$%.2f", customer.getTotalSpent()),
                    customer.getLoyaltyPoints(),
                    customer.getRegistrationDate() != null ?
                            dateFormat.format(customer.getRegistrationDate()) : "N/A",
                    vipStatus
            };
            tableModel.addRow(row);
        }
    }
}

/**
 * Dialog for adding/editing customers
 */
class CustomerDialog extends JDialog {
    private EnhancedHotelManagementService hotelService;
    private Customer customer;
    private boolean customerSaved = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField dobField;

    public CustomerDialog(JFrame parent, EnhancedHotelManagementService hotelService, Customer customer) {
        super(parent, customer == null ? "Add Customer" : "Edit Customer", true);
        this.hotelService = hotelService;
        this.customer = customer;
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Font boldFont = getFont().deriveFont(Font.BOLD);

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel firstNameLabel = new JLabel("First Name:*");
        firstNameLabel.setFont(boldFont);
        firstNameLabel.setForeground(Color.BLACK);
        formPanel.add(firstNameLabel, gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        firstNameField.setFont(boldFont);
        formPanel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lastNameLabel = new JLabel("Last Name:*");
        lastNameLabel.setFont(boldFont);
        lastNameLabel.setForeground(Color.BLACK);
        formPanel.add(lastNameLabel, gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        lastNameField.setFont(boldFont);
        formPanel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:*");
        emailLabel.setFont(boldFont);
        emailLabel.setForeground(Color.BLACK);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(boldFont);
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel phoneLabel = new JLabel("Phone:*");
        phoneLabel.setFont(boldFont);
        phoneLabel.setForeground(Color.BLACK);
        formPanel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        phoneField.setFont(boldFont);
        formPanel.add(phoneField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(boldFont);
        addressLabel.setForeground(Color.BLACK);
        formPanel.add(addressLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        addressArea = new JTextArea(3, 20);
        addressArea.setFont(boldFont);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        formPanel.add(addressScrollPane, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        JLabel dobLabel = new JLabel("Date of Birth (YYYY-MM-DD):");
        dobLabel.setFont(boldFont);
        dobLabel.setForeground(Color.BLACK);
        formPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        dobField = new JTextField(20);
        dobField.setFont(boldFont);
        formPanel.add(dobField, gbc);

        // Required fields note
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("* Required fields");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.BOLD | Font.ITALIC));
        noteLabel.setForeground(Color.BLACK);
        formPanel.add(noteLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton(customer == null ? "Add Customer" : "Update Customer");
        JButton cancelButton = new JButton("Cancel");

        saveButton.setFont(boldFont);
        cancelButton.setFont(boldFont);
        saveButton.setForeground(Color.BLACK);
        cancelButton.setForeground(Color.BLACK);

        saveButton.addActionListener(e -> saveCustomer());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Populate fields if editing
        if (customer != null) {
            populateFields();
        }
    }

    private void populateFields() {
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressArea.setText(customer.getAddress());

        if (customer.getDateOfBirth() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dobField.setText(dateFormat.format(customer.getDateOfBirth()));
        }
    }

    private void saveCustomer() {
        try {
            // Validate required fields
            if (firstNameField.getText().trim().isEmpty() ||
                    lastNameField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            // Parse date of birth if provided
            Date dateOfBirth = null;
            if (!dobField.getText().trim().isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateOfBirth = dateFormat.parse(dobField.getText().trim());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
                    return;
                }
            }

            if (customer == null) {
                // Add new customer
                Customer newCustomer = hotelService.registerCustomer(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        dateOfBirth,
                        addressArea.getText().trim()
                );

                if (newCustomer != null) {
                    customerSaved = true;
                    JOptionPane.showMessageDialog(this, "Customer added successfully!");
                    dispose();
                }
            } else {
                // Update existing customer
                customer.setFirstName(firstNameField.getText().trim());
                customer.setLastName(lastNameField.getText().trim());
                customer.setEmail(emailField.getText().trim());
                customer.setPhone(phoneField.getText().trim());
                customer.setAddress(addressArea.getText().trim());
                customer.setDateOfBirth(dateOfBirth);

                boolean success = hotelService.updateCustomer(customer);
                if (success) {
                    customerSaved = true;
                    JOptionPane.showMessageDialog(this, "Customer updated successfully!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update customer.");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving customer: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCustomerSaved() {
        return customerSaved;
    }
}

/**
 * Dialog for displaying detailed customer information
 */
class CustomerDetailsDialog extends JDialog {
    private Customer customer;
    private EnhancedHotelManagementService hotelService;

    public CustomerDetailsDialog(JFrame parent, Customer customer, EnhancedHotelManagementService hotelService) {
        super(parent, "Customer Details - " + customer.getFullName(), true);
        this.customer = customer;
        this.hotelService = hotelService;
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        Font boldFont = getFont().deriveFont(Font.BOLD);
        tabbedPane.setFont(boldFont);

        // Customer info tab
        JPanel infoPanel = createCustomerInfoPanel();
        tabbedPane.addTab("Customer Information", infoPanel);

        // Booking history tab
        JPanel historyPanel = createBookingHistoryPanel();
        tabbedPane.addTab("Booking History", historyPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.setFont(boldFont);
        closeButton.setForeground(Color.BLACK);
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createCustomerInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        int row = 0;
        addDetailRow(panel, gbc, row++, "Customer ID:", String.valueOf(customer.getCustomerId()));
        addDetailRow(panel, gbc, row++, "Full Name:", customer.getFullName());
        addDetailRow(panel, gbc, row++, "Email:", customer.getEmail());
        addDetailRow(panel, gbc, row++, "Phone:", customer.getPhone());
        addDetailRow(panel, gbc, row++, "Address:", customer.getAddress());

        if (customer.getDateOfBirth() != null) {
            addDetailRow(panel, gbc, row++, "Date of Birth:", dateFormat.format(customer.getDateOfBirth()));
        }

        addDetailRow(panel, gbc, row++, "Registration Date:",
                customer.getRegistrationDate() != null ? dateFormat.format(customer.getRegistrationDate()) : "N/A");
        addDetailRow(panel, gbc, row++, "Total Spent:", String.format("$%.2f", customer.getTotalSpent()));
        addDetailRow(panel, gbc, row++, "Loyalty Points:", String.valueOf(customer.getLoyaltyPoints()));

        // Check VIP status
        try {
            VIPMember vipMember = hotelService.getCustomerVIPStatus(customer.getCustomerId());
            if (vipMember != null && vipMember.isValidMembership()) {
                addDetailRow(panel, gbc, row++, "VIP Status:", "VIP Member");
                addDetailRow(panel, gbc, row++, "VIP Level:", vipMember.getMembershipLevelString());
                addDetailRow(panel, gbc, row++, "VIP Discount:", vipMember.getFormattedDiscountPercentage());
            } else {
                addDetailRow(panel, gbc, row++, "VIP Status:", "Regular Customer");

                // Check eligibility
                String eligibility = hotelService.checkVIPEligibility(customer.getCustomerId());
                if (!"NOT_ELIGIBLE".equals(eligibility) && !"ALREADY_VIP".equals(eligibility)) {
                    addDetailRow(panel, gbc, row++, "VIP Eligibility:", "Eligible for " + eligibility);
                }
            }
        } catch (Exception e) {
            addDetailRow(panel, gbc, row++, "VIP Status:", "Error checking status");
        }

        return panel;
    }

    private JPanel createBookingHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        try {
            List<Booking> bookings = hotelService.getCustomerBookingHistory(customer.getCustomerId());

            if (bookings.isEmpty()) {
                JLabel noBookingsLabel = new JLabel("No booking history found.", JLabel.CENTER);
                Font boldFont = getFont().deriveFont(Font.BOLD);
                noBookingsLabel.setFont(boldFont);
                noBookingsLabel.setForeground(Color.BLACK);
                panel.add(noBookingsLabel, BorderLayout.CENTER);
            } else {
                String[] columnNames = {"Booking ID", "Room", "Check-In", "Check-Out", "Amount", "Status"};
                DefaultTableModel historyModel = new DefaultTableModel(columnNames, 0);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                for (Booking booking : bookings) {
                    Object[] row = {
                            booking.getBookingId(),
                            booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "N/A",
                            dateFormat.format(booking.getCheckInDate()),
                            dateFormat.format(booking.getCheckOutDate()),
                            String.format("$%.2f", booking.getTotalAmount()),
                            booking.getBookingStatusString()
                    };
                    historyModel.addRow(row);
                }

                JTable historyTable = new JTable(historyModel);
                historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                Font boldFont = getFont().deriveFont(Font.BOLD);
                historyTable.setFont(boldFont);
                historyTable.getTableHeader().setFont(boldFont);
                JScrollPane scrollPane = new JScrollPane(historyTable);
                panel.add(scrollPane, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading booking history: " + e.getMessage(), JLabel.CENTER);
            Font boldFont = getFont().deriveFont(Font.BOLD);
            errorLabel.setFont(boldFont);
            errorLabel.setForeground(Color.BLACK);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        Font boldFont = getFont().deriveFont(Font.BOLD);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(boldFont);
        labelComponent.setForeground(Color.BLACK);
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value != null ? value : "N/A");
        valueLabel.setFont(boldFont);
        valueLabel.setForeground(Color.BLACK);
        panel.add(valueLabel, gbc);
    }
}