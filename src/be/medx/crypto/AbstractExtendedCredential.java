package be.medx.crypto;

import java.security.NoSuchProviderException;

import be.fgov.ehealth.etee.crypto.utils.SecurityConfiguration;
import be.medx.exceptions.TechnicalConnectorException;

import org.joda.time.DateTime;

import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Arrays;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;

public abstract class AbstractExtendedCredential implements ExtendedCredential
{
    private static final CertificateFactory CF;
    
    @Override
    public CertPath getCertPath() throws TechnicalConnectorException {
        try {
            return AbstractExtendedCredential.CF.generateCertPath(Arrays.asList(this.getCertificateChain()));
        }
        catch (CertificateException e) {
            throw new TechnicalConnectorException();
        }
    }
    
    @Override
    public DateTime getExpirationDateTime() throws TechnicalConnectorException {
        return new DateTime((Object)this.getCertificate().getNotAfter());
    }
    
    static {
        try {
            SecurityConfiguration.configure();
            CF = CertificateFactory.getInstance("X.509", "BC");
        }
        catch (NoSuchProviderException e) {
            throw new IllegalArgumentException(e);
        }
        catch (CertificateException e2) {
            throw new IllegalArgumentException(e2);
        }
    }
}
