package xyz.ezsky.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;


    @GetMapping("/{id}")
    public ResponseEntity<VideoVo> getVideoById(@PathVariable Integer id) {
        VideoVo video = videoService.getVideoById(id);
        if (video != null) {
            return new ResponseEntity<>(video, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoVo>> getAllVideos() {
        List<VideoVo> videos = videoService.getAllVideos();
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateVideo(@PathVariable Integer id, @RequestBody VideoVo video) {
        video.setId(id); // 设置要更新的记录的ID
        videoService.updateVideo(video);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideoById(@PathVariable Integer id) {
        videoService.deleteVideoById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }/**
     * 读取视频文件
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<FileSystemResource> displayMp4(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        VideoVo videoVo = videoService.getVideoById(id);
        File videoFile = new File(videoVo.getFilePath());
        if(videoFile.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(videoFile.length());
            headers.setContentDispositionFormData("attachment", videoVo.getFileName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(videoFile));
        } else {
            return ResponseEntity.notFound().build();
        }

    }


        @GetMapping("/convert")
        public ResponseEntity<String> convertJsonToXml(@RequestParam String episodeId) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String jsonString = restTemplate.getForObject("https://api.dandanplay.net/api/v2/comment/"+episodeId+"?withRelated=true", String.class);

                // Convert JSON to XML
                String xmlString = convertJsonToXmlString(jsonString);

                return ResponseEntity.ok(xmlString);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
            }
        }

        private String convertJsonToXmlString(String jsonString) throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            int count = jsonNode.get("count").asInt();

            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<i xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
                    .append("<chatserver>chat.bilibili.com</chatserver>")
                    .append("<chatid>10000</chatid>")
                    .append("<mission>0</mission>")
                    .append("<maxlimit>8000</maxlimit>")
                    .append("<source>e-r</source>")
                    .append("<ds>931869000</ds>")
                    .append("<de>937654881</de>")
                    .append("<max_count>")
                    .append(count)
                    .append("</max_count>");

            JsonNode commentsNode = jsonNode.get("comments");
            if (commentsNode != null && commentsNode.isArray()) {
                for (JsonNode commentNode : commentsNode) {
                    String[] plist = commentNode.get("p").asText().split(",");
                    String P = plist[0] + "," + plist[1] + ",25," + plist[2] + "," + commentNode.get("cid") + ",0,0,0";
                    String m = commentNode.get("m").asText();
                    xmlBuilder.append("<d p=\"").append(P).append("\">")
                            .append(m)
                            .append("</d>");
                }
            }

            xmlBuilder.append("</i>");
            return xmlBuilder.toString();
        }


}

