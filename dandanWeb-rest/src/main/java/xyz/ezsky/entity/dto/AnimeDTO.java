package xyz.ezsky.entity.dto;

import lombok.Data;

@Data
public class AnimeDTO {
    //作品ID
    private int animeId;
    //作品标题
    private String animeTitle;
    //作品类别
    private String type;
    //类型描述
    private String typeDescription;
    //是否连载中
    private boolean isAir;
    //连载时间
    private int airDay;
}
