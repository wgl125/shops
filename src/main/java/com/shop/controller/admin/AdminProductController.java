package com.shop.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dao.ProductDao;
import com.shop.model.PageResult;
import com.shop.model.Product;
import com.shop.model.Result;
import com.shop.model.User;
import com.shop.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet("/api/admin/products/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 100    // 100 MB
)
public class AdminProductController extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) req.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取查询参数
                String keyword = req.getParameter("keyword");
                String categoryIdStr = req.getParameter("categoryId");
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                String orderBy = req.getParameter("orderBy");

                // 设置默认值
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                Long categoryId = categoryIdStr != null ? Long.parseLong(categoryIdStr) : null;

                // 计算偏移量
                int offset = (page - 1) * pageSize;

                // 获取商品列表和总数
                List<Product> products = productDao.findByPage(keyword, categoryId, offset, pageSize, orderBy);
                int total = productDao.count(keyword, categoryId);

                // 构建分页结果
                PageResult<Product> pageResult = new PageResult<>();
                pageResult.setList(products);
                pageResult.setTotal(total);
                pageResult.setPage(page);
                pageResult.setPageSize(pageSize);
                pageResult.setTotalPages((total + pageSize - 1) / pageSize);

                JsonUtil.writeJsonResponse(resp, Result.success("获取商品列表成功", pageResult));
            } else {
                // 获取单个商品
                try {
                    Long productId = Long.parseLong(pathInfo.substring(1));
                    Product product = productDao.findById(productId);
                    if (product == null) {
                        JsonUtil.writeJsonResponse(resp, Result.error("商品不存在"));
                        return;
                    }
                    JsonUtil.writeJsonResponse(resp, Result.success("获取商品成功", product));
                } catch (NumberFormatException e) {
                    JsonUtil.writeJsonResponse(resp, Result.error("商品ID格式不正确"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("数据库错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) req.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");

        try {
            // 获取表单数据
            String name = req.getParameter("name");
            String priceStr = req.getParameter("price");
            String stockStr = req.getParameter("stock");
            String description = req.getParameter("description");
            String categoryIdStr = req.getParameter("categoryId");
            Part imagePart = req.getPart("image");

            // 验证必填参数
            if (name == null || name.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty() ||
                stockStr == null || stockStr.trim().isEmpty() ||
                categoryIdStr == null || categoryIdStr.trim().isEmpty() ||
                imagePart == null) {
                JsonUtil.writeJsonResponse(resp, Result.error("参数不完整"));
                return;
            }

            // 处理图片上传
            String fileName = UUID.randomUUID().toString() + getFileExtension(imagePart);
            String uploadPath = getUploadPath();
            Path filePath = Paths.get(uploadPath, fileName);
            Files.createDirectories(filePath.getParent());
            imagePart.write(filePath.toString());

            // 创建商品对象
            Product product = new Product();
            product.setName(name);
            product.setPrice(new BigDecimal(priceStr));
            product.setStock(Integer.parseInt(stockStr));
            product.setDescription(description);
            product.setCategoryId(Long.parseLong(categoryIdStr));
            product.setImageUrl("/uploads/" + fileName);
            product.setStatus(1); // 1 表示上架状态

            // 保存商品
            productDao.insert(product);
            JsonUtil.writeJsonResponse(resp, Result.success("商品创建成功", product));

        } catch (NumberFormatException e) {
            JsonUtil.writeJsonResponse(resp, Result.error("参数格式错误"));
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("数据库错误"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("服务器错误"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) req.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.writeJsonResponse(resp, Result.error("未指定商品ID"));
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            Product existingProduct = productDao.findById(productId);
            if (existingProduct == null) {
                JsonUtil.writeJsonResponse(resp, Result.error("商品不存在"));
                return;
            }

            // 获取更新数据
            String name = req.getParameter("name");
            String priceStr = req.getParameter("price");
            String stockStr = req.getParameter("stock");
            String description = req.getParameter("description");
            String categoryIdStr = req.getParameter("categoryId");
            String statusStr = req.getParameter("status");
            Part imagePart = req.getPart("image");

            // 更新商品信息
            if (name != null && !name.trim().isEmpty()) {
                existingProduct.setName(name);
            }
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                existingProduct.setPrice(new BigDecimal(priceStr));
            }
            if (stockStr != null && !stockStr.trim().isEmpty()) {
                existingProduct.setStock(Integer.parseInt(stockStr));
            }
            if (description != null) {
                existingProduct.setDescription(description);
            }
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                existingProduct.setCategoryId(Long.parseLong(categoryIdStr));
            }
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                existingProduct.setStatus(Integer.parseInt(statusStr));
            }

            // 处理图片更新
            if (imagePart != null && imagePart.getSize() > 0) {
                String fileName = UUID.randomUUID().toString() + getFileExtension(imagePart);
                String uploadPath = getUploadPath();
                Path filePath = Paths.get(uploadPath, fileName);
                Files.createDirectories(filePath.getParent());
                imagePart.write(filePath.toString());
                existingProduct.setImageUrl("/uploads/" + fileName);
            }

            // 保存更新
            productDao.update(existingProduct);
            JsonUtil.writeJsonResponse(resp, Result.success("商品更新成功", existingProduct));

        } catch (NumberFormatException e) {
            JsonUtil.writeJsonResponse(resp, Result.error("参数格式错误"));
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("数据库错误"));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("服务器错误"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // 检查管理员权限
        User user = (User) req.getAttribute("user");
        if (!"admin".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.writeJsonResponse(resp, Result.error("未指定商品ID"));
            return;
        }

        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            Product product = productDao.findById(productId);
            if (product == null) {
                JsonUtil.writeJsonResponse(resp, Result.error("商品不存在"));
                return;
            }

            // 软删除：更新商品状态为下架
            product.setStatus(0); // 0 表示下架状态
            productDao.update(product);
            JsonUtil.writeJsonResponse(resp, Result.success("商品删除成功"));

        } catch (NumberFormatException e) {
            JsonUtil.writeJsonResponse(resp, Result.error("商品ID格式不正确"));
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.writeJsonResponse(resp, Result.error("数据库错误"));
        }
    }

    private String getUploadPath() {
        return getServletContext().getRealPath("") + UPLOAD_DIR;
    }

    private String getFileExtension(Part part) {
        String submittedFileName = part.getSubmittedFileName();
        return submittedFileName.substring(submittedFileName.lastIndexOf("."));
    }
}
