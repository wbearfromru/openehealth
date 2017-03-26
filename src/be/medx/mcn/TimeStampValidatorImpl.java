// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.exceptions.TechnicalConnectorException;

public class TimeStampValidatorImpl implements TimeStampValidator {
	private static final Logger LOG;
	private KeyStore keyStore;
	private List<String> aliases;

	@Override
	public void validateTimeStampToken(final byte[] bs, final TimeStampToken tsToken) throws TechnicalConnectorException {
		final byte[] calculatedDigest = ConnectorCryptoUtils.calculateDigest(tsToken.getTimeStampInfo().getMessageImprintAlgOID().getId(), bs);
		final byte[] tokenDigestValue = tsToken.getTimeStampInfo().getMessageImprintDigest();
		if (!MessageDigest.isEqual(calculatedDigest, tokenDigestValue)) {
			throw new TechnicalConnectorException();
		}
		final Attribute scV1 = tsToken.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificate);
		final Attribute scV2 = tsToken.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);
		if (scV1 == null && scV2 == null) {
			throw new TechnicalConnectorException();
		}
		if (scV1 != null && scV2 != null) {
			throw new TechnicalConnectorException();
		}
		this.validateTimeStampToken(tsToken);
	}

	@Override
	public void validateTimeStampToken(final TimeStampToken tsToken) throws TechnicalConnectorException {
		Validate.notNull(this.keyStore, "keyStore is not correctly initialised.");
		Validate.notNull(this.aliases, "aliases is not correctly initialised.");
		Validate.notNull(tsToken, "Parameter tsToken value is not nullable.");
		if (tsToken.getTimeStampInfo() != null) {
			TimeStampValidatorImpl.LOG.debug("Validating TimeStampToken with SerialNumber [" + tsToken.getTimeStampInfo().getSerialNumber() + "]");
		}
		boolean signatureValid = false;
		Exception lastException = null;
		for (final String alias : this.aliases) {
			try {
				final X509Certificate ttsaCert = (X509Certificate) this.keyStore.getCertificate(alias);
				TimeStampValidatorImpl.LOG.debug("Trying to validate timestamp against certificate with alias [" + alias + "] : [" + ttsaCert.getSubjectX500Principal().getName("RFC1779") + "]");
				final X509CertificateHolder tokenSigner = new X509CertificateHolder(ttsaCert.getEncoded());
				final SignerInformationVerifier verifier = new BcRSASignerInfoVerifierBuilder(new DefaultCMSSignatureAlgorithmNameGenerator(), new DefaultSignatureAlgorithmIdentifierFinder(), new DefaultDigestAlgorithmIdentifierFinder(), new BcDigestCalculatorProvider()).build(tokenSigner);
				tsToken.validate(verifier);
				signatureValid = true;
			} catch (Exception e) {
				lastException = e;
				TimeStampValidatorImpl.LOG.debug("TimeStampToken not valid with certificate-alias [" + alias + "]: " + e.getMessage());
				continue;
			}
			break;
		}
		if (!signatureValid) {
			throw new TechnicalConnectorException();
		}
		TimeStampValidatorImpl.LOG.debug("timestampToken is valid");
	}

	private List<String> getAliases() {
		try {
			return Collections.list(this.keyStore.aliases());
		} catch (KeyStoreException e) {
			return new ArrayList<String>();
		}
	}

	static {
		LOG = LoggerFactory.getLogger(TimeStampValidatorImpl.class);
	}
}
