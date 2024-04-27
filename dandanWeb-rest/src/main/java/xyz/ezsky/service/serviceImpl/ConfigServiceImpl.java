package xyz.ezsky.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.ezsky.dao.ScanPathMapper;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.ConfigService;
import xyz.ezsky.tasks.FileMonitor;
import xyz.ezsky.utils.FileTool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private FileMonitor fileMonitor;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private ScanPathMapper scanPathMapper;

    @Autowired
    private SubtitleMapper subtitleMapper;

    @Override
    public List<VideoVo> addPath(String path) {
        if (scanPathMapper.selectScanPathBypath(path).isEmpty()) {
            scanPathMapper.insertScanPath(path);
            return addPathScan(path);
        } else {
            return null;
        }
    }

    @Override
    @Async
    public List<VideoVo> addPathScan(String path) {
        List<VideoVo> folderFiles = new ArrayList<>();
        List<Subtitle> subtitles = new ArrayList<>();
        fileMonitor.addMonitoredFolderFirstTime(path, folderFiles, subtitles);
        for (String s : fileMonitor.getMonitoredFolders()) {
            System.out.println(s);
        }
        fileMonitor.startMonitoring();
        List<String> dbFiles = videoMapper.selectAllVideos().stream()
                .map(VideoVo::getFilePath) // 提取每个 VideoVo 对象的 filePath 字段
                .collect(Collectors.toList());
        for (VideoVo folderFile : folderFiles) {
            if (!dbFiles.contains(folderFile.getFilePath())) {
                folderFile.setMatched("0");
                videoMapper.insertVideo(folderFile);
                subtitles.addAll(FileTool.scanMkv(folderFile));
            } else {
                log.info(folderFile.getFilePath() + "已存在于数据库");
            }
        }
        List<String> dbSbFiles = subtitleMapper.selectAllSubtitle().stream()
                .map(Subtitle::getPath) // 提取每个 VideoVo 对象的 filePath 字段
                .collect(Collectors.toList());
        if (!subtitles.isEmpty()) {
            for (Subtitle subtitle : subtitles) {
                if (!dbSbFiles.contains(subtitle.getPath())) {
                    subtitleMapper.insertSubtitle(subtitle);
                } else {
                    log.info(subtitle.getPath() + "已存在于数据库");
                }
            }
        }
        return folderFiles;
    }
}
