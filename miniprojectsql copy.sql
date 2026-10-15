CREATE DATABASE IF NOT EXISTS ck_hotels_management;
USE ck_hotels_management;

CREATE TABLE hotels (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_name VARCHAR(60) NOT NULL,
    city VARCHAR(40) NOT NULL,
    star_rating INT
);

CREATE TABLE rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id),
    UNIQUE (hotel_id, room_number)
);

CREATE TABLE guests (
    guest_id INT PRIMARY KEY AUTO_INCREMENT,
    guest_name VARCHAR(60) NOT NULL,
    phone VARCHAR(15),
    city VARCHAR(40)
);

CREATE TABLE reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    CHECK (check_out > check_in)
);

CREATE TABLE payment_records (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    reservation_id INT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

INSERT INTO hotels (hotel_name, city, star_rating) VALUES
('Taj', 'Jaipur', 5),
('Radisson', 'Mysuru', 5),
('Antera', 'Visakhapatnam', 3),
('Bluesea', 'Pune', 5),
('Greenmeridian', 'Kochi', 4);

INSERT INTO rooms (hotel_id, room_number, room_type, price, status) VALUES
(1, 'V501', 'Standard', 3100, 'Available'),
(1, 'V502', 'Deluxe', 4900, 'Occupied'),
(1, 'V503', 'Suite', 9200, 'Occupied'),
(2, 'Z601', 'Standard', 2700, 'Available'),
(2, 'Z602', 'Deluxe', 4600, 'Occupied'),
(3, 'O701', 'Standard', 2300, 'Available'),
(3, 'O702', 'Suite', 8100, 'Occupied'),
(4, 'N801', 'Standard', 3400, 'Available'),
(4, 'N802', 'Deluxe', 5300, 'Occupied'),
(4, 'N803', 'Suite', 10500, 'Available'),
(5, 'E901', 'Standard', 2900, 'Available'),
(5, 'E902', 'Deluxe', 4700, 'Occupied'),
(5, 'E903', 'Suite', 8800, 'Available');

INSERT INTO guests (guest_name, phone, city) VALUES
('Devansh', '9123405671', 'Pune'),
('Tanvi', '9123405672', 'Mumbai'),
('Menon', '9123405673', 'Kochi'),
('Kavya', '9123405674', 'Coimbatore'),
('krishna', '9123405675', 'Surat'),
('mahesh', '9123405676', 'Delhi'),
('saikiran', '9123405677', 'Indore'),
('narendra', '9123405678', 'Nagpur');

INSERT INTO reservations
(guest_id, room_id, check_in, check_out, booking_status) VALUES
(1, 2, '2026-10-12', '2026-10-17', 'Active'),
(2, 3, '2026-10-13', '2026-10-19', 'Active'),
(3, 5, '2026-10-14', '2026-10-18', 'Active'),
(4, 7, '2026-10-01', '2026-10-04', 'Completed'),
(1, 1, '2026-10-20', '2026-10-23', 'Booked'),
(5, 4, '2026-10-05', '2026-10-08', 'Cancelled'),
(7, 9, '2026-10-15', '2026-10-20', 'Active'),
(8, 12, '2026-10-16', '2026-10-21', 'Active'),
(2, 10, '2026-11-02', '2026-11-06', 'Booked'),
(3, 13, '2026-11-10', '2026-11-13', 'Booked');

INSERT INTO payment_records
(reservation_id, amount, payment_status) VALUES
(1, 19500, 'Paid'),
(2, 39000, 'Paid'),
(3, 18400, 'Pending'),
(4, 18000, 'Paid'),
(5, 9300, 'Pending'),
(6, 0, 'Refunded'),
(7, 26500, 'Paid'),
(8, 23500, 'Paid'),
(9, 31500, 'Pending'),
(10, 26400, 'Paid');

SELECT r.room_number, r.room_type, r.price, h.hotel_name
FROM rooms r
INNER JOIN hotels h ON r.hotel_id = h.hotel_id
WHERE r.status = 'Available';

SELECT g.guest_name, h.hotel_name, r.room_number,
       b.check_in, b.check_out
FROM guests g
INNER JOIN reservations b ON g.guest_id = b.guest_id
INNER JOIN rooms r ON b.room_id = r.room_id
INNER JOIN hotels h ON r.hotel_id = h.hotel_id
WHERE CURDATE() >= b.check_in
  AND CURDATE() < b.check_out
  AND b.booking_status = 'Active';

SELECT COALESCE(SUM(amount), 0) AS total_revenue
FROM payment_records
WHERE payment_status = 'Paid';

SELECT b.reservation_id, g.guest_name, h.hotel_name,
       b.check_in, b.check_out
FROM reservations b
INNER JOIN guests g ON b.guest_id = g.guest_id
INNER JOIN rooms r ON b.room_id = r.room_id
INNER JOIN hotels h ON r.hotel_id = h.hotel_id
WHERE b.check_in BETWEEN '2026-10-01' AND '2026-10-31';

SELECT r.room_type, COUNT(*) AS total_bookings
FROM rooms r
INNER JOIN reservations b ON r.room_id = b.room_id
WHERE b.booking_status <> 'Cancelled'
GROUP BY r.room_type
ORDER BY total_bookings DESC, r.room_type
LIMIT 1;

SELECT ROUND(
    COUNT(CASE WHEN status = 'Occupied' THEN 1 END) * 100.0
    / NULLIF(COUNT(*), 0), 2
) AS occupancy_rate
FROM rooms;

SELECT b.reservation_id, g.guest_name, h.hotel_name,
       r.room_number
FROM reservations b
INNER JOIN guests g ON b.guest_id = g.guest_id
INNER JOIN rooms r ON b.room_id = r.room_id
INNER JOIN hotels h ON r.hotel_id = h.hotel_id
WHERE b.booking_status = 'Cancelled';

SELECT g.guest_name, COUNT(b.reservation_id) AS total_bookings
FROM guests g
INNER JOIN reservations b ON g.guest_id = b.guest_id
GROUP BY g.guest_id, g.guest_name
HAVING COUNT(b.reservation_id) > 1;

SELECT ROUND(AVG(price), 2) AS average_room_price
FROM rooms;

SELECT h.hotel_name, COUNT(r.room_id) AS total_rooms
FROM hotels h
INNER JOIN rooms r ON h.hotel_id = r.hotel_id
GROUP BY h.hotel_id, h.hotel_name
HAVING COUNT(r.room_id) > 100;

SELECT g.guest_name, SUM(p.amount) AS total_spent
FROM guests g
INNER JOIN reservations b ON g.guest_id = b.guest_id
INNER JOIN payment_records p
    ON b.reservation_id = p.reservation_id
WHERE p.payment_status = 'Paid'
GROUP BY g.guest_id, g.guest_name
ORDER BY total_spent DESC
LIMIT 1;

SELECT h.hotel_name, COALESCE(SUM(p.amount), 0) AS revenue
FROM hotels h
INNER JOIN rooms r ON h.hotel_id = r.hotel_id
INNER JOIN reservations b ON r.room_id = b.room_id
INNER JOIN payment_records p
    ON b.reservation_id = p.reservation_id
WHERE p.payment_status = 'Paid'
GROUP BY h.hotel_id, h.hotel_name
ORDER BY revenue DESC;

SELECT h.hotel_name, r.room_number, r.room_type, r.price
FROM rooms r
INNER JOIN hotels h ON r.hotel_id = h.hotel_id
ORDER BY r.price DESC
LIMIT 1;

SELECT g.guest_name
FROM guests g
LEFT JOIN reservations b ON g.guest_id = b.guest_id
WHERE b.reservation_id IS NULL;

SELECT h.hotel_name,
       COALESCE(SUM(p.amount), 0) AS revenue,
       RANK() OVER (
           ORDER BY COALESCE(SUM(p.amount), 0) DESC
       ) AS revenue_rank
FROM hotels h
LEFT JOIN rooms r ON h.hotel_id = r.hotel_id
LEFT JOIN reservations b ON r.room_id = b.room_id
LEFT JOIN payment_records p
    ON b.reservation_id = p.reservation_id
    AND p.payment_status = 'Paid'
GROUP BY h.hotel_id, h.hotel_name
ORDER BY revenue_rank;
