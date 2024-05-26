package xyz.ezsky.dao;

import org.apache.ibatis.annotations.Mapper;
import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.dto.AnimeDTO;

import java.util.List;

@Mapper
public interface AnimeMapper {

    void insertAnime(AnimeDTO Anime);

    AnimeDTO selectAnimeById(Integer id);


    List<AnimeDTO> selectAnime(AnimeVO animeVO);

    void deleteAnimeById(Integer id);
    void updateAnime(AnimeDTO animeDTO);
    void updateAllAnimeIsAirToFalse();
}

