// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import org.joda.time.DateTime;

import be.fgov.ehealth.etee.crypto.cert.CertPathCheckerBuilder;
import be.fgov.ehealth.etee.crypto.cert.CertificateStatus;
import be.fgov.ehealth.etee.crypto.status.CryptoResult;
import be.medx.exceptions.TechnicalConnectorException;

public class ConnectorCertificateChecker implements CertificateChecker {
	@Override
	public boolean isCertificateRevoked(final File certFile) throws TechnicalConnectorException {
		return this.isCertificateRevoked(certFile, new DateTime());
	}

	@Override
	public boolean isCertificateRevoked(final File certFile, final DateTime validOn) throws TechnicalConnectorException {
		try {
			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			final X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new FileInputStream(certFile));
			return this.isCertificateRevoked(cert, validOn);
		} catch (FileNotFoundException e) {
			throw new TechnicalConnectorException();
		} catch (CertificateException e2) {
			throw new TechnicalConnectorException();
		}
	}

	@Override
	public boolean isCertificateRevoked(final X509Certificate cert) throws TechnicalConnectorException {
		return this.isCertificateRevoked(cert, new DateTime());
	}

	@Override
	public boolean isCertificateRevoked(final X509Certificate cert, final DateTime validOn) throws TechnicalConnectorException {
		try {
			return new ConnectorRevocationStatusChecker().isRevoked(cert, validOn);
		} catch (CertificateException e) {
			throw new TechnicalConnectorException();
		}
	}

	@Override
	public boolean isValidCertificateChain(final List<X509Certificate> certificateChain) throws TechnicalConnectorException {
		final CryptoResult<CertificateStatus> result = CertPathCheckerBuilder.newBuilder().addTrustStore(CryptoFactory.getCaCertificateStore()).build().validate(certificateChain);
		return !result.hasErrors();
	}
}
