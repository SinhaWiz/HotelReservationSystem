package com.hotel.model;

import java.util.Date;

/**
 * Customer model class representing a hotel customer
 */
public class Customer {
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Date dateOfBirth;
    private double totalSpent;
    private Date registrationDate;
    private boolean isActive;
    private int loyaltyPoints;

    // Default constructor
    public Customer() {}

    // Constructor with required fields
    public Customer(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
        this.registrationDate = new Date();
        this.totalSpent = 0.0;
        this.loyaltyPoints = 0;
    }

    // Full constructor
    public Customer(int customerId, String firstName, String lastName, String email,
                    String phone, String address, Date dateOfBirth, double totalSpent,
                    Date registrationDate, boolean isActive, int loyaltyPoints) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.totalSpent = totalSpent;
        this.registrationDate = registrationDate;
        this.isActive = isActive;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() {
        return phone != null ? phone : "";
    }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public double getTotalSpent() {
        return totalSpent;
    }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }

    // Utility methods
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", totalSpent=" + totalSpent +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(customerId);
    }
}
