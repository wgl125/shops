package com.shop.controller.api;

import com.shop.dao.ProductDao;
import com.shop.model.Product;
import com.shop.model.Result;
import com.shop.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/products/*")
public class ProductController extends HttpServlet {
    private ProductDao productDao = new ProductDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            // 获取单个商品详情
            if (pathInfo != null && !pathInfo.equals("/")) {
                try {
                    Long productId = Long.parseLong(pathInfo.substring(1));
                    Product product = productDao.findById(productId);
                    if (product == null) {
                        JsonUtil.writeJsonResponse(response, Result.error("商品不存在"));
                        return;
                    }
                    if (product.getStatus() != 1) {
                        JsonUtil.writeJsonResponse(response, Result.error("商品已下架"));
                        return;
                    }
                    JsonUtil.writeJsonResponse(response, Result.success("获取商品成功", product));
                    return;
                } catch (NumberFormatException e) {
                    JsonUtil.writeJsonResponse(response, Result.error("商品ID格式不正确"));
                    return;
                }
            }
            
            // 获取商品列表
            String pageStr = request.getParameter("page");
            String pageSizeStr = request.getParameter("pageSize");
            String keyword = request.getParameter("keyword");
            String categoryIdStr = request.getParameter("categoryId");
            String orderBy = request.getParameter("orderBy"); // price_asc, price_desc, create_time_desc
            
            int page = 1;
            int pageSize = 10;
            Long categoryId = null;
            
            try {
                if (pageStr != null) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) {
                        page = 1;
                    }
                }
                if (pageSizeStr != null) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize < 1) {
                        pageSize = 10;
                    }
                    if (pageSize > 100) {
                        pageSize = 100;
                    }
                }
                if (categoryIdStr != null) {
                    categoryId = Long.parseLong(categoryIdStr);
                }
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("参数格式不正确"));
                return;
            }
            
            // 计算总数
            int total = productDao.count(keyword, categoryId);
            int totalPages = (total + pageSize - 1) / pageSize;
            if (page > totalPages && totalPages > 0) {
                page = totalPages;
            }
            
            // 获取商品列表
            List<Product> products = productDao.findByPage(keyword, categoryId, (page - 1) * pageSize, pageSize, orderBy);
            
            // 过滤掉已下架商品
            products.removeIf(product -> product.getStatus() != 1);
            
            // 构造分页数据
            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", totalPages);
            data.put("list", products);
            
            JsonUtil.writeJsonResponse(response, Result.success("获取商品列表成功", data));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("获取商品列表失败: " + e.getMessage()));
        }
    }
}
