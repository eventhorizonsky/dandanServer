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

//FileListener类
@Slf4j
public class FileListener extends FileAlterationListenerAdaptor {


    private final VideoMapper videoMapper;

    private final SubtitleMapper subtitleMapper;

    @Autowired
    public FileListener(VideoMapper videoMapper,SubtitleMapper subtitleMapper) {
        this.subtitleMapper=subtitleMapper;
        this.videoMapper = videoMapper;
    }
    public void onFileDelete(File file) {
        log.info("[删除]:" + file.getAbsolutePath());
        videoMapper.deleteVideoByFilePath(file.getAbsolutePath());
    }
    public void onFileCreate(File file) {
        if (file.isFile() && FileTool.isVideoFile(file.getName())) {
            VideoVo videoVo=FileTool.extractVideoInfo(file.getAbsolutePath());
            List<Subtitle> subtitles=new ArrayList<>();
            if(!Objects.isNull(videoVo)){
              subtitles.addAll(FileTool.scanMkv(videoVo));
                List<String> dbFiles = subtitleMapper.selectAllSubtitle().stream()
                        .map(Subtitle::getPath) // 提取每个 VideoVo 对象的 filePath 字段
                        .collect(Collectors.toList());
                if(!subtitles.isEmpty()){
                    for(Subtitle subtitle:subtitles){
                        if (!dbFiles.contains(subtitle.getPath())) {
                            subtitleMapper.insertSubtitle(subtitle);
                        }else{
                            log.info(subtitle.getPath()+"已存在于数据库");
                        }
                    }
                }
            }
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


    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
    }

    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }
}
