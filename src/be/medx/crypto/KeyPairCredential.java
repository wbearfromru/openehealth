// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.crypto;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;

import be.medx.exceptions.TechnicalConnectorException;

public class KeyPairCredential extends AbstractExtendedCredential
{
    private PrivateKey privateKey;
    private X509Certificate certificate;
    
    public KeyPairCredential(final PrivateKey privateKey, final X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }
    
    @Override
    public String getIssuer() {
        return this.certificate.getSubjectX500Principal().getName("RFC1779");
    }
    
    @Override
    public String getIssuerQualifier() {
        return this.certificate.getIssuerX500Principal().getName("RFC1779");
    }
    
    @Override
    public PublicKey getPublicKey() {
        return this.certificate.getPublicKey();
    }
    
    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }
    
    @Override
    public X509Certificate getCertificate() {
        return this.certificate;
    }
    
    @Override
    public String getProviderName() {
        throw new UnsupportedOperationException("getProviderName is not supported.");
    }
    
    @Override
    public Certificate[] getCertificateChain() {
        return new Certificate[] { this.certificate };
    }
    
    @Override
    public KeyStore getKeyStore() throws TechnicalConnectorException {
        throw new UnsupportedOperationException("getKeyStore is not supported.");
    }
}
