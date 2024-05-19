package xyz.ezsky.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;
import xyz.ezsky.entity.vo.Subtitle;
import xyz.ezsky.entity.vo.VideoVo;

@Slf4j
public class FileTool {

    public static VideoVo extractVideoInfo(String filePath) {
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            String fileName = file.getName();
            long fileSize = file.length();
            String hashValue = calculateFileHash(filePath);
            String fileExtension = "mp4";

            int lastIndexOfDot = fileName.lastIndexOf('.');
            if (lastIndexOfDot > 0) { // 确保文件名中包含了后缀
                fileExtension = fileName.substring(lastIndexOfDot + 1);
            }
            return new VideoVo(filePath,fileName, fileSize, hashValue,fileExtension);
        } else {
            return null; // 文件不存在或不是普通文件，返回null表示获取失败
        }
    }
    public static List<Subtitle> scanMkv(VideoVo videoVo){
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
                        processSubtitleInfo(subtitleInfo, subtitles,videoVo.getId());
                        subtitleInfo.clear();
                        isSubtitleInfo = false;
                    }
                }
                // Process last subtitle info block if any
                if (isSubtitleInfo && !subtitleInfo.isEmpty()) {
                    processSubtitleInfo(subtitleInfo, subtitles, videoVo.getId());
                }
            } catch (Exception exception) {
                log.error(exception.getLocalizedMessage());
            } finally {
                // 关闭 FFmpeg 进程
                ffmpeg.destroy();
            }
        }
        getSrt(subtitles,videoFile);
        return subtitles;
    }
    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            doNothing(line);
        }
    }
    private static void doNothing(String line) {
        System.out.println(line);
    }
    private static void getSrt(List<Subtitle> subtitles,File videoFile){
        for(int i=0;i<subtitles.size();i++){
            ProcessWrapper ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            try {
                File srcFile = new File(videoFile.getPath().split("\\.")[0]+i+".ass");
                subtitles.get(i).setPath(srcFile.getPath());
                while (!srcFile.exists()) {
                    // 执行 FFmpeg 命令来提取字幕
                    ffmpeg.addArgument("-i");
                    ffmpeg.addArgument(videoFile.getPath()); // 使用视频文件的实际路径
                    ffmpeg.addArgument("-map");
                    ffmpeg.addArgument("0:s:"+i); // 提取指定字幕轨道
                    ffmpeg.addArgument("-f");
                    ffmpeg.addArgument("ass"); // 指定输出字幕格式
                    ffmpeg.addArgument(srcFile.getPath()); // 输出到标准输出流
                    ffmpeg.execute();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                        blockFfmpeg(br);
                    }
                }
            } catch (IOException e) {
                return ;
            } finally {
                // 关闭 FFmpeg 进程
                ffmpeg.destroy();
            }

        }
    }
    private static void processSubtitleInfo(List<String> subtitleInfo, List<Subtitle> subtitles, Integer id) {
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
        subtitle.setSubtitleName(title);
        if (defaultExists) {
            subtitle.setDefault(false); // Set default to false if a default already exists
        } else {
            subtitle.setDefault(isDefault);
        }
        subtitles.add(subtitle);
    }
    public static boolean isSubtitile(String fileName) {
        return fileName.toLowerCase().endsWith(".ass")
                || fileName.toLowerCase().endsWith(".srt");
        // 添加其他字幕格式的判断条件
    }
    public static boolean isVideoFile(String fileName) {
        return fileName.toLowerCase().endsWith(".mp4")
                || fileName.toLowerCase().endsWith(".mkv")
                || fileName.toLowerCase().endsWith(".mov");
        // 添加其他视频格式的判断条件
    }
    public static String calculateFileHash(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);
            byte[] dataBytes = new byte[16 * 1024 * 1024]; // 16MB

            int bytesRead = fis.read(dataBytes);
            md.update(dataBytes, 0, bytesRead);

            byte[] hashBytes = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            fis.close();
            return hexString.toString().toUpperCase(); // 不区分大小写

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
