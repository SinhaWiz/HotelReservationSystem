package com.hotel.model;

/**
 * Class representing different types of hotel rooms
 */
public class RoomType {
    private int typeId;
    private String typeName;
    private double basePrice;
    private int maxOccupancy;
    private String description;
    private String amenities;

    public RoomType() {}

    public RoomType(int typeId, String typeName, double basePrice, int maxOccupancy) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
    }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public double getBaseRate() { return basePrice; }
    public void setBaseRate(double basePrice) { this.basePrice = basePrice; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    @Override
    public String toString() {
        return typeName;
    }
}
