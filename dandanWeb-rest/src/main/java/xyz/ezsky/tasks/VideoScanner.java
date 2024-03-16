package xyz.ezsky.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;
import xyz.ezsky.utils.FileTool;

@Component
public class VideoScanner {

    @Autowired
    private VideoService videoService;

    @Scheduled(fixedRate = 60000) // 每10分钟执行一次
    public void scanVideos() {
        String directoryPath = "G:\\动漫";
        List<VideoVo> videoVoList=scanDirectory(directoryPath);
        OkHttpClient client = new OkHttpClient();
        List<JSONObject> requests = new ArrayList<>();
        if (videoVoList == null) {
            System.out.println("当前目录没有新的视频，扫描停止");
            return;
        }
        for (VideoVo video : videoVoList) {
            JSONObject requestObject = new JSONObject();
            requestObject.put("fileName", video.getFileName());
            requestObject.put("fileHash", video.getHashValue());
            requestObject.put("fileSize", video.getFileSize());
            requestObject.put("matchMode", "hashAndFileName");
            requests.add(requestObject);
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("requests", requests);

        String url = "https://api.dandanplay.net/api/v2/match/batch";
        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject responseObject = JSON.parseObject(responseBody);
                    JSONArray results = responseObject.getJSONArray("results");

                    if (results != null && !results.isEmpty()) {
                        for (int i = 0; i < results.size(); i++) {
                            JSONObject matchResult = results.getJSONObject(i).getJSONObject("matchResult");

                            int episodeId = matchResult.getIntValue("episodeId");
                            int animeId = matchResult.getIntValue("animeId");
                            String animeTitle = matchResult.getString("animeTitle");
                            String episodeTitle = matchResult.getString("episodeTitle");
                            String type = matchResult.getString("type");
                            String typeDescription = matchResult.getString("typeDescription");
                            int shift = matchResult.getIntValue("shift");
                            videoVoList.get(i).setAnimeId(animeId);
                            videoVoList.get(i).setEpisodeId(episodeId);
                            videoVoList.get(i).setAnimeTitle(animeTitle);
                            videoVoList.get(i).setEpisodeTitle(episodeTitle);
                            videoVoList.get(i).setType(type);
                            videoVoList.get(i).setTypeDescription(typeDescription);
                            videoVoList.get(i).setShift(shift);
                            File destDirectory = new File("G:\\newanime\\" + animeTitle);
                            destDirectory.mkdirs();
                            File sourceFile = new File(videoVoList.get(i).getFilePath());
                            File destFile = new File(destDirectory, episodeTitle+"."+videoVoList.get(i).getFileExtension());

                            Files.move(sourceFile.toPath(), destFile.toPath());
                            videoVoList.get(i).setFilePath(destFile.getPath());
                            System.out.println(videoVoList.get(i).getFileName()+"迁移成功");
                        }
                        videoService.addVideo(videoVoList);
                    }
                } else {
                    System.err.println("Request failed with code: " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private List<VideoVo> scanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("目录不存在或不是一个有效的目录：" + directoryPath);
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("目录为空：" + directoryPath);
            return null;
        }
        List<VideoVo> videoVoList=new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && isVideoFile(file.getName())) {
                videoVoList.add(extractVideoInfo(file.getAbsolutePath()));
            }
            if(videoVoList.size()>20){
                System.out.println("一次性扫描20个文件");
                break;
            }
        }
        return videoVoList;
    }

    private boolean isVideoFile(String fileName) {
        return fileName.toLowerCase().endsWith(".mp4")
                || fileName.toLowerCase().endsWith(".mkv")
                || fileName.toLowerCase().endsWith(".mov");
        // 添加其他视频格式的判断条件
    }

    private VideoVo extractVideoInfo(String filePath) {
        // 调用解析视频信息的方法，这里使用你实际的解析逻辑
        System.out.println("解析视频文件信息：" + filePath);
        return FileTool.extractVideoInfo(filePath);
    }
}
