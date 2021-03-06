package nl.juraji.pinterestdownloader.util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Juraji on 27-4-2018.
 * Pinterest Downloader
 */
public final class FormUtils {

    private FormUtils() {
    }

    public static FormLock lockForm(Container form) {
        return new FormLock(form);
    }

    public static final class FormLock {
        private final Container form;
        private final Map<Component, Boolean> componentStates;

        private FormLock(Container form) {
            this.form = form;
            componentStates = new HashMap<>();
            this.disableComponents(this.form);
        }

        public void unlock() {
            this.componentStates.forEach(Component::setEnabled);
        }

        private void disableComponents(Component component) {
            componentStates.put(component, component.isEnabled());
            component.setEnabled(false);
            if (component instanceof Container) {
                Component[] children = ((Container) component).getComponents();
                for (Component child : children) {
                    this.disableComponents(child);
                }
            }
        }
    }
}
