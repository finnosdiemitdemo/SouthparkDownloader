package de.finnos.southparkdownloader.gui.pages.main;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ImageLoader;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.data.Setup;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.downloadepisode.multiple.DownloadAllSeasonsDialog;
import de.finnos.southparkdownloader.gui.downloadepisode.multiple.DownloadSeasonDialog;
import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadEpisodeDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SeasonsTree extends JTree {
    private DefaultMutableTreeNode treeRoot;
    private List<Season> seasons;

    public void init () {
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
            final JPanel  result = new JPanel(new MigLayout("insets 5 5 5 0"));
            result.setBackground(new Color(0,0,0,0));

            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject != null) {
                String label = null;
                Icon image = null;
                if (userObject instanceof final Season season) {
                    final long downloadedEpisodes = season.getEpisodes().stream().filter(Episode::downloaded).count();
                    final boolean downloaded = downloadedEpisodes == season.getEpisodes().size();

                    label = String.format("%s", season.getName());
                    image = downloaded ? ImageLoader.IMAGE_CHECK : ImageLoader.IMAGE_CROSS;
                } else if (userObject instanceof final Episode episode) {
                    final boolean downloaded = episode.downloaded();

                    label = String.format("%s %d %s", I18N.i18n("episode"), episode.getNumber(), episode.getName());
                    image = downloaded ? ImageLoader.IMAGE_CHECK : ImageLoader.IMAGE_CROSS;
                }

                if (label != null) {
                    result.add(new JLabel(label));
                }

                if (image != null) {
                    result.add(new JLabel(image));
                }
            }

            if (result.getComponents().length == 0) {
                result.add(new JLabel(value.toString()));
            }

            return result;
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    setSelectionRow(row);

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                    if (node != null) {
                        final Object object = node.getUserObject();

                        if (object instanceof Episode || object instanceof Season || node == treeRoot) {
                            JMenuItem itemDownload = new JMenuItem(I18N.i18n("seasons.open_manage"), ImageLoader.IMAGE_DOWNLOAD);
                            JMenuItem itemOpenFolder = new JMenuItem(I18N.i18n("open_folder"), ImageLoader.IMAGE_OPEN_FOLDER);

                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(itemDownload);
                            popupMenu.add(itemOpenFolder);

                            if (object instanceof final Episode episode) {
                                JMenuItem itemPlay = new JMenuItem(I18N.i18n("play"), ImageLoader.IMAGE_PLAY);
                                popupMenu.add(itemPlay);

                                itemDownload.addActionListener(e1 -> {
                                    new DownloadEpisodeDialog(episode, new DialogEvent() {
                                        @Override
                                        public void closed() {
                                            update();
                                        }

                                        @Override
                                        public void done() {
                                            update();
                                        }
                                    });
                                });
                                itemPlay.addActionListener(e1 -> episode.play());
                                itemOpenFolder.addActionListener(e1 -> episode.openFolder());

                                itemPlay.setEnabled(episode.canPlay());
                                itemOpenFolder.setEnabled(episode.canOpenFolder());
                            } else if (object instanceof final Season season) {
                                itemDownload.addActionListener(e1 -> new DownloadSeasonDialog(null, season));
                                itemOpenFolder.addActionListener(e1 -> season.openFolder());
                                itemOpenFolder.setEnabled(season.canOpenFolder());
                            } else if (node == treeRoot) {
                                itemDownload.addActionListener(e1 -> new DownloadAllSeasonsDialog(null, seasons));
                                itemOpenFolder.addActionListener(e1 -> Helper.openFileWithSystemDefaultProgram(Setup.EPISODES_FOLDER_PATH));
                            }

                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }

    public void update() {
        fillTree();
    }

    private void fillTree () {
        seasons = DownloadDatabase.get().getSeasons();
        treeRoot = new DefaultMutableTreeNode(I18N.i18n("seasons"));
        for (Season season : seasons) {
            // Weil die Pfade abhängig von der Sprache anders sind, hier die Pfade nochmal laden
            season.setPath();

            DefaultMutableTreeNode nodeSeason = new DefaultMutableTreeNode(season);

            for (Episode episode : season.getEpisodes()) {
                DefaultMutableTreeNode nodeEpisode = new DefaultMutableTreeNode(episode);
                nodeSeason.add(nodeEpisode);
            }

            treeRoot.add(nodeSeason);
        }
        setModel(new DefaultTreeModel(treeRoot));
        expandRow(0);
    }
}
