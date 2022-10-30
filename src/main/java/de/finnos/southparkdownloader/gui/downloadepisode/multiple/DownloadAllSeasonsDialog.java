package de.finnos.southparkdownloader.gui.downloadepisode.multiple;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.gui.DialogEvent;

import java.util.List;

public class DownloadAllSeasonsDialog extends MultipleSeasonsDownloadDialog {
    public DownloadAllSeasonsDialog(final DialogEvent event, final List<Season> seasons) {
        super(event, seasons);
        setTitle(I18N.i18n("download") + " Southpark");
    }

}
