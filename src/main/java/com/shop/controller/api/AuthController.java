package com.shop.controller.api;

import com.shop.dao.UserDao;
import com.shop.model.User;
import com.shop.model.Result;
import com.shop.util.JsonUtil;
import com.shop.util.JwtUtil;
import com.shop.util.MD5Util;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet {
    private UserDao userDao = new UserDao();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if ("/register".equals(pathInfo)) {
            handleRegister(request, response);
        } else if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else {
            JsonUtil.writeJsonResponse(response, Result.error("无效的请求路径"));
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // 获取注册参数
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            
            System.out.println("Register attempt - username: " + username);
            System.out.println("Register attempt - password MD5: " + MD5Util.md5(password));
            
            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名不能为空"));
                return;
            }
            if (username.length() < 3 || username.length() > 20) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名长度必须在3-20个字符之间"));
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                JsonUtil.writeJsonResponse(response, Result.error("密码不能为空"));
                return;
            }
            if (password.length() < 6 || password.length() > 20) {
                JsonUtil.writeJsonResponse(response, Result.error("密码长度必须在6-20个字符之间"));
                return;
            }
            if (!password.equals(confirmPassword)) {
                JsonUtil.writeJsonResponse(response, Result.error("两次输入的密码不一致"));
                return;
            }
            
            // 检查用户名是否已存在
            if (userDao.findByUsername(username) != null) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名已存在"));
                return;
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole("user");
            user.setStatus(1);
            
            if (!userDao.save(user)) {
                JsonUtil.writeJsonResponse(response, Result.error("注册失败"));
                return;
            }
            
            // 生成token
            String token = JwtUtil.generateToken(user);
            
            // 返回用户信息和token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            user.setPassword(null); // 不返回密码
            data.put("user", user);
            
            JsonUtil.writeJsonResponse(response, Result.success("注册成功", data));
        } catch (Exception e) {
            JsonUtil.writeJsonResponse(response, Result.error("注册失败: " + e.getMessage()));
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // 获取登录参数
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            System.out.println("Login attempt - username: " + username);
            
            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名不能为空"));
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                JsonUtil.writeJsonResponse(response, Result.error("密码不能为空"));
                return;
            }
            
            // 查找用户
            User user = userDao.findByUsername(username);
            System.out.println("User found: " + (user != null));
            if (user != null) {
                System.out.println("User password: " + user.getPassword());
                System.out.println("Input password MD5: " + MD5Util.md5(password));
            }
            
            if (user == null) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名或密码错误"));
                return;
            }
            
            // 验证密码
            if (!user.getPassword().equals(MD5Util.md5(password))) {
                JsonUtil.writeJsonResponse(response, Result.error("用户名或密码错误"));
                return;
            }
            
            // 检查用户状态
            if (user.getStatus() != 1) {
                JsonUtil.writeJsonResponse(response, Result.error("账号已被禁用"));
                return;
            }
            
            // 生成token
            String token = JwtUtil.generateToken(user);
            
            // 返回用户信息和token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            user.setPassword(null); // 不返回密码
            data.put("user", user);
            
            JsonUtil.writeJsonResponse(response, Result.success("登录成功", data));
        } catch (Exception e) {
            JsonUtil.writeJsonResponse(response, Result.error("登录失败: " + e.getMessage()));
        }
    }
}
