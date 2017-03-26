// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.services.CryptoService;
import be.medx.utils.ConnectorIOUtils;
import be.medx.utils.TimestampUtil;

public class XadesTSpecification implements XadesSpecification {
	private static final String DEFAULT_C14N_METHOD = "http://www.w3.org/2001/10/xml-exc-c14n#";
	private static final Logger LOG;

	public XadesTSpecification() {
	}

	@Override
	public void addOptionalBeforeSignatureParts(final SignedPropertiesBuilder signedProps, final XMLSignature sig, final Credential signing, final String uuid, final Map<String, Object> options) throws TechnicalConnectorException {
	}

	@Override
	public void addOptionalAfterSignatureParts(CryptoService cryptoService, final UnsignedPropertiesBuilder unsignedProps, final XMLSignature sig, final String uuid, final Map<String, Object> options) throws TechnicalConnectorException {
		final String c14nMethod = SignatureUtils.getOption("SignatureTimeStampCanonicalizationMethodURI", options, "http://www.w3.org/2001/10/xml-exc-c14n#");
		final byte[] tsToken = this.generateSignatureTimestamp(cryptoService, sig, options, c14nMethod);
		unsignedProps.addSignatureTimestamp(tsToken, c14nMethod);
	}

	@Override
	public void verify(final SignatureVerificationResult result, final Element sigElement) {
		this.verifySignatureTimeStamp(result, sigElement);
	}

	private byte[] generateSignatureTimestamp(CryptoService cryptoService, final XMLSignature sig, final Map<String, Object> options, final String c14nMethodValue) throws TechnicalConnectorException {
		final byte[] digest = this.generateTimestampDigest(sig.getElement(), c14nMethodValue);
		final String digestAlgoUri = SignatureUtils.getOption("SignatureTimestampAlgorithmURI", options, "http://www.w3.org/2001/04/xmlenc#sha256");
		return new TimeStampGeneratorImpl(cryptoService).generate(sig.getId(), digestAlgoUri, digest);
	}

	private void verifySignatureTimeStamp(final SignatureVerificationResult result, final Element baseElement) {
		try {
			final NodeList signatureTimeStampList = DomUtils.getMatchingChilds(baseElement, "http://uri.etsi.org/01903/v1.3.2#", "SignatureTimeStamp");
			if (signatureTimeStampList != null && signatureTimeStampList.getLength() > 0) {
				for (int i = 0; i < signatureTimeStampList.getLength(); ++i) {
					final Element signatureTimeStamp = (Element) signatureTimeStampList.item(i);
					final NodeList timestampList = DomUtils.getMatchingChilds(signatureTimeStamp, "http://uri.etsi.org/01903/v1.3.2#", "EncapsulatedTimeStamp");
					final NodeList c14nNodeList = DomUtils.getMatchingChilds(signatureTimeStamp, "http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
					String c14nMethodValue = null;
					if (c14nNodeList == null || c14nNodeList.getLength() == 0) {
						XadesTSpecification.LOG.info("Unable to detect CanonicalizationMethod, using default [http://www.w3.org/2001/10/xml-exc-c14n#]");
						c14nMethodValue = "http://www.w3.org/2001/10/xml-exc-c14n#";
					} else {
						c14nMethodValue = c14nNodeList.item(0).getAttributes().getNamedItem("Algorithm").getTextContent();
					}
					if (timestampList != null && timestampList.getLength() > 0) {
						for (int j = 0; j < timestampList.getLength(); ++j) {
							try {
								final Node timestampNode = timestampList.item(j);
								final byte[] digestValue = this.generateTimestampDigest(baseElement, c14nMethodValue);
								final TimeStampToken tsToken = TimestampUtil.getTimeStampToken(Base64.decode(timestampNode.getTextContent().getBytes()));
								new TimeStampValidatorImpl().validateTimeStampToken(digestValue, tsToken);
								result.getTimestampGenTimes().add(new DateTime(tsToken.getTimeStampInfo().getGenTime()));
								result.getTsTokens().add(tsToken);
							} catch (TechnicalConnectorException e) {
								XadesTSpecification.LOG.error(e.getMessage());
								result.getErrors().add(SignatureVerificationError.XADES_ENCAPSULATED_TIMESTAMP_NOT_VALID);
							}
						}
					} else {
						result.getErrors().add(SignatureVerificationError.XADES_ENCAPSULATED_TIMESTAMP_NOT_FOUND);
					}
				}
			} else {
				result.getErrors().add(SignatureVerificationError.XADES_ENCAPSULATED_TIMESTAMP_NOT_FOUND);
			}
		} catch (Exception e2) {
			XadesTSpecification.LOG.error("Unable to verify Timestamp", e2);
			result.getErrors().add(SignatureVerificationError.XADES_ENCAPSULATED_TIMESTAMP_NOT_VERIFIED);
		}
	}

	private byte[] generateTimestampDigest(final Element baseElement, final String c14nMethodValue) {
		try {
			final Node signatureValue = DomUtils.getMatchingChilds(baseElement, "http://www.w3.org/2000/09/xmldsig#", "SignatureValue").item(0);
			final Transform transform = new Transform(signatureValue.getOwnerDocument(), c14nMethodValue);
			final XMLSignatureInput refData = transform.performTransform(new XMLSignatureInput(signatureValue));
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (refData.isByteArray()) {
				baos.write(refData.getBytes());
			} else if (refData.isOctetStream()) {
				baos.write(ConnectorIOUtils.getBytes(refData.getOctetStream()));
			}
			return baos.toByteArray();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to calculateDigest", e);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(XadesBesSpecification.class);
	}
}
