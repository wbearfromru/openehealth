// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import javax.xml.ws.handler.Handler;
import java.util.List;

public abstract class GenericFeature
{
    protected boolean enabled;
    
    public abstract String getID();
    
    protected GenericFeature() {
        this.enabled = false;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public abstract List<Handler<?>> getHandlers();
}
