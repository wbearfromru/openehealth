package be.medx.services;

import be.medx.exceptions.TechnicalConnectorException;

public interface GlobalMedicalFileService {

	public String requestGMDList() throws TechnicalConnectorException;
}
