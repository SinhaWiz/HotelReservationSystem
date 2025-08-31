package com.hotel.dao;

import com.hotel.model.ServiceUsage;
import com.hotel.model.RoomService;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for ServiceUsage operations
 */
public class ServiceUsageDAO {
    
    // Add service usage using stored procedure
    public long addServiceUsage(long bookingId, int customerId, int serviceId, int quantity) throws SQLException {
        String sql = "{CALL add_service_usage(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setLong(1, bookingId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, serviceId);
            stmt.setInt(4, quantity);
            stmt.registerOutParameter(5, Types.NUMERIC); // usage_id
            stmt.registerOutParameter(6, Types.NUMERIC); // success
            stmt.registerOutParameter(7, Types.VARCHAR); // message
            
            stmt.execute();
            
            int success = stmt.getInt(6);
            String message = stmt.getString(7);
            
            if (success == 1) {
                return stmt.getLong(5);
            } else {
                throw new SQLException("Failed to add service usage: " + message);
            }
        }
    }
    
    // Create service usage (alias for addServiceUsage)
    public long create(long bookingId, int customerId, int serviceId, int quantity) throws SQLException {
        return addServiceUsage(bookingId, customerId, serviceId, quantity);
    }

    // Find service usage by ID
    public ServiceUsage findById(long usageId) throws SQLException {
        String sql = "SELECT csu.usage_id, csu.booking_id, csu.customer_id, csu.service_id, " +
                    "csu.usage_date, csu.quantity, csu.unit_price, csu.total_cost, " +
                    "csu.is_complimentary, " + // removed csu.notes
                    "rs.service_name, rs.service_category " +
                    "FROM customer_service_usage csu " +
                    "JOIN room_services rs ON csu.service_id = rs.service_id " +
                    "WHERE csu.usage_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, usageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToServiceUsage(rs);
                }
            }
        }
        return null;
    }
    
    // Find all service usage for a customer
    public List<ServiceUsage> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT csu.usage_id, csu.booking_id, csu.customer_id, csu.service_id, " +
                    "csu.usage_date, csu.quantity, csu.unit_price, csu.total_cost, " +
                    "csu.is_complimentary, " + // removed csu.notes
                    "rs.service_name, rs.service_category " +
                    "FROM customer_service_usage csu " +
                    "JOIN room_services rs ON csu.service_id = rs.service_id " +
                    "WHERE csu.customer_id = ? ORDER BY csu.usage_date DESC";
        
        List<ServiceUsage> usageList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usageList.add(mapResultSetToServiceUsage(rs));
                }
            }
        }
        return usageList;
    }
    
    // Find all service usage for a booking
    public List<ServiceUsage> findByBookingId(long bookingId) throws SQLException {
        String sql = "SELECT csu.usage_id, csu.booking_id, csu.customer_id, csu.service_id, " +
                    "csu.usage_date, csu.quantity, csu.unit_price, csu.total_cost, " +
                    "csu.is_complimentary, " + // removed csu.notes
                    "rs.service_name, rs.service_category " +
                    "FROM customer_service_usage csu " +
                    "JOIN room_services rs ON csu.service_id = rs.service_id " +
                    "WHERE csu.booking_id = ? ORDER BY csu.usage_date DESC";
        
        List<ServiceUsage> usageList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usageList.add(mapResultSetToServiceUsage(rs));
                }
            }
        }
        return usageList;
    }
    
    // Get customer service summary using stored procedure
    public List<ServiceUsage> getCustomerServiceSummary(int customerId) throws SQLException {
        String sql = "{CALL get_customer_service_summary(?, ?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.registerOutParameter(2, Types.REF_CURSOR); // service_cursor
            stmt.registerOutParameter(3, Types.NUMERIC); // total_cost
            stmt.registerOutParameter(4, Types.NUMERIC); // success
            stmt.registerOutParameter(5, Types.VARCHAR); // message
            
            stmt.execute();
            
            int success = stmt.getInt(4);
            if (success == 1) {
                List<ServiceUsage> summaryList = new ArrayList<>();
                try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                    while (rs.next()) {
                        ServiceUsage summary = new ServiceUsage();
                        summary.setServiceId(0); // Summary record
                        summary.setCustomerId(customerId);
                        summary.setQuantity(rs.getInt("total_quantity"));
                        summary.setUnitPrice(rs.getDouble("avg_unit_price"));
                        summary.setTotalCost(rs.getDouble("total_cost"));

                        // Convert java.sql.Date to java.util.Date
                        java.sql.Date usageSqlDate = rs.getDate("last_used_date");
                        summary.setUsageDate(usageSqlDate != null ? new java.util.Date(usageSqlDate.getTime()) : null);

                        summary.setNotes("Service: " + rs.getString("service_name") +
                                       ", Category: " + rs.getString("service_category") +
                                       ", Bookings: " + rs.getInt("bookings_used"));
                        summaryList.add(summary);
                    }
                }
                return summaryList;
            } else {
                throw new SQLException("Failed to get service summary: " + stmt.getString(5));
            }
        }
    }
    
    // Calculate total service cost for customer
    public double calculateCustomerServiceTotal(int customerId, Long bookingId) throws SQLException {
        String sql = "SELECT calculate_customer_service_total(?, ?) FROM DUAL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            if (bookingId != null) {
                stmt.setLong(2, bookingId);
            } else {
                stmt.setNull(2, Types.NUMERIC);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    // Update service usage (removed notes column)
    public void update(ServiceUsage serviceUsage) throws SQLException {
        String sql = "UPDATE customer_service_usage SET quantity = ?, unit_price = ?, " +
                    "total_cost = ?, is_complimentary = ? WHERE usage_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, serviceUsage.getQuantity());
            stmt.setDouble(2, serviceUsage.getUnitPrice());
            stmt.setDouble(3, serviceUsage.getTotalCost());
            stmt.setString(4, serviceUsage.isComplimentary() ? "Y" : "N");
            stmt.setLong(5, serviceUsage.getUsageId());

            stmt.executeUpdate();
        }
    }
    
    // Delete service usage
    public void delete(long usageId) throws SQLException {
        String sql = "DELETE FROM customer_service_usage WHERE usage_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, usageId);
            stmt.executeUpdate();
        }
    }
    
    // Find service usage by date range
    public List<ServiceUsage> findByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT csu.usage_id, csu.booking_id, csu.customer_id, csu.service_id, " +
                    "csu.usage_date, csu.quantity, csu.unit_price, csu.total_cost, " +
                    "csu.is_complimentary, " + // removed csu.notes
                    "rs.service_name, rs.service_category " +
                    "FROM customer_service_usage csu " +
                    "JOIN room_services rs ON csu.service_id = rs.service_id " +
                    "WHERE csu.usage_date BETWEEN ? AND ? ORDER BY csu.usage_date DESC";
        
        List<ServiceUsage> usageList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, new java.sql.Date(startDate.getTime()));
            stmt.setDate(2, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usageList.add(mapResultSetToServiceUsage(rs));
                }
            }
        }
        return usageList;
    }
    
    // Get most popular services
    public List<ServiceUsage> getMostPopularServices(int limit) throws SQLException {
        String sql = "SELECT csu.service_id, rs.service_name, rs.service_category, " +
                    "SUM(csu.quantity) as total_quantity, " +
                    "SUM(csu.total_cost) as total_revenue, " +
                    "COUNT(DISTINCT csu.customer_id) as unique_customers " +
                    "FROM customer_service_usage csu " +
                    "JOIN room_services rs ON csu.service_id = rs.service_id " +
                    "GROUP BY csu.service_id, rs.service_name, rs.service_category " +
                    "ORDER BY total_quantity DESC " +
                    "FETCH FIRST ? ROWS ONLY";
        
        List<ServiceUsage> popularServices = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceUsage summary = new ServiceUsage();
                    summary.setServiceId(rs.getInt("service_id"));
                    summary.setQuantity(rs.getInt("total_quantity"));
                    summary.setTotalCost(rs.getDouble("total_revenue"));
                    summary.setNotes("Service: " + rs.getString("service_name") + 
                                   ", Category: " + rs.getString("service_category") +
                                   ", Unique Customers: " + rs.getInt("unique_customers"));
                    popularServices.add(summary);
                }
            }
        }
        return popularServices;
    }
    
    // Find service usage by customer ID and date
    public List<ServiceUsage> findByCustomerIdAndDate(int customerId, Date date) throws SQLException {
        String sql = "SELECT su.*, rs.* FROM service_usage su " +
                    "JOIN room_services rs ON su.service_id = rs.service_id " +
                    "WHERE su.customer_id = ? AND DATE(su.usage_date) = DATE(?)";

        List<ServiceUsage> usages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceUsage usage = mapResultSetToServiceUsage(rs);
                    RoomService service = new RoomService();
                    service.setServiceId(rs.getInt("service_id"));
                    service.setServiceName(rs.getString("service_name"));
                    service.setBasePrice(rs.getDouble("price"));
                    usage.setRoomService(service);
                    usages.add(usage);
                }
            }
        }
        return usages;
    }

    // Find most popular services
    public List<ServiceUsage> findMostPopular(int limit) throws SQLException {
        String sql = "SELECT su.*, rs.*, COUNT(*) as usage_count " +
                    "FROM service_usage su " +
                    "JOIN room_services rs ON su.service_id = rs.service_id " +
                    "GROUP BY su.service_id, rs.service_name, rs.price " +
                    "ORDER BY usage_count DESC " +
                    "FETCH ?";

        List<ServiceUsage> usages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceUsage usage = mapResultSetToServiceUsage(rs);
                    RoomService service = new RoomService();
                    service.setServiceId(rs.getInt("service_id"));
                    service.setServiceName(rs.getString("service_name"));
                    service.setBasePrice(rs.getDouble("price"));
                    usage.setRoomService(service);
                    usages.add(usage);
                }
            }
        }
        return usages;
    }

    // Helper method to map ResultSet to ServiceUsage object (notes column removed from table)
    private ServiceUsage mapResultSetToServiceUsage(ResultSet rs) throws SQLException {
        ServiceUsage usage = new ServiceUsage();
        usage.setUsageId(rs.getLong("usage_id"));
        usage.setBookingId(rs.getLong("booking_id"));
        usage.setCustomerId(rs.getInt("customer_id"));
        usage.setServiceId(rs.getInt("service_id"));
        usage.setUsageDate(rs.getTimestamp("usage_date"));
        usage.setQuantity(rs.getInt("quantity"));
        usage.setUnitPrice(rs.getDouble("unit_price"));
        usage.setTotalCost(rs.getDouble("total_cost"));
        usage.setComplimentary("Y".equals(rs.getString("is_complimentary")));
        // notes not stored in DB; leave null (can be populated for summaries elsewhere)
        return usage;
    }

    // ==================== MISSING METHODS ====================

    /**
     * Calculate customer service total with date parameter (alternative signature)
     */
    public double calculateCustomerServiceTotal(int customerId, Date fromDate) throws SQLException {
        String sql;
        if (fromDate != null) {
            sql = "SELECT NVL(SUM(total_cost), 0) FROM customer_service_usage " +
                  "WHERE customer_id = ? AND usage_date >= ?";
        } else {
            sql = "SELECT NVL(SUM(total_cost), 0) FROM customer_service_usage " +
                  "WHERE customer_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            if (fromDate != null) {
                stmt.setDate(2, new java.sql.Date(fromDate.getTime()));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
}
