package com.hotel.model;

import java.util.Date;

/**
 * Room model class representing individual hotel rooms
 */
public class Room {
    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_ORDER;

        @Override
        public String toString() {
            return name().replace('_', ' ');
        }
    }
    
    private int roomId;
    private String roomNumber;
    private int typeId;
    private RoomType roomType;
    private int floorNumber;
    private RoomStatus status;
    private Date lastMaintenance;
    private Date createdDate;
    private String amenities;
    private Date lastCleaned;
    private String notes;
    private String description;
    private double basePrice;

    // Default constructor
    public Room() {
        this.status = RoomStatus.AVAILABLE;
        this.createdDate = new Date();
        this.roomType = new RoomType();
    }
    
    // Constructor with required fields
    public Room(String roomNumber, int typeId, int floorNumber) {
        this.roomNumber = roomNumber;
        this.typeId = typeId;
        this.floorNumber = floorNumber;
        this.status = RoomStatus.AVAILABLE;
        this.createdDate = new Date();
    }
    
    // Full constructor
    public Room(int roomId, String roomNumber, int typeId, int floorNumber, 
               RoomStatus status, Date lastMaintenance, Date createdDate) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.typeId = typeId;
        this.floorNumber = floorNumber;
        this.status = status;
        this.lastMaintenance = lastMaintenance;
        this.createdDate = createdDate;
    }
    
    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType;
        if (roomType != null) {
            this.typeId = roomType.getTypeId();
        }
    }
    
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public void setStatusFromString(String statusStr) {
        try {
            this.status = RoomStatus.valueOf(statusStr.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            this.status = RoomStatus.AVAILABLE;
        }
    }
    
    public Date getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(Date lastMaintenance) { this.lastMaintenance = lastMaintenance; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public Date getLastCleaned() { return lastCleaned; }
    public void setLastCleaned(Date lastCleaned) { this.lastCleaned = lastCleaned; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    
    // Utility methods
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }
    
    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }
    
    public boolean isUnderMaintenance() {
        return status == RoomStatus.MAINTENANCE;
    }
    
    public String getRoomStatusString() {
        return status != null ? status.toString() : "";
    }
    
    public double getBaseRate() {
        return roomType != null ? roomType.getBaseRate() : 0.0;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", typeId=" + typeId +
                ", floorNumber=" + floorNumber +
                ", status=" + status +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return roomId == room.roomId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(roomId);
    }
}
