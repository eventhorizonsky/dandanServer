package xyz.ezsky.utils;

import java.io.*;
import java.util.Scanner;

public class SrtToAssConverter {

    public static String convertSrtToAss(String srtFilePath) {
        try {
            File srtFile = new File(srtFilePath);
            String assFilePath = srtFilePath.replaceAll("(?i).srt$", ".ass");

            FileWriter assWriter = new FileWriter(assFilePath);
            BufferedWriter bufferedWriter = new BufferedWriter(assWriter);
            Scanner scanner = new Scanner(srtFile);
            int index = 1;
            bufferedWriter.write("[V4+ Styles]\n");
            bufferedWriter.write("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, TertiaryColour, BackColour, Bold, Italic, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, AlphaLevel, Encoding\n");
            bufferedWriter.write("Style: Default,Arial,20,&H00FFFFFF,&H0000FFFF,&H00000000,&H80000000,0,0,1,1,2,2,20,20,20,0,0\n");
            bufferedWriter.write("[Events]\n");
            bufferedWriter.write("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" --> ");
                    if (parts.length == 2) {
                        String[] startTime = parts[0].split(",");
                        String[] endTime = parts[1].split(",");

                        // Formatting time with two-digit precision for milliseconds
                        String startTimeFormatted = String.format("%s.%02d", startTime[0], Integer.parseInt(startTime[1]) / 10);
                        String endTimeFormatted = String.format("%s.%02d", endTime[0], Integer.parseInt(endTime[1]) / 10);

                        bufferedWriter.write("Dialogue: 0,");
                        bufferedWriter.write(startTimeFormatted + ",");
                        bufferedWriter.write(endTimeFormatted + ",");
                        bufferedWriter.write("Default,,0,0,0,,");
                        bufferedWriter.write(scanner.nextLine().trim());
                        bufferedWriter.newLine();
                    }
                }
            }

            bufferedWriter.close();
            return assFilePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) {
        String srtFilePath = "D:\\code lib\\dandan\\dandanWeb\\app\\media\\temp\\[ReinForce] Shingeki no Kyojin - Chronicle (BDRip 1920x1080 x264 FLAC).srt";
        String assFilePath = convertSrtToAss(srtFilePath);
        if (assFilePath != null) {
            System.out.println("Converted successfully. ASS file path: " + assFilePath);
        } else {
            System.out.println("Conversion failed.");
        }
    }
}
