package com.shop.controller.api;

import com.shop.dao.CartItemDao;
import com.shop.dao.ProductDao;
import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.model.Result;
import com.shop.model.User;
import com.shop.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/cart/*")
public class CartController extends HttpServlet {
    private CartItemDao cartItemDao = new CartItemDao();
    private ProductDao productDao = new ProductDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 获取当前用户
        User user = (User) request.getAttribute("user");
        if (user == null) {
            JsonUtil.writeJsonResponse(response, Result.error("请先登录"));
            return;
        }
        
        try {
            // 获取购物车列表
            List<CartItem> cartItems = cartItemDao.findByUserId(user.getId());
            JsonUtil.writeJsonResponse(response, Result.success("获取购物车成功", cartItems));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("获取购物车失败: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 获取当前用户
        User user = (User) request.getAttribute("user");
        if (user == null) {
            JsonUtil.writeJsonResponse(response, Result.error("请先登录"));
            return;
        }
        
        try {
            // 获取商品ID和数量
            String productIdStr = request.getParameter("productId");
            String quantityStr = request.getParameter("quantity");
            
            if (productIdStr == null || quantityStr == null) {
                JsonUtil.writeJsonResponse(response, Result.error("参数不完整"));
                return;
            }
            
            Long productId;
            int quantity;
            try {
                productId = Long.parseLong(productIdStr);
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("参数格式不正确"));
                return;
            }
            
            if (quantity <= 0) {
                JsonUtil.writeJsonResponse(response, Result.error("商品数量必须大于0"));
                return;
            }
            
            // 检查商品是否存在且在售
            Product product = productDao.findById(productId);
            if (product == null) {
                JsonUtil.writeJsonResponse(response, Result.error("商品不存在"));
                return;
            }
            if (product.getStatus() != 1) {
                JsonUtil.writeJsonResponse(response, Result.error("商品已下架"));
                return;
            }
            if (product.getStock() < quantity) {
                JsonUtil.writeJsonResponse(response, Result.error("商品库存不足"));
                return;
            }
            
            // 检查购物车是否已有此商品
            CartItem existingItem = cartItemDao.findByProductId(productId, user.getId());
            if (existingItem != null) {
                // 更新数量
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    JsonUtil.writeJsonResponse(response, Result.error("商品库存不足"));
                    return;
                }
                existingItem.setQuantity(newQuantity);
                cartItemDao.update(existingItem);
                JsonUtil.writeJsonResponse(response, Result.success("更新购物车成功", existingItem));
            } else {
                // 新增购物车项
                CartItem cartItem = new CartItem();
                cartItem.setUserId(user.getId());
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);
                cartItemDao.insert(cartItem);
                cartItem.setProduct(product);
                JsonUtil.writeJsonResponse(response, Result.success("添加到购物车成功", cartItem));
            }
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("操作购物车失败: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 获取当前用户
        User user = (User) request.getAttribute("user");
        if (user == null) {
            JsonUtil.writeJsonResponse(response, Result.error("请先登录"));
            return;
        }
        
        try {
            // 获取购物车项ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeJsonResponse(response, Result.error("购物车项ID不能为空"));
                return;
            }
            
            Long cartItemId;
            try {
                cartItemId = Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("购物车项ID格式不正确"));
                return;
            }
            
            // 从请求体中读取参数
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            // 解析参数
            String[] params = sb.toString().split("&");
            String quantityStr = null;
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("quantity")) {
                    quantityStr = pair[1];
                    break;
                }
            }
            
            if (quantityStr == null) {
                JsonUtil.writeJsonResponse(response, Result.error("数量不能为空"));
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("数量格式不正确"));
                return;
            }
            
            if (quantity <= 0) {
                JsonUtil.writeJsonResponse(response, Result.error("商品数量必须大于0"));
                return;
            }
            
            // 获取购物车项
            CartItem cartItem = cartItemDao.findById(cartItemId, user.getId());
            if (cartItem == null) {
                JsonUtil.writeJsonResponse(response, Result.error("购物车项不存在"));
                return;
            }
            
            // 检查商品库存
            Product product = productDao.findById(cartItem.getProductId());
            if (product == null) {
                JsonUtil.writeJsonResponse(response, Result.error("商品不存在"));
                return;
            }
            if (product.getStatus() != 1) {
                JsonUtil.writeJsonResponse(response, Result.error("商品已下架"));
                return;
            }
            if (product.getStock() < quantity) {
                JsonUtil.writeJsonResponse(response, Result.error("商品库存不足"));
                return;
            }
            
            // 更新数量
            cartItem.setQuantity(quantity);
            cartItemDao.update(cartItem);
            cartItem.setProduct(product);
            
            JsonUtil.writeJsonResponse(response, Result.success("更新购物车成功", cartItem));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("更新购物车失败: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 获取当前用户
        User user = (User) request.getAttribute("user");
        if (user == null) {
            JsonUtil.writeJsonResponse(response, Result.error("请先登录"));
            return;
        }
        
        try {
            // 获取购物车项ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeJsonResponse(response, Result.error("购物车项ID不能为空"));
                return;
            }
            
            Long cartItemId;
            try {
                cartItemId = Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("购物车项ID格式不正确"));
                return;
            }
            
            // 删除购物车项
            cartItemDao.delete(cartItemId, user.getId());
            JsonUtil.writeJsonResponse(response, Result.success("删除购物车项成功"));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("删除购物车项失败: " + e.getMessage()));
        }
    }
}
