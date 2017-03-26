package be.medx.mcn;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectorRevocationStatusChecker implements RevocationStatusChecker {
	private static final Logger LOG;
	private RevocationStatusChecker ocsp;
	private RevocationStatusChecker crl;

	public ConnectorRevocationStatusChecker() {
		this.ocsp = new ConnectorOCSPRevocationStatusChecker();
		this.crl = new ConnectorCRLRevocationStatusChecker();
	}

	@Override
	public boolean isRevoked(final X509Certificate cert) throws CertificateException {
		return this.isRevoked(cert, new DateTime());
	}

	@Override
	public boolean isRevoked(final X509Certificate cert, final DateTime validOn) throws CertificateException {
		if (cert == null) {
			throw new CertificateException("X509Certificate is empty.");
		}
		try {
			ConnectorRevocationStatusChecker.LOG.debug("Using ConnectorOCSPRevocationStatusChecker for RevocationCheck");
			return this.ocsp.isRevoked(cert, validOn);
		} catch (CertificateException e) {
			ConnectorRevocationStatusChecker.LOG.debug("Using ConnectorCRLRevocationStatusChecker for RevocationCheck, OCSP failed Reason:[" + e.getMessage() + "];");
			return this.crl.isRevoked(cert);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(ConnectorRevocationStatusChecker.class);
	}
}
