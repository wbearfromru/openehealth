// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.crypto;

import java.security.cert.CertPath;

import org.joda.time.DateTime;

import be.medx.exceptions.TechnicalConnectorException;

public interface ExtendedCredential extends Credential
{
    DateTime getExpirationDateTime() throws TechnicalConnectorException;
    
    CertPath getCertPath() throws TechnicalConnectorException;
}
