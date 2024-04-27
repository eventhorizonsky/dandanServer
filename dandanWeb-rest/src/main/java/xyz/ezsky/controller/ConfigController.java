package xyz.ezsky.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.ConfigService;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@Slf4j
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping("/addPath")
    public List<VideoVo> addPath(@Param("path") String path) {
        return configService.addPath(path);
    }
}
