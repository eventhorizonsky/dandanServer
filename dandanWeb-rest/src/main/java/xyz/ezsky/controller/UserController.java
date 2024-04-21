package xyz.ezsky.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import xyz.ezsky.entity.vo.LoginVO;
import xyz.ezsky.entity.vo.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/login")
    public ResponseEntity<Object> login(HttpServletRequest request, @RequestBody User user) {
        String externalUrl = "https://api.dandanplay.net/api/v2/login";
        String appId="ezskyxyz";
        String AppSecret="H9GyRm6oFY1ppukjCH7z6WJwG23jGqRZ";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        LoginVO loginVO=new LoginVO();
        loginVO.setUserName(user.getLoginName());
        loginVO.setPassword(user.getPassWord());
        loginVO.setUnixTimestamp(System.currentTimeMillis()/ 1000);
        loginVO.setAppId(appId);
        String hashStr=appId+loginVO.getPassword()+loginVO.getUnixTimestamp()+loginVO.getUserName()+AppSecret;
        loginVO.setHash(getMD5Hash(hashStr));
        HttpEntity<Object> requestEntity = new HttpEntity<>(loginVO, headers);
        return restTemplate.postForEntity(externalUrl, requestEntity, Object.class);
    }
    public  String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
