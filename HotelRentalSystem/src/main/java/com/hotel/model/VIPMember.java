package com.hotel.model;

import java.util.Date;

/**
 * VIPMember model class representing VIP customers with special privileges
 */
public class VIPMember {
    public enum MembershipLevel {
        GOLD, PLATINUM, DIAMOND
    }
    
    private int vipId;
    private int customerId;
    private Customer customer; // For joined queries
    private MembershipLevel membershipLevel;
    private double discountPercentage;
    private Date membershipStartDate;
    private Date membershipEndDate;
    private String benefits;
    private boolean isActive;
    private int bookingCount;

    // Default constructor
    public VIPMember() {
        this.membershipLevel = MembershipLevel.GOLD;
        this.discountPercentage = 10.0;
        this.membershipStartDate = new Date();
        this.isActive = true;
    }
    
    // Constructor with required fields
    public VIPMember(int customerId, MembershipLevel membershipLevel, double discountPercentage) {
        this();
        this.customerId = customerId;
        this.membershipLevel = membershipLevel;
        this.discountPercentage = discountPercentage;
    }
    
    // Full constructor
    public VIPMember(int vipId, int customerId, MembershipLevel membershipLevel, 
                    double discountPercentage, Date membershipStartDate, Date membershipEndDate,
                    String benefits, boolean isActive) {
        this.vipId = vipId;
        this.customerId = customerId;
        this.membershipLevel = membershipLevel;
        this.discountPercentage = discountPercentage;
        this.membershipStartDate = membershipStartDate;
        this.membershipEndDate = membershipEndDate;
        this.benefits = benefits;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getVipId() {
        return vipId;
    }

    public void setVipId(int vipId) {
        this.vipId = vipId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public MembershipLevel getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(MembershipLevel membershipLevel) {
        this.membershipLevel = membershipLevel;
        // Update discount percentage based on level
        switch (membershipLevel) {
            case GOLD:
                this.discountPercentage = 10.0;
                break;
            case PLATINUM:
                this.discountPercentage = 15.0;
                break;
            case DIAMOND:
                this.discountPercentage = 20.0;
                break;
        }
    }

    public String getMembershipLevelString() {
        return membershipLevel != null ? membershipLevel.toString() : "GOLD";
    }

    public void setMembershipLevelFromString(String levelString) {
        if (levelString != null) {
            try {
                this.membershipLevel = MembershipLevel.valueOf(levelString.toUpperCase());
                // Update discount percentage based on level
                switch (this.membershipLevel) {
                    case GOLD:
                        this.discountPercentage = 10.0;
                        break;
                    case PLATINUM:
                        this.discountPercentage = 15.0;
                        break;
                    case DIAMOND:
                        this.discountPercentage = 20.0;
                        break;
                }
            } catch (IllegalArgumentException e) {
                this.membershipLevel = MembershipLevel.GOLD; // Default fallback
                this.discountPercentage = 10.0;
            }
        }
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getFormattedDiscountPercentage() {
        return String.format("%.1f%%", discountPercentage);
    }

    public Date getMembershipStartDate() {
        return membershipStartDate;
    }

    public void setMembershipStartDate(Date membershipStartDate) {
        this.membershipStartDate = membershipStartDate;
    }

    // Add missing setter methods
    public void setJoinDate(Date joinDate) {
        this.membershipStartDate = joinDate;
    }

    public void setUpgradeDate(Date upgradeDate) {
        // For upgrade tracking - could be stored separately if needed
        this.membershipStartDate = upgradeDate;
    }

    public Date getMembershipEndDate() {
        return membershipEndDate;
    }

    public void setMembershipEndDate(Date membershipEndDate) {
        this.membershipEndDate = membershipEndDate;
    }

    public String getBenefits() {
        return benefits != null ? benefits : "";
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public void setSpecialRequests(String specialRequests) {
        this.benefits = specialRequests; // Using benefits field for special requests
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    // Add missing setter for total spent (could be tracked separately)
    private double totalSpent = 0.0;

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    // Utility methods
    public boolean isValidMembership() {
        if (!isActive) return false;
        if (membershipEndDate == null) return true;
        return membershipEndDate.after(new java.util.Date());
    }

    public String getStatusString() {
        return isValidMembership() ? "Active" : "Inactive";
    }

    public long getDaysSinceJoining() {
        if (membershipStartDate == null) return 0;
        long diff = new java.util.Date().getTime() - membershipStartDate.getTime();
        return diff / (24 * 60 * 60 * 1000);
    }

    @Override
    public String toString() {
        return String.format("VIPMember{vipId=%d, customerId=%d, level=%s, discount=%.1f%%, active=%s}",
                vipId, customerId, membershipLevel, discountPercentage, isActive);
    }
}
