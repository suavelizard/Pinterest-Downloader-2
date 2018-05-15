package nl.juraji.pinterestdownloader.util.workers;

import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import javax.swing.*;

/**
 * Created by Juraji on 30-4-2018.
 * Pinterest Downloader
 */
public abstract class WorkerWithTask<T, V> extends SwingWorker<T, V> {
    private final Task task;

    public WorkerWithTask(Task task) {
        task.reset();
        this.task = task;
    }

    public Task getTask() {
        task.start();
        return task;
    }

    public static class CanceledException extends RuntimeException{
    }
}
