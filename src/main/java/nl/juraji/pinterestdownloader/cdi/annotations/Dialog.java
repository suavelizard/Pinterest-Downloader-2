package nl.juraji.pinterestdownloader.cdi.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.swing.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Juraji on 25-4-2018.
 * Pinterest Downloader
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dialog {
    @Nonbinding Class<? extends JDialog> value();
    @Nonbinding boolean resizeToMainWindow() default true;
}
