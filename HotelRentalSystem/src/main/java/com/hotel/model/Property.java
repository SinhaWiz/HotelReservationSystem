package com.hotel.model;

import java.math.BigDecimal;

/**
 * Model class representing a Property in the system
 */
public class Property {
    private int propertyId;
    private int hostId;
    private PropertyType propertyType;
    private String location;
    private BigDecimal pricePerNight;
    private String description;
    private boolean availabilityStatus;
    private int maxGuests;
    
    public enum PropertyType {
        APARTMENT("apartment"),
        HOUSE("house"),
        VILLA("villa");
        
        private final String value;
        
        PropertyType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static PropertyType fromString(String text) {
            for (PropertyType type : PropertyType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    // Constructors
    public Property() {
    }
    
    public Property(int propertyId, int hostId, PropertyType propertyType, String location,
                   BigDecimal pricePerNight, String description, boolean availabilityStatus, int maxGuests) {
        this.propertyId = propertyId;
        this.hostId = hostId;
        this.propertyType = propertyType;
        this.location = location;
        this.pricePerNight = pricePerNight;
        this.description = description;
        this.availabilityStatus = availabilityStatus;
        this.maxGuests = maxGuests;
    }
    
    // Getters and Setters
    public int getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    
    public int getHostId() {
        return hostId;
    }
    
    public void setHostId(int hostId) {
        this.hostId = hostId;
    }
    
    public PropertyType getPropertyType() {
        return propertyType;
    }
    
    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isAvailabilityStatus() {
        return availabilityStatus;
    }
    
    public void setAvailabilityStatus(boolean availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
    
    public int getMaxGuests() {
        return maxGuests;
    }
    
    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }
    
    @Override
    public String toString() {
        return "Property{" +
                "propertyId=" + propertyId +
                ", hostId=" + hostId +
                ", propertyType=" + propertyType +
                ", location='" + location + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", availabilityStatus=" + availabilityStatus +
                ", maxGuests=" + maxGuests +
                '}';
    }
} 