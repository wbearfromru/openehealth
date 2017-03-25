package be.medx.services;

import be.medx.crypto.AbstractExtendedCredential;
import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;

public interface CryptoService {
	public void loadCerificate(String certificatePath, String certificatePassword) throws TechnicalConnectorException;

	public Credential getHeaderCredential();

	public AbstractExtendedCredential getHOKCredential();
}
