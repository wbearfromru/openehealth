package be.medx.crypto;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;

import be.fgov.ehealth.etee.crypto.utils.KeyManager;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorIOUtils;

import org.slf4j.Logger;

import java.security.KeyStore;

public class KeyStoreManager
{
    private KeyStoreInfo keyStoreInfo;
    private KeyStore keyStore;
    private static final Logger LOG;
    
    public KeyStoreManager(final KeyStore keyStore) {
        this.keyStore = keyStore;
    }
    
    public KeyStoreManager(final KeyStore keyStore, final KeyStoreInfo keyStoreInfo) {
        this.keyStore = keyStore;
        this.keyStoreInfo = keyStoreInfo;
    }
    
    public KeyStoreManager(final KeyStoreInfo keyStoreInfo) throws TechnicalConnectorException {
        this.keyStoreInfo = keyStoreInfo;
        this.keyStore = this.getKeyStore(keyStoreInfo.getKeystorePath(), keyStoreInfo.getKeystorePassword());
    }
    
    public KeyStoreManager(final String pathKeystore, final char[] keyStorePassword) throws TechnicalConnectorException {
        this.keyStore = this.getKeyStore(pathKeystore, keyStorePassword);
    }
    
    private KeyStore getKeyStore(final String pathKeystore, final char[] keyStorePassword) throws TechnicalConnectorException {
        try {
            if (pathKeystore != null) {
                String keystoreType = "PKCS12";
                if (pathKeystore.toLowerCase().contains(".jks")) {
                    keystoreType = "JKS";
                }
                try {
                    return KeyManager.getKeyStore(ConnectorIOUtils.getResourceAsStream(pathKeystore), keystoreType, keyStorePassword);
                }
                catch (KeyManager.KeyStoreOpeningException e4) {
                    KeyStoreManager.LOG.error("Trying to load keystore with ./");
                    return KeyManager.getKeyStore(ConnectorIOUtils.getResourceAsStream("./" + pathKeystore), keystoreType, keyStorePassword);
                }
            }
            throw new TechnicalConnectorException();
        }
        catch (KeyManager.KeyStoreOpeningException e) {
            throw new TechnicalConnectorException();
        }
        catch (CertificateException e2) {
            throw new TechnicalConnectorException();
        }
        catch (IOException e3) {
            throw new TechnicalConnectorException();
        }
    }
    
    public final KeyStore getKeyStore() {
        return this.keyStore;
    }
    
    public final KeyStoreInfo getKeyStoreInfo() {
        return this.keyStoreInfo;
    }
    
    static {
        LOG = LoggerFactory.getLogger(KeyStoreManager.class);
    }
}
