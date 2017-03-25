package be.medx;

import org.slf4j.LoggerFactory;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.lang.ArrayUtils;

import java.security.KeyStore;

import org.slf4j.Logger;

public class KeyStoreCredential extends AbstractExtendedCredential
{
    private static final Logger LOG;
    private KeyStore keystore;
    private String alias;
    private char[] pwd;
    
    public KeyStoreCredential(final KeyStore keystore, final String alias, final String password) throws TechnicalConnectorException {
        this.pwd = ((password == null) ? ArrayUtils.EMPTY_CHAR_ARRAY : password.toCharArray());
        this.alias = alias;
        this.keystore = keystore;
    }
    
    public KeyStoreCredential(final KeyStoreInfo keyStoreInfo) throws TechnicalConnectorException {
        final KeyStoreManager keyStoreManager = new KeyStoreManager(keyStoreInfo);
        this.keystore = keyStoreManager.getKeyStore();
        this.alias = keyStoreManager.getKeyStoreInfo().getAlias();
        this.pwd = keyStoreManager.getKeyStoreInfo().getPrivateKeyPassword();
    }
    
    public KeyStoreCredential(final String keystorePath, final String alias, final String password) throws TechnicalConnectorException {
        this(new KeyStoreInfo(keystorePath, password.toCharArray(), alias, password.toCharArray()));
    }
    
    public KeyStoreCredential(final String keystorePath, final String pwdKeystore, final String privateKeyAlias, final String pwdPrivateKey) throws TechnicalConnectorException {
        this(new KeyStoreInfo(keystorePath, pwdKeystore.toCharArray(), privateKeyAlias, pwdPrivateKey.toCharArray()));
    }
    
    @Override
    public String getIssuer() {
        return this.getCertificate().getSubjectX500Principal().getName("RFC1779");
    }
    
    @Override
    public String getIssuerQualifier() {
        return this.getCertificate().getIssuerX500Principal().getName("RFC1779");
    }
    
    @Override
    public PublicKey getPublicKey() {
        return this.getCertificate().getPublicKey();
    }
    
    @Override
    public PrivateKey getPrivateKey() {
        try {
            return (PrivateKey)this.keystore.getKey(this.alias, this.pwd);
        }
        catch (UnrecoverableKeyException e) {
            KeyStoreCredential.LOG.error(e.getMessage(), e.getCause());
            return null;
        }
        catch (KeyStoreException e2) {
            KeyStoreCredential.LOG.error(e2.getMessage(), e2.getCause());
            return null;
        }
        catch (NoSuchAlgorithmException e3) {
            KeyStoreCredential.LOG.error(e3.getMessage(), e3.getCause());
            return null;
        }
    }
    
    @Override
    public X509Certificate getCertificate() {
        try {
            return (X509Certificate)this.keystore.getCertificate(this.alias);
        }
        catch (KeyStoreException e) {
            KeyStoreCredential.LOG.error(e.getMessage(), e.getCause());
            return null;
        }
    }
    
    @Override
    public String getProviderName() {
        return this.keystore.getProvider().getName();
    }
    
    @Override
    public Certificate[] getCertificateChain() {
        try {
            return this.keystore.getCertificateChain(this.alias);
        }
        catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public KeyStore getKeyStore() throws TechnicalConnectorException {
        return this.keystore;
    }
    
    static {
        LOG = LoggerFactory.getLogger(KeyStoreCredential.class);
    }
}
