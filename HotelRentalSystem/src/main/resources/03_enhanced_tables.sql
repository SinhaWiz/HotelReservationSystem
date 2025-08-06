-- Enhanced Hotel Management System Database Schema
-- Additional tables for new features

-- Room Services Table
CREATE TABLE room_services (
    service_id          NUMBER(8) PRIMARY KEY,
    service_name        VARCHAR2(100) NOT NULL,
    service_description VARCHAR2(300),
    service_category    VARCHAR2(50) NOT NULL, -- HOUSEKEEPING, FOOD, LAUNDRY, MAINTENANCE, ENTERTAINMENT
    base_price          NUMBER(8,2) NOT NULL,
    is_active           CHAR(1) DEFAULT 'Y',
    created_date        DATE DEFAULT SYSDATE,
    CONSTRAINT chk_service_active CHECK (is_active IN ('Y', 'N'))
);

-- Room Service Assignments (which services are available for which room types)
CREATE TABLE room_service_assignments (
    assignment_id       NUMBER(10) PRIMARY KEY,
    room_type_id        NUMBER(5) NOT NULL,
    service_id          NUMBER(8) NOT NULL,
    is_complimentary    CHAR(1) DEFAULT 'N',
    assigned_date       DATE DEFAULT SYSDATE,
    CONSTRAINT fk_rsa_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id),
    CONSTRAINT fk_rsa_service FOREIGN KEY (service_id) REFERENCES room_services(service_id),
    CONSTRAINT chk_complimentary CHECK (is_complimentary IN ('Y', 'N')),
    CONSTRAINT uk_room_service UNIQUE (room_type_id, service_id)
);

-- Customer Service Usage (track services used by customers)
CREATE TABLE customer_service_usage (
    usage_id           NUMBER(12) PRIMARY KEY,
    booking_id         NUMBER(12) NOT NULL,
    customer_id        NUMBER(10) NOT NULL,
    service_id         NUMBER(8) NOT NULL,
    usage_date         DATE DEFAULT SYSDATE,
    quantity           NUMBER(3) DEFAULT 1,
    unit_price         NUMBER(8,2) NOT NULL,
    total_cost         NUMBER(10,2) NOT NULL,
    is_complimentary   CHAR(1) DEFAULT 'N',
    notes              VARCHAR2(300),
    CONSTRAINT fk_csu_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_csu_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_csu_service FOREIGN KEY (service_id) REFERENCES room_services(service_id),
    CONSTRAINT chk_usage_complimentary CHECK (is_complimentary IN ('Y', 'N'))
);

-- Blacklisted Customers Table
CREATE TABLE blacklisted_customers (
    blacklist_id        NUMBER(10) PRIMARY KEY,
    customer_id         NUMBER(10) NOT NULL,
    blacklist_reason    VARCHAR2(500) NOT NULL,
    blacklisted_by      VARCHAR2(100) NOT NULL, -- Staff member who blacklisted
    blacklist_date      DATE DEFAULT SYSDATE,
    expiry_date         DATE, -- NULL for permanent blacklist
    is_active           CHAR(1) DEFAULT 'Y',
    notes               VARCHAR2(1000),
    CONSTRAINT fk_bl_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT chk_blacklist_active CHECK (is_active IN ('Y', 'N'))
);

-- Invoices Table
CREATE TABLE invoices (
    invoice_id          NUMBER(12) PRIMARY KEY,
    booking_id          NUMBER(12) NOT NULL,
    customer_id         NUMBER(10) NOT NULL,
    invoice_number      VARCHAR2(20) UNIQUE NOT NULL,
    invoice_date        DATE DEFAULT SYSDATE,
    due_date            DATE,
    subtotal            NUMBER(12,2) NOT NULL,
    tax_amount          NUMBER(10,2) DEFAULT 0,
    discount_amount     NUMBER(10,2) DEFAULT 0,
    total_amount        NUMBER(12,2) NOT NULL,
    payment_status      VARCHAR2(20) DEFAULT 'PENDING',
    payment_date        DATE,
    payment_method      VARCHAR2(50),
    notes               VARCHAR2(500),
    created_by          VARCHAR2(100),
    CONSTRAINT fk_inv_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_inv_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED'))
);

-- Invoice Line Items Table
CREATE TABLE invoice_line_items (
    line_item_id        NUMBER(12) PRIMARY KEY,
    invoice_id          NUMBER(12) NOT NULL,
    item_type           VARCHAR2(20) NOT NULL, -- ROOM, SERVICE, TAX, DISCOUNT
    item_description    VARCHAR2(200) NOT NULL,
    quantity            NUMBER(6,2) DEFAULT 1,
    unit_price          NUMBER(10,2) NOT NULL,
    line_total          NUMBER(12,2) NOT NULL,
    service_id          NUMBER(8), -- NULL for room charges
    usage_id            NUMBER(12), -- Reference to customer_service_usage
    CONSTRAINT fk_ili_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id),
    CONSTRAINT fk_ili_service FOREIGN KEY (service_id) REFERENCES room_services(service_id),
    CONSTRAINT fk_ili_usage FOREIGN KEY (usage_id) REFERENCES customer_service_usage(usage_id),
    CONSTRAINT chk_item_type CHECK (item_type IN ('ROOM', 'SERVICE', 'TAX', 'DISCOUNT', 'EXTRA_CHARGE'))
);

-- Extended Booking Audit Table (60 days retention)
CREATE TABLE booking_audit_extended (
    audit_id            NUMBER(15) PRIMARY KEY,
    original_booking_id NUMBER(12) NOT NULL,
    customer_id         NUMBER(10) NOT NULL,
    room_id             NUMBER(8) NOT NULL,
    check_in_date       DATE NOT NULL,
    check_out_date      DATE NOT NULL,
    actual_checkout_date DATE,
    booking_date        DATE NOT NULL,
    total_amount        NUMBER(10,2) NOT NULL,
    booking_status      VARCHAR2(20) NOT NULL,
    extra_charges       NUMBER(8,2) DEFAULT 0,
    discount_applied    NUMBER(6,2) DEFAULT 0,
    services_total      NUMBER(10,2) DEFAULT 0,
    archived_date       DATE DEFAULT SYSDATE,
    archive_reason      VARCHAR2(100),
    retention_period    NUMBER(3) DEFAULT 60 -- Days to retain
);

-- Customer Service Usage Audit Table
CREATE TABLE service_usage_audit (
    audit_usage_id      NUMBER(15) PRIMARY KEY,
    original_usage_id   NUMBER(12) NOT NULL,
    booking_id          NUMBER(12) NOT NULL,
    customer_id         NUMBER(10) NOT NULL,
    service_id          NUMBER(8) NOT NULL,
    usage_date          DATE NOT NULL,
    quantity            NUMBER(3) NOT NULL,
    unit_price          NUMBER(8,2) NOT NULL,
    total_cost          NUMBER(10,2) NOT NULL,
    archived_date       DATE DEFAULT SYSDATE,
    archive_reason      VARCHAR2(100)
);

-- VIP Promotion History Table
CREATE TABLE vip_promotion_history (
    promotion_id        NUMBER(10) PRIMARY KEY,
    customer_id         NUMBER(10) NOT NULL,
    previous_level      VARCHAR2(20), -- NULL for first promotion
    new_level           VARCHAR2(20) NOT NULL,
    promotion_date      DATE DEFAULT SYSDATE,
    promotion_reason    VARCHAR2(200),
    total_spent_at_promotion NUMBER(12,2),
    promoted_by         VARCHAR2(100),
    CONSTRAINT fk_vph_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Room Maintenance Log Table
CREATE TABLE room_maintenance_log (
    maintenance_id      NUMBER(10) PRIMARY KEY,
    room_id             NUMBER(8) NOT NULL,
    maintenance_type    VARCHAR2(50) NOT NULL, -- CLEANING, REPAIR, INSPECTION, UPGRADE
    description         VARCHAR2(500) NOT NULL,
    scheduled_date      DATE,
    start_date          DATE,
    completion_date     DATE,
    maintenance_status  VARCHAR2(20) DEFAULT 'SCHEDULED',
    assigned_to         VARCHAR2(100),
    cost                NUMBER(8,2),
    notes               VARCHAR2(1000),
    CONSTRAINT fk_rml_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    CONSTRAINT chk_maintenance_status CHECK (maintenance_status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Create sequences for new tables
CREATE SEQUENCE service_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE assignment_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE usage_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE blacklist_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE invoice_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE line_item_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE audit_extended_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE service_audit_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE promotion_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE maintenance_seq START WITH 1 INCREMENT BY 1;

-- Create indexes for performance
CREATE INDEX idx_service_usage_booking ON customer_service_usage(booking_id);
CREATE INDEX idx_service_usage_customer ON customer_service_usage(customer_id);
CREATE INDEX idx_service_usage_date ON customer_service_usage(usage_date);
CREATE INDEX idx_blacklist_customer ON blacklisted_customers(customer_id);
CREATE INDEX idx_blacklist_active ON blacklisted_customers(is_active);
CREATE INDEX idx_invoice_customer ON invoices(customer_id);
CREATE INDEX idx_invoice_booking ON invoices(booking_id);
CREATE INDEX idx_invoice_status ON invoices(payment_status);
CREATE INDEX idx_invoice_date ON invoices(invoice_date);
CREATE INDEX idx_maintenance_room ON room_maintenance_log(room_id);
CREATE INDEX idx_maintenance_status ON room_maintenance_log(maintenance_status);

-- Add new columns to existing tables
ALTER TABLE bookings ADD (
    actual_checkout_date DATE,
    late_checkout_hours NUMBER(4,2) DEFAULT 0,
    services_total NUMBER(10,2) DEFAULT 0
);

ALTER TABLE customers ADD (
    blacklist_status CHAR(1) DEFAULT 'N',
    vip_promotion_eligible CHAR(1) DEFAULT 'N',
    last_service_date DATE
);

-- Add constraints for new columns
ALTER TABLE customers ADD CONSTRAINT chk_blacklist_status CHECK (blacklist_status IN ('Y', 'N'));
ALTER TABLE customers ADD CONSTRAINT chk_vip_eligible CHECK (vip_promotion_eligible IN ('Y', 'N'));

-- Insert sample room services
INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Room Cleaning', 'Daily housekeeping service', 'HOUSEKEEPING', 25.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Laundry Service', 'Wash and fold laundry service', 'LAUNDRY', 15.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Room Service Breakfast', 'Continental breakfast delivered to room', 'FOOD', 35.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Room Service Lunch', 'Lunch menu delivered to room', 'FOOD', 45.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Room Service Dinner', 'Dinner menu delivered to room', 'FOOD', 55.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Spa Service', 'In-room massage and spa treatment', 'ENTERTAINMENT', 120.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Airport Transfer', 'Transportation to/from airport', 'TRANSPORTATION', 75.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Mini Bar Restock', 'Restock mini bar with beverages and snacks', 'FOOD', 50.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Late Checkout', 'Extended checkout time beyond standard hours', 'ACCOMMODATION', 30.00);

INSERT INTO room_services (service_id, service_name, service_description, service_category, base_price) VALUES
(service_seq.NEXTVAL, 'Extra Towels', 'Additional towel service', 'HOUSEKEEPING', 10.00);

-- Assign services to room types (all services available to all room types for now)
INSERT INTO room_service_assignments (assignment_id, room_type_id, service_id, is_complimentary)
SELECT assignment_seq.NEXTVAL, rt.room_type_id, rs.service_id, 
       CASE WHEN rs.service_name IN ('Room Cleaning', 'Extra Towels') THEN 'Y' ELSE 'N' END
FROM room_types rt, room_services rs
WHERE rt.is_active = 'Y' AND rs.is_active = 'Y';

COMMIT;

-- Add comments to tables
COMMENT ON TABLE room_services IS 'Available hotel services that can be assigned to rooms';
COMMENT ON TABLE room_service_assignments IS 'Mapping of services available for each room type';
COMMENT ON TABLE customer_service_usage IS 'Track services used by customers during their stay';
COMMENT ON TABLE blacklisted_customers IS 'Customers who are banned from making reservations';
COMMENT ON TABLE invoices IS 'Generated invoices for customer bookings and services';
COMMENT ON TABLE invoice_line_items IS 'Detailed line items for each invoice';
COMMENT ON TABLE booking_audit_extended IS 'Extended audit trail for bookings (60-day retention)';
COMMENT ON TABLE service_usage_audit IS 'Audit trail for service usage records';
COMMENT ON TABLE vip_promotion_history IS 'History of VIP promotions and level changes';
COMMENT ON TABLE room_maintenance_log IS 'Log of room maintenance activities';

-- Create views for commonly used queries
CREATE OR REPLACE VIEW v_customer_total_spending AS
SELECT 
    c.customer_id,
    c.first_name || ' ' || c.last_name as customer_name,
    c.total_spent as booking_total,
    NVL(SUM(csu.total_cost), 0) as services_total,
    c.total_spent + NVL(SUM(csu.total_cost), 0) as grand_total,
    c.loyalty_points,
    CASE WHEN bl.customer_id IS NOT NULL THEN 'Y' ELSE 'N' END as is_blacklisted,
    vm.membership_level as vip_level
FROM customers c
LEFT JOIN customer_service_usage csu ON c.customer_id = csu.customer_id
LEFT JOIN blacklisted_customers bl ON c.customer_id = bl.customer_id AND bl.is_active = 'Y'
LEFT JOIN vip_members vm ON c.customer_id = vm.customer_id AND vm.is_active = 'Y'
GROUP BY c.customer_id, c.first_name, c.last_name, c.total_spent, c.loyalty_points, 
         bl.customer_id, vm.membership_level;

CREATE OR REPLACE VIEW v_available_rooms AS
SELECT 
    r.room_id,
    r.room_number,
    rt.type_name,
    rt.base_rate,
    rt.max_occupancy,
    r.floor_number,
    r.room_status,
    COUNT(rsa.service_id) as available_services
FROM rooms r
JOIN room_types rt ON r.room_type_id = rt.room_type_id
LEFT JOIN room_service_assignments rsa ON rt.room_type_id = rsa.room_type_id
WHERE r.room_status = 'AVAILABLE'
GROUP BY r.room_id, r.room_number, rt.type_name, rt.base_rate, rt.max_occupancy, 
         r.floor_number, r.room_status
ORDER BY r.room_number;

CREATE OR REPLACE VIEW v_expired_reservations AS
SELECT 
    b.booking_id,
    b.customer_id,
    c.first_name || ' ' || c.last_name as customer_name,
    b.room_id,
    r.room_number,
    b.check_in_date,
    b.check_out_date,
    b.booking_status,
    CASE 
        WHEN b.booking_status = 'CHECKED_IN' AND SYSDATE > b.check_out_date 
        THEN ROUND((SYSDATE - b.check_out_date) * 24, 2)
        ELSE 0 
    END as hours_overdue
FROM bookings b
JOIN customers c ON b.customer_id = c.customer_id
JOIN rooms r ON b.room_id = r.room_id
WHERE (b.booking_status = 'CHECKED_IN' AND SYSDATE > b.check_out_date)
   OR (b.booking_status = 'CONFIRMED' AND SYSDATE > b.check_out_date)
ORDER BY b.check_out_date;

COMMIT;

