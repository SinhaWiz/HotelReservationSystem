package com.hotel.dao;

import com.hotel.model.Invoice;
import com.hotel.model.InvoiceLineItem;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Invoice operations
 */
public class InvoiceDAO {
    
    // Generate invoice using stored procedure
    public Invoice generateInvoice(long bookingId, double taxRate, String createdBy) throws SQLException {
        String sql = "{CALL generate_invoice(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setLong(1, bookingId);
            stmt.setDouble(2, taxRate);
            stmt.setString(3, createdBy);
            stmt.registerOutParameter(4, Types.NUMERIC); // invoice_id
            stmt.registerOutParameter(5, Types.VARCHAR); // invoice_number
            stmt.registerOutParameter(6, Types.NUMERIC); // success
            stmt.registerOutParameter(7, Types.VARCHAR); // message
            
            stmt.execute();
            
            int success = stmt.getInt(6);
            String message = stmt.getString(7);
            
            if (success == 1) {
                long invoiceId = stmt.getLong(4);
                String invoiceNumber = stmt.getString(5);
                
                // Retrieve the generated invoice
                Invoice invoice = findById(invoiceId);
                if (invoice != null) {
                    invoice.setInvoiceNumber(invoiceNumber);
                }
                return invoice;
            } else {
                throw new SQLException("Failed to generate invoice: " + message);
            }
        }
    }
    
    // Find invoice by ID
    public Invoice findById(long invoiceId) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, invoiceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    // Load line items
                    invoice.setLineItems(findLineItemsByInvoiceId(invoiceId));
                    return invoice;
                }
            }
        }
        return null;
    }
    
    // Find invoice by invoice number
    public Invoice findByInvoiceNumber(String invoiceNumber) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.invoice_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, invoiceNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    // Load line items
                    invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                    return invoice;
                }
            }
        }
        return null;
    }
    
    // Find invoices by customer ID
    public List<Invoice> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.customer_id = ? ORDER BY i.invoice_date DESC";
        
        List<Invoice> invoices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    // Load line items for each invoice
                    invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                    invoices.add(invoice);
                }
            }
        }
        return invoices;
    }
    
    // Find invoices by booking ID
    public List<Invoice> findByBookingId(long bookingId) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.booking_id = ? ORDER BY i.invoice_date DESC";
        
        List<Invoice> invoices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    // Load line items for each invoice
                    invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                    invoices.add(invoice);
                }
            }
        }
        return invoices;
    }
    
    // Find all invoices
    public List<Invoice> findAll() throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "ORDER BY i.invoice_date DESC";
        
        List<Invoice> invoices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                // Load line items for each invoice
                invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
        }
        return invoices;
    }
    
    // Find pending invoices
    public List<Invoice> findPendingInvoices() throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.payment_status = 'PENDING' " +
                    "ORDER BY i.due_date";
        
        List<Invoice> invoices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
        }
        return invoices;
    }
    
    // Find overdue invoices
    public List<Invoice> findOverdueInvoices() throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.payment_status IN ('PENDING', 'OVERDUE') " +
                    "AND i.due_date < SYSDATE " +
                    "ORDER BY i.due_date";
        
        List<Invoice> invoices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
        }
        return invoices;
    }
    
    // Update invoice payment status
    public void updatePaymentStatus(long invoiceId, Invoice.PaymentStatus paymentStatus, 
                                   Date paymentDate, String paymentMethod) throws SQLException {
        String sql = "UPDATE invoices SET payment_status = ?, payment_date = ?, payment_method = ? " +
                    "WHERE invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentStatus.name());
            if (paymentDate != null) {
                stmt.setTimestamp(2, new Timestamp(paymentDate.getTime()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setString(3, paymentMethod);
            stmt.setLong(4, invoiceId);
            
            stmt.executeUpdate();
        }
    }
    
    // Update invoice
    public void update(Invoice invoice) throws SQLException {
        String sql = "UPDATE invoices SET subtotal = ?, tax_amount = ?, discount_amount = ?, " +
                    "total_amount = ?, payment_status = ?, payment_date = ?, payment_method = ?, " +
                    "notes = ? WHERE invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, invoice.getSubtotal());
            stmt.setDouble(2, invoice.getTaxAmount());
            stmt.setDouble(3, invoice.getDiscountAmount());
            stmt.setDouble(4, invoice.getTotalAmount());
            stmt.setString(5, invoice.getPaymentStatusString());
            if (invoice.getPaymentDate() != null) {
                stmt.setTimestamp(6, new Timestamp(invoice.getPaymentDate().getTime()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }
            stmt.setString(7, invoice.getPaymentMethod());
            stmt.setString(8, invoice.getNotes());
            stmt.setLong(9, invoice.getInvoiceId());
            
            stmt.executeUpdate();
        }
    }
    
    // Find line items by invoice ID
    public List<InvoiceLineItem> findLineItemsByInvoiceId(long invoiceId) throws SQLException {
        String sql = "SELECT line_item_id, invoice_id, item_type, item_description, " +
                    "quantity, unit_price, line_total, service_id, usage_id " +
                    "FROM invoice_line_items WHERE invoice_id = ? ORDER BY line_item_id";
        
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, invoiceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lineItems.add(mapResultSetToInvoiceLineItem(rs));
                }
            }
        }
        return lineItems;
    }
    
    // Get invoice statistics
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT NVL(SUM(total_amount), 0) FROM invoices WHERE payment_status = 'PAID'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    
    // Get pending payment amount
    public double getPendingPaymentAmount() throws SQLException {
        String sql = "SELECT NVL(SUM(total_amount), 0) FROM invoices " +
                    "WHERE payment_status IN ('PENDING', 'OVERDUE')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    
    // Find invoice by number
    public Invoice findByNumber(String invoiceNumber) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE invoice_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, invoiceNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        return null;
    }

    // Find pending invoices
    public List<Invoice> findPending() throws SQLException {
        String sql = "SELECT * FROM invoices WHERE payment_status = 'PENDING'";
        List<Invoice> pendingInvoices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                pendingInvoices.add(mapResultSetToInvoice(rs));
            }
        }
        return pendingInvoices;
    }

    // Update invoice payment
    public void updatePayment(long invoiceId, Invoice.PaymentStatus status,
                            Date paymentDate, String paymentMethod) throws SQLException {
        String sql = "UPDATE invoices SET payment_status = ?, payment_date = ?, " +
                    "payment_method = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setDate(2, new java.sql.Date(paymentDate.getTime()));
            pstmt.setString(3, paymentMethod);
            pstmt.setLong(4, invoiceId);

            pstmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Invoice object
    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getLong("invoice_id"));
        invoice.setBookingId(rs.getLong("booking_id"));
        invoice.setCustomerId(rs.getInt("customer_id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setInvoiceDate(rs.getTimestamp("invoice_date"));
        invoice.setDueDate(rs.getTimestamp("due_date"));
        invoice.setSubtotal(rs.getDouble("subtotal"));
        invoice.setTaxAmount(rs.getDouble("tax_amount"));
        invoice.setDiscountAmount(rs.getDouble("discount_amount"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setPaymentStatusFromString(rs.getString("payment_status"));
        invoice.setPaymentDate(rs.getTimestamp("payment_date"));
        invoice.setPaymentMethod(rs.getString("payment_method"));
        invoice.setNotes(rs.getString("notes"));
        invoice.setCreatedBy(rs.getString("created_by"));
        return invoice;
    }
    
    // Helper method to map ResultSet to InvoiceLineItem object
    private InvoiceLineItem mapResultSetToInvoiceLineItem(ResultSet rs) throws SQLException {
        InvoiceLineItem lineItem = new InvoiceLineItem();
        lineItem.setLineItemId(rs.getLong("line_item_id"));
        lineItem.setInvoiceId(rs.getLong("invoice_id"));
        lineItem.setItemTypeFromString(rs.getString("item_type"));
        lineItem.setItemDescription(rs.getString("item_description"));
        lineItem.setQuantity(rs.getDouble("quantity"));
        lineItem.setUnitPrice(rs.getDouble("unit_price"));
        lineItem.setLineTotal(rs.getDouble("line_total"));
        
        int serviceId = rs.getInt("service_id");
        if (!rs.wasNull()) {
            lineItem.setServiceId(serviceId);
        }
        
        long usageId = rs.getLong("usage_id");
        if (!rs.wasNull()) {
            lineItem.setUsageId(usageId);
        }
        
        return lineItem;
    }

    public Invoice generate(long bookingId, double taxRate, String createdBy) throws SQLException {
        String sql = "{CALL generate_invoice(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setLong(1, bookingId);
            stmt.setDouble(2, taxRate);
            stmt.setString(3, createdBy);
            stmt.registerOutParameter(4, Types.NUMERIC); // invoice_id
            stmt.registerOutParameter(5, Types.VARCHAR); // invoice_number
            stmt.registerOutParameter(6, Types.NUMERIC); // success
            stmt.registerOutParameter(7, Types.VARCHAR); // message

            stmt.execute();

            int success = stmt.getInt(6);
            String message = stmt.getString(7);

            if (success == 1) {
                long invoiceId = stmt.getLong(4);
                String invoiceNumber = stmt.getString(5);

                // Retrieve the generated invoice
                Invoice invoice = findById(invoiceId);
                if (invoice != null) {
                    invoice.setInvoiceNumber(invoiceNumber);
                }
                return invoice;
            } else {
                throw new SQLException("Failed to generate invoice: " + message);
            }
        }
    }

    // ==================== MISSING METHODS ====================

    // Create invoice method
    public Invoice createInvoice(int customerId, long bookingId) throws SQLException {
        String sql = "INSERT INTO invoices (customer_id, booking_id, invoice_number, invoice_date, " +
                    "due_date, subtotal, tax_amount, total_amount, payment_status, created_by) " +
                    "VALUES (?, ?, ?, SYSDATE, SYSDATE + 30, 0, 0, 0, 'PENDING', 'SYSTEM')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            stmt.setLong(2, bookingId);
            stmt.setString(3, "INV-" + System.currentTimeMillis());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long invoiceId = generatedKeys.getLong(1);
                        return findById(invoiceId);
                    }
                }
            }
        }
        throw new SQLException("Failed to create invoice");
    }

    // Create invoice with full details
    public Invoice create(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (customer_id, booking_id, invoice_number, invoice_date, " +
                    "due_date, subtotal, tax_amount, discount_amount, total_amount, payment_status, " +
                    "notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, invoice.getCustomerId());
            stmt.setLong(2, invoice.getBookingId());
            stmt.setString(3, invoice.getInvoiceNumber());
            stmt.setTimestamp(4, new Timestamp(invoice.getInvoiceDate().getTime()));
            stmt.setTimestamp(5, new Timestamp(invoice.getDueDate().getTime()));
            stmt.setDouble(6, invoice.getSubtotal());
            stmt.setDouble(7, invoice.getTaxAmount());
            stmt.setDouble(8, invoice.getDiscountAmount());
            stmt.setDouble(9, invoice.getTotalAmount());
            stmt.setString(10, invoice.getPaymentStatusString());
            stmt.setString(11, invoice.getNotes());
            stmt.setString(12, invoice.getCreatedBy());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        invoice.setInvoiceId(generatedKeys.getLong(1));
                        return invoice;
                    }
                }
            }
        }
        throw new SQLException("Failed to create invoice");
    }

    // Update payment status with additional parameters
    public void updatePaymentStatus(long invoiceId, Invoice.PaymentStatus status) throws SQLException {
        String sql = "UPDATE invoices SET payment_status = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, invoiceId);

            stmt.executeUpdate();
        }
    }

    // Find invoices by payment status
    public List<Invoice> findByPaymentStatus(Invoice.PaymentStatus status) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.payment_status = ? ORDER BY i.invoice_date DESC";

        List<Invoice> invoices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                    invoices.add(invoice);
                }
            }
        }
        return invoices;
    }

    // Find unpaid invoices
    public List<Invoice> findUnpaidInvoices() throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.payment_status IN ('PENDING', 'OVERDUE') ORDER BY i.due_date";

        List<Invoice> invoices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                invoices.add(invoice);
            }
        }
        return invoices;
    }

    // Find invoices by date range
    public List<Invoice> findByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT i.invoice_id, i.booking_id, i.customer_id, i.invoice_number, " +
                    "i.invoice_date, i.due_date, i.subtotal, i.tax_amount, i.discount_amount, " +
                    "i.total_amount, i.payment_status, i.payment_date, i.payment_method, " +
                    "i.notes, i.created_by, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.invoice_date >= ? AND i.invoice_date <= ? ORDER BY i.invoice_date DESC";

        List<Invoice> invoices = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapResultSetToInvoice(rs);
                    invoice.setLineItems(findLineItemsByInvoiceId(invoice.getInvoiceId()));
                    invoices.add(invoice);
                }
            }
        }
        return invoices;
    }
}
