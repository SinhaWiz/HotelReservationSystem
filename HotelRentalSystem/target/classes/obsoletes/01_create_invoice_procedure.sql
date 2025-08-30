-- ======================================================
-- Step 1: Create or replace the generate_invoice procedure
-- ======================================================

CREATE OR REPLACE PROCEDURE generate_invoice(
    p_booking_id IN NUMBER
) AS
    v_invoice_id NUMBER;
    v_customer_id NUMBER;
    v_room_charge NUMBER;
    v_service_total NUMBER := 0;
    v_tax_rate NUMBER := 0.08; -- 8% tax
    v_subtotal NUMBER;
    v_tax_amount NUMBER;
    v_total_amount NUMBER;
    v_discount NUMBER := 0;
    v_invoice_number VARCHAR2(30);
BEGIN
    -- Get booking information
    SELECT customer_id, total_amount, NVL(discount_applied, 0), NVL(services_total, 0)
    INTO v_customer_id, v_room_charge, v_discount, v_service_total
    FROM bookings
    WHERE booking_id = p_booking_id;

    -- Calculate invoice amounts
    v_subtotal := v_room_charge + v_service_total;
    v_tax_amount := v_subtotal * v_tax_rate;
    v_total_amount := v_subtotal + v_tax_amount - v_discount;

    -- Create invoice
    v_invoice_id := invoice_seq.NEXTVAL;
    v_invoice_number := 'INV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(v_invoice_id, 4, '0');

    INSERT INTO invoices (
        invoice_id, booking_id, customer_id, invoice_number,
        invoice_date, due_date, subtotal, tax_amount,
        discount_amount, total_amount, payment_status
    ) VALUES (
        v_invoice_id, p_booking_id, v_customer_id, v_invoice_number,
        SYSDATE, SYSDATE + 15, v_subtotal, v_tax_amount,
        v_discount, v_total_amount, 'PENDING'
    );

    -- Add line item for room charge
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'ROOM', 'Room Charge',
        1, v_room_charge, v_room_charge
    );

    -- Add line items for services
    FOR svc IN (
        SELECT service_id, usage_id, service_name, quantity, unit_price, total_cost
        FROM customer_service_usage csu
        JOIN room_services rs ON csu.service_id = rs.service_id
        WHERE booking_id = p_booking_id
    ) LOOP
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total, service_id, usage_id
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'SERVICE', svc.service_name,
            svc.quantity, svc.unit_price, svc.total_cost, svc.service_id, svc.usage_id
        );
    END LOOP;

    -- Add tax line item
    INSERT INTO invoice_line_items (
        line_item_id, invoice_id, item_type, item_description,
        quantity, unit_price, line_total
    ) VALUES (
        line_item_seq.NEXTVAL, v_invoice_id, 'TAX', 'Tax (' || (v_tax_rate * 100) || '%)',
        1, v_tax_amount, v_tax_amount
    );

    -- Add discount line item if applicable
    IF v_discount > 0 THEN
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, item_type, item_description,
            quantity, unit_price, line_total
        ) VALUES (
            line_item_seq.NEXTVAL, v_invoice_id, 'DISCOUNT', 'Discount',
            1, -v_discount, -v_discount
        );
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20008, 'Booking not found');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END generate_invoice;
/

-- Make sure the procedure is created
SHOW ERRORS;
