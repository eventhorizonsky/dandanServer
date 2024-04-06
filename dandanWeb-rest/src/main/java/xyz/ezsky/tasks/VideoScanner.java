package xyz.ezsky.tasks;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;
import xyz.ezsky.utils.FileTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class VideoScanner {

    @Autowired
    private VideoService videoService;
    @Value("${danDanWeb.scanPath}")
    private String scanPath;
    @Value("${danDanWeb.tempPath}")
    private String tempPath;
    @Value("${danDanWeb.targetPath}")
    private String targetPath;
    @Value("${danDanWeb.failedPath}")
    private String failedPath;
    @Value("${danDanApi.match}")
    private String matchApi;

    @Scheduled(cron  = "${danDanWeb.cron}")
    public void scanVideos() {
        String directoryPath = scanPath;
        List<VideoVo> videoVoList=scanDirectory(directoryPath);
        if (videoVoList == null || videoVoList.isEmpty()) {
            return;
        }
        for (VideoVo video : videoVoList) {
           autoScanVideo(video);
        }
    }
    public void autoScanVideo(VideoVo video){
        OkHttpClient client = new OkHttpClient();
        String url = matchApi;
        JSONObject requestBody = new JSONObject();
        requestBody.put("fileName", video.getFileName());
        requestBody.put("fileHash", video.getHashValue());
        requestBody.put("fileSize", video.getFileSize());
        requestBody.put("matchMode", "hashAndFileName");
        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()&&response.body()!=null) {
                String responseBody = response.body().string();
                JSONObject responseObject = JSON.parseObject(responseBody);
                boolean isMatched=responseObject.getBoolean("isMatched");
                JSONArray results = responseObject.getJSONArray("matches");

                if (isMatched && !results.isEmpty()) {
                    log.info(video.getFileName()+"找到了匹配的弹幕，将自动进行刮削");
                    setVideoInfo(results,video);
                    File destDirectory = new File(targetPath + video.getAnimeTitle());
                    try{
                        destDirectory.mkdirs();
                        File sourceFile = new File(video.getFilePath());
                        String baseName= video.getEpisodeTitle().replaceAll("/", Integer.toString("/".hashCode()));
                        File destFile = new File(destDirectory, baseName+"."+video.getFileExtension());
                        int count = 1;
                        while (destFile.exists()) {
                            log.info(destFile.getName()+"已存在，进行重命名");
                            String newName = baseName + "(" + count + ")." + video.getFileExtension();
                            destFile = new File(destDirectory, newName);
                            count++;
                        }
                        Files.move(sourceFile.toPath(), destFile.toPath());
                        video.setFilePath(destFile.getPath());
                        log.info(video.getFileName()+"迁移成功");
                        video.setMatched("1");
                        videoService.addVideo(video);
                    }catch (Exception e){
                        log.error("迁移失败："+e.getLocalizedMessage());
                    }

                }else if(!isMatched&&!results.isEmpty()){
                    log.info(video.getFileName()+"没有找到文件的弹幕，自动选用最接近的内容，放置在temp目录");
                    setVideoInfo(results,video);
                    File destDirectory = new File(tempPath + video.getAnimeTitle());
                    try{
                        destDirectory.mkdirs();
                        File sourceFile = new File(video.getFilePath());
                        File destFile = new File(destDirectory, video.getFileName());
                        Files.move(sourceFile.toPath(), destFile.toPath());
                        video.setFilePath(destFile.getPath());
                        System.out.println(video.getFileName()+"迁移成功");
                        video.setMatched("2");
                        videoService.addVideo(video);
                    }catch (Exception e){
                        log.error("迁移失败："+e.getLocalizedMessage());
                    }

                }else {
                    log.info(video.getFileName()+"未能成功匹配");
                    File destDirectory = new File(failedPath + video.getAnimeTitle());
                    try{
                        destDirectory.mkdirs();
                        File sourceFile = new File(video.getFilePath());
                        File destFile = new File(destDirectory, video.getFileName());
                        Files.move(sourceFile.toPath(), destFile.toPath());
                        video.setFilePath(destFile.getPath());
                        System.out.println(video.getFileName()+"迁移成功");
                        video.setMatched("0");
                        videoService.addVideo(video);
                    }catch (Exception e){
                        log.error("迁移失败："+e.getLocalizedMessage());
                    }

                }
            } else {
                log.error(video.getFileName()+"匹配失败");
                log.error("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
    private void setVideoInfo(JSONArray results,VideoVo video){
        JSONObject matchResult = results.getJSONObject(0);
        int episodeId = matchResult.getIntValue("episodeId");
        int animeId = matchResult.getIntValue("animeId");
        String animeTitle = matchResult.getString("animeTitle");
        String episodeTitle = matchResult.getString("episodeTitle");
        String type = matchResult.getString("type");
        String typeDescription = matchResult.getString("typeDescription");
        int shift = matchResult.getIntValue("shift");
        video.setAnimeId(animeId);
        video.setEpisodeId(episodeId);
        video.setAnimeTitle(animeTitle);
        video.setEpisodeTitle(episodeTitle);
        video.setType(type);
        video.setTypeDescription(typeDescription);
        video.setShift(shift);
    }
    private List<VideoVo> scanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.info("目录不存在或不是一个有效的目录：" + directoryPath);
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("目录为空：" + directoryPath);
            return null;
        }
        List<VideoVo> videoVoList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && isVideoFile(file.getName())) {
                if(isDownloading(file)){
                    log.info(file.getName()+"正在下载中，跳过");
                }else{
                    videoVoList.add(extractVideoInfo(file.getAbsolutePath()));
                }
            }
            if (videoVoList.size() > 20) {
                log.info("一次性扫描20个文件");
                break;
            }
        }
        return videoVoList;
    }


    private boolean isDownloading(File file) {
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000); // 5 minutes in milliseconds
        return file.lastModified() > fiveMinutesAgo;

    }

    private boolean isVideoFile(String fileName) {
        return fileName.toLowerCase().endsWith(".mp4")
                || fileName.toLowerCase().endsWith(".mkv")
                || fileName.toLowerCase().endsWith(".mov");
        // 添加其他视频格式的判断条件
    }

    private VideoVo extractVideoInfo(String filePath) {
        log.info("解析视频文件信息：" + filePath);
        return FileTool.extractVideoInfo(filePath);
    }
}
