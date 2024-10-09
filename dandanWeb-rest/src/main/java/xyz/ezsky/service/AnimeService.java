package xyz.ezsky.service;

import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.common.PageBean;
import xyz.ezsky.entity.dto.AnimeDTO;

import java.util.List;

public interface AnimeService {
    PageBean findAnimeList(AnimeVO animeVO);
}
