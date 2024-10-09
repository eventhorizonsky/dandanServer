package xyz.ezsky.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.ezsky.entity.common.PageVO;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnimeVO extends PageVO {
    private boolean isAir;
    //作品标题
    private String animeTitle;
    //作品类别
    private String type;
}
