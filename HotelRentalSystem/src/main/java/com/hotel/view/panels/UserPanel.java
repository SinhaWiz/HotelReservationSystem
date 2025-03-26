package com.hotel.view.panels;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing users
 */
public class UserPanel extends JPanel {
    
    private UserDAO userDAO;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    
    /**
     * Constructor for the UserPanel
     */
    public UserPanel() {
        userDAO = new UserDAO();
        
        initComponents();
        loadUsers();
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
        
        JLabel titleLabel = new JLabel("Users Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(new Color(41, 128, 185));
        
        filterComboBox = new JComboBox<>(new String[]{"All Users", "Hosts Only", "Renters Only"});
        filterComboBox.addActionListener(e -> filterUsers());
        
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);
        
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        // Create table
        String[] columnNames = {"ID", "Name", "Email", "Phone", "Type", "Registration Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");
        JButton viewButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());
        viewButton.addActionListener(e -> viewUserDetails());
        refreshButton.addActionListener(e -> loadUsers());
        
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
     * Load users from the database and display them in the table
     */
    private void loadUsers() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Get all users
        List<User> users = userDAO.getAllUsers();
        
        // Add users to the table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (User user : users) {
            Object[] rowData = {
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserType(),
                dateFormat.format(user.getDateOfRegistration())
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Filter users based on the selected filter
     */
    private void filterUsers() {
        String filter = (String) filterComboBox.getSelectedItem();
        
        // Clear the table
        tableModel.setRowCount(0);
        
        List<User> users;
        
        if ("Hosts Only".equals(filter)) {
            users = userDAO.getAllHosts();
        } else if ("Renters Only".equals(filter)) {
            users = userDAO.getAllRenters();
        } else {
            users = userDAO.getAllUsers();
        }
        
        // Add users to the table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (User user : users) {
            Object[] rowData = {
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserType(),
                dateFormat.format(user.getDateOfRegistration())
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Add a new user
     */
    private void addUser() {
        // Create a dialog for adding a user
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add User", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"host", "renter"});
        JTextField addressField = new JTextField();
        JTextField registrationDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Registration Date:"));
        formPanel.add(registrationDateField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String type = (String) typeComboBox.getSelectedItem();
                String address = addressField.getText();
                Date registrationDate = new SimpleDateFormat("yyyy-MM-dd").parse(registrationDateField.getText());
                
                if (name.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and Email are required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                User user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNumber(phone);
                user.setUserType(User.UserType.fromString(type));
                user.setAddress(address);
                user.setDateOfRegistration(registrationDate);
                
                boolean success = userDAO.addUser(user);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "User added successfully!");
                    dialog.dispose();
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add user!", "Error", JOptionPane.ERROR_MESSAGE);
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
     * Edit the selected user
     */
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) userTable.getValueAt(selectedRow, 0);
        User user = userDAO.getUserById(userId);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a dialog for editing the user
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(user.getName());
        JTextField emailField = new JTextField(user.getEmail());
        JTextField phoneField = new JTextField(user.getPhoneNumber());
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"host", "renter"});
        JTextField addressField = new JTextField(user.getAddress());
        JTextField registrationDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(user.getDateOfRegistration()));
        
        // Set selected type
        typeComboBox.setSelectedItem(user.getUserType().getValue());
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Registration Date:"));
        formPanel.add(registrationDateField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String type = (String) typeComboBox.getSelectedItem();
                String address = addressField.getText();
                Date registrationDate = new SimpleDateFormat("yyyy-MM-dd").parse(registrationDateField.getText());
                
                if (name.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and Email are required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNumber(phone);
                user.setUserType(User.UserType.fromString(type));
                user.setAddress(address);
                user.setDateOfRegistration(registrationDate);
                
                boolean success = userDAO.updateUser(user);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "User updated successfully!");
                    dialog.dispose();
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update user!", "Error", JOptionPane.ERROR_MESSAGE);
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
     * Delete the selected user
     */
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) userTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user? This will also delete all associated properties and bookings.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userDAO.deleteUser(userId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * View details of the selected user
     */
    private void viewUserDetails() {
        int selectedRow = userTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) userTable.getValueAt(selectedRow, 0);
        User user = userDAO.getUserById(userId);
        
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        StringBuilder details = new StringBuilder();
        details.append("User ID: ").append(user.getUserId()).append("\n");
        details.append("Name: ").append(user.getName()).append("\n");
        details.append("Email: ").append(user.getEmail()).append("\n");
        details.append("Phone: ").append(user.getPhoneNumber()).append("\n");
        details.append("Type: ").append(user.getUserType()).append("\n");
        details.append("Address: ").append(user.getAddress()).append("\n");
        details.append("Registration Date: ").append(dateFormat.format(user.getDateOfRegistration()));
        
        JOptionPane.showMessageDialog(this, details.toString(), "User Details", JOptionPane.INFORMATION_MESSAGE);
    }
} 