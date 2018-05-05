package nl.juraji.pinterestdownloader.util;

import java.awt.*;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
public final class FormUtils {

    private FormUtils() {
    }

    public static void moveToCenterOf(Component component, Component target) {
        Point targetLocation = target.getLocation();
        Dimension targetSize = target.getSize();
        Dimension componentSize = component.getSize();
        component.setLocation(
                targetLocation.x + (targetSize.width / 2 - componentSize.width / 2),
                targetLocation.y + (targetSize.height / 2 - componentSize.height / 2)
        );
    }

    public static void moveAbove(Component component, Component target, int marginPx) {
        Point targetLocation = target.getLocation();
        Dimension targetSize = target.getSize();
        Dimension componentSize = component.getSize();
        component.setLocation(
                targetLocation.x + (targetSize.width / 2 - componentSize.width / 2),
                targetLocation.y - (componentSize.height + marginPx)
        );
    }

    public static FormLock lockForm(Container form) {
        return new FormLock(form);
    }

    public static final class FormLock {
        private final Container form;

        private FormLock(Container form) {
            this.form = form;
            this.setEnabled(this.form, false);
        }

        public void unlock() {
            this.setEnabled(form, true);
        }

        private void setEnabled(Component component, boolean enable) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                Component[] children = ((Container) component).getComponents();
                for (Component child : children) {
                    this.setEnabled(child, enable);
                }
            }
        }
    }
}
