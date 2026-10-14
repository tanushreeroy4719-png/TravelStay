package com.travelstay.dao;

import com.travelstay.config.Database;
import com.travelstay.model.User;
import com.travelstay.util.PasswordUtil;
import java.sql.*;

public class AuthDAO {

    /** Returns the matching user, or null if the email/password combination is invalid. */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, full_name, email, role, password FROM users WHERE email=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String storedHash = rs.getString(5);
                if (!PasswordUtil.verify(password, storedHash)) return null;
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
    }

    /**
     * Registers a new customer account.
     * @throws IllegalStateException if the email is already registered.
     */
    public User register(String fullName, String email, String password, String phone) throws SQLException {
        String checkSql = "SELECT 1 FROM users WHERE email=?";
        String insertSql = "INSERT INTO users(full_name, email, password, phone, role) VALUES(?,?,?,?,'CUSTOMER')";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(checkSql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) throw new IllegalStateException("An account with this email already exists.");
                }
            }
            String hashed = PasswordUtil.hash(password);
            try (PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, hashed);
                ps.setString(4, phone);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return new User(rs.getInt(1), fullName, email, "CUSTOMER");
                }
            }
        }
    }
}
