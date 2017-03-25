// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import java.security.KeyStoreException;
import java.security.KeyStore;

public interface KeyStoreAdaptor
{
    KeyStore getKeyStore() throws KeyStoreException, TechnicalConnectorException;
}
