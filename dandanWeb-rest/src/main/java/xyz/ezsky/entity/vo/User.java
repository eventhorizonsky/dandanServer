package xyz.ezsky.entity.vo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class User {
    private Long id;
    private String username;
    private String password;
}

