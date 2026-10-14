CREATE DATABASE IF NOT EXISTS travelstay;
USE travelstay;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS restaurant_bookings;
DROP TABLE IF EXISTS hotel_bookings;
DROP TABLE IF EXISTS restaurant_tables;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS hotels;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('CUSTOMER','ADMIN') DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE locations (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(80) NOT NULL UNIQUE,
    state VARCHAR(80) NOT NULL
);

CREATE TABLE hotels (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    location_id INT NOT NULL,
    hotel_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    rating DECIMAL(2,1) DEFAULT 4.0,
    description TEXT,
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

CREATE TABLE rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    room_type VARCHAR(80) NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    total_rooms INT NOT NULL DEFAULT 1,
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id)
);

CREATE TABLE restaurants (
    restaurant_id INT PRIMARY KEY AUTO_INCREMENT,
    location_id INT NOT NULL,
    restaurant_name VARCHAR(150) NOT NULL,
    cuisine VARCHAR(100),
    address VARCHAR(255),
    rating DECIMAL(2,1) DEFAULT 4.0,
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

CREATE TABLE restaurant_tables (
    table_id INT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id INT NOT NULL,
    table_number VARCHAR(20) NOT NULL,
    seats INT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);

CREATE TABLE hotel_bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

CREATE TABLE restaurant_bookings (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    table_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    guests INT NOT NULL,
    status ENUM('CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    booking_type ENUM('HOTEL','RESTAURANT') NOT NULL,
    reference_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'MOCK',
    payment_status ENUM('SUCCESS','FAILED','REFUNDED') DEFAULT 'SUCCESS',
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Passwords below are PBKDF2WithHmacSHA256 hashes (see com.travelstay.util.PasswordUtil),
-- NOT plaintext. The real password for each demo account is still admin123 / customer123 —
-- PasswordUtil.verify() re-derives the hash from the typed password and compares it.
INSERT INTO users(full_name,email,password,phone,role) VALUES
('TravelStay Admin','admin@travelstay.com','PBKDF2:65536:F8GmdmdTmGDz4+VvRPvbNA==:n7O0H88SD1qHi8qpRnstwx+Q9ljwj/jrmD+oJYzDw7w=','9999999999','ADMIN'),
('Demo Customer','customer@travelstay.com','PBKDF2:65536:dtUwcHuGEgySdgl655prnA==:/ztPbHoWFXaVfsctFBBt6IXG/wkm8ZhWge5hfZl0K0Y=','8888888888','CUSTOMER');

INSERT INTO locations(city,state) VALUES
('Ahmedabad','Gujarat'),('Mumbai','Maharashtra'),('Delhi','Delhi'),
('Bengaluru','Karnataka'),('Jaipur','Rajasthan'),('Goa','Goa'),
('Hyderabad','Telangana'),('Chennai','Tamil Nadu'),('Kolkata','West Bengal'),('Pune','Maharashtra');

INSERT INTO hotels(location_id,hotel_name,address,rating,description) VALUES
(1,'Riverfront Grand Hotel','Ashram Road, Ahmedabad',4.5,'Modern hotel near Sabarmati Riverfront.'),
(1,'Heritage Inn Ahmedabad','Navrangpura, Ahmedabad',4.2,'Comfortable city hotel with business facilities.'),
(2,'Marine View Palace','Marine Drive, Mumbai',4.6,'Premium stay near Marine Drive.'),
(3,'Capital Residency','Connaught Place, Delhi',4.4,'Central Delhi hotel for business and leisure.'),
(4,'Bangalore Tech Suites','Whitefield, Bengaluru',4.5,'Modern suites close to technology hubs.'),
(5,'Pink City Palace','C-Scheme, Jaipur',4.7,'Heritage-inspired hotel in Jaipur.'),
(6,'Beachside Retreat','Calangute, Goa',4.6,'Relaxed stay near the beach.'),
(7,'Deccan Grand Hyderabad','Banjara Hills, Hyderabad',4.3,'Business and leisure hotel.'),
(8,'Marina Comfort Chennai','T Nagar, Chennai',4.2,'Convenient city accommodation.'),
(9,'City of Joy Residency','Park Street, Kolkata',4.4,'Central Kolkata stay.'),
(10,'Pune Central Hotel','Shivajinagar, Pune',4.3,'Comfortable hotel near central Pune.');

INSERT INTO rooms(hotel_id,room_type,price_per_night,capacity,total_rooms) VALUES
(1,'Deluxe Room',3500,2,8),(1,'Premium Suite',6000,4,4),
(2,'Standard Room',2200,2,10),(2,'Deluxe Room',3200,3,6),
(3,'Sea View Room',7500,2,6),(3,'Executive Suite',12000,4,3),
(4,'Business Room',4200,2,10),(4,'Family Suite',7000,4,5),
(5,'Deluxe Heritage Room',5000,2,7),(5,'Royal Suite',9000,4,3),
(6,'Garden Room',4500,2,8),(6,'Beach Suite',8500,4,4),
(7,'Executive Room',3800,2,8),(8,'Premium Room',3600,2,8),
(9,'City Room',3000,2,10),(10,'Deluxe Room',3300,2,8);

INSERT INTO restaurants(location_id,restaurant_name,cuisine,address,rating) VALUES
(1,'The Spice Route','Indian & North Indian','SG Highway, Ahmedabad',4.6),
(1,'Green Leaf Bistro','Multi Cuisine','Satellite, Ahmedabad',4.3),
(2,'Mumbai Tadka','Indian','Bandra West, Mumbai',4.5),
(3,'Delhi Darbar','Mughlai & North Indian','Connaught Place, Delhi',4.4),
(4,'Bangalore Bites','South Indian & Continental','Indiranagar, Bengaluru',4.6),
(5,'Royal Thali House','Rajasthani','MI Road, Jaipur',4.7),
(6,'Goa Sunset Kitchen','Goan & Seafood','Calangute, Goa',4.8),
(7,'Hyderabad Spice Hub','Hyderabadi','Banjara Hills, Hyderabad',4.6),
(8,'Marina Food House','South Indian','T Nagar, Chennai',4.3),
(9,'Kolkata Kitchen','Bengali','Park Street, Kolkata',4.5),
(10,'Pune Plate','Indian & Asian','Koregaon Park, Pune',4.4);

INSERT INTO restaurant_tables(restaurant_id,table_number,seats) VALUES
(1,'T1',2),(1,'T2',4),(1,'T3',6),(1,'T4',8),
(2,'T1',2),(2,'T2',4),(3,'T1',4),(3,'T2',6),
(4,'T1',2),(4,'T2',4),(5,'T1',4),(5,'T2',8),
(6,'T1',2),(6,'T2',4),(6,'T3',6),(7,'T1',4),
(8,'T1',4),(9,'T1',2),(10,'T1',4),(11,'T1',4);
