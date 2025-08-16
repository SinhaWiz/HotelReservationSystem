package com.hotel.dao;

import com.hotel.model.VIPMember;
import com.hotel.model.Customer;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for VIP Member operations
 */
public class VIPMemberDAO {
    
    /**
     * Create a new VIP member
     */
    public int createVIPMember(VIPMember vipMember) throws SQLException {
        String sql = "INSERT INTO vip_members (vip_id, customer_id, membership_level, " +
                    "discount_percentage, membership_start_date, membership_end_date, benefits) " +
                    "VALUES (vip_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, new String[]{"vip_id"});
            
            pstmt.setInt(1, vipMember.getCustomerId());
            pstmt.setString(2, vipMember.getMembershipLevelString());
            pstmt.setDouble(3, vipMember.getDiscountPercentage());
            
            if (vipMember.getMembershipStartDate() != null) {
                pstmt.setDate(4, new Date(vipMember.getMembershipStartDate().getTime()));
            } else {
                pstmt.setDate(4, new Date(System.currentTimeMillis()));
            }
            
            if (vipMember.getMembershipEndDate() != null) {
                pstmt.setDate(5, new Date(vipMember.getMembershipEndDate().getTime()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            
            pstmt.setString(6, vipMember.getBenefits());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int vipId = rs.getInt(1);
                    vipMember.setVipId(vipId);
                    return vipId;
                }
            }
            
            return 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get all VIP members with detailed information using cursor procedure
     */
    public List<VIPMember> getAllVIPMembersDetailed(String membershipLevel) throws SQLException {
        String sql = "{call get_vip_members_detailed(?, ?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;
        List<VIPMember> vipMembers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            if (membershipLevel != null && !membershipLevel.trim().isEmpty()) {
                cstmt.setString(1, membershipLevel);
            } else {
                cstmt.setNull(1, Types.VARCHAR);
            }
            
            cstmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            
            cstmt.execute();
            
            rs = (ResultSet) cstmt.getObject(2);
            
            while (rs.next()) {
                vipMembers.add(mapCursorResultToVIPMember(rs));
            }
            
            return vipMembers;
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt, rs);
        }
    }
    
    /**
     * Find VIP member by customer ID
     */
    public VIPMember findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT vm.vip_id, vm.customer_id, vm.membership_level, " +
                    "vm.discount_percentage, vm.membership_start_date, vm.membership_end_date, " +
                    "vm.benefits, vm.is_active, " +
                    "c.first_name, c.last_name, c.email, c.phone, c.total_spent, c.loyalty_points " +
                    "FROM vip_members vm " +
                    "JOIN customers c ON vm.customer_id = c.customer_id " +
                    "WHERE vm.customer_id = ? AND vm.is_active = 'Y'";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVIPMember(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Find VIP member by VIP ID
     */
    public VIPMember findById(int vipId) throws SQLException {
        String sql = "SELECT vm.vip_id, vm.customer_id, vm.membership_level, " +
                    "vm.discount_percentage, vm.membership_start_date, vm.membership_end_date, " +
                    "vm.benefits, vm.is_active, " +
                    "c.first_name, c.last_name, c.email, c.phone, c.total_spent, c.loyalty_points " +
                    "FROM vip_members vm " +
                    "JOIN customers c ON vm.customer_id = c.customer_id " +
                    "WHERE vm.vip_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vipId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVIPMember(rs);
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Get all active VIP members
     */
    public List<VIPMember> findAllActive() throws SQLException {
        String sql = "SELECT vm.vip_id, vm.customer_id, vm.membership_level, " +
                    "vm.discount_percentage, vm.membership_start_date, vm.membership_end_date, " +
                    "vm.benefits, vm.is_active, " +
                    "c.first_name, c.last_name, c.email, c.phone, c.total_spent, c.loyalty_points " +
                    "FROM vip_members vm " +
                    "JOIN customers c ON vm.customer_id = c.customer_id " +
                    "WHERE vm.is_active = 'Y' " +
                    "AND (vm.membership_end_date IS NULL OR vm.membership_end_date >= SYSDATE) " +
                    "ORDER BY vm.membership_level, c.total_spent DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<VIPMember> vipMembers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vipMembers.add(mapResultSetToVIPMember(rs));
            }
            
            return vipMembers;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Update VIP member information
     */
    public boolean updateVIPMember(VIPMember vipMember) throws SQLException {
        String sql = "UPDATE vip_members SET membership_level = ?, discount_percentage = ?, " +
                    "membership_end_date = ?, benefits = ? WHERE vip_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, vipMember.getMembershipLevelString());
            pstmt.setDouble(2, vipMember.getDiscountPercentage());
            
            if (vipMember.getMembershipEndDate() != null) {
                pstmt.setDate(3, new Date(vipMember.getMembershipEndDate().getTime()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            pstmt.setString(4, vipMember.getBenefits());
            pstmt.setInt(5, vipMember.getVipId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Deactivate VIP membership
     */
    public boolean deactivateVIPMember(int vipId) throws SQLException {
        String sql = "UPDATE vip_members SET is_active = 'N', membership_end_date = SYSDATE " +
                    "WHERE vip_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vipId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
    }
    
    /**
     * Check VIP eligibility using Oracle function
     */
    public String checkVIPEligibility(int customerId) throws SQLException {
        String sql = "{? = call check_vip_eligibility(?)}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            
            cstmt.registerOutParameter(1, Types.VARCHAR);
            cstmt.setInt(2, customerId);
            
            cstmt.execute();
            
            return cstmt.getString(1);
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Get VIP members by membership level
     */
    public List<VIPMember> findByMembershipLevel(VIPMember.MembershipLevel level) throws SQLException {
        String sql = "SELECT vm.vip_id, vm.customer_id, vm.membership_level, " +
                    "vm.discount_percentage, vm.membership_start_date, vm.membership_end_date, " +
                    "vm.benefits, vm.is_active, " +
                    "c.first_name, c.last_name, c.email, c.phone, c.total_spent, c.loyalty_points " +
                    "FROM vip_members vm " +
                    "JOIN customers c ON vm.customer_id = c.customer_id " +
                    "WHERE vm.membership_level = ? AND vm.is_active = 'Y' " +
                    "ORDER BY c.total_spent DESC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<VIPMember> vipMembers = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, level.toString());
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vipMembers.add(mapResultSetToVIPMember(rs));
            }
            
            return vipMembers;
            
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Process VIP renewals using stored procedure
     */
    public void processVIPRenewals() throws SQLException {
        String sql = "{call process_vip_renewals}";
        
        Connection conn = null;
        CallableStatement cstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            cstmt = conn.prepareCall(sql);
            cstmt.execute();
            
        } finally {
            DatabaseConnection.closeResources(conn, cstmt);
        }
    }
    
    /**
     * Find all VIP members
     */
    public List<VIPMember> findAll() throws SQLException {
        String sql = "SELECT v.*, c.* FROM vip_members v " +
                    "JOIN customers c ON v.customer_id = c.customer_id " +
                    "WHERE v.is_active = 'Y'";
        List<VIPMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapResultSetToVIPMember(rs));
            }
        }
        return members;
    }

    /**
     * Update VIP member information (overloaded)
     */
    public void update(VIPMember member) throws SQLException {
        String sql = "UPDATE vip_members SET membership_level = ?, discount_percentage = ?, " +
                    "membership_end_date = ?, benefits = ?, is_active = ? WHERE vip_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getMembershipLevel().name());
            pstmt.setDouble(2, member.getDiscountPercentage());
            pstmt.setDate(3, new java.sql.Date(member.getMembershipEndDate().getTime()));
            pstmt.setString(4, member.getBenefits());
            pstmt.setString(5, member.isActive() ? "Y" : "N");
            pstmt.setInt(6, member.getVipId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Save VIP member (insert or update)
     */
    public void save(VIPMember member) throws SQLException {
        if (member.getVipId() == 0) {
            createVIPMember(member);
        } else {
            update(member);
        }
    }

    /**
     * Get VIP members by membership level with details
     */
    public List<VIPMember> findByLevelWithDetails(VIPMember.MembershipLevel level) throws SQLException {
        String sql = "SELECT v.*, c.* FROM vip_members v " +
                    "JOIN customers c ON v.customer_id = c.customer_id " +
                    "WHERE v.is_active = 'Y' AND v.membership_level = ?";
        List<VIPMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, level.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToVIPMember(rs));
                }
            }
        }
        return members;
    }

    /**
     * Get all active VIP members with additional details
     */
    public List<VIPMember> findAllWithDetails() throws SQLException {
        String sql = "SELECT v.*, c.*, " +
                    "(SELECT COUNT(*) FROM bookings b WHERE b.customer_id = c.customer_id) as booking_count " +
                    "FROM vip_members v " +
                    "JOIN customers c ON v.customer_id = c.customer_id " +
                    "WHERE v.is_active = 'Y'";
        List<VIPMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapResultSetToVIPMemberWithDetails(rs));
            }
        }
        return members;
    }

    /**
     * Map ResultSet to VIPMember object
     */
    private VIPMember mapResultSetToVIPMember(ResultSet rs) throws SQLException {
        VIPMember vipMember = new VIPMember();
        
        vipMember.setVipId(rs.getInt("vip_id"));
        vipMember.setCustomerId(rs.getInt("customer_id"));
        vipMember.setMembershipLevelFromString(rs.getString("membership_level"));
        vipMember.setDiscountPercentage(rs.getDouble("discount_percentage"));
        
        Date startDate = rs.getDate("membership_start_date");
        if (startDate != null) {
            vipMember.setMembershipStartDate(new java.util.Date(startDate.getTime()));
        }
        
        Date endDate = rs.getDate("membership_end_date");
        if (endDate != null) {
            vipMember.setMembershipEndDate(new java.util.Date(endDate.getTime()));
        }
        
        vipMember.setBenefits(rs.getString("benefits"));
        vipMember.setActive("Y".equals(rs.getString("is_active")));
        
        // Add customer details
        Customer customer = new Customer();
        customer.setCustomerId(vipMember.getCustomerId());
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setTotalSpent(rs.getDouble("total_spent"));
        customer.setLoyaltyPoints(rs.getInt("loyalty_points"));
        vipMember.setCustomer(customer);
        
        return vipMember;
    }
    
    /**
     * Map cursor result to VIPMember object (for stored procedure results)
     */
    private VIPMember mapCursorResultToVIPMember(ResultSet rs) throws SQLException {
        VIPMember vipMember = new VIPMember();
        
        vipMember.setVipId(rs.getInt("vip_id"));
        vipMember.setCustomerId(rs.getInt("customer_id"));
        vipMember.setMembershipLevelFromString(rs.getString("membership_level"));
        vipMember.setDiscountPercentage(rs.getDouble("discount_percentage"));
        
        Date startDate = rs.getDate("membership_start_date");
        if (startDate != null) {
            vipMember.setMembershipStartDate(new java.util.Date(startDate.getTime()));
        }
        
        Date endDate = rs.getDate("membership_end_date");
        if (endDate != null) {
            vipMember.setMembershipEndDate(new java.util.Date(endDate.getTime()));
        }
        
        vipMember.setBenefits(rs.getString("benefits"));
        vipMember.setActive(true); // From cursor, only active members are returned
        
        // Add customer details
        Customer customer = new Customer();
        customer.setCustomerId(vipMember.getCustomerId());
        customer.setFirstName(rs.getString("customer_name").split(" ")[0]);
        if (rs.getString("customer_name").split(" ").length > 1) {
            customer.setLastName(rs.getString("customer_name").split(" ")[1]);
        }
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setTotalSpent(rs.getDouble("total_spent"));
        customer.setLoyaltyPoints(rs.getInt("loyalty_points"));
        vipMember.setCustomer(customer);
        
        return vipMember;
    }

    /**
     * Map ResultSet to VIPMember object with additional details (e.g., booking count)
     */
    private VIPMember mapResultSetToVIPMemberWithDetails(ResultSet rs) throws SQLException {
        VIPMember member = mapResultSetToVIPMember(rs);
        member.setBookingCount(rs.getInt("booking_count"));
        return member;
    }

    /**
     * Promote top customers to VIP status using stored procedure
     */
    public void promoteTopCustomersToVIP(String promotedBy) throws SQLException {
        String sql = "{call promote_top_customers_to_vip(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, promotedBy);
            cstmt.execute();
        }
    }
}
