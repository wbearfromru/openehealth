// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import be.medx.TechnicalConnectorException;

public interface GenericWsSender
{
    GenericResponse send(GenericRequest p0) throws TechnicalConnectorException;
}
