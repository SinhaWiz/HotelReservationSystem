package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Booking entities
 */
public class BookingDAO {
    
    /**
     * Get all bookings from the database
     * @return List of Booking objects
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Booking";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setPropertyId(rs.getInt("property_id"));
                booking.setRenterId(rs.getInt("renter_id"));
                booking.setCheckInDate(rs.getDate("check_in_date"));
                booking.setCheckOutDate(rs.getDate("check_out_date"));
                booking.setTotalPrice(rs.getBigDecimal("total_price"));
                booking.setBookingStatus(Booking.BookingStatus.fromString(rs.getString("booking_status")));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all bookings: " + e.getMessage());
        }
        
        return bookings;
    }
    
    /**
     * Get a booking by ID
     * @param bookingId The ID of the booking to retrieve
     * @return Booking object if found, null otherwise
     */
    public Booking getBookingById(int bookingId) {
        String query = "SELECT * FROM Booking WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setPropertyId(rs.getInt("property_id"));
                    booking.setRenterId(rs.getInt("renter_id"));
                    booking.setCheckInDate(rs.getDate("check_in_date"));
                    booking.setCheckOutDate(rs.getDate("check_out_date"));
                    booking.setTotalPrice(rs.getBigDecimal("total_price"));
                    booking.setBookingStatus(Booking.BookingStatus.fromString(rs.getString("booking_status")));
                    return booking;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting booking by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all bookings for a property
     * @param propertyId The ID of the property
     * @return List of Booking objects for the specified property
     */
    public List<Booking> getBookingsByPropertyId(int propertyId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Booking WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setPropertyId(rs.getInt("property_id"));
                    booking.setRenterId(rs.getInt("renter_id"));
                    booking.setCheckInDate(rs.getDate("check_in_date"));
                    booking.setCheckOutDate(rs.getDate("check_out_date"));
                    booking.setTotalPrice(rs.getBigDecimal("total_price"));
                    booking.setBookingStatus(Booking.BookingStatus.fromString(rs.getString("booking_status")));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by property ID: " + e.getMessage());
        }
        
        return bookings;
    }
    
    /**
     * Get all bookings for a renter
     * @param renterId The ID of the renter
     * @return List of Booking objects for the specified renter
     */
    public List<Booking> getBookingsByRenterId(int renterId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Booking WHERE renter_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, renterId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setPropertyId(rs.getInt("property_id"));
                    booking.setRenterId(rs.getInt("renter_id"));
                    booking.setCheckInDate(rs.getDate("check_in_date"));
                    booking.setCheckOutDate(rs.getDate("check_out_date"));
                    booking.setTotalPrice(rs.getBigDecimal("total_price"));
                    booking.setBookingStatus(Booking.BookingStatus.fromString(rs.getString("booking_status")));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by renter ID: " + e.getMessage());
        }
        
        return bookings;
    }
    
    /**
     * Get all bookings by status
     * @param status The booking status to filter by
     * @return List of Booking objects with the specified status
     */
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Booking WHERE booking_status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status.getValue());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(rs.getInt("booking_id"));
                    booking.setPropertyId(rs.getInt("property_id"));
                    booking.setRenterId(rs.getInt("renter_id"));
                    booking.setCheckInDate(rs.getDate("check_in_date"));
                    booking.setCheckOutDate(rs.getDate("check_out_date"));
                    booking.setTotalPrice(rs.getBigDecimal("total_price"));
                    booking.setBookingStatus(status);
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by status: " + e.getMessage());
        }
        
        return bookings;
    }
    
    /**
     * Add a new booking to the database
     * @param booking The Booking object to add
     * @return true if successful, false otherwise
     */
    public boolean addBooking(Booking booking) {
        String query = "INSERT INTO Booking (property_id, renter_id, check_in_date, check_out_date, " +
                       "total_price, booking_status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, booking.getPropertyId());
            pstmt.setInt(2, booking.getRenterId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setBigDecimal(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getBookingStatus().getValue());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setBookingId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing booking in the database
     * @param booking The Booking object to update
     * @return true if successful, false otherwise
     */
    public boolean updateBooking(Booking booking) {
        String query = "UPDATE Booking SET property_id = ?, renter_id = ?, check_in_date = ?, " +
                       "check_out_date = ?, total_price = ?, booking_status = ? WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, booking.getPropertyId());
            pstmt.setInt(2, booking.getRenterId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setBigDecimal(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getBookingStatus().getValue());
            pstmt.setInt(7, booking.getBookingId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete a booking from the database
     * @param bookingId The ID of the booking to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteBooking(int bookingId) {
        String query = "DELETE FROM Booking WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if a property is available for the given date range
     * @param propertyId The ID of the property to check
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return true if the property is available, false otherwise
     */
    public boolean isPropertyAvailable(int propertyId, Date checkInDate, Date checkOutDate) {
        String query = "SELECT COUNT(*) FROM Booking WHERE property_id = ? AND booking_status != 'cancelled' " +
                       "AND ((check_in_date <= ? AND check_out_date >= ?) OR " +
                       "(check_in_date <= ? AND check_out_date >= ?) OR " +
                       "(check_in_date >= ? AND check_out_date <= ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            pstmt.setDate(2, checkInDate);
            pstmt.setDate(3, checkInDate);
            pstmt.setDate(4, checkOutDate);
            pstmt.setDate(5, checkOutDate);
            pstmt.setDate(6, checkInDate);
            pstmt.setDate(7, checkOutDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking property availability: " + e.getMessage());
        }
        
        return false;
    }
} 