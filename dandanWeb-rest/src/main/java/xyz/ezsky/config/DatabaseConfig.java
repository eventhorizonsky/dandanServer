package xyz.ezsky.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.sqlite.SQLiteDataSource;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${database.file.path}")
    private String dbFilePath;

    @Value("${database.create.table.sql.file}")
    private String createTableSqlFile;

    public DatabaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public SQLiteDataSource dataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFilePath);
        return dataSource;
    }

    @PostConstruct
    private void createTable() {
        try (Connection connection = dataSource().getConnection();
             Statement statement = connection.createStatement()) {
            // 读取 SQL 文件中的建表语句
            String createTableQuery = loadSqlFromFile(createTableSqlFile);
            statement.executeUpdate(createTableQuery);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create table.", e);
        }
    }

    private String loadSqlFromFile(String sqlFilePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + sqlFilePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
    }
}


