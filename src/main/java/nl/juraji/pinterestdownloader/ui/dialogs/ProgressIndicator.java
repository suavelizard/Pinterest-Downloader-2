package nl.juraji.pinterestdownloader.ui.dialogs;

import nl.juraji.pinterestdownloader.resources.I18n;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.util.FormUtils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressIndicator extends JDialog {

    private final AtomicInteger counter;

    private JPanel contentPane;
    private JLabel actionLabel;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    public ProgressIndicator() {
        super(getFrame(), I18n.get("ui.applicationName"));

        setIconImage(Icons.getApplicationIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(contentPane);
        pack();

        FormUtils.moveToCenterOf(this, getOwner());
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

    private static Frame getFrame() {
        return Frame.getFrames()[0];
    }
}
