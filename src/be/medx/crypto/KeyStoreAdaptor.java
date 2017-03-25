// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.crypto;

import java.security.KeyStoreException;
import java.security.KeyStore;

import be.medx.exceptions.TechnicalConnectorException;

public interface KeyStoreAdaptor
{
    KeyStore getKeyStore() throws KeyStoreException, TechnicalConnectorException;
}
