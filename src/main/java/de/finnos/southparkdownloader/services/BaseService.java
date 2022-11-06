package de.finnos.southparkdownloader.services;

import de.finnos.southparkdownloader.Registration;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.processes.CancelableProcess;

import java.util.*;
import java.util.function.Consumer;

public abstract class BaseService<E extends BaseServiceEvent, PROGRESS_ITEM extends ProgressItem> {
    protected final ProgressDialog progressDialog;
    protected E serviceEvent;

    protected List<Consumer<ProcessChangedEvent>> onProcessesChangedListeners = new ArrayList<>();

    protected List<PROGRESS_ITEM> downloadItemQueue = new ArrayList<>();
    protected Map<PROGRESS_ITEM, CancelableProcess> downloadingItems = new HashMap<>();

    public BaseService(final ProgressDialog progressDialog, E event) {
        this.progressDialog = progressDialog;
        this.serviceEvent = event;

        addProcessesChangeListener((processChangedEvent) -> {
            if (processChangedEvent.type == ProcessChangedEvent.Type.DELETE
                || processChangedEvent.type == ProcessChangedEvent.Type.KILL_ALL) {
                for (int i = downloadingItems.size(); i < Config.get().getMaxFfmpegProcesses(); i++) {
                    handleNext();
                }
            }
        });

        progressDialog.registerService(this);
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

        for (int i = downloadingItems.size(); i < Config.get().getMaxFfmpegProcesses(); i++) {
            handleNext();
        }
    }

    public void stop() {
        this.downloadItemQueue.clear();
        downloadingItems.forEach((progress_item, cancelableProcess) -> cancelableProcess.cancel());
        downloadingItems.clear();
    }

    private void handleNext() {
        if (downloadingItems.size() >= Config.get().getMaxFfmpegProcesses()) {
            return;
        }
        final Optional<PROGRESS_ITEM> optItem = downloadItemQueue.stream().findFirst();
        if (optItem.isPresent()) {
            final var item = optItem.get();
            downloadItemQueue.remove(item);
            final var cancelableProcess = handleItem(item);
            downloadingItems.put(item, cancelableProcess);
            onProcessesChangedListeners.forEach(booleanConsumer -> booleanConsumer.accept(new ProcessChangedEvent(ProcessChangedEvent.Type.ADD)));
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
        onProcessesChangedListeners.forEach(booleanConsumer -> booleanConsumer.accept(new ProcessChangedEvent(ProcessChangedEvent.Type.DELETE)));
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

    public Registration addProcessesChangeListener(final Consumer<ProcessChangedEvent> listener) {
        onProcessesChangedListeners.add(listener);
        return () -> onProcessesChangedListeners.remove(listener);
    }

    public Map<PROGRESS_ITEM, CancelableProcess> getDownloadingItems() {
        return downloadingItems;
    }

    public List<PROGRESS_ITEM> getDownloadItemQueue() {
        return Collections.unmodifiableList(downloadItemQueue);
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
