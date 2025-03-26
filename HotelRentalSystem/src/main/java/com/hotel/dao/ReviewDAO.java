package com.hotel.dao;

import com.hotel.model.Review;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Review entities
 */
public class ReviewDAO {
    
    /**
     * Get all reviews from the database
     * @return List of Review objects
     */
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Review";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setPropertyId(rs.getInt("property_id"));
                review.setUserId(rs.getInt("user_id"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setDatePosted(rs.getDate("date_posted"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get a review by ID
     * @param reviewId The ID of the review to retrieve
     * @return Review object if found, null otherwise
     */
    public Review getReviewById(int reviewId) {
        String query = "SELECT * FROM Review WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reviewId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Review review = new Review();
                    review.setReviewId(rs.getInt("review_id"));
                    review.setPropertyId(rs.getInt("property_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setDatePosted(rs.getDate("date_posted"));
                    return review;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting review by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all reviews for a property
     * @param propertyId The ID of the property
     * @return List of Review objects for the specified property
     */
    public List<Review> getReviewsByPropertyId(int propertyId) {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Review WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setReviewId(rs.getInt("review_id"));
                    review.setPropertyId(rs.getInt("property_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setDatePosted(rs.getDate("date_posted"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews by property ID: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get all reviews by a user
     * @param userId The ID of the user
     * @return List of Review objects by the specified user
     */
    public List<Review> getReviewsByUserId(int userId) {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Review WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setReviewId(rs.getInt("review_id"));
                    review.setPropertyId(rs.getInt("property_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setDatePosted(rs.getDate("date_posted"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews by user ID: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Add a new review to the database
     * @param review The Review object to add
     * @return true if successful, false otherwise
     */
    public boolean addReview(Review review) {
        String query = "INSERT INTO Review (property_id, user_id, rating, comment, date_posted) " +
                       "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, review.getPropertyId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());
            pstmt.setDate(5, new java.sql.Date(review.getDatePosted().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        review.setReviewId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing review in the database
     * @param review The Review object to update
     * @return true if successful, false otherwise
     */
    public boolean updateReview(Review review) {
        String query = "UPDATE Review SET property_id = ?, user_id = ?, rating = ?, " +
                       "comment = ?, date_posted = ? WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, review.getPropertyId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());
            pstmt.setDate(5, new java.sql.Date(review.getDatePosted().getTime()));
            pstmt.setInt(6, review.getReviewId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete a review from the database
     * @param reviewId The ID of the review to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteReview(int reviewId) {
        String query = "DELETE FROM Review WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, reviewId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get the average rating for a property
     * @param propertyId The ID of the property
     * @return The average rating for the property, or 0 if no reviews
     */
    public double getAverageRatingForProperty(int propertyId) {
        String query = "SELECT AVG(rating) FROM Review WHERE property_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, propertyId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating for property: " + e.getMessage());
        }
        
        return 0.0;
    }
} 