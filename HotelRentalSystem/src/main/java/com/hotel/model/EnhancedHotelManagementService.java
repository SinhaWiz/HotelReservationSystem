package com.hotel.model;

import com.hotel.dao.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Enhanced service class for hotel management operations with new features
 * Includes room services, invoice generation, and more
 */
public class EnhancedHotelManagementService {
    private final CustomerDAO customerDAO;
    private final BookingDAO bookingDAO;
    private final RoomDAO roomDAO;
    private final VIPMemberDAO vipMemberDAO;
    private final RoomServiceDAO roomServiceDAO;
    private final ServiceUsageDAO serviceUsageDAO;
    private final InvoiceDAO invoiceDAO;

    public EnhancedHotelManagementService() {
        this.customerDAO = new CustomerDAO();
        this.bookingDAO = new BookingDAO();
        this.roomDAO = new RoomDAO();
        this.vipMemberDAO = new VIPMemberDAO();
        this.roomServiceDAO = new RoomServiceDAO();
        this.serviceUsageDAO = new ServiceUsageDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    // ==================== ENHANCED CUSTOMER MANAGEMENT ====================
    
    public Customer createCustomer(Customer customer) throws SQLException {
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
    
    public Customer findCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public Customer registerCustomer(String firstName, String lastName, String email, String phone, Date dateOfBirth, String address) throws SQLException {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setDateOfBirth(dateOfBirth);
        customer.setAddress(address);
        customer.setRegistrationDate(new Date());
        customer.setTotalSpent(0.0);
        customer.setLoyaltyPoints(0);

        return createCustomer(customer);
    }

    // ==================== ENHANCED BOOKING MANAGEMENT ====================
    
    public Booking createBooking(Booking booking) throws SQLException {
        // Check room availability
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
            throw new SQLException("Room is not available for the selected dates");
        }

        // Calculate total amount based on room price and nights
        Room room = roomDAO.findById(booking.getRoomId());
        if (room == null) {
            throw new SQLException("Room not found with ID: " + booking.getRoomId());
        }

        // Calculate number of nights
        long diffInMillies = booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime();
        int numberOfNights = (int) (diffInMillies / (1000 * 60 * 60 * 24));
        if (numberOfNights <= 0) {
            numberOfNights = 1; // Minimum 1 night
        }

        // Calculate total amount
        double baseAmount = room.getBasePrice() * numberOfNights;

        // Check for VIP discount
        double discount = 0.0;
        try {
            VIPMember vipMember = vipMemberDAO.findByCustomerId(booking.getCustomerId());
            if (vipMember != null && vipMember.isActive()) {
                discount = baseAmount * (vipMember.getDiscountPercentage() / 100.0);
            }
        } catch (SQLException e) {
            // No VIP membership, continue without discount
        }

        booking.setTotalAmount(baseAmount - discount);
        booking.setDiscountApplied(discount);
        booking.setCreatedBy("SYSTEM");
        booking.setBookingDate(new Date());

        // Load customer information
        Customer customer = customerDAO.findById(booking.getCustomerId());
        booking.setCustomer(customer);

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
    
    // ==================== VIP MEMBER MANAGEMENT ====================

    public VIPMember createVIPMember(VIPMember vipMember) throws SQLException {
        int vipId = vipMemberDAO.createVIPMember(vipMember);
        vipMember.setVipId(vipId);
        return vipMember;
    }

    public VIPMember getVIPMemberByCustomerId(int customerId) throws SQLException {
        return vipMemberDAO.findByCustomerId(customerId);
    }

    public List<VIPMember> getAllVIPMembers() throws SQLException {
        return vipMemberDAO.findAll();
    }

    public List<VIPMember> getActiveVIPMembers() throws SQLException {
        return vipMemberDAO.findAllActive();
    }
    
    public void updateVIPMember(VIPMember vipMember) throws SQLException {
        vipMemberDAO.updateVIPMember(vipMember);
    }
    
    public boolean deactivateVIPMember(int vipId) throws SQLException {
        return vipMemberDAO.deactivateVIPMember(vipId);
    }

    public String checkVIPEligibility(int customerId) throws SQLException {
        return vipMemberDAO.checkVIPEligibility(customerId);
    }

    public List<VIPMember> getVIPMembersByLevel(VIPMember.MembershipLevel level) throws SQLException {
        if (level == null) {
            return vipMemberDAO.findAllWithDetails();
        }
        return vipMemberDAO.findByMembershipLevelWithDetails(level);
    }

    public void processVIPRenewals() throws SQLException {
        vipMemberDAO.processVIPRenewals();
    }

    public void promoteTopCustomersToVIP(String promotedBy) throws SQLException {
        vipMemberDAO.promoteTopCustomersToVIP(promotedBy);
    }
    
    // ==================== INVOICE MANAGEMENT ====================
    
    public Invoice createInvoice(int customerId, long bookingId) throws SQLException {
        return invoiceDAO.createInvoice(customerId, bookingId);
    }
    
    public Invoice getInvoice(int invoiceId) throws SQLException {
        return invoiceDAO.findById(invoiceId);
    }
    
    public List<Invoice> getCustomerInvoices(int customerId) throws SQLException {
        return invoiceDAO.findByCustomerId(customerId);
    }
    
    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceDAO.findAll();
    }

    public void updateInvoicePaymentStatus(int invoiceId, Invoice.PaymentStatus status) throws SQLException {
        invoiceDAO.updatePaymentStatus(invoiceId, status);
    }

    public List<Invoice> getUnpaidInvoices() throws SQLException {
        return invoiceDAO.findUnpaidInvoices();
    }

    public List<Invoice> getInvoicesByDateRange(Date startDate, Date endDate) throws SQLException {
        return invoiceDAO.findByDateRange(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
    }

    // ==================== REPORTING AND ANALYTICS ====================

    public double getTotalRevenue(Date startDate, Date endDate) throws SQLException {
        List<Invoice> invoices = invoiceDAO.findByDateRange(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
        return invoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    public int getTotalCustomersCount() throws SQLException {
        return customerDAO.findAll().size();
    }

    public int getTotalVIPMembersCount() throws SQLException {
        return vipMemberDAO.findAllActive().size();
    }

    public int getTotalRoomsCount() throws SQLException {
        return roomDAO.findAll().size();
    }

    public int getAvailableRoomsCount() throws SQLException {
        return roomDAO.findAvailable().size();
    }
    
    public int getOccupiedRoomsCount() throws SQLException {
        List<Room> allRooms = roomDAO.findAll();
        return (int) allRooms.stream()
                .filter(room -> room.getStatus() == Room.RoomStatus.OCCUPIED)
                .count();
    }
    
    public int getCurrentReservationsCount() throws SQLException {
        return bookingDAO.getCurrentBookings().size();
    }
    
    public double getOccupancyRate() throws SQLException {
        int totalRooms = getTotalRoomsCount();
        int occupiedRooms = getOccupiedRoomsCount();

        if (totalRooms == 0) return 0.0;
        return (double) occupiedRooms / totalRooms * 100.0;
    }
    
    // ==================== UTILITY METHODS ====================

    public Customer findCustomerByID(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        try {
            customerDAO.delete(customerId);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Room getRoomById(int roomId) throws SQLException {
        return roomDAO.findById(roomId);
    }

    public boolean updateRoom(Room room) throws SQLException {
        try {
            roomDAO.update(room);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public List<Booking> searchBookings(String searchTerm) throws SQLException {
        // Implementation would depend on BookingDAO having a search method
        return bookingDAO.getAll(); // Placeholder - would need to implement search in BookingDAO
    }
    
    public void generateReports() throws SQLException {
        // Placeholder for report generation functionality
        // Could generate various reports based on the data
    }

    // ==================== ADDITIONAL MISSING METHODS ====================

    // Invoice Management Methods
    public List<Invoice> getBookingInvoices(int bookingId) throws SQLException {
        return invoiceDAO.findByBookingId(bookingId);
    }

    public Invoice generateInvoice(int bookingId, double taxRate, String createdBy) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new SQLException("Booking not found");
        }

        Invoice invoice = new Invoice();
        invoice.setBookingId(booking.getBookingId());
        invoice.setCustomerId(booking.getCustomerId());
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setInvoiceDate(new Date());

        // Calculate amounts
        double subtotal = booking.getTotalAmount();
        double taxAmount = subtotal * (taxRate / 100);
        double totalAmount = subtotal + taxAmount;

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);

        return invoiceDAO.create(invoice);
    }

    public void updateInvoicePaymentStatus(int invoiceId, Invoice.PaymentStatus paymentStatus,
                                         Date paymentDate, String paymentMethod) throws SQLException {
        invoiceDAO.updatePaymentStatus((long)invoiceId, paymentStatus,
            paymentDate != null ? new java.sql.Date(paymentDate.getTime()) : null, paymentMethod);
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) throws SQLException {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber);
    }

    public List<Invoice> getPendingInvoices() throws SQLException {
        return invoiceDAO.findByPaymentStatus(Invoice.PaymentStatus.PENDING);
    }

    public List<Invoice> getOverdueInvoices() throws SQLException {
        return invoiceDAO.findOverdueInvoices();
    }

    public double getPendingPaymentAmount() throws SQLException {
        List<Invoice> pendingInvoices = getPendingInvoices();
        return pendingInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    // VIP Member Methods
    public VIPMember getCustomerVIPStatus(int customerId) throws SQLException {
        return vipMemberDAO.findByCustomerId(customerId);
    }

    public List<VIPMember> getVIPMembersDetailed(VIPMember.MembershipLevel level) throws SQLException {
        if (level == null) {
            return vipMemberDAO.findAllWithDetails();
        } else {
            return vipMemberDAO.findByMembershipLevelWithDetails(level);
        }
    }

    public VIPMember promoteToVIP(int customerId, VIPMember.MembershipLevel level) throws SQLException {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new SQLException("Customer not found");
        }

        VIPMember vipMember = new VIPMember();
        vipMember.setCustomerId(customerId);
        vipMember.setMembershipLevel(level);
        vipMember.setJoinDate(new java.sql.Date(System.currentTimeMillis()));
        vipMember.setDiscountPercentage(VIPMember.getDefaultDiscountForLevel(level));
        vipMember.setBenefits(VIPMember.getDefaultBenefitsForLevel(level));
        vipMember.setActive(true);

        int vipId = vipMemberDAO.createVIPMember(vipMember);
        vipMember.setVipId(vipId);

        return vipMember;
    }

    // Customer Methods
    public List<Booking> getCustomerBookingHistory(int customerId) throws SQLException {
        return bookingDAO.findByCustomerId(customerId);
    }

    public List<Customer> getVIPEligibleCustomers() throws SQLException {
        return customerDAO.findVIPEligibleCustomers();
    }

    // Service Usage Methods
    public List<ServiceUsage> getCustomerServiceSummary(int customerId) throws SQLException {
        return serviceUsageDAO.getCustomerServiceSummary(customerId);
    }

    public double calculateCustomerServiceTotal(int customerId, Date fromDate) throws SQLException {
        return serviceUsageDAO.calculateCustomerServiceTotal(customerId, fromDate);
    }

    public List<ServiceUsage> getMostPopularServices(int limit) throws SQLException {
        return serviceUsageDAO.getMostPopularServices(limit);
    }

    // Room and Reports Methods
    public double getRoomOccupancyRate(Date startDate, Date endDate) throws SQLException {
        List<Room> allRooms = roomDAO.findAll();
        if (allRooms.isEmpty()) return 0.0;

        int totalRooms = allRooms.size();
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        long totalRoomDays = totalRooms * daysBetween;

        List<Booking> bookings = bookingDAO.getBookingsByDateRange(startDate, endDate);
        long occupiedRoomDays = 0;

        for (Booking booking : bookings) {
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                long bookingDays = (booking.getCheckOutDate().getTime() - booking.getCheckInDate().getTime()) / (1000 * 60 * 60 * 24);
                occupiedRoomDays += bookingDays;
            }
        }

        return totalRoomDays > 0 ? (double) occupiedRoomDays / totalRoomDays * 100.0 : 0.0;
    }

    public Object[] getSystemStatistics() throws SQLException {
        return new Object[] {
            getTotalCustomersCount(),
            getTotalVIPMembersCount(),
            getCurrentReservationsCount(),
            getAvailableRoomsCount(),
            getOccupiedRoomsCount(),
            String.format("%.1f%%", getOccupancyRate()),
            getPendingPaymentAmount(),
            getTotalRevenue(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000), new Date()) // Last 30 days
        };
    }
}
