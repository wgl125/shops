package com.shop.controller;

import com.shop.dao.UserDao;
import com.shop.model.Result;
import com.shop.model.User;
import com.shop.util.JsonUtil;
import com.shop.util.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/login")
@MultipartConfig
public class AuthController extends HttpServlet {
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

        // 验证用户名和密码
        User user = userDao.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            JsonUtil.writeJson(response, Result.error(401, "用户名或密码错误"));
            return;
        }

        if (user.getStatus() == 0) {
            JsonUtil.writeJson(response, Result.error(403, "账号已被禁用"));
            return;
        }

        // 生成token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 返回token和用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        JsonUtil.writeJson(response, Result.success(data));
    }
}
