package be.medx.mcn;

import java.io.IOException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;

public class CmsSignatureBuilder extends AbstractSignatureBuilder implements SignatureBuilder {
	private static final String MSG_VERIFY_FAILED = "Unable to verify signature";
	private static final Logger LOG;
	private static JcaX509CertificateConverter converter;
	private static JcaSimpleSignerInfoVerifierBuilder verifierBuilder;
	private AdvancedElectronicSignatureEnumeration aes;

	public CmsSignatureBuilder(final AdvancedElectronicSignatureEnumeration aes) {
		this.aes = aes;
	}

	@Override
	public AdvancedElectronicSignatureEnumeration getSupportedAES() {
		return this.aes;
	}

	@Override
	public SignatureVerificationResult verify(final byte[] content, final byte[] signature, final Map<String, Object> options) throws TechnicalConnectorException {
		final SignatureVerificationResult result = new SignatureVerificationResult();
		try {
			final CMSSignedData signedContent = new CMSSignedData(signature);
			byte[] signedData;
			if (signedContent.getSignedContent() == null) {
				CmsSignatureBuilder.LOG.info("Signature has no ecapsulated signature. Adding content.");
				signedData = new CMSSignedData(new CMSProcessableByteArray(content), signature).getEncoded();
			} else {
				signedData = ArrayUtils.clone(signature);
			}
			return this.verify(signedData, options);
		} catch (CMSException e) {
			CmsSignatureBuilder.LOG.error("Unable to verify signature", e);
			result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
		} catch (IOException e2) {
			CmsSignatureBuilder.LOG.error("Unable to verify signature", e2);
			result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
		}
		return result;
	}

	@Override
	public SignatureVerificationResult verify(final byte[] signedByteArray, final Map<String, Object> options) throws TechnicalConnectorException {
		final SignatureVerificationResult result = new SignatureVerificationResult();
		try {
			final CMSSignedData signedData = new CMSSignedData(signedByteArray);
			this.extractChain(result, signedData);
			this.validateChain(result, options);
			for (final SignerInformation signer : signedData.getSignerInfos()) {
				if (!signer.verify(CmsSignatureBuilder.verifierBuilder.build(result.getSigningCert().getPublicKey()))) {
					result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
				}
			}
		} catch (Exception e) {
			CmsSignatureBuilder.LOG.error("Unable to verify signature", e);
			result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
		}
		return result;
	}

	@Override
	public byte[] sign(final Credential signatureCredential, final byte[] byteArrayToSign) throws TechnicalConnectorException {
		return this.sign(signatureCredential, byteArrayToSign, null);
	}

	@Override
	public byte[] sign(final Credential signatureCredential, final byte[] byteToSign, final Map<String, Object> options) throws TechnicalConnectorException {
		final byte[] contentToSign = ArrayUtils.clone(byteToSign);
		final Map<String, Object> optionMap = new HashMap<String, Object>();
		if (options != null) {
			optionMap.putAll(options);
		}
		this.validateInput(signatureCredential, contentToSign);
		try {
			final CMSTypedData content = new CMSProcessableByteArray(contentToSign);
			final CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			final String signatureAlgorithm = SignatureUtils.getOption("signatureAlgorithm", optionMap, "Sha1WithRSA");
			final JcaSignerInfoGeneratorBuilder signerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build());
			final ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(signatureCredential.getPrivateKey());
			generator.addSignerInfoGenerator(signerInfoGeneratorBuilder.build(contentSigner, signatureCredential.getCertificate()));
			final Certificate[] certificateChain = signatureCredential.getCertificateChain();
			if (certificateChain != null && certificateChain.length > 0) {
				generator.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
			}
			final boolean encapsulate = SignatureUtils.getOption("encapsulate", optionMap, Boolean.FALSE);
			return generator.generate(content, encapsulate).getEncoded();
		} catch (Exception e) {
			CmsSignatureBuilder.LOG.error(e.getMessage(), e);
			throw new TechnicalConnectorException();
		}
	}

	private void extractChain(final SignatureVerificationResult result, final CMSSignedData signedData) throws CertificateException {
		final Store<X509CertificateHolder> certs = signedData.getCertificates();
		final Collection<X509CertificateHolder> certCollection = certs.getMatches(new Selector<X509CertificateHolder>() {
			@Override
			public boolean match(final X509CertificateHolder cert) {
				return true;
			}

			@Override
			public Object clone() {
				throw new UnsupportedOperationException();
			}
		});
		final Iterator<X509CertificateHolder> iterator = certCollection.iterator();
		while (iterator.hasNext()) {
			result.getCertChain().add(CmsSignatureBuilder.converter.getCertificate(iterator.next()));
		}
	}

	static {
		LOG = LoggerFactory.getLogger(CmsSignatureBuilder.class);
		CmsSignatureBuilder.converter = new JcaX509CertificateConverter();
		CmsSignatureBuilder.verifierBuilder = new JcaSimpleSignerInfoVerifierBuilder();
		Security.addProvider(new BouncyCastleProvider());
	}
}
