package de.finnos.southparkdownloader.ffmpeg.ffprobe;

import de.finnos.southparkdownloader.RegexHelper;
import de.finnos.southparkdownloader.ffmpeg.FfmpegExecutionHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class FfprobeHelper {

    private static final double DURATION_PER_FRAME = 0.041700;

    private static final Map<String, FileInfo> MAP_FILE_PATH_INFO = new HashMap<>();

    public static VideoInfo getInfo (final String filepath) {
        VideoInfo info = null;

        final String command = String.format("-show_format -v quiet -show_streams \"%s\"", filepath);
        final String content = FfmpegExecutionHelper.executeSyncFfprobeCommand(command);

        final Matcher matcher = RegexHelper.matchPrepare("\\[STREAM\\][\\s\\S]+?codec_type=video[\\s\\S]+?width=(\\d+)\\nheight=(\\d+)[\\s\\S]+?avg_frame_rate=(\\d+)\\/(\\d+)[\\s\\S]+?duration=([\\d.]+)[\\s\\S]+?nb_frames=([\\d.]+)[\\s\\S]+?\\[\\/STREAM\\]", content);
        if (matcher.find()) {
            final var durationPerFrame = Double.parseDouble(matcher.group(4)) / Double.parseDouble(matcher.group(3));
            info = new VideoInfo();
            info.setWidth(Integer.parseInt(matcher.group(1)));
            info.setHeight(Integer.parseInt(matcher.group(2)));
            info.setExpectedDurationInSeconds((int)Double.parseDouble(matcher.group(5)));
            info.setActualDurationInSeconds((int)((Double.parseDouble(matcher.group(6)) * durationPerFrame)));
        }

        return info;
    }

    public static boolean isVideoCorrupt(final String filepath) {
        final var lastModified = new File(filepath).lastModified();
        if (MAP_FILE_PATH_INFO.containsKey(filepath) && MAP_FILE_PATH_INFO.get(filepath).lastModified == lastModified) {
            return MAP_FILE_PATH_INFO.get(filepath).isCorrupt;
        }
        final String command = String.format("-v error \"%s\"", filepath);
        final String content = FfmpegExecutionHelper.executeSyncFfprobeCommand(command);
        final var isCorrupt = content.toLowerCase().contains("invalid data found when processing input");
        MAP_FILE_PATH_INFO.put(filepath, new FileInfo(lastModified, isCorrupt));
        return isCorrupt;
    }

    public record FileInfo (long lastModified, boolean isCorrupt){}
}
