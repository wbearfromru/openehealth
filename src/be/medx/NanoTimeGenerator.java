package be.medx;

public class NanoTimeGenerator implements IdGenerator
{
    @Override
    public String generateId() {
        final long time = System.nanoTime();
        return Long.toString(time, 36).toUpperCase();
    }
}
