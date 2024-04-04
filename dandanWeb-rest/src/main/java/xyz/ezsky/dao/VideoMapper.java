package xyz.ezsky.dao;

import org.apache.ibatis.annotations.Mapper;
import xyz.ezsky.entity.vo.User;
import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

@Mapper
public interface VideoMapper {

    void insertVideo(VideoVo videoVo);

    VideoVo selectVideoById(Integer id);

    List<VideoVo> selectAllVideos();

    void updateVideo(VideoVo video);

    void deleteVideoById(Integer id);

    VideoVo getNotMatchedVideoByName(String fileName);
}

