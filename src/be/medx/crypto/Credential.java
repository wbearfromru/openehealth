// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.crypto;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;

import be.medx.exceptions.TechnicalConnectorException;

public interface Credential
{
    String getIssuer() throws TechnicalConnectorException;
    
    String getIssuerQualifier() throws TechnicalConnectorException;
    
    PublicKey getPublicKey() throws TechnicalConnectorException;
    
    PrivateKey getPrivateKey() throws TechnicalConnectorException;
    
    X509Certificate getCertificate() throws TechnicalConnectorException;
    
    String getProviderName() throws TechnicalConnectorException;
    
    Certificate[] getCertificateChain() throws TechnicalConnectorException;
    
    KeyStore getKeyStore() throws TechnicalConnectorException;
}
