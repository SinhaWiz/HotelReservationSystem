package com.hotel.model;

import java.util.Date;

/**
 * Model class representing customer service usage
 */
public class ServiceUsage {
    
    private long usageId;
    private long bookingId;
    private int customerId;
    private int serviceId;
    private Date usageDate;
    private int quantity;
    private double unitPrice;
    private double totalCost;
    private boolean isComplimentary;
    private String notes;
    
    // Related objects
    private Customer customer;
    private RoomService roomService;
    private Booking booking;
    
    // Constructors
    public ServiceUsage() {
        this.usageDate = new Date();
        this.quantity = 1;
        this.isComplimentary = false;
    }
    
    public ServiceUsage(long bookingId, int customerId, int serviceId, 
                       int quantity, double unitPrice) {
        this();
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalCost = quantity * unitPrice;
    }
    
    // Getters and Setters
    public long getUsageId() {
        return usageId;
    }
    
    public void setUsageId(long usageId) {
        this.usageId = usageId;
    }
    
    public long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public int getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    
    public Date getUsageDate() {
        return usageDate;
    }
    
    public void setUsageDate(Date usageDate) {
        this.usageDate = usageDate;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Recalculate total cost
        this.totalCost = quantity * unitPrice;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        // Recalculate total cost
        this.totalCost = quantity * unitPrice;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    public boolean isComplimentary() {
        return isComplimentary;
    }
    
    public void setComplimentary(boolean complimentary) {
        isComplimentary = complimentary;
        if (complimentary) {
            this.totalCost = 0.0;
        } else {
            this.totalCost = quantity * unitPrice;
        }
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerId = customer.getCustomerId();
        }
    }
    
    public RoomService getRoomService() {
        return roomService;
    }
    
    public void setRoomService(RoomService roomService) {
        this.roomService = roomService;
        if (roomService != null) {
            this.serviceId = roomService.getServiceId();
            this.unitPrice = roomService.getBasePrice();
            // Recalculate total cost
            this.totalCost = quantity * unitPrice;
        }
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
        if (booking != null) {
            this.bookingId = booking.getBookingId();
        }
    }
    
    // Utility methods
    public String getFormattedTotalCost() {
        return String.format("$%.2f", totalCost);
    }
    
    public String getFormattedUnitPrice() {
        return String.format("$%.2f", unitPrice);
    }
    
    public String getServiceName() {
        return roomService != null ? roomService.getServiceName() : "Unknown Service";
    }
    
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "Unknown Customer";
    }
    
    public String getComplimentaryStatus() {
        return isComplimentary ? "Complimentary" : "Paid";
    }
    
    public void calculateTotalCost() {
        if (isComplimentary) {
            this.totalCost = 0.0;
        } else {
            this.totalCost = quantity * unitPrice;
        }
    }
    
    @Override
    public String toString() {
        return "ServiceUsage{" +
                "usageId=" + usageId +
                ", bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", serviceId=" + serviceId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalCost=" + totalCost +
                ", isComplimentary=" + isComplimentary +
                ", usageDate=" + usageDate +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ServiceUsage that = (ServiceUsage) obj;
        return usageId == that.usageId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(usageId);
    }

    public RoomService getService() {
        return this.roomService;
    }

    public void setService(RoomService service) {
        this.roomService = service;
    }
}
