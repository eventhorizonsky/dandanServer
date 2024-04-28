package xyz.ezsky.service;

import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

public interface ConfigService {

    public boolean isAddPath(String path);
    public void addPathScan(String path);
}
