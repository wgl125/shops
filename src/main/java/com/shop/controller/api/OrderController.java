package com.shop.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dao.CartItemDao;
import com.shop.dao.OrderDao;
import com.shop.dao.ProductDao;
import com.shop.model.*;
import com.shop.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/api/orders/*")
public class OrderController extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final CartItemDao cartItemDao = new CartItemDao();
    private final ProductDao productDao = new ProductDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        User user = (User) req.getAttribute("user");

        // 设置响应类型
        resp.setContentType("application/json;charset=UTF-8");

        try {
            System.out.println("Received POST request with pathInfo: " + pathInfo);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // 兼容旧接口
                createOrderFromCart(req, resp, user);
            } else if (pathInfo.equals("/cart")) {
                // 从购物车创建订单
                createOrderFromCart(req, resp, user);
            } else if (pathInfo.equals("/direct")) {
                // 直接从商品创建订单
                createOrderFromProduct(req, resp, user);
            } else if (pathInfo.matches("/\\d+/cancel")) {
                // 取消订单
                cancelOrder(req, resp, user);
            } else if (pathInfo.matches("/\\d+/complete")) {
                // 确认收货
                completeOrder(req, resp, user);
            } else if (pathInfo.startsWith("/pay/")) {
                // 支付订单
                payOrder(req, resp, user);
            } else {
                System.out.println("No matching path found for: " + pathInfo);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        User user = (User) req.getAttribute("user");

        // 设置响应类型
        resp.setContentType("application/json;charset=UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取订单列表
                getOrderList(req, resp, user);
            } else if (pathInfo.matches("/\\d+")) {
                // 获取订单详情
                getOrderDetail(req, resp, user);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    private void createOrderFromCart(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        // 获取表单参数
        String receiverName = req.getParameter("receiver_name");
        String receiverPhone = req.getParameter("receiver_phone");
        String receiverAddress = req.getParameter("receiver_address");

        // 验证必填参数
        if (receiverName == null || receiverName.trim().isEmpty() ||
            receiverPhone == null || receiverPhone.trim().isEmpty() ||
            receiverAddress == null || receiverAddress.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "收货信息不完整");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 获取购物车商品
        List<CartItem> cartItems = cartItemDao.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "购物车为空");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 检查商品库存
        for (CartItem cartItem : cartItems) {
            Product product = productDao.findById(cartItem.getProductId());
            if (product.getStock() < cartItem.getQuantity()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "商品" + product.getName() + "库存不足");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.getId());
        order.setStatus("pending");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);

        // 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productDao.findById(cartItem.getProductId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            // 计算总金额
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));

            // 扣减库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productDao.updateStock(product.getId(), product.getStock());
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        // 保存订单
        order = orderDao.save(order);

        // 清空购物车
        cartItemDao.deleteByUserId(user.getId());

        // 返回成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单创建成功");
        result.put("data", order);
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void createOrderFromProduct(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        // 获取表单参数
        String productIdStr = req.getParameter("product_id");
        String quantityStr = req.getParameter("quantity");
        String receiverName = req.getParameter("receiver_name");
        String receiverPhone = req.getParameter("receiver_phone");
        String receiverAddress = req.getParameter("receiver_address");

        // 验证必填参数
        if (productIdStr == null || quantityStr == null ||
            receiverName == null || receiverName.trim().isEmpty() ||
            receiverPhone == null || receiverPhone.trim().isEmpty() ||
            receiverAddress == null || receiverAddress.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "参数不完整");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 转换参数
        long productId;
        int quantity;
        try {
            productId = Long.parseLong(productIdStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "参数格式错误");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 验证数量
        if (quantity <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "购买数量必须大于0");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 检查商品是否存在
        Product product = productDao.findById(productId);
        if (product == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "商品不存在");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 检查库存
        if (product.getStock() < quantity) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "商品库存不足");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.getId());
        order.setStatus("pending");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setPrice(product.getPrice());
        orderItem.setQuantity(quantity);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setItems(orderItems);

        // 计算总金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
        order.setTotalAmount(totalAmount);

        // 扣减库存
        product.setStock(product.getStock() - quantity);
        productDao.updateStock(product.getId(), product.getStock());

        // 保存订单
        order = orderDao.save(order);

        // 返回成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单创建成功");
        result.put("data", order);
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void getOrderList(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        List<Order> orders = orderDao.findByUserId(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("total", orders.size());
        data.put("list", orders);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取订单列表成功");
        result.put("data", data);
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void getOrderDetail(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        String pathInfo = req.getPathInfo();
        Long orderId = Long.parseLong(pathInfo.substring(1));

        Order order = orderDao.findById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取订单详情成功");
        result.put("data", order);
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        String pathInfo = req.getPathInfo();
        Long orderId = Long.parseLong(pathInfo.substring(1, pathInfo.indexOf("/cancel")));

        Order order = orderDao.findById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!"pending".equals(order.getStatus())) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "只能取消待付款的订单");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 恢复库存
        for (OrderItem item : order.getItems()) {
            Product product = productDao.findById(item.getProductId());
            productDao.updateStock(product.getId(), product.getStock() + item.getQuantity());
        }

        orderDao.updateStatus(orderId, "cancelled");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "订单取消成功");
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void completeOrder(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        String pathInfo = req.getPathInfo();
        Long orderId = Long.parseLong(pathInfo.substring(1, pathInfo.indexOf("/complete")));

        Order order = orderDao.findById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // if (!"shipped".equals(order.getStatus())) {
        //     Map<String, Object> error = new HashMap<>();
        //     error.put("code", 400);
        //     error.put("message", "只能确认已发货的订单");
        //     resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        //     resp.getWriter().write(objectMapper.writeValueAsString(error));
        //     return;
        // }

        orderDao.updateStatus(orderId, "completed");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "确认收货成功");
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void payOrder(HttpServletRequest req, HttpServletResponse resp, User user) throws SQLException, IOException {
        String pathInfo = req.getPathInfo();
        String orderNo = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);

        Order order = orderDao.findByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(user.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 404);
            error.put("message", "订单不存在");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        if (!"pending".equals(order.getStatus())) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "只能支付待付款的订单");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // 模拟支付过程，直接更新订单状态为已支付
        orderDao.updateStatus(order.getId(), "paid");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "支付成功");
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String random = String.format("%03d", new Random().nextInt(1000));
        return timestamp + random;
    }
}
