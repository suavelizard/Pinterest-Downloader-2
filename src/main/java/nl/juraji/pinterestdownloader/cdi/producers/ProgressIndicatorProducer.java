package nl.juraji.pinterestdownloader.cdi.producers;

import nl.juraji.pinterestdownloader.resources.Icons;
import nl.juraji.pinterestdownloader.ui.dialogs.ProgressIndicator;
import nl.juraji.pinterestdownloader.util.FormUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.swing.*;
import java.awt.*;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 * <p>
 * Produces a dialog from the class set in the @Dialog annotation.
 * Do not inject Dialogs directly, since they need additional bootstrapping
 */
@Default
@SuppressWarnings("unchecked")
public class ProgressIndicatorProducer {

    @Produces
    public ProgressIndicator produceProgressIndicator() {
        BeanManager beanManager = CDI.current().getBeanManager();
        Frame mainFrame = JFrame.getFrames()[0];

        ProgressIndicator dialog = new ProgressIndicator(mainFrame);
        dialog.setIconImage(Icons.getApplicationIcon());

        FormUtils.moveToCenterOf(dialog, mainFrame);

        CreationalContext<ProgressIndicator> context = beanManager.createCreationalContext(null);
        AnnotatedType<ProgressIndicator> type = beanManager.createAnnotatedType(ProgressIndicator.class);
        InjectionTarget<ProgressIndicator> injectionTarget = beanManager.createInjectionTarget(type);
        injectionTarget.inject(dialog, context);
        injectionTarget.postConstruct(dialog);

        return dialog;
    }
}
