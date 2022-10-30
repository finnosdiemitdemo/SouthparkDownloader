package de.finnos.southparkdownloader.gui.downloadepisode.single;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ImageLoader;
import de.finnos.southparkdownloader.SouthparkDownloader;
import de.finnos.southparkdownloader.classes.EpisodePart;
import de.finnos.southparkdownloader.classes.EpisodePartStream;
import de.finnos.southparkdownloader.classes.Resolution;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;

public class EpisodePartPanel extends JPanel {
    private final EpisodePart episodePart;
    private final DownloadEpisodeDialog dialog;

    private JComboBox<EpisodePartStream> comboResolutions;
    private JLabel labelIcon;
    private JButton buttonDelete;
    private JButton buttonDownload;

    public EpisodePartPanel(EpisodePart mediaInfo, DownloadEpisodeDialog dialog) {
        this.episodePart = mediaInfo;
        this.dialog = dialog;

        init();
    }

    private void init() {
        setLayout(new MigLayout("insets 5, gap 10"));

        comboResolutions = new JComboBox<>();
        comboResolutions.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (value != null) {
                return new JLabel(value.getResolution().resolutionString());
            }

            return new JLabel("");
        });

        labelIcon = new JLabel();

        buttonDelete = new JButton(I18N.i18n("delete"), ImageLoader.IMAGE_DELETE);
        buttonDelete.addActionListener(e -> {
            if (episodePart.downloaded()) {
                dialog.deletePart(episodePart, this);
                dialog.update();
            }
        });

        buttonDownload = new JButton(I18N.i18n("download"), ImageLoader.IMAGE_DOWNLOAD);
        buttonDownload.addActionListener(e -> {
            if (!episodePart.downloaded()) {
                dialog.downloadPart(episodePart, this);
            }
        });

        final var panelPartName = new JPanel(new MigLayout("insets 0"));
        panelPartName.add(new JLabel(I18N.i18n("episode.part")));
        panelPartName.add(new JLabel(String.valueOf(episodePart.getIndex())));

        final var panelResolution = new JPanel(new MigLayout("insets 0"));
        panelResolution.add(new JLabel(I18N.i18n("episode.part.resolution")));
        panelResolution.add(comboResolutions, "w 100px");

        final var panelStatus = new JPanel(new MigLayout("insets 0"));
        panelStatus.add(new JLabel(I18N.i18n("episode.part.status")));
        panelStatus.add(labelIcon);

        JPanel panelButtons = new JPanel(new MigLayout("insets 0"));
        panelButtons.add(buttonDownload);
        panelButtons.add(buttonDelete);

        add(panelPartName);
        add(panelResolution);
        add(panelStatus);
        add(panelButtons);
        setBorder(BorderFactory.createLineBorder(SouthparkDownloader.getBorderColor()));
        setBackground(UIManager.getColor("componentBackground"));

        final ArrayList<EpisodePartStream> streams = episodePart.getStreams();
        streams.sort(Comparator.comparing(EpisodePartStream::getResolution));

        final Resolution bestResolution = streams.stream()
            .map(EpisodePartStream::getResolution)
            .max(Resolution::compareTo)
            .orElse(null);
        for (EpisodePartStream stream : streams) {
            comboResolutions.addItem(stream);
        }
        comboResolutions.setSelectedItem(bestResolution);

        update(DownloadEpisodeDialog.DownloadAction.NONE);
    }

    public void update(final DownloadEpisodeDialog.DownloadAction downloadAction) {
        final boolean downloaded = episodePart.downloaded();

        labelIcon.setIcon(downloaded ? ImageLoader.IMAGE_CHECK : ImageLoader.IMAGE_CROSS);

        if (downloadAction == DownloadEpisodeDialog.DownloadAction.NONE) {
            Helper.setEnabledWithTooltip(buttonDownload, !downloaded, null);
            Helper.setEnabledWithTooltip(buttonDelete, downloaded, null);
            comboResolutions.setEnabled(!downloaded);
        } else if (downloadAction == DownloadEpisodeDialog.DownloadAction.DOWNLOAD_ALL
            || downloadAction == DownloadEpisodeDialog.DownloadAction.DOWNLOAD_PART) {

            final var message = I18N.i18n("download_episode.tooltip.unable_to_execute_during_download");
            Helper.setEnabledWithTooltip(buttonDownload, false, message);
            Helper.setEnabledWithTooltip(buttonDelete, false, message);
            comboResolutions.setEnabled(false);
        }
    }

    public EpisodePartStream getStream() {
        return (EpisodePartStream) comboResolutions.getSelectedItem();
    }

    public EpisodePart getEpisodePart() {
        return episodePart;
    }
}