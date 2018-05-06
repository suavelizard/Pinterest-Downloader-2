package nl.juraji.pinterestdownloader.ui.dialogs;

import nl.juraji.pinterestdownloader.resources.I18n;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressIndicator extends JDialog {

    private final AtomicInteger counter;

    private JPanel contentPane;
    private JLabel actionLabel;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    public ProgressIndicator(Frame owner) {
        super(owner, I18n.get("ui.applicationName"));

        setContentPane(contentPane);
        pack();

        progressBar.setMinimum(0);
        counter = new AtomicInteger();
    }

    public void setTask(String task) {
        setTitle(task);
    }

    public void setAction(String action) {
        actionLabel.setText(action);
    }

    public void setProgressBarMax(int max) {
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.setMaximum(max);
        progressBar.setValue(0);
    }

    public void setProgressBarValue(int value) {
        progressBar.setValue(value);
        updateLabel(value);
    }

    public void incrementProgressBar() {
        progressBar.setValue(counter.getAndIncrement());
        updateLabel(counter.get());
    }

    public void resetProgressBar() {
        counter.set(0);
        progressBar.setValue(0);
    }

    private void updateLabel(int current) {
        progressLabel.setText(I18n.get("ui.progressBar.progressLabel", current, progressBar.getMaximum()));
    }
}
