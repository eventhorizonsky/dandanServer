package xyz.ezsky.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import xyz.ezsky.dao.ScanPathMapper;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.ConfigService;
import xyz.ezsky.tasks.FileMonitor;
import xyz.ezsky.tasks.VideoScanner;
import xyz.ezsky.utils.FileTool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableAsync
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private FileMonitor fileMonitor;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private ScanPathMapper scanPathMapper;

    @Autowired
    private SubtitleMapper subtitleMapper;

    @Autowired
    private VideoScanner videoScanner;

    @Override
    public boolean isAddPath(String path) {
        if (scanPathMapper.selectScanPathBypath(path).isEmpty()) {
            scanPathMapper.insertScanPath(path);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Async
    public void addPathScan(String path) {
        videoScanner.stopScanVideos();
        List<VideoVo> folderFiles = new ArrayList<>();
        List<Subtitle> subtitles = new ArrayList<>();
        fileMonitor.addMonitoredFolderFirstTime(path, folderFiles, subtitles);
        for (String s : fileMonitor.getMonitoredFolders()) {
            System.out.println(s);
        }
        fileMonitor.startMonitoring();

        List<String> dbFiles = videoMapper.selectAllVideos().stream()
                .map(VideoVo::getFilePath)
                .collect(Collectors.toList());

        List<VideoVo> newVideos = folderFiles.stream()
                .filter(folderFile -> {
                    if (dbFiles.contains(folderFile.getFilePath())) {
                        log.info(folderFile.getFilePath() + "已存在于数据库");
                        return false;
                    } else {
                        folderFile.setMatched("0");
                        return true;
                    }
                })
                .collect(Collectors.toList());

        if (!newVideos.isEmpty()) {
            videoMapper.batchInsertVideos(newVideos);
        }

        List<String> dbSbFiles = subtitleMapper.selectAllSubtitle().stream()
                .map(Subtitle::getPath)
                .collect(Collectors.toList());

        List<Subtitle> newSubtitles = subtitles.stream()
                .filter(subtitle -> {
                    if (dbSbFiles.contains(subtitle.getPath())) {
                        log.info(subtitle.getPath() + "已存在于数据库");
                        return false;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        if (!newSubtitles.isEmpty()) {
            subtitleMapper.batchInsertSubtitles(newSubtitles);
        }

        videoScanner.startScanVideos("30 * * * * ?");
    }
}

