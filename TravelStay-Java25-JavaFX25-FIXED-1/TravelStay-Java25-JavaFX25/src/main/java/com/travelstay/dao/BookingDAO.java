package com.travelstay.dao;

import com.travelstay.config.Database;
import com.travelstay.model.HotelBooking;
import com.travelstay.model.RestaurantBooking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<HotelBooking> findHotelBookingsByUser(int userId) throws SQLException {
        String sql = "SELECT b.booking_id, h.hotel_name, r.room_type, b.check_in, b.check_out, " +
                     "b.guests, b.total_amount, b.status " +
                     "FROM hotel_bookings b " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "JOIN hotels h ON r.hotel_id = h.hotel_id " +
                     "WHERE b.user_id = ? ORDER BY b.booked_at DESC";
        List<HotelBooking> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new HotelBooking(
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getDate(4).toLocalDate(), rs.getDate(5).toLocalDate(),
                            rs.getInt(6), rs.getDouble(7), rs.getString(8)));
                }
            }
        }
        return result;
    }

    public List<RestaurantBooking> findRestaurantBookingsByUser(int userId) throws SQLException {
        String sql = "SELECT rb.reservation_id, res.restaurant_name, rt.table_number, " +
                     "rb.reservation_date, rb.reservation_time, rb.guests, rb.status " +
                     "FROM restaurant_bookings rb " +
                     "JOIN restaurant_tables rt ON rb.table_id = rt.table_id " +
                     "JOIN restaurants res ON rt.restaurant_id = res.restaurant_id " +
                     "WHERE rb.user_id = ? ORDER BY rb.booked_at DESC";
        List<RestaurantBooking> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new RestaurantBooking(
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getDate(4).toLocalDate(), rs.getTime(5).toLocalTime(),
                            rs.getInt(6), rs.getString(7)));
                }
            }
        }
        return result;
    }
}
