package xyz.ezsky.tasks;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import xyz.ezsky.dao.AnimeMapper;
import xyz.ezsky.dao.SubtitleMapper;
import xyz.ezsky.dao.VideoMapper;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.entity.dto.AppConfigDTO;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;
import xyz.ezsky.service.VideoService;
import xyz.ezsky.utils.FileTool;
import xyz.ezsky.utils.SrtToAssConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * 视频扫描仪
 *
 * @author eventhorizonsky
 * @date 2024/04/28
 */
@Component
@Slf4j
public class VideoScanner {
    @Value("${danDanApi.match}")
    private String matchApi;
    @Value("${danDanApi.anime}")
    private String animeApi;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private SubtitleMapper subtitleMapper;
    @Autowired
    private AnimeMapper animeMapper;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * 开始扫描视频
     *
     * @param cronExpression cron 表达式
     */
    public void startScanVideos(String cronExpression) {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }

        scheduledFuture = taskScheduler.schedule(this::scan, new CronTrigger(cronExpression));
    }

    /**
     * 停止扫描视频
     */
    public void stopScanVideos() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 扫描
     */
    public void scan() {
        scanVideos();
        scanSrt();
    }

    /**
     * 匹配字幕
     */
    public void scanSrt() {
        List<Subtitle> subtitles = subtitleMapper.selectSubtitleNotMatch();
        if (subtitles.isEmpty()) {
            return;
        }
        for (Subtitle subtitle : subtitles) {
            if (FileTool.isSrtFile(subtitle.getPath())) {
                String newPath = SrtToAssConverter.convertSrtToAss(subtitle.getPath());
                subtitle.setPath(newPath);
            }
            List<VideoVo> videoVos = videoMapper.selectVideoBySubtitle(subtitle.getPath().substring(0, subtitle.getPath().lastIndexOf(".")));
            if (videoVos != null && !videoVos.isEmpty()) {
                subtitle.setVideoId(videoVos.get(0).getId());
                subtitleMapper.updateSubtitle(subtitle);
            }
        }
    }

    /**
     * 扫描视频
     */
    public void scanVideos() {
        List<VideoVo> videoVoList = videoMapper.selectAllVideosNotMatch();
        if (videoVoList.isEmpty()) {
            return;
        }
        for (VideoVo video : videoVoList) {
            autoScanVideo(video);
        }
    }

    /**
     * 自动扫描视频
     *
     * @param video 视频
     */
    public void autoScanVideo(VideoVo video) {
        File targetFile = new File(video.getFilePath());
        if (isDownloading(targetFile)) {
            log.info(targetFile.getName() + "正在下载中，跳过");
            return;
        }
        List<Subtitle> subtitles = FileTool.scanMkv(video);
        if (!subtitles.isEmpty()) {
            for (Subtitle subtitle : subtitles) {
                subtitle.setVideoId(video.getId());
                subtitleMapper.insertSubtitle(subtitle);
            }
        }
        video.setHashValue(FileTool.calculateFileHash(video.getFilePath()));
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
                .addHeader("X-AppId","ezskyxyz")
                .addHeader("X-AppSecret","H9GyRm6oFY1ppukjCH7z6WJwG23jGqRZ")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject responseObject = JSON.parseObject(responseBody);
                boolean isMatched = responseObject.getBoolean("isMatched");
                JSONArray results = responseObject.getJSONArray("matches");

                if (isMatched && !results.isEmpty()) {
                    log.info(video.getFileName() + "找到了匹配的弹幕，将自动进行刮削");
                    setVideoInfo(results, video);
                    try {
                        video.setMatched("1");
                        videoMapper.updateVideo(video);
                    } catch (Exception e) {
                        log.error("迁移失败：" + e.getLocalizedMessage());
                    }

                } else if (!isMatched && !results.isEmpty()) {
                    log.info(video.getFileName() + "没有找到文件的弹幕，自动选用最接近的内容，放置在temp目录");
                    setVideoInfo(results, video);
                    try {
                        video.setMatched("2");
                        videoMapper.updateVideo(video);
                    } catch (Exception e) {
                        log.error("迁移失败：" + e.getLocalizedMessage());
                    }

                } else {
                    log.info(video.getFileName() + "未能成功匹配");
                    try {
                        video.setMatched("3");
                        videoMapper.updateVideo(video);
                    } catch (Exception e) {
                        log.error("迁移失败：" + e.getLocalizedMessage());
                    }
                }
                AnimeDTO animeDTO = new AnimeDTO();
                animeDTO.setAnimeId(video.getAnimeId());
                animeDTO.setAnimeTitle(video.getAnimeTitle());
                animeDTO.setType(video.getType());
                animeDTO.setTypeDescription(video.getTypeDescription());
                if (video.getAnimeId() != 0 && animeMapper.selectAnimeById(video.getAnimeId()) == null) {
                    animeMapper.insertAnime(animeDTO);
                }
                if (animeDTO.getAnimeId() != 0){
                    setAirInfo(animeDTO);
                }
            } else {
                log.error(video.getFileName() + "匹配失败");
                log.error("Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void setAirInfo(AnimeDTO animeDTO) {
        OkHttpClient client = new OkHttpClient();
        String url = animeApi + "/" + animeDTO.getAnimeId();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-AppId","ezskyxyz")
                .addHeader("X-AppSecret","H9GyRm6oFY1ppukjCH7z6WJwG23jGqRZ")
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject responseObject = JSON.parseObject(responseBody).getObject("bangumi",JSONObject.class);
                animeDTO.setAir(responseObject.getBoolean("isOnAir"));
                animeDTO.setAirDay(responseObject.getInteger("airDay"));
                animeMapper.updateAnime(animeDTO);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 设置视频信息
     *
     * @param results 结果
     * @param video   视频
     */
    private void setVideoInfo(JSONArray results, VideoVo video) {
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


    private boolean isDownloading(File file) {
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000); // 5 minutes in milliseconds
        return file.lastModified() > fiveMinutesAgo;

    }
}
