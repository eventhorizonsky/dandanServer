package xyz.ezsky.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.ezsky.entity.dto.AppConfigDTO;
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
    public boolean addPath(@RequestBody AppConfigDTO appConfigDTO) {
        boolean needAdd=configService.isAddPath(appConfigDTO.getTargetPath());
        if(needAdd){
            configService.addPathScan(appConfigDTO.getTargetPath());
        }
        return needAdd;
    }
}
