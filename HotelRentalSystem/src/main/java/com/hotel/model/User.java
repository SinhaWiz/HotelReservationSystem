package com.hotel.model;

import java.util.Date;

/**
 * Model class representing a User in the system
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String name; // Changed from fullName to match panel usage
    private UserType userType;
    private String phoneNumber; // Changed from phone to match panel usage
    private String address;
    private Date dateOfRegistration; // Changed from createdAt to match panel usage
    private Date updatedAt;
    
    public enum UserType {
        HOST("host"),
        RENTER("renter");
        
        private final String value;
        
        UserType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static UserType fromString(String text) {
            for (UserType type : UserType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
    
    // Default constructor
    public User() {
    }
    
    // Parameterized constructor
    public User(int userId, String username, String password, String email, String name, 
                UserType userType, String phoneNumber, String address, Date dateOfRegistration, Date updatedAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateOfRegistration = dateOfRegistration;
        this.updatedAt = updatedAt;
    }
    
    // Constructor without userId (for new user creation)
    public User(String username, String password, String email, String name, 
                UserType userType, String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // Backward compatibility methods
    public String getFullName() {
        return name;
    }
    
    public void setFullName(String fullName) {
        this.name = fullName;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    // Backward compatibility methods
    public String getPhone() {
        return phoneNumber;
    }
    
    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Date getDateOfRegistration() {
        return dateOfRegistration;
    }
    
    public void setDateOfRegistration(Date dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }
    
    // Backward compatibility methods
    public Date getCreatedAt() {
        return dateOfRegistration;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.dateOfRegistration = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", userType=" + userType +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", dateOfRegistration=" + dateOfRegistration +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 