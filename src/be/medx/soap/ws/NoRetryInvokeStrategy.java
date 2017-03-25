// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.soap.ws;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import be.medx.exceptions.TechnicalConnectorException;

public class NoRetryInvokeStrategy extends AbstractWsSender implements InvokeStrategy
{
    //private static Logger LOG;
    
    @Override
    public GenericResponse invoke(final SOAPMessageContext request, final Handler<?>[] chain) throws TechnicalConnectorException {
        try {
            return AbstractWsSender.call(request, chain);
        }
        catch (Exception e) {
            throw new TechnicalConnectorException();
        }
    }
}
