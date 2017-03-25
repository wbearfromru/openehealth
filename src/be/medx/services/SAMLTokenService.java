package be.medx.services;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;

public interface SAMLTokenService {
	public SAMLToken getSAMLToken() throws TechnicalConnectorException;
}
