package xyz.ezsky.controller.v1;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.entity.AnimeVO;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.entity.vo.PlayerVo;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.AnimeService;
import xyz.ezsky.service.VideoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/anime")
public class AnimeController {

    @Autowired
    private AnimeService animeService;

    @Autowired
    private SubtitleMapper subtitleMapper;

    @PostMapping
    public ResponseEntity<List<AnimeDTO>> getAnime(@RequestBody AnimeVO animeVO) {
        List<AnimeDTO> videos = animeService.findAnimeList(animeVO);
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }


}

