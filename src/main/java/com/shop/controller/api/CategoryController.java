package com.shop.controller.api;

import com.shop.dao.CategoryDao;
import com.shop.model.Category;
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

@WebServlet("/api/categories/*")
public class CategoryController extends HttpServlet {
    private CategoryDao categoryDao = new CategoryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            // 获取所有分类
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Category> categories = categoryDao.findAll();
                JsonUtil.writeJsonResponse(response, Result.success("获取分类列表成功", categories));
                return;
            }
            
            // 获取单个分类
            try {
                Long categoryId = Long.parseLong(pathInfo.substring(1));
                Category category = categoryDao.findById(categoryId);
                if (category == null) {
                    JsonUtil.writeJsonResponse(response, Result.error("分类不存在"));
                    return;
                }
                JsonUtil.writeJsonResponse(response, Result.success("获取分类成功", category));
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("分类ID格式不正确"));
            }
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("获取分类失败: " + e.getMessage()));
        }
    }
}
