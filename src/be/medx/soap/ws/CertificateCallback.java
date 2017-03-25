package be.medx.soap.ws;

import org.slf4j.LoggerFactory;
import org.apache.ws.security.WSSecurityException;

import java.util.concurrent.TimeUnit;
import java.io.IOException;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;

import be.medx.crypto.Credential;
import be.medx.crypto.KeyPairCredential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.soap.handlers.AbstractWsSecurityHandler;

public class CertificateCallback extends AbstractWsSecurityHandler
{
    private static final Logger LOG;
    private Credential cred;
    
    public CertificateCallback() throws TechnicalConnectorException {
    }
    
    public CertificateCallback(final X509Certificate certificate, final PrivateKey privateKey) throws TechnicalConnectorException {
        this.cred = new KeyPairCredential(privateKey, certificate);
    }
    
    public CertificateCallback(final Credential cred) throws TechnicalConnectorException {
        this.cred = cred;
    }
    
    @Override
    protected void addWSSecurity(final SOAPMessageContext context) throws IOException, WSSecurityException, TechnicalConnectorException {
        Credential lazyCred = this.cred;
        if (lazyCred == null) {
           throw new IOException("Unable to lazy load credential.");
        }
        this.buildSignature().on(context.getMessage()).withTimeStamp(60L, TimeUnit.SECONDS).withBinarySecurityToken(lazyCred).sign(SignedParts.BODY, SignedParts.TIMESTAMP, SignedParts.BST);
    }
    
    @Override
    protected Logger getLogger() {
        return CertificateCallback.LOG;
    }
    
    static {
        LOG = LoggerFactory.getLogger(CertificateCallback.class);
    }
}
