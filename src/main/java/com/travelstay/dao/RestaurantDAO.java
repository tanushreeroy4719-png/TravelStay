package com.travelstay.dao;

import com.travelstay.config.Database;
import com.travelstay.model.Restaurant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    public List<Restaurant> findByCity(String city) throws SQLException {
        String sql = "SELECT r.restaurant_id,r.restaurant_name,l.city,r.cuisine,r.address,r.rating " +
                     "FROM restaurants r JOIN locations l ON r.location_id=l.location_id " +
                     "WHERE l.city LIKE ? ORDER BY r.rating DESC";
        List<Restaurant> result = new ArrayList<>();
        try (Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1, "%" + city + "%");
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) result.add(new Restaurant(rs.getInt(1),rs.getString(2),rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getDouble(6)));
            }
        }
        return result;
    }
}
