package xyz.ezsky.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.utils.FileTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件侦听器
 *
 * @author eventhorizonsky
 * @date 2024/04/28
 *///FileListener类
@Slf4j
public class FileListener extends FileAlterationListenerAdaptor {


    /**
     * 视频映射器
     */
    private final VideoMapper videoMapper;

    /**
     * 字幕映射器
     */
    private final SubtitleMapper subtitleMapper;

    /**
     * 文件侦听器
     *
     * @param videoMapper    视频映射器
     * @param subtitleMapper 字幕映射器
     */
    @Autowired
    public FileListener(VideoMapper videoMapper,SubtitleMapper subtitleMapper) {
        this.subtitleMapper=subtitleMapper;
        this.videoMapper = videoMapper;
    }

    /**
     * 监听到文件删除
     *
     * @param file 文件
     */
    public void onFileDelete(File file) {
        log.info("[删除]:" + file.getAbsolutePath());
        videoMapper.deleteVideoByFilePath(file.getAbsolutePath());
    }

    /**
     * 监听到文件创建
     *
     * @param file 文件
     */
    public void onFileCreate(File file) {
        if (file.isFile() && FileTool.isVideoFile(file.getName())) {
            VideoVo videoVo=FileTool.extractVideoInfo(file.getAbsolutePath());
            if (videoVo != null) {
                List<VideoVo> videoVos=videoMapper.selectVideoByFilePath(videoVo.getFilePath());
                if(videoVos.isEmpty()){
                    videoVo.setMatched("0");
                    videoMapper.insertVideo(videoVo);

                }else{
                    log.info("已经录入过啦");
                }

            }
        } else if (file.isFile()&&FileTool.isSubtitile(file.getName())) {
            Subtitle subtitle=new Subtitle();
            List<VideoVo> videoVos= videoMapper.selectVideoBySubtitle(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            if(!videoVos.isEmpty()){
                subtitle.setVideoId(videoVos.get(0).getId());
            }
            subtitle.setPath(file.getAbsolutePath());
            subtitle.setSubtitleName("外挂字幕");
            List<Subtitle> subtitles=subtitleMapper.selectSubtitleByPath(subtitle.getPath());
            if(subtitles.isEmpty()){
                subtitleMapper.insertSubtitle(subtitle);
            }
        }
    }


    /**
     * 启动时
     *
     * @param observer 观察者
     */
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
    }

    /**
     * 停止时
     *
     * @param observer 观察者
     */
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }
}
