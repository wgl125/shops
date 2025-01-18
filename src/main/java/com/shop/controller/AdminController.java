package com.shop.controller;

import com.shop.dao.UserDao;
import com.shop.model.Result;
import com.shop.model.User;
import com.shop.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/admin/create")
@MultipartConfig
public class AdminController extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 获取表单数据
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 验证参数
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            JsonUtil.writeJson(response, Result.error(400, "用户名和密码不能为空"));
            return;
        }

        // 检查用户名是否已存在
        if (userDao.findByUsername(username) != null) {
            JsonUtil.writeJson(response, Result.error(400, "用户名已存在"));
            return;
        }

        // 创建管理员用户
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setRole("admin");
        admin.setStatus(1);

        // 保存到数据库
        userDao.save(admin);

        JsonUtil.writeJson(response, Result.success("管理员创建成功"));
    }
}
