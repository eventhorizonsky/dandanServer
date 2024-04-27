package xyz.ezsky.dao;

import org.apache.ibatis.annotations.Mapper;
import xyz.ezsky.entity.vo.VideoVo;

import java.util.List;

@Mapper
public interface ScanPathMapper {

    void insertScanPath(String scanPath);

    String selectScanPathById(Integer id);

    List<String> selectScanPathBypath(String scanPath);


    List<String> selectAllScanPath();

    void deleteScanPathById(Integer id);
}

