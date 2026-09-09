# TravelStay — Hotel & Restaurant Booking System

Portfolio starter project built with **Java 25, JavaFX 25, Maven, JDBC and MySQL**.

## Features

### Customer
- Register and login
- Search hotels by city
- View hotel and room details
- Check room availability
- Create hotel bookings
- Search restaurants by city
- View restaurant details
- Reserve restaurant tables
- View and cancel bookings

### Admin-ready architecture
The project is organized into model, DAO, service and UI layers so an admin dashboard can be added cleanly.

## Tech stack

Java 25 • JavaFX 25 • MySQL • JDBC • Maven • IntelliJ IDEA • XAMPP

## Setup

1. Install Java 25.
2. Install IntelliJ IDEA.
3. Install XAMPP.
4. Start **MySQL** in XAMPP.
5. Open phpMyAdmin.
6. Import `database/travelstay.sql`.
7. Open this folder in IntelliJ as a Maven project.
8. Check `src/main/java/com/travelstay/config/Database.java`.
9. If your XAMPP MySQL password is not blank, change the password there.
10. Run:

```bash
mvn clean javafx:run
```

## Demo account

Username/email: `admin@travelstay.com`
Password: `admin123`

## Database

The SQL script creates:
- users
- locations
- hotels
- rooms
- restaurants
- restaurant_tables
- hotel_bookings
- restaurant_bookings
- payments

The sample data includes Ahmedabad, Mumbai, Delhi, Bengaluru, Jaipur, Goa, Hyderabad, Chennai, Kolkata and Pune.

## Portfolio improvements

Next add:
- Admin dashboard
- Images
- Ratings and reviews
- Coupons
- Advanced filters
- Payment gateway/mock payment
- Reports
- Booking confirmation PDF
- Email notifications
- Role-based access

## Changelog

**Security / correctness fixes:**
- Passwords are now hashed with PBKDF2WithHmacSHA256 (`com.travelstay.util.PasswordUtil`) instead of
  stored/compared in plaintext. The demo accounts still log in with `admin123` / `customer123` — only
  the stored representation changed.
- Room and table booking now run inside a transaction with a `SELECT ... FOR UPDATE` row lock, closing
  a race condition where two concurrent bookings could both pass the availability check and double-book
  the same room/table.
- UI no longer shows raw exception/SQL error text to the user; errors are logged server-side and a
  generic message is shown instead.

**Previously-missing features, now implemented:**
- Registration screen (`RegisterView`) + `AuthDAO.register(...)`.
- Hotel search → "Book a Room" now opens a real booking dialog (room, dates, guests) that calls
  `BookingService.bookRoom(...)`.
- Restaurant search → "Reserve Table" now opens a real reservation dialog that calls
  `BookingService.reserveTable(...)`.
- "My Bookings" screen listing hotel bookings and restaurant reservations, with cancel support.
