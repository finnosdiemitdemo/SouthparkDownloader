package de.finnos.southparkdownloader.services.download;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.finnos.southparkdownloader.DownloadHelper;
import de.finnos.southparkdownloader.RegexHelper;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.SimpleProgressDialogEvent;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SouthparkDownloadHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SouthparkDownloadHelper.class);

    public static String getPartUrl(final String generatorUrl) {
        LOG.trace("Download generator file: {}", generatorUrl);
        final JsonObject infoJson = DownloadHelper.downloadJsonFile(generatorUrl);
        LOG.trace("Parse json");
        if (infoJson.has("package")) {
            final JsonObject objectPackage = infoJson.getAsJsonObject("package");
            if (objectPackage.has("video")) {
                final JsonObject objectVideo = objectPackage.getAsJsonObject("video");
                if (objectVideo.has("item")) {
                    final JsonArray items = objectVideo.getAsJsonArray("item");
                    if (items.size() >= 1) {
                        final JsonObject item = items.get(0).getAsJsonObject();
                        if (item.has("rendition")) {
                            final JsonArray renditions = item.getAsJsonArray("rendition");
                            if (renditions.size() >= 1) {
                                final JsonObject objectRendition = renditions.get(0).getAsJsonObject();
                                if (objectRendition.has("src")) {
                                    final String partUrl = objectRendition.get("src").getAsString();

                                    LOG.trace("Found part url: {}", partUrl);

                                    return partUrl;
                                }
                            }
                        }
                    }
                }
            }
        }

        LOG.error("Could not found part url");

        return null;
    }

    public static Map<String, String> getPartStreams(final String streamsUrl) {
        final Map<String, String> result = new HashMap<>();

        final String fileContent = DownloadHelper.downloadFile(streamsUrl);

        RegexHelper.match("#EXT-X-STREAM-INF:.*RESOLUTION=([^,\\n]+).*\\n(.*)", fileContent, matcher -> result.put(matcher.group(1), matcher.group(2)));

        return result;
    }

    public static FfmpegProcess executeSingleStepFfmpegCommandWithProgressDialog(final SimpleProgressDialogEvent progressEvent,
                                                                                 final String initialMessage,
                                                                                 final Supplier<FfmpegProcess> executeFunction) {
        progressEvent.initProgress(1);
        progressEvent.updateProgress(initialMessage);

        final FfmpegProcess ffmpegProcess = executeFunction.get();
        ffmpegProcess.addFinishedListener(() -> {
            progressEvent.updateProgress(1, false);
            progressEvent.finished();
        });
        ffmpegProcess.addInterruptedListener(progressEvent::interrupted);
        return ffmpegProcess;
    }
}
