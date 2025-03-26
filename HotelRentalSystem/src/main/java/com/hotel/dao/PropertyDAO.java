package com.hotel.dao;

import com.hotel.model.Property;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Property entities
 */
public class PropertyDAO {
    
    /**
     * Get all properties from the database
     * @return List of Property objects
     */
    public List<Property> getAllProperties() {
        List<Property> properties = new ArrayList<>();
        String query = "SELECT * FROM Property";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Property property = new Property();
                property.setPropertyId(rs.getInt("property_id"));
                property.setHostId(rs.getInt("host_id"));
                property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                property.setLocation(rs.getString("location"));
                property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                property.setDescription(rs.getString("description"));
                property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                property.setMaxGuests(rs.getInt("max_guests"));
                properties.add(property);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all properties: " + e.getMessage());
        }
        
        return properties;
    }
    
    /**
     * Get a property by ID
     * @param propertyId The ID of the property to retrieve
     * @return Property object if found, null otherwise
     */
    public Property getPropertyById(int propertyId) {
        String query = "SELECT * FROM Property WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setHostId(rs.getInt("host_id"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setLocation(rs.getString("location"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setDescription(rs.getString("description"));
                    property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    return property;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting property by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all properties by host ID
     * @param hostId The ID of the host
     * @return List of Property objects owned by the specified host
     */
    public List<Property> getPropertiesByHostId(int hostId) {
        List<Property> properties = new ArrayList<>();
        String query = "SELECT * FROM Property WHERE host_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, hostId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setHostId(rs.getInt("host_id"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setLocation(rs.getString("location"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setDescription(rs.getString("description"));
                    property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting properties by host ID: " + e.getMessage());
        }
        
        return properties;
    }
    
    /**
     * Add a new property to the database
     * @param property The Property object to add
     * @return true if successful, false otherwise
     */
    public boolean addProperty(Property property) {
        String query = "INSERT INTO Property (host_id, property_type, location, price_per_night, " +
                       "description, availability_status, max_guests) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, property.getHostId());
            pstmt.setString(2, property.getPropertyType().getValue());
            pstmt.setString(3, property.getLocation());
            pstmt.setBigDecimal(4, property.getPricePerNight());
            pstmt.setString(5, property.getDescription());
            pstmt.setBoolean(6, property.isAvailabilityStatus());
            pstmt.setInt(7, property.getMaxGuests());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        property.setPropertyId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding property: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing property in the database
     * @param property The Property object to update
     * @return true if successful, false otherwise
     */
    public boolean updateProperty(Property property) {
        String query = "UPDATE Property SET host_id = ?, property_type = ?, location = ?, " +
                       "price_per_night = ?, description = ?, availability_status = ?, " +
                       "max_guests = ? WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, property.getHostId());
            pstmt.setString(2, property.getPropertyType().getValue());
            pstmt.setString(3, property.getLocation());
            pstmt.setBigDecimal(4, property.getPricePerNight());
            pstmt.setString(5, property.getDescription());
            pstmt.setBoolean(6, property.isAvailabilityStatus());
            pstmt.setInt(7, property.getMaxGuests());
            pstmt.setInt(8, property.getPropertyId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating property: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete a property from the database
     * @param propertyId The ID of the property to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteProperty(int propertyId) {
        String query = "DELETE FROM Property WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting property: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all available properties
     * @return List of Property objects that are available
     */
    public List<Property> getAvailableProperties() {
        List<Property> properties = new ArrayList<>();
        String query = "SELECT * FROM Property WHERE availability_status = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Property property = new Property();
                property.setPropertyId(rs.getInt("property_id"));
                property.setHostId(rs.getInt("host_id"));
                property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                property.setLocation(rs.getString("location"));
                property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                property.setDescription(rs.getString("description"));
                property.setAvailabilityStatus(true);
                property.setMaxGuests(rs.getInt("max_guests"));
                properties.add(property);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available properties: " + e.getMessage());
        }
        
        return properties;
    }
    
    /**
     * Search properties by location
     * @param location The location to search for
     * @return List of Property objects matching the location
     */
    public List<Property> searchPropertiesByLocation(String location) {
        List<Property> properties = new ArrayList<>();
        String query = "SELECT * FROM Property WHERE location LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + location + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setHostId(rs.getInt("host_id"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setLocation(rs.getString("location"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setDescription(rs.getString("description"));
                    property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching properties by location: " + e.getMessage());
        }
        
        return properties;
    }
} 