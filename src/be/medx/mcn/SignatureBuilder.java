package be.medx.mcn;

import java.util.Map;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;

public interface SignatureBuilder {
	byte[] sign(Credential p0, byte[] p1) throws TechnicalConnectorException;

	byte[] sign(Credential p0, byte[] p1, Map<String, Object> p2) throws TechnicalConnectorException;

	SignatureVerificationResult verify(byte[] p0, byte[] p1, Map<String, Object> p2) throws TechnicalConnectorException;

	SignatureVerificationResult verify(byte[] p0, Map<String, Object> p1) throws TechnicalConnectorException;

	AdvancedElectronicSignatureEnumeration getSupportedAES();
}
