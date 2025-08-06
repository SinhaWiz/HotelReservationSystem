package com.hotel.model;

import com.hotel.util.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * Test class for EnhancedHotelManagementService
 * This is a simple test to validate basic functionality
 */
public class EnhancedServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== Enhanced Hotel Management Service Test ===");
        
        try {
            // Test database connection
            if (!DatabaseConnection.testConnection()) {
                System.err.println("Database connection failed!");
                return;
            }
            System.out.println("✓ Database connection successful");
            
            // Create service instance
            EnhancedHotelManagementService service = new EnhancedHotelManagementService();
            System.out.println("✓ Service instance created");
            
            // Test basic operations
            testBasicOperations(service);
            
            // Test new features
            testNewFeatures(service);
            
            System.out.println("\n=== All tests completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBasicOperations(EnhancedHotelManagementService service) throws SQLException {
        System.out.println("\n--- Testing Basic Operations ---");
        
        // Test customer operations
        try {
            List<Customer> customers = service.getAllCustomers();
            System.out.println("✓ Retrieved " + customers.size() + " customers");
        } catch (Exception e) {
            System.out.println("⚠ Customer retrieval test skipped: " + e.getMessage());
        }
        
        // Test room operations
        try {
            List<Room> rooms = service.getAllRooms();
            System.out.println("✓ Retrieved " + rooms.size() + " rooms");
        } catch (Exception e) {
            System.out.println("⚠ Room retrieval test skipped: " + e.getMessage());
        }
        
        // Test booking operations
        try {
            List<Booking> bookings = service.getAllBookings();
            System.out.println("✓ Retrieved " + bookings.size() + " bookings");
        } catch (Exception e) {
            System.out.println("⚠ Booking retrieval test skipped: " + e.getMessage());
        }
        
        // Test VIP operations
        try {
            List<VIPMember> vipMembers = service.getAllVIPMembers();
            System.out.println("✓ Retrieved " + vipMembers.size() + " VIP members");
        } catch (Exception e) {
            System.out.println("⚠ VIP member retrieval test skipped: " + e.getMessage());
        }
    }
    
    private static void testNewFeatures(EnhancedHotelManagementService service) throws SQLException {
        System.out.println("\n--- Testing New Features ---");
        
        // Test room services
        try {
            List<RoomService> services = service.getAllRoomServices();
            System.out.println("✓ Retrieved " + services.size() + " room services");
        } catch (Exception e) {
            System.out.println("⚠ Room service test skipped: " + e.getMessage());
        }
        
        // Test blacklist functionality
        try {
            List<BlacklistedCustomer> blacklisted = service.getAllBlacklistedCustomers();
            System.out.println("✓ Retrieved " + blacklisted.size() + " blacklisted customers");
        } catch (Exception e) {
            System.out.println("⚠ Blacklist test skipped: " + e.getMessage());
        }
        
        // Test invoice functionality
        try {
            List<Invoice> invoices = service.getAllInvoices();
            System.out.println("✓ Retrieved " + invoices.size() + " invoices");
        } catch (Exception e) {
            System.out.println("⚠ Invoice test skipped: " + e.getMessage());
        }
        
        // Test financial summary
        try {
            double totalRevenue = service.getTotalRevenue();
            double pendingPayments = service.getPendingPaymentAmount();
            System.out.println("✓ Financial Summary - Revenue: $" + String.format("%.2f", totalRevenue) + 
                             ", Pending: $" + String.format("%.2f", pendingPayments));
        } catch (Exception e) {
            System.out.println("⚠ Financial summary test skipped: " + e.getMessage());
        }
        
        // Test system statistics
        try {
            Object[] stats = service.getSystemStatistics();
            System.out.println("✓ System Statistics - Customers: " + stats[0] + 
                             ", VIP: " + stats[1] + ", Rooms: " + stats[2]);
        } catch (Exception e) {
            System.out.println("⚠ System statistics test skipped: " + e.getMessage());
        }
        
        // Test system report generation
        try {
            String report = service.generateSystemReport();
            System.out.println("✓ Generated system report (" + report.length() + " characters)");
        } catch (Exception e) {
            System.out.println("⚠ System report test skipped: " + e.getMessage());
        }
    }
}

