package de.finnos.southparkdownloader.services.download;

import de.finnos.southparkdownloader.DownloadHelper;
import de.finnos.southparkdownloader.RegexHelper;
import de.finnos.southparkdownloader.classes.EpisodePartStream;
import de.finnos.southparkdownloader.ffmpeg.ffmpeg.FfmpegHelper;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.ParameterizedProgressDialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressEvent;
import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadItem;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import de.finnos.southparkdownloader.services.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadService extends BaseService<DownloadServiceEvent, DownloadItem> {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadService.class);

    public DownloadService(ProgressDialog progressDialog) {
        super(progressDialog);
    }

    @Override
    public FfmpegProcess handleItem(final DownloadItem item) {
        final var progressItemPanel = new ProgressItemPanel();
        final var event = new ParameterizedProgressDialogEvent<DownloadItem>(progressItemPanel) {
            @Override
            public void finished(final DownloadItem currentItem) {
                getServiceEvent().doneItem(currentItem);
            }

            @Override
            public void updateProgress(final String message) {
                super.updateProgress(message);
                updateProgress(RegexHelper.matchCount("\\.ts' for reading", message), true);
            }
        };
        final FfmpegProcess process = download(item, event);
        if (process != null && getServiceEvent() != null) {
            progressDialog.addProgressItem(process, item, progressItemPanel, event);
            getServiceEvent().startedItem(item);
        }
        return process;
    }

    private static FfmpegProcess download(final DownloadItem item, final ParameterizedProgressDialogEvent<DownloadItem> progressEvent) {
        if (item.prepareForDownload()) {
            progressEvent.updateProgress(String.format("Download part %d...", item.getEpisodePart().getIndex() + 1));

            if (new File(item.getEpisodePart().getInputPath()).exists()) {
                if (!new File(item.getEpisodePart().getInputPath()).delete()) {
                    return null;
                }
            }

            final String streamFilePath = createStreamFile(item.getStream(), item.getEpisodePart().getInputPath(), progressEvent);
            if (streamFilePath != null) {
                final var ffmpegProcess = FfmpegHelper.executeFFmpegDownload(streamFilePath, item.getEpisodePart().getOutputPath());
                ffmpegProcess.addFinishedListener(() -> progressEvent.finished(item));
                ffmpegProcess.addInterruptedListener((e) -> progressEvent.interrupted(item, e));
                return ffmpegProcess;
            } else {
                progressEvent.updateProgress("Unable create the download stream file: " + item.getEpisodePart().getInputPath());
            }
        }

        return null;
    }

    private static String createStreamFile(final EpisodePartStream stream, final String inputPath, final ProgressEvent progressEvent) {
        final String streamFileContent = DownloadHelper.downloadFile(stream.getUrl());

        if (progressEvent != null) {
            progressEvent.initProgress(RegexHelper.matchCount("https:\\/\\/.*\\.ts", streamFileContent));
        }

        try {
            final File streamFileTmp = new File(inputPath);
            if (streamFileTmp.createNewFile()) {
                Files.writeString(Paths.get(inputPath), streamFileContent);
                return inputPath;
            }
        } catch (IOException e) {
            LOG.error("Unable to create download stream file", e);
        }

        return null;
    }
}
