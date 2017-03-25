package be.medx;

import org.joda.time.DateTime;

public class DateTimeIdGenerator implements IdGenerator
{
    @Override
    public String generateId() {
        final DateTime currentDateTime = new DateTime();
        return currentDateTime.toString("yyyyMMddHHmmss");
    }
}
