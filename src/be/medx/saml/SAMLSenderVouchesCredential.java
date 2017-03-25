package be.medx.saml;

import org.w3c.dom.Element;

import be.medx.crypto.Credential;

public class SAMLSenderVouchesCredential extends AbstractSAMLToken
{
    public SAMLSenderVouchesCredential(final Element assertion, final Credential credential) {
        super(assertion, credential);
    }
}
