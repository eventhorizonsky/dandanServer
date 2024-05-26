package xyz.ezsky.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.entity.vo.PlayerVo;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private SubtitleMapper subtitleMapper;

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
    @GetMapping("/anime")
    public ResponseEntity<List<VideoVo>> getVideosFromAnime(@Param("animeId")String animeId) {
        List<VideoVo> videos = videoService.getVideosFromAnime(animeId);
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
    }

    /**
     * 读取视频文件
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity displayMp4(@PathVariable Integer id,@RequestHeader(value = "Range", required = false) String rangeHeader, HttpServletRequest request, HttpServletResponse response) throws IOException {
        VideoVo videoVo = videoService.getVideoById(id);
        File videoFile = new File(videoVo.getFilePath());

        if (!videoFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        long fileSize = videoFile.length();

        InputStream inputStream;
        long start = 0;
        long end = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] range = rangeHeader.substring(6).split("-");
            start = Long.parseLong(range[0]);
            end = range.length > 1 ? Long.parseLong(range[1]) : fileSize - 1;
        }

        inputStream = new FileInputStream(videoFile);
        inputStream.skip(start);

        long contentLength = end - start + 1;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaTypeFactory.getMediaType(videoFile.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM));
        headers.setContentLength(contentLength);
        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(resource);

    }
    @GetMapping("/Subtitle/{id}")
    public ResponseEntity<FileSystemResource> getOutSrt(@PathVariable Integer id,HttpServletRequest request, HttpServletResponse response) {
        Subtitle subtitle = subtitleMapper.selectSubtitleById(id);
        File subFile = new File(subtitle.getPath());
        if(subFile.exists()) {
            return ResponseEntity.ok()
                    .body(new FileSystemResource(subFile));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{id}/playerInfo")
    public PlayerVo getPlayerInfo(@PathVariable Integer id) throws IOException {
        PlayerVo playerVo = new PlayerVo();
        VideoVo videoVo = videoService.getVideoById(id);
        playerVo.setUrl("/api/videos/" + id + "/stream");
        playerVo.setPoster("");
        File videoFile = new File(videoVo.getFilePath());
        List<Subtitle> subtitles = new ArrayList<>();
        List<Subtitle> dbSubtitles=subtitleMapper.selectSubtitleByVideoId(id);
        boolean hasDefault = false;
        for(Subtitle dbsubtitle:dbSubtitles){
            dbsubtitle.isDefault();
        }
        for(Subtitle dbsubtitle:dbSubtitles){
            // 检查是否有 isDefault() 为 true 的 Subtitle
            if (dbsubtitle.isDefault()) {
                hasDefault = true;
            }
            dbsubtitle.setUrl("/api/videos/Subtitle/"+dbsubtitle.getId());

            subtitles.add(dbsubtitle);
        }
        if(!hasDefault&&!dbSubtitles.isEmpty()){
            dbSubtitles.get(0).setDefault(true);
        }
        playerVo.setSubtitles(subtitles);
        return playerVo;
    }

    @GetMapping("/convert")
    public ResponseEntity<String> convertJsonToXml(@RequestParam String episodeId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonString = restTemplate.getForObject("https://api.dandanplay.net/api/v2/comment/" + episodeId + "?withRelated=true", String.class);

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

