package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Booking operations
 */
public class BookingDAO {
    
    /**
     * Create a new booking using stored procedure
     */
    public int createBooking(Booking booking) throws SQLException {
        String sql = "{call create_booking(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            // Input parameters
            cstmt.setInt(1, booking.getCustomerId());
            cstmt.setInt(2, booking.getRoomId());
            cstmt.setDate(3, new Date(booking.getCheckInDate().getTime()));
            cstmt.setDate(4, new Date(booking.getCheckOutDate().getTime()));
            cstmt.setString(5, booking.getSpecialRequests());
            
            // Output parameters
            cstmt.registerOutParameter(6, Types.NUMERIC); // booking_id
            cstmt.registerOutParameter(7, Types.NUMERIC); // total_amount
            cstmt.registerOutParameter(8, Types.VARCHAR); // message
            
            cstmt.execute();
            
            int bookingId = cstmt.getInt(6);
            double totalAmount = cstmt.getDouble(7);
            String message = cstmt.getString(8);
            
            if (bookingId > 0) {
                booking.setBookingId(bookingId);
                booking.setTotalAmount(totalAmount);
                return bookingId;
            } else {
                throw new SQLException("Booking creation failed: " + message);
            }
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Check room availability using stored procedure
     */
    public boolean isRoomAvailable(int roomId, java.util.Date checkInDate, java.util.Date checkOutDate) throws SQLException {
        String sql = "{call check_room_availability(?, ?, ?, ?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.setInt(1, roomId);
            cstmt.setDate(2, new Date(checkInDate.getTime()));
            cstmt.setDate(3, new Date(checkOutDate.getTime()));
            cstmt.registerOutParameter(4, Types.NUMERIC); // is_available
            cstmt.registerOutParameter(5, Types.VARCHAR); // message
            
            cstmt.execute();
            
            int isAvailable = cstmt.getInt(4);
            return isAvailable == 1;
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Check in customer using stored procedure
     */
    public boolean checkInCustomer(int bookingId) throws SQLException {
        String sql = "{call check_in_customer(?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.setInt(1, bookingId);
            cstmt.registerOutParameter(2, Types.VARCHAR); // message
            
            cstmt.execute();
            
            String message = cstmt.getString(2);
            return message.contains("successfully");
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Check out customer using stored procedure
     */
    public boolean checkOutCustomer(int bookingId) throws SQLException {
        String sql = "{call check_out_customer(?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.setInt(1, bookingId);
            cstmt.registerOutParameter(2, Types.VARCHAR); // message
            
            cstmt.execute();
            
            String message = cstmt.getString(2);
            return message.contains("successfully");
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Cancel booking using stored procedure
     */
    public boolean cancelBooking(int bookingId) throws SQLException {
        String sql = "{call cancel_booking(?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.setInt(1, bookingId);
            cstmt.registerOutParameter(2, Types.VARCHAR); // message
            
            cstmt.execute();
            
            String message = cstmt.getString(2);
            return message.contains("successfully");
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Find booking by ID with customer and room details
     */
    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in_date, " +
                    "b.check_out_date, b.actual_check_in, b.actual_check_out, b.booking_date, " +
                    "b.total_amount, b.discount_applied, b.extra_charges, b.payment_status, " +
                    "b.booking_status, b.special_requests, b.created_by, " +
                    "c.first_name, c.last_name, c.email, c.phone, " +
                    "r.room_number, rt.type_name, rt.base_price " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBookingWithDetails(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get all current reservations using cursor procedure
     */
    public List<Booking> getCurrentReservations() throws SQLException {
        String sql = "{call get_current_reservations(?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            cstmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            
            cstmt.execute();
            
            rs = (ResultSet) cstmt.getObject(1);
            
            while (rs.next()) {
                bookings.add(mapCursorResultToBooking(rs));
            }
            
            return bookings;
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt, rs);
        }
    }
    
    /**
     * Get bookings by customer ID
     */
    public List<Booking> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in_date, " +
                    "b.check_out_date, b.actual_check_in, b.actual_check_out, b.booking_date, " +
                    "b.total_amount, b.discount_applied, b.extra_charges, b.payment_status, " +
                    "b.booking_status, b.special_requests, b.created_by, " +
                    "r.room_number, rt.type_name " +
                    "FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.customer_id = ? " +
                    "ORDER BY b.booking_date DESC";
        
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
    
    /**
     * Get bookings by date range
     */
    public List<Booking> findByDateRange(java.util.Date startDate, java.util.Date endDate) throws SQLException {
        String sql = "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in_date, " +
                    "b.check_out_date, b.actual_check_in, b.actual_check_out, b.booking_date, " +
                    "b.total_amount, b.discount_applied, b.extra_charges, b.payment_status, " +
                    "b.booking_status, b.special_requests, b.created_by, " +
                    "c.first_name, c.last_name, r.room_number, rt.type_name " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "JOIN room_types rt ON r.type_id = rt.type_id " +
                    "WHERE b.check_in_date BETWEEN ? AND ? " +
                    "ORDER BY b.check_in_date";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> bookings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new Date(startDate.getTime()));
            pstmt.setDate(2, new Date(endDate.getTime()));
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToBookingWithDetails(rs));
            }
            
            return bookings;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Update booking
     */
    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET check_in_date = ?, check_out_date = ?, " +
                    "total_amount = ?, special_requests = ? WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setDate(1, new Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(2, new Date(booking.getCheckOutDate().getTime()));
            pstmt.setDouble(3, booking.getTotalAmount());
            pstmt.setString(4, booking.getSpecialRequests());
            pstmt.setInt(5, booking.getBookingId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Map ResultSet to Booking object
     */
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setRoomId(rs.getInt("room_id"));
        
        Date checkInDate = rs.getDate("check_in_date");
        if (checkInDate != null) {
            booking.setCheckInDate(new java.util.Date(checkInDate.getTime()));
        }
        
        Date checkOutDate = rs.getDate("check_out_date");
        if (checkOutDate != null) {
            booking.setCheckOutDate(new java.util.Date(checkOutDate.getTime()));
        }
        
        Date actualCheckIn = rs.getDate("actual_check_in");
        if (actualCheckIn != null) {
            booking.setActualCheckIn(new java.util.Date(actualCheckIn.getTime()));
        }
        
        Date actualCheckOut = rs.getDate("actual_check_out");
        if (actualCheckOut != null) {
            booking.setActualCheckOut(new java.util.Date(actualCheckOut.getTime()));
        }
        
        Date bookingDate = rs.getDate("booking_date");
        if (bookingDate != null) {
            booking.setBookingDate(new java.util.Date(bookingDate.getTime()));
        }
        
        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setDiscountApplied(rs.getDouble("discount_applied"));
        booking.setExtraCharges(rs.getDouble("extra_charges"));
        booking.setPaymentStatusFromString(rs.getString("payment_status"));
        booking.setBookingStatusFromString(rs.getString("booking_status"));
        booking.setSpecialRequests(rs.getString("special_requests"));
        booking.setCreatedBy(rs.getString("created_by"));
        
        return booking;
    }
    
    /**
     * Map ResultSet to Booking object with customer and room details
     */
    private Booking mapResultSetToBookingWithDetails(ResultSet rs) throws SQLException {
        Booking booking = mapResultSetToBooking(rs);
        
        // Add customer details
        Customer customer = new Customer();
        customer.setCustomerId(booking.getCustomerId());
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        booking.setCustomer(customer);
        
        // Add room details
        Room room = new Room();
        room.setRoomId(booking.getRoomId());
        room.setRoomNumber(rs.getString("room_number"));
        
        RoomType roomType = new RoomType();
        roomType.setTypeName(rs.getString("type_name"));
        roomType.setBasePrice(rs.getDouble("base_price"));
        room.setRoomType(roomType);
        
        booking.setRoom(room);
        
        return booking;
    }
    
    /**
     * Map cursor result to Booking object (for stored procedure results)
     */
    private Booking mapCursorResultToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        
        booking.setBookingId(rs.getInt("booking_id"));
        
        // Customer details
        Customer customer = new Customer();
        customer.setFirstName(rs.getString("customer_name").split(" ")[0]);
        customer.setLastName(rs.getString("customer_name").split(" ")[1]);
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        booking.setCustomer(customer);
        
        // Room details
        Room room = new Room();
        room.setRoomNumber(rs.getString("room_number"));
        
        RoomType roomType = new RoomType();
        roomType.setTypeName(rs.getString("room_type"));
        room.setRoomType(roomType);
        
        booking.setRoom(room);
        
        // Booking details
        Date checkInDate = rs.getDate("check_in_date");
        if (checkInDate != null) {
            booking.setCheckInDate(new java.util.Date(checkInDate.getTime()));
        }
        
        Date checkOutDate = rs.getDate("check_out_date");
        if (checkOutDate != null) {
            booking.setCheckOutDate(new java.util.Date(checkOutDate.getTime()));
        }
        
        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setBookingStatusFromString(rs.getString("booking_status"));
        booking.setSpecialRequests(rs.getString("special_requests"));
        
        return booking;
    }
}

