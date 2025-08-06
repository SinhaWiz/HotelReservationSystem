package com.hotel.model;

import java.util.Date;

/**
 * RoomType model class representing different types of hotel rooms
 */
public class RoomType {
    private int typeId;
    private String typeName;
    private double basePrice;
    private int maxOccupancy;
    private String amenities;
    private Date createdDate;
    
    // Default constructor
    public RoomType() {}
    
    // Constructor with required fields
    public RoomType(String typeName, double basePrice, int maxOccupancy) {
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
        this.createdDate = new Date();
    }
    
    // Full constructor
    public RoomType(int typeId, String typeName, double basePrice, int maxOccupancy, 
                   String amenities, Date createdDate) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
        this.amenities = amenities;
        this.createdDate = createdDate;
    }
    
    // Getters and Setters
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    
    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    @Override
    public String toString() {
        return "RoomType{" +
                "typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                ", basePrice=" + basePrice +
                ", maxOccupancy=" + maxOccupancy +
                ", amenities='" + amenities + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoomType roomType = (RoomType) obj;
        return typeId == roomType.typeId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(typeId);
    }
}

