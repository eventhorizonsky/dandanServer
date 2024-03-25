package xyz.ezsky.controller;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
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
    public Object  proxyRequest(HttpServletRequest request) {
        String externalUrl = "https://api.dandanplay.net" + request.getRequestURI().split("/dandanapi")[1];
        return restTemplate.getForObject(externalUrl, Object.class);
    }
}
