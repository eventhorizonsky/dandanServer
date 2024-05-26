package xyz.ezsky.entity;

import lombok.Data;

@Data
public class AnimeVO {
    private boolean isAir;
    //作品标题
    private String animeTitle;
    //作品类别
    private String type;
}
