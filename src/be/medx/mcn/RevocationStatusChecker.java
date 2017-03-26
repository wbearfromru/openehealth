package be.medx.mcn;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.joda.time.DateTime;

public interface RevocationStatusChecker {
	boolean isRevoked(X509Certificate p0) throws CertificateException;

	boolean isRevoked(X509Certificate p0, DateTime p1) throws CertificateException;
}
