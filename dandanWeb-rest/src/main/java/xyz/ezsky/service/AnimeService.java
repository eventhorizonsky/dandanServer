package xyz.ezsky.service;

import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.dto.AnimeDTO;

import java.util.List;

public interface AnimeService {
    List<AnimeDTO> findAnimeList(AnimeVO animeVO);
}
