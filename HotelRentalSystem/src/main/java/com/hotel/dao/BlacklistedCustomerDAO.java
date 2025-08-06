package com.hotel.dao;
import com.hotel.util.DatabaseConnection;
import com.hotel.model.BlackListedCustomer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BlacklistedCustomer operations
 */
public class BlacklistedCustomerDAO {
    
    // Blacklist a customer using stored procedure
    public int blacklistCustomer(int customerId, String reason, String blacklistedBy, Date expiryDate) throws SQLException {
        String sql = "{CALL blacklist_customer(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, reason);
            stmt.setString(3, blacklistedBy);
            if (expiryDate != null) {
                stmt.setDate(4, expiryDate);
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.registerOutParameter(5, Types.NUMERIC); // blacklist_id
            stmt.registerOutParameter(6, Types.NUMERIC); // success
            stmt.registerOutParameter(7, Types.VARCHAR); // message
            
            stmt.execute();
            
            int success = stmt.getInt(6);
            String message = stmt.getString(7);
            
            if (success == 1) {
                return stmt.getInt(5);
            } else {
                throw new SQLException("Failed to blacklist customer: " + message);
            }
        }
    }
    
    // Remove customer from blacklist using stored procedure
    public void removeFromBlacklist(int customerId, String removedBy) throws SQLException {
        String sql = "{CALL remove_from_blacklist(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, removedBy);
            stmt.registerOutParameter(3, Types.NUMERIC); // success
            stmt.registerOutParameter(4, Types.VARCHAR); // message
            
            stmt.execute();
            
            int success = stmt.getInt(3);
            String message = stmt.getString(4);
            
            if (success != 1) {
                throw new SQLException("Failed to remove customer from blacklist: " + message);
            }
        }
    }
    
    // Check if customer is blacklisted using function
    public boolean isCustomerBlacklisted(int customerId) throws SQLException {
        String sql = "SELECT is_customer_blacklisted(?) FROM DUAL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "Y".equals(rs.getString(1));
                }
            }
        }
        return false;
    }
    
    // Find blacklist record by ID
    public BlacklistedCustomer findById(int blacklistId) throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "WHERE bl.blacklist_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, blacklistId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBlacklistedCustomer(rs);
                }
            }
        }
        return null;
    }
    
    // Find blacklist records by customer ID
    public List<BlacklistedCustomer> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "WHERE bl.customer_id = ? ORDER BY bl.blacklist_date DESC";
        
        List<BlacklistedCustomer> blacklistRecords = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    blacklistRecords.add(mapResultSetToBlacklistedCustomer(rs));
                }
            }
        }
        return blacklistRecords;
    }
    
    // Find all active blacklisted customers
    public List<BlacklistedCustomer> findActiveBlacklistedCustomers() throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "WHERE bl.is_active = 'Y' " +
                    "AND (bl.expiry_date IS NULL OR bl.expiry_date > SYSDATE) " +
                    "ORDER BY bl.blacklist_date DESC";
        
        List<BlacklistedCustomer> blacklistRecords = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                blacklistRecords.add(mapResultSetToBlacklistedCustomer(rs));
            }
        }
        return blacklistRecords;
    }
    
    // Find all blacklisted customers (including inactive)
    public List<BlacklistedCustomer> findAllBlacklistedCustomers() throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "ORDER BY bl.blacklist_date DESC";
        
        List<BlacklistedCustomer> blacklistRecords = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                blacklistRecords.add(mapResultSetToBlacklistedCustomer(rs));
            }
        }
        return blacklistRecords;
    }
    
    // Find expired blacklist records
    public List<BlacklistedCustomer> findExpiredBlacklistRecords() throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "WHERE bl.is_active = 'Y' " +
                    "AND bl.expiry_date IS NOT NULL " +
                    "AND bl.expiry_date <= SYSDATE " +
                    "ORDER BY bl.expiry_date";
        
        List<BlacklistedCustomer> expiredRecords = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                expiredRecords.add(mapResultSetToBlacklistedCustomer(rs));
            }
        }
        return expiredRecords;
    }
    
    // Search blacklisted customers by name or reason
    public List<BlacklistedCustomer> searchBlacklistedCustomers(String searchTerm) throws SQLException {
        String sql = "SELECT bl.blacklist_id, bl.customer_id, bl.blacklist_reason, " +
                    "bl.blacklisted_by, bl.blacklist_date, bl.expiry_date, " +
                    "bl.is_active, bl.notes, " +
                    "c.first_name, c.last_name, c.email " +
                    "FROM blacklisted_customers bl " +
                    "JOIN customers c ON bl.customer_id = c.customer_id " +
                    "WHERE (UPPER(c.first_name) LIKE UPPER(?) " +
                    "OR UPPER(c.last_name) LIKE UPPER(?) " +
                    "OR UPPER(bl.blacklist_reason) LIKE UPPER(?)) " +
                    "ORDER BY bl.blacklist_date DESC";
        
        List<BlacklistedCustomer> searchResults = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    searchResults.add(mapResultSetToBlacklistedCustomer(rs));
                }
            }
        }
        return searchResults;
    }
    
    // Update blacklist record
    public void update(BlacklistedCustomer blacklistedCustomer) throws SQLException {
        String sql = "UPDATE blacklisted_customers SET blacklist_reason = ?, " +
                    "expiry_date = ?, is_active = ?, notes = ? WHERE blacklist_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, blacklistedCustomer.getBlacklistReason());
            if (blacklistedCustomer.getExpiryDate() != null) {
                stmt.setDate(2, new java.sql.Date(blacklistedCustomer.getExpiryDate().getTime()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            stmt.setString(3, blacklistedCustomer.isActive() ? "Y" : "N");
            stmt.setString(4, blacklistedCustomer.getNotes());
            stmt.setInt(5, blacklistedCustomer.getBlacklistId());
            
            stmt.executeUpdate();
        }
    }
    
    // Get blacklist statistics
    public int getActiveBlacklistCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blacklisted_customers " +
                    "WHERE is_active = 'Y' " +
                    "AND (expiry_date IS NULL OR expiry_date > SYSDATE)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // Get blacklist count by reason
    public List<String> getBlacklistReasonStatistics() throws SQLException {
        String sql = "SELECT blacklist_reason, COUNT(*) as count " +
                    "FROM blacklisted_customers " +
                    "WHERE is_active = 'Y' " +
                    "GROUP BY blacklist_reason " +
                    "ORDER BY count DESC";
        
        List<String> statistics = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                statistics.add(rs.getString("blacklist_reason") + ": " + rs.getInt("count"));
            }
        }
        return statistics;
    }
    
    // Helper method to map ResultSet to BlacklistedCustomer object
    private BlacklistedCustomer mapResultSetToBlacklistedCustomer(ResultSet rs) throws SQLException {
        BlacklistedCustomer blacklistedCustomer = new BlacklistedCustomer();
        blacklistedCustomer.setBlacklistId(rs.getInt("blacklist_id"));
        blacklistedCustomer.setCustomerId(rs.getInt("customer_id"));
        blacklistedCustomer.setBlacklistReason(rs.getString("blacklist_reason"));
        blacklistedCustomer.setBlacklistedBy(rs.getString("blacklisted_by"));
        blacklistedCustomer.setBlacklistDate(rs.getTimestamp("blacklist_date"));
        blacklistedCustomer.setExpiryDate(rs.getTimestamp("expiry_date"));
        blacklistedCustomer.setActive("Y".equals(rs.getString("is_active")));
        blacklistedCustomer.setNotes(rs.getString("notes"));
        return blacklistedCustomer;
    }
}

