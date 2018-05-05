package nl.juraji.pinterestdownloader.cdi.producers;

import nl.juraji.pinterestdownloader.cdi.annotations.Dialog;
import nl.juraji.pinterestdownloader.cdi.annotations.Indicator;
import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.FormUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 * <p>
 * Produces a dialog from the class set in the @Dialog annotation.
 * Do not inject Dialogs directly, since they need additional bootstrapping
 */
@Default
@SuppressWarnings("unchecked")
public class DialogProducer {

    public static final String MAIN_FRAME_NAME = "MASTER_FRAME";

    @Produces
    @Indicator
    public ProgressIndicator produceProgressIndicator() throws ReflectiveOperationException {
        ProgressIndicator indicator = createDialogInstance(ProgressIndicator.class, false);
        indicator.setAlwaysOnTop(true);
        return indicator;
    }

    @Produces
    @Dialog(JDialog.class)
    public JDialog produceDialog(InjectionPoint injectionPoint) throws ReflectiveOperationException {
        Dialog annotation = injectionPoint.getAnnotated().getAnnotation(Dialog.class);
        Class<JDialog> dialogClass = (Class<JDialog>) annotation.value();
        return createDialogInstance(dialogClass, annotation.resizeToMainWindow());
    }

    private <T extends JDialog> T createDialogInstance(Class<T> dialogClass, boolean resizeToMainWindow) throws ReflectiveOperationException {
        BeanManager beanManager = CDI.current().getBeanManager();
        Frame owner = getMainWindowFrame();
        Constructor<T> constructor = dialogClass.getConstructor(Frame.class);

        T dialog = constructor.newInstance(owner);
        dialog.setIconImage(Icons.getApplicationIcon());

        if (resizeToMainWindow) {
            dialog.setSize(owner.getSize());
        }

        FormUtils.moveToCenterOf(dialog, owner);

        CreationalContext<T> context = beanManager.createCreationalContext(null);
        AnnotatedType<T> type = beanManager.createAnnotatedType(dialogClass);
        InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(type);
        injectionTarget.inject(dialog, context);
        injectionTarget.postConstruct(dialog);

        return dialog;
    }

    private Frame getMainWindowFrame() {
        return Arrays.stream(JFrame.getFrames())
                .filter(frame -> MAIN_FRAME_NAME.equals(frame.getName()))
                .findFirst()
                .orElse(null);
    }
}
