package com.hotel.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class representing a Property in the system
 */
public class Property {
    private int propertyId;
    private int ownerId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal pricePerNight;
    private int bedrooms;
    private int bathrooms;
    private int maxGuests;
    private String amenities;
    private Status status;
    private Date createdAt;
    private Date updatedAt;
    
    public enum PropertyType {
        HOTEL("HOTEL"),
        APARTMENT("APARTMENT"),
        VILLA("VILLA"),
        RESORT("RESORT");
        
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
    
    public enum Status {
        AVAILABLE("AVAILABLE"),
        BOOKED("BOOKED"),
        MAINTENANCE("MAINTENANCE"),
        INACTIVE("INACTIVE");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static Status fromString(String text) {
            for (Status status : Status.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    // Constructors
    public Property() {
    }
    
    public Property(int propertyId, int ownerId, String title, String description, PropertyType propertyType,
                   String address, String city, String state, String country, BigDecimal pricePerNight,
                   int bedrooms, int bathrooms, int maxGuests, String amenities, Status status,
                   Date createdAt, Date updatedAt) {
        this.propertyId = propertyId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pricePerNight = pricePerNight;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.maxGuests = maxGuests;
        this.amenities = amenities;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    
    public int getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PropertyType getPropertyType() {
        return propertyType;
    }
    
    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    public int getBedrooms() {
        return bedrooms;
    }
    
    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }
    
    public int getBathrooms() {
        return bathrooms;
    }
    
    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }
    
    public int getMaxGuests() {
        return maxGuests;
    }
    
    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }
    
    public String getAmenities() {
        return amenities;
    }
    
    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Property{" +
                "propertyId=" + propertyId +
                ", ownerId=" + ownerId +
                ", title='" + title + '\'' +
                ", propertyType=" + propertyType +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", status=" + status +
                ", maxGuests=" + maxGuests +
                '}';
    }
} 