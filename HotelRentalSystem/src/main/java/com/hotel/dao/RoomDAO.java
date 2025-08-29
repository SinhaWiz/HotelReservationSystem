package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.util.DatabaseConnection;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class RoomDAO {
    // Map a ResultSet row (rooms + room_types joined) to Room object
    private Room mapRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setTypeId(rs.getInt("type_id"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setStatusFromString(rs.getString("status"));
        java.sql.Date lm = rs.getDate("last_maintenance");
        if (lm != null) room.setLastMaintenance(new java.util.Date(lm.getTime()));
        java.sql.Date lc = rs.getDate("last_cleaned");
        if (lc != null) room.setLastCleaned(new java.util.Date(lc.getTime()));
        java.sql.Date cd = rs.getDate("created_date");
        if (cd != null) room.setCreatedDate(new java.util.Date(cd.getTime()));
        room.setAmenities(rs.getString("amenities"));
        room.setNotes(rs.getString("notes"));
        room.setDescription(rs.getString("description"));
        room.setBasePrice(rs.getDouble("base_price"));
        // RoomType (optional enrichment)
        RoomType rt = new RoomType();
        rt.setTypeId(rs.getInt("type_id"));
        rt.setTypeName(rs.getString("type_name"));
        rt.setBaseRate(rs.getDouble("rt_base_price"));
        rt.setMaxOccupancy(rs.getInt("max_occupancy"));
        rt.setAmenities(rs.getString("rt_amenities"));
        rt.setDescription(rs.getString("rt_description"));
        room.setRoomType(rt);
        return room;
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT r.*, rt.type_name, rt.base_price rt_base_price, rt.max_occupancy, rt.amenities rt_amenities, rt.description rt_description " +
                     "FROM rooms r JOIN room_types rt ON r.type_id = rt.type_id ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapRoom(rs));
        }
        return rooms;
    }

    public Room findById(int id) throws SQLException {
        String sql = "SELECT r.*, rt.type_name, rt.base_price rt_base_price, rt.max_occupancy, rt.amenities rt_amenities, rt.description rt_description " +
                     "FROM rooms r JOIN room_types rt ON r.type_id = rt.type_id WHERE r.room_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRoom(rs);
            }
        }
        return null;
    }

    public List<Room> findAvailable() throws SQLException {
        String sql = "SELECT r.*, rt.type_name, rt.base_price rt_base_price, rt.max_occupancy, rt.amenities rt_amenities, rt.description rt_description " +
                     "FROM rooms r JOIN room_types rt ON r.type_id = rt.type_id WHERE r.status = 'AVAILABLE' ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapRoom(rs));
        }
        return rooms;
    }

    public List<Room> findAvailableForDates(Date checkIn, Date checkOut) throws SQLException {
        String sql = "SELECT r.*, rt.type_name, rt.base_price rt_base_price, rt.max_occupancy, rt.amenities rt_amenities, rt.description rt_description " +
                     "FROM rooms r JOIN room_types rt ON r.type_id = rt.type_id " +
                     "WHERE r.status = 'AVAILABLE' AND NOT EXISTS (" +
                     "  SELECT 1 FROM bookings b WHERE b.room_id = r.room_id " +
                     "    AND b.booking_status IN ('CONFIRMED','CHECKED_IN') " +
                     "    AND (b.check_in_date < ? AND b.check_out_date > ?)" +
                     ") ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(checkOut.getTime()));
            ps.setDate(2, new java.sql.Date(checkIn.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rooms.add(mapRoom(rs));
            }
        }
        return rooms;
    }

    public boolean checkAvailability(int roomId, Date checkIn, Date checkOut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings b WHERE b.room_id = ? " +
                     "AND b.booking_status IN ('CONFIRMED','CHECKED_IN') " +
                     "AND (b.check_in_date < ? AND b.check_out_date > ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, new java.sql.Date(checkOut.getTime()));
            ps.setDate(3, new java.sql.Date(checkIn.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    public void update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number=?, type_id=?, floor_number=?, status=?, last_maintenance=?, last_cleaned=?, amenities=?, notes=?, description=?, base_price=? WHERE room_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getTypeId());
            ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getStatus() != null ? room.getStatus().name() : "AVAILABLE");
            if (room.getLastMaintenance() != null) ps.setDate(5, new java.sql.Date(room.getLastMaintenance().getTime())); else ps.setNull(5, Types.DATE);
            if (room.getLastCleaned() != null) ps.setDate(6, new java.sql.Date(room.getLastCleaned().getTime())); else ps.setNull(6, Types.DATE);
            ps.setString(7, room.getAmenities());
            ps.setString(8, room.getNotes());
            ps.setString(9, room.getDescription());
            ps.setDouble(10, room.getBasePrice());
            ps.setInt(11, room.getRoomId());
            ps.executeUpdate();
        }
    }

    public List<RoomType> getAllRoomTypes() throws SQLException {
        String sql = "SELECT * FROM room_types ORDER BY type_id";
        List<RoomType> types = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomType rt = new RoomType();
                rt.setTypeId(rs.getInt("type_id"));
                rt.setTypeName(rs.getString("type_name"));
                rt.setBaseRate(rs.getDouble("base_price"));
                rt.setMaxOccupancy(rs.getInt("max_occupancy"));
                rt.setAmenities(rs.getString("amenities"));
                rt.setDescription(rs.getString("description"));
                types.add(rt);
            }
        }
        return types;
    }

    public List<Room> findByStatus(Room.RoomStatus status) throws SQLException {
        String sql = "SELECT r.*, rt.type_name, rt.base_price rt_base_price, rt.max_occupancy, rt.amenities rt_amenities, rt.description rt_description FROM rooms r JOIN room_types rt ON r.type_id=rt.type_id WHERE r.status = ? ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rooms.add(mapRoom(rs)); }
        }
        return rooms;
    }

    public boolean updateStatus(int roomId, Room.RoomStatus newStatus) throws SQLException {
        String sql = "UPDATE rooms SET status=? WHERE room_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Room> findAvailableRooms(Date checkIn, Date checkOut) throws SQLException {
        return findAvailableForDates(checkIn, checkOut);
    }
}
