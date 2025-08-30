-- ======================================================
-- Fix for generate_invoice procedure and booking creation
-- ======================================================

-- First, create the generate_invoice procedure
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

-- Now create the booking creation script, using the procedure above
DECLARE
  PROCEDURE create_booking(
    p_customer_email VARCHAR2,
    p_room_number VARCHAR2,
    p_check_in DATE,
    p_check_out DATE,
    p_status VARCHAR2,
    p_payment_status VARCHAR2 DEFAULT 'PENDING'
  ) IS
    v_customer_id NUMBER;
    v_room_id NUMBER;
    v_room_price NUMBER;
    v_total_price NUMBER;
    v_discount NUMBER := 0;
    v_discount_pct NUMBER := 0;
    v_booking_id NUMBER;
  BEGIN
    -- Get customer ID
    BEGIN
      SELECT customer_id INTO v_customer_id FROM customers WHERE email = p_customer_email;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RETURN; -- Skip if customer not found
    END;

    -- Get room ID and price
    BEGIN
      SELECT room_id, base_price INTO v_room_id, v_room_price
      FROM rooms
      WHERE room_number = p_room_number;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RETURN; -- Skip if room not found
    END;

    -- Calculate total price
    v_total_price := v_room_price * (p_check_out - p_check_in);

    -- Check for VIP status and apply discount
    BEGIN
      SELECT discount_percentage INTO v_discount_pct
      FROM vip_members
      WHERE customer_id = v_customer_id
      AND is_active = 'Y';

      v_discount := v_total_price * (v_discount_pct / 100);
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_discount := 0;
    END;

    -- Create booking
    v_booking_id := booking_seq.NEXTVAL;

    INSERT INTO bookings(
      booking_id, customer_id, room_id, check_in_date, check_out_date,
      booking_date, total_amount, discount_applied, booking_status,
      payment_status, created_date, created_by
    ) VALUES (
      v_booking_id, v_customer_id, v_room_id, p_check_in, p_check_out,
      SYSDATE, v_total_price - v_discount, v_discount, p_status,
      p_payment_status, SYSDATE, USER
    );

    -- Add actual check-in/out dates for appropriate statuses
    IF p_status = 'CHECKED_IN' THEN
      UPDATE bookings
      SET actual_check_in = p_check_in
      WHERE booking_id = v_booking_id;
    ELSIF p_status = 'CHECKED_OUT' THEN
      UPDATE bookings
      SET actual_check_in = p_check_in,
          actual_check_out = p_check_out
      WHERE booking_id = v_booking_id;

      -- Generate invoice for completed stay
      -- Make the call with explicit commit to avoid issues
      BEGIN
        generate_invoice(v_booking_id);
        COMMIT;
      EXCEPTION
        WHEN OTHERS THEN
          -- Log error but continue processing
          DBMS_OUTPUT.PUT_LINE('Error generating invoice: ' || SQLERRM);
      END;
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      -- Just log and skip on any other error
      DBMS_OUTPUT.PUT_LINE('Error creating booking: ' || SQLERRM);
  END;
BEGIN
  -- Historical bookings (checked out)
  create_booking('alice.morgan@example.com', '101', SYSDATE - 20, SYSDATE - 15, 'CHECKED_OUT', 'PAID');
  create_booking('brian.chen@example.com', '201', SYSDATE - 25, SYSDATE - 20, 'CHECKED_OUT', 'PAID');
  create_booking('carla.diaz@example.com', '301', SYSDATE - 30, SYSDATE - 25, 'CHECKED_OUT', 'PAID');
  create_booking('david.edwards@example.com', '102', SYSDATE - 35, SYSDATE - 32, 'CHECKED_OUT', 'PAID');
  create_booking('emma.foster@example.com', '204', SYSDATE - 15, SYSDATE - 10, 'CHECKED_OUT', 'PAID');

  -- Current bookings (checked in)
  create_booking('farid.ghani@example.com', '202', SYSDATE - 2, SYSDATE + 3, 'CHECKED_IN');
  create_booking('grace.hopper@example.com', '301', SYSDATE - 1, SYSDATE + 2, 'CHECKED_IN');
  create_booking('jack.kumar@example.com', '305', SYSDATE - 3, SYSDATE + 1, 'CHECKED_IN');

  -- Future bookings (confirmed)
  create_booking('henry.iverson@example.com', '103', SYSDATE + 5, SYSDATE + 8, 'CONFIRMED');
  create_booking('isla.jones@example.com', '302', SYSDATE + 10, SYSDATE + 15, 'CONFIRMED');
  create_booking('qing.ramirez@example.com', '401', SYSDATE + 7, SYSDATE + 12, 'CONFIRMED');
  create_booking('victor.wong@example.com', '410', SYSDATE + 20, SYSDATE + 25, 'CONFIRMED');
END;
/
