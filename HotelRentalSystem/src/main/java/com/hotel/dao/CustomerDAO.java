package com.hotel.dao;

import com.hotel.model.Customer;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations
 */
public class CustomerDAO {
    
    /**
     * Create a new customer
     */
    public Customer create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, first_name, last_name, email, phone, " +
                    "address, date_of_birth, total_spent, loyalty_points) " +
                    "VALUES (customer_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"customer_id"});
            
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getAddress());
            
            if (customer.getDateOfBirth() != null) {
                pstmt.setDate(6, new Date(customer.getDateOfBirth().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            pstmt.setDouble(7, customer.getTotalSpent());
            pstmt.setInt(8, customer.getLoyaltyPoints());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int customerId = rs.getInt(1);
                    customer.setCustomerId(customerId);
                    return customer;
                }
            }
            
            return null;

        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Find customer by ID
     */
    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, first_name, last_name, email, phone, address, " +
                    "date_of_birth, total_spent, registration_date, is_active, loyalty_points " +
                    "FROM customers WHERE customer_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Find customer by email
     */
    public Customer findByEmail(String email) throws SQLException {
        String sql = "SELECT customer_id, first_name, last_name, email, phone, address, " +
                    "date_of_birth, total_spent, registration_date, is_active, loyalty_points " +
                    "FROM customers WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get all customers
     */
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT customer_id, first_name, last_name, email, phone, address, " +
                    "date_of_birth, total_spent, registration_date, is_active, loyalty_points " +
                    "FROM customers WHERE is_active = 'Y' ORDER BY registration_date DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Customer> customers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Update customer information
     */
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, " +
                    "phone = ?, address = ?, date_of_birth = ?, total_spent = ?, loyalty_points = ? " +
                    "WHERE customer_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getAddress());
            
            if (customer.getDateOfBirth() != null) {
                pstmt.setDate(6, new Date(customer.getDateOfBirth().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            pstmt.setDouble(7, customer.getTotalSpent());
            pstmt.setInt(8, customer.getLoyaltyPoints());
            pstmt.setInt(9, customer.getCustomerId());
            
            pstmt.executeUpdate();

        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Delete customer (soft delete - set inactive)
     */
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "UPDATE customers SET is_active = 'N' WHERE customer_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Search customers by name or email
     */
    public List<Customer> search(String searchTerm) throws SQLException {
        String sql = "SELECT customer_id, first_name, last_name, email, phone, address, " +
                    "date_of_birth, total_spent, registration_date, is_active, loyalty_points " +
                    "FROM customers WHERE is_active = 'Y' AND " +
                    "(UPPER(first_name) LIKE UPPER(?) OR UPPER(last_name) LIKE UPPER(?) OR " +
                    "UPPER(email) LIKE UPPER(?) OR phone LIKE ?) " +
                    "ORDER BY total_spent DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Customer> customers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get customers eligible for VIP membership
     */
    public List<Customer> getVIPEligibleCustomers(double spendingThreshold) throws SQLException {
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone, " +
                    "c.address, c.date_of_birth, c.total_spent, c.registration_date, " +
                    "c.is_active, c.loyalty_points " +
                    "FROM customers c " +
                    "LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y' " +
                    "WHERE c.is_active = 'Y' AND c.total_spent >= ? AND vm.customer_id IS NULL " +
                    "ORDER BY c.total_spent DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Customer> customers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, spendingThreshold);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            
            return customers;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get customer discount using Oracle function
     */
    public double getCustomerDiscount(int customerId) throws SQLException {
        String sql = "{? = call calculate_customer_discount(?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.registerOutParameter(1, Types.NUMERIC);
            cstmt.setInt(2, customerId);
            
            cstmt.execute();
            
            return cstmt.getDouble(1);
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Map ResultSet to Customer object
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            customer.setDateOfBirth(new java.util.Date(dateOfBirth.getTime()));
        }
        
        customer.setTotalSpent(rs.getDouble("total_spent"));
        
        Date registrationDate = rs.getDate("registration_date");
        if (registrationDate != null) {
            customer.setRegistrationDate(new java.util.Date(registrationDate.getTime()));
        }
        
        customer.setActive("Y".equals(rs.getString("is_active")));
        customer.setLoyaltyPoints(rs.getInt("loyalty_points"));
        
        return customer;
    }

    /**
     * Save customer (create or update)
     */
    public void save(Customer customer) throws SQLException {
        if (customer.getCustomerId() == 0) {
            create(customer);
        } else {
            update(customer);
        }
    }

    /**
     * Search customers by name
     */
    public List<Customer> searchByName(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM customers WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        }
        return customers;
    }

    /**
     * Find customers eligible for VIP membership based on activity
     */
    public List<Customer> findVIPEligible() throws SQLException {
        String sql = "SELECT c.* FROM customers c " +
                    "JOIN bookings b ON c.customer_id = b.customer_id " +
                    "GROUP BY c.customer_id, c.first_name, c.last_name, c.email, " +
                    "c.phone, c.address, c.created_date, c.last_updated " +
                    "HAVING COUNT(*) >= 5 AND SUM(b.total_amount) >= 5000";

        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // ==================== MISSING METHODS ====================

    /**
     * Delete customer (hard delete method)
     */
    public void delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Find VIP eligible customers (alternative method signature)
     */
    public List<Customer> findVIPEligibleCustomers() throws SQLException {
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone, " +
                    "c.address, c.date_of_birth, c.total_spent, c.registration_date, " +
                    "c.is_active, c.loyalty_points " +
                    "FROM customers c " +
                    "LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y' " +
                    "WHERE c.is_active = 'Y' AND c.total_spent >= 5000 AND vm.customer_id IS NULL " +
                    "ORDER BY c.total_spent DESC";

        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }
}
