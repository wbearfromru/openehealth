package be.medx;

import java.util.Random;

public class XSIDGenerator implements IdGenerator
{
    private Random random;
    
    public XSIDGenerator() {
        this.random = new Random();
    }
    
    @Override
    public String generateId() {
        final long now = System.currentTimeMillis();
        final long randomLong = this.random.nextLong();
        return "ID_" + now + "-" + randomLong;
    }
}
