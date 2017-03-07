package au.gov.dva.sopapi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        // PDFBox library logs too many inconsequential errors.
        if (event.getLevel().isGreaterOrEqual(Level.ERROR)
            && event.getLoggerName().contains("PDCIDFontType2"))
            return FilterReply.DENY;
        else {
            return FilterReply.NEUTRAL;
        }
    }
}
