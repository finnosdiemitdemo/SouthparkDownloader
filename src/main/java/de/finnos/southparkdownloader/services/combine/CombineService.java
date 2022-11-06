package de.finnos.southparkdownloader.services.combine;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ffmpeg.ffmpeg.FfmpegHelper;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.SimpleProgressDialogEvent;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import de.finnos.southparkdownloader.services.BaseService;
import de.finnos.southparkdownloader.services.BaseServiceEvent;
import de.finnos.southparkdownloader.services.ProgressItem;
import de.finnos.southparkdownloader.services.download.SouthparkDownloadHelper;

import java.nio.file.Path;
import java.util.ArrayList;

public class CombineService extends BaseService<BaseServiceEvent, CombineService.CombineItem> {

    public CombineService(final ProgressDialog progressDialog) {
        super(progressDialog);
    }

    @Override
    public FfmpegProcess handleItem(final CombineItem combineItem) {
        setServiceEvent(getServiceEvent());

        final var progressItemPanel = new ProgressItemPanel();
        final var progressDialogEvent = new SimpleProgressDialogEvent(progressItemPanel);
        final FfmpegProcess process = combineParts(combineItem.getPaths(), combineItem.getOutputDir(), combineItem.getFilename(), progressDialogEvent);
        if (process != null) {
            progressDialog.addProgressItem(process, combineItem, progressItemPanel, progressDialogEvent);
        }
        return process;
    }

    private static FfmpegProcess combineParts (final ArrayList<String> paths, final String destEpisodeFolder, final String destinationFileName,
                                              final SimpleProgressDialogEvent progressEvent) {
        if (!paths.isEmpty()) {
            return SouthparkDownloadHelper.executeSingleStepFfmpegCommandWithProgressDialog(progressEvent,
                String.format("Combine %d paths...", paths.size()),
                () -> {
                    final String destFinalPath = Path.of(destEpisodeFolder, destinationFileName).toString();
                    return FfmpegHelper.executeFFmpegCombine(paths, destFinalPath);
                });
        }
        return null;
    }

    public static class CombineItem implements ProgressItem {
        private ArrayList<String> paths;
        private String outputDir;
        private String filename;

        public CombineItem(final ArrayList<String> paths, final String outputDir, final String filename) {
            this.paths = paths;
            this.outputDir = outputDir;
            this.filename = filename;
        }

        public ArrayList<String> getPaths() {
            return paths;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public String getFilename() {
            return filename;
        }

        @Override
        public String getName() {
            return I18N.i18n("episode.combine_parts");
        }
    }
}
