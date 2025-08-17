-- Enhanced Hotel Management System Triggers
-- Additional triggers for new features

-- Trigger to archive data older than 60 days (runs on booking insert/update)
CREATE OR REPLACE TRIGGER trg_archive_60_day_data
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
DECLARE
    v_bookings_archived NUMBER;
    v_services_archived NUMBER;
    v_success NUMBER;
    v_message VARCHAR2(500);
BEGIN
    -- Only run archiving process occasionally (1% chance) to avoid performance impact
    IF DBMS_RANDOM.VALUE(0, 100) < 1 THEN
        archive_old_data_60_days(
            SYSDATE - 60,
            v_bookings_archived,
            v_services_archived,
            v_success,
            v_message
        );
    END IF;
END;
/

-- Trigger to automatically check for expired reservations daily
CREATE OR REPLACE TRIGGER trg_check_expired_reservations
    BEFORE INSERT ON bookings
    FOR EACH ROW
DECLARE
    v_processed_count NUMBER;
    v_success NUMBER;
    v_message VARCHAR2(500);
    v_last_check DATE;
BEGIN
    -- Check if we've already processed today
    SELECT NVL(MAX(TO_DATE(notes, 'DD-MON-YYYY')), SYSDATE - 1)
    INTO v_last_check
    FROM room_maintenance_log
    WHERE description = 'Daily expired reservation check'
    AND maintenance_status = 'COMPLETED';
    
    -- Only process if we haven't checked today
    IF TRUNC(v_last_check) < TRUNC(SYSDATE) THEN
        process_expired_reservations(
            v_processed_count,
            v_success,
            v_message
        );
        
        -- Log the check
        INSERT INTO room_maintenance_log (
            maintenance_id, room_id, maintenance_type, description,
            start_date, completion_date, maintenance_status, notes
        ) VALUES (
            maintenance_seq.NEXTVAL, 1, 'INSPECTION', 'Daily expired reservation check',
            SYSDATE, SYSDATE, 'COMPLETED', TO_CHAR(SYSDATE, 'DD-MON-YYYY')
        );
    END IF;
END;
/

-- Trigger to update customer VIP eligibility status
CREATE OR REPLACE TRIGGER trg_update_vip_eligibility
    AFTER UPDATE OF total_spent ON customers
    FOR EACH ROW
BEGIN
    -- Update VIP eligibility based on spending
    IF :NEW.total_spent >= 5000 THEN
        UPDATE customers
        SET vip_promotion_eligible = 'Y'
        WHERE customer_id = :NEW.customer_id;
    ELSE
        UPDATE customers 
        SET vip_promotion_eligible = 'N'
        WHERE customer_id = :NEW.customer_id;
    END IF;
END;
/

-- Trigger to automatically update room status based on bookings
CREATE OR REPLACE TRIGGER trg_auto_update_room_status
    AFTER UPDATE OF booking_status ON bookings
    FOR EACH ROW
BEGIN
    -- Update room status based on booking status changes
    IF :NEW.booking_status = 'CHECKED_IN' AND :OLD.booking_status = 'CONFIRMED' THEN
        UPDATE rooms 
        SET room_status = 'OCCUPIED'
        WHERE room_id = :NEW.room_id;
        
    ELSIF :NEW.booking_status = 'CHECKED_OUT' AND :OLD.booking_status = 'CHECKED_IN' THEN
        UPDATE rooms 
        SET room_status = 'MAINTENANCE' -- Needs cleaning before next guest
        WHERE room_id = :NEW.room_id;
        
        -- Schedule automatic cleaning
        INSERT INTO room_maintenance_log (
            maintenance_id, room_id, maintenance_type, description,
            scheduled_date, maintenance_status, assigned_to
        ) VALUES (
            maintenance_seq.NEXTVAL, :NEW.room_id, 'CLEANING', 
            'Post-checkout cleaning for booking ' || :NEW.booking_id,
            SYSDATE, 'SCHEDULED', 'Housekeeping Team'
        );
        
    ELSIF :NEW.booking_status = 'CANCELLED' AND :OLD.booking_status = 'CONFIRMED' THEN
        -- Check if room has other bookings, if not make it available
        DECLARE
            v_other_bookings NUMBER;
        BEGIN
            SELECT COUNT(*)
            INTO v_other_bookings
            FROM bookings
            WHERE room_id = :NEW.room_id
            AND booking_status IN ('CONFIRMED', 'CHECKED_IN')
            AND booking_id != :NEW.booking_id;
            
            IF v_other_bookings = 0 THEN
                UPDATE rooms 
                SET room_status = 'AVAILABLE'
                WHERE room_id = :NEW.room_id;
            END IF;
        END;
    END IF;
END;
/

-- Trigger to calculate and update services total in bookings
CREATE OR REPLACE TRIGGER trg_update_services_total
    AFTER INSERT OR UPDATE OR DELETE ON customer_service_usage
    FOR EACH ROW
DECLARE
    v_booking_id NUMBER;
    v_new_total NUMBER(10,2);
BEGIN
    -- Determine which booking to update
    IF INSERTING OR UPDATING THEN
        v_booking_id := :NEW.booking_id;
    ELSE -- DELETING
        v_booking_id := :OLD.booking_id;
    END IF;
    
    -- Calculate new services total
    SELECT NVL(SUM(total_cost), 0)
    INTO v_new_total
    FROM customer_service_usage
    WHERE booking_id = v_booking_id;
    
    -- Update booking services total
    UPDATE bookings 
    SET services_total = v_new_total,
        total_amount = (total_amount - services_total) + v_new_total
    WHERE booking_id = v_booking_id;
END;
/

-- Trigger to log VIP member changes
CREATE OR REPLACE TRIGGER trg_log_vip_changes
    AFTER INSERT OR UPDATE ON vip_members
    FOR EACH ROW
BEGIN
    IF INSERTING THEN
        INSERT INTO vip_promotion_history (
            promotion_id, customer_id, new_level, promotion_date,
            promotion_reason, promoted_by
        ) VALUES (
            promotion_seq.NEXTVAL, :NEW.customer_id, :NEW.membership_level, SYSDATE,
            'VIP membership created', USER
        );
        
    ELSIF UPDATING AND :OLD.membership_level != :NEW.membership_level THEN
        INSERT INTO vip_promotion_history (
            promotion_id, customer_id, previous_level, new_level, promotion_date,
            promotion_reason, promoted_by
        ) VALUES (
            promotion_seq.NEXTVAL, :NEW.customer_id, :OLD.membership_level, 
            :NEW.membership_level, SYSDATE, 'VIP level updated', USER
        );
    END IF;
END;
/

-- Trigger to validate service assignments
CREATE OR REPLACE TRIGGER trg_validate_service_assignment
    BEFORE INSERT OR UPDATE ON customer_service_usage
    FOR EACH ROW
DECLARE
    v_room_type_id NUMBER;
    v_service_available NUMBER;
BEGIN
    -- Get room type for the booking
    SELECT rt.room_type_id
    INTO v_room_type_id
    FROM bookings b
    JOIN rooms r ON b.room_id = r.room_id
    JOIN room_types rt ON r.room_type_id = rt.room_type_id
    WHERE b.booking_id = :NEW.booking_id;
    
    -- Check if service is available for this room type
    SELECT COUNT(*)
    INTO v_service_available
    FROM room_service_assignments rsa
    WHERE rsa.room_type_id = v_room_type_id
    AND rsa.service_id = :NEW.service_id;
    
    IF v_service_available = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 
            'Service not available for this room type');
    END IF;
END;
/

-- Trigger to automatically promote top customers weekly
CREATE OR REPLACE TRIGGER trg_weekly_vip_promotion
    BEFORE INSERT ON bookings
    FOR EACH ROW
DECLARE
    v_last_promotion DATE;
    v_promotions_count NUMBER;
    v_success NUMBER;
    v_message VARCHAR2(500);
BEGIN
    -- Check if we've promoted customers this week
    SELECT NVL(MAX(promotion_date), SYSDATE - 8)
    INTO v_last_promotion
    FROM vip_promotion_history
    WHERE promotion_reason = 'Top 5 customer automatic promotion';
    
    -- Only promote if it's been more than 7 days
    IF SYSDATE - v_last_promotion > 7 THEN
        promote_top_customers_to_vip(
            'SYSTEM_AUTO',
            v_promotions_count,
            v_success,
            v_message
        );
    END IF;
END;
/

-- Trigger to update invoice status based on payment
CREATE OR REPLACE TRIGGER trg_update_invoice_status
    BEFORE UPDATE OF payment_date, payment_method ON invoices
    FOR EACH ROW
BEGIN
    IF :NEW.payment_date IS NOT NULL AND :OLD.payment_date IS NULL THEN
        :NEW.payment_status := 'PAID';
    ELSIF :NEW.payment_date IS NULL AND :OLD.payment_date IS NOT NULL THEN
        :NEW.payment_status := 'PENDING';
    END IF;
    
    -- Check for overdue invoices
    IF :NEW.payment_status = 'PENDING' AND SYSDATE > :NEW.due_date THEN
        :NEW.payment_status := 'OVERDUE';
    END IF;
END;
/

-- Trigger to prevent deletion of customers with active bookings or unpaid invoices
CREATE OR REPLACE TRIGGER trg_prevent_customer_deletion
    BEFORE DELETE ON customers
    FOR EACH ROW
DECLARE
    v_active_bookings NUMBER;
    v_unpaid_invoices NUMBER;
BEGIN
    -- Check for active bookings
    SELECT COUNT(*)
    INTO v_active_bookings
    FROM bookings
    WHERE customer_id = :OLD.customer_id
    AND booking_status IN ('CONFIRMED', 'CHECKED_IN');
    
    IF v_active_bookings > 0 THEN
        RAISE_APPLICATION_ERROR(-20012, 
            'Cannot delete customer with active bookings');
    END IF;
    
    -- Check for unpaid invoices
    SELECT COUNT(*)
    INTO v_unpaid_invoices
    FROM invoices
    WHERE customer_id = :OLD.customer_id
    AND payment_status IN ('PENDING', 'OVERDUE');
    
    IF v_unpaid_invoices > 0 THEN
        RAISE_APPLICATION_ERROR(-20013, 
            'Cannot delete customer with unpaid invoices');
    END IF;
END;
/

-- Trigger to automatically set room to available after maintenance completion
CREATE OR REPLACE TRIGGER trg_complete_room_maintenance
    AFTER UPDATE OF maintenance_status ON room_maintenance_log
    FOR EACH ROW
BEGIN
    IF :NEW.maintenance_status = 'COMPLETED' AND :OLD.maintenance_status != 'COMPLETED' THEN
        -- Check if this was the last pending maintenance for the room
        DECLARE
            v_pending_maintenance NUMBER;
        BEGIN
            SELECT COUNT(*)
            INTO v_pending_maintenance
            FROM room_maintenance_log
            WHERE room_id = :NEW.room_id
            AND maintenance_status IN ('SCHEDULED', 'IN_PROGRESS')
            AND maintenance_id != :NEW.maintenance_id;
            
            -- If no more pending maintenance, make room available
            IF v_pending_maintenance = 0 THEN
                UPDATE rooms 
                SET room_status = 'AVAILABLE',
                    last_maintenance = SYSDATE
                WHERE room_id = :NEW.room_id
                AND room_status = 'MAINTENANCE';
            END IF;
        END;
    END IF;
END;
/

COMMIT;
