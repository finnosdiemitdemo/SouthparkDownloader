package de.finnos.southparkdownloader.gui.downloadepisode.multiple;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.gui.DialogEvent;

import java.util.List;

public class DownloadSeasonDialog extends MultipleSeasonsDownloadDialog {
    public DownloadSeasonDialog(final DialogEvent event, final Season season) {
        super(event, List.of(season));
        setTitle(I18N.i18n("download") + " " + season.getName());
    }
}
