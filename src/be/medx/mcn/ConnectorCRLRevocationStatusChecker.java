package be.medx.mcn;

import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.ehealth.etee.crypto.crl.CRLChecker;
import be.fgov.ehealth.etee.crypto.crl.CRLCheckerBuilder;
import be.fgov.ehealth.etee.crypto.crl.CRLData;
import be.fgov.ehealth.etee.crypto.policies.OCSPOption;
import be.fgov.ehealth.etee.crypto.status.CryptoResult;
import be.medx.exceptions.TechnicalConnectorException;

public final class ConnectorCRLRevocationStatusChecker extends AbstractRevocationStatusChecker {
	private static final Logger LOG;
	private CRLChecker crlChecker;

	public ConnectorCRLRevocationStatusChecker() {
		try {
			this.crlChecker = CRLCheckerBuilder.newBuilder().addCertStore((CertStore) CryptoFactory.getOCSPOptions().get(OCSPOption.CERT_STORE)).build();
		} catch (TechnicalConnectorException e) {
			ConnectorCRLRevocationStatusChecker.LOG.warn("Unable to obtain CertStore");
			this.crlChecker = CRLCheckerBuilder.newBuilder().build();
		}
	}

	@Override
	boolean delegateRevoke(final X509Certificate cert, final DateTime ValidOn) throws CertificateException {
		final CryptoResult<CRLData> crlData = this.crlChecker.validate(cert);
		if (crlData.getFatal() != null) {
			throw new CertificateException(crlData.getFatal().getErrorMessage());
		}
		switch (crlData.getData().getCertStatus()) {
		case REVOKED: {
			return true;
		}
		default: {
			return false;
		}
		}
	}

	static {
		LOG = LoggerFactory.getLogger(ConnectorCRLRevocationStatusChecker.class);
	}
}
