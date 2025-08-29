package com.hotel.view.panels;

import com.hotel.model.*;
import com.hotel.model.EnhancedHotelManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing invoices and billing
 */
public class InvoiceManagementPanel extends JPanel {
    
    private EnhancedHotelManagementService hotelService;
    private JTable invoicesTable;
    private JTable lineItemsTable;
    private DefaultTableModel invoicesTableModel;
    private DefaultTableModel lineItemsTableModel;
    private JComboBox<String> bookingComboBox;
    private JTextField taxRateField;
    private JTextField createdByField;
    private JTextField invoiceSearchField;
    private JComboBox<Invoice.PaymentStatus> paymentStatusComboBox;
    private JTextField paymentMethodField;
    private JLabel financialSummaryLabel;
    
    public InvoiceManagementPanel() {
        this.hotelService = new EnhancedHotelManagementService();
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        loadInvoicesData();
        updateFinancialSummary();
    }
    
    private void initializeComponents() {
        // Invoices table
        String[] invoiceColumns = {"Invoice ID", "Invoice Number", "Customer", "Booking ID", 
                                 "Invoice Date", "Due Date", "Subtotal", "Tax", "Discount", 
                                 "Total", "Payment Status", "Payment Date", "Payment Method"};
        invoicesTableModel = new DefaultTableModel(invoiceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        invoicesTable = new JTable(invoicesTableModel);
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        invoicesTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // Invoice ID
        invoicesTable.getColumnModel().getColumn(1).setPreferredWidth(120);  // Invoice Number
        invoicesTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Customer
        invoicesTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Booking ID
        invoicesTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Invoice Date
        invoicesTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Due Date
        invoicesTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Subtotal
        invoicesTable.getColumnModel().getColumn(7).setPreferredWidth(60);   // Tax
        invoicesTable.getColumnModel().getColumn(8).setPreferredWidth(70);   // Discount
        invoicesTable.getColumnModel().getColumn(9).setPreferredWidth(80);   // Total
        invoicesTable.getColumnModel().getColumn(10).setPreferredWidth(100); // Payment Status
        invoicesTable.getColumnModel().getColumn(11).setPreferredWidth(100); // Payment Date
        invoicesTable.getColumnModel().getColumn(12).setPreferredWidth(120); // Payment Method
        
        // Line items table
        String[] lineItemColumns = {"Item ID", "Type", "Description", "Quantity", "Unit Price", "Line Total"};
        lineItemsTableModel = new DefaultTableModel(lineItemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lineItemsTable = new JTable(lineItemsTableModel);
        lineItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Form components
        bookingComboBox = new JComboBox<>();
        bookingComboBox.setPreferredSize(new Dimension(300, 25));
        
        taxRateField = new JTextField("0.08", 10); // Default 8% tax
        createdByField = new JTextField(System.getProperty("user.name", "Admin"), 15);
        invoiceSearchField = new JTextField(20);
        
        paymentStatusComboBox = new JComboBox<>(Invoice.PaymentStatus.values());
        paymentMethodField = new JTextField(15);
        
        financialSummaryLabel = new JLabel();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Invoice Generation Tab
        JPanel generationPanel = createInvoiceGenerationPanel();
        tabbedPane.addTab("Generate Invoices", generationPanel);
        
        // Invoice Management Tab
        JPanel managementPanel = createInvoiceManagementPanel();
        tabbedPane.addTab("Manage Invoices", managementPanel);
        
        // Financial Reports Tab
        JPanel reportsPanel = createFinancialReportsPanel();
        tabbedPane.addTab("Financial Reports", reportsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createInvoiceGenerationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with generation form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Generate New Invoice"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Booking selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Booking:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(bookingComboBox, gbc);
        
        JButton refreshBookingsButton = new JButton("Refresh");
        refreshBookingsButton.addActionListener(e -> loadBookingsForInvoicing());
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(refreshBookingsButton, gbc);
        
        // Tax rate
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tax Rate (%):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(taxRateField, gbc);
        
        // Created by
        gbc.gridx = 2;
        formPanel.add(new JLabel("Created By:"), gbc);
        gbc.gridx = 3;
        formPanel.add(createdByField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton generateButton = new JButton("Generate Invoice");
        generateButton.addActionListener(e -> generateInvoice());
        buttonPanel.add(generateButton);
        
        JButton previewButton = new JButton("Preview Invoice");
        previewButton.addActionListener(e -> previewInvoice());
        buttonPanel.add(previewButton);
        
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Center panel with booking details
        JTextArea bookingDetailsArea = new JTextArea(15, 60);
        bookingDetailsArea.setEditable(false);
        bookingDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bookingDetailsArea.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        
        // Add listener to booking combo box to show details
        bookingComboBox.addActionListener(e -> showBookingDetails(bookingDetailsArea));
        
        panel.add(new JScrollPane(bookingDetailsArea), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInvoiceManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with search and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(invoiceSearchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchInvoices());
        searchPanel.add(searchButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadInvoicesData();
            updateFinancialSummary();
        });
        searchPanel.add(refreshButton);
        
        JButton pendingButton = new JButton("Show Pending");
        pendingButton.addActionListener(e -> loadPendingInvoices());
        searchPanel.add(pendingButton);
        
        JButton overdueButton = new JButton("Show Overdue");
        overdueButton.addActionListener(e -> loadOverdueInvoices());
        searchPanel.add(overdueButton);
        
        JButton allButton = new JButton("Show All");
        allButton.addActionListener(e -> loadInvoicesData());
        searchPanel.add(allButton);
        
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(financialSummaryLabel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Top: Invoices table
        JScrollPane invoicesScrollPane = new JScrollPane(invoicesTable);
        invoicesScrollPane.setPreferredSize(new Dimension(900, 300));
        invoicesScrollPane.setBorder(BorderFactory.createTitledBorder("Invoices"));
        splitPane.setTopComponent(invoicesScrollPane);
        
        // Bottom: Line items table
        JScrollPane lineItemsScrollPane = new JScrollPane(lineItemsTable);
        lineItemsScrollPane.setPreferredSize(new Dimension(900, 200));
        lineItemsScrollPane.setBorder(BorderFactory.createTitledBorder("Invoice Line Items"));
        splitPane.setBottomComponent(lineItemsScrollPane);
        
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel with payment management
        JPanel paymentPanel = createPaymentManagementPanel();
        panel.add(paymentPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPaymentManagementPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Payment Management"));
        
        panel.add(new JLabel("Payment Status:"));
        panel.add(paymentStatusComboBox);
        
        panel.add(new JLabel("Payment Method:"));
        panel.add(paymentMethodField);
        
        JButton updatePaymentButton = new JButton("Update Payment Status");
        updatePaymentButton.addActionListener(e -> updatePaymentStatus());
        panel.add(updatePaymentButton);
        
        JButton printInvoiceButton = new JButton("Print Invoice");
        printInvoiceButton.addActionListener(e -> printInvoice());
        panel.add(printInvoiceButton);
        
        JButton emailInvoiceButton = new JButton("Email Invoice");
        emailInvoiceButton.addActionListener(e -> emailInvoice());
        panel.add(emailInvoiceButton);
        
        return panel;
    }
    
    private JPanel createFinancialReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea reportArea = new JTextArea(20, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton revenueReportButton = new JButton("Revenue Report");
        revenueReportButton.addActionListener(e -> {
            try {
                String report = generateRevenueReport();
                reportArea.setText(report);
            } catch (SQLException ex) {
                showError("Error generating revenue report: " + ex.getMessage());
            }
        });
        buttonPanel.add(revenueReportButton);
        
        JButton paymentReportButton = new JButton("Payment Status Report");
        paymentReportButton.addActionListener(e -> {
            try {
                String report = generatePaymentStatusReport();
                reportArea.setText(report);
            } catch (SQLException ex) {
                showError("Error generating payment report: " + ex.getMessage());
            }
        });
        buttonPanel.add(paymentReportButton);
        
        JButton customerBillingButton = new JButton("Customer Billing Summary");
        customerBillingButton.addActionListener(e -> showCustomerBillingSummary());
        buttonPanel.add(customerBillingButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void attachEventListeners() {
        // Invoice table selection listener
        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedInvoiceLineItems();
                loadSelectedInvoiceToPaymentForm();
            }
        });
    }
    
    private void loadInvoicesData() {
        try {
            List<Invoice> invoices = hotelService.getAllInvoices();
            invoicesTableModel.setRowCount(0);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Invoice invoice : invoices) {
                Date invDate = invoice.getInvoiceDate();
                Date dueDate = invoice.getDueDate();
                Date payDate = invoice.getPaymentDate();
                String invDateStr = invDate != null ? dateFormat.format(invDate) : "";
                String dueDateStr = dueDate != null ? dateFormat.format(dueDate) : "";
                String payDateStr = payDate != null ? dateFormat.format(payDate) : "";
                Object[] row = {
                    invoice.getInvoiceId(),
                    invoice.getInvoiceNumber(),
                    invoice.getCustomerName(),
                    invoice.getBookingId(),
                    invDateStr,
                    dueDateStr,
                    invoice.getFormattedSubtotal(),
                    invoice.getFormattedTaxAmount(),
                    invoice.getFormattedDiscountAmount(),
                    invoice.getFormattedTotalAmount(),
                    invoice.getPaymentStatus(),
                    payDateStr,
                    invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : ""
                };
                invoicesTableModel.addRow(row);
            }

        } catch (SQLException e) {
            showError("Error loading invoices: " + e.getMessage());
        }
    }
    
    private void loadBookingsForInvoicing() {
        try {
            bookingComboBox.removeAllItems();
            List<Booking> bookings = hotelService.getAllBookings();
            
            for (Booking booking : bookings) {
                // Only show checked-out bookings that don't have invoices yet
                if ("CHECKED_OUT".equals(booking.getBookingStatus())) {
                    List<Invoice> existingInvoices = hotelService.getBookingInvoices((int) booking.getBookingId());
                    if (existingInvoices.isEmpty()) {
                        Customer customer = hotelService.getCustomer(booking.getCustomerId());
                        String displayText = String.format("Booking %d - %s (Room %d) - $%.2f", 
                                                          booking.getBookingId(),
                                                          customer != null ? customer.getFullName() : "Unknown",
                                                          booking.getRoomId(),
                                                          booking.getTotalAmount());
                        bookingComboBox.addItem(displayText);
                    }
                }
            }
            
        } catch (SQLException e) {
            showError("Error loading bookings: " + e.getMessage());
        }
    }
    
    private void showBookingDetails(JTextArea detailsArea) {
        String selectedBooking = (String) bookingComboBox.getSelectedItem();
        if (selectedBooking == null) {
            detailsArea.setText("");
            return;
        }
        
        try {
            long bookingId = Long.parseLong(selectedBooking.split(" - ")[0].replace("Booking ", ""));
            Booking booking = hotelService.getBooking((int) bookingId);
            
            if (booking != null) {
                Customer customer = hotelService.getCustomer(booking.getCustomerId());
                List<ServiceUsage> serviceUsage = hotelService.getBookingServiceUsage(bookingId);
                
                StringBuilder details = new StringBuilder();
                details.append("BOOKING DETAILS\n");
                details.append("===============\n\n");
                details.append("Booking ID: ").append(booking.getBookingId()).append("\n");
                details.append("Customer: ").append(customer != null ? customer.getFullName() : "Unknown").append("\n");
                details.append("Email: ").append(customer != null ? customer.getEmail() : "Unknown").append("\n");
                details.append("Room: ").append(booking.getRoomId()).append("\n");
                details.append("Check-in: ").append(booking.getCheckInDate()).append("\n");
                details.append("Check-out: ").append(booking.getCheckOutDate()).append("\n");
                details.append("Base Amount: $").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
                details.append("Extra Charges: $").append(String.format("%.2f", booking.getExtraCharges())).append("\n");
                details.append("Discount Applied: $").append(String.format("%.2f", booking.getDiscountApplied())).append("\n\n");
                
                if (!serviceUsage.isEmpty()) {
                    details.append("SERVICE USAGE:\n");
                    double totalServiceCost = 0;
                    for (ServiceUsage usage : serviceUsage) {
                        details.append("- ").append(usage.getServiceName())
                               .append(" x").append(usage.getQuantity())
                               .append(" = $").append(String.format("%.2f", usage.getTotalCost())).append("\n");
                        totalServiceCost += usage.getTotalCost();
                    }
                    details.append("Total Service Cost: $").append(String.format("%.2f", totalServiceCost)).append("\n\n");
                }
                
                double grandTotal = booking.getTotalAmount() + booking.getExtraCharges();
                details.append("GRAND TOTAL: $").append(String.format("%.2f", grandTotal)).append("\n");
                
                detailsArea.setText(details.toString());
            }
            
        } catch (SQLException | NumberFormatException e) {
            detailsArea.setText("Error loading booking details: " + e.getMessage());
        }
    }
    
    private void generateInvoice() {
        String selectedBooking = (String) bookingComboBox.getSelectedItem();
        if (selectedBooking == null) {
            showError("Please select a booking to generate invoice");
            return;
        }
        
        try {
            long bookingId = Long.parseLong(selectedBooking.split(" - ")[0].replace("Booking ", ""));
            double taxRate = Double.parseDouble(taxRateField.getText().trim()) / 100.0;
            String createdBy = createdByField.getText().trim();
            
            if (createdBy.isEmpty()) {
                showError("Please enter who is creating the invoice");
                return;
            }
            
            Invoice invoice = hotelService.generateInvoice((int) bookingId, taxRate, createdBy);
            
            showSuccess("Invoice generated successfully!\nInvoice Number: " + invoice.getInvoiceNumber() +
                       "\nTotal Amount: " + invoice.getFormattedTotalAmount());
            
            loadInvoicesData();
            loadBookingsForInvoicing();
            updateFinancialSummary();
            
        } catch (SQLException e) {
            showError("Error generating invoice: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter a valid tax rate");
        }
    }
    
    private void previewInvoice() {
        String selectedBooking = (String) bookingComboBox.getSelectedItem();
        if (selectedBooking == null) {
            showError("Please select a booking to preview invoice");
            return;
        }
        
        try {
            long bookingId = Long.parseLong(selectedBooking.split(" - ")[0].replace("Booking ", ""));
            double taxRate = Double.parseDouble(taxRateField.getText().trim()) / 100.0;
            
            Booking booking = hotelService.getBooking((int) bookingId);
            Customer customer = hotelService.getCustomer(booking.getCustomerId());
            List<ServiceUsage> serviceUsage = hotelService.getBookingServiceUsage(bookingId);
            
            StringBuilder preview = new StringBuilder();
            preview.append("INVOICE PREVIEW\n");
            preview.append("===============\n\n");
            preview.append("Customer: ").append(customer.getFullName()).append("\n");
            preview.append("Email: ").append(customer.getEmail()).append("\n");
            preview.append("Booking ID: ").append(bookingId).append("\n\n");
            
            double subtotal = booking.getTotalAmount() + booking.getExtraCharges();
            for (ServiceUsage usage : serviceUsage) {
                subtotal += usage.getTotalCost();
            }
            
            double taxAmount = subtotal * taxRate;
            double total = subtotal + taxAmount;
            
            preview.append("Subtotal: $").append(String.format("%.2f", subtotal)).append("\n");
            preview.append("Tax (").append(String.format("%.1f", taxRate * 100)).append("%): $")
                   .append(String.format("%.2f", taxAmount)).append("\n");
            preview.append("Total: $").append(String.format("%.2f", total)).append("\n");
            
            JTextArea previewArea = new JTextArea(preview.toString());
            previewArea.setEditable(false);
            previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(previewArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Invoice Preview", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException | NumberFormatException e) {
            showError("Error generating preview: " + e.getMessage());
        }
    }
    
    private void loadSelectedInvoiceLineItems() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            lineItemsTableModel.setRowCount(0);
            return;
        }
        
        try {
            long invoiceId = (Long) invoicesTableModel.getValueAt(selectedRow, 0);
            Invoice invoice = hotelService.getInvoice((int) invoiceId);
            
            lineItemsTableModel.setRowCount(0);
            
            if (invoice != null && invoice.getLineItems() != null) {
                for (InvoiceLineItem lineItem : invoice.getLineItems()) {
                    Object[] row = {
                        lineItem.getLineItemId(),
                        lineItem.getItemTypeString(),
                        lineItem.getItemDescription(),
                        lineItem.getQuantity(),
                        lineItem.getFormattedUnitPrice(),
                        lineItem.getFormattedLineTotal()
                    };
                    lineItemsTableModel.addRow(row);
                }
            }
            
        } catch (SQLException e) {
            showError("Error loading line items: " + e.getMessage());
        }
    }
    
    private void loadSelectedInvoiceToPaymentForm() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow != -1) {
            String paymentStatus = (String) invoicesTableModel.getValueAt(selectedRow, 10);
            String paymentMethod = (String) invoicesTableModel.getValueAt(selectedRow, 12);
            
            try {
                paymentStatusComboBox.setSelectedItem(Invoice.PaymentStatus.valueOf(paymentStatus));
            } catch (IllegalArgumentException e) {
                paymentStatusComboBox.setSelectedIndex(0);
            }
            
            paymentMethodField.setText(paymentMethod != null ? paymentMethod : "");
        }
    }
    
    private void updatePaymentStatus() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an invoice to update payment status");
            return;
        }
        
        try {
            long invoiceId = (Long) invoicesTableModel.getValueAt(selectedRow, 0);
            Invoice.PaymentStatus paymentStatus = (Invoice.PaymentStatus) paymentStatusComboBox.getSelectedItem();
            String paymentMethod = paymentMethodField.getText().trim();
            
            Date paymentDate = null;
            if (paymentStatus == Invoice.PaymentStatus.PAID) {
                paymentDate = new Date();
            }
            
            hotelService.updateInvoicePaymentStatus((int) invoiceId, paymentStatus, paymentDate, paymentMethod);
            
            showSuccess("Payment status updated successfully!");
            loadInvoicesData();
            updateFinancialSummary();
            
        } catch (SQLException e) {
            showError("Error updating payment status: " + e.getMessage());
        }
    }
    
    private void searchInvoices() {
        String searchTerm = invoiceSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadInvoicesData();
            return;
        }
        
        try {
            // Search by invoice number
            Invoice invoice = hotelService.getInvoiceByNumber(searchTerm);
            invoicesTableModel.setRowCount(0);
            
            if (invoice != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Object[] row = {
                    invoice.getInvoiceId(),
                    invoice.getInvoiceNumber(),
                    invoice.getCustomerName(),
                    invoice.getBookingId(),
                    dateFormat.format(invoice.getInvoiceDate()),
                    dateFormat.format(invoice.getDueDate()),
                    invoice.getFormattedSubtotal(),
                    invoice.getFormattedTaxAmount(),
                    invoice.getFormattedDiscountAmount(),
                    invoice.getFormattedTotalAmount(),
                    invoice.getPaymentStatus(),
                    invoice.getPaymentDate() != null ? dateFormat.format(invoice.getPaymentDate()) : "",
                    invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : ""
                };
                invoicesTableModel.addRow(row);
            } else {
                showInfo("No invoice found with number: " + searchTerm);
            }
            
        } catch (SQLException e) {
            showError("Error searching invoices: " + e.getMessage());
        }
    }
    
    private void loadPendingInvoices() {
        try {
            List<Invoice> invoices = hotelService.getPendingInvoices();
            invoicesTableModel.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Invoice invoice : invoices) {
                Object[] row = {
                    invoice.getInvoiceId(),
                    invoice.getInvoiceNumber(),
                    invoice.getCustomerName(),
                    invoice.getBookingId(),
                    dateFormat.format(invoice.getInvoiceDate()),
                    dateFormat.format(invoice.getDueDate()),
                    invoice.getFormattedSubtotal(),
                    invoice.getFormattedTaxAmount(),
                    invoice.getFormattedDiscountAmount(),
                    invoice.getFormattedTotalAmount(),
                    invoice.getPaymentStatus(),
                    invoice.getPaymentDate() != null ? dateFormat.format(invoice.getPaymentDate()) : "",
                    invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : ""
                };
                invoicesTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error loading pending invoices: " + e.getMessage());
        }
    }
    
    private void loadOverdueInvoices() {
        try {
            List<Invoice> invoices = hotelService.getOverdueInvoices();
            invoicesTableModel.setRowCount(0);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Invoice invoice : invoices) {
                Object[] row = {
                    invoice.getInvoiceId(),
                    invoice.getInvoiceNumber(),
                    invoice.getCustomerName(),
                    invoice.getBookingId(),
                    dateFormat.format(invoice.getInvoiceDate()),
                    dateFormat.format(invoice.getDueDate()),
                    invoice.getFormattedSubtotal(),
                    invoice.getFormattedTaxAmount(),
                    invoice.getFormattedDiscountAmount(),
                    invoice.getFormattedTotalAmount(),
                    invoice.getPaymentStatus(),
                    invoice.getPaymentDate() != null ? dateFormat.format(invoice.getPaymentDate()) : "",
                    invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : ""
                };
                invoicesTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error loading overdue invoices: " + e.getMessage());
        }
    }
    
    private void updateFinancialSummary() {
        try {
            // Calculate revenue for the last 30 days
            Date endDate = new Date();
            Date startDate = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);

            double totalRevenue = hotelService.getTotalRevenue(startDate, endDate);
            double pendingPayments = hotelService.getPendingPaymentAmount();
            
            financialSummaryLabel.setText(String.format(
                "Financial Summary - Total Revenue (Last 30 days): $%.2f | Pending Payments: $%.2f",
                totalRevenue, pendingPayments));
            
        } catch (SQLException e) {
            financialSummaryLabel.setText("Error loading financial summary");
        }
    }
    
    private String generateRevenueReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("REVENUE REPORT\n");
        report.append("==============\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        // Calculate revenue for the last 30 days
        Date endDate = new Date();
        Date startDate = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);

        double totalRevenue = hotelService.getTotalRevenue(startDate, endDate);
        double pendingPayments = hotelService.getPendingPaymentAmount();
        
        report.append("FINANCIAL SUMMARY (Last 30 days):\n");
        report.append("- Total Revenue (Paid): $").append(String.format("%.2f", totalRevenue)).append("\n");
        report.append("- Pending Payments: $").append(String.format("%.2f", pendingPayments)).append("\n");
        report.append("- Total Outstanding: $").append(String.format("%.2f", totalRevenue + pendingPayments)).append("\n\n");
        
        // Recent invoices
        List<Invoice> recentInvoices = hotelService.getAllInvoices();
        report.append("RECENT INVOICES (Last 10):\n");
        int count = 0;
        for (Invoice invoice : recentInvoices) {
            if (count >= 10) break;
            report.append("- ").append(invoice.getInvoiceNumber())
                  .append(" - ").append(invoice.getCustomerName())
                  .append(" - ").append(invoice.getFormattedTotalAmount())
                  .append(" (").append(invoice.getPaymentStatus()).append(")")
                  .append("\n");
            count++;
        }
        
        return report.toString();
    }
    
    private String generatePaymentStatusReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("PAYMENT STATUS REPORT\n");
        report.append("=====================\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        List<Invoice> pendingInvoices = hotelService.getPendingInvoices();
        List<Invoice> overdueInvoices = hotelService.getOverdueInvoices();
        
        report.append("PENDING INVOICES (").append(pendingInvoices.size()).append("):\n");
        for (Invoice invoice : pendingInvoices) {
            report.append("- ").append(invoice.getInvoiceNumber())
                  .append(" - ").append(invoice.getCustomerName())
                  .append(" - ").append(invoice.getFormattedTotalAmount())
                  .append(" (Due: ").append(new SimpleDateFormat("yyyy-MM-dd").format(invoice.getDueDate())).append(")")
                  .append("\n");
        }
        
        report.append("\nOVERDUE INVOICES (").append(overdueInvoices.size()).append("):\n");
        for (Invoice invoice : overdueInvoices) {
            report.append("- ").append(invoice.getInvoiceNumber())
                  .append(" - ").append(invoice.getCustomerName())
                  .append(" - ").append(invoice.getFormattedTotalAmount())
                  .append(" (Due: ").append(new SimpleDateFormat("yyyy-MM-dd").format(invoice.getDueDate())).append(")")
                  .append("\n");
        }
        
        return report.toString();
    }
    
    private void showCustomerBillingSummary() {
        String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer ID:");
        if (customerIdStr != null && !customerIdStr.trim().isEmpty()) {
            try {
                int customerId = Integer.parseInt(customerIdStr.trim());
                List<Invoice> customerInvoices = hotelService.getCustomerInvoices(customerId);
                
                StringBuilder summary = new StringBuilder();
                summary.append("CUSTOMER BILLING SUMMARY\n");
                summary.append("========================\n");
                summary.append("Customer ID: ").append(customerId).append("\n\n");
                
                double totalBilled = 0;
                double totalPaid = 0;
                double totalPending = 0;
                
                for (Invoice invoice : customerInvoices) {
                    summary.append("Invoice: ").append(invoice.getInvoiceNumber())
                           .append(" - ").append(invoice.getFormattedTotalAmount())
                           .append(" (").append(invoice.getPaymentStatus()).append(")")
                           .append("\n");
                    
                    totalBilled += invoice.getTotalAmount();
                    if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
                        totalPaid += invoice.getTotalAmount();
                    } else {
                        totalPending += invoice.getTotalAmount();
                    }
                }
                
                summary.append("\nSUMMARY:\n");
                summary.append("Total Billed: $").append(String.format("%.2f", totalBilled)).append("\n");
                summary.append("Total Paid: $").append(String.format("%.2f", totalPaid)).append("\n");
                summary.append("Total Pending: $").append(String.format("%.2f", totalPending)).append("\n");
                
                JTextArea summaryArea = new JTextArea(summary.toString());
                summaryArea.setEditable(false);
                summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(summaryArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Customer Billing Summary", 
                                            JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException e) {
                showError("Error generating customer billing summary: " + e.getMessage());
            } catch (NumberFormatException e) {
                showError("Please enter a valid customer ID");
            }
        }
    }
    
    private void printInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an invoice to print");
            return;
        }
        
        showInfo("Print functionality would be implemented here");
        // Implementation would involve generating a printable invoice format
    }
    
    private void emailInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an invoice to email");
            return;
        }
        
        showInfo("Email functionality would be implemented here");
        // Implementation would involve sending invoice via email
    }
    
    public void refreshData() {
        loadInvoicesData();
        loadBookingsForInvoicing();
        updateFinancialSummary();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
