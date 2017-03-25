package be.medx;

import java.util.Date;
import java.util.Calendar;
import org.joda.time.ReadableInstant;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.DateTime;

public final class DateUtils
{
    private DateUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static DateTime parseDateTime(final String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return ISODateTimeFormat.dateTimeParser().parseDateTime(text);
    }
    
    public static String printDateTime(final DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ISODateTimeFormat.dateTime().print((ReadableInstant)dateTime);
    }
    
    public static DateTime parseTime(final String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return ISODateTimeFormat.timeParser().parseDateTime(text);
    }
    
    public static String printTime(final DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ISODateTimeFormat.time().print((ReadableInstant)dateTime);
    }
    
    public static DateTime parseDate(final String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return ISODateTimeFormat.dateParser().parseDateTime(text);
    }
    
    public static String printDate(final DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ISODateTimeFormat.date().print((ReadableInstant)dateTime);
    }
    
    public static DateTime convert(final Calendar cal) {
        if (cal == null) {
            return null;
        }
        return new DateTime((Object)cal);
    }
    
    public static DateTime convert(final Date date) {
        if (date == null) {
            return null;
        }
        return new DateTime((Object)date);
    }
}
