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
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class SeasonsTree extends JTree {
    private DefaultMutableTreeNode treeRoot;
    private List<Season> seasons;

    public SeasonsTree() {
        init();
    }

    private void init() {
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
            if (value instanceof final DefaultMutableTreeNode node) {
                return createTreeItemComponent(node.getUserObject());
            }
            return new JLabel(value.toString());
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    setSelectionRow(row);

                    if (getLastSelectedPathComponent() instanceof final DefaultMutableTreeNode node) {
                        final var contextMenu = createContextMenu(node);
                        if (contextMenu != null) {
                            contextMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2
                    && getLastSelectedPathComponent() instanceof final DefaultMutableTreeNode node
                    && node.getUserObject() instanceof final Episode episode) {
                    openEpisodeDownloadDialog(episode);
                }
            }
        });
    }

    private JComponent createTreeItemComponent(final Object treeItem) {
        final JPanel result = new JPanel(new MigLayout("insets 5 5 5 0"));
        result.setBackground(new Color(0, 0, 0, 0));

        if (treeItem instanceof Season || treeItem instanceof Episode) {
            final String label;
            final Icon image;
            if (treeItem instanceof final Season season) {
                final long downloadedEpisodes = season.getEpisodes().stream().filter(Episode::downloaded).count();
                final boolean downloaded = downloadedEpisodes == season.getEpisodes().size();

                label = String.format("%s", season.getName());
                image = downloaded ? ImageLoader.IMAGE_CHECK : ImageLoader.IMAGE_CROSS;
            } else {
                Episode episode = (Episode) treeItem;
                final boolean downloaded = episode.downloaded();

                label = String.format("%s %d %s", I18N.i18n("episode"), episode.getNumber(), episode.getName());
                image = downloaded ? ImageLoader.IMAGE_CHECK : ImageLoader.IMAGE_CROSS;
            }

            result.add(new JLabel(label));
            result.add(new JLabel(image));
        } else {
            result.add(new JLabel(treeItem.toString()));
        }

        return result;
    }

    private JPopupMenu createContextMenu(final DefaultMutableTreeNode treeNode) {
        final var downloadDialogEvent = new DialogEvent() {
            @Override
            public void closed() {
                update();
            }

            @Override
            public void done() {
                update();
            }
        };

        final var treeItem = treeNode.getUserObject();

        JPopupMenu popupMenu = null;
        if (treeItem instanceof Episode || treeItem instanceof Season || treeNode == treeRoot) {
            final JMenuItem itemDownload = new JMenuItem(I18N.i18n("seasons.open_manage"), ImageLoader.IMAGE_DOWNLOAD);
            final JMenuItem itemOpenFolder = new JMenuItem(I18N.i18n("open_folder"), ImageLoader.IMAGE_OPEN_FOLDER);
            final JMenuItem itemDelete = new JMenuItem(I18N.i18n("delete"), ImageLoader.IMAGE_DELETE);

            popupMenu = new JPopupMenu();
            popupMenu.add(itemDownload);
            popupMenu.add(itemOpenFolder);
            popupMenu.add(itemDelete);

            if (treeItem instanceof final Episode episode) {
                final JMenuItem itemPlay = new JMenuItem(I18N.i18n("play"), ImageLoader.IMAGE_PLAY);
                popupMenu.add(itemPlay);
                itemPlay.setEnabled(episode.canPlay());
                itemPlay.addActionListener(e1 -> episode.play());

                itemDownload.addActionListener(e1 -> openEpisodeDownloadDialog(episode));
                itemOpenFolder.addActionListener(e1 -> episode.openFolder());
                itemOpenFolder.setEnabled(episode.canOpenFolder());
                itemDelete.setEnabled(episode.canDelete());
                itemDelete.addActionListener(e -> episode.delete());
            } else if (treeItem instanceof final Season season) {
                itemDownload.addActionListener(e1 -> new DownloadSeasonDialog(downloadDialogEvent, season));
                itemOpenFolder.addActionListener(e1 -> season.openFolder());
                itemOpenFolder.setEnabled(season.canOpenFolder());
                itemDelete.setEnabled(season.canDelete());
                itemDelete.addActionListener(e -> season.delete());
            } else if (treeNode == treeRoot) {
                itemDownload.addActionListener(e1 -> new DownloadAllSeasonsDialog(downloadDialogEvent, seasons));
                itemOpenFolder.addActionListener(e1 -> Helper.openFileWithSystemDefaultProgram(Setup.getDownloadFolder()));
                itemOpenFolder.setEnabled(Setup.downloadFolderExists());
                itemDelete.addActionListener(e -> Setup.deleteDownloadFolder());
                itemDelete.setEnabled(Setup.downloadFolderExists());
            }
        }

        return popupMenu;
    }

    private void openEpisodeDownloadDialog(final Episode episode) {
        final var downloadDialogEvent = new DialogEvent() {
            @Override
            public void closed() {
                update();
            }

            @Override
            public void done() {
                update();
            }
        };
        new DownloadEpisodeDialog(episode, downloadDialogEvent);
    }

    public void update() {
        seasons = DownloadDatabase.get().getSeasons();
        fillTree(seasons);
    }

    private void fillTree(final List<Season> seasons) {
        setEnabled(false);
        treeRoot = new DefaultMutableTreeNode(I18N.i18n("seasons"));
        setModel(new DefaultTreeModel(treeRoot));
        for (Season season : seasons) {
            // Weil die Pfade abhängig von der Sprache anders sind, hier die Pfade nochmal laden
            season.setPath();

            final DefaultMutableTreeNode nodeSeason = new DefaultMutableTreeNode(season);
            for (Episode episode : season.getEpisodes()) {
                nodeSeason.add(new DefaultMutableTreeNode(episode));
            }

            treeRoot.add(nodeSeason);
        }
        expandRow(0);
        setEnabled(true);
    }

    private void expandAllNodes(int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            expandRow(i);
        }

        if (getRowCount() != rowCount) {
            expandAllNodes(rowCount, getRowCount());
        }
    }

    public void search(final String searchString) {
        if (searchString.isBlank()) {
            fillTree(seasons);
        } else {
            final List<Season> copySeasons = seasons.stream()
                .map(SerializationUtils::clone)
                .collect(Collectors.toList());
            for (final Season season : copySeasons) {
                season.getEpisodes().removeIf(episode -> !episode.getName().toLowerCase().contains(searchString.toLowerCase())
                    && !episode.getDescription().toLowerCase().contains(searchString.toLowerCase()));
            }
            copySeasons.removeIf(season -> season.getEpisodes().isEmpty());
            fillTree(copySeasons);
            expandAllNodes(0, getRowCount());
        }
    }
}
