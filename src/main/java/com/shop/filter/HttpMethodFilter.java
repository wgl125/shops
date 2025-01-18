package com.shop.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@WebFilter("/*")
public class HttpMethodFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getParameter("_method");
        
        if (method != null) {
            HttpMethodRequestWrapper wrapper = new HttpMethodRequestWrapper(httpRequest, method.toUpperCase());
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public void destroy() {
    }
    
    private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {
        private final String method;
        
        public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
            super(request);
            this.method = method;
        }
        
        @Override
        public String getMethod() {
            return method;
        }
    }
}
