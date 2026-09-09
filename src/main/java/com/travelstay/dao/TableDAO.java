package com.travelstay.dao;

import com.travelstay.config.Database;
import com.travelstay.model.RestaurantTable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    public List<RestaurantTable> findByRestaurant(int restaurantId) throws SQLException {
        String sql = "SELECT table_id, restaurant_id, table_number, seats " +
                     "FROM restaurant_tables WHERE restaurant_id=? ORDER BY seats";
        List<RestaurantTable> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new RestaurantTable(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4)));
                }
            }
        }
        return result;
    }
}
