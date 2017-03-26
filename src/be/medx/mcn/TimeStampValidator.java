package be.medx.mcn;

import org.bouncycastle.tsp.TimeStampToken;

import be.medx.exceptions.TechnicalConnectorException;

public interface TimeStampValidator {
	public static final String KEYSTORE = "timestampvalidatior.keystore";

	void validateTimeStampToken(TimeStampToken p0) throws TechnicalConnectorException;

	void validateTimeStampToken(byte[] p0, TimeStampToken p1) throws TechnicalConnectorException;

}
