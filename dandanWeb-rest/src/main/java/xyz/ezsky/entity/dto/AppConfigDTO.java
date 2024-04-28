package xyz.ezsky.entity.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class AppConfigDTO {
    private boolean firstTime;
    private String appName;
    private List<String> scanPath;
    private String targetPath;
    private String cron;
}
