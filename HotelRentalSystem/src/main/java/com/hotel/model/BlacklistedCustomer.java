package com.hotel.model;

import java.util.Date;

/**
 * Model class representing a blacklisted customer
 */
public class BlacklistedCustomer {
    
    private int blacklistId;
    private int customerId;
    private String blacklistReason;
    private String blacklistedBy;
    private Date blacklistDate;
    private Date expiryDate;
    private boolean isActive;
    private String notes;
    
    // Related objects
    private Customer customer;
    
    // Constructors
    public BlacklistedCustomer() {
        this.blacklistDate = new Date();
        this.isActive = true;
    }
    
    public BlacklistedCustomer(int customerId, String blacklistReason, String blacklistedBy) {
        this();
        this.customerId = customerId;
        this.blacklistReason = blacklistReason;
        this.blacklistedBy = blacklistedBy;
    }
    
    public BlacklistedCustomer(int customerId, String blacklistReason, 
                              String blacklistedBy, Date expiryDate) {
        this(customerId, blacklistReason, blacklistedBy);
        this.expiryDate = expiryDate;
    }
    
    // Getters and Setters
    public int getBlacklistId() {
        return blacklistId;
    }
    
    public void setBlacklistId(int blacklistId) {
        this.blacklistId = blacklistId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public String getBlacklistReason() {
        return blacklistReason;
    }
    
    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }
    
    public String getBlacklistedBy() {
        return blacklistedBy;
    }
    
    public void setBlacklistedBy(String blacklistedBy) {
        this.blacklistedBy = blacklistedBy;
    }
    
    public Date getBlacklistDate() {
        return blacklistDate;
    }
    
    public void setBlacklistDate(Date blacklistDate) {
        this.blacklistDate = blacklistDate;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
    
    // Utility methods
    public boolean isPermanent() {
        return expiryDate == null;
    }
    
    public boolean isExpired() {
        if (expiryDate == null) {
            return false; // Permanent blacklist never expires
        }
        return new Date().after(expiryDate);
    }
    
    public boolean isCurrentlyBlacklisted() {
        return isActive && !isExpired();
    }
    
    public String getBlacklistStatus() {
        if (!isActive) {
            return "Removed";
        } else if (isExpired()) {
            return "Expired";
        } else if (isPermanent()) {
            return "Permanent";
        } else {
            return "Active";
        }
    }
    
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "Unknown Customer";
    }
    
    public String getBlacklistType() {
        return isPermanent() ? "Permanent" : "Temporary";
    }
    
    public long getDaysRemaining() {
        if (isPermanent() || isExpired()) {
            return 0;
        }
        
        long diffInMillies = expiryDate.getTime() - new Date().getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }
    
    public String getFormattedExpiryDate() {
        if (expiryDate == null) {
            return "Never";
        }
        return java.text.DateFormat.getDateInstance().format(expiryDate);
    }
    
    public String getFormattedBlacklistDate() {
        return java.text.DateFormat.getDateInstance().format(blacklistDate);
    }
    
    @Override
    public String toString() {
        return "BlacklistedCustomer{" +
                "blacklistId=" + blacklistId +
                ", customerId=" + customerId +
                ", blacklistReason='" + blacklistReason + '\'' +
                ", blacklistedBy='" + blacklistedBy + '\'' +
                ", blacklistDate=" + blacklistDate +
                ", expiryDate=" + expiryDate +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BlacklistedCustomer that = (BlacklistedCustomer) obj;
        return blacklistId == that.blacklistId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(blacklistId);
    }
}

