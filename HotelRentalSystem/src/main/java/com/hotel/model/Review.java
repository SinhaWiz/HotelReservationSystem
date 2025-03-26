package com.hotel.model;

import java.util.Date;

/**
 * Model class representing a Review in the system
 */
public class Review {
    private int reviewId;
    private int propertyId;
    private int userId;
    private int rating;
    private String comment;
    private Date datePosted;
    
    // Default constructor
    public Review() {
    }
    
    // Parameterized constructor
    public Review(int reviewId, int propertyId, int userId, int rating, 
                  String comment, Date datePosted) {
        this.reviewId = reviewId;
        this.propertyId = propertyId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.datePosted = datePosted;
    }
    
    // Constructor without reviewId (for new review creation)
    public Review(int propertyId, int userId, int rating, String comment, Date datePosted) {
        this.propertyId = propertyId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.datePosted = datePosted;
    }
    
    // Getters and Setters
    public int getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }
    
    public int getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public Date getDatePosted() {
        return datePosted;
    }
    
    public void setDatePosted(Date datePosted) {
        this.datePosted = datePosted;
    }
    
    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", propertyId=" + propertyId +
                ", userId=" + userId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", datePosted=" + datePosted +
                '}';
    }
} 