package com.shop.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseUtil {
    private static HikariDataSource dataSource;
    
    static {
        try {
            Properties props = new Properties();
            try (InputStream is = DatabaseUtil.class.getClassLoader()
                    .getResourceAsStream("config/database.properties")) {
                props.load(is);
            }
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.username"));
            config.setPassword(props.getProperty("jdbc.password"));
            config.setDriverClassName(props.getProperty("jdbc.driver"));
            
            // 连接池配置
            config.setMinimumIdle(Integer.parseInt(props.getProperty("jdbc.minIdle", "5")));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("jdbc.maxActive", "20")));
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
