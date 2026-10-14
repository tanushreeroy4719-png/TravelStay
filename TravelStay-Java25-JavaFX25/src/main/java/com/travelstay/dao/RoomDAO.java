package com.travelstay.dao;

import com.travelstay.config.Database;
import com.travelstay.model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> findByHotel(int hotelId) throws SQLException {
        String sql = "SELECT room_id, hotel_id, room_type, price_per_night, capacity, total_rooms " +
                     "FROM rooms WHERE hotel_id=? ORDER BY price_per_night";
        List<Room> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Room(rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getDouble(4), rs.getInt(5), rs.getInt(6)));
                }
            }
        }
        return result;
    }
}
