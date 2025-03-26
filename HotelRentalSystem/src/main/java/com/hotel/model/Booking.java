package com.hotel.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class representing a Booking in the system
 */
public class Booking {
    private int bookingId;
    private int propertyId;
    private int renterId;
    private Date checkInDate;
    private Date checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus;
    
    public enum BookingStatus {
        CONFIRMED("confirmed"),
        PENDING("pending"),
        CANCELLED("cancelled");
        
        private final String value;
        
        BookingStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static BookingStatus fromString(String text) {
            for (BookingStatus status : BookingStatus.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    // Constructors
    public Booking() {
    }
    
    public Booking(int bookingId, int propertyId, int renterId, Date checkInDate, 
                  Date checkOutDate, BigDecimal totalPrice, BookingStatus bookingStatus) {
        this.bookingId = bookingId;
        this.propertyId = propertyId;
        this.renterId = renterId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.bookingStatus = bookingStatus;
    }
    
    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    
    public int getRenterId() {
        return renterId;
    }
    
    public void setRenterId(int renterId) {
        this.renterId = renterId;
    }
    
    public Date getCheckInDate() {
        return checkInDate;
    }
    
    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }
    
    public Date getCheckOutDate() {
        return checkOutDate;
    }
    
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }
    
    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", propertyId=" + propertyId +
                ", renterId=" + renterId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalPrice=" + totalPrice +
                ", bookingStatus=" + bookingStatus +
                '}';
    }
} 