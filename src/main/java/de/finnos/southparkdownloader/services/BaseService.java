package de.finnos.southparkdownloader.services;

import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.processes.CancelableProcess;
import de.finnos.southparkdownloader.processes.FfmpegProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class BaseService<E extends BaseServiceEvent, PROGRESS_ITEM> {
    protected final ProgressDialog progressDialog;
    protected E serviceEvent;

    protected ArrayList<Process> processes = new ArrayList<>();
    protected List<Consumer<ProcessChangedEvent>> onProcessesChangedListeners = new ArrayList<>();

    protected List<PROGRESS_ITEM> downloadItemQueue = new ArrayList<>();
    protected List<PROGRESS_ITEM> downloadingItems = new ArrayList<>();

    public BaseService(final ProgressDialog progressDialog, E event) {
        this.progressDialog = progressDialog;
        this.serviceEvent = event;

        addProcessesChangeListener((processChangedEvent) -> {
            if (processChangedEvent.type == ProcessChangedEvent.Type.DELETE
                || processChangedEvent.type == ProcessChangedEvent.Type.KILL_ALL) {
                for (int i = processes.size(); i < Config.get().getMaxFfmpegProcesses(); i++) {
                    handleNext();
                }
            }
        });
    }

    public void start(final List<PROGRESS_ITEM> downloadItemQueue, final E event) {
        if (this.downloadItemQueue.isEmpty() && this.downloadingItems.isEmpty()) {
            setServiceEvent(event);
            getServiceEvent().started();
            this.downloadItemQueue = new ArrayList<>(downloadItemQueue);
            downloadingItems.clear();
        } else {
            this.downloadItemQueue.addAll(downloadItemQueue);
        }

        for (int i = processes.size(); i < Config.get().getMaxFfmpegProcesses(); i++) {
            handleNext();
        }
    }

    private void handleNext() {
        if (processes.size() >= Config.get().getMaxFfmpegProcesses()) {
            return;
        }
        final Optional<PROGRESS_ITEM> optItem = downloadItemQueue.stream().findFirst();
        if (optItem.isPresent()) {
            final var item = optItem.get();
            downloadItemQueue.remove(item);
            downloadingItems.add(item);
            final var cancelableProcess = handleItem(item);
            if (cancelableProcess != null) {
                cancelableProcess.addFinishedListener(() -> onProcessDone(item, false, null));
                cancelableProcess.addInterruptedListener((e) -> onProcessDone(item, true, e));
            }
        }
    }

    private void onProcessDone(final PROGRESS_ITEM item, final boolean wasInterrupted, final Throwable throwable) {
        downloadingItems.remove(item);
        if (downloadItemQueue.isEmpty() && downloadingItems.isEmpty()) {
            getServiceEvent().done(wasInterrupted, throwable);
        }
    }

    public abstract CancelableProcess handleItem(final PROGRESS_ITEM item);

    public BaseService(final ProgressDialog progressDialog) {
        this(progressDialog, null);
    }

    public E getServiceEvent() {
        return serviceEvent;
    }

    public void setServiceEvent(E serviceEvent) {
        this.serviceEvent = serviceEvent;
    }

    public void addProcess(final FfmpegProcess ffmpegProcess) {
        final var process = ffmpegProcess.getProcess();
        processes.add(process);

        process.onExit().whenCompleteAsync((process1, throwable) -> removeProcess(ffmpegProcess));
        onProcessesChangedListeners.forEach(booleanConsumer -> booleanConsumer.accept(new ProcessChangedEvent(ProcessChangedEvent.Type.ADD)));
    }

    private void removeProcess(final FfmpegProcess ffmpegProcess) {
        final var process = ffmpegProcess.getProcess();
        processes.remove(process);
        onProcessesChangedListeners.forEach(booleanConsumer -> booleanConsumer.accept(new ProcessChangedEvent(ProcessChangedEvent.Type.DELETE)));
    }

    public void addProcessesChangeListener(final Consumer<ProcessChangedEvent> listener) {
        onProcessesChangedListeners.add(listener);
    }

    public List<PROGRESS_ITEM> getDownloadingItems() {
        return downloadingItems;
    }

    public void stop() {

    }

    public static class ProcessChangedEvent {
        enum Type {
            ADD,
            DELETE,
            KILL_ALL
        }

        private final Type type;

        public ProcessChangedEvent(final Type type) {
            this.type = type;
        }
    }
}
