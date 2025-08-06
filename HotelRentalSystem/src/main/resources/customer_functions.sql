-- Hotel Management System - Customer Functions
-- Oracle Database Functions for Customer Management and Discount Calculations

-- Function to calculate customer discount based on total spending
CREATE OR REPLACE FUNCTION calculate_customer_discount(
    p_customer_id IN NUMBER
) RETURN NUMBER AS
    v_total_spent NUMBER(12,2);
    v_discount_percentage NUMBER(5,2) := 0;
    v_is_vip NUMBER;
    v_vip_discount NUMBER(5,2);
BEGIN
    -- Get customer's total spending
    SELECT total_spent INTO v_total_spent
    FROM customers
    WHERE customer_id = p_customer_id;
    
    -- Check if customer is already VIP
    SELECT COUNT(*), NVL(MAX(discount_percentage), 0)
    INTO v_is_vip, v_vip_discount
    FROM vip_members
    WHERE customer_id = p_customer_id
    AND is_active = 'Y'
    AND (membership_end_date IS NULL OR membership_end_date >= SYSDATE);
    
    -- If already VIP, return VIP discount
    IF v_is_vip > 0 THEN
        RETURN v_vip_discount;
    END IF;
    
    -- Calculate discount based on spending thresholds
    IF v_total_spent >= 10000 THEN
        v_discount_percentage := 15.0; -- 15% for spending over $10,000
    ELSIF v_total_spent >= 5000 THEN
        v_discount_percentage := 10.0; -- 10% for spending over $5,000
    ELSIF v_total_spent >= 2000 THEN
        v_discount_percentage := 5.0;  -- 5% for spending over $2,000
    END IF;
    
    RETURN v_discount_percentage;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END calculate_customer_discount;
/

-- Function to check VIP eligibility based on spending threshold
CREATE OR REPLACE FUNCTION check_vip_eligibility(
    p_customer_id IN NUMBER,
    p_spending_threshold IN NUMBER DEFAULT 5000
) RETURN VARCHAR2 AS
    v_total_spent NUMBER(12,2);
    v_is_already_vip NUMBER;
    v_recommended_level VARCHAR2(20);
BEGIN
    -- Get customer's total spending
    SELECT total_spent INTO v_total_spent
    FROM customers
    WHERE customer_id = p_customer_id;
    
    -- Check if customer is already VIP
    SELECT COUNT(*) INTO v_is_already_vip
    FROM vip_members
    WHERE customer_id = p_customer_id
    AND is_active = 'Y';
    
    -- If already VIP, return current status
    IF v_is_already_vip > 0 THEN
        RETURN 'ALREADY_VIP';
    END IF;
    
    -- Determine recommended VIP level based on spending
    IF v_total_spent >= 15000 THEN
        v_recommended_level := 'DIAMOND';
    ELSIF v_total_spent >= 8000 THEN
        v_recommended_level := 'PLATINUM';
    ELSIF v_total_spent >= p_spending_threshold THEN
        v_recommended_level := 'GOLD';
    ELSE
        v_recommended_level := 'NOT_ELIGIBLE';
    END IF;
    
    RETURN v_recommended_level;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'CUSTOMER_NOT_FOUND';
    WHEN OTHERS THEN
        RETURN 'ERROR';
END check_vip_eligibility;
/

-- Function to calculate loyalty points based on spending
CREATE OR REPLACE FUNCTION calculate_loyalty_points(
    p_amount_spent IN NUMBER
) RETURN NUMBER AS
    v_points NUMBER(10);
BEGIN
    -- Calculate points: 1 point per dollar spent, bonus for higher amounts
    v_points := FLOOR(p_amount_spent);
    
    -- Bonus points for higher spending
    IF p_amount_spent >= 1000 THEN
        v_points := v_points + FLOOR(p_amount_spent * 0.1); -- 10% bonus
    ELSIF p_amount_spent >= 500 THEN
        v_points := v_points + FLOOR(p_amount_spent * 0.05); -- 5% bonus
    END IF;
    
    RETURN v_points;
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END calculate_loyalty_points;
/

-- Function to get room occupancy rate
CREATE OR REPLACE FUNCTION get_room_occupancy_rate(
    p_start_date IN DATE DEFAULT SYSDATE - 30,
    p_end_date IN DATE DEFAULT SYSDATE
) RETURN NUMBER AS
    v_total_rooms NUMBER;
    v_occupied_room_nights NUMBER;
    v_total_possible_nights NUMBER;
    v_occupancy_rate NUMBER(5,2);
BEGIN
    -- Get total number of rooms
    SELECT COUNT(*) INTO v_total_rooms
    FROM rooms
    WHERE status != 'MAINTENANCE';
    
    -- Calculate total possible room nights
    v_total_possible_nights := v_total_rooms * (p_end_date - p_start_date);
    
    -- Calculate occupied room nights
    SELECT NVL(SUM(
        CASE 
            WHEN actual_check_out IS NOT NULL THEN
                LEAST(actual_check_out, p_end_date) - GREATEST(GREATEST(actual_check_in, check_in_date), p_start_date)
            WHEN booking_status = 'CHECKED_IN' THEN
                p_end_date - GREATEST(GREATEST(actual_check_in, check_in_date), p_start_date)
            ELSE 0
        END
    ), 0) INTO v_occupied_room_nights
    FROM bookings
    WHERE booking_status IN ('CHECKED_IN', 'CHECKED_OUT')
    AND (
        (actual_check_in IS NOT NULL AND actual_check_in <= p_end_date)
        OR (check_in_date <= p_end_date)
    )
    AND (
        (actual_check_out IS NULL OR actual_check_out >= p_start_date)
        OR (check_out_date >= p_start_date)
    );
    
    -- Calculate occupancy rate as percentage
    IF v_total_possible_nights > 0 THEN
        v_occupancy_rate := (v_occupied_room_nights / v_total_possible_nights) * 100;
    ELSE
        v_occupancy_rate := 0;
    END IF;
    
    RETURN ROUND(v_occupancy_rate, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_room_occupancy_rate;
/

-- Function to get customer booking history count
CREATE OR REPLACE FUNCTION get_customer_booking_count(
    p_customer_id IN NUMBER,
    p_status IN VARCHAR2 DEFAULT 'ALL'
) RETURN NUMBER AS
    v_count NUMBER;
BEGIN
    IF p_status = 'ALL' THEN
        SELECT COUNT(*) INTO v_count
        FROM bookings
        WHERE customer_id = p_customer_id;
    ELSE
        SELECT COUNT(*) INTO v_count
        FROM bookings
        WHERE customer_id = p_customer_id
        AND booking_status = p_status;
    END IF;
    
    RETURN v_count;
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_customer_booking_count;
/

-- Function to calculate average room rate
CREATE OR REPLACE FUNCTION get_average_room_rate(
    p_room_type_id IN NUMBER DEFAULT NULL,
    p_start_date IN DATE DEFAULT SYSDATE - 30,
    p_end_date IN DATE DEFAULT SYSDATE
) RETURN NUMBER AS
    v_avg_rate NUMBER(10,2);
BEGIN
    IF p_room_type_id IS NULL THEN
        -- Calculate average for all room types
        SELECT NVL(AVG(total_amount / (check_out_date - check_in_date)), 0)
        INTO v_avg_rate
        FROM bookings
        WHERE booking_date BETWEEN p_start_date AND p_end_date
        AND booking_status != 'CANCELLED'
        AND check_out_date > check_in_date;
    ELSE
        -- Calculate average for specific room type
        SELECT NVL(AVG(b.total_amount / (b.check_out_date - b.check_in_date)), 0)
        INTO v_avg_rate
        FROM bookings b
        JOIN rooms r ON b.room_id = r.room_id
        WHERE r.type_id = p_room_type_id
        AND b.booking_date BETWEEN p_start_date AND p_end_date
        AND b.booking_status != 'CANCELLED'
        AND b.check_out_date > b.check_in_date;
    END IF;
    
    RETURN ROUND(v_avg_rate, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_average_room_rate;
/

