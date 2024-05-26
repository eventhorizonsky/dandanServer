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
    }

    @Override
    public void deleteVideoById(Integer id) {
        videoMapper.deleteVideoById(id);
    }

    @Override
    public VideoVo getNotMatchedVideoByName(String fileName) {
        return videoMapper.getNotMatchedVideoByName(fileName);
    }

    @Override
    public List<VideoVo> getVideosFromAnime(String animeId) {
        return videoMapper.getVideosFromAnime(animeId);
    }

}
