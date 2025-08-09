package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Room operations
 */
public class RoomDAO {
    
    /**
     * Find room by ID with room type details
     */
    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.room_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, roomId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRoom(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Find room by room number
     */
    public Room findByRoomNumber(String roomNumber) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.room_number = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomNumber);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRoom(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get all rooms with their types
     */
    public List<Room> findAll() throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "ORDER BY r.floor_number, r.room_number";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            
            return rooms;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get rooms by status
     */
    public List<Room> findByStatus(Room.RoomStatus status) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.status = ? " +
                    "ORDER BY r.floor_number, r.room_number";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.toString());
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            
            return rooms;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get available rooms for date range
     */
    public List<Room> findAvailableRooms(java.util.Date checkInDate, java.util.Date checkOutDate) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.status IN ('AVAILABLE', 'RESERVED') " +
                    "AND r.room_id NOT IN ( " +
                    "    SELECT b.room_id FROM bookings b " +
                    "    WHERE b.booking_status IN ('CONFIRMED', 'CHECKED_IN') " +
                    "    AND ( " +
                    "        (? >= b.check_in_date AND ? < b.check_out_date) " +
                    "        OR (? > b.check_in_date AND ? <= b.check_out_date) " +
                    "        OR (? <= b.check_in_date AND ? >= b.check_out_date) " +
                    "    ) " +
                    ") " +
                    "ORDER BY rt.base_price, r.floor_number, r.room_number";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            Date sqlCheckInDate = new Date(checkInDate.getTime());
            Date sqlCheckOutDate = new Date(checkOutDate.getTime());
            
            pstmt.setDate(1, sqlCheckInDate);
            pstmt.setDate(2, sqlCheckInDate);
            pstmt.setDate(3, sqlCheckOutDate);
            pstmt.setDate(4, sqlCheckOutDate);
            pstmt.setDate(5, sqlCheckInDate);
            pstmt.setDate(6, sqlCheckOutDate);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            
            return rooms;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get rooms by type
     */
    public List<Room> findByRoomType(int typeId) throws SQLException {
        String sql = "SELECT r.room_id, r.room_number, r.type_id, r.floor_number, " +
                    "r.status, r.last_maintenance, r.created_date, " +
                    "rt.type_name, rt.base_price, rt.max_occupancy, rt.amenities " +
                    "FROM rooms r " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE r.type_id = ? " +
                    "ORDER BY r.floor_number, r.room_number";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, typeId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            
            return rooms;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Update room status
     */
    public boolean updateRoomStatus(int roomId, Room.RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, roomId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Update room maintenance date
     */
    public boolean updateMaintenanceDate(int roomId, java.util.Date maintenanceDate) throws SQLException {
        String sql = "UPDATE rooms SET last_maintenance = ? WHERE room_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            if (maintenanceDate != null) {
                pstmt.setDate(1, new Date(maintenanceDate.getTime()));
            } else {
                pstmt.setNull(1, Types.DATE);
            }
            
            pstmt.setInt(2, roomId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Get all room types
     */
    public List<RoomType> getAllRoomTypes() throws SQLException {
        String sql = "SELECT type_id, type_name, base_price, max_occupancy, amenities, created_date " +
                    "FROM room_types ORDER BY base_price";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RoomType> roomTypes = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                roomTypes.add(mapResultSetToRoomType(rs));
            }
            
            return roomTypes;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get room utilization statistics using cursor procedure
     */
    public List<Object[]> getRoomUtilizationStats(java.util.Date startDate, java.util.Date endDate) throws SQLException {
        String sql = "{call get_room_utilization_stats(?, ?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;
        List<Object[]> stats = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.setDate(1, new Date(startDate.getTime()));
            cstmt.setDate(2, new Date(endDate.getTime()));
            cstmt.registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR);
            
            cstmt.execute();
            
            rs = (ResultSet) cstmt.getObject(3);
            
            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getString("room_number"),
                    rs.getString("type_name"),
                    rs.getDouble("base_price"),
                    rs.getInt("total_bookings"),
                    rs.getInt("completed_stays"),
                    rs.getInt("cancelled_bookings"),
                    rs.getDouble("total_revenue"),
                    rs.getInt("total_nights_occupied"),
                    rs.getDouble("avg_booking_value"),
                    rs.getDouble("occupancy_rate_percent"),
                    rs.getString("utilization_category")
                };
                stats.add(row);
            }
            
            return stats;
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt, rs);
        }
    }
    
    /**
     * Get room occupancy rate using Oracle function
     */
    public double getRoomOccupancyRate(java.util.Date startDate, java.util.Date endDate) throws SQLException {
        String sql = "{? = call get_room_occupancy_rate(?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.registerOutParameter(1, Types.NUMERIC);
            cstmt.setDate(2, new Date(startDate.getTime()));
            cstmt.setDate(3, new Date(endDate.getTime()));
            
            cstmt.execute();
            
            return cstmt.getDouble(1);
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Map ResultSet to Room object
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setTypeId(rs.getInt("type_id"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setStatusFromString(rs.getString("status"));
        
        Date lastMaintenance = rs.getDate("last_maintenance");
        if (lastMaintenance != null) {
            room.setLastMaintenance(new java.util.Date(lastMaintenance.getTime()));
        }
        
        Date createdDate = rs.getDate("created_date");
        if (createdDate != null) {
            room.setCreatedDate(new java.util.Date(createdDate.getTime()));
        }
        
        // Add room type details
        RoomType roomType = new RoomType();
        roomType.setTypeId(room.getTypeId());
        roomType.setTypeName(rs.getString("type_name"));
        roomType.setBasePrice(rs.getDouble("base_price"));
        roomType.setMaxOccupancy(rs.getInt("max_occupancy"));
        roomType.setAmenities(rs.getString("amenities"));
        room.setRoomType(roomType);
        
        return room;
    }
    
    /**
     * Map ResultSet to RoomType object
     */
    private RoomType mapResultSetToRoomType(ResultSet rs) throws SQLException {
        RoomType roomType = new RoomType();
        
        roomType.setTypeId(rs.getInt("type_id"));
        roomType.setTypeName(rs.getString("type_name"));
        roomType.setBasePrice(rs.getDouble("base_price"));
        roomType.setMaxOccupancy(rs.getInt("max_occupancy"));
        roomType.setAmenities(rs.getString("amenities"));
        
        Date createdDate = rs.getDate("created_date");
        if (createdDate != null) {
            roomType.setCreatedDate(new java.util.Date(createdDate.getTime()));
        }
        
        return roomType;
    }
}

