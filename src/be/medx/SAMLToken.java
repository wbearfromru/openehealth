package be.medx;

import org.w3c.dom.Element;

public interface SAMLToken extends ExtendedCredential
{
    Element getAssertion();
    
    String getAssertionID();
    
    void checkValidity() throws TechnicalConnectorException;
}
