package be.medx.saml;

import org.w3c.dom.Element;

import be.medx.crypto.ExtendedCredential;
import be.medx.exceptions.TechnicalConnectorException;

public interface SAMLToken extends ExtendedCredential
{
    Element getAssertion();
    
    String getAssertionID();
    
    void checkValidity() throws TechnicalConnectorException;
}
