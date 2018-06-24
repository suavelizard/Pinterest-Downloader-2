package nl.juraji.pinterestdownloader.executors;

import nl.juraji.pinterestdownloader.ui.dialogs.Task;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Juraji on 23-6-2018.
 * Pinterest Downloader
 */
public abstract class TaskExecutor<T> implements Callable<T> {

    private final Task task;
    private final Logger logger;

    public TaskExecutor(Task task) {
        this.task = task;
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public T call() throws Exception {
        task.reset();
        final T result;

        try {
            result = this.execute();
        } finally {
            this.done();
        }

        return result;
    }

    public static  <R> SwingWorker<R, Void> asSwingWorker(TaskExecutor<R> executor) {
        return new SwingWorker<R, Void>() {
            @Override
            protected R doInBackground() throws Exception {
                return executor.call();
            }
        };
    }

    /**
     * Run executor implementation
     *
     * @return T
     */
    protected abstract T execute() throws Exception;

    /**
     * After running execute
     * ALWAYS GETS EXECUTED
     */
    protected void done() {
        // Do nothing by default
    }

    protected Task getTask() {
        return task;
    }

    protected void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    protected void logWarning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    protected void logError(String msg, Throwable thrown) {
        logger.log(Level.SEVERE, msg, thrown);
    }
}
