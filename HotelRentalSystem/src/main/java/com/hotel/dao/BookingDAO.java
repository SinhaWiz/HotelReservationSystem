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
        Connection conn = null;
        PreparedStatement bookingStmt = null;
        PreparedStatement revenueStmt = null;
        PreparedStatement invoiceCheckStmt = null;
        CallableStatement invoiceStmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            String bookingUpdateSql = "UPDATE bookings SET booking_status = 'CHECKED_OUT', payment_status = 'PAID', actual_check_out = SYSDATE WHERE booking_id = ? AND booking_status = 'CHECKED_IN'";
            bookingStmt = conn.prepareStatement(bookingUpdateSql);
            bookingStmt.setInt(1, bookingId);
            int updatedRows = bookingStmt.executeUpdate();
            if (updatedRows > 0) {
                String revenueSql = "UPDATE customers c SET c.total_spent = c.total_spent + (SELECT b.total_amount + NVL(b.services_total,0) + NVL(b.extra_charges,0) - NVL(b.discount_applied,0) FROM bookings b WHERE b.booking_id = ?), c.loyalty_points = c.loyalty_points + FLOOR((SELECT b.total_amount + NVL(b.services_total,0) + NVL(b.extra_charges,0) - NVL(b.discount_applied,0) FROM bookings b WHERE b.booking_id = ?) / 10) WHERE c.customer_id = (SELECT customer_id FROM bookings WHERE booking_id = ?)";
                revenueStmt = conn.prepareStatement(revenueSql);
                revenueStmt.setInt(1, bookingId);
                revenueStmt.setInt(2, bookingId);
                revenueStmt.setInt(3, bookingId);
                revenueStmt.executeUpdate();
                try {
                    invoiceCheckStmt = conn.prepareStatement("SELECT COUNT(*) FROM invoices WHERE booking_id = ?");
                    invoiceCheckStmt.setInt(1, bookingId);
                    rs = invoiceCheckStmt.executeQuery();
                    boolean invoiceExists = false;
                    if (rs.next()) invoiceExists = rs.getInt(1) > 0;
                    if (!invoiceExists) {
                        invoiceStmt = conn.prepareCall("{call generate_invoice(?)}");
                        invoiceStmt.setInt(1, bookingId);
                        invoiceStmt.execute();
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Invoice generation failed: " + e.getMessage());
                }
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (invoiceStmt != null) try { invoiceStmt.close(); } catch (SQLException ignored) {}
            if (invoiceCheckStmt != null) try { invoiceCheckStmt.close(); } catch (SQLException ignored) {}
            if (revenueStmt != null) try { revenueStmt.close(); } catch (SQLException ignored) {}
            if (bookingStmt != null) try { bookingStmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
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

        // Check if customer data is available (for JOINed queries)
        try {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String email = rs.getString("email");

            if (firstName != null && lastName != null) {
                booking.getCustomer().setFirstName(firstName);
                booking.getCustomer().setLastName(lastName);
                booking.getCustomer().setEmail(email);
                booking.getCustomer().setCustomerId(booking.getCustomerId());
            }
        } catch (SQLException e) {
            // Column not found, which is okay for non-JOIN queries
        }

        // Check if room data is available (for JOINed queries)
        try {
            String roomNumber = rs.getString("room_number");
            int typeId = rs.getInt("type_id");
            String typeName = rs.getString("type_name");

            if (roomNumber != null) {
                booking.getRoom().setRoomNumber(roomNumber);
                booking.getRoom().setRoomId(booking.getRoomId());
                booking.getRoom().setTypeId(typeId);
                booking.getRoom().getRoomType().setTypeName(typeName);
            }
        } catch (SQLException e) {
            // Column not found, which is okay for non-JOIN queries
        }

        // NEW: map payment status & actual check-in/out timestamps if present
        try {
            String paymentStatus = rs.getString("payment_status");
            if (paymentStatus != null) booking.setPaymentStatus(paymentStatus);
        } catch (SQLException ignore) {}
        try {
            Timestamp actIn = rs.getTimestamp("actual_check_in");
            if (actIn != null) booking.setActualCheckIn(new Date(actIn.getTime()));
        } catch (SQLException ignore) {}
        try {
            Timestamp actOut = rs.getTimestamp("actual_check_out");
            if (actOut != null) booking.setActualCheckOut(new Date(actOut.getTime()));
        } catch (SQLException ignore) {}

        return booking;
    }
}
