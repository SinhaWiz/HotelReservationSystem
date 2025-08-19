package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingDAO {
    public Booking create(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, " +
                    "check_out_date, booking_status, total_amount, discount_applied, " +
                    "extra_charges, created_by) VALUES (booking_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"booking_id"});
            
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setString(5, booking.getBookingStatus());
            pstmt.setDouble(6, booking.getTotalAmount());
            pstmt.setDouble(7, booking.getDiscountApplied());
            pstmt.setDouble(8, booking.getExtraCharges());
            pstmt.setString(9, booking.getCreatedBy());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    booking.setBookingId(rs.getLong(1));
                    return booking;
                }
            }
            
            return null;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBooking(rs);
            }
            
            return null;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public List<Booking> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY check_in_date DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
            return bookings;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public List<Booking> findByRoomIdAndDates(int roomId, Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE room_id = ? AND " +
                    "((check_in_date BETWEEN ? AND ?) OR " +
                    "(check_out_date BETWEEN ? AND ?) OR " +
                    "(check_in_date <= ? AND check_out_date >= ?))";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            pstmt.setDate(4, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(5, new java.sql.Date(endDate.getTime()));
            pstmt.setDate(6, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(7, new java.sql.Date(endDate.getTime()));
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
            return bookings;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public List<Booking> getCurrentBookings() throws SQLException {
        String sql = "SELECT b.*, c.first_name, c.last_name, c.email, " +
                    "r.room_number, r.type_id, rt.type_name " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.booking_status IN ('CONFIRMED', 'CHECKED_IN') " +
                    "AND b.check_out_date >= SYSDATE";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
            return bookings;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public List<Booking> getExpiredBookings() throws SQLException {
        String sql = "SELECT * FROM bookings WHERE check_out_date < TRUNC(SYSDATE) " +
                    "AND booking_status NOT IN ('COMPLETED', 'CANCELLED') " +
                    "ORDER BY check_out_date DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
            return bookings;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public void update(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET customer_id = ?, room_id = ?, check_in_date = ?, " +
                    "check_out_date = ?, booking_status = ?, total_amount = ?, " +
                    "discount_applied = ?, extra_charges = ? WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setString(5, booking.getBookingStatus());
            pstmt.setDouble(6, booking.getTotalAmount());
            pstmt.setDouble(7, booking.getDiscountApplied());
            pstmt.setDouble(8, booking.getExtraCharges());
            pstmt.setLong(9, booking.getBookingId());
            
            pstmt.executeUpdate();
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }

    public void cancel(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            pstmt.executeUpdate();
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }

    public List<Booking> getAll() throws SQLException {
        String sql = "SELECT * FROM bookings ORDER BY booking_id DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
            
            return bookings;
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, c.first_name, c.last_name, c.email, " +
                    "r.room_number, r.type_id, rt.type_name " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> getBookingsByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT b.*, c.first_name, c.last_name, c.email, " +
                    "r.room_number, r.type_id, rt.type_name " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.check_in_date <= ? AND b.check_out_date >= ?";

        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(endDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        }
        return bookings;
    }

    public boolean checkInCustomer(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = 'CHECKED_IN', " +
                    "actual_check_in = SYSDATE WHERE booking_id = ? " +
                    "AND booking_status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean checkOutCustomer(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = 'COMPLETED', " +
                    "actual_check_out = SYSDATE WHERE booking_id = ? " +
                    "AND booking_status = 'CHECKED_IN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Booking> findByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE check_in_date BETWEEN ? AND ? OR check_out_date BETWEEN ? AND ?";
        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(4, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        }
        return bookings;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getLong("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setRoomId(rs.getInt("room_id"));

        // Convert java.sql.Date to java.util.Date
        java.sql.Date checkInSqlDate = rs.getDate("check_in_date");
        booking.setCheckInDate(checkInSqlDate != null ? new java.util.Date(checkInSqlDate.getTime()) : null);

        java.sql.Date checkOutSqlDate = rs.getDate("check_out_date");
        booking.setCheckOutDate(checkOutSqlDate != null ? new java.util.Date(checkOutSqlDate.getTime()) : null);

        booking.setBookingStatus(rs.getString("booking_status"));
        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setDiscountApplied(rs.getDouble("discount_applied"));
        booking.setExtraCharges(rs.getDouble("extra_charges"));

        java.sql.Date createdSqlDate = rs.getDate("created_date");
        booking.setCreatedDate(createdSqlDate != null ? new java.util.Date(createdSqlDate.getTime()) : null);

        booking.setCreatedBy(rs.getString("created_by"));
        return booking;
    }
}
