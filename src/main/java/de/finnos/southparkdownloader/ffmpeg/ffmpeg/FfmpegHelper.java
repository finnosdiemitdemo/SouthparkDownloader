package de.finnos.southparkdownloader.ffmpeg.ffmpeg;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.ffmpeg.FfmpegExecutionHelper;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class FfmpegHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FfmpegHelper.class);

    public static FfmpegProcess executeFFmpegDownload(final String source, final String destination) {
        return executeFFmpeg(
            String.format("-y -protocol_whitelist \"file,https,crypto,tls,tcp\" -i \"%s\" -c copy -bsf:a aac_adtstoasc \"%s\"", source, destination));
    }

    public static FfmpegProcess executeFFmpegCombine(final ArrayList<String> files, final String destFilePath) {
        String playlistFileContent = "";

        for (String path : files) {
            playlistFileContent += String.format("file '%s'\n", new File(path).getAbsolutePath());
        }

        final String playlistFilePath = "tmp.m3u8";
        final File playlistFile = new File(playlistFilePath);

        boolean _continue = true;
        if (playlistFile.exists() && !playlistFile.delete()) {
            _continue = false;
        }

        if (_continue) {
            try {
                if (playlistFile.createNewFile()) {
                    Files.writeString(Paths.get(playlistFilePath), playlistFileContent);
                    final var ffmpegProcess = FfmpegExecutionHelper.executeAsyncFfmpegCommand(
                        String.format("-y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", playlistFilePath, destFilePath));
                    ffmpegProcess.addFinishedListener(playlistFile::delete);
                    return ffmpegProcess;
                }
            } catch (IOException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }
        }

        return null;
    }

    public static FfmpegProcess executeFFmpegAddMetaData(final String filePath, final Map<String, String> metaData) {
        final var tmpSuffix = "_meta";
        final var tmpFilePath = filePath.replace(".mp4", tmpSuffix + ".mp4");
        final var ffmpegProcess = executeFFmpeg(String.format("-y -i \"%s\" %s -c copy \"%s\"", filePath, metaData.entrySet().stream()
            .map(entry -> String.format("-metadata %s=\"%s\"", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(" ")), tmpFilePath
        ));
        ffmpegProcess.addFinishedListener(() -> {
            final var oldFile = new File(filePath);
            if (oldFile.delete()) {
                if (!new File(tmpFilePath).renameTo(oldFile)) {
                    LOG.error("Unable to rename tmp file " + tmpFilePath);
                }
            }
        });
        return ffmpegProcess;
    }

    private static FfmpegProcess executeFFmpeg(final String command) {
        return FfmpegExecutionHelper.executeAsyncFfmpegCommand(command);
    }


}
