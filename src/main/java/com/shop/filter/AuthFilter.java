package com.shop.filter;

import com.shop.model.Result;
import com.shop.model.User;
import com.shop.util.JsonUtil;
import com.shop.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 设置跨域响应头
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // 对于 OPTIONS 请求直接放行
        if (httpRequest.getMethod().equals("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // 不需要验证token的路径
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 获取token
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证token
        if (token == null || token.isEmpty()) {
            JsonUtil.writeJsonResponse(httpResponse, Result.error("请先登录"));
            return;
        }
        
        try {
            // 验证token并获取用户信息
            User user = JwtUtil.verifyToken(token);
            if (user == null) {
                JsonUtil.writeJsonResponse(httpResponse, Result.error("登录已过期，请重新登录"));
                return;
            }
            
            // 将用户信息存入request
            request.setAttribute("user", user);
            
            chain.doFilter(request, response);
        } catch (Exception e) {
            JsonUtil.writeJsonResponse(httpResponse, Result.error("登录已过期，请重新登录"));
        }
    }
    
    @Override
    public void destroy() {
    }
    
    private boolean isPublicPath(String path) {
        // 静态资源
        if (path.startsWith("/images/") || 
            path.startsWith("/css/") || 
            path.startsWith("/js/")) {
            return true;
        }
        
        // 公开API
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.startsWith("/api/products") ||
               path.startsWith("/api/categories");
    }
}
