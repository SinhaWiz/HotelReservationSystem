package com.hotel.model;

import com.hotel.dao.*;
import com.hotel.model.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Main service class for Hotel Management System
 * Coordinates all business operations and provides a unified interface
 */
public class HotelManagementService {
    
    private CustomerDAO customerDAO;
    private BookingDAO bookingDAO;
    private VIPMemberDAO vipMemberDAO;
    private RoomDAO roomDAO;
    
    // Business constants
    private static final double VIP_SPENDING_THRESHOLD = 5000.0;
    
    /**
     * Constructor - Initialize all DAOs
     */
    public HotelManagementService() {
        this.customerDAO = new CustomerDAO();
        this.bookingDAO = new BookingDAO();
        this.vipMemberDAO = new VIPMemberDAO();
        this.roomDAO = new RoomDAO();
    }
    
    // ==================== CUSTOMER MANAGEMENT ====================
    
    /**
     * Register a new customer
     */
    public Customer registerCustomer(String firstName, String lastName, String email, 
                                   String phone, String address, Date dateOfBirth) throws SQLException {
        // Check if customer already exists
        Customer existingCustomer = customerDAO.findByEmail(email);
        if (existingCustomer != null) {
            throw new SQLException("Customer with email " + email + " already exists");
        }
        
        Customer customer = new Customer(firstName, lastName, email, phone);
        customer.setAddress(address);
        customer.setDateOfBirth(dateOfBirth);
        
        int customerId = customerDAO.createCustomer(customer);
        if (customerId > 0) {
            customer.setCustomerId(customerId);
            return customer;
        } else {
            throw new SQLException("Failed to register customer");
        }
    }
    
    /**
     * Find customer by ID
     */
    public Customer findCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }
    
    /**
     * Find customer by email
     */
    public Customer findCustomerByEmail(String email) throws SQLException {
        return customerDAO.findByEmail(email);
    }
    
    /**
     * Search customers
     */
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        return customerDAO.searchCustomers(searchTerm);
    }
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }
    
    /**
     * Update customer information
     */
    public boolean updateCustomer(Customer customer) throws SQLException {
        return customerDAO.updateCustomer(customer);
    }
    
    // ==================== BOOKING MANAGEMENT ====================
    
    /**
     * Create a new booking
     */
    public Booking createBooking(int customerId, int roomId, Date checkInDate, 
                               Date checkOutDate, String specialRequests) throws SQLException {
        // Validate customer exists
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new SQLException("Customer not found with ID: " + customerId);
        }
        
        // Validate room exists
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new SQLException("Room not found with ID: " + roomId);
        }
        
        // Check room availability
        if (!bookingDAO.isRoomAvailable(roomId, checkInDate, checkOutDate)) {
            throw new SQLException("Room is not available for the selected dates");
        }
        
        // Create booking
        Booking booking = new Booking(customerId, roomId, checkInDate, checkOutDate, 0.0);
        booking.setSpecialRequests(specialRequests);
        
        int bookingId = bookingDAO.createBooking(booking);
        if (bookingId > 0) {
            return bookingDAO.findById(bookingId);
        } else {
            throw new SQLException("Failed to create booking");
        }
    }
    
    /**
     * Check in customer
     */
    public boolean checkInCustomer(int bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found with ID: " + bookingId);
        }
        
        if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new SQLException("Booking is not in confirmed status");
        }
        
        return bookingDAO.checkInCustomer(bookingId);
    }
    
    /**
     * Check out customer
     */
    public boolean checkOutCustomer(int bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found with ID: " + bookingId);
        }
        
        if (booking.getBookingStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new SQLException("Customer is not checked in");
        }
        
        return bookingDAO.checkOutCustomer(bookingId);
    }
    
    /**
     * Cancel booking
     */
    public boolean cancelBooking(int bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found with ID: " + bookingId);
        }
        
        if (booking.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
            throw new SQLException("Cannot cancel a completed booking");
        }
        
        return bookingDAO.cancelBooking(bookingId);
    }
    
    /**
     * Get all current reservations
     */
    public List<Booking> getCurrentReservations() throws SQLException {
        return bookingDAO.getCurrentReservations();
    }
    
    /**
     * Get customer booking history
     */
    public List<Booking> getCustomerBookingHistory(int customerId) throws SQLException {
        return bookingDAO.findByCustomerId(customerId);
    }
    
    /**
     * Get bookings by date range
     */
    public List<Booking> getBookingsByDateRange(Date startDate, Date endDate) throws SQLException {
        return bookingDAO.findByDateRange(startDate, endDate);
    }
    
    // ==================== ROOM MANAGEMENT ====================
    
    /**
     * Get all rooms
     */
    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }
    
    /**
     * Get available rooms for date range
     */
    public List<Room> getAvailableRooms(Date checkInDate, Date checkOutDate) throws SQLException {
        return roomDAO.findAvailableRooms(checkInDate, checkOutDate);
    }
    
    /**
     * Get rooms by status
     */
    public List<Room> getRoomsByStatus(Room.RoomStatus status) throws SQLException {
        return roomDAO.findByStatus(status);
    }
    
    /**
     * Update room status
     */
    public boolean updateRoomStatus(int roomId, Room.RoomStatus status) throws SQLException {
        return roomDAO.updateRoomStatus(roomId, status);
    }
    
    /**
     * Get all room types
     */
    public List<RoomType> getAllRoomTypes() throws SQLException {
        return roomDAO.getAllRoomTypes();
    }
    
    /**
     * Get room occupancy rate
     */
    public double getRoomOccupancyRate(Date startDate, Date endDate) throws SQLException {
        return roomDAO.getRoomOccupancyRate(startDate, endDate);
    }
    
    // ==================== VIP MEMBER MANAGEMENT ====================
    
    /**
     * Get all VIP members
     */
    public List<VIPMember> getAllVIPMembers() throws SQLException {
        return vipMemberDAO.findAllActive();
    }
    
    /**
     * Get VIP members with detailed information
     */
    public List<VIPMember> getVIPMembersDetailed(String membershipLevel) throws SQLException {
        return vipMemberDAO.getAllVIPMembersDetailed(membershipLevel);
    }
    
    /**
     * Check if customer is VIP
     */
    public VIPMember getCustomerVIPStatus(int customerId) throws SQLException {
        return vipMemberDAO.findByCustomerId(customerId);
    }
    
    /**
     * Check VIP eligibility for customer
     */
    public String checkVIPEligibility(int customerId) throws SQLException {
        return vipMemberDAO.checkVIPEligibility(customerId);
    }
    
    /**
     * Promote customer to VIP
     */
    public VIPMember promoteToVIP(int customerId, VIPMember.MembershipLevel level) throws SQLException {
        // Check if customer exists
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new SQLException("Customer not found with ID: " + customerId);
        }
        
        // Check if already VIP
        VIPMember existingVIP = vipMemberDAO.findByCustomerId(customerId);
        if (existingVIP != null) {
            throw new SQLException("Customer is already a VIP member");
        }
        
        // Check eligibility
        String eligibility = vipMemberDAO.checkVIPEligibility(customerId);
        if ("NOT_ELIGIBLE".equals(eligibility)) {
            throw new SQLException("Customer does not meet VIP spending requirements");
        }
        
        // Create VIP membership
        VIPMember vipMember = new VIPMember(customerId, level, 
                                          VIPMember.getDefaultDiscountForLevel(level));
        vipMember.setBenefits(VIPMember.getDefaultBenefitsForLevel(level));
        
        int vipId = vipMemberDAO.createVIPMember(vipMember);
        if (vipId > 0) {
            return vipMemberDAO.findById(vipId);
        } else {
            throw new SQLException("Failed to create VIP membership");
        }
    }
    
    /**
     * Update VIP member information
     */
    public boolean updateVIPMember(VIPMember vipMember) throws SQLException {
        return vipMemberDAO.updateVIPMember(vipMember);
    }
    
    /**
     * Deactivate VIP membership
     */
    public boolean deactivateVIPMember(int vipId) throws SQLException {
        return vipMemberDAO.deactivateVIPMember(vipId);
    }
    
    /**
     * Process VIP renewals
     */
    public void processVIPRenewals() throws SQLException {
        vipMemberDAO.processVIPRenewals();
    }
    
    // ==================== REPORTING AND ANALYTICS ====================
    
    /**
     * Get customers eligible for VIP membership
     */
    public List<Customer> getVIPEligibleCustomers() throws SQLException {
        return customerDAO.getVIPEligibleCustomers(VIP_SPENDING_THRESHOLD);
    }
    
    /**
     * Get customer discount
     */
    public double getCustomerDiscount(int customerId) throws SQLException {
        return customerDAO.getCustomerDiscount(customerId);
    }
    
    /**
     * Get room utilization statistics
     */
    public List<Object[]> getRoomUtilizationStats(Date startDate, Date endDate) throws SQLException {
        return roomDAO.getRoomUtilizationStats(startDate, endDate);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check room availability
     */
    public boolean isRoomAvailable(int roomId, Date checkInDate, Date checkOutDate) throws SQLException {
        return bookingDAO.isRoomAvailable(roomId, checkInDate, checkOutDate);
    }
    
    /**
     * Get booking by ID
     */
    public Booking getBookingById(int bookingId) throws SQLException {
        return bookingDAO.findById(bookingId);
    }
    
    /**
     * Get room by ID
     */
    public Room getRoomById(int roomId) throws SQLException {
        return roomDAO.findById(roomId);
    }
    
    /**
     * Get room by room number
     */
    public Room getRoomByNumber(String roomNumber) throws SQLException {
        return roomDAO.findByRoomNumber(roomNumber);
    }
    
    /**
     * Validate date range
     */
    public boolean isValidDateRange(Date checkInDate, Date checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        
        Date today = new Date();
        return checkInDate.after(today) && checkOutDate.after(checkInDate);
    }
    
    /**
     * Calculate number of nights
     */
    public int calculateNights(Date checkInDate, Date checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        
        long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
        return (int) (diffInMillies / (1000 * 60 * 60 * 24));
    }
    
    /**
     * Get system statistics
     */
    public Object[] getSystemStatistics() throws SQLException {
        List<Customer> allCustomers = customerDAO.findAll();
        List<VIPMember> vipMembers = vipMemberDAO.findAllActive();
        List<Room> allRooms = roomDAO.findAll();
        List<Room> availableRooms = roomDAO.findByStatus(Room.RoomStatus.AVAILABLE);
        List<Room> occupiedRooms = roomDAO.findByStatus(Room.RoomStatus.OCCUPIED);
        List<Booking> currentReservations = bookingDAO.getCurrentReservations();
        
        return new Object[]{
            allCustomers.size(),      // Total customers
            vipMembers.size(),        // Total VIP members
            allRooms.size(),          // Total rooms
            availableRooms.size(),    // Available rooms
            occupiedRooms.size(),     // Occupied rooms
            currentReservations.size() // Current reservations
        };
    }
}

