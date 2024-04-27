package xyz.ezsky.dao;

import org.apache.ibatis.annotations.Mapper;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

@Mapper
public interface SubtitleMapper {

    void insertSubtitle(Subtitle subtitle);

    void updateSubtitle(Subtitle subtitle);

    Subtitle selectSubtitleById(Integer id);
    List<Subtitle> selectSubtitleByVideoId(Integer videoId);

    List<Subtitle> selectSubtitleByPath(String path);

    List<Subtitle> selectAllSubtitle();

    void deleteSubtitleById(Integer id);

    List<Subtitle> selectSubtitleNotMatch();
}

