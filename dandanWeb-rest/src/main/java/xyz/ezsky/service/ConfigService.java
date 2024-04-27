package xyz.ezsky.service;

import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

public interface ConfigService {

    public List<VideoVo> addPath(String path);
    public List<VideoVo> addPathScan(String path);
}
