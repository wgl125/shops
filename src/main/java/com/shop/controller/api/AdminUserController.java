package com.shop.controller.api;

import com.shop.dao.UserDao;
import com.shop.model.User;
import com.shop.model.Result;
import com.shop.model.PageResult;
import com.shop.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/admin/users", "/api/admin/users/*"})
public class AdminUserController extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) request.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        try {
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
                JsonUtil.writeJson(response, Result.error("无效的分页参数"));
                return;
            }

            // 获取搜索参数
            String username = request.getParameter("username");

            // 获取用户列表
            List<User> users = userDao.findAll(page, pageSize, username);
            int total = userDao.count(username);

            PageResult<User> pageResult = new PageResult<>(users, total, page, pageSize);
            JsonUtil.writeJson(response, Result.success(pageResult));

        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJson(response, Result.error("数据库错误"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) request.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        try {
            // 获取用户ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeJson(response, Result.error("缺少用户ID"));
                return;
            }

            Long userId = Long.parseLong(pathInfo.substring(1));

            // 删除用户
            boolean success = userDao.delete(userId);
            if (success) {
                JsonUtil.writeJson(response, Result.success("用户删除成功"));
            } else {
                JsonUtil.writeJson(response, Result.error("用户删除失败"));
            }

        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, Result.error("无效的用户ID"));
        } catch (SQLException e) {
            if (e.getMessage().contains("不能删除管理员账号")) {
                JsonUtil.writeJson(response, Result.error(e.getMessage()));
            } else {
                e.printStackTrace();
                JsonUtil.writeJson(response, Result.error("数据库错误"));
            }
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
            // 获取用户ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeJson(response, Result.error("缺少用户ID"));
                return;
            }

            Long userId = Long.parseLong(pathInfo.substring(1));

            // 读取请求体数据
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            // 解析请求体数据
            String[] params = sb.toString().split("&");
            Integer status = null;
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "status".equals(pair[0])) {
                    status = Integer.parseInt(java.net.URLDecoder.decode(pair[1], "UTF-8"));
                    break;
                }
            }

            if (status == null || (status != 0 && status != 1)) {
                JsonUtil.writeJson(response, Result.error("无效的状态值"));
                return;
            }

            // 更新用户状态
            boolean success = userDao.updateStatus(userId, status);
            if (success) {
                JsonUtil.writeJson(response, Result.success("用户状态更新成功"));
            } else {
                JsonUtil.writeJson(response, Result.error("用户状态更新失败"));
            }

        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, Result.error("无效的参数"));
        } catch (SQLException e) {
            if (e.getMessage().contains("不能修改管理员账号状态")) {
                JsonUtil.writeJson(response, Result.error(e.getMessage()));
            } else {
                e.printStackTrace();
                JsonUtil.writeJson(response, Result.error("数据库错误"));
            }
        }
    }
}
