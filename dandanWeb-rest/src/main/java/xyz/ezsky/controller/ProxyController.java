package xyz.ezsky.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/dandanapi/api/v2")
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/**")
    public Object  getRequest(HttpServletRequest request) {
        String externalUrl = "https://api.dandanplay.net" + request.getRequestURI().split("/dandanapi")[1];
        return restTemplate.getForObject(externalUrl, Object.class);
    }
    @PostMapping("/**")
    public ResponseEntity<Object> postRequest(HttpServletRequest request, @RequestBody(required = false) Object requestBody) {
        String externalUrl = "https://api.dandanplay.net" + request.getRequestURI().split("/dandanapi")[1];

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(externalUrl, requestEntity, Object.class);
    }
}
