package xyz.ezsky.service.serviceImpl;

import com.github.pagehelper.Page;
import org.springframework.stereotype.Service;
import xyz.ezsky.dao.AnimeMapper;
import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.common.PageBean;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.service.AnimeService;

import javax.annotation.Resource;
import java.util.List;
import com.github.pagehelper.PageHelper;


@Service
public class AnimeServiceImpl implements AnimeService {
    @Resource
    private AnimeMapper animeMapper;
    @Override
    public PageBean findAnimeList(AnimeVO animeVO) {
            PageHelper.startPage(animeVO.getPageNum(), animeVO.getPageSize()); // 默认第1页，每页10条数据
            Page<AnimeDTO> animeDTOList=(Page<AnimeDTO>)animeMapper.selectAnime(animeVO);
            return new PageBean(animeDTOList.getTotal(),animeDTOList.getResult());
    }
}
