package xyz.ezsky.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.utils.FileTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileMonitor {
    private List<FileAlterationObserver> observers;
    private static final long SCAN_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1); // 1 minute

    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private SubtitleMapper subtitleMapper;

    public FileMonitor() {
        this.observers = new ArrayList<>();
    }


    /**
     * 首次添加受监控文件夹
     *
     * @param folderPath  文件夹路径
     * @param videoVoList 视频列表
     * @param subtitles   字幕
     */
    public void addMonitoredFolderFirstTime(String folderPath, List<VideoVo> videoVoList, List<Subtitle> subtitles) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            log.error("Folder '{}' does not exist or is not a directory", folderPath);
            return;
        }
        // 创建递归观察者，监控指定文件夹及其所有子文件夹
        FileAlterationObserver observer = new FileAlterationObserver(folder);
        observer.addListener(new FileListener(videoMapper, subtitleMapper));
        observers.add(observer);
        List<VideoVo> videoVos = scanDirectory(folderPath, subtitles);
        if (videoVos != null && !videoVos.isEmpty()) {
            videoVoList.addAll(videoVos);
        }
        File[] subFolders = folder.listFiles(File::isDirectory);
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                addMonitoredFolderFirstTime(subFolder.getPath(), videoVoList, subtitles); // 递归添加子文件夹
            }
        }
    }

    /**
     * 扫描目录
     *
     * @param directoryPath 目录路径
     * @param subtitles     字幕
     * @return {@link List}<{@link VideoVo}>
     */
    private List<VideoVo> scanDirectory(String directoryPath, List<Subtitle> subtitles) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.info("目录不存在或不是一个有效的目录：" + directoryPath);
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("目录为空：" + directoryPath);
            return null;
        }
        List<VideoVo> videoVoList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && FileTool.isVideoFile(file.getName())) {
                VideoVo videoVo = FileTool.extractVideoInfo(file.getAbsolutePath());
                if (!Objects.isNull(videoVo)) {
                    videoVoList.add(videoVo);
                }
            } else if (file.isFile() && FileTool.isSubtitile(file.getName())) {
                Subtitle subtitle = new Subtitle();
                subtitle.setPath(file.getAbsolutePath());
                subtitle.setSubtitleName("外挂字幕");
                subtitles.add(subtitle);
            }
        }
        return videoVoList;
    }


    /**
     * 删除受监视文件夹
     *
     * @param folderPath 文件夹路径
     */
    public void removeMonitoredFolder(String folderPath) {
        File folder = new File(folderPath);
        FileAlterationObserver observerToRemove = null;
        for (FileAlterationObserver observer : observers) {
            if (observer.getDirectory().equals(folder)) {
                observerToRemove = observer;
                break;
            }
        }
        if (observerToRemove != null) {
            observers.remove(observerToRemove);
        } else {
            log.warn("文件夹 '{}' 目前未被监听中", folderPath);
        }
    }


    /**
     * 获取受监视文件夹
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> getMonitoredFolders() {
        List<String> folderPaths = new ArrayList<>();
        for (FileAlterationObserver observer : observers) {
            folderPaths.add(observer.getDirectory().getPath());
        }
        return folderPaths;
    }

    /**
     * 开始监控
     */
    public void startMonitoring() {
//        FileAlterationMonitor monitor = new FileAlterationMonitor(SCAN_INTERVAL_MILLIS);
        FileAlterationMonitor monitor = new FileAlterationMonitor();
        for (FileAlterationObserver observer : observers) {
            monitor.addObserver(observer);
        }
        try {
            monitor.start();
        } catch (Exception e) {
            log.error("创建监视器失败", e);
        }
    }
}
