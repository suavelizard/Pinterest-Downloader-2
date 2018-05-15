package nl.juraji.pinterestdownloader.ui.dialogs;

import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.ui.components.TasksList;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Juraji on 15-5-2018.
 * Pinterest Downloader
 */
public class Task {

    private final AtomicInteger counter;
    private final TasksList tasksList;

    private JPanel contentPane;
    private JLabel taskLabel;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private boolean started;

    public Task(TasksList tasksList) {
        this.tasksList = tasksList;
        counter = new AtomicInteger();
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    public void setTask(String task) {
        taskLabel.setText(task);
        tasksList.repaint();
    }

    public void setProgressMax(int max) {
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.setMaximum(max);
    }

    public int getProgressMax() {
        return progressBar.getMaximum();
    }

    public void setProgress(int value) {
        counter.set(value);
        updateProgress();
    }

    public int getProgress() {
        return counter.get();
    }

    public void incrementProgress() {
        counter.getAndIncrement();
        updateProgress();
    }

    public void reset() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        counter.set(0);
        progressBar.setMaximum(0);
        progressBar.setValue(0);
    }

    public void start() {
        tasksList.repaintTask(this);
    }

    public void complete() {
        tasksList.removeTask(this);
    }

    private void updateProgress() {
        final int c = counter.get();
        if (c > progressBar.getMaximum()) {
            progressBar.setMaximum(c);
        }

        progressBar.setValue(c);
        progressLabel.setText(I18n.get("ui.task.progressLabel", c, progressBar.getMaximum()));

        tasksList.repaintTask(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(taskLabel, ((Task) o).taskLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskLabel);
    }
}
