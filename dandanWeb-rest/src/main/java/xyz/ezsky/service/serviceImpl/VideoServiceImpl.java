package xyz.ezsky.service.serviceImpl;

import org.springframework.stereotype.Service;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.vo.User;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;

    @Override
    public void addVideo(List<VideoVo> videoVoList) {
        for(VideoVo videoVo :videoVoList){
            videoMapper.insertVideo(videoVo);
        }
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

}
