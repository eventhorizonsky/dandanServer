package xyz.ezsky.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@RestController
@RequestMapping("/dandanapi/api/v2")
public class ProxyController {

    @Autowired
    private  RestTemplate restTemplate;

    @GetMapping("/**")
    public Object getRequest(HttpServletRequest request) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String externalUrl = "https://api.dandanplay.net" + request.getRequestURI().substring("/dandanapi".length());
        String queryString = request.getQueryString();
        if (queryString != null) {
            externalUrl = externalUrl + "?" + queryString;
        }
        Request httpRequest = new Request.Builder()
                .url(externalUrl)
                .addHeader("Authorization",request.getHeader("Authorization")==null?"":request.getHeader("Authorization"))
                .addHeader("X-AppId","ezskyxyz")
                .addHeader("X-AppSecret","H9GyRm6oFY1ppukjCH7z6WJwG23jGqRZ")
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            // 这里可以根据需要处理响应，比如将响应转换为对象等
            String responseBody = response.body().string();
            return responseBody;
        }
    }


    @PostMapping("/**")
    public ResponseEntity<Object> postRequest(HttpServletRequest request, @RequestBody(required = false) Object requestBody) {
        String externalUrl = "https://api.dandanplay.net" + request.getRequestURI().split("/dandanapi")[1];

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization",request.getHeader("Authorization")==null?"":request.getHeader("Authorization"));
        headers.add("X-AppId","ezskyxyz");
        headers.add("X-AppSecret","H9GyRm6oFY1ppukjCH7z6WJwG23jGqRZ");
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            return restTemplate.postForEntity(externalUrl, requestEntity, Object.class);
    }
}
