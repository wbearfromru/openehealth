package be.medx.soap.handlers;

import java.util.concurrent.TimeUnit;

public class Duration
{
    private long duration;
    private TimeUnit sourceUnit;
    
    public Duration(final long duration, final TimeUnit sourceUnit) {
        this.duration = duration;
        this.sourceUnit = sourceUnit;
    }
    
    public long convert(final TimeUnit targetUnit) {
        return targetUnit.convert(this.duration, this.sourceUnit);
    }
}
