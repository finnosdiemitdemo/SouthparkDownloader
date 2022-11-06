package de.finnos.southparkdownloader.processes;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.services.BaseService;
import de.finnos.southparkdownloader.services.HasServices;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessesHolder {
    private static final Map<JFrame, List<CancelableProcess>> PROCESSES = new HashMap<>();

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (final CancelableProcess process : PROCESSES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())) {
                if (process == null) {
                    continue;
                }
                process.cancel();
            }
        }));
    }

    public static void addProcess(final CancelableProcess process, final JFrame jFrame) {
        if (!PROCESSES.containsKey(jFrame)) {
            PROCESSES.put(jFrame, new ArrayList<>());

            jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            jFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    final var runningProcesses = PROCESSES.containsKey(jFrame)
                        ? PROCESSES.get(jFrame).size()
                        : 0;
                    if (runningProcesses == 0 ||
                        Helper.showConfirmMessage(jFrame, I18N.i18n("process_dialog_close_question.title"),
                            I18N.i18n("process_dialog_close_question.message").formatted(runningProcesses)) == JOptionPane.YES_OPTION) {
                        jFrame.dispose();
                    }
                }

                @Override
                public void windowClosed(final WindowEvent e) {
                    super.windowClosed(e);

                    // Alle Items, die noch in der Warteschlange sind, werden erstmal entfernt,
                    // damit diese beim Beenden der laufenden Prozesse nicht gestartet werden.
                    // Danach werden alle Prozesse beendet
                    if (jFrame instanceof final HasServices hasServices) {
                        hasServices.getServices().forEach(BaseService::stop);
                    }

                    // to avoid ConcurrentModificationException with addInterruptedListener
                    final var cancelableProcesses = PROCESSES.get(jFrame);
                    PROCESSES.remove(jFrame);
                    cancelableProcesses.forEach(CancelableProcess::cancel);
                }
            });
        }
        if (PROCESSES.containsKey(jFrame)) {
            PROCESSES.get(jFrame).add(process);
            process.addFinishedListener(() -> remove(jFrame, process));
            process.addInterruptedListener((e) -> remove(jFrame, process));
        }
    }

    public static void addProcess(final CancelableProcess process) {
        if (FocusManager.getCurrentManager().getActiveWindow() instanceof final JFrame jFrame) {
            addProcess(process, jFrame);
        }
    }

    private static void remove(final JFrame jFrame, final CancelableProcess process) {
        if (PROCESSES.containsKey(jFrame)) {
            PROCESSES.get(jFrame).remove(process);
        }
    }
}
