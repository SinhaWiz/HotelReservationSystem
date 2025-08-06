-- Hotel Management System - Audit and Archive Triggers
-- Oracle Database Triggers for Audit Logging and Data Archiving

-- Trigger to automatically archive bookings older than 1 month
CREATE OR REPLACE TRIGGER trg_archive_old_bookings
    AFTER INSERT OR UPDATE ON bookings
    FOR EACH ROW
DECLARE
    v_archive_count NUMBER;
BEGIN
    -- Only process completed bookings
    IF :NEW.booking_status = 'CHECKED_OUT' AND :NEW.actual_check_out IS NOT NULL THEN
        -- Check if booking is older than 1 month and not already archived
        IF :NEW.actual_check_out <= ADD_MONTHS(SYSDATE, -1) THEN
            -- Check if already archived
            SELECT COUNT(*) INTO v_archive_count
            FROM booking_archive
            WHERE booking_id = :NEW.booking_id;
            
            -- Archive if not already archived
            IF v_archive_count = 0 THEN
                INSERT INTO booking_archive (
                    archive_id, booking_id, customer_id, room_id,
                    check_in_date, check_out_date, actual_check_in, actual_check_out,
                    booking_date, total_amount, discount_applied, extra_charges,
                    payment_status, booking_status, special_requests
                ) VALUES (
                    archive_seq.NEXTVAL, :NEW.booking_id, :NEW.customer_id, :NEW.room_id,
                    :NEW.check_in_date, :NEW.check_out_date, :NEW.actual_check_in, :NEW.actual_check_out,
                    :NEW.booking_date, :NEW.total_amount, :NEW.discount_applied, :NEW.extra_charges,
                    :NEW.payment_status, :NEW.booking_status, :NEW.special_requests
                );
                
                -- Log the archiving action
                INSERT INTO audit_log (
                    log_id, table_name, operation, record_id,
                    new_values, changed_by, change_date
                ) VALUES (
                    audit_seq.NEXTVAL, 'BOOKING_ARCHIVE', 'INSERT', :NEW.booking_id,
                    'Booking archived: ID=' || :NEW.booking_id || ', Customer=' || :NEW.customer_id,
                    USER, SYSDATE
                );
            END IF;
        END IF;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Log error but don't fail the main transaction
        INSERT INTO audit_log (
            log_id, table_name, operation, record_id,
            new_values, changed_by, change_date
        ) VALUES (
            audit_seq.NEXTVAL, 'BOOKING_ARCHIVE', 'ERROR', :NEW.booking_id,
            'Archive error: ' || SQLERRM,
            USER, SYSDATE
        );
END trg_archive_old_bookings;
/

-- Trigger to audit customer changes
CREATE OR REPLACE TRIGGER trg_audit_customers
    AFTER INSERT OR UPDATE OR DELETE ON customers
    FOR EACH ROW
DECLARE
    v_operation VARCHAR2(10);
    v_old_values CLOB;
    v_new_values CLOB;
    v_record_id NUMBER;
BEGIN
    -- Determine operation type
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_record_id := :NEW.customer_id;
        v_new_values := 'ID=' || :NEW.customer_id || 
                       ', Name=' || :NEW.first_name || ' ' || :NEW.last_name ||
                       ', Email=' || :NEW.email ||
                       ', Phone=' || :NEW.phone ||
                       ', Total_Spent=' || :NEW.total_spent;
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_record_id := :NEW.customer_id;
        v_old_values := 'ID=' || :OLD.customer_id || 
                       ', Name=' || :OLD.first_name || ' ' || :OLD.last_name ||
                       ', Email=' || :OLD.email ||
                       ', Phone=' || :OLD.phone ||
                       ', Total_Spent=' || :OLD.total_spent;
        v_new_values := 'ID=' || :NEW.customer_id || 
                       ', Name=' || :NEW.first_name || ' ' || :NEW.last_name ||
                       ', Email=' || :NEW.email ||
                       ', Phone=' || :NEW.phone ||
                       ', Total_Spent=' || :NEW.total_spent;
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_record_id := :OLD.customer_id;
        v_old_values := 'ID=' || :OLD.customer_id || 
                       ', Name=' || :OLD.first_name || ' ' || :OLD.last_name ||
                       ', Email=' || :OLD.email ||
                       ', Phone=' || :OLD.phone ||
                       ', Total_Spent=' || :OLD.total_spent;
    END IF;
    
    -- Insert audit record
    INSERT INTO audit_log (
        log_id, table_name, operation, record_id,
        old_values, new_values, changed_by, change_date
    ) VALUES (
        audit_seq.NEXTVAL, 'CUSTOMERS', v_operation, v_record_id,
        v_old_values, v_new_values, USER, SYSDATE
    );
    
EXCEPTION
    WHEN OTHERS THEN
        NULL; -- Don't fail the main transaction due to audit issues
END trg_audit_customers;
/

-- Trigger to audit booking changes
CREATE OR REPLACE TRIGGER trg_audit_bookings
    AFTER INSERT OR UPDATE OR DELETE ON bookings
    FOR EACH ROW
DECLARE
    v_operation VARCHAR2(10);
    v_old_values CLOB;
    v_new_values CLOB;
    v_record_id NUMBER;
BEGIN
    -- Determine operation type
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_record_id := :NEW.booking_id;
        v_new_values := 'ID=' || :NEW.booking_id || 
                       ', Customer=' || :NEW.customer_id ||
                       ', Room=' || :NEW.room_id ||
                       ', CheckIn=' || TO_CHAR(:NEW.check_in_date, 'YYYY-MM-DD') ||
                       ', CheckOut=' || TO_CHAR(:NEW.check_out_date, 'YYYY-MM-DD') ||
                       ', Amount=' || :NEW.total_amount ||
                       ', Status=' || :NEW.booking_status;
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_record_id := :NEW.booking_id;
        v_old_values := 'ID=' || :OLD.booking_id || 
                       ', Customer=' || :OLD.customer_id ||
                       ', Room=' || :OLD.room_id ||
                       ', CheckIn=' || TO_CHAR(:OLD.check_in_date, 'YYYY-MM-DD') ||
                       ', CheckOut=' || TO_CHAR(:OLD.check_out_date, 'YYYY-MM-DD') ||
                       ', Amount=' || :OLD.total_amount ||
                       ', Status=' || :OLD.booking_status;
        v_new_values := 'ID=' || :NEW.booking_id || 
                       ', Customer=' || :NEW.customer_id ||
                       ', Room=' || :NEW.room_id ||
                       ', CheckIn=' || TO_CHAR(:NEW.check_in_date, 'YYYY-MM-DD') ||
                       ', CheckOut=' || TO_CHAR(:NEW.check_out_date, 'YYYY-MM-DD') ||
                       ', Amount=' || :NEW.total_amount ||
                       ', Status=' || :NEW.booking_status;
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_record_id := :OLD.booking_id;
        v_old_values := 'ID=' || :OLD.booking_id || 
                       ', Customer=' || :OLD.customer_id ||
                       ', Room=' || :OLD.room_id ||
                       ', CheckIn=' || TO_CHAR(:OLD.check_in_date, 'YYYY-MM-DD') ||
                       ', CheckOut=' || TO_CHAR(:OLD.check_out_date, 'YYYY-MM-DD') ||
                       ', Amount=' || :OLD.total_amount ||
                       ', Status=' || :OLD.booking_status;
    END IF;
    
    -- Insert audit record
    INSERT INTO audit_log (
        log_id, table_name, operation, record_id,
        old_values, new_values, changed_by, change_date
    ) VALUES (
        audit_seq.NEXTVAL, 'BOOKINGS', v_operation, v_record_id,
        v_old_values, v_new_values, USER, SYSDATE
    );
    
EXCEPTION
    WHEN OTHERS THEN
        NULL; -- Don't fail the main transaction due to audit issues
END trg_audit_bookings;
/

-- Trigger to automatically update customer total spent and loyalty points
CREATE OR REPLACE TRIGGER trg_update_customer_spending
    AFTER UPDATE ON bookings
    FOR EACH ROW
DECLARE
    v_spending_difference NUMBER(12,2);
    v_loyalty_points NUMBER(10);
BEGIN
    -- Only process when booking is completed or amount changes
    IF (:NEW.booking_status = 'CHECKED_OUT' AND :OLD.booking_status != 'CHECKED_OUT') 
       OR (:NEW.total_amount != :OLD.total_amount AND :NEW.booking_status = 'CHECKED_OUT') THEN
        
        -- Calculate spending difference
        v_spending_difference := :NEW.total_amount - NVL(:OLD.total_amount, 0);
        
        -- Update customer's total spent
        UPDATE customers 
        SET total_spent = total_spent + v_spending_difference,
            loyalty_points = loyalty_points + calculate_loyalty_points(v_spending_difference)
        WHERE customer_id = :NEW.customer_id;
        
        -- Check if customer should be promoted to VIP
        IF check_vip_eligibility(:NEW.customer_id) NOT IN ('ALREADY_VIP', 'NOT_ELIGIBLE', 'CUSTOMER_NOT_FOUND', 'ERROR') THEN
            -- Auto-promote to VIP if eligible
            INSERT INTO vip_members (
                vip_id, customer_id, membership_level, discount_percentage,
                membership_start_date, benefits
            ) VALUES (
                vip_seq.NEXTVAL, :NEW.customer_id, 
                check_vip_eligibility(:NEW.customer_id),
                CASE check_vip_eligibility(:NEW.customer_id)
                    WHEN 'GOLD' THEN 10.00
                    WHEN 'PLATINUM' THEN 15.00
                    WHEN 'DIAMOND' THEN 20.00
                    ELSE 5.00
                END,
                SYSDATE,
                CASE check_vip_eligibility(:NEW.customer_id)
                    WHEN 'GOLD' THEN 'Free WiFi, Late Checkout, Priority Booking'
                    WHEN 'PLATINUM' THEN 'Free WiFi, Late Checkout, Priority Booking, Complimentary Breakfast'
                    WHEN 'DIAMOND' THEN 'Free WiFi, Late Checkout, Priority Booking, Complimentary Breakfast, Room Upgrade, Concierge Service'
                    ELSE 'Basic VIP Benefits'
                END
            );
            
            -- Log VIP promotion
            INSERT INTO audit_log (
                log_id, table_name, operation, record_id,
                new_values, changed_by, change_date
            ) VALUES (
                audit_seq.NEXTVAL, 'VIP_MEMBERS', 'AUTO_PROMOTE', :NEW.customer_id,
                'Customer auto-promoted to VIP: Level=' || check_vip_eligibility(:NEW.customer_id),
                USER, SYSDATE
            );
        END IF;
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        -- Log error but don't fail the main transaction
        INSERT INTO audit_log (
            log_id, table_name, operation, record_id,
            new_values, changed_by, change_date
        ) VALUES (
            audit_seq.NEXTVAL, 'CUSTOMERS', 'ERROR', :NEW.customer_id,
            'Error updating customer spending: ' || SQLERRM,
            USER, SYSDATE
        );
END trg_update_customer_spending;
/

-- Trigger to clean up old audit logs (keep only last 6 months)
CREATE OR REPLACE TRIGGER trg_cleanup_audit_logs
    AFTER INSERT ON audit_log
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_deleted_count NUMBER;
BEGIN
    -- Delete audit logs older than 6 months
    DELETE FROM audit_log 
    WHERE change_date < ADD_MONTHS(SYSDATE, -6);
    
    v_deleted_count := SQL%ROWCOUNT;
    
    -- Log cleanup if records were deleted
    IF v_deleted_count > 0 THEN
        INSERT INTO audit_log (
            log_id, table_name, operation, record_id,
            new_values, changed_by, change_date
        ) VALUES (
            audit_seq.NEXTVAL, 'AUDIT_LOG', 'CLEANUP', 0,
            'Cleaned up ' || v_deleted_count || ' old audit records',
            'SYSTEM', SYSDATE
        );
    END IF;
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
END trg_cleanup_audit_logs;
/

