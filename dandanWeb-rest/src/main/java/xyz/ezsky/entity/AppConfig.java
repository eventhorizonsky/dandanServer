package xyz.ezsky.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class AppConfig {
    private boolean firstTime;
    private String appName;
    private List<String> scanPath;
    private String targetPath;
    private String cron;
}
