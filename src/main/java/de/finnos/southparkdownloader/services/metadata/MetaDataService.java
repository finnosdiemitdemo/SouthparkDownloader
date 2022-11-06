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
import de.finnos.southparkdownloader.services.ProgressItem;
import de.finnos.southparkdownloader.services.download.SouthparkDownloadHelper;

import java.util.Map;
import java.util.stream.Collectors;

public class MetaDataService extends BaseService<BaseServiceEvent, MetaDataService.MetaProgressItem> {
    public MetaDataService(ProgressDialog dialog) {
        super(dialog);
    }

    @Override
    public FfmpegProcess handleItem(final MetaProgressItem item) {
        return setMetaData(item);
    }

    private FfmpegProcess setMetaData(final MetaProgressItem item) {
        final var metaMap = Map.of(
            "episode_id", item.episode.getNumber().toString(),
            "title", item.episode.getName(),
            "description", item.episode.getDescription()
        );
        setServiceEvent(serviceEvent);

        final var progressItemPanel = new ProgressItemPanel();
        final var progressDialogEvent = new SimpleProgressDialogEvent(progressItemPanel);
        final FfmpegProcess process = setMetaData(item.episode.getFinalPath(), metaMap, progressDialogEvent);
        progressDialog.addProgressItem(process, item, progressItemPanel, progressDialogEvent);
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

    public static class MetaProgressItem implements ProgressItem {
        private final Episode episode;

        public MetaProgressItem(final Episode episode) {
            this.episode = episode;
        }

        @Override
        public String getName() {
            return I18N.i18n("set_meta_data");
        }
    }
}
