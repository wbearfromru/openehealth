package be.medx;

import org.w3c.dom.Element;

public class SAMLSenderVouchesCredential extends AbstractSAMLToken
{
    public SAMLSenderVouchesCredential(final Element assertion, final Credential credential) {
        super(assertion, credential);
    }
}
