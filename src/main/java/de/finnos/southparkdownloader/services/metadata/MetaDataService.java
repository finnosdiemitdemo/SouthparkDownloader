package de.finnos.southparkdownloader.services.metadata;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.ffmpeg.ffmpeg.FfmpegHelper;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.SimpleProgressDialogEvent;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import de.finnos.southparkdownloader.services.BaseService;
import de.finnos.southparkdownloader.services.BaseServiceEvent;
import de.finnos.southparkdownloader.services.download.SouthparkDownloadHelper;

import java.util.Map;
import java.util.stream.Collectors;

public class MetaDataService extends BaseService<BaseServiceEvent, Episode> {
    public MetaDataService(ProgressDialog dialog) {
        super(dialog);
    }

    @Override
    public FfmpegProcess handleItem(final Episode episode) {
        return setMetaData(episode);
    }

    private FfmpegProcess setMetaData(final Episode episode) {
        final var metaMap = Map.of(
            "episode_id", episode.getNumber().toString(),
            "title", episode.getName(),
            "description", episode.getDescription()
        );
        setServiceEvent(serviceEvent);

        final var progressItemPanel = new ProgressItemPanel();
        final var progressDialogEvent = new SimpleProgressDialogEvent(progressItemPanel);
        final FfmpegProcess process = setMetaData(episode.getFinalPath(), metaMap, progressDialogEvent);
        progressDialog.addProgressItem(process, I18N.i18n("set_meta_data"), progressItemPanel, progressDialogEvent);
        addProcess(process);
        return process;
    }

    public static FfmpegProcess setMetaData (final String fileName,
                                             final Map<String, String> mapMeta,
                                             final SimpleProgressDialogEvent progressEvent) {
        return SouthparkDownloadHelper.executeSingleStepFfmpegCommandWithProgressDialog(progressEvent,
            String.format("Set the following meta information's %s...", mapMeta.entrySet().stream()
                .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "))
            ),
            () -> FfmpegHelper.executeFFmpegAddMetaData(fileName, mapMeta));
    }
}
