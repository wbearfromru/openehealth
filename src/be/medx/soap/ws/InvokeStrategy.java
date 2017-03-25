// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.soap.ws;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import be.medx.exceptions.TechnicalConnectorException;

public interface InvokeStrategy
{
    GenericResponse invoke(SOAPMessageContext p0, Handler<?>[] p1) throws TechnicalConnectorException;
}
