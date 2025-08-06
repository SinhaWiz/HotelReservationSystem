package com.hotel.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing an invoice
 */
public class Invoice {
    
    public enum PaymentStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }
    
    private long invoiceId;
    private long bookingId;
    private int customerId;
    private String invoiceNumber;
    private Date invoiceDate;
    private Date dueDate;
    private double subtotal;
    private double taxAmount;
    private double discountAmount;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private Date paymentDate;
    private String paymentMethod;
    private String notes;
    private String createdBy;
    
    // Related objects
    private Customer customer;
    private Booking booking;
    private List<InvoiceLineItem> lineItems;
    
    // Constructors
    public Invoice() {
        this.invoiceDate = new Date();
        this.paymentStatus = PaymentStatus.PENDING;
        this.lineItems = new ArrayList<>();
        // Set due date to 30 days from invoice date
        this.dueDate = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
    }
    
    public Invoice(long bookingId, int customerId, String invoiceNumber) {
        this();
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.invoiceNumber = invoiceNumber;
    }
    
    // Getters and Setters
    public long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
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
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public Date getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public double getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPaymentStatusString() {
        return paymentStatus != null ? paymentStatus.name() : PaymentStatus.PENDING.name();
    }
    
    public void setPaymentStatusFromString(String statusString) {
        try {
            this.paymentStatus = PaymentStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }
    
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
        // Automatically update payment status when payment date is set
        if (paymentDate != null && paymentStatus == PaymentStatus.PENDING) {
            this.paymentStatus = PaymentStatus.PAID;
        }
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
        if (booking != null) {
            this.bookingId = booking.getBookingId();
        }
    }
    
    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }
    
    public void setLineItems(List<InvoiceLineItem> lineItems) {
        this.lineItems = lineItems != null ? lineItems : new ArrayList<>();
    }
    
    // Utility methods
    public void addLineItem(InvoiceLineItem lineItem) {
        if (lineItems == null) {
            lineItems = new ArrayList<>();
        }
        lineItems.add(lineItem);
        lineItem.setInvoiceId(this.invoiceId);
    }
    
    public void removeLineItem(InvoiceLineItem lineItem) {
        if (lineItems != null) {
            lineItems.remove(lineItem);
        }
    }
    
    public boolean isOverdue() {
        return paymentStatus == PaymentStatus.PENDING && 
               dueDate != null && new Date().after(dueDate);
    }
    
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
    
    public long getDaysUntilDue() {
        if (dueDate == null || isPaid()) {
            return 0;
        }
        
        long diffInMillies = dueDate.getTime() - new Date().getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }
    
    public long getDaysOverdue() {
        if (dueDate == null || !isOverdue()) {
            return 0;
        }
        
        long diffInMillies = new Date().getTime() - dueDate.getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }
    
    public String getFormattedSubtotal() {
        return String.format("$%.2f", subtotal);
    }
    
    public String getFormattedTaxAmount() {
        return String.format("$%.2f", taxAmount);
    }
    
    public String getFormattedDiscountAmount() {
        return String.format("$%.2f", discountAmount);
    }
    
    public String getFormattedTotalAmount() {
        return String.format("$%.2f", totalAmount);
    }
    
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "Unknown Customer";
    }
    
    public String getFormattedInvoiceDate() {
        return java.text.DateFormat.getDateInstance().format(invoiceDate);
    }
    
    public String getFormattedDueDate() {
        return dueDate != null ? java.text.DateFormat.getDateInstance().format(dueDate) : "N/A";
    }
    
    public String getFormattedPaymentDate() {
        return paymentDate != null ? java.text.DateFormat.getDateInstance().format(paymentDate) : "Not Paid";
    }
    
    public String getPaymentStatusDisplay() {
        switch (paymentStatus) {
            case PAID:
                return "Paid";
            case PENDING:
                return isOverdue() ? "Overdue" : "Pending";
            case OVERDUE:
                return "Overdue";
            case CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }
    
    public void calculateTotals() {
        if (lineItems != null) {
            subtotal = lineItems.stream()
                    .filter(item -> !item.getItemType().equals("TAX") && !item.getItemType().equals("DISCOUNT"))
                    .mapToDouble(InvoiceLineItem::getLineTotal)
                    .sum();
            
            taxAmount = lineItems.stream()
                    .filter(item -> item.getItemType().equals("TAX"))
                    .mapToDouble(InvoiceLineItem::getLineTotal)
                    .sum();
            
            discountAmount = Math.abs(lineItems.stream()
                    .filter(item -> item.getItemType().equals("DISCOUNT"))
                    .mapToDouble(InvoiceLineItem::getLineTotal)
                    .sum());
            
            totalAmount = subtotal + taxAmount - discountAmount;
        }
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", customerId=" + customerId +
                ", totalAmount=" + totalAmount +
                ", paymentStatus=" + paymentStatus +
                ", invoiceDate=" + invoiceDate +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Invoice invoice = (Invoice) obj;
        return invoiceId == invoice.invoiceId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(invoiceId);
    }
}

