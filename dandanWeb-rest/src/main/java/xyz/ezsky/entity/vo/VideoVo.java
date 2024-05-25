package xyz.ezsky.entity.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class VideoVo {
    private int id;
    //文件路径
    private String filePath;
    //文件名称
    private String fileName;
    //文件大小
    private long fileSize;
    //视频前16MB哈希值
    private String hashValue;
    //视频格式
    private String fileExtension;
    //弹幕库ID
    private int episodeId;
    //作品ID
    private int animeId;
    //作品标题
    private String animeTitle;
    //剧集标题
    private String episodeTitle;
    //作品类别
    private String type;
    //类型描述
    private String typeDescription;
    //弹幕偏移时间（弹幕应延迟多少秒出现）。此数字为负数时表示弹幕应提前多少秒出现。
    private int shift;
    //是否匹配成功，0为未匹配，1为成功匹配，2为临时匹配
    private String Matched;
    //添加时间
    private String joinTime;
    //是否连载中
    private String isAir;

    public VideoVo() {
    }

    public VideoVo(String filePath, String fileName, long fileSize, String hashValue, String fileExtension,String joinTime) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.hashValue = hashValue;
        this.fileExtension=fileExtension;
        this.joinTime=joinTime;
    }

}
