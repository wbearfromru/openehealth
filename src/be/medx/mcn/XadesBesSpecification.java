package be.medx.mcn;

import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.services.CryptoService;

public class XadesBesSpecification implements XadesSpecification {
	private static final Logger LOG;

	@Override
	public void addOptionalBeforeSignatureParts(final SignedPropertiesBuilder signedProps, final XMLSignature sig, final Credential signing, final String uuid, final Map<String, Object> options) throws TechnicalConnectorException {
		signedProps.setId(uuid);
		signedProps.setSigningCert(signing.getCertificate());
		signedProps.setSigningTime(new DateTime());
	}

	@Override
	public void addOptionalAfterSignatureParts(CryptoService cryptoService, final UnsignedPropertiesBuilder unsignedProps, final XMLSignature sig, final String uuid, final Map<String, Object> options) throws TechnicalConnectorException {
	}

	@Override
	public void verify(final SignatureVerificationResult result, final Element sigElement) {
		this.verifySigningTime(result, sigElement);
		this.verifySigningCertificate(result, sigElement);
	}

	private void verifySigningTime(final SignatureVerificationResult result, final Element sigElement) {
		final NodeList signingTime = DomUtils.getMatchingChilds(sigElement, "http://uri.etsi.org/01903/v1.3.2#", "SigningTime");
		if (signingTime != null && signingTime.getLength() == 1) {
			try {
				result.setSigningTime(new DateTime(((Element) signingTime.item(0)).getTextContent()));
			} catch (IllegalArgumentException e) {
				result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_INVALID_SIGNINGTIME);
			}
		} else {
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_DONT_HAVE_SIGNINGTIME);
		}
	}

	private void verifySigningCertificate(final SignatureVerificationResult result, final Element sigElement) {
		if (result.getSigningCert() == null) {
			XadesBesSpecification.LOG.debug("Unable to obtain signing certificate.");
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_COULD_NOT_BE_VERIFIED);
			return;
		}
		final NodeList signingCertificateList = DomUtils.getMatchingChilds(sigElement, "http://uri.etsi.org/01903/v1.3.2#", "SigningCertificate");
		if (signingCertificateList != null && signingCertificateList.getLength() == 1) {
			final Element certEl = (Element) signingCertificateList.item(0);
			this.verifyDigest(result, certEl);
			this.verifyIssuerName(result, certEl);
			this.verifySerialNumber(result, certEl);
			this.verifyValidity(result);
		} else {
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
		}
	}

	private void verifyValidity(final SignatureVerificationResult result) {
		try {
			result.getSigningCert().checkValidity();
		} catch (CertificateExpiredException e) {
			result.getErrors().add(SignatureVerificationError.CERTIFICATE_EXPIRED);
		} catch (CertificateNotYetValidException e2) {
			result.getErrors().add(SignatureVerificationError.CERTIFICATE_NOT_YET_VALID);
		}
	}

	private void verifyDigest(final SignatureVerificationResult result, final Element certEl) {
		final X509Certificate signingCert = result.getSigningCert();
		try {
			final String digestMethod = ((Element) certEl.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "DigestMethod").item(0)).getAttribute("Algorithm");
			final String digestValue = ((Element) certEl.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "DigestValue").item(0)).getTextContent();
			final MessageDigest messageDigest = SignatureUtils.getDigestInstance(digestMethod);
			messageDigest.reset();
			try {
				final byte[] calculatedDigest = messageDigest.digest(signingCert.getEncoded());
				if (!MessageDigest.isEqual(calculatedDigest, Base64.decode(digestValue))) {
					result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
				}
			} catch (CertificateEncodingException e) {
				XadesBesSpecification.LOG.warn("Unable to encode certificate with CN [" + signingCert.getSubjectX500Principal().getName("RFC1779") + "] Reason: " + e.getMessage(), e);
				result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_COULD_NOT_BE_VERIFIED);
			}
		} catch (Exception e2) {
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
		}
	}

	private void verifyIssuerName(final SignatureVerificationResult result, final Element certEl) {
		try {
			final String x509IssuerName = ((Element) certEl.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509IssuerName").item(0)).getTextContent();
			final X500Principal principal = new X500Principal(x509IssuerName);
			if (!principal.getName("RFC1779").equalsIgnoreCase(result.getSigningCert().getIssuerX500Principal().getName("RFC1779"))) {
				result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
			}
		} catch (Exception e) {
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
		}
	}

	private void verifySerialNumber(final SignatureVerificationResult result, final Element certEl) {
		try {
			final String x509SerialNumber = ((Element) certEl.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509SerialNumber").item(0)).getTextContent();
			if (!x509SerialNumber.equals(result.getSigningCert().getSerialNumber().toString())) {
				result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
			}
		} catch (Exception e) {
			result.getErrors().add(SignatureVerificationError.XADES_SIGNEDPROPS_NOT_VALID);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(XadesBesSpecification.class);
	}
}
