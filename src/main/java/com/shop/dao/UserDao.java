package com.shop.dao;

import com.shop.model.User;
import com.shop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getInt("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getInt("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public void updatePassword(Long id, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPassword);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新密码失败", e);
        } finally {
            DBUtil.close(conn, stmt, null);
        }
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, role, status, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getStatus());
            
            int rows = stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getLong(1));
            }
            
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("保存用户失败", e);
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public List<User> findAll(int page, int pageSize, String username) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT SQL_CALC_FOUND_ROWS * FROM users WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        
        // 添加搜索条件
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + username.trim() + "%");
        }
        
        // 添加排序和分页
        sql.append(" ORDER BY created_at DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);
        
        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql.toString());
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            // 执行查询
            rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getInt("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                // 不返回密码
                users.add(user);
            }
            
            return users;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    public int count(String username) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + username.trim() + "%");
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean delete(Long id) throws SQLException {
        // 检查是否是管理员
        User user = findById(id);
        if (user == null) {
            throw new SQLException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new SQLException("不能删除管理员账号");
        }

        String sql = "DELETE FROM users WHERE id = ? AND role != 'admin'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public boolean updateStatus(Long id, int status) throws SQLException {
        // 检查是否是管理员
        User user = findById(id);
        if (user == null) {
            throw new SQLException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new SQLException("不能修改管理员账号状态");
        }

        String sql = "UPDATE users SET status = ? WHERE id = ? AND role != 'admin'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, status);
            stmt.setLong(2, id);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }
}
