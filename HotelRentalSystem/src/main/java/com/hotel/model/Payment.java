package com.hotel.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class representing a Payment in the system
 */
public class Payment {
    private int paymentId;
    private int bookingId;
    private Date paymentDate;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    
    public enum PaymentMethod {
        CREDIT_CARD("credit card"),
        PAYPAL("PayPal"),
        BANK_TRANSFER("bank transfer");
        
        private final String value;
        
        PaymentMethod(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static PaymentMethod fromString(String text) {
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.value.equalsIgnoreCase(text)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    // Default constructor
    public Payment() {
    }
    
    // Parameterized constructor
    public Payment(int paymentId, int bookingId, Date paymentDate, 
                   PaymentMethod paymentMethod, BigDecimal amount) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
    
    // Constructor without paymentId (for new payment creation)
    public Payment(int bookingId, Date paymentDate, PaymentMethod paymentMethod, BigDecimal amount) {
        this.bookingId = bookingId;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
    
    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", bookingId=" + bookingId +
                ", paymentDate=" + paymentDate +
                ", paymentMethod=" + paymentMethod +
                ", amount=" + amount +
                '}';
    }
} 