package com.hotel.view.panels;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PropertyDAO;
import com.hotel.dao.UserDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.model.Booking;
import com.hotel.model.Property;
import com.hotel.model.User;
import com.hotel.model.Review;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Dashboard panel showing system overview
 */
public class DashboardPanel extends JPanel {
    
    private UserDAO userDAO;
    private PropertyDAO propertyDAO;
    private BookingDAO bookingDAO;
    private ReviewDAO reviewDAO;
    
    private JLabel totalUsersLabel;
    private JLabel totalPropertiesLabel;
    private JLabel totalBookingsLabel;
    private JLabel pendingBookingsLabel;
    private JLabel recentReviewsLabel;
    
    /**
     * Constructor for the DashboardPanel
     */
    public DashboardPanel() {
        userDAO = new UserDAO();
        propertyDAO = new PropertyDAO();
        bookingDAO = new BookingDAO();
        reviewDAO = new ReviewDAO();
        
        initComponents();
        loadDashboardData();
    }
    
    /**
     * Initialize the components of the panel
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create stat cards
        JPanel usersCard = createStatCard("Total Users", "0");
        totalUsersLabel = (JLabel) ((JPanel) usersCard.getComponent(1)).getComponent(0);
        
        JPanel propertiesCard = createStatCard("Total Properties", "0");
        totalPropertiesLabel = (JLabel) ((JPanel) propertiesCard.getComponent(1)).getComponent(0);
        
        JPanel bookingsCard = createStatCard("Total Bookings", "0");
        totalBookingsLabel = (JLabel) ((JPanel) bookingsCard.getComponent(1)).getComponent(0);
        
        JPanel pendingBookingsCard = createStatCard("Pending Bookings", "0");
        pendingBookingsLabel = (JLabel) ((JPanel) pendingBookingsCard.getComponent(1)).getComponent(0);
        
        JPanel recentReviewsCard = createStatCard("Recent Reviews", "0");
        recentReviewsLabel = (JLabel) ((JPanel) recentReviewsCard.getComponent(1)).getComponent(0);
        
        statsPanel.add(usersCard);
        statsPanel.add(propertiesCard);
        statsPanel.add(bookingsCard);
        statsPanel.add(pendingBookingsCard);
        statsPanel.add(recentReviewsCard);
        
        // Create recent activity panel
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        
        JTextArea activityTextArea = new JTextArea();
        activityTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(activityTextArea);
        activityPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(activityPanel, BorderLayout.CENTER);
        
        // Add components to the panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadDashboardData());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create a stat card with title and value
     * @param title The title of the stat
     * @param value The value of the stat
     * @return The created stat card panel
     */
    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.add(valueLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Load dashboard data from the database
     */
    private void loadDashboardData() {
        // Get counts
        List<User> users = userDAO.getAllUsers();
        List<Property> properties = propertyDAO.getAllProperties();
        List<Booking> bookings = bookingDAO.getAllBookings();
        List<Booking> pendingBookings = bookingDAO.getBookingsByStatus(Booking.BookingStatus.PENDING);
        List<Review> reviews = reviewDAO.getAllReviews();
        
        // Update labels
        totalUsersLabel.setText(String.valueOf(users.size()));
        totalPropertiesLabel.setText(String.valueOf(properties.size()));
        totalBookingsLabel.setText(String.valueOf(bookings.size()));
        pendingBookingsLabel.setText(String.valueOf(pendingBookings.size()));
        recentReviewsLabel.setText(String.valueOf(reviews.size()));
    }
} 