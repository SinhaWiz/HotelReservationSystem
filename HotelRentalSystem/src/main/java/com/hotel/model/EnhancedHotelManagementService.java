package com.hotel.model;

import com.hotel.dao.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Enhanced service class for hotel management operations with new features
 * Includes room services, blacklist management, invoice generation, and more
 */
public class EnhancedHotelManagementService {
    private final CustomerDAO customerDAO;
    private final BookingDAO bookingDAO;
    private final RoomDAO roomDAO;
    private final VIPMemberDAO vipMemberDAO;
    private final RoomServiceDAO roomServiceDAO;
    private final ServiceUsageDAO serviceUsageDAO;
    private final BlacklistedCustomerDAO blacklistedCustomerDAO;
    private final InvoiceDAO invoiceDAO;

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
    
    public boolean updateCustomer(Customer customer) throws SQLException {
        customerDAO.update(customer);
        return false;
    }
    
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        return customerDAO.searchByName(searchTerm);
    }
    
    // ==================== ENHANCED BOOKING MANAGEMENT ====================
    
    public Booking createBooking(Booking booking) throws SQLException {
        // Validate customer is not blacklisted
        if (blacklistedCustomerDAO.isCustomerBlacklisted(booking.getCustomerId())) {
            throw new SQLException("Customer is blacklisted");
        }

        // Check room availability
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
            throw new SQLException("Room is not available for the selected dates");
        }

        return bookingDAO.create(booking);
    }
    
    public Booking getBooking(int bookingId) throws SQLException {
        return bookingDAO.findById(bookingId);
    }
    
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAll();
    }
    
    public List<Booking> getCustomerBookings(int customerId) throws SQLException {
        return bookingDAO.findByCustomerId(customerId);
    }
    
    public List<Booking> getCurrentBookings() throws SQLException {
        return bookingDAO.getCurrentBookings();
    }
    
    public List<Booking> getExpiredBookings() throws SQLException {
        return bookingDAO.getExpiredBookings();
    }
    
    public void updateBooking(Booking booking) throws SQLException {
        bookingDAO.update(booking);
    }
    
    public boolean cancelBooking(int bookingId) throws SQLException {
        bookingDAO.cancel(bookingId);
        return false;
    }
    
    public List<Booking> getBookingsByDateRange(Date startDate, Date endDate) throws SQLException {
        return bookingDAO.getBookingsByDateRange(startDate, endDate);
    }

    public boolean checkInCustomer(int bookingId) throws SQLException {
        return bookingDAO.checkInCustomer(bookingId);
    }

    public boolean checkOutCustomer(int bookingId) throws SQLException {
        return bookingDAO.checkOutCustomer(bookingId);
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        return bookingDAO.getBookingById(bookingId);
    }

    public List<Booking> getCurrentReservations() throws SQLException {
        return bookingDAO.getCurrentBookings();
    }

    // ==================== ENHANCED ROOM MANAGEMENT ====================
    
    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }
    
    public List<Room> getAvailableRooms() throws SQLException {
        return roomDAO.findAvailable();
    }
    
    public List<Room> getAvailableRooms(Date checkIn, Date checkOut) throws SQLException {
        return roomDAO.findAvailableForDates(checkIn, checkOut);
    }
    
    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) throws SQLException {
        return roomDAO.checkAvailability(roomId, checkIn, checkOut);
    }
    
    public void updateRoomStatus(int roomId, String status) throws SQLException {
        Room room = roomDAO.findById(roomId);
        if (room != null) {
            room.setStatusFromString(status);
            roomDAO.update(room);
        }
    }

    // Room Management Methods
    public List<Room> getRoomsByStatus(Room.RoomStatus status) throws SQLException {
        List<Room> allRooms = roomDAO.findAll();
        return allRooms.stream()
                .filter(room -> room.getStatus() == status)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean updateRoomStatus(int roomId, Room.RoomStatus newStatus) throws SQLException {
        Room room = roomDAO.findById(roomId);
        if (room != null) {
            room.setStatus(newStatus);
            roomDAO.update(room);
            return true;
        }
        return false;
    }

    public List<RoomType> getAllRoomTypes() throws SQLException {
        return roomDAO.getAllRoomTypes();
    }

    public Map<RoomType, Double> getRoomUtilizationStats(Date startDate, Date endDate) throws SQLException {
        List<Room> allRooms = roomDAO.findAll();
        Map<RoomType, Integer> totalRooms = new java.util.HashMap<>();
        Map<RoomType, Double> stats = new java.util.HashMap<>();

        for (Room room : allRooms) {
            RoomType roomType = room.getRoomType();
            totalRooms.merge(roomType, 1, Integer::sum);

            List<Booking> bookings = bookingDAO.findByRoomIdAndDates(room.getRoomId(), startDate, endDate);
            double occupiedDays = 0;

            for (Booking booking : bookings) {
                if (booking.getCheckOutDate() != null && booking.getCheckInDate() != null) {
                    long diffInMillies = booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime();
                    occupiedDays += diffInMillies / (1000.0 * 60 * 60 * 24);
                }
            }

            stats.merge(roomType, occupiedDays, Double::sum);
        }

        for (Map.Entry<RoomType, Double> entry : stats.entrySet()) {
            RoomType roomType = entry.getKey();
            double occupiedDays = entry.getValue();
            int total = totalRooms.get(roomType);

            // Calculate utilization percentage
            double totalPossibleDays = total * ((endDate.getTime() - startDate.getTime()) / (1000.0 * 60 * 60 * 24));
            double utilization = (occupiedDays / totalPossibleDays) * 100;

            stats.put(roomType, utilization);
        }

        return stats;
    }

    // ==================== ROOM SERVICE MANAGEMENT ====================
    
    public List<RoomService> getAllRoomServices() throws SQLException {
        return roomServiceDAO.findAll();
    }
    
    public List<RoomService> getActiveRoomServices() throws SQLException {
        return roomServiceDAO.findActive();
    }
    
    public List<RoomService> getServicesForRoomType(int roomTypeId) throws SQLException {
        return roomServiceDAO.findByRoomType(roomTypeId);
    }
    
    public RoomService getRoomService(int serviceId) throws SQLException {
        return roomServiceDAO.findById(serviceId);
    }
    
    public void createRoomService(RoomService service) throws SQLException {
        roomServiceDAO.create(service);
    }
    
    public void updateRoomService(RoomService service) throws SQLException {
        roomServiceDAO.update(service);
    }
    
    public List<RoomService> searchRoomServices(String searchTerm) throws SQLException {
        return roomServiceDAO.search(searchTerm);
    }
    
    public List<RoomService> getServicesByCategory(RoomService.ServiceCategory category) throws SQLException {
        return roomServiceDAO.findByCategory(category);
    }
    
    // ==================== SERVICE USAGE MANAGEMENT ====================
    
    public long addServiceUsage(long bookingId, int customerId, int serviceId, int quantity) throws SQLException {
        return serviceUsageDAO.create(bookingId, customerId, serviceId, quantity);
    }
    
    public List<ServiceUsage> getCustomerServiceUsage(int customerId) throws SQLException {
        return serviceUsageDAO.findByCustomerId(customerId);
    }
    
    public List<ServiceUsage> getBookingServiceUsage(long bookingId) throws SQLException {
        return serviceUsageDAO.findByBookingId(bookingId);
    }
    
    // ==================== BLACKLIST MANAGEMENT ====================
    
    public void blacklistCustomer(int customerId, String reason, String blacklistedBy, Date expiryDate) throws SQLException {
        java.sql.Date sqlDate = expiryDate != null ? new java.sql.Date(expiryDate.getTime()) : null;
        blacklistedCustomerDAO.create(customerId, reason, blacklistedBy, sqlDate);
    }
    
    public void removeFromBlacklist(int customerId, String removedBy) throws SQLException {
        blacklistedCustomerDAO.remove(customerId, removedBy);
    }
    
    public boolean isCustomerBlacklisted(int customerId) throws SQLException {
        return blacklistedCustomerDAO.isCustomerBlacklisted(customerId);
    }
    
    public List<BlacklistedCustomer> getAllBlacklistedCustomers() throws SQLException {
        return blacklistedCustomerDAO.findAll();
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
        return invoiceDAO.generate(bookingId, taxRate, createdBy);
    }
    
    public Invoice getInvoice(long invoiceId) throws SQLException {
        return invoiceDAO.findById(invoiceId);
    }
    
    public Invoice getInvoiceByNumber(String invoiceNumber) throws SQLException {
        return invoiceDAO.findByNumber(invoiceNumber);
    }
    
    public List<Invoice> getCustomerInvoices(int customerId) throws SQLException {
        return invoiceDAO.findByCustomerId(customerId);
    }
    
    public List<Invoice> getPendingInvoices() throws SQLException {
        return invoiceDAO.findPending();
    }
    
    public double getTotalRevenue() throws SQLException {
        return invoiceDAO.getTotalRevenue();
    }

    public double getPendingPaymentAmount() throws SQLException {
        return invoiceDAO.getPendingPaymentAmount();
    }

    public void updateInvoicePaymentStatus(long invoiceId, Invoice.PaymentStatus status,
                                         Date paymentDate, String paymentMethod) throws SQLException {
        invoiceDAO.updatePayment(invoiceId, status, new java.sql.Date(paymentDate.getTime()), paymentMethod);
    }
    
    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceDAO.findAll();
    }

    public List<Invoice> getBookingInvoices(int bookingId) throws SQLException {
        return invoiceDAO.findByBookingId(bookingId);
    }

    public List<Invoice> getOverdueInvoices() throws SQLException {
        return invoiceDAO.findOverdueInvoices();
    }

    public double getRoomOccupancyRate(Date startDate, Date endDate) throws SQLException {
        List<Room> allRooms = roomDAO.findAll();
        List<Booking> bookings = bookingDAO.findByDateRange(startDate, endDate);

        int totalRoomDays = 0;
        int occupiedRoomDays = 0;

        // Calculate total available room days
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        totalRoomDays = allRooms.size() * (int)daysBetween;

        // Calculate occupied room days
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.CHECKED_OUT ||
                booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                long bookingDays = (booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime())
                                 / (1000 * 60 * 60 * 24);
                occupiedRoomDays += bookingDays;
            }
        }

        return totalRoomDays > 0 ? ((double)occupiedRoomDays / totalRoomDays) * 100 : 0;
    }

    public VIPMember getCustomerVIPStatus(int customerId) throws SQLException {
        return vipMemberDAO.findByCustomerId(customerId);
    }

    public List<Booking> getCustomerBookingHistory(int customerId) throws SQLException {
        return bookingDAO.findByCustomerId(customerId);
    }

    public List<Customer> getVIPEligibleCustomers() throws SQLException {
        return customerDAO.findVIPEligible();
    }

    public List<ServiceUsage> getCustomerServiceSummary(int customerId) throws SQLException {
        return serviceUsageDAO.findByCustomerId(customerId);
    }

    public double calculateCustomerServiceTotal(int customerId, Date date) throws SQLException {
        List<ServiceUsage> usages = date == null ?
            serviceUsageDAO.findByCustomerId(customerId) :
            serviceUsageDAO.findByCustomerIdAndDate(customerId, date);

        return usages.stream()
                    .mapToDouble(usage -> usage.getQuantity() * usage.getService().getPrice())
                    .sum();
    }

    public List<ServiceUsage> getMostPopularServices(int limit) throws SQLException {
        return serviceUsageDAO.findMostPopular(limit);
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
    
    // VIP Member Management Methods
    public List<VIPMember> getVIPMembersDetailed(String membershipLevel) throws SQLException {
        if (membershipLevel == null || membershipLevel.equals("All Levels")) {
            return vipMemberDAO.findAllWithDetails();
        }
        return vipMemberDAO.findByLevelWithDetails(VIPMember.MembershipLevel.valueOf(membershipLevel));
    }

    public boolean deactivateVIPMember(int vipId) throws SQLException {
        VIPMember member = vipMemberDAO.findById(vipId);
        if (member != null) {
            member.setActive(false);
            vipMemberDAO.update(member);
            return true;
        }
        return false;
    }

    public void processVIPRenewals() throws SQLException {
        List<VIPMember> members = vipMemberDAO.findAll();
        for (VIPMember member : members) {
            // Check if membership has expired
            if (member.getMembershipEndDate() != null &&
                member.getMembershipEndDate().before(new Date())) {
                member.setActive(false);
                vipMemberDAO.update(member);
            }
        }
    }
    
    public String checkVIPEligibility(int customerId) throws SQLException {
        return vipMemberDAO.checkVIPEligibility(customerId);
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
        List<Room> availableRooms = roomDAO.findAvailable();
        List<Booking> currentReservations = getCurrentBookings();
        List<Booking> expiredReservations = getExpiredBookings();
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

    private void validateBookingDates(Date checkIn, Date checkOut) throws SQLException {
        if (checkIn == null || checkOut == null) {
            throw new SQLException("Check-in and check-out dates are required");
        }
        if (checkIn.after(checkOut)) {
            throw new SQLException("Check-in date must be before check-out date");
        }
        if (checkIn.before(new Date())) {
            throw new SQLException("Check-in date cannot be in the past");
        }
    }
}

