package nl.juraji.pinterestdownloader.ui.components;

/**
 * Created by Juraji on 15-5-2018.
 * Pinterest Downloader
 */
@FunctionalInterface
public interface TaskRemovedListener {
    void update(Integer taskCount);
}
