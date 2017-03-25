// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.soap.ws;

import java.util.ArrayList;

import javax.xml.ws.handler.Handler;

import java.util.List;

public class XOPFeature extends GenericFeature
{
    public static final String ID = "http://www.w3.org/2004/08/soap/features/http-optimization";
    private int threshold;
    
    public XOPFeature() {
        this.enabled = true;
        this.threshold = 10;
    }
    
    public XOPFeature(final int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be >= 0, actual value: " + threshold);
        }
        this.enabled = true;
        this.threshold = threshold;
    }
    
    public int getThreshold() {
        return this.threshold;
    }
    
    @Override
    public String getID() {
        return "http://www.w3.org/2004/08/soap/features/http-optimization";
    }
    
    @Override
    public List<Handler<?>> getHandlers() {
        return new ArrayList<Handler<?>>();
    }
}
