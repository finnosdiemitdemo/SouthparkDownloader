package de.finnos.southparkdownloader.gui.components.progressdialog;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.Registration;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.AbstractProgressDialogEvent;
import de.finnos.southparkdownloader.processes.CancelableProcess;
import de.finnos.southparkdownloader.processes.ThreadProcess;
import de.finnos.southparkdownloader.services.BaseService;
import de.finnos.southparkdownloader.services.HasServices;
import de.finnos.southparkdownloader.services.ProgressItem;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgressDialog extends JFrame implements HasServices {
    private DialogEvent dialogEvent;

    private final JPanel contentPanel = new JPanel(new MigLayout("insets 0"));
    private final JPanel processesPanel = new JPanel(new MigLayout("insets 0"));
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> processQueueList = new JList<>(listModel);
    private final JTabbedPane processesTabPane = new JTabbedPane();

    private final List<Tab> tabs = new ArrayList<>();
    private final List<BaseService<?, ?>> services = new ArrayList<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private JLabel labelQueue;

    public ProgressDialog(final DialogEvent dialogEvent) {
        this.dialogEvent = dialogEvent;

        init();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                if (dialogEvent != null) {
                    dialogEvent.closed();
                }
            }
        });
    }

    public ProgressDialog() {
        this(null);
    }

    private void init() {
        final JPanel panel = new JPanel(new MigLayout());

        panel.add(contentPanel, "w 100%,wrap");
        panel.add(processesPanel, "h 100%,w 100%");

        setSize(new Dimension(500, 300));
        setContentPane(panel);

        Helper.defaultDialog(this);

        updateQueueAndTabSheet();
    }

    private void updateQueueAndTabSheet () {
        processesPanel.removeAll();
        if (services.isEmpty()) {
            processesPanel.add(processesTabPane, "w 100%, h 100%");
        } else {
            labelQueue = new JLabel(I18N.i18n("queue"));

            final var paneQueue = new JPanel(new MigLayout("insets 0"));
            paneQueue.add(labelQueue, "w 100%, wrap");
            paneQueue.add(new JScrollPane(processQueueList), "w 100%, h 100%");

            final var paneTabSheet= new JPanel(new MigLayout("insets 0"));
            paneTabSheet.add(new JLabel(I18N.i18n("active_processes")), "w 100%, wrap");
            paneTabSheet.add(processesTabPane, "w 100%, h 100%");

            processesPanel.add(paneQueue, "w 220px, h 100%");
            processesPanel.add(paneTabSheet, "w 100%, h 100%");
        }
    }

    public void addProgressItem(final CancelableProcess process,
                                final ProgressItem progressItem,
                                final ProgressItemPanel progressItemPanel,
                                final AbstractProgressDialogEvent abstractProgressDialogEvent,
                                final boolean removeTabOnFinished,
                                final boolean isTabCloseable) {
        addProgressItem(process, progressItem.getName(), progressItemPanel, abstractProgressDialogEvent, removeTabOnFinished, isTabCloseable);
    }

    public void addProgressItem(final CancelableProcess process,
                                final ProgressItem progressItem,
                                final ProgressItemPanel progressItemPanel,
                                final AbstractProgressDialogEvent abstractProgressDialogEvent) {
        addProgressItem(process, progressItem, progressItemPanel, abstractProgressDialogEvent, true, true);
    }

    public void addProgressItem(final CancelableProcess process,
                                final String progressName,
                                final ProgressItemPanel progressItemPanel,
                                final AbstractProgressDialogEvent abstractProgressDialogEvent,
                                final boolean removeTabOnFinished,
                                final boolean isTabCloseable) {
        tabs.add(addTab(progressName, progressItemPanel, process, abstractProgressDialogEvent, removeTabOnFinished, isTabCloseable));
        if (!process.getThread().isAlive()) {
            process.getThread().start();
        }
    }

    public void addProgressItem(final CancelableProcess process,
                                final String progressName,
                                final ProgressItemPanel progressItemPanel,
                                final AbstractProgressDialogEvent abstractProgressDialogEvent) {
        addProgressItem(process, progressName, progressItemPanel, abstractProgressDialogEvent, true, true);
    }

    private Tab addTab(final String name,
                       final ProgressItemPanel progressItemPanel,
                       final CancelableProcess process,
                       final AbstractProgressDialogEvent abstractProgressDialogEvent,
                       final boolean removeTabOnFinished,
                       final boolean isTabCloseable
    ) {
        process.getThread().setProgressEvent(abstractProgressDialogEvent);

        int tabIndex = processesTabPane.getTabCount();

        // Add the tab content
        processesTabPane.insertTab(name, null, progressItemPanel, null, tabIndex);

        final var titlePanel = new JPanel(new MigLayout("insets 0, gap 10"));
        titlePanel.add(new JLabel(name));
        titlePanel.setBackground(new Color(0, 0, 0, 0));

        // Add the tab caption layout
        processesTabPane.setTabComponentAt(tabIndex, titlePanel);

        final var tab = new Tab(titlePanel, progressItemPanel);

        if (removeTabOnFinished) {
            process.addFinishedListener(() -> {
                removeTab(tab);
            });
            process.addInterruptedListener((e) -> {
                // Der Tab soll nur zugemacht werden, wenn dieser aktiv durch den Benutzer geschlossen wurde.
                // Bei allen anderen Exceptions werden diese dann im Tab angezeigt.
                if (e instanceof ThreadProcess.InterruptThreadProcessException) {
                    removeTab(tab);
                }
            });
        }

        if (isTabCloseable) {
            final var closeButton = new JButton(IconFontSwing.buildIcon(FontAwesome.TIMES, 14, Color.WHITE));
            closeButton.addActionListener(e -> {
                if (Helper.showConfirmMessage(progressItemPanel,
                    I18N.i18n("cancel_progress_question.title"),
                    I18N.i18n("cancel_progress_question.message")) == JOptionPane.OK_OPTION) {
                    process.cancel();

                    // Normalerweise werden nach dem cancel Aufruf die Tabs automatisch zu gemacht.
                    // Wenn dies über den removeTabOnFinished Parameter deaktiviert ist, wird der Tab manuelle entfernt
                    if (!removeTabOnFinished) {
                        removeTab(tab);
                    }
                }
            });
            titlePanel.add(closeButton);
        }

        return tab;
    }

    public void removeFinishedTabs() {
        for (final Tab tab : new ArrayList<>(tabs)) {
            removeTab(tab);
        }
    }

    private void removeTab(final Tab tab) {
        lock.writeLock().lock();
        var tabIndex = processesTabPane.indexOfComponent(tab.tabContentComponent);
        if (tabIndex != -1) {
            processesTabPane.remove(tabIndex);
        }
        tabs.remove(tab);
        lock.writeLock().unlock();
    }

    private record Tab(JComponent tabCaptionComponent, ProgressItemPanel tabContentComponent) {
    }

    protected JPanel getContentPanel() {
        return contentPanel;
    }

    public void done() {
        if (dialogEvent != null) {
            dialogEvent.done();
        }
    }

    private void updateQueueItems() {
        listModel.removeAllElements();
        services.stream()
            .flatMap(service -> service.getDownloadItemQueue().stream())
            .map(ProgressItem::getName)
            .forEach(listModel::addElement);
        labelQueue.setText("%s (%d)".formatted(I18N.i18n("queue"), listModel.size()));
    }

    @Override
    public Registration registerService(final BaseService<?, ?> service) {
        services.add(service);
        final var registration = service.addProcessesChangeListener(processChangedEvent -> {
            updateQueueItems();
        });
        updateQueueAndTabSheet();
        return () -> {
            services.remove(service);
            registration.remove();
            updateQueueAndTabSheet();
        };
    }

    @Override
    public List<BaseService<?, ?>> getServices() {
        return services;
    }
}
