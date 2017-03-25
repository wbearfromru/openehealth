package be.medx;

import java.security.Provider;

public interface ProviderAdaptor
{
    Provider getProvider() throws TechnicalConnectorException;
}
