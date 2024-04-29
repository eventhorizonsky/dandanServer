package xyz.ezsky.config;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.sqlite.SQLiteDataSource;
import xyz.ezsky.dao.ScanPathMapper;
import xyz.ezsky.entity.dto.AppConfigDTO;
import xyz.ezsky.service.ConfigService;
import xyz.ezsky.tasks.VideoScanner;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 应用配置
 *
 * @author eventhorizonsky
 * @date 2024/04/28
 */
@Configuration
@Slf4j
@MapperScan("xyz.ezsky.dao")
public class DatabaseConfig {


    /**
     * DB 文件路径
     */
    @Value("${database.file.path}")
    private String dbFilePath;

    /**
     * 创建表 SQL 文件
     */
    @Value("${database.create.table.sql.file}")
    private String createTableSqlFile;
    @Autowired
    private VideoScanner videoScanner;
    @Autowired
    private ConfigService configService;
    @Autowired
    private ScanPathMapper scanPathMapper;
    @Autowired
    private AppConfigDTO appConfigDTO;


    private final ResourceLoader resourceLoader;

    public DatabaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    /**
     * 数据源
     *
     * @return {@link SQLiteDataSource}
     */
    @Bean
    public SQLiteDataSource dataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFilePath);
        return dataSource;
    }

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        // 检查数据库文件是否存在
        File dbFile = new File(dbFilePath);
        if (dbFile.exists()) {
            List<String> paths=scanPathMapper.selectAllScanPath();
            for(String path:paths){
                configService.addPathScan(path);
            }
            videoScanner.startScanVideos("44 * * * * ?");
            // 如果数据库文件已经存在，则不执行创建表的操作
            return;
        }
        try (Connection connection = dataSource().getConnection();
             Statement statement = connection.createStatement()) {
            // 读取 SQL 文件中的建表语句
            String createTableQuery = loadSqlFromFile(createTableSqlFile);
            statement.executeUpdate(createTableQuery);
            appConfigDTO.setFirstTime(true);
            videoScanner.startScanVideos("30 * * * * ?");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create table.", e);
        }
    }


    /**
     * 从文件加载 SQL
     *
     * @param sqlFilePath SQL 文件路径
     * @return {@link String}
     * @throws IOException io异常
     */
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


