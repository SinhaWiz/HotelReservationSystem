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
        String query = "SELECT * FROM properties";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Property property = new Property();
                property.setPropertyId(rs.getInt("property_id"));
                property.setHostId(rs.getInt("host_id"));
                property.setTitle(rs.getString("title"));
                property.setDescription(rs.getString("description"));
                property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                property.setLocation(rs.getString("location"));
                property.setCity(rs.getString("city"));
                property.setState(rs.getString("state"));
                property.setCountry(rs.getString("country"));
                property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                property.setBedrooms(rs.getInt("bedrooms"));
                property.setBathrooms(rs.getInt("bathrooms"));
                property.setMaxGuests(rs.getInt("max_guests"));
                property.setAmenities(rs.getString("amenities"));
                property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                property.setCreatedAt(rs.getTimestamp("created_at"));
                property.setUpdatedAt(rs.getTimestamp("updated_at"));
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
        String query = "SELECT * FROM properties WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setHostId(rs.getInt("host_id"));
                    property.setTitle(rs.getString("title"));
                    property.setDescription(rs.getString("description"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setLocation(rs.getString("location"));
                    property.setCity(rs.getString("city"));
                    property.setState(rs.getString("state"));
                    property.setCountry(rs.getString("country"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setBedrooms(rs.getInt("bedrooms"));
                    property.setBathrooms(rs.getInt("bathrooms"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    property.setAmenities(rs.getString("amenities"));
                    property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                    property.setCreatedAt(rs.getTimestamp("created_at"));
                    property.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return property;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting property by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get properties by host ID
     * @param hostId The ID of the host
     * @return List of Property objects owned by the specified host
     */
    public List<Property> getPropertiesByHostId(int hostId) {
        List<Property> properties = new ArrayList<>();
        String query = "SELECT * FROM properties WHERE host_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, hostId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setHostId(rs.getInt("host_id"));
                    property.setTitle(rs.getString("title"));
                    property.setDescription(rs.getString("description"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setLocation(rs.getString("location"));
                    property.setCity(rs.getString("city"));
                    property.setState(rs.getString("state"));
                    property.setCountry(rs.getString("country"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setBedrooms(rs.getInt("bedrooms"));
                    property.setBathrooms(rs.getInt("bathrooms"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    property.setAmenities(rs.getString("amenities"));
                    property.setAvailabilityStatus(rs.getBoolean("availability_status"));
                    property.setCreatedAt(rs.getTimestamp("created_at"));
                    property.setUpdatedAt(rs.getTimestamp("updated_at"));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting properties by owner ID: " + e.getMessage());
        }
        
        return properties;
    }
    
    /**
     * Add a new property to the database
     * @param property The Property object to add
     * @return true if successful, false otherwise
     */
    public boolean addProperty(Property property) {
        String query = "INSERT INTO properties (host_id, title, description, property_type, location, city, state, country, " +
                       "price_per_night, bedrooms, bathrooms, max_guests, amenities, availability_status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, new String[]{"property_id"})) {
            
            pstmt.setInt(1, property.getHostId());
            pstmt.setString(2, property.getTitle());
            pstmt.setString(3, property.getDescription());
            pstmt.setString(4, property.getPropertyType().getValue());
            pstmt.setString(5, property.getLocation());
            pstmt.setString(6, property.getCity());
            pstmt.setString(7, property.getState());
            pstmt.setString(8, property.getCountry());
            pstmt.setBigDecimal(9, property.getPricePerNight());
            pstmt.setInt(10, property.getBedrooms());
            pstmt.setInt(11, property.getBathrooms());
            pstmt.setInt(12, property.getMaxGuests());
            pstmt.setString(13, property.getAmenities());
            pstmt.setBoolean(14, property.isAvailabilityStatus());
            
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
        String query = "UPDATE properties SET owner_id = ?, title = ?, description = ?, property_type = ?, " +
                       "address = ?, city = ?, state = ?, country = ?, price_per_night = ?, bedrooms = ?, " +
                       "bathrooms = ?, max_guests = ?, amenities = ?, status = ? WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, property.getOwnerId());
            pstmt.setString(2, property.getTitle());
            pstmt.setString(3, property.getDescription());
            pstmt.setString(4, property.getPropertyType().getValue());
            pstmt.setString(5, property.getAddress());
            pstmt.setString(6, property.getCity());
            pstmt.setString(7, property.getState());
            pstmt.setString(8, property.getCountry());
            pstmt.setBigDecimal(9, property.getPricePerNight());
            pstmt.setInt(10, property.getBedrooms());
            pstmt.setInt(11, property.getBathrooms());
            pstmt.setInt(12, property.getMaxGuests());
            pstmt.setString(13, property.getAmenities());
            pstmt.setString(14, property.getStatus().getValue());
            pstmt.setInt(15, property.getPropertyId());
            
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
        String query = "DELETE FROM properties WHERE property_id = ?";
        
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
        String query = "SELECT * FROM properties WHERE status = 'AVAILABLE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Property property = new Property();
                property.setPropertyId(rs.getInt("property_id"));
                property.setOwnerId(rs.getInt("owner_id"));
                property.setTitle(rs.getString("title"));
                property.setDescription(rs.getString("description"));
                property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                property.setAddress(rs.getString("address"));
                property.setCity(rs.getString("city"));
                property.setState(rs.getString("state"));
                property.setCountry(rs.getString("country"));
                property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                property.setBedrooms(rs.getInt("bedrooms"));
                property.setBathrooms(rs.getInt("bathrooms"));
                property.setMaxGuests(rs.getInt("max_guests"));
                property.setAmenities(rs.getString("amenities"));
                property.setStatus(Property.Status.fromString(rs.getString("status")));
                property.setCreatedAt(rs.getTimestamp("created_at"));
                property.setUpdatedAt(rs.getTimestamp("updated_at"));
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
        String query = "SELECT * FROM properties WHERE LOWER(city) LIKE LOWER(?) OR LOWER(address) LIKE LOWER(?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + location + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property property = new Property();
                    property.setPropertyId(rs.getInt("property_id"));
                    property.setOwnerId(rs.getInt("owner_id"));
                    property.setTitle(rs.getString("title"));
                    property.setDescription(rs.getString("description"));
                    property.setPropertyType(Property.PropertyType.fromString(rs.getString("property_type")));
                    property.setAddress(rs.getString("address"));
                    property.setCity(rs.getString("city"));
                    property.setState(rs.getString("state"));
                    property.setCountry(rs.getString("country"));
                    property.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    property.setBedrooms(rs.getInt("bedrooms"));
                    property.setBathrooms(rs.getInt("bathrooms"));
                    property.setMaxGuests(rs.getInt("max_guests"));
                    property.setAmenities(rs.getString("amenities"));
                    property.setStatus(Property.Status.fromString(rs.getString("status")));
                    property.setCreatedAt(rs.getTimestamp("created_at"));
                    property.setUpdatedAt(rs.getTimestamp("updated_at"));
                    properties.add(property);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching properties by location: " + e.getMessage());
        }
        
        return properties;
    }
} 