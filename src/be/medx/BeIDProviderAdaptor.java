package be.medx;

import be.fedict.commons.eid.jca.BeIDProvider;
import java.security.Provider;

public class BeIDProviderAdaptor implements ProviderAdaptor
{
    @Override
    public Provider getProvider() {
        return (Provider)new BeIDProvider();
    }
}
