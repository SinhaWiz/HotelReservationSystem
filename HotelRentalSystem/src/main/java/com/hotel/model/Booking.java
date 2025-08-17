package com.hotel.model;

import java.util.Date;

public class Booking {
    public enum BookingStatus {
        CONFIRMED,
        CHECKED_IN,
        CHECKED_OUT,
        CANCELLED,
        NO_SHOW
    }

    private long bookingId;
    private int customerId;
    private int roomId;
    private Date checkInDate;
    private Date checkOutDate;
    private String bookingStatus;
    private double totalAmount;
    private double discountApplied;
    private double extraCharges;
    private Date createdDate;
    private String createdBy;
    private Date actualCheckIn;
    private Date actualCheckOut;
    private String specialRequests;
    private String paymentStatus;
    private Customer customer;
    private Room room;
    private Date bookingDate;

    public Booking() {
        this.customer = new Customer();
        this.room = new Room();
        this.bookingDate = new Date();
        this.bookingStatus = "CONFIRMED";
        this.paymentStatus = "PENDING";
    }

    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public Date getCheckInDate() { return checkInDate; }
    public void setCheckInDate(Date checkInDate) { this.checkInDate = checkInDate; }

    public Date getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(Date checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(double discountApplied) { this.discountApplied = discountApplied; }

    public double getExtraCharges() { return extraCharges; }
    public void setExtraCharges(double extraCharges) { this.extraCharges = extraCharges; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(Date actualCheckIn) { this.actualCheckIn = actualCheckIn; }

    public Date getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(Date actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingStatusString() {
        return bookingStatus != null ? bookingStatus : "";
    }

    public String getPaymentStatusString() {
        return paymentStatus != null ? paymentStatus : "PENDING";
    }

    public int getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
        return (int) (diffInMillies / (1000 * 60 * 60 * 24));
    }

    public double getFinalAmount() {
        return totalAmount + extraCharges - discountApplied;
    }

    public void setBookingStatusFromString(String status) {
        this.bookingStatus = status;
    }

    public void setPaymentStatusFromString(String status) {
        this.paymentStatus = status;
    }

    public BookingStatus getStatus() {
        return BookingStatus.valueOf(bookingStatus);
    }

    public void setStatus(BookingStatus status) {
        this.bookingStatus = status.name();
    }
}
