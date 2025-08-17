package com.hotel.model;

import java.util.Date;

/**
 * Model class representing a hotel room service
 */
public class RoomService {
    
    public enum ServiceCategory {
        HOUSEKEEPING, FOOD, LAUNDRY, MAINTENANCE, ENTERTAINMENT, TRANSPORTATION, ACCOMMODATION
    }
    
    private int serviceId;
    private String serviceName;
    private String serviceDescription;
    private ServiceCategory serviceCategory;
    private double basePrice;
    private boolean isActive;
    private Date createdDate;
    
    // Constructors
    public RoomService() {
        this.isActive = true;
        this.createdDate = new Date();
    }
    
    public RoomService(String serviceName, String serviceDescription, 
                      ServiceCategory serviceCategory, double basePrice) {
        this();
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceCategory = serviceCategory;
        this.basePrice = basePrice;
    }
    
    // Getters and Setters
    public int getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceDescription() {
        return serviceDescription;
    }
    
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }
    
    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }
    
    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }
    
    public String getServiceCategoryString() {
        return serviceCategory != null ? serviceCategory.name() : "";
    }
    
    public void setServiceCategoryFromString(String categoryString) {
        try {
            this.serviceCategory = ServiceCategory.valueOf(categoryString);
        } catch (IllegalArgumentException e) {
            this.serviceCategory = ServiceCategory.HOUSEKEEPING;
        }
    }
    
    public double getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    // Utility methods
    public String getFormattedPrice() {
        return String.format("$%.2f", basePrice);
    }
    
    public String getDisplayName() {
        return serviceName + " (" + getFormattedPrice() + ")";
    }
    
    public double getPrice() {
        return this.basePrice;
    }

    @Override
    public String toString() {
        return "RoomService{" +
                "serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", serviceCategory=" + serviceCategory +
                ", basePrice=" + basePrice +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RoomService that = (RoomService) obj;
        return serviceId == that.serviceId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(serviceId);
    }
}
