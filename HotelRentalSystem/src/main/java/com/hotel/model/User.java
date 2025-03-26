package com.hotel.model;

import java.util.Date;

/**
 * Model class representing a User in the system
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String phoneNumber;
    private UserType userType;
    private String address;
    private Date dateOfRegistration;
    
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
    public User(int userId, String name, String email, String phoneNumber, UserType userType, 
                String address, Date dateOfRegistration) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.address = address;
        this.dateOfRegistration = dateOfRegistration;
    }
    
    // Constructor without userId (for new user creation)
    public User(String name, String email, String phoneNumber, UserType userType, String address, Date dateOfRegistration) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.address = address;
        this.dateOfRegistration = dateOfRegistration;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
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
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", userType=" + userType +
                ", address='" + address + '\'' +
                ", dateOfRegistration=" + dateOfRegistration +
                '}';
    }
} 