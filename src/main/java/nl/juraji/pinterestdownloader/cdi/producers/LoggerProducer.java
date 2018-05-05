package nl.juraji.pinterestdownloader.cdi.producers;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * Created by Juraji on 28-4-2018.
 * Pinterest Downloader
 */
public class LoggerProducer {

    @Produces
    private Logger getLogger(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
}
