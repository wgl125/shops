package com.shop.controller.api;

import com.shop.dao.OrderDao;
import com.shop.dao.OrderItemDao;
import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.Result;
import com.shop.model.User;
import com.shop.model.PageResult;
import com.shop.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/api/admin/orders/*")
public class AdminOrderController extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) request.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        // 设置响应类型
        response.setContentType("application/json;charset=UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            
            // 获取所有订单（带分页和筛选）
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取分页参数
                int page = 1;
                int pageSize = 10;
                try {
                    String pageStr = request.getParameter("page");
                    String pageSizeStr = request.getParameter("pageSize");
                    if (pageStr != null) {
                        page = Integer.parseInt(pageStr);
                    }
                    if (pageSizeStr != null) {
                        pageSize = Integer.parseInt(pageSizeStr);
                    }
                } catch (NumberFormatException e) {
                    JsonUtil.writeJsonResponse(response, Result.error("无效的分页参数"));
                    return;
                }

                // 获取筛选参数
                String status = request.getParameter("status");
                String userId = request.getParameter("userId");
                String orderNo = request.getParameter("orderNo");

                // 调用 DAO 获取订单列表
                PageResult<Order> orders = orderDao.findAll(page, pageSize, status, orderNo, userId);
                JsonUtil.writeJsonResponse(response, Result.success(orders));
            } 
            // 获取单个订单详情
            else {
                try {
                    Long orderId = Long.parseLong(pathInfo.substring(1));
                    Order order = orderDao.findById(orderId);
                    if (order == null) {
                        JsonUtil.writeJsonResponse(response, Result.error("订单不存在"));
                        return;
                    }
                    // 获取订单项
                    List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
                    order.setItems(orderItems);
                    JsonUtil.writeJsonResponse(response, Result.success(order));
                } catch (NumberFormatException e) {
                    JsonUtil.writeJsonResponse(response, Result.error("无效的订单ID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(response, Result.error("数据库错误"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) request.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 获取订单ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeJsonResponse(response, Result.error("缺少订单ID"));
                return;
            }

            Long orderId = Long.parseLong(pathInfo.substring(1));
            Order order = orderDao.findById(orderId);
            if (order == null) {
                JsonUtil.writeJsonResponse(response, Result.error("订单不存在"));
                return;
            }

            // 读取请求体数据
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            // 解析请求体数据
            String[] params = sb.toString().split("&");
            String status = null;
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "status".equals(pair[0])) {
                    status = URLDecoder.decode(pair[1], "UTF-8");
                    break;
                }
            }

            if (status == null || !isValidStatus(status)) {
                JsonUtil.writeJsonResponse(response, Result.error("无效的订单状态"));
                return;
            }

            // 更新订单状态
            orderDao.updateStatus(orderId, status);
            JsonUtil.writeJsonResponse(response, Result.success("订单状态更新成功"));
            
        } catch (NumberFormatException e) {
            JsonUtil.writeJsonResponse(response, Result.error("无效的订单ID"));
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(response, Result.error("数据库错误"));
        }
    }

    private boolean isValidStatus(String status) {
        return Arrays.asList("pending", "paid", "shipped", "completed", "cancelled").contains(status);
    }
}
