package com.hotel.model;

import com.hotel.dao.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Enhanced service class for hotel management operations with new features
 * Includes room services, blacklist management, invoice generation, and more
 */
public class EnhancedHotelManagementService {
    
    private CustomerDAO customerDAO;
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private VIPMemberDAO vipMemberDAO;
    private RoomServiceDAO roomServiceDAO;
    private ServiceUsageDAO serviceUsageDAO;
    private BlacklistedCustomerDAO blacklistedCustomerDAO;
    private InvoiceDAO invoiceDAO;

    public EnhancedHotelManagementService() {
        this.customerDAO = new CustomerDAO();
        this.bookingDAO = new BookingDAO();
        this.roomDAO = new RoomDAO();
        this.vipMemberDAO = new VIPMemberDAO();
        this.roomServiceDAO = new RoomServiceDAO();
        this.serviceUsageDAO = new ServiceUsageDAO();
        this.blacklistedCustomerDAO = new BlacklistedCustomerDAO();
        this.invoiceDAO = new InvoiceDAO();
    }
    
    // ==================== ENHANCED CUSTOMER MANAGEMENT ====================
    
    public Customer createCustomer(Customer customer) throws SQLException {
        // Check if customer is blacklisted before creating
        if (blacklistedCustomerDAO.isCustomerBlacklisted(customer.getCustomerId())) {
            throw new SQLException("Cannot create booking for blacklisted customer");
        }
        customerDAO.save(customer);
        return customer;
    }
    
    public Customer getCustomer(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }
    
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }
    
    public void updateCustomer(Customer customer) throws SQLException {
        customerDAO.update(customer);
    }
    
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        return customerDAO.searchByName(searchTerm);
    }
    
    // ==================== ENHANCED BOOKING MANAGEMENT ====================
    
    public Booking createBooking(Booking booking) throws SQLException {
        // Check if customer is blacklisted
       blacklistedCustomerDAO.isCustomerBlacklisted(booking.getCustomerId()) {
            throw new SQLException("Cannot create booking for blacklisted customer");
        }
        
        // Validate room availability
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
            throw new SQLException("Room is not available for the selected dates");
        }
        
        bookingDAO.save(booking);
        return booking;
    }
    
    public Booking getBooking(long bookingId) throws SQLException {
        return bookingDAO.findById((int) bookingId);
    }
    
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.findAll();
    }
    
    public List<Booking> getCustomerBookings(int customerId) throws SQLException {
        return bookingDAO.findByCustomerId(customerId);
    }
    
    public List<Booking> getCurrentReservations() throws SQLException {
        return bookingDAO.findCurrentReservations();
    }
    
    public List<Booking> getExpiredReservations() throws SQLException {
        return bookingDAO.findExpiredReservations();
    }
    
    public void updateBooking(Booking booking) throws SQLException {
        bookingDAO.update(booking);
    }
    
    public void cancelBooking(long bookingId) throws SQLException {
        bookingDAO.cancelBooking(bookingId);
    }
    
    // ==================== ENHANCED ROOM MANAGEMENT ====================
    
    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }
    
    public List<Room> getAvailableRooms() throws SQLException {
        return roomDAO.findAvailableRooms();
    }
    
    public List<Room> getAvailableRooms(Date checkIn, Date checkOut) throws SQLException {
        return roomDAO.findAvailableRooms(checkIn, checkOut);
    }
    
    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) throws SQLException {
        return roomDAO.isRoomAvailable(roomId, checkIn, checkOut);
    }
    
    public void updateRoomStatus(int roomId, String status) throws SQLException {
        roomDAO.updateRoomStatus(roomId, status);
    }
    
    // ==================== ROOM SERVICE MANAGEMENT ====================
    
    public List<RoomService> getAllRoomServices() throws SQLException {
        return roomServiceDAO.findAll();
    }
    
    public List<RoomService> getActiveRoomServices() throws SQLException {
        return roomServiceDAO.findActiveServices();
    }
    
    public List<RoomService> getServicesForRoomType(int roomTypeId) throws SQLException {
        return roomServiceDAO.findServicesForRoomType(roomTypeId);
    }
    
    public RoomService getRoomService(int serviceId) throws SQLException {
        return roomServiceDAO.findById(serviceId);
    }
    
    public void createRoomService(RoomService service) throws SQLException {
        roomServiceDAO.save(service);
    }
    
    public void updateRoomService(RoomService service) throws SQLException {
        roomServiceDAO.update(service);
    }
    
    public List<RoomService> searchRoomServices(String searchTerm) throws SQLException {
        return roomServiceDAO.searchByName(searchTerm);
    }
    
    public List<RoomService> getServicesByCategory(RoomService.ServiceCategory category) throws SQLException {
        return roomServiceDAO.findByCategory(category);
    }
    
    // ==================== SERVICE USAGE MANAGEMENT ====================
    
    public long addServiceUsage(long bookingId, int customerId, int serviceId, int quantity) throws SQLException {
        return serviceUsageDAO.addServiceUsage(bookingId, customerId, serviceId, quantity);
    }
    
    public List<ServiceUsage> getCustomerServiceUsage(int customerId) throws SQLException {
        return serviceUsageDAO.findByCustomerId(customerId);
    }
    
    public List<ServiceUsage> getBookingServiceUsage(long bookingId) throws SQLException {
        return serviceUsageDAO.findByBookingId(bookingId);
    }
    
    public List<ServiceUsage> getCustomerServiceSummary(int customerId) throws SQLException {
        return serviceUsageDAO.getCustomerServiceSummary(customerId);
    }
    
    public double calculateCustomerServiceTotal(int customerId, Long bookingId) throws SQLException {
        return serviceUsageDAO.calculateCustomerServiceTotal(customerId, bookingId);
    }
    
    public List<ServiceUsage> getMostPopularServices(int limit) throws SQLException {
        return serviceUsageDAO.getMostPopularServices(limit);
    }
    
    // ==================== BLACKLIST MANAGEMENT ====================
    
    public int blacklistCustomer(int customerId, String reason, String blacklistedBy, Date expiryDate) throws SQLException {
        return blacklistedCustomerDAO.blacklistCustomer(customerId, reason, blacklistedBy, expiryDate);
    }
    
    public void removeFromBlacklist(int customerId, String removedBy) throws SQLException {
        blacklistedCustomerDAO.removeFromBlacklist(customerId, removedBy);
    }
    
    public boolean isCustomerBlacklisted(int customerId) throws SQLException {
        return blacklistedCustomerDAO.isCustomerBlacklisted(customerId);
    }
    
    public List<BlacklistedCustomer> getAllBlacklistedCustomers() throws SQLException {
        return blacklistedCustomerDAO.findAllBlacklistedCustomers();
    }
    
    public List<BlacklistedCustomer> getActiveBlacklistedCustomers() throws SQLException {
        return blacklistedCustomerDAO.findActiveBlacklistedCustomers();
    }
    
    public List<BlacklistedCustomer> searchBlacklistedCustomers(String searchTerm) throws SQLException {
        return blacklistedCustomerDAO.searchBlacklistedCustomers(searchTerm);
    }
    
    public int getActiveBlacklistCount() throws SQLException {
        return blacklistedCustomerDAO.getActiveBlacklistCount();
    }
    
    public List<String> getBlacklistReasonStatistics() throws SQLException {
        return blacklistedCustomerDAO.getBlacklistReasonStatistics();
    }
    
    // ==================== INVOICE MANAGEMENT ====================
    
    public Invoice generateInvoice(long bookingId, double taxRate, String createdBy) throws SQLException {
        return invoiceDAO.generateInvoice(bookingId, taxRate, createdBy);
    }
    
    public Invoice getInvoice(long invoiceId) throws SQLException {
        return invoiceDAO.findById(invoiceId);
    }
    
    public Invoice getInvoiceByNumber(String invoiceNumber) throws SQLException {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber);
    }
    
    public List<Invoice> getCustomerInvoices(int customerId) throws SQLException {
        return invoiceDAO.findByCustomerId(customerId);
    }
    
    public List<Invoice> getBookingInvoices(long bookingId) throws SQLException {
        return invoiceDAO.findByBookingId(bookingId);
    }
    
    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceDAO.findAll();
    }
    
    public List<Invoice> getPendingInvoices() throws SQLException {
        return invoiceDAO.findPendingInvoices();
    }
    
    public List<Invoice> getOverdueInvoices() throws SQLException {
        return invoiceDAO.findOverdueInvoices();
    }
    
    public void updateInvoicePaymentStatus(long invoiceId, Invoice.PaymentStatus paymentStatus, 
                                         Date paymentDate, String paymentMethod) throws SQLException {
        invoiceDAO.updatePaymentStatus(invoiceId, paymentStatus, paymentDate, paymentMethod);
    }
    
    public double getTotalRevenue() throws SQLException {
        return invoiceDAO.getTotalRevenue();
    }
    
    public double getPendingPaymentAmount() throws SQLException {
        return invoiceDAO.getPendingPaymentAmount();
    }
    
    // ==================== ENHANCED VIP MEMBER MANAGEMENT ====================
    
    public List<VIPMember> getAllVIPMembers() throws SQLException {
        return vipMemberDAO.findAll();
    }
    
    public VIPMember getVIPMember(int vipId) throws SQLException {
        return vipMemberDAO.findById(vipId);
    }
    
    public VIPMember getVIPMemberByCustomerId(int customerId) throws SQLException {
        return vipMemberDAO.findByCustomerId(customerId);
    }
    
    public void createVIPMember(VIPMember vipMember) throws SQLException {
        vipMemberDAO.save(vipMember);
    }
    
    public void updateVIPMember(VIPMember vipMember) throws SQLException {
        vipMemberDAO.update(vipMember);
    }
    
    public void promoteTopCustomersToVIP(String promotedBy) throws SQLException {
        vipMemberDAO.promoteTopCustomersToVIP(promotedBy);
    }
    
    // ==================== ENHANCED BUSINESS LOGIC METHODS ====================
    
    public double calculateBookingTotal(Booking booking) throws SQLException {
        // Get room rate
        Room room = roomDAO.findById(booking.getRoomId());
        if (room == null) {
            throw new SQLException("Room not found");
        }
        
        // Calculate number of nights
        long diffInMillies = booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime();
        int nights = (int) (diffInMillies / (24 * 60 * 60 * 1000));
        
        double baseAmount = room.getRoomType().getBaseRate() * nights;
        
        // Apply VIP discount if applicable
        VIPMember vipMember = vipMemberDAO.findByCustomerId(booking.getCustomerId());
        if (vipMember != null && vipMember.isActive()) {
            double discount = baseAmount * (vipMember.getDiscountPercentage() / 100);
            baseAmount -= discount;
            booking.setDiscountApplied(discount);
        }
        
        booking.setTotalAmount(baseAmount);
        return baseAmount;
    }
    
    public void processCheckIn(long bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found");
        }
        
        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new SQLException("Booking is not in confirmed status");
        }
        
        // Check if customer is blacklisted
        if (blacklistedCustomerDAO.isCustomerBlacklisted(booking.getCustomerId())) {
            throw new SQLException("Cannot check in blacklisted customer");
        }
        
        booking.setBookingStatus("CHECKED_IN");
        bookingDAO.update(booking);
        
        // Update room status
        roomDAO.updateRoomStatus(booking.getRoomId(), "OCCUPIED");
    }
    
    public void processCheckOut(long bookingId, Date actualCheckoutDate) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found");
        }
        
        if (!"CHECKED_IN".equals(booking.getBookingStatus())) {
            throw new SQLException("Booking is not checked in");
        }
        
        // Calculate extra charges for late checkout
        if (actualCheckoutDate.after(booking.getCheckOutDate())) {
            long hoursLate = (actualCheckoutDate.getTime() - booking.getCheckOutDate().getTime()) / (60 * 60 * 1000);
            double extraCharge = Math.max(1, hoursLate) * 25.0; // $25 per hour
            booking.setExtraCharges(booking.getExtraCharges() + extraCharge);
            booking.setTotalAmount(booking.getTotalAmount() + extraCharge);
        }
        
        booking.setBookingStatus("CHECKED_OUT");
        bookingDAO.update(booking);
        
        // Update room status to maintenance (needs cleaning)
        roomDAO.updateRoomStatus(booking.getRoomId(), "MAINTENANCE");
        
        // Update customer total spent
        Customer customer = customerDAO.findById(booking.getCustomerId());
        if (customer != null) {
            customer.setTotalSpent(customer.getTotalSpent() + booking.getTotalAmount());
            customerDAO.update(customer);
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    public boolean isServiceAvailableForRoom(int roomId, int serviceId) throws SQLException {
        return roomServiceDAO.isServiceAvailableForRoom(roomId, serviceId);
    }
    
    public List<String> getServiceCategories() throws SQLException {
        return roomServiceDAO.getServiceCategories();
    }
    
    // ==================== COMPREHENSIVE REPORTING ====================
    
    public Object[] getSystemStatistics() throws SQLException {
        List<Customer> allCustomers = customerDAO.findAll();
        List<VIPMember> vipMembers = vipMemberDAO.findAll();
        List<Room> allRooms = roomDAO.findAll();
        List<Room> availableRooms = roomDAO.findAvailableRooms();
        List<Booking> currentReservations = getCurrentReservations();
        List<Booking> expiredReservations = getExpiredReservations();
        int activeBlacklist = getActiveBlacklistCount();
        double totalRevenue = getTotalRevenue();
        double pendingPayments = getPendingPaymentAmount();
        
        return new Object[]{
            allCustomers.size(),           // Total customers
            vipMembers.size(),            // Total VIP members
            allRooms.size(),              // Total rooms
            availableRooms.size(),        // Available rooms
            currentReservations.size(),   // Current reservations
            expiredReservations.size(),   // Expired reservations
            activeBlacklist,              // Active blacklisted customers
            totalRevenue,                 // Total revenue
            pendingPayments               // Pending payments
        };
    }
    
    public String generateSystemReport() throws SQLException {
        Object[] stats = getSystemStatistics();
        StringBuilder report = new StringBuilder();
        
        report.append("=== HOTEL MANAGEMENT SYSTEM REPORT ===\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        report.append("CUSTOMER STATISTICS:\n");
        report.append("- Total Customers: ").append(stats[0]).append("\n");
        report.append("- VIP Members: ").append(stats[1]).append("\n");
        report.append("- Blacklisted Customers: ").append(stats[6]).append("\n\n");
        
        report.append("ROOM STATISTICS:\n");
        report.append("- Total Rooms: ").append(stats[2]).append("\n");
        report.append("- Available Rooms: ").append(stats[3]).append("\n");
        report.append("- Occupancy Rate: ").append(
            String.format("%.1f%%", 
                ((Integer)stats[2] - (Integer)stats[3]) * 100.0 / (Integer)stats[2])
        ).append("\n\n");
        
        report.append("BOOKING STATISTICS:\n");
        report.append("- Current Reservations: ").append(stats[4]).append("\n");
        report.append("- Expired Reservations: ").append(stats[5]).append("\n\n");
        
        report.append("FINANCIAL STATISTICS:\n");
        report.append("- Total Revenue: $").append(String.format("%.2f", stats[7])).append("\n");
        report.append("- Pending Payments: $").append(String.format("%.2f", stats[8])).append("\n");
        
        return report.toString();
    }
}

