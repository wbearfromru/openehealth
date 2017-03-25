package be.medx;

import org.w3c.dom.Element;

public class SAMLHolderOfKeyToken extends AbstractSAMLToken
{
    public SAMLHolderOfKeyToken(final Element assertion, final Credential credential) {
        super(assertion, credential);
    }
}
