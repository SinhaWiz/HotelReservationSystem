package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fixed version of the checkOutCustomer method to properly:
 * 1. Update payment status to PAID
 * 2. Track revenue by updating customer's total_spent
 * 3. Generate invoice if needed
 */
public class BookingDAOFix {

    /**
     * Replace the existing checkOutCustomer method in BookingDAO.java with this improved version
     */
    public boolean checkOutCustomer(int bookingId) throws SQLException {
        Connection conn = null;
        PreparedStatement bookingStmt = null;
        PreparedStatement revenueStmt = null;
        PreparedStatement invoiceCheckStmt = null;
        CallableStatement invoiceStmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Update booking status AND payment status
            String bookingUpdateSql = "UPDATE bookings SET booking_status = 'CHECKED_OUT', " +
                    "payment_status = 'PAID', actual_check_out = SYSDATE " +
                    "WHERE booking_id = ? AND booking_status = 'CHECKED_IN'";
            bookingStmt = conn.prepareStatement(bookingUpdateSql);
            bookingStmt.setInt(1, bookingId);
            int updatedRows = bookingStmt.executeUpdate();

            if (updatedRows > 0) {
                // 2. Update customer's total_spent and loyalty_points
                String revenueSql = "UPDATE customers c " +
                      "SET c.total_spent = c.total_spent + " +
                      "(SELECT b.total_amount + NVL(b.services_total, 0) + NVL(b.extra_charges, 0) - NVL(b.discount_applied, 0) " +
                      " FROM bookings b WHERE b.booking_id = ?), " +
                      "c.loyalty_points = c.loyalty_points + " +
                      "FLOOR((SELECT b.total_amount + NVL(b.services_total, 0) + NVL(b.extra_charges, 0) - NVL(b.discount_applied, 0) " +
                      " FROM bookings b WHERE b.booking_id = ?) / 10) " +
                      "WHERE c.customer_id = (SELECT customer_id FROM bookings WHERE booking_id = ?)";

                revenueStmt = conn.prepareStatement(revenueSql);
                revenueStmt.setInt(1, bookingId);
                revenueStmt.setInt(2, bookingId);
                revenueStmt.setInt(3, bookingId);
                revenueStmt.executeUpdate();

                // 3. Check if invoice exists, if not generate one
                try {
                    invoiceCheckStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM invoices WHERE booking_id = ?");
                    invoiceCheckStmt.setInt(1, bookingId);
                    rs = invoiceCheckStmt.executeQuery();

                    boolean invoiceExists = false;
                    if (rs.next()) {
                        invoiceExists = rs.getInt(1) > 0;
                    }

                    if (!invoiceExists) {
                        // Call the generate_invoice procedure
                        invoiceStmt = conn.prepareCall("{call generate_invoice(?)}");
                        invoiceStmt.setInt(1, bookingId);
                        invoiceStmt.execute();
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not generate invoice: " + e.getMessage());
                    // Continue with checkout even if invoice generation fails
                }

                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (invoiceStmt != null) try { invoiceStmt.close(); } catch (SQLException e) {}
            if (invoiceCheckStmt != null) try { invoiceCheckStmt.close(); } catch (SQLException e) {}
            if (revenueStmt != null) try { revenueStmt.close(); } catch (SQLException e) {}
            if (bookingStmt != null) try { bookingStmt.close(); } catch (SQLException e) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
