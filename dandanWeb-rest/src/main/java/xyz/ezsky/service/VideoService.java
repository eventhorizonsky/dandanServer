package xyz.ezsky.service;

import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

public interface VideoService {

    public void addVideo(VideoVo videoVoList);

    VideoVo getVideoById(Integer id);
    List<VideoVo> getAllVideos();

    void updateVideo(VideoVo video);

    void deleteVideoById(Integer id);

}
