-- Hotel Management System - Cursor-based Procedures
-- Oracle Database Procedures using Cursors for Complex Queries

-- Procedure to generate detailed revenue report using cursor
CREATE OR REPLACE PROCEDURE generate_revenue_report(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_report_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_report_cursor FOR
        WITH revenue_data AS (
            SELECT 
                rt.type_name,
                r.room_number,
                b.booking_id,
                c.first_name || ' ' || c.last_name AS customer_name,
                b.check_in_date,
                b.check_out_date,
                b.total_amount,
                b.discount_applied,
                b.extra_charges,
                b.booking_status,
                CASE WHEN vm.customer_id IS NOT NULL THEN 'VIP' ELSE 'Regular' END AS customer_type
            FROM bookings b
            JOIN customers c ON b.customer_id = c.customer_id
            JOIN rooms r ON b.room_id = r.room_id
            JOIN room_types rt ON r.type_id = rt.type_id
            LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y'
            WHERE b.booking_date BETWEEN p_start_date AND p_end_date
            AND b.booking_status != 'CANCELLED'
        )
        SELECT 
            type_name,
            COUNT(*) AS total_bookings,
            SUM(total_amount) AS total_revenue,
            SUM(discount_applied) AS total_discounts,
            SUM(extra_charges) AS total_extra_charges,
            AVG(total_amount) AS avg_booking_value,
            COUNT(CASE WHEN customer_type = 'VIP' THEN 1 END) AS vip_bookings,
            COUNT(CASE WHEN customer_type = 'Regular' THEN 1 END) AS regular_bookings
        FROM revenue_data
        GROUP BY type_name
        ORDER BY total_revenue DESC;
END generate_revenue_report;
/

-- Procedure to get VIP members list with detailed information using cursor
CREATE OR REPLACE PROCEDURE get_vip_members_detailed(
    p_membership_level IN VARCHAR2 DEFAULT NULL,
    p_vip_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_vip_cursor FOR
        SELECT 
            vm.vip_id,
            c.customer_id,
            c.first_name || ' ' || c.last_name AS customer_name,
            c.email,
            c.phone,
            c.total_spent,
            c.loyalty_points,
            vm.membership_level,
            vm.discount_percentage,
            vm.membership_start_date,
            vm.membership_end_date,
            vm.benefits,
            COUNT(b.booking_id) AS total_bookings,
            NVL(SUM(b.total_amount), 0) AS total_booking_value,
            MAX(b.booking_date) AS last_booking_date
        FROM vip_members vm
        JOIN customers c ON vm.customer_id = c.customer_id
        LEFT JOIN bookings b ON c.customer_id = b.customer_id 
            AND b.booking_status != 'CANCELLED'
        WHERE vm.is_active = 'Y'
        AND (p_membership_level IS NULL OR vm.membership_level = p_membership_level)
        GROUP BY 
            vm.vip_id, c.customer_id, c.first_name, c.last_name, c.email, c.phone,
            c.total_spent, c.loyalty_points, vm.membership_level, vm.discount_percentage,
            vm.membership_start_date, vm.membership_end_date, vm.benefits
        ORDER BY c.total_spent DESC, vm.membership_start_date DESC;
END get_vip_members_detailed;
/

-- Procedure to get currently reserved rooms with customer details
CREATE OR REPLACE PROCEDURE get_current_reservations(
    p_reservation_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_reservation_cursor FOR
        SELECT 
            b.booking_id,
            r.room_number,
            rt.type_name AS room_type,
            c.first_name || ' ' || c.last_name AS customer_name,
            c.email,
            c.phone,
            b.check_in_date,
            b.check_out_date,
            b.actual_check_in,
            b.total_amount,
            b.booking_status,
            b.special_requests,
            CASE WHEN vm.customer_id IS NOT NULL THEN 'VIP (' || vm.membership_level || ')' ELSE 'Regular' END AS customer_type,
            CASE 
                WHEN b.check_in_date > SYSDATE THEN 'Future Reservation'
                WHEN b.check_in_date <= SYSDATE AND b.check_out_date >= SYSDATE THEN 'Current Stay'
                ELSE 'Past Due'
            END AS reservation_status
        FROM bookings b
        JOIN customers c ON b.customer_id = c.customer_id
        JOIN rooms r ON b.room_id = r.room_id
        JOIN room_types rt ON r.type_id = rt.type_id
        LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y'
        WHERE b.booking_status IN ('CONFIRMED', 'CHECKED_IN')
        ORDER BY b.check_in_date, r.room_number;
END get_current_reservations;
/

-- Procedure to analyze customer spending patterns using cursor
CREATE OR REPLACE PROCEDURE analyze_customer_patterns(
    p_analysis_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_analysis_cursor FOR
        WITH customer_stats AS (
            SELECT 
                c.customer_id,
                c.first_name || ' ' || c.last_name AS customer_name,
                c.email,
                c.registration_date,
                c.total_spent,
                c.loyalty_points,
                COUNT(b.booking_id) AS total_bookings,
                COUNT(CASE WHEN b.booking_status = 'CHECKED_OUT' THEN 1 END) AS completed_bookings,
                COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled_bookings,
                AVG(b.total_amount) AS avg_booking_value,
                MIN(b.booking_date) AS first_booking_date,
                MAX(b.booking_date) AS last_booking_date,
                CASE WHEN vm.customer_id IS NOT NULL THEN vm.membership_level ELSE 'Regular' END AS customer_type
            FROM customers c
            LEFT JOIN bookings b ON c.customer_id = b.customer_id
            LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y'
            WHERE c.is_active = 'Y'
            GROUP BY 
                c.customer_id, c.first_name, c.last_name, c.email, c.registration_date,
                c.total_spent, c.loyalty_points, vm.membership_level
        )
        SELECT 
            customer_name,
            email,
            customer_type,
            total_spent,
            loyalty_points,
            total_bookings,
            completed_bookings,
            cancelled_bookings,
            ROUND(avg_booking_value, 2) AS avg_booking_value,
            first_booking_date,
            last_booking_date,
            CASE 
                WHEN last_booking_date IS NULL THEN 'No Bookings'
                WHEN last_booking_date < ADD_MONTHS(SYSDATE, -6) THEN 'Inactive (6+ months)'
                WHEN last_booking_date < ADD_MONTHS(SYSDATE, -3) THEN 'Low Activity (3-6 months)'
                ELSE 'Active'
            END AS activity_status,
            CASE 
                WHEN total_spent >= 15000 THEN 'High Value'
                WHEN total_spent >= 5000 THEN 'Medium Value'
                WHEN total_spent >= 1000 THEN 'Low Value'
                ELSE 'New Customer'
            END AS value_segment
        FROM customer_stats
        ORDER BY total_spent DESC, last_booking_date DESC;
END analyze_customer_patterns;
/

-- Procedure to get room utilization statistics using cursor
CREATE OR REPLACE PROCEDURE get_room_utilization_stats(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_stats_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_stats_cursor FOR
        WITH room_stats AS (
            SELECT 
                r.room_id,
                r.room_number,
                rt.type_name,
                rt.base_price,
                COUNT(b.booking_id) AS total_bookings,
                COUNT(CASE WHEN b.booking_status = 'CHECKED_OUT' THEN 1 END) AS completed_stays,
                COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled_bookings,
                NVL(SUM(CASE WHEN b.booking_status = 'CHECKED_OUT' THEN b.total_amount ELSE 0 END), 0) AS total_revenue,
                NVL(SUM(CASE WHEN b.booking_status = 'CHECKED_OUT' THEN (b.check_out_date - b.check_in_date) ELSE 0 END), 0) AS total_nights_occupied,
                AVG(CASE WHEN b.booking_status = 'CHECKED_OUT' THEN b.total_amount ELSE NULL END) AS avg_booking_value
            FROM rooms r
            JOIN room_types rt ON r.type_id = rt.type_id
            LEFT JOIN bookings b ON r.room_id = b.room_id 
                AND b.booking_date BETWEEN p_start_date AND p_end_date
            GROUP BY r.room_id, r.room_number, rt.type_name, rt.base_price
        )
        SELECT 
            room_number,
            type_name,
            base_price,
            total_bookings,
            completed_stays,
            cancelled_bookings,
            ROUND(total_revenue, 2) AS total_revenue,
            total_nights_occupied,
            ROUND(avg_booking_value, 2) AS avg_booking_value,
            ROUND((total_nights_occupied / GREATEST((p_end_date - p_start_date), 1)) * 100, 2) AS occupancy_rate_percent,
            CASE 
                WHEN total_nights_occupied = 0 THEN 'Unused'
                WHEN (total_nights_occupied / GREATEST((p_end_date - p_start_date), 1)) >= 0.8 THEN 'High Utilization'
                WHEN (total_nights_occupied / GREATEST((p_end_date - p_start_date), 1)) >= 0.5 THEN 'Medium Utilization'
                ELSE 'Low Utilization'
            END AS utilization_category
        FROM room_stats
        ORDER BY total_revenue DESC, occupancy_rate_percent DESC;
END get_room_utilization_stats;
/

-- Procedure to process monthly VIP membership renewals using cursor
CREATE OR REPLACE PROCEDURE process_vip_renewals AS
    CURSOR vip_renewal_cursor IS
        SELECT 
            vm.vip_id,
            vm.customer_id,
            c.first_name || ' ' || c.last_name AS customer_name,
            vm.membership_level,
            vm.membership_end_date,
            c.total_spent
        FROM vip_members vm
        JOIN customers c ON vm.customer_id = c.customer_id
        WHERE vm.is_active = 'Y'
        AND vm.membership_end_date IS NOT NULL
        AND vm.membership_end_date <= SYSDATE + 30  -- Expiring within 30 days
        ORDER BY vm.membership_end_date;
    
    v_renewal_count NUMBER := 0;
    v_expiry_count NUMBER := 0;
BEGIN
    -- Process each VIP member
    FOR vip_rec IN vip_renewal_cursor LOOP
        -- Check if customer still qualifies for VIP based on spending
        IF check_vip_eligibility(vip_rec.customer_id) != 'NOT_ELIGIBLE' THEN
            -- Renew membership for another year
            UPDATE vip_members 
            SET membership_end_date = ADD_MONTHS(SYSDATE, 12)
            WHERE vip_id = vip_rec.vip_id;
            
            v_renewal_count := v_renewal_count + 1;
            
            -- Log renewal
            INSERT INTO audit_log (
                log_id, table_name, operation, record_id,
                new_values, changed_by, change_date
            ) VALUES (
                audit_seq.NEXTVAL, 'VIP_MEMBERS', 'RENEWAL', vip_rec.customer_id,
                'VIP membership renewed for: ' || vip_rec.customer_name || 
                ', Level: ' || vip_rec.membership_level,
                'SYSTEM', SYSDATE
            );
        ELSE
            -- Expire membership
            UPDATE vip_members 
            SET is_active = 'N',
                membership_end_date = SYSDATE
            WHERE vip_id = vip_rec.vip_id;
            
            v_expiry_count := v_expiry_count + 1;
            
            -- Log expiry
            INSERT INTO audit_log (
                log_id, table_name, operation, record_id,
                new_values, changed_by, change_date
            ) VALUES (
                audit_seq.NEXTVAL, 'VIP_MEMBERS', 'EXPIRY', vip_rec.customer_id,
                'VIP membership expired for: ' || vip_rec.customer_name || 
                ', Reason: Insufficient spending',
                'SYSTEM', SYSDATE
            );
        END IF;
    END LOOP;
    
    -- Log summary
    INSERT INTO audit_log (
        log_id, table_name, operation, record_id,
        new_values, changed_by, change_date
    ) VALUES (
        audit_seq.NEXTVAL, 'VIP_MEMBERS', 'BATCH_PROCESS', 0,
        'VIP renewal process completed. Renewed: ' || v_renewal_count || 
        ', Expired: ' || v_expiry_count,
        'SYSTEM', SYSDATE
    );
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        -- Log error
        INSERT INTO audit_log (
            log_id, table_name, operation, record_id,
            new_values, changed_by, change_date
        ) VALUES (
            audit_seq.NEXTVAL, 'VIP_MEMBERS', 'ERROR', 0,
            'VIP renewal process failed: ' || SQLERRM,
            'SYSTEM', SYSDATE
        );
        COMMIT;
        RAISE;
END process_vip_renewals;
/

