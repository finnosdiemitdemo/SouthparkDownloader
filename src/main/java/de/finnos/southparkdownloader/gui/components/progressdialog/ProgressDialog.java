package de.finnos.southparkdownloader.gui.components.progressdialog;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.AbstractProgressDialogEvent;
import de.finnos.southparkdownloader.processes.CancelableProcess;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgressDialog extends JFrame implements WindowListener {
    private DialogEvent dialogEvent;

    private final JPanel contentPanel = new JPanel(new MigLayout("insets 0"));
    private final JTabbedPane tabPane = new JTabbedPane();
    private final List<Tab> tabs = new ArrayList<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ProgressDialog(final DialogEvent dialogEvent) {
        this.dialogEvent = dialogEvent;

        init();
    }

    public ProgressDialog() {
        this(null);
    }

    private void init() {
        final JPanel panel = new JPanel(new MigLayout());

        panel.add(contentPanel, "w 100%,wrap");
        panel.add(tabPane, "h 100%,w 100%");
        tabPane.setBorder(new LineBorder(Color.GRAY));

        setSize(new Dimension(500, 300));
        setContentPane(panel);

        Helper.defaultDialog(this);
    }

    public void addProgressItem(final CancelableProcess process,
                                final String progressName,
                                final ProgressItemPanel progressItemPanel,
                                final AbstractProgressDialogEvent abstractProgressDialogEvent,
                                final boolean removeTabOnFinished,
                                final boolean isTabCloseable) {
        tabs.add(addTab(progressName, progressItemPanel, process, abstractProgressDialogEvent, removeTabOnFinished, isTabCloseable));
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

        int tabIndex = tabPane.getTabCount();

        // Add the tab content
        tabPane.insertTab(name, null, progressItemPanel, null, tabIndex);

        final var titlePanel = new JPanel(new MigLayout("insets 0, gap 10"));
        titlePanel.add(new JLabel(name));
        titlePanel.setBackground(new Color(0, 0, 0, 0));

        // Add the tab caption layout
        tabPane.setTabComponentAt(tabIndex, titlePanel);

        final var tab = new Tab(titlePanel, progressItemPanel);

        if (removeTabOnFinished) {
            process.addFinishedListener(() -> removeTab(tab));
            process.addInterruptedListener((e) -> removeTab(tab));
        }

        if (isTabCloseable) {
            final var closeButton = new JButton(IconFontSwing.buildIcon(FontAwesome.TIMES, 14, Color.WHITE));
            closeButton.addActionListener(e -> {
                if (Helper.showConfirmMessage(progressItemPanel,
                    I18N.i18n("cancel_progress_question.title"),
                    I18N.i18n("cancel_progress_question.message")) == JOptionPane.OK_OPTION) {
                    process.cancel();

                    // Normaerweise werden nach dem cancel Aufruf die Tabs automatisch zu gemacht.
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
        var tabIndex = tabPane.indexOfComponent(tab.tabContentComponent);
        if (tabIndex != -1) {
            tabPane.remove(tabIndex);
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

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        if (dialogEvent != null) {
            dialogEvent.closed();
        }
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
