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
    /**
     * 读取视频文件
     */
    @GetMapping("/{id}/subtitle")
    public ResponseEntity<FileSystemResource> getSrt(@PathVariable Integer id, @Param("tracks")String tracks, HttpServletRequest request, HttpServletResponse response) {
        VideoVo videoVo = videoService.getVideoById(id);
        File videoFile = new File(videoVo.getFilePath());
        if (videoFile.exists() && Objects.equals(videoVo.getFileExtension(), "mkv")) {
            ProcessWrapper ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            try {
                File srcFile = new File(videoFile.getPath().split("\\.")[0]+tracks+".ass");
                while (!srcFile.exists()) {
                    // 执行 FFmpeg 命令来提取字幕
                    ffmpeg.addArgument("-i");
                    ffmpeg.addArgument(videoFile.getPath()); // 使用视频文件的实际路径
                    ffmpeg.addArgument("-map");
                    ffmpeg.addArgument("0:s:"+tracks); // 提取指定字幕轨道
                    ffmpeg.addArgument("-f");
                    ffmpeg.addArgument("ass"); // 指定输出字幕格式
                    ffmpeg.addArgument(srcFile.getPath()); // 输出到标准输出流
                    ffmpeg.execute();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                        blockFfmpeg(br);
                    }
                }

                File file = new File(srcFile.getPath());
                if(file.exists()) {
                    return ResponseEntity.ok()
                            .body(new FileSystemResource(file));
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (IOException e) {
                log.error("Failed to extract subtitle: {}", e.getMessage());
                return null;
            } finally {
                // 关闭 FFmpeg 进程
                ffmpeg.destroy();
            }
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
        if (videoFile.exists() && Objects.equals(videoVo.getFileExtension(), "mkv")) {
            ProcessWrapper ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            try {
                // 执行 FFmpeg 命令来获取字幕信息
                ffmpeg.addArgument("-i");
                ffmpeg.addArgument(videoFile.getPath()); // 使用视频文件的实际路径
                ffmpeg.execute();
                BufferedReader br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
                String line;
                List<String> subtitleInfo = new ArrayList<>();
                boolean isSubtitleInfo = false;
                while ((line = br.readLine()) != null) {
                    // 检查输出以获取字幕轨道信息
                    if (line.contains("Stream #") && line.contains("Subtitle")) {
                        isSubtitleInfo = true;
                        subtitleInfo.add(line);
                    } else if (isSubtitleInfo && line.contains("title")) {
                        subtitleInfo.add(line);
                    } else if (isSubtitleInfo&&!line.contains("Metadata")) {
                        processSubtitleInfo(subtitleInfo, subtitles, id);
                        subtitleInfo.clear();
                        isSubtitleInfo = false;
                    }
                }
                // Process last subtitle info block if any
                if (isSubtitleInfo && !subtitleInfo.isEmpty()) {
                    processSubtitleInfo(subtitleInfo, subtitles, id);
                }
            } catch (Exception exception) {
                log.error(exception.getLocalizedMessage());
            }
        }
        playerVo.setSubtitles(subtitles);
        return playerVo;
    }

    private void processSubtitleInfo(List<String> subtitleInfo, List<Subtitle> subtitles, Integer id) {
        String trackIndex = "";
        String title = "";
        boolean isDefault = false;

        // Check if any default subtitle already exists
        boolean defaultExists = false;
        for (Subtitle subtitle : subtitles) {
            if (subtitle.isDefault()) {
                defaultExists = true;
                break;
            }
        }

        for (String line : subtitleInfo) {
            if (line.contains("Stream #")) {
                if (line.contains("default")) {
                    if (!defaultExists) {
                        isDefault = true;
                    }
                }
            } else if (line.contains("title")) {
                title = line.split("title\\s*:\\s*")[1].trim();
            }
        }
            Subtitle subtitle = new Subtitle();
        if (title.isEmpty()){
            title="字幕"+subtitles.size();
        }
        subtitle.setName(title);
            subtitle.setUrl("/api/videos/" + id + "/subtitle?tracks=" + subtitles.size());
            if (defaultExists) {
                subtitle.setDefault(false); // Set default to false if a default already exists
            } else {
                subtitle.setDefault(isDefault);
            }
            subtitles.add(subtitle);
    }


    private void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            doNothing(line);
        }
    }

    private void doNothing(String line) {
        System.out.println(line);
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

