// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.soap.ws;

import be.medx.exceptions.TechnicalConnectorException;

public interface GenericWsSender
{
    GenericResponse send(GenericRequest p0) throws TechnicalConnectorException;
}
