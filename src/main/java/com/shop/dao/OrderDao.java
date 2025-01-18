package com.shop.dao;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.PageResult;
import com.shop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    
    public Order save(Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, order.getOrderNo());
            stmt.setLong(2, order.getUserId());
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getReceiverName());
            stmt.setString(6, order.getReceiverPhone());
            stmt.setString(7, order.getReceiverAddress());
            
            stmt.executeUpdate();
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                order.setId(rs.getLong(1));
            }
            
            // 保存订单项
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS);
                
                for (OrderItem item : order.getItems()) {
                    stmt.setLong(1, order.getId());
                    stmt.setLong(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setBigDecimal(4, item.getPrice());
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
            }
            
            conn.commit();
            return order;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBUtil.close(conn, stmt, rs);
        }
    }
    
    public Order findById(Long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getLong("id"));
                    order.setOrderNo(rs.getString("order_no"));
                    order.setUserId(rs.getLong("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 获取订单项
                    order.setItems(findOrderItems(order.getId()));
                    
                    return order;
                }
            }
        }
        return null;
    }
    
    public Order findByOrderNo(String orderNo) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_no = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderNo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getLong("id"));
                    order.setOrderNo(rs.getString("order_no"));
                    order.setUserId(rs.getLong("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 获取订单项
                    order.setItems(findOrderItems(order.getId()));
                    
                    return order;
                }
            }
        }
        
        return null;
    }
    
    public List<Order> findByUserId(Long userId) throws SQLException {
        System.out.println("Finding orders for user: " + userId);
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            System.out.println("Executing SQL: " + sql + " with userId: " + userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getLong("id"));
                    order.setOrderNo(rs.getString("order_no")); // 确保正确设置 orderNo
                    order.setUserId(rs.getLong("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    System.out.println("Found order: " + order.getId() + ", order_no: " + order.getOrderNo());
                    
                    // 获取订单项
                    List<OrderItem> items = findOrderItems(order.getId());
                    System.out.println("Found " + items.size() + " items for order: " + order.getId());
                    order.setItems(items);
                    
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding orders: " + e.getMessage());
            throw e;
        }
        
        System.out.println("Returning " + orders.size() + " orders");
        return orders;
    }
    
    public int countByUserId(Long userId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE user_id = ?");
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            stmt.setLong(paramIndex++, userId);
            if (status != null && !status.isEmpty()) {
                stmt.setString(paramIndex, status);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    public void updateStatus(Long id, String status) throws SQLException {
        if (id == null || status == null) {
            throw new SQLException("订单ID和状态不能为空");
        }

        // 先检查订单是否存在
        Order order = findById(id);
        if (order == null) {
            throw new SQLException("订单不存在");
        }

        // 验证状态转换的合法性
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new SQLException("非法的状态转换: " + order.getStatus() + " -> " + status);
        }

        String sql = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setLong(2, id);
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("更新订单状态失败");
            }
        }
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 定义合法的状态转换
        switch (currentStatus) {
            case "pending":
                return "processing".equals(newStatus) || "cancelled".equals(newStatus);
            case "processing":
                return "shipped".equals(newStatus) || "cancelled".equals(newStatus);
            case "shipped":
                return "delivered".equals(newStatus);
            case "delivered":
                return "completed".equals(newStatus);
            case "cancelled":
                return false; // 取消状态是终态
            case "completed":
                return false; // 完成状态是终态
            default:
                return false;
        }
    }
    
    public PageResult<Order> findAll(int page, int pageSize, String status, String orderNo, String userId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT SQL_CALC_FOUND_ROWS * FROM orders WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加筛选条件
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            sql.append(" AND order_no LIKE ?");
            params.add("%" + orderNo + "%");
        }
        if (userId != null && !userId.isEmpty()) {
            sql.append(" AND user_id = ?");
            params.add(Long.parseLong(userId));
        }
        
        // 添加排序和分页
        sql.append(" ORDER BY created_at DESC LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);
        
        List<Order> orders = new ArrayList<>();
        int total = 0;
        
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
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setOrderNo(rs.getString("order_no"));
                order.setUserId(rs.getLong("user_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setReceiverName(rs.getString("receiver_name"));
                order.setReceiverPhone(rs.getString("receiver_phone"));
                order.setReceiverAddress(rs.getString("receiver_address"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                order.setUpdatedAt(rs.getTimestamp("updated_at"));
                
                // 获取订单项
                order.setItems(findOrderItems(order.getId()));
                
                orders.add(order);
            }
            
            // 获取总记录数
            try (PreparedStatement countStmt = conn.prepareStatement("SELECT FOUND_ROWS()");
                 ResultSet countRs = countStmt.executeQuery()) {
                if (countRs.next()) {
                    total = countRs.getInt(1);
                }
            }
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        
        return new PageResult<Order>(orders, total, page, pageSize);
    }
    
    private List<OrderItem> findOrderItems(Long orderId) throws SQLException {
        String sql = "SELECT oi.*, p.name as product_name FROM order_items oi " +
                    "LEFT JOIN products p ON oi.product_id = p.id " +
                    "WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
