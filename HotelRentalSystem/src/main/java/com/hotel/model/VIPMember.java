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
    public int getVipId() { return vipId; }
    public void setVipId(int vipId) { this.vipId = vipId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public MembershipLevel getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(MembershipLevel membershipLevel) { this.membershipLevel = membershipLevel; }
    
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public Date getMembershipStartDate() { return membershipStartDate; }
    public void setMembershipStartDate(Date membershipStartDate) { this.membershipStartDate = membershipStartDate; }
    
    public Date getMembershipEndDate() { return membershipEndDate; }
    public void setMembershipEndDate(Date membershipEndDate) { this.membershipEndDate = membershipEndDate; }
    
    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }

    // Utility methods
    public boolean isExpired() {
        return membershipEndDate != null && membershipEndDate.before(new Date());
    }
    
    public boolean isValidMembership() {
        return isActive && !isExpired();
    }
    
    public String getMembershipLevelString() {
        return membershipLevel.toString();
    }
    
    public void setMembershipLevelFromString(String levelStr) {
        try {
            this.membershipLevel = MembershipLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.membershipLevel = MembershipLevel.GOLD;
        }
    }
    
    public String getFormattedDiscountPercentage() {
        return String.format("%.1f%%", discountPercentage);
    }

    public static double getDefaultDiscountForLevel(MembershipLevel level) {
        switch (level) {
            case GOLD:
                return 10.0;
            case PLATINUM:
                return 15.0;
            case DIAMOND:
                return 20.0;
            default:
                return 5.0;
        }
    }

    public static String getDefaultBenefitsForLevel(MembershipLevel level) {
        switch (level) {
            case GOLD:
                return "- 10% discount on room rates\n" +
                       "- Early check-in when available\n" +
                       "- Welcome drink";
            case PLATINUM:
                return "- 15% discount on room rates\n" +
                       "- Early check-in and late check-out\n" +
                       "- Welcome drink and fruit basket\n" +
                       "- Access to executive lounge";
            case DIAMOND:
                return "- 20% discount on room rates\n" +
                       "- Flexible check-in and check-out\n" +
                       "- Welcome drink and premium amenities\n" +
                       "- Access to executive lounge\n" +
                       "- Free spa treatment once per stay";
            default:
                return "Basic VIP benefits";
        }
    }

    @Override
    public String toString() {
        return "VIPMember{" +
                "vipId=" + vipId +
                ", customerId=" + customerId +
                ", membershipLevel=" + membershipLevel +
                ", discountPercentage=" + discountPercentage +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VIPMember vipMember = (VIPMember) obj;
        return vipId == vipMember.vipId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(vipId);
    }
}
