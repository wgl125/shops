package com.shop.controller.admin;

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

@WebServlet("/api/admin/categories/*")
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
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 验证参数
            String name = request.getParameter("name");
            if (name == null || name.trim().isEmpty()) {
                JsonUtil.writeJsonResponse(response, Result.error("分类名称不能为空"));
                return;
            }
            if (name.trim().length() > 50) {
                JsonUtil.writeJsonResponse(response, Result.error("分类名称不能超过50个字符"));
                return;
            }
            
            String description = request.getParameter("description");
            if (description != null && description.length() > 200) {
                JsonUtil.writeJsonResponse(response, Result.error("分类描述不能超过200个字符"));
                return;
            }
            
            // 创建分类
            Category category = new Category();
            category.setName(name.trim());
            category.setDescription(description);
            
            categoryDao.insert(category);
            
            JsonUtil.writeJsonResponse(response, Result.success("创建分类成功", category));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("创建分类失败: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 获取分类ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeJsonResponse(response, Result.error("分类ID不能为空"));
                return;
            }
            
            Long categoryId;
            try {
                categoryId = Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("分类ID格式不正确"));
                return;
            }
            
            // 检查分类是否存在
            Category existingCategory = categoryDao.findById(categoryId);
            if (existingCategory == null) {
                JsonUtil.writeJsonResponse(response, Result.error("分类不存在"));
                return;
            }
            
            // 验证参数
            String name = request.getParameter("name");
            if (name != null) {
                if (name.trim().isEmpty()) {
                    JsonUtil.writeJsonResponse(response, Result.error("分类名称不能为空"));
                    return;
                }
                if (name.trim().length() > 50) {
                    JsonUtil.writeJsonResponse(response, Result.error("分类名称不能超过50个字符"));
                    return;
                }
                existingCategory.setName(name.trim());
            }
            
            String description = request.getParameter("description");
            if (description != null) {
                if (description.length() > 200) {
                    JsonUtil.writeJsonResponse(response, Result.error("分类描述不能超过200个字符"));
                    return;
                }
                existingCategory.setDescription(description);
            }
            
            // 更新分类
            categoryDao.update(existingCategory);
            
            JsonUtil.writeJsonResponse(response, Result.success("更新分类成功", existingCategory));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("更新分类失败: " + e.getMessage()));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 获取分类ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeJsonResponse(response, Result.error("分类ID不能为空"));
                return;
            }
            
            Long categoryId;
            try {
                categoryId = Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                JsonUtil.writeJsonResponse(response, Result.error("分类ID格式不正确"));
                return;
            }
            
            // 检查分类是否存在
            Category category = categoryDao.findById(categoryId);
            if (category == null) {
                JsonUtil.writeJsonResponse(response, Result.error("分类不存在"));
                return;
            }
            
            // 删除分类
            categoryDao.delete(categoryId);
            
            JsonUtil.writeJsonResponse(response, Result.success("删除分类成功"));
        } catch (SQLException e) {
            JsonUtil.writeJsonResponse(response, Result.error("删除分类失败: " + e.getMessage()));
        }
    }
}
