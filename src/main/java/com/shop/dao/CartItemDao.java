package com.shop.dao;

import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartItemDao {
    
    public void insert(CartItem cartItem) throws SQLException {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, cartItem.getUserId());
            stmt.setLong(2, cartItem.getProductId());
            stmt.setInt(3, cartItem.getQuantity());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cartItem.setId(rs.getLong(1));
                }
            }
        }
    }
    
    public void update(CartItem cartItem) throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, cartItem.getQuantity());
            stmt.setLong(2, cartItem.getId());
            stmt.setLong(3, cartItem.getUserId());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            
            stmt.executeUpdate();
        }
    }
    
    public List<CartItem> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT ci.*, p.name as product_name, p.description as product_description, " +
                    "p.price as product_price, p.stock as product_stock, p.image_url as product_image_url, " +
                    "p.status as product_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN products p ON ci.product_id = p.id " +
                    "WHERE ci.user_id = ?";
        
        List<CartItem> cartItems = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem cartItem = new CartItem();
                    cartItem.setId(rs.getLong("id"));
                    cartItem.setUserId(rs.getLong("user_id"));
                    cartItem.setProductId(rs.getLong("product_id"));
                    cartItem.setQuantity(rs.getInt("quantity"));
                    cartItem.setCreatedAt(rs.getTimestamp("created_at"));
                    cartItem.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 设置商品信息
                    Product product = new Product();
                    product.setId(rs.getLong("product_id"));
                    product.setName(rs.getString("product_name"));
                    product.setDescription(rs.getString("product_description"));
                    product.setPrice(rs.getBigDecimal("product_price"));
                    product.setStock(rs.getInt("product_stock"));
                    product.setImageUrl(rs.getString("product_image_url"));
                    product.setStatus(rs.getInt("product_status"));
                    
                    cartItem.setProduct(product);
                    cartItems.add(cartItem);
                }
            }
        }
        
        return cartItems;
    }
    
    public CartItem findById(Long id, Long userId) throws SQLException {
        String sql = "SELECT ci.*, p.name as product_name, p.description as product_description, " +
                    "p.price as product_price, p.stock as product_stock, p.image_url as product_image_url, " +
                    "p.status as product_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN products p ON ci.product_id = p.id " +
                    "WHERE ci.id = ? AND ci.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CartItem cartItem = new CartItem();
                    cartItem.setId(rs.getLong("id"));
                    cartItem.setUserId(rs.getLong("user_id"));
                    cartItem.setProductId(rs.getLong("product_id"));
                    cartItem.setQuantity(rs.getInt("quantity"));
                    cartItem.setCreatedAt(rs.getTimestamp("created_at"));
                    cartItem.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 设置商品信息
                    Product product = new Product();
                    product.setId(rs.getLong("product_id"));
                    product.setName(rs.getString("product_name"));
                    product.setDescription(rs.getString("product_description"));
                    product.setPrice(rs.getBigDecimal("product_price"));
                    product.setStock(rs.getInt("product_stock"));
                    product.setImageUrl(rs.getString("product_image_url"));
                    product.setStatus(rs.getInt("product_status"));
                    
                    cartItem.setProduct(product);
                    return cartItem;
                }
            }
        }
        
        return null;
    }
    
    public CartItem findByProductId(Long productId, Long userId) throws SQLException {
        String sql = "SELECT ci.*, p.name as product_name, p.description as product_description, " +
                    "p.price as product_price, p.stock as product_stock, p.image_url as product_image_url, " +
                    "p.status as product_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN products p ON ci.product_id = p.id " +
                    "WHERE ci.product_id = ? AND ci.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, productId);
            stmt.setLong(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CartItem cartItem = new CartItem();
                    cartItem.setId(rs.getLong("id"));
                    cartItem.setUserId(rs.getLong("user_id"));
                    cartItem.setProductId(rs.getLong("product_id"));
                    cartItem.setQuantity(rs.getInt("quantity"));
                    cartItem.setCreatedAt(rs.getTimestamp("created_at"));
                    cartItem.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    // 设置商品信息
                    Product product = new Product();
                    product.setId(rs.getLong("product_id"));
                    product.setName(rs.getString("product_name"));
                    product.setDescription(rs.getString("product_description"));
                    product.setPrice(rs.getBigDecimal("product_price"));
                    product.setStock(rs.getInt("product_stock"));
                    product.setImageUrl(rs.getString("product_image_url"));
                    product.setStatus(rs.getInt("product_status"));
                    
                    cartItem.setProduct(product);
                    return cartItem;
                }
            }
        }
        
        return null;
    }
    
    public void deleteByUserId(Long userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }
}
