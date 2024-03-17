package xyz.ezsky.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;
    @Value("${danDanWeb.targetPath}")
    private String targetPath;

    @Override
    public void addVideo(VideoVo videoVo) {
            videoMapper.insertVideo(videoVo);
    }
    @Override
    public VideoVo getVideoById(Integer id) {
        return videoMapper.selectVideoById(id);
    }

    @Override
    public List<VideoVo> getAllVideos() {
        return videoMapper.selectAllVideos();
    }

    @Override
    public void updateVideo(VideoVo video) {
        videoMapper.updateVideo(video);
        VideoVo videoVo=videoMapper.selectVideoById(video.getId());
        File destDirectory = new File(targetPath + videoVo.getAnimeTitle());
        try{
            destDirectory.mkdirs();
            File sourceFile = new File(videoVo.getFilePath());
            String baseName= videoVo.getEpisodeTitle().replaceAll("/", Integer.toString("/".hashCode()));
            File destFile = new File(destDirectory, baseName+"."+videoVo.getFileExtension());
            int count = 1;
            while (destFile.exists()) {
                log.info(destFile.getName()+"已存在，进行重命名");
                String newName = baseName + "(" + count + ")." + videoVo.getFileExtension();
                destFile = new File(destDirectory, newName);
                count++;
            }
            Files.move(sourceFile.toPath(), destFile.toPath());
            videoVo.setFilePath(destFile.getPath());
            log.info(videoVo.getFileName()+"迁移成功");
            videoMapper.updateVideo(videoVo);
        }catch (Exception e){
            log.error("迁移失败："+e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteVideoById(Integer id) {
        videoMapper.deleteVideoById(id);
    }

}
