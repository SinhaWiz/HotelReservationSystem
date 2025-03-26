package com.hotel.dao;

import com.hotel.model.User;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities
 */
public class UserDAO {
    
    /**
     * Get all users from the database
     * @return List of User objects
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM User";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                user.setAddress(rs.getString("address"));
                user.setDateOfRegistration(rs.getDate("date_of_registration"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Get a user by ID
     * @param userId The ID of the user to retrieve
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String query = "SELECT * FROM User WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                    user.setAddress(rs.getString("address"));
                    user.setDateOfRegistration(rs.getDate("date_of_registration"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Add a new user to the database
     * @param user The User object to add
     * @return true if successful, false otherwise
     */
    public boolean addUser(User user) {
        String query = "INSERT INTO User (name, email, phone_number, user_type, address, date_of_registration) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setString(4, user.getUserType().getValue());
            pstmt.setString(5, user.getAddress());
            pstmt.setDate(6, new java.sql.Date(user.getDateOfRegistration().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing user in the database
     * @param user The User object to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        String query = "UPDATE User SET name = ?, email = ?, phone_number = ?, " +
                       "user_type = ?, address = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setString(4, user.getUserType().getValue());
            pstmt.setString(5, user.getAddress());
            pstmt.setInt(6, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete a user from the database
     * @param userId The ID of the user to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM User WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all hosts from the database
     * @return List of User objects with user_type = 'host'
     */
    public List<User> getAllHosts() {
        List<User> hosts = new ArrayList<>();
        String query = "SELECT * FROM User WHERE user_type = 'host'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setUserType(User.UserType.HOST);
                user.setAddress(rs.getString("address"));
                user.setDateOfRegistration(rs.getDate("date_of_registration"));
                hosts.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all hosts: " + e.getMessage());
        }
        
        return hosts;
    }
    
    /**
     * Get all renters from the database
     * @return List of User objects with user_type = 'renter'
     */
    public List<User> getAllRenters() {
        List<User> renters = new ArrayList<>();
        String query = "SELECT * FROM User WHERE user_type = 'renter'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setUserType(User.UserType.RENTER);
                user.setAddress(rs.getString("address"));
                user.setDateOfRegistration(rs.getDate("date_of_registration"));
                renters.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all renters: " + e.getMessage());
        }
        
        return renters;
    }
} 