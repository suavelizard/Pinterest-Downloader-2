package nl.juraji.pinterestdownloader.ui.components;

import nl.juraji.pinterestdownloader.ui.components.renderers.TaskRenderer;
import nl.juraji.pinterestdownloader.ui.dialogs.Task;
import nl.juraji.pinterestdownloader.util.ArrayListModel;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Juraji on 15-5-2018.
 * Pinterest Downloader
 */
public class TasksList extends JList<Task> {
    private static final AtomicReference<TasksList> REF = new AtomicReference<>();
    private final ArrayListModel<Task> model = new ArrayListModel<>();

    public TasksList() {
        if (REF.get() != null) {
            throw new RuntimeException("Only one Taskslist may be initialized at any given time");
        }

        setCellRenderer(new TaskRenderer());
        setModel(model);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        REF.set(this);
    }

    public void addTask(Task task) {
        model.add(task);
    }

    public void removeTask(Task task) {
        model.remove(task);
    }

    public static Task newTask() {
        final TasksList tasksList = REF.get();

        if (tasksList == null) {
            throw new RuntimeException("No Taskslist instance available");
        }

        Task task = new Task(tasksList);
        tasksList.addTask(task);
        return task;
    }

    public void repaintTask(Task task) {
        final int i = model.indexOf(task);

        try {
            final Rectangle cellBounds = getCellBounds(i, i);
            repaint(cellBounds);
        } catch (IndexOutOfBoundsException e) {
            repaint();
        }
    }

    public void addListDataListener(ListDataListener l) {
        model.addListDataListener(l);
    }
}
