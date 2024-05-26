package xyz.ezsky.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.ezsky.dao.AnimeMapper;
import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.service.AnimeService;

import java.util.List;

@Service
public class AnimeServiceImpl implements AnimeService {
    @Autowired
    private AnimeMapper animeMapper;
    @Override
    public List<AnimeDTO> findAnimeList(AnimeVO animeVO) {
        return animeMapper.selectAnime(animeVO);
    }
}
