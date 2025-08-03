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
        String query = "SELECT * FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
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
        String query = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setFullName(rs.getString("full_name"));
                    user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get a user by username
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setFullName(rs.getString("full_name"));
                    user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Add a new user to the database
     * @param user The User object to add
     * @return true if successful, false otherwise
     */
    public boolean addUser(User user) {
        String query = "INSERT INTO users (username, password, email, full_name, user_type, phone, address) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, new String[]{"user_id"})) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getUserType().getValue());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getAddress());
            
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
        String query = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, " +
                       "user_type = ?, phone = ?, address = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getUserType().getValue());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getAddress());
            pstmt.setInt(8, user.getUserId());
            
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
        String query = "DELETE FROM users WHERE user_id = ?";
        
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
     * Get all owners from the database
     * @return List of User objects with type OWNER
     */
    public List<User> getAllOwners() {
        List<User> owners = new ArrayList<>();
        String query = "SELECT * FROM users WHERE user_type = 'OWNER'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                owners.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all owners: " + e.getMessage());
        }
        
        return owners;
    }
    
    /**
     * Get all customers from the database
     * @return List of User objects with type CUSTOMER
     */
    public List<User> getAllCustomers() {
        List<User> customers = new ArrayList<>();
        String query = "SELECT * FROM users WHERE user_type = 'CUSTOMER'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setUserType(User.UserType.fromString(rs.getString("user_type")));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                customers.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
        }
        
        return customers;
    }
} 