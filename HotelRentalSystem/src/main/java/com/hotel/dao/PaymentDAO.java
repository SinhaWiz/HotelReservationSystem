package com.hotel.dao;

import com.hotel.model.Payment;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment entities
 */
public class PaymentDAO {
    
    /**
     * Get all payments from the database
     * @return List of Payment objects
     */
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payment";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setPaymentDate(rs.getDate("payment_date"));
                payment.setPaymentMethod(Payment.PaymentMethod.fromString(rs.getString("payment_method")));
                payment.setAmount(rs.getBigDecimal("amount"));
                payments.add(payment);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all payments: " + e.getMessage());
        }
        
        return payments;
    }
    
    /**
     * Get a payment by ID
     * @param paymentId The ID of the payment to retrieve
     * @return Payment object if found, null otherwise
     */
    public Payment getPaymentById(int paymentId) {
        String query = "SELECT * FROM Payment WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, paymentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt("payment_id"));
                    payment.setBookingId(rs.getInt("booking_id"));
                    payment.setPaymentDate(rs.getDate("payment_date"));
                    payment.setPaymentMethod(Payment.PaymentMethod.fromString(rs.getString("payment_method")));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all payments for a booking
     * @param bookingId The ID of the booking
     * @return List of Payment objects for the specified booking
     */
    public List<Payment> getPaymentsByBookingId(int bookingId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payment WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt("payment_id"));
                    payment.setBookingId(rs.getInt("booking_id"));
                    payment.setPaymentDate(rs.getDate("payment_date"));
                    payment.setPaymentMethod(Payment.PaymentMethod.fromString(rs.getString("payment_method")));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments by booking ID: " + e.getMessage());
        }
        
        return payments;
    }
    
    /**
     * Add a new payment to the database
     * @param payment The Payment object to add
     * @return true if successful, false otherwise
     */
    public boolean addPayment(Payment payment) {
        String query = "INSERT INTO Payment (booking_id, payment_date, payment_method, amount) " +
                       "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDate(2, new java.sql.Date(payment.getPaymentDate().getTime()));
            pstmt.setString(3, payment.getPaymentMethod().getValue());
            pstmt.setBigDecimal(4, payment.getAmount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding payment: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing payment in the database
     * @param payment The Payment object to update
     * @return true if successful, false otherwise
     */
    public boolean updatePayment(Payment payment) {
        String query = "UPDATE Payment SET booking_id = ?, payment_date = ?, " +
                       "payment_method = ?, amount = ? WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDate(2, new java.sql.Date(payment.getPaymentDate().getTime()));
            pstmt.setString(3, payment.getPaymentMethod().getValue());
            pstmt.setBigDecimal(4, payment.getAmount());
            pstmt.setInt(5, payment.getPaymentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete a payment from the database
     * @param paymentId The ID of the payment to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePayment(int paymentId) {
        String query = "DELETE FROM Payment WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, paymentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting payment: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get total payments for a booking
     * @param bookingId The ID of the booking
     * @return The total amount paid for the booking
     */
    public double getTotalPaymentsForBooking(int bookingId) {
        String query = "SELECT SUM(amount) FROM Payment WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total payments for booking: " + e.getMessage());
        }
        
        return 0.0;
    }
} 