-- Enhanced Hotel Management System Stored Procedures
-- Additional procedures for new features

-- Procedure to add service usage for a customer
CREATE OR REPLACE PROCEDURE add_service_usage(
    p_booking_id IN NUMBER,
    p_customer_id IN NUMBER,
    p_service_id IN NUMBER,
    p_quantity IN NUMBER DEFAULT 1,
    p_usage_id OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_unit_price NUMBER(8,2);
    v_total_cost NUMBER(10,2);
    v_is_complimentary CHAR(1) := 'N';
    v_booking_exists NUMBER;
    v_service_exists NUMBER;
BEGIN
    p_success := 0;
    p_message := '';
    
    -- Validate booking exists and is active
    SELECT COUNT(*) INTO v_booking_exists
    FROM bookings 
    WHERE booking_id = p_booking_id 
    AND customer_id = p_customer_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN');
    
    IF v_booking_exists = 0 THEN
        p_message := 'Invalid booking or booking not active';
        RETURN;
    END IF;
    
    -- Get service price and check if complimentary
    SELECT rs.base_price,
           CASE WHEN rsa.is_complimentary = 'Y' THEN 'Y' ELSE 'N' END
    INTO v_unit_price, v_is_complimentary
    FROM room_services rs
    JOIN bookings b ON b.booking_id = p_booking_id
    JOIN rooms r ON b.room_id = r.room_id
    LEFT JOIN room_service_assignments rsa ON r.room_type_id = rsa.room_type_id 
                                           AND rs.service_id = rsa.service_id
    WHERE rs.service_id = p_service_id
    AND rs.is_active = 'Y';
    
    -- Calculate total cost
    IF v_is_complimentary = 'Y' THEN
        v_total_cost := 0;
    ELSE
        v_total_cost := v_unit_price * p_quantity;
    END IF;
    
    -- Insert service usage record
    INSERT INTO customer_service_usage (
        usage_id, booking_id, customer_id, service_id, 
        quantity, unit_price, total_cost, is_complimentary
    ) VALUES (
        usage_seq.NEXTVAL, p_booking_id, p_customer_id, p_service_id,
        p_quantity, v_unit_price, v_total_cost, v_is_complimentary
    ) RETURNING usage_id INTO p_usage_id;
    
    -- Update booking services total
    UPDATE bookings 
    SET services_total = services_total + v_total_cost
    WHERE booking_id = p_booking_id;
    
    -- Update customer last service date
    UPDATE customers 
    SET last_service_date = SYSDATE
    WHERE customer_id = p_customer_id;
    
    COMMIT;
    p_success := 1;
    p_message := 'Service usage added successfully';
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_message := 'Service not found or not available for this room type';
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error adding service usage: ' || SQLERRM;
END add_service_usage;
/

-- Procedure to blacklist a customer
CREATE OR REPLACE PROCEDURE blacklist_customer(
    p_customer_id IN NUMBER,
    p_reason IN VARCHAR2,
    p_blacklisted_by IN VARCHAR2,
    p_expiry_date IN DATE DEFAULT NULL,
    p_blacklist_id OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_customer_exists NUMBER;
    v_active_bookings NUMBER;
BEGIN
    p_success := 0;
    p_message := '';
    
    -- Check if customer exists
    SELECT COUNT(*) INTO v_customer_exists
    FROM customers WHERE customer_id = p_customer_id;
    
    IF v_customer_exists = 0 THEN
        p_message := 'Customer not found';
        RETURN;
    END IF;
    
    -- Check for active bookings
    SELECT COUNT(*) INTO v_active_bookings
    FROM bookings 
    WHERE customer_id = p_customer_id 
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN');
    
    IF v_active_bookings > 0 THEN
        p_message := 'Cannot blacklist customer with active bookings';
        RETURN;
    END IF;
    
    -- Insert blacklist record
    INSERT INTO blacklisted_customers (
        blacklist_id, customer_id, blacklist_reason, 
        blacklisted_by, expiry_date
    ) VALUES (
        blacklist_seq.NEXTVAL, p_customer_id, p_reason,
        p_blacklisted_by, p_expiry_date
    ) RETURNING blacklist_id INTO p_blacklist_id;
    
    -- Update customer blacklist status
    UPDATE customers 
    SET blacklist_status = 'Y'
    WHERE customer_id = p_customer_id;
    
    COMMIT;
    p_success := 1;
    p_message := 'Customer blacklisted successfully';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error blacklisting customer: ' || SQLERRM;
END blacklist_customer;
/

-- Procedure to remove customer from blacklist
CREATE OR REPLACE PROCEDURE remove_from_blacklist(
    p_customer_id IN NUMBER,
    p_removed_by IN VARCHAR2,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
BEGIN
    p_success := 0;
    p_message := '';
    
    -- Deactivate blacklist records
    UPDATE blacklisted_customers 
    SET is_active = 'N',
        notes = notes || ' | Removed by: ' || p_removed_by || ' on ' || TO_CHAR(SYSDATE, 'DD-MON-YYYY')
    WHERE customer_id = p_customer_id 
    AND is_active = 'Y';
    
    IF SQL%ROWCOUNT = 0 THEN
        p_message := 'Customer is not blacklisted';
        RETURN;
    END IF;
    
    -- Update customer blacklist status
    UPDATE customers 
    SET blacklist_status = 'N'
    WHERE customer_id = p_customer_id;
    
    COMMIT;
    p_success := 1;
    p_message := 'Customer removed from blacklist successfully';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error removing customer from blacklist: ' || SQLERRM;
END remove_from_blacklist;
/

-- Procedure to generate invoice for a booking
CREATE OR REPLACE PROCEDURE generate_invoice(
    p_booking_id IN NUMBER,
    p_tax_rate IN NUMBER DEFAULT 0.10,
    p_created_by IN VARCHAR2,
    p_invoice_id OUT NUMBER,
    p_invoice_number OUT VARCHAR2,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_customer_id NUMBER;
    v_room_total NUMBER(10,2);
    v_services_total NUMBER(10,2);
    v_extra_charges NUMBER(8,2);
    v_discount_amount NUMBER(10,2) := 0;
    v_subtotal NUMBER(12,2);
    v_tax_amount NUMBER(10,2);
    v_total_amount NUMBER(12,2);
    v_vip_discount NUMBER(4,2) := 0;
    v_booking_exists NUMBER;
    
    CURSOR service_cursor IS
        SELECT csu.usage_id, rs.service_name, csu.quantity, 
               csu.unit_price, csu.total_cost, csu.is_complimentary
        FROM customer_service_usage csu
        JOIN room_services rs ON csu.service_id = rs.service_id
        WHERE csu.booking_id = p_booking_id;
BEGIN
    p_success := 0;
    p_message := '';
    
    -- Validate booking
    SELECT COUNT(*) INTO v_booking_exists
    FROM bookings WHERE booking_id = p_booking_id;
    
    IF v_booking_exists = 0 THEN
        p_message := 'Booking not found';
        RETURN;
    END IF;
    
    -- Get booking details
    SELECT b.customer_id, b.total_amount, b.services_total, b.extra_charges,
           NVL(vm.discount_percentage, 0)
    INTO v_customer_id, v_room_total, v_services_total, v_extra_charges, v_vip_discount
    FROM bookings b
    LEFT JOIN vip_members vm ON b.customer_id = vm.customer_id AND vm.is_active = 'Y'
    WHERE b.booking_id = p_booking_id;
    
    -- Calculate discount amount
    v_discount_amount := (v_room_total + v_services_total) * (v_vip_discount / 100);
    
    -- Calculate totals
    v_subtotal := v_room_total + v_services_total + v_extra_charges - v_discount_amount;
    v_tax_amount := v_subtotal * p_tax_rate;
    v_total_amount := v_subtotal + v_tax_amount;
    
    -- Generate invoice number
    p_invoice_number := 'INV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(invoice_seq.NEXTVAL, 6, '0');
    
    -- Create invoice
    INSERT INTO invoices (
        invoice_id, booking_id, customer_id, invoice_number,
        subtotal, tax_amount, discount_amount, total_amount,
        due_date, created_by
    ) VALUES (
        invoice_seq.NEXTVAL, p_booking_id, v_customer_id, p_invoice_number,
        v_subtotal, v_tax_amount, v_discount_amount, v_total_amount,
        SYSDATE + 30, p_created_by
    ) RETURNING invoice_id INTO p_invoice_id;
    
    -- Add room charges line item
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, p_invoice_id, 'ROOM', 'Room Charges',
        1, v_room_total, v_room_total
    );
    
    -- Add service line items
    FOR service_rec IN service_cursor LOOP
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total, usage_id
        ) VALUES (
            line_item_seq.NEXTVAL, p_invoice_id, 'SERVICE', service_rec.service_name,
            service_rec.quantity, service_rec.unit_price, service_rec.total_cost,
            service_rec.usage_id
        );
    END LOOP;
    
    -- Add extra charges if any
    IF v_extra_charges > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, p_invoice_id, 'EXTRA_CHARGE', 'Late Checkout Charges',
            1, v_extra_charges, v_extra_charges
        );
    END IF;
    
    -- Add discount if applicable
    IF v_discount_amount > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, p_invoice_id, 'DISCOUNT', 'VIP Discount (' || v_vip_discount || '%)',
            1, -v_discount_amount, -v_discount_amount
        );
    END IF;
    
    -- Add tax line item
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, p_invoice_id, 'TAX', 'Tax (' || (p_tax_rate * 100) || '%)',
        1, v_tax_amount, v_tax_amount
    );
    
    COMMIT;
    p_success := 1;
    p_message := 'Invoice generated successfully';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error generating invoice: ' || SQLERRM;
END generate_invoice;
/

-- Procedure to promote top 5 customers to VIP
CREATE OR REPLACE PROCEDURE promote_top_customers_to_vip(
    p_promoted_by IN VARCHAR2,
    p_promotions_count OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    CURSOR top_customers_cursor IS
        SELECT customer_id, grand_total
        FROM (
            SELECT customer_id, grand_total,
                   ROW_NUMBER() OVER (ORDER BY grand_total DESC) as rank
            FROM v_customer_total_spending
            WHERE is_blacklisted = 'N'
            AND vip_level IS NULL
        )
        WHERE rank <= 5
        AND grand_total >= 5000; -- Minimum spending requirement
        
    v_vip_level VARCHAR2(20);
    v_discount_pct NUMBER(4,2);
    v_vip_id NUMBER;
    v_promo_success NUMBER;
    v_promo_message VARCHAR2(500);
BEGIN
    p_success := 0;
    p_message := '';
    p_promotions_count := 0;
    
    FOR customer_rec IN top_customers_cursor LOOP
        -- Determine VIP level based on spending
        IF customer_rec.grand_total >= 15000 THEN
            v_vip_level := 'DIAMOND';
            v_discount_pct := 15;
        ELSIF customer_rec.grand_total >= 10000 THEN
            v_vip_level := 'PLATINUM';
            v_discount_pct := 10;
        ELSE
            v_vip_level := 'GOLD';
            v_discount_pct := 5;
        END IF;
        
        -- Promote customer to VIP
        promote_to_vip(
            customer_rec.customer_id,
            v_vip_level,
            v_vip_id,
            v_promo_success,
            v_promo_message
        );
        
        IF v_promo_success = 1 THEN
            -- Record promotion history
            INSERT INTO vip_promotion_history (
                promotion_id, customer_id, new_level, 
                promotion_reason, total_spent_at_promotion, promoted_by
            ) VALUES (
                promotion_seq.NEXTVAL, customer_rec.customer_id, v_vip_level,
                'Top 5 customer automatic promotion', customer_rec.grand_total, p_promoted_by
            );
            
            p_promotions_count := p_promotions_count + 1;
        END IF;
    END LOOP;
    
    COMMIT;
    p_success := 1;
    p_message := p_promotions_count || ' customers promoted to VIP status';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error promoting customers to VIP: ' || SQLERRM;
END promote_top_customers_to_vip;
/

-- Procedure to archive data older than 60 days
CREATE OR REPLACE PROCEDURE archive_old_data_60_days(
    p_cutoff_date IN DATE DEFAULT SYSDATE - 60,
    p_bookings_archived OUT NUMBER,
    p_services_archived OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    CURSOR old_bookings_cursor IS
        SELECT * FROM bookings 
        WHERE booking_date < p_cutoff_date
        AND booking_status = 'CHECKED_OUT';
        
    CURSOR old_services_cursor IS
        SELECT * FROM customer_service_usage
        WHERE usage_date < p_cutoff_date;
BEGIN
    p_success := 0;
    p_message := '';
    p_bookings_archived := 0;
    p_services_archived := 0;
    
    -- Archive old bookings
    FOR booking_rec IN old_bookings_cursor LOOP
        INSERT INTO booking_audit_extended (
            audit_id, original_booking_id, customer_id, room_id,
            check_in_date, check_out_date, actual_checkout_date,
            booking_date, total_amount, booking_status,
            extra_charges, discount_applied, services_total,
            archive_reason
        ) VALUES (
            audit_extended_seq.NEXTVAL, booking_rec.booking_id, booking_rec.customer_id,
            booking_rec.room_id, booking_rec.check_in_date, booking_rec.check_out_date,
            booking_rec.actual_checkout_date, booking_rec.booking_date,
            booking_rec.total_amount, booking_rec.booking_status,
            booking_rec.extra_charges, booking_rec.discount_applied,
            booking_rec.services_total, '60-day automatic archive'
        );
        
        DELETE FROM bookings WHERE booking_id = booking_rec.booking_id;
        p_bookings_archived := p_bookings_archived + 1;
    END LOOP;
    
    -- Archive old service usage records
    FOR service_rec IN old_services_cursor LOOP
        INSERT INTO service_usage_audit (
            audit_usage_id, original_usage_id, booking_id, customer_id,
            service_id, usage_date, quantity, unit_price, total_cost,
            archive_reason
        ) VALUES (
            service_audit_seq.NEXTVAL, service_rec.usage_id, service_rec.booking_id,
            service_rec.customer_id, service_rec.service_id, service_rec.usage_date,
            service_rec.quantity, service_rec.unit_price, service_rec.total_cost,
            '60-day automatic archive'
        );
        
        DELETE FROM customer_service_usage WHERE usage_id = service_rec.usage_id;
        p_services_archived := p_services_archived + 1;
    END LOOP;
    
    COMMIT;
    p_success := 1;
    p_message := 'Archived ' || p_bookings_archived || ' bookings and ' || 
                 p_services_archived || ' service records';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error archiving old data: ' || SQLERRM;
END archive_old_data_60_days;
/

-- Procedure to get customer service summary
CREATE OR REPLACE PROCEDURE get_customer_service_summary(
    p_customer_id IN NUMBER,
    p_service_cursor OUT SYS_REFCURSOR,
    p_total_cost OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
BEGIN
    p_success := 0;
    p_message := '';
    p_total_cost := 0;
    
    -- Get total cost
    SELECT NVL(SUM(total_cost), 0)
    INTO p_total_cost
    FROM customer_service_usage
    WHERE customer_id = p_customer_id;
    
    -- Open cursor for service details
    OPEN p_service_cursor FOR
        SELECT 
            rs.service_name,
            rs.service_category,
            SUM(csu.quantity) as total_quantity,
            AVG(csu.unit_price) as avg_unit_price,
            SUM(csu.total_cost) as total_cost,
            COUNT(DISTINCT csu.booking_id) as bookings_used,
            MAX(csu.usage_date) as last_used_date
        FROM customer_service_usage csu
        JOIN room_services rs ON csu.service_id = rs.service_id
        WHERE csu.customer_id = p_customer_id
        GROUP BY rs.service_name, rs.service_category
        ORDER BY total_cost DESC;
    
    p_success := 1;
    p_message := 'Service summary retrieved successfully';
    
EXCEPTION
    WHEN OTHERS THEN
        p_message := 'Error retrieving service summary: ' || SQLERRM;
        IF p_service_cursor%ISOPEN THEN
            CLOSE p_service_cursor;
        END IF;
END get_customer_service_summary;
/

-- Procedure to check expired reservations and calculate extra charges
CREATE OR REPLACE PROCEDURE process_expired_reservations(
    p_processed_count OUT NUMBER,
    p_success OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    CURSOR expired_cursor IS
        SELECT booking_id, customer_id, check_out_date,
               ROUND((SYSDATE - check_out_date) * 24, 2) as hours_overdue
        FROM bookings
        WHERE booking_status = 'CHECKED_IN'
        AND SYSDATE > check_out_date;
        
    v_extra_charge NUMBER(8,2);
    v_hourly_rate NUMBER(6,2) := 25.00; -- $25 per hour for late checkout
BEGIN
    p_success := 0;
    p_message := '';
    p_processed_count := 0;
    
    FOR expired_rec IN expired_cursor LOOP
        -- Calculate extra charge (minimum 1 hour charge)
        v_extra_charge := GREATEST(1, CEIL(expired_rec.hours_overdue)) * v_hourly_rate;
        
        -- Update booking with extra charges
        UPDATE bookings 
        SET extra_charges = extra_charges + v_extra_charge,
            total_amount = total_amount + v_extra_charge,
            late_checkout_hours = expired_rec.hours_overdue
        WHERE booking_id = expired_rec.booking_id;
        
        -- Add service usage record for late checkout
        INSERT INTO customer_service_usage (
            usage_id, booking_id, customer_id, service_id,
            quantity, unit_price, total_cost, notes
        ) VALUES (
            usage_seq.NEXTVAL, expired_rec.booking_id, expired_rec.customer_id,
            (SELECT service_id FROM room_services WHERE service_name = 'Late Checkout'),
            CEIL(expired_rec.hours_overdue), v_hourly_rate, v_extra_charge,
            'Automatic charge for ' || expired_rec.hours_overdue || ' hours late checkout'
        );
        
        p_processed_count := p_processed_count + 1;
    END LOOP;
    
    COMMIT;
    p_success := 1;
    p_message := 'Processed ' || p_processed_count || ' expired reservations';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_message := 'Error processing expired reservations: ' || SQLERRM;
END process_expired_reservations;
/

COMMIT;

