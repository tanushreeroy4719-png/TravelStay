package com.travelstay.service;

import com.travelstay.config.Database;
import java.sql.*;
import java.time.LocalDate;

public class BookingService {

    /**
     * Books a room, guarding against double-booking with a row lock + transaction:
     * the room row is locked (SELECT ... FOR UPDATE) for the duration of the
     * availability check and the insert, so two concurrent requests for the
     * same room can't both pass the overlap check.
     */
    public int bookRoom(int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                        int guests, double total) throws SQLException {
        if (!checkOut.isAfter(checkIn)) throw new IllegalArgumentException("Check-out must be after check-in.");
        if (guests <= 0) throw new IllegalArgumentException("Guests must be at least 1.");

        String lockRoom = "SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE";
        String availability = "SELECT COUNT(*) FROM hotel_bookings WHERE room_id=? AND status='CONFIRMED' " +
                "AND check_in < ? AND check_out > ?";
        String insert = "INSERT INTO hotel_bookings(user_id,room_id,check_in,check_out,guests,total_amount) " +
                "VALUES(?,?,?,?,?,?)";

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(lockRoom)) {
                    ps.setInt(1, roomId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Room does not exist.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(availability)) {
                    ps.setInt(1, roomId);
                    ps.setDate(2, Date.valueOf(checkOut));
                    ps.setDate(3, Date.valueOf(checkIn));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) throw new IllegalStateException("Room is not available for those dates.");
                    }
                }
                int bookingId;
                try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, roomId);
                    ps.setDate(3, Date.valueOf(checkIn));
                    ps.setDate(4, Date.valueOf(checkOut));
                    ps.setInt(5, guests);
                    ps.setDouble(6, total);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        bookingId = rs.getInt(1);
                    }
                }
                c.commit();
                return bookingId;
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Reserves a table, using the same lock-then-check-then-insert transaction
     * pattern as bookRoom to avoid double-booking the same table/time slot.
     */
    public int reserveTable(int userId, int tableId, LocalDate date, Time time, int guests) throws SQLException {
        if (guests <= 0) throw new IllegalArgumentException("Guests must be at least 1.");

        String lockTable = "SELECT table_id FROM restaurant_tables WHERE table_id=? FOR UPDATE";
        String availability = "SELECT COUNT(*) FROM restaurant_bookings WHERE table_id=? AND reservation_date=? " +
                "AND reservation_time=? AND status='CONFIRMED'";
        String insert = "INSERT INTO restaurant_bookings(user_id,table_id,reservation_date,reservation_time,guests) " +
                "VALUES(?,?,?,?,?)";

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(lockTable)) {
                    ps.setInt(1, tableId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Table does not exist.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(availability)) {
                    ps.setInt(1, tableId);
                    ps.setDate(2, Date.valueOf(date));
                    ps.setTime(3, time);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) throw new IllegalStateException("Table is not available at that time.");
                    }
                }
                int reservationId;
                try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, tableId);
                    ps.setDate(3, Date.valueOf(date));
                    ps.setTime(4, time);
                    ps.setInt(5, guests);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        reservationId = rs.getInt(1);
                    }
                }
                c.commit();
                return reservationId;
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Cancels a hotel booking, but only if it belongs to the given user. */
    public boolean cancelHotelBooking(int bookingId, int userId) throws SQLException {
        String sql = "UPDATE hotel_bookings SET status='CANCELLED' WHERE booking_id=? AND user_id=? AND status='CONFIRMED'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cancels a restaurant reservation, but only if it belongs to the given user. */
    public boolean cancelRestaurantBooking(int reservationId, int userId) throws SQLException {
        String sql = "UPDATE restaurant_bookings SET status='CANCELLED' WHERE reservation_id=? AND user_id=? AND status='CONFIRMED'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
