package xyz.ezsky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import xyz.ezsky.dao.ScanPathMapper;
import xyz.ezsky.service.ConfigService;
import xyz.ezsky.tasks.VideoScanner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

/**
 * 应用配置
 *
 * @author eventhorizonsky
 * @date 2024/04/28
 */
@Configuration
@Slf4j
public class FolderConfig {
    @Value("${database.file.path}")
    private String dbFilePath;
    @Autowired
    private VideoScanner videoScanner;
    @Autowired
    private ConfigService configService;
    @Autowired
    private ScanPathMapper scanPathMapper;

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
        }
    }
}


