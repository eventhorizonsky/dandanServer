package xyz.ezsky.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.File;

@Configuration
public class FolderConfig {
    @EventListener(ApplicationReadyEvent.class)
    public void createDirectories() {
        File tempDir = new File("app/media/temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File targetDir = new File("app/media/target");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File sourceDir = new File("app/media/source");
        if (!sourceDir.exists()) {
            sourceDir.mkdirs();
        }
        File failedDir = new File("app/media/source/failed");
        if (!failedDir.exists()) {
            failedDir.mkdirs();
        }
    }
}
