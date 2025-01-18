package com.shop.filter;

import com.shop.util.JsonUtil;
import com.shop.util.JwtUtil;
import com.shop.model.Result;
import io.jsonwebtoken.Claims;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthFilter implements Filter {
    private List<String> excludeUrls;

    @Override
    public void init(FilterConfig filterConfig) {
        String excludeUrlStr = filterConfig.getInitParameter("excludeUrls");
        if (excludeUrlStr != null) {
            excludeUrls = Arrays.asList(excludeUrlStr.split(","));
        }
    }

    private boolean isExcludeUrl(String path) {
        if (excludeUrls == null) return false;
        for (String pattern : excludeUrls) {
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (path.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // 检查是否是排除的URL
        if (isExcludeUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取token
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            JsonUtil.writeJson(httpResponse, Result.error(401, "未登录"));
            return;
        }

        token = token.substring(7);

        try {
            // 验证token
            if (!JwtUtil.validateToken(token)) {
                JsonUtil.writeJson(httpResponse, Result.error(401, "token无效"));
                return;
            }

            // 解析token
            Claims claims = JwtUtil.parseToken(token);
            
            // 将用户信息存入request
            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));

            chain.doFilter(request, response);
        } catch (Exception e) {
            JsonUtil.writeJson(httpResponse, Result.error(401, "token验证失败"));
        }
    }

    @Override
    public void destroy() {
    }
}
