package xyz.ezsky.entity.vo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class LoginVO {
    private String userName;
    private String password;
    private String appId;
    private Long unixTimestamp;
    private String hash;
}

