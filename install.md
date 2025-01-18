# 完整部署指南 / Complete Deployment Guide

## 目录 / Contents
1. [环境准备 Prerequisites](#环境准备-prerequisites)
2. [开发环境搭建 Development Environment Setup](#开发环境搭建-development-environment-setup)
3. [数据库配置 Database Configuration](#数据库配置-database-configuration)
4. [项目部署 Project Deployment](#项目部署-project-deployment)
5. [常见问题 FAQ](#常见问题-faq)

## 环境准备 Prerequisites

### 必需软件 Required Software
1. **JDK 17**
   - 下载链接：[Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
   - 安装步骤：
     1. 下载对应操作系统的安装包
     2. 运行安装程序，按提示完成安装
     3. 验证安装：打开终端，输入 `java -version`

2. **Maven 3.8+**
   - 下载链接：[Apache Maven](https://maven.apache.org/download.cgi)
   - 安装步骤：
     1. 下载二进制包（apache-maven-3.8.x-bin.zip）
     2. 解压到指定目录（如：/opt/maven）
     3. 设置环境变量：
        ```bash
        # Windows: 添加到系统环境变量 Path
        C:\path\to\maven\bin
        
        # Mac/Linux: 编辑 ~/.bash_profile 或 ~/.zshrc
        export M2_HOME=/path/to/maven
        export PATH=$M2_HOME/bin:$PATH
        ```
     4. 验证安装：`mvn -version`

3. **MySQL 8.0**
   - 下载链接：[MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
   - 安装步骤：
     1. 下载并安装 MySQL
     2. 记住设置的 root 密码
     3. 验证安装：`mysql --version`

4. **Tomcat 10**
   - 下载链接：[Apache Tomcat](https://tomcat.apache.org/download-10.cgi)
   - 安装步骤：
     1. 下载 Core 版本的 zip/tar.gz 文件
     2. 解压到指定目录（如：/opt/tomcat）
     3. 设置权限（Linux/Mac）：
        ```bash
        chmod +x /opt/tomcat/bin/*.sh
        ```

### 开发工具 Recommended IDE
- **IntelliJ IDEA**
  - 下载链接：[IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
  - Community（免费）版本足够使用
  - 建议安装插件：
    - Maven Helper
    - Database Navigator
    - Git Integration

## 开发环境搭建 Development Environment Setup

### 1. 获取代码 Get the Code
```bash
# 克隆项目
git clone https://github.com/wgl125/shops.git
cd shops

# 或者下载 ZIP 包并解压
```

### 2. 配置 IDE Configure IDE
1. 打开 IntelliJ IDEA
2. 选择 "Open Project"
3. 选择项目目录
4. 等待 Maven 下载依赖
5. 设置项目 SDK 为 JDK 17

### 3. 配置 Maven Configure Maven
1. 打开 `pom.xml`
2. 右键选择 "Maven -> Reload Project"
3. 等待依赖下载完成

## 数据库配置 Database Configuration

### 1. 创建数据库 Create Database
1. 登录 MySQL：
   ```bash
   mysql -u root -p
   ```

2. 创建数据库：
   ```sql
   CREATE DATABASE student_shops CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. 创建用户并授权：
   ```sql
   CREATE USER 'shopuser'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON student_shops.* TO 'shopuser'@'localhost';
   FLUSH PRIVILEGES;
   ```

### 2. 导入数据 Import Data
1. 找到项目中的 SQL 文件：`sql.sql`
2. 导入数据：
   ```bash
   mysql -u shopuser -p student_shops < sql.sql
   ```

### 3. 配置数据库连接 Configure Database Connection
1. 编辑 `src/main/resources/config/database.properties`：
   ```properties
   jdbc.driver=com.mysql.cj.jdbc.Driver
   jdbc.url=jdbc:mysql://localhost:3306/student_shops?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   jdbc.username=shopuser
   jdbc.password=your_password
   ```

## 项目部署 Project Deployment

### 1. 构建项目 Build Project
```bash
mvn clean package
```

### 2. 部署到 Tomcat Deploy to Tomcat
1. 复制 WAR 文件：
   ```bash
   cp target/ROOT.war /path/to/tomcat/webapps/
   ```

2. 启动 Tomcat：
   ```bash
   # Windows
   /path/to/tomcat/bin/startup.bat

   # Mac/Linux
   /path/to/tomcat/bin/startup.sh
   ```

3. 检查日志：
   ```bash
   tail -f /path/to/tomcat/logs/catalina.out
   ```

### 3. 验证部署 Verify Deployment
1. 访问：`http://localhost:8080`
2. 测试 API：
   ```bash
   # 测试用户登录
   curl -X POST http://localhost:8080/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{"username":"admin","password":"admin123"}'
   ```

## 常见问题 FAQ

### 1. 端口占用 Port in Use
```bash
# 查找占用端口的进程
netstat -ano | findstr 8080    # Windows
lsof -i :8080                  # Mac/Linux

# 修改 Tomcat 端口
# 编辑 conf/server.xml
<Connector port="8081" .../>
```

### 2. 数据库连接失败 Database Connection Failed
1. 检查数据库服务是否运行
2. 验证数据库用户名密码
3. 检查数据库配置文件

### 3. 文件上传失败 File Upload Failed
1. 检查上传目录权限
2. 确保目录存在：
   ```bash
   mkdir -p src/main/webapp/static/images/products
   mkdir -p src/main/webapp/static/images/upload
   chmod 755 src/main/webapp/static/images/products
   chmod 755 src/main/webapp/static/images/upload
   ```

### 4. 内存不足 Out of Memory
修改 Tomcat 的 JVM 参数：
```bash
# 编辑 bin/catalina.sh 或 catalina.bat
JAVA_OPTS="-Xms512m -Xmx1024m -XX:MaxPermSize=256m"
```

## 开发建议 Development Tips
1. 使用 Git 进行版本控制
2. 定期备份数据库
3. 遵循代码规范
4. 编写单元测试
5. 记录重要的配置修改

## 安全建议 Security Tips
1. 修改默认密码
2. 使用 HTTPS
3. 定期更新依赖
4. 配置防火墙
5. 启用日志审计
