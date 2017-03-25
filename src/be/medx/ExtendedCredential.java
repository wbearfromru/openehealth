// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import java.security.cert.CertPath;
import org.joda.time.DateTime;

public interface ExtendedCredential extends Credential
{
    DateTime getExpirationDateTime() throws TechnicalConnectorException;
    
    CertPath getCertPath() throws TechnicalConnectorException;
}
