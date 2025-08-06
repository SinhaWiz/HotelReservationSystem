package com.hotel.model;

import java.util.Date;

/**
 * Room model class representing individual hotel rooms
 */
public class Room {
    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
    }
    
    private int roomId;
    private String roomNumber;
    private int typeId;
    private RoomType roomType; // For joined queries
    private int floorNumber;
    private RoomStatus status;
    private Date lastMaintenance;
    private Date createdDate;
    
    // Default constructor
    public Room() {
        this.status = RoomStatus.AVAILABLE;
        this.createdDate = new Date();
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
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    
    public Date getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(Date lastMaintenance) { this.lastMaintenance = lastMaintenance; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    // Utility methods
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }
    
    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }
    
    public boolean isReserved() {
        return status == RoomStatus.RESERVED;
    }
    
    public boolean isUnderMaintenance() {
        return status == RoomStatus.MAINTENANCE;
    }
    
    public String getStatusString() {
        return status.toString();
    }
    
    public void setStatusFromString(String statusStr) {
        try {
            this.status = RoomStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.status = RoomStatus.AVAILABLE;
        }
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

