package be.medx.mcn;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;

import org.joda.time.DateTime;

import be.medx.exceptions.TechnicalConnectorException;

public interface CertificateChecker {
	boolean isCertificateRevoked(File p0) throws TechnicalConnectorException;

	boolean isCertificateRevoked(X509Certificate p0) throws TechnicalConnectorException;

	boolean isCertificateRevoked(File p0, DateTime p1) throws TechnicalConnectorException;

	boolean isCertificateRevoked(X509Certificate p0, DateTime p1) throws TechnicalConnectorException;

	boolean isValidCertificateChain(List<X509Certificate> p0) throws TechnicalConnectorException;
}
