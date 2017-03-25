// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;
import org.slf4j.Logger;

public abstract class AbstractSAMLToken extends AbstractExtendedCredential implements SAMLToken
{
    private static final Logger LOG;
    private final Credential credential;
    private final Element assertion;
    
    public AbstractSAMLToken(final Element assertion, final Credential credential) {
        Validate.notNull((Object)assertion);
        Validate.notNull((Object)credential);
        this.assertion = assertion;
        this.credential = credential;
    }
    
    @Override
    public String getIssuer() throws TechnicalConnectorException {
        return this.credential.getIssuer();
    }
    
    @Override
    public String getIssuerQualifier() throws TechnicalConnectorException {
        return this.credential.getIssuerQualifier();
    }
    
    @Override
    public PublicKey getPublicKey() throws TechnicalConnectorException {
        return this.credential.getPublicKey();
    }
    
    @Override
    public PrivateKey getPrivateKey() throws TechnicalConnectorException {
        return this.credential.getPrivateKey();
    }
    
    @Override
    public X509Certificate getCertificate() throws TechnicalConnectorException {
        return this.credential.getCertificate();
    }
    
    @Override
    public Element getAssertion() {
        return this.assertion;
    }
    
    @Override
    public String getProviderName() {
        try {
            return this.credential.getProviderName();
        }
        catch (TechnicalConnectorException e) {
            AbstractSAMLToken.LOG.error(e.getClass().getSimpleName() + ":" + e.getMessage(), (Throwable)e);
            return "";
        }
    }
    
    @Override
    public Certificate[] getCertificateChain() throws TechnicalConnectorException {
        return this.credential.getCertificateChain();
    }
    
    @Override
    public KeyStore getKeyStore() throws TechnicalConnectorException {
        return this.credential.getKeyStore();
    }
    
    @Override
    public String getAssertionID() {
        return this.assertion.getAttribute("AssertionID");
    }
    
    @Override
    public void checkValidity() throws TechnicalConnectorException {
        final DateTime calendar = SAMLHelper.getNotOnOrAfterCondition(this.assertion);
        if (calendar.isBeforeNow()) {
            throw new TechnicalConnectorException();
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(SAMLHolderOfKeyToken.class);
    }
}
