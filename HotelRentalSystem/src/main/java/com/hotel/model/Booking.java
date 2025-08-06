package com.hotel.model;

import java.util.Date;

/**
 * Booking model class representing hotel room bookings
 */
public class Booking {
    public enum BookingStatus {
        CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW
    }
    
    public enum PaymentStatus {
        PENDING, PAID, CANCELLED, REFUNDED
    }
    
    private int bookingId;
    private int customerId;
    private Customer customer; // For joined queries
    private int roomId;
    private Room room; // For joined queries
    private Date checkInDate;
    private Date checkOutDate;
    private Date actualCheckIn;
    private Date actualCheckOut;
    private Date bookingDate;
    private double totalAmount;
    private double discountApplied;
    private double extraCharges;
    private PaymentStatus paymentStatus;
    private BookingStatus bookingStatus;
    private String specialRequests;
    private String createdBy;
    
    // Default constructor
    public Booking() {
        this.bookingDate = new Date();
        this.paymentStatus = PaymentStatus.PENDING;
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.discountApplied = 0.0;
        this.extraCharges = 0.0;
    }
    
    // Constructor with required fields
    public Booking(int customerId, int roomId, Date checkInDate, Date checkOutDate, double totalAmount) {
        this();
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
    }
    
    // Full constructor
    public Booking(int bookingId, int customerId, int roomId, Date checkInDate, Date checkOutDate,
                  Date actualCheckIn, Date actualCheckOut, Date bookingDate, double totalAmount,
                  double discountApplied, double extraCharges, PaymentStatus paymentStatus,
                  BookingStatus bookingStatus, String specialRequests, String createdBy) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.actualCheckIn = actualCheckIn;
        this.actualCheckOut = actualCheckOut;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.discountApplied = discountApplied;
        this.extraCharges = extraCharges;
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
        this.specialRequests = specialRequests;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    
    public Date getCheckInDate() { return checkInDate; }
    public void setCheckInDate(Date checkInDate) { this.checkInDate = checkInDate; }
    
    public Date getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(Date checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public Date getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(Date actualCheckIn) { this.actualCheckIn = actualCheckIn; }
    
    public Date getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(Date actualCheckOut) { this.actualCheckOut = actualCheckOut; }
    
    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public double getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(double discountApplied) { this.discountApplied = discountApplied; }
    
    public double getExtraCharges() { return extraCharges; }
    public void setExtraCharges(double extraCharges) { this.extraCharges = extraCharges; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    // Utility methods
    public int getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
            return (int) (diffInMillies / (1000 * 60 * 60 * 24));
        }
        return 0;
    }
    
    public double getFinalAmount() {
        return totalAmount - discountApplied + extraCharges;
    }
    
    public boolean isActive() {
        return bookingStatus == BookingStatus.CONFIRMED || bookingStatus == BookingStatus.CHECKED_IN;
    }
    
    public boolean isCompleted() {
        return bookingStatus == BookingStatus.CHECKED_OUT;
    }
    
    public boolean isCancelled() {
        return bookingStatus == BookingStatus.CANCELLED;
    }
    
    public String getPaymentStatusString() {
        return paymentStatus.toString();
    }
    
    public void setPaymentStatusFromString(String statusStr) {
        try {
            this.paymentStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }
    
    public String getBookingStatusString() {
        return bookingStatus.toString();
    }
    
    public void setBookingStatusFromString(String statusStr) {
        try {
            this.bookingStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.bookingStatus = BookingStatus.CONFIRMED;
        }
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", roomId=" + roomId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalAmount=" + totalAmount +
                ", bookingStatus=" + bookingStatus +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Booking booking = (Booking) obj;
        return bookingId == booking.bookingId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(bookingId);
    }
}

