-- Enhanced Hotel Management System Functions
-- Additional functions for new features

-- Function to check if customer is blacklisted
CREATE OR REPLACE FUNCTION is_customer_blacklisted(
    p_customer_id IN NUMBER
) RETURN CHAR
AS
    v_is_blacklisted CHAR(1) := 'N';
BEGIN
    SELECT CASE 
        WHEN COUNT(*) > 0 THEN 'Y' 
        ELSE 'N' 
    END
    INTO v_is_blacklisted
    FROM blacklisted_customers
    WHERE customer_id = p_customer_id
    AND is_active = 'Y'
    AND (expiry_date IS NULL OR expiry_date > SYSDATE);
    
    RETURN v_is_blacklisted;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'N';
END is_customer_blacklisted;
/

-- Function to calculate total service cost for a customer
CREATE OR REPLACE FUNCTION calculate_customer_service_total(
    p_customer_id IN NUMBER,
    p_booking_id IN NUMBER DEFAULT NULL
) RETURN NUMBER
AS
    v_total_cost NUMBER(12,2) := 0;
BEGIN
    IF p_booking_id IS NOT NULL THEN
        -- Calculate for specific booking
        SELECT NVL(SUM(total_cost), 0)
        INTO v_total_cost
        FROM customer_service_usage
        WHERE customer_id = p_customer_id
        AND booking_id = p_booking_id;
    ELSE
        -- Calculate for all bookings
        SELECT NVL(SUM(total_cost), 0)
        INTO v_total_cost
        FROM customer_service_usage
        WHERE customer_id = p_customer_id;
    END IF;
    
    RETURN v_total_cost;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END calculate_customer_service_total;
/

-- Function to get customer's grand total spending (bookings + services)
CREATE OR REPLACE FUNCTION get_customer_grand_total(
    p_customer_id IN NUMBER
) RETURN NUMBER
AS
    v_booking_total NUMBER(12,2) := 0;
    v_service_total NUMBER(12,2) := 0;
    v_grand_total NUMBER(12,2) := 0;
BEGIN
    -- Get booking total from customer record
    SELECT NVL(total_spent, 0)
    INTO v_booking_total
    FROM customers
    WHERE customer_id = p_customer_id;
    
    -- Get service total
    v_service_total := calculate_customer_service_total(p_customer_id);
    
    v_grand_total := v_booking_total + v_service_total;
    
    RETURN v_grand_total;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_customer_grand_total;
/

-- Function to determine VIP level based on spending
CREATE OR REPLACE FUNCTION determine_vip_level(
    p_total_spending IN NUMBER
) RETURN VARCHAR2
AS
BEGIN
    IF p_total_spending >= 15000 THEN
        RETURN 'DIAMOND';
    ELSIF p_total_spending >= 10000 THEN
        RETURN 'PLATINUM';
    ELSIF p_total_spending >= 5000 THEN
        RETURN 'GOLD';
    ELSE
        RETURN NULL;
    END IF;
END determine_vip_level;
/

-- Function to check if service is available for room type
CREATE OR REPLACE FUNCTION is_service_available_for_room(
    p_room_id IN NUMBER,
    p_service_id IN NUMBER
) RETURN CHAR
AS
    v_available CHAR(1) := 'N';
BEGIN
    SELECT CASE 
        WHEN COUNT(*) > 0 THEN 'Y' 
        ELSE 'N' 
    END
    INTO v_available
    FROM room_service_assignments rsa
    JOIN rooms r ON rsa.room_type_id = r.room_type_id
    WHERE r.room_id = p_room_id
    AND rsa.service_id = p_service_id;
    
    RETURN v_available;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'N';
END is_service_available_for_room;
/

-- Function to calculate late checkout charges
CREATE OR REPLACE FUNCTION calculate_late_checkout_charge(
    p_checkout_date IN DATE,
    p_actual_checkout_date IN DATE,
    p_hourly_rate IN NUMBER DEFAULT 25.00
) RETURN NUMBER
AS
    v_hours_late NUMBER;
    v_charge NUMBER(8,2) := 0;
BEGIN
    IF p_actual_checkout_date > p_checkout_date THEN
        v_hours_late := (p_actual_checkout_date - p_checkout_date) * 24;
        -- Minimum 1 hour charge, round up to next hour
        v_charge := GREATEST(1, CEIL(v_hours_late)) * p_hourly_rate;
    END IF;
    
    RETURN v_charge;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END calculate_late_checkout_charge;
/

-- Function to get available rooms count by type
CREATE OR REPLACE FUNCTION get_available_rooms_count(
    p_room_type_id IN NUMBER DEFAULT NULL,
    p_check_in_date IN DATE DEFAULT SYSDATE,
    p_check_out_date IN DATE DEFAULT SYSDATE + 1
) RETURN NUMBER
AS
    v_count NUMBER := 0;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM rooms r
    WHERE r.room_status = 'AVAILABLE'
    AND (p_room_type_id IS NULL OR r.room_type_id = p_room_type_id)
    AND r.room_id NOT IN (
        SELECT DISTINCT b.room_id
        FROM bookings b
        WHERE b.booking_status IN ('CONFIRMED', 'CHECKED_IN')
        AND NOT (b.check_out_date <= p_check_in_date OR b.check_in_date >= p_check_out_date)
    );
    
    RETURN v_count;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_available_rooms_count;
/

-- Function to calculate invoice subtotal
CREATE OR REPLACE FUNCTION calculate_invoice_subtotal(
    p_booking_id IN NUMBER
) RETURN NUMBER
AS
    v_room_total NUMBER(10,2) := 0;
    v_services_total NUMBER(10,2) := 0;
    v_extra_charges NUMBER(8,2) := 0;
    v_subtotal NUMBER(12,2) := 0;
BEGIN
    -- Get booking totals
    SELECT NVL(total_amount, 0) - NVL(extra_charges, 0) - NVL(services_total, 0),
           NVL(services_total, 0),
           NVL(extra_charges, 0)
    INTO v_room_total, v_services_total, v_extra_charges
    FROM bookings
    WHERE booking_id = p_booking_id;
    
    v_subtotal := v_room_total + v_services_total + v_extra_charges;
    
    RETURN v_subtotal;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END calculate_invoice_subtotal;
/

-- Function to get customer ranking by spending
CREATE OR REPLACE FUNCTION get_customer_spending_rank(
    p_customer_id IN NUMBER
) RETURN NUMBER
AS
    v_rank NUMBER := 0;
BEGIN
    SELECT rank
    INTO v_rank
    FROM (
        SELECT customer_id, 
               RANK() OVER (ORDER BY grand_total DESC) as rank
        FROM v_customer_total_spending
        WHERE is_blacklisted = 'N'
    )
    WHERE customer_id = p_customer_id;
    
    RETURN v_rank;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 999999;
END get_customer_spending_rank;
/

-- Function to check if customer is in top N spenders
CREATE OR REPLACE FUNCTION is_customer_top_spender(
    p_customer_id IN NUMBER,
    p_top_n IN NUMBER DEFAULT 5
) RETURN CHAR
AS
    v_rank NUMBER;
BEGIN
    v_rank := get_customer_spending_rank(p_customer_id);
    
    IF v_rank <= p_top_n THEN
        RETURN 'Y';
    ELSE
        RETURN 'N';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'N';
END is_customer_top_spender;
/

-- Function to get room occupancy rate for date range
CREATE OR REPLACE FUNCTION get_room_occupancy_rate_enhanced(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_room_type_id IN NUMBER DEFAULT NULL
) RETURN NUMBER
AS
    v_total_room_days NUMBER := 0;
    v_occupied_days NUMBER := 0;
    v_occupancy_rate NUMBER := 0;
BEGIN
    -- Calculate total possible room days
    SELECT COUNT(*) * (p_end_date - p_start_date)
    INTO v_total_room_days
    FROM rooms r
    WHERE (p_room_type_id IS NULL OR r.room_type_id = p_room_type_id);
    
    -- Calculate occupied days
    SELECT NVL(SUM(LEAST(b.check_out_date, p_end_date) - GREATEST(b.check_in_date, p_start_date)), 0)
    INTO v_occupied_days
    FROM bookings b
    JOIN rooms r ON b.room_id = r.room_id
    WHERE b.booking_status IN ('CHECKED_IN', 'CHECKED_OUT')
    AND b.check_in_date < p_end_date
    AND b.check_out_date > p_start_date
    AND (p_room_type_id IS NULL OR r.room_type_id = p_room_type_id);
    
    IF v_total_room_days > 0 THEN
        v_occupancy_rate := (v_occupied_days / v_total_room_days) * 100;
    END IF;
    
    RETURN ROUND(v_occupancy_rate, 2);
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_room_occupancy_rate_enhanced;
/

-- Function to get service usage count for customer
CREATE OR REPLACE FUNCTION get_customer_service_usage_count(
    p_customer_id IN NUMBER,
    p_service_id IN NUMBER DEFAULT NULL
) RETURN NUMBER
AS
    v_count NUMBER := 0;
BEGIN
    SELECT NVL(SUM(quantity), 0)
    INTO v_count
    FROM customer_service_usage
    WHERE customer_id = p_customer_id
    AND (p_service_id IS NULL OR service_id = p_service_id);
    
    RETURN v_count;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END get_customer_service_usage_count;
/

-- Function to calculate VIP discount amount
CREATE OR REPLACE FUNCTION calculate_vip_discount_amount(
    p_customer_id IN NUMBER,
    p_base_amount IN NUMBER
) RETURN NUMBER
AS
    v_discount_percentage NUMBER(4,2) := 0;
    v_discount_amount NUMBER(10,2) := 0;
BEGIN
    -- Get VIP discount percentage
    SELECT NVL(discount_percentage, 0)
    INTO v_discount_percentage
    FROM vip_members
    WHERE customer_id = p_customer_id
    AND is_active = 'Y';
    
    v_discount_amount := p_base_amount * (v_discount_percentage / 100);
    
    RETURN ROUND(v_discount_amount, 2);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END calculate_vip_discount_amount;
/

-- Function to get next available room of specific type
CREATE OR REPLACE FUNCTION get_next_available_room(
    p_room_type_id IN NUMBER,
    p_check_in_date IN DATE,
    p_check_out_date IN DATE
) RETURN NUMBER
AS
    v_room_id NUMBER := NULL;
BEGIN
    SELECT room_id
    INTO v_room_id
    FROM (
        SELECT r.room_id
        FROM rooms r
        WHERE r.room_type_id = p_room_type_id
        AND r.room_status = 'AVAILABLE'
        AND r.room_id NOT IN (
            SELECT DISTINCT b.room_id
            FROM bookings b
            WHERE b.booking_status IN ('CONFIRMED', 'CHECKED_IN')
            AND NOT (b.check_out_date <= p_check_in_date OR b.check_in_date >= p_check_out_date)
        )
        ORDER BY r.room_number
    )
    WHERE ROWNUM = 1;
    
    RETURN v_room_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
    WHEN OTHERS THEN
        RETURN NULL;
END get_next_available_room;
/

-- Function to format currency for display
CREATE OR REPLACE FUNCTION format_currency_enhanced(
    p_amount IN NUMBER,
    p_currency_symbol IN VARCHAR2 DEFAULT '$'
) RETURN VARCHAR2
AS
BEGIN
    IF p_amount IS NULL THEN
        RETURN p_currency_symbol || '0.00';
    END IF;
    
    RETURN p_currency_symbol || TO_CHAR(p_amount, 'FM999,999,990.00');
EXCEPTION
    WHEN OTHERS THEN
        RETURN p_currency_symbol || '0.00';
END format_currency_enhanced;
/

-- Function to get customer loyalty tier
CREATE OR REPLACE FUNCTION get_customer_loyalty_tier(
    p_customer_id IN NUMBER
) RETURN VARCHAR2
AS
    v_total_spending NUMBER;
    v_loyalty_points NUMBER;
    v_tier VARCHAR2(20);
BEGIN
    SELECT NVL(total_spent, 0), NVL(loyalty_points, 0)
    INTO v_total_spending, v_loyalty_points
    FROM customers
    WHERE customer_id = p_customer_id;
    
    -- Determine tier based on spending and points
    IF v_total_spending >= 20000 OR v_loyalty_points >= 2000 THEN
        v_tier := 'PLATINUM';
    ELSIF v_total_spending >= 10000 OR v_loyalty_points >= 1000 THEN
        v_tier := 'GOLD';
    ELSIF v_total_spending >= 5000 OR v_loyalty_points >= 500 THEN
        v_tier := 'SILVER';
    ELSE
        v_tier := 'BRONZE';
    END IF;
    
    RETURN v_tier;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'BRONZE';
END get_customer_loyalty_tier;
/

COMMIT;

