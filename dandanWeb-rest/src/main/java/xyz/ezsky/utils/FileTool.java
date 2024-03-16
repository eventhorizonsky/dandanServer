package xyz.ezsky.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import xyz.ezsky.entity.vo.VideoVo;

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

    private static String calculateFileHash(String filePath) {
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
