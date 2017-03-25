package be.medx;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.util.List;
import java.util.Map;

public class HandlerChain
{
    private Map<HandlerPosition, List<SOAPHandler<?>>> registeredHandlers;
    
    public HandlerChain() {
        (this.registeredHandlers = new HashMap<HandlerPosition, List<SOAPHandler<?>>>()).put(HandlerPosition.BEFORE, new ArrayList<SOAPHandler<?>>());
        this.registeredHandlers.put(HandlerPosition.SECURITY, new ArrayList<SOAPHandler<?>>());
        this.registeredHandlers.put(HandlerPosition.AFTER, new ArrayList<SOAPHandler<?>>());
    }
        
    public HandlerChain register(final HandlerPosition position, final SOAPHandler<?> handler) {
        final List<SOAPHandler<?>> resultHandler = this.registeredHandlers.get(position);
        resultHandler.add(handler);
        return this;
    }
    
    public HandlerChain unregisterHandler(final HandlerPosition position, final SOAPHandler<?> handler) {
        final List<SOAPHandler<?>> resultHandler = this.registeredHandlers.get(position);
        resultHandler.remove(handler);
        return this;
    }
    
    public List<SOAPHandler<?>> getHandlers(final HandlerPosition position) {
        return this.registeredHandlers.get(position);
    }
}
