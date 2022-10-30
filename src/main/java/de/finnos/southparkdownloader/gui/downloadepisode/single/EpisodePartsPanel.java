package de.finnos.southparkdownloader.gui.downloadepisode.single;

import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.EpisodePart;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;

public class EpisodePartsPanel extends JPanel {
    private final Episode episode;
    private final DownloadEpisodeDialog dialog;

    private ArrayList<EpisodePartPanel> partPanels;

    public EpisodePartsPanel(Episode episode, DownloadEpisodeDialog dialog) {
        this.episode = episode;
        this.dialog = dialog;

        partPanels = new ArrayList<>();

        init();
    }

    private void init () {
        setLayout(new MigLayout("insets 0"));

        for (EpisodePart mediaInfo : episode.getParts()) {
            final EpisodePartPanel panelPart = new EpisodePartPanel(mediaInfo, dialog);
            partPanels.add(panelPart);
            add(panelPart, "wrap, w 100%");
        }
    }

    public ArrayList<EpisodePartPanel> getPanels () {
        return partPanels;
    }
}
