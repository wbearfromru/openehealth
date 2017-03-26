package be.medx.mcn;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRevocationStatusChecker implements RevocationStatusChecker {
	private static final Logger LOG;
	private Map<X509Certificate, Boolean> cache;

	public AbstractRevocationStatusChecker() {
		this.cache = new HashMap<X509Certificate, Boolean>();
	}

	@Override
	public boolean isRevoked(final X509Certificate x509certificate) throws CertificateException {
		return this.isRevoked(x509certificate, new DateTime());
	}

	@Override
	public boolean isRevoked(final X509Certificate cert, final DateTime validOn) throws CertificateException {
		if (cert == null) {
			throw new CertificateException("X509Certificate is empty.");
		}
		if (!this.cache.containsKey(cert)) {
			AbstractRevocationStatusChecker.LOG.info("Checking revocation status for cert from subject : " + cert.getSubjectX500Principal().toString());
			boolean isRevoked = false;
			if (!this.isSelfSigned(cert)) {
				isRevoked = this.delegateRevoke(cert, validOn);
			} else {
				AbstractRevocationStatusChecker.LOG.info("Selfsigned certificate detected, skipping delegateRevoke.");
			}
			this.cache.put(cert, isRevoked);
		}
		return this.cache.get(cert);
	}

	abstract boolean delegateRevoke(final X509Certificate p0, final DateTime p1) throws CertificateException;

	private boolean isSelfSigned(final X509Certificate cert) {
		try {
			cert.verify(cert.getPublicKey());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static {
		LOG = LoggerFactory.getLogger(AbstractRevocationStatusChecker.class);
	}
}
