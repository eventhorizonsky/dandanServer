package xyz.ezsky.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.ezsky.dao.AnimeMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;
import xyz.ezsky.tasks.VideoScanner;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;
    @Autowired
    private AnimeMapper animeMapper;
    @Autowired
    private VideoScanner videoScanner;

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
        VideoVo videoVo=videoMapper.selectVideoById(video.getId());
        videoMapper.updateVideo(video);
        if(videoVo.getAnimeId()!=0){
            List<VideoVo> videoVoList=videoMapper.selectVideoByAnimeId(videoVo.getAnimeId());
            if(videoVoList.isEmpty()){
                animeMapper.deleteAnimeById(videoVo.getAnimeId());
            }
        }
        AnimeDTO animeDTO=animeMapper.selectAnimeById(video.getAnimeId());
        if(animeDTO==null){
            AnimeDTO animeDTO1=new AnimeDTO();
            animeDTO1.setAnimeId(video.getAnimeId());
            animeDTO1.setAnimeTitle(video.getAnimeTitle());
            animeDTO1.setTypeDescription(videoVo.getTypeDescription());
            animeDTO1.setType(video.getType());
            videoScanner.setAirInfo(animeDTO1);
            animeMapper.insertAnime(animeDTO1);
        }
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
