package be.medx.mcn;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.ehealth.etee.crypto.cert.CertificateStatus;
import be.fgov.ehealth.etee.crypto.ocsp.OCSPChecker;
import be.fgov.ehealth.etee.crypto.ocsp.OCSPCheckerBuilder;
import be.fgov.ehealth.etee.crypto.ocsp.OCSPData;
import be.fgov.ehealth.etee.crypto.ocsp.RevocationValues;
import be.fgov.ehealth.etee.crypto.policies.OCSPOption;
import be.fgov.ehealth.etee.crypto.policies.OCSPPolicy;
import be.fgov.ehealth.etee.crypto.status.CryptoResult;
import be.medx.exceptions.TechnicalConnectorException;

public final class ConnectorOCSPRevocationStatusChecker extends AbstractRevocationStatusChecker {
	private static final Logger LOG;
	private OCSPChecker ocspchecker;

	public ConnectorOCSPRevocationStatusChecker() {
		final Map<OCSPOption, Object> options = new HashMap<OCSPOption, Object>();
		try {
			options.putAll(CryptoFactory.getOCSPOptions());
		} catch (TechnicalConnectorException e) {
			ConnectorOCSPRevocationStatusChecker.LOG.warn("Unable to load ocsp options.", (Throwable) e);
		}
		this.ocspchecker = OCSPCheckerBuilder.newBuilder().addOCSPPolicy(OCSPPolicy.RECEIVER_MANDATORY, options).build();
	}

	@Override
	boolean delegateRevoke(final X509Certificate cert, final DateTime validOn) throws CertificateException {
		final CryptoResult<OCSPData> result = this.ocspchecker.validate(cert, validOn.toDate(), new RevocationValues());
		if (result.getFatal() != null || result.getData() == null) {
			throw new CertificateException(result.toString());
		}
		return !result.getData().getCertStatus().equals(CertificateStatus.VALID);
	}

	static {
		LOG = LoggerFactory.getLogger(ConnectorOCSPRevocationStatusChecker.class);
	}
}
