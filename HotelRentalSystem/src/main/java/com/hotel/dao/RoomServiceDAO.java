package com.hotel.dao;

import com.hotel.model.RoomService;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for RoomService operations
 */
public class RoomServiceDAO {
    
    // Find service by ID
    public RoomService findById(int serviceId) throws SQLException {
        String sql = "SELECT service_id, service_name, service_description, service_category, " +
                    "base_price, is_active, created_date FROM room_services WHERE service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, serviceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoomService(rs);
                }
            }
        }
        return null;
    }
    
    // Find all services
    public List<RoomService> findAll() throws SQLException {
        String sql = "SELECT service_id, service_name, service_description, service_category, " +
                    "base_price, is_active, created_date FROM room_services ORDER BY service_name";
        
        List<RoomService> services = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                services.add(mapResultSetToRoomService(rs));
            }
        }
        return services;
    }
    
    // Find active services
    public List<RoomService> findActiveServices() throws SQLException {
        String sql = "SELECT service_id, service_name, service_description, service_category, " +
                    "base_price, is_active, created_date FROM room_services " +
                    "WHERE is_active = 'Y' ORDER BY service_name";
        
        List<RoomService> services = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                services.add(mapResultSetToRoomService(rs));
            }
        }
        return services;
    }
    
    // Find services by category
    public List<RoomService> findByCategory(RoomService.ServiceCategory category) throws SQLException {
        String sql = "SELECT service_id, service_name, service_description, service_category, " +
                    "base_price, is_active, created_date FROM room_services " +
                    "WHERE service_category = ? AND is_active = 'Y' ORDER BY service_name";
        
        List<RoomService> services = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToRoomService(rs));
                }
            }
        }
        return services;
    }
    
    // Find services available for a room type
    public List<RoomService> findServicesForRoomType(int roomTypeId) throws SQLException {
        String sql = "SELECT rs.service_id, rs.service_name, rs.service_description, " +
                    "rs.service_category, rs.base_price, rs.is_active, rs.created_date, " +
                    "rsa.is_complimentary " +
                    "FROM room_services rs " +
                    "JOIN room_service_assignments rsa ON rs.service_id = rsa.service_id " +
                    "WHERE rsa.room_type_id = ? AND rs.is_active = 'Y' " +
                    "ORDER BY rs.service_name";
        
        List<RoomService> services = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomTypeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RoomService service = mapResultSetToRoomService(rs);
                    // Note: You might want to add a field to track if service is complimentary
                    services.add(service);
                }
            }
        }
        return services;
    }
    
    // Save new service
    public void save(RoomService service) throws SQLException {
        String sql = "INSERT INTO room_services (service_id, service_name, service_description, " +
                    "service_category, base_price, is_active) VALUES (service_seq.NEXTVAL, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getServiceDescription());
            stmt.setString(3, service.getServiceCategoryString());
            stmt.setDouble(4, service.getBasePrice());
            stmt.setString(5, service.isActive() ? "Y" : "N");
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        service.setServiceId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }
    
    // Update existing service
    public void update(RoomService service) throws SQLException {
        String sql = "UPDATE room_services SET service_name = ?, service_description = ?, " +
                    "service_category = ?, base_price = ?, is_active = ? WHERE service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getServiceDescription());
            stmt.setString(3, service.getServiceCategoryString());
            stmt.setDouble(4, service.getBasePrice());
            stmt.setString(5, service.isActive() ? "Y" : "N");
            stmt.setInt(6, service.getServiceId());
            
            stmt.executeUpdate();
        }
    }
    
    // Delete service (soft delete by setting inactive)
    public void delete(int serviceId) throws SQLException {
        String sql = "UPDATE room_services SET is_active = 'N' WHERE service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, serviceId);
            stmt.executeUpdate();
        }
    }
    
    // Search services by name
    public List<RoomService> searchByName(String searchTerm) throws SQLException {
        String sql = "SELECT service_id, service_name, service_description, service_category, " +
                    "base_price, is_active, created_date FROM room_services " +
                    "WHERE UPPER(service_name) LIKE UPPER(?) AND is_active = 'Y' " +
                    "ORDER BY service_name";
        
        List<RoomService> services = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToRoomService(rs));
                }
            }
        }
        return services;
    }
    
    // Check if service is available for a specific room
    public boolean isServiceAvailableForRoom(int roomId, int serviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM room_service_assignments rsa " +
                    "JOIN rooms r ON rsa.room_type_id = r.room_type_id " +
                    "WHERE r.room_id = ? AND rsa.service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomId);
            stmt.setInt(2, serviceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // Get service categories
    public List<String> getServiceCategories() throws SQLException {
        String sql = "SELECT DISTINCT service_category FROM room_services " +
                    "WHERE is_active = 'Y' ORDER BY service_category";
        
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("service_category"));
            }
        }
        return categories;
    }
    
    // Create new service
    public void create(RoomService service) throws SQLException {
        String sql = "INSERT INTO room_services (service_id, service_name, service_description, " +
                    "service_category, base_price, is_active) " +
                    "VALUES (room_service_seq.NEXTVAL, ?, ?, ?, ?, 'Y')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, service.getServiceName());
            pstmt.setString(2, service.getServiceDescription());
            pstmt.setString(3, service.getServiceCategoryString());
            pstmt.setDouble(4, service.getBasePrice());

            pstmt.executeUpdate();
        }
    }

    // Find active services (alternative method)
    public List<RoomService> findActive() throws SQLException {
        String sql = "SELECT * FROM room_services WHERE is_active = 'Y'";
        List<RoomService> services = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                services.add(mapResultSetToRoomService(rs));
            }
        }
        return services;
    }

    // Find services by room type (alternative method)
    public List<RoomService> findByRoomType(int roomTypeId) throws SQLException {
        String sql = "SELECT rs.* FROM room_services rs " +
                    "JOIN room_type_services rts ON rs.service_id = rts.service_id " +
                    "WHERE rts.room_type_id = ? AND rs.is_active = 'Y'";
        List<RoomService> services = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomTypeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToRoomService(rs));
                }
            }
        }
        return services;
    }

    // Search services by term (alternative method)
    public List<RoomService> search(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM room_services WHERE is_active = 'Y' AND " +
                    "(LOWER(service_name) LIKE ? OR LOWER(service_description) LIKE ?)";
        List<RoomService> services = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToRoomService(rs));
                }
            }
        }
        return services;
    }

    // Helper method to map ResultSet to RoomService object
    private RoomService mapResultSetToRoomService(ResultSet rs) throws SQLException {
        RoomService service = new RoomService();
        service.setServiceId(rs.getInt("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setServiceDescription(rs.getString("service_description"));
        service.setServiceCategoryFromString(rs.getString("service_category"));
        service.setBasePrice(rs.getDouble("base_price"));
        service.setActive("Y".equals(rs.getString("is_active")));
        service.setCreatedDate(rs.getTimestamp("created_date"));
        return service;
    }
}
