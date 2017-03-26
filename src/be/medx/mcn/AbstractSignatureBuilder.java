package be.medx.mcn;

import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;

public class AbstractSignatureBuilder {
	private static final Logger LOG;
	private static final CertificateFactory CF;

	protected void validateInput(final Credential signatureCredential, final byte[] byteArrayToSign) throws TechnicalConnectorException {
		if (byteArrayToSign == null || byteArrayToSign.length == 0) {
			throw new TechnicalConnectorException();
		}
		if (signatureCredential == null) {
			throw new TechnicalConnectorException();
		}
	}

	protected void validateChain(final SignatureVerificationResult result, final Map<String, Object> options) throws TechnicalConnectorException {
		final Integer duration = SignatureUtils.getOption("SigningTimeClockSkewDuration", options, new Integer(5));
		final TimeUnit timeUnit = SignatureUtils.getOption("SigningTimeClockSkewTimeUnit", options, TimeUnit.MINUTES);
		final CertificateChecker certChecker = new ConnectorCertificateChecker();
		for (final X509Certificate cert : result.getCertChain()) {
			try {
				cert.checkValidity(result.getVerifiedSigningTime(duration, timeUnit).toDate());
			} catch (CertificateExpiredException e) {
				result.getErrors().add(SignatureVerificationError.CERTIFICATE_EXPIRED);
			} catch (CertificateNotYetValidException e2) {
				result.getErrors().add(SignatureVerificationError.CERTIFICATE_NOT_YET_VALID);
			}
		}
		try {
			if (!certChecker.isValidCertificateChain(result.getCertChain())) {
				result.getErrors().add(SignatureVerificationError.CERTIFICATE_CHAIN_NOT_TRUSTED);
			}
			this.validateEndCertificate(result, certChecker, duration, timeUnit);
		} catch (TechnicalConnectorException e3) {
			result.getErrors().add(SignatureVerificationError.CERTIFICATE_CHAIN_COULD_NOT_BE_VERIFIED);
		}
	}

	protected X509Certificate extractEndCertificate(final List<X509Certificate> chain) throws CertificateException {
		final CertPath certChain = AbstractSignatureBuilder.CF.generateCertPath(chain);
		return (X509Certificate) certChain.getCertificates().get(0);
	}

	private void validateEndCertificate(final SignatureVerificationResult result, final CertificateChecker certChecker, final Integer duration, final TimeUnit timeUnit) throws TechnicalConnectorException {
		try {
			final X509Certificate cert = this.extractEndCertificate(result.getCertChain());
			if (certChecker.isCertificateRevoked(cert, result.getVerifiedSigningTime(duration, timeUnit))) {
				result.getErrors().add(SignatureVerificationError.CERTIFICATE_REVOKED);
			}
			result.setSigningCert(cert);
		} catch (CertificateException e) {
			AbstractSignatureBuilder.LOG.error("EndCertificate invalid.", e);
			result.getErrors().add(SignatureVerificationError.CERTIFICATE_COULD_NOT_BE_VERIFIED);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(AbstractSignatureBuilder.class);
		try {
			CF = CertificateFactory.getInstance("X.509", "BC");
		} catch (NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		} catch (CertificateException e2) {
			throw new IllegalArgumentException(e2);
		}
	}
}
