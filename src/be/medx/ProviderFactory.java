package be.medx;

import org.slf4j.LoggerFactory;
import java.security.AuthProvider;
import org.slf4j.Logger;

public final class ProviderFactory
{
    private static final Logger LOG;
    private static final String PROP_PROVIDER = "provider.class";
    private static final String DEFAULT_PROVIDER;
    
    public static AuthProvider getProvider() throws TechnicalConnectorException {
        final String providerClassName = ProviderFactory.DEFAULT_PROVIDER;
        try {
            final Class<?> provider = Class.forName(providerClassName);
            final Object providerObject = provider.newInstance();
            if (providerObject instanceof ProviderAdaptor) {
                return (AuthProvider)((ProviderAdaptor)providerObject).getProvider();
            }
            final String msg = "Class with name [" + provider + "] is not an instance of RevocationStatusChecker, but an instance of [" + providerObject.getClass() + "]";
            ProviderFactory.LOG.debug(msg);
            throw new TechnicalConnectorException();
        }
        catch (ClassNotFoundException e) {
            ProviderFactory.LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new TechnicalConnectorException();
        }
        catch (InstantiationException e2) {
            ProviderFactory.LOG.error(e2.getClass().getSimpleName() + ": " + e2.getMessage());
            throw new TechnicalConnectorException();
        }
        catch (IllegalAccessException e3) {
            ProviderFactory.LOG.error(e3.getClass().getSimpleName() + ": " + e3.getMessage());
            throw new TechnicalConnectorException();
        }
        catch (SecurityException e4) {
            ProviderFactory.LOG.error(e4.getClass().getSimpleName() + ": " + e4.getMessage());
            throw new TechnicalConnectorException();
        }
        catch (IllegalArgumentException e5) {
            ProviderFactory.LOG.error(e5.getClass().getSimpleName() + ": " + e5.getMessage());
            throw new TechnicalConnectorException();
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(ProviderFactory.class);
        DEFAULT_PROVIDER = BeIDProviderAdaptor.class.getName();
    }
}
