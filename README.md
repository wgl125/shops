# 电商网站项目

## 项目说明
使用Java原生技术栈开发的轻量购物系统，项目采用纯后端API架构，不依赖Spring等重量级框架，提供完整的用户管理、商品管理、购物车和订单处理功能。

## 技术栈
- 后端：Java Servlet + JDBC
- 数据库：MySQL
- Java开发环境：JDK 17 Maven 3.8+ (项目构建工具)
- Web容器：Jakarta EE 6.0 (Servlet 6.0) Tomcat 10+

## 配置
数据库配置位置：`src/main/resources/config/database.properties`

## 项目结构
```
student_shops/
├── src/                    # 源代码目录
│   ├── main/              # 主要代码
│   │   ├── java/         # Java源代码
│   │   │   └── com/shop/
│   │   │       ├── controller/    # API接口控制器
│   │   │       ├── service/      # 业务逻辑层
│   │   │       ├── dao/         # 数据访问层
│   │   │       ├── model/       # 数据模型
│   │   │       ├── util/        # 工具类
│   │   │       └── filter/      # 过滤器
│   │   ├── resources/   # 资源文件目录
│   │   │   ├── config/  # 配置文件
│   │   │   └── sql/     # SQL脚本
│   │   └── webapp/     # Web应用目录
│   │       ├── WEB-INF/
│   │       │   └── web.xml  # Web配置文件
│   │       └── static/   # 静态资源
│   │           ├── images/  # 图片文件
│   │           │   ├── products/  # 商品图片
│   │           │   └── upload/    # 上传文件临时目录
│   │           ├── js/      # JavaScript文件
│   │           └── css/     # CSS样式文件
│   └── test/              # 测试代码
├── pom.xml               # Maven配置文件
└── README.md            # 项目说明文档
```

## 文件上传配置
- 商品图片上传路径：`/static/images/products/`
- 临时文件上传路径：`/static/images/upload/`
- 支持的图片格式：jpg、jpeg、png
- 单个文件最大大小：5MB
- 图片访问URL格式：`http://域名/images/products/图片名称`

