package be.medx.mcn;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.IdGeneratorFactory;
import be.medx.saml.SAMLToken;
import be.medx.services.CryptoService;
import be.medx.soap.enums.Charset;
import be.medx.utils.ConnectorIOUtils;
import be.medx.utils.ConnectorXmlUtils;

public class XmlSignatureBuilder extends AbstractSignatureBuilder implements SignatureBuilder {
	private static final Logger LOG;
	private XadesSpecification[] specs;
	private AdvancedElectronicSignatureEnumeration aes;
	private CryptoService cryptoService;

	public XmlSignatureBuilder(CryptoService cryptoService, final AdvancedElectronicSignatureEnumeration aes, final XadesSpecification... specs) {
		this.specs = specs;
		this.aes = aes;
		this.cryptoService = cryptoService;
	}

	@Override
	public byte[] sign(final Credential signatureCredential, final byte[] byteArrayToSign) throws TechnicalConnectorException {
		return this.sign(signatureCredential, byteArrayToSign, new HashMap<String, Object>());
	}

	@Override
	public byte[] sign(final Credential signatureCredential, final byte[] byteArrayToSign, final Map<String, Object> options) throws TechnicalConnectorException {
		final Map<String, Object> optionMap = new HashMap<String, Object>();
		if (options != null) {
			optionMap.putAll(options);
		}
		this.validateInput(signatureCredential, byteArrayToSign);
		try {
			final String baseURI = SignatureUtils.getOption("baseURI", optionMap, "");
			final String signatureMethodURI = SignatureUtils.getOption("signatureMethodURI", optionMap, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
			final String canonicalizationMethodURI = SignatureUtils.getOption("canonicalizationMethodURI", optionMap, "http://www.w3.org/2001/10/xml-exc-c14n#");
			final List<String> transformerList = SignatureUtils.getOption("transformerList", optionMap, new ArrayList<String>());
			final String digestURI = SignatureUtils.getOption("digestURI", optionMap, "http://www.w3.org/2001/04/xmlenc#sha256");
			boolean encapsulate = SignatureUtils.getOption("encapsulate", optionMap, Boolean.FALSE);
			if (encapsulate && !transformerList.contains("http://www.w3.org/2000/09/xmldsig#enveloped-signature")) {
				transformerList.add(0, "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
			} else if (!encapsulate && transformerList.contains("http://www.w3.org/2000/09/xmldsig#enveloped-signature")) {
				encapsulate = true;
			}
			final Document doc = ConnectorXmlUtils.toDocument(byteArrayToSign);
			final XMLSignature sig = new XMLSignature(doc, baseURI, signatureMethodURI, canonicalizationMethodURI);
			final DocumentResolver resolver = new DocumentResolver(baseURI, doc);
			sig.addResourceResolver(resolver);
			final Transforms baseDocTransform = this.createDocumentTransform(transformerList, doc);
			sig.addDocument(ref(baseURI), baseDocTransform, digestURI);
			final Transforms xadesTransform = new Transforms(doc);
			xadesTransform.addTransform("http://www.w3.org/2001/10/xml-exc-c14n#");
			final ObjectContainer container = new ObjectContainer(sig.getDocument());
			sig.appendObject(container);
			if (signatureCredential instanceof SAMLToken) {
				final SAMLToken token = (SAMLToken) signatureCredential;
				sig.getKeyInfo().addUnknownElement((Element) sig.getDocument().importNode(this.obtainSAMLTokenReference(token), true));
				container.appendChild(sig.getDocument().importNode(token.getAssertion(), true));
				final Transforms samlToken = new Transforms(doc);
				samlToken.addTransform("http://www.w3.org/2001/10/xml-exc-c14n#");
				final String samlTokenURI = token.getAssertion().getAttribute("AssertionID");
				final DocumentResolver samlTokenResolver = new DocumentResolver(samlTokenURI, ((SAMLToken) signatureCredential).getAssertion().getOwnerDocument());
				sig.addResourceResolver(samlTokenResolver);
				sig.addDocument(ref(samlTokenURI), samlToken, digestURI, (String) null, "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1");
			} else if (signatureCredential.getCertificateChain() != null) {
				for (final Certificate cert : signatureCredential.getCertificateChain()) {
					sig.addKeyInfo((X509Certificate) cert);
				}
			}
			final String xadesUuid = IdGeneratorFactory.getIdGenerator("uuid").generateId();
			final QualifyingPropertiesBuilder qualProperties = new QualifyingPropertiesBuilder();
			for (final XadesSpecification spec : this.specs) {
				spec.addOptionalBeforeSignatureParts(qualProperties.getSignedProps(), sig, signatureCredential, xadesUuid, options);
			}
			final Document xadesQualPropertiesDocument = qualProperties.buildBeforeSigningAsDocument();
			final Element xadesQualProperties = (Element) sig.getDocument().importNode(xadesQualPropertiesDocument.getDocumentElement(), true);
			container.appendChild(xadesQualProperties);
			resolver.addDocument(qualProperties.getSignedProps().getId(), xadesQualPropertiesDocument);
			sig.addDocument(ref(qualProperties.getSignedProps().getId()), xadesTransform, digestURI, (String) null, "http://uri.etsi.org/01903#SignedProperties");
			sig.sign(signatureCredential.getPrivateKey());
			final String xmldsigId = "xmldsig-" + xadesUuid;
			sig.setId(xmldsigId);
			xadesQualProperties.setAttribute("Target", ref(xmldsigId));
			final UnsignedPropertiesBuilder unsignedProperties = new UnsignedPropertiesBuilder();
			unsignedProperties.setId(xadesUuid);
			for (final XadesSpecification spec2 : this.specs) {
				spec2.addOptionalAfterSignatureParts(this.cryptoService, unsignedProperties, sig, xadesUuid, options);
			}
			final Document xadesUnsignedPropertiesDoc = unsignedProperties.buildAsDocument();
			if (xadesUnsignedPropertiesDoc != null) {
				final Element xadesUnsignedProperties = (Element) sig.getDocument().importNode(unsignedProperties.buildAsDocument().getDocumentElement(), true);
				xadesQualProperties.appendChild(xadesUnsignedProperties);
			}
			if (encapsulate) {
				doc.getFirstChild().insertBefore(doc.adoptNode(sig.getElement()), null);
				return ConnectorXmlUtils.toByteArray(doc);
			}
			return ConnectorXmlUtils.toByteArray(sig.getElement());
		} catch (TransformationException e) {
			throw new TechnicalConnectorException();
		} catch (XMLSignatureException e2) {
			throw new TechnicalConnectorException();
		} catch (XMLSecurityException e3) {
			throw new TechnicalConnectorException();
		}
	}

	private Transforms createDocumentTransform(final List<String> tranformerList, final Document doc) throws TransformationException {
		final Transforms baseDocTransform = new Transforms(doc);
		for (final String transform : tranformerList) {
			baseDocTransform.addTransform(transform);
		}
		return baseDocTransform;
	}

	@Override
	public SignatureVerificationResult verify(final byte[] signedByteArray, final Map<String, Object> options) throws TechnicalConnectorException {
		final Document signedContent = ConnectorXmlUtils.toDocument(signedByteArray);
		final NodeList signatureList = DomUtils.getMatchingChilds(signedContent, "http://www.w3.org/2000/09/xmldsig#", "Signature");
		if (signatureList == null || signatureList.getLength() == 0) {
			XmlSignatureBuilder.LOG.info("No signature found in signedContent");
			final SignatureVerificationResult result = new SignatureVerificationResult();
			result.getErrors().add(SignatureVerificationError.SIGNATURE_NOT_PRESENT);
			return result;
		}
		if (signatureList.getLength() > 1) {
			XmlSignatureBuilder.LOG.info("Multiple signature found, using first one.");
		}
		return this.verify(signedContent, (Element) signatureList.item(0), options);
	}

	@Override
	public SignatureVerificationResult verify(final byte[] signedByteArray, final byte[] signature, final Map<String, Object> options) throws TechnicalConnectorException {
		final Element sigElement = ConnectorXmlUtils.toElement(signature);
		final Document signedContent = ConnectorXmlUtils.toDocument(signedByteArray);
		return this.verify(signedContent, sigElement, options);
	}

	public SignatureVerificationResult verify(final Document signedContent, final Element sigElement, final Map<String, Object> options) throws TechnicalConnectorException {
		final Map<String, Object> optionMap = new HashMap<String, Object>();
		if (options != null) {
			optionMap.putAll(options);
		}
		final SignatureVerificationResult result = new SignatureVerificationResult();
		final NodeList signatureList = DomUtils.getMatchingChilds(signedContent, "http://www.w3.org/2000/09/xmldsig#", "Signature");
		if (signatureList == null || signatureList.getLength() == 0) {
			XmlSignatureBuilder.LOG.info("Adding signature to signedContent");
			signedContent.getFirstChild().appendChild(signedContent.importNode(sigElement, true));
		}
		this.verifyXmlDsigSignature(result, sigElement, signedContent, optionMap);
		this.verifyManifest(result, sigElement, optionMap);
		for (final XadesSpecification spec : this.specs) {
			spec.verify(result, sigElement);
		}
		this.validateChain(result, options);
		return result;
	}

	private void verifyManifest(final SignatureVerificationResult result, final Element sigElement, final Map<String, Object> options) {
		final Boolean followNestedManifest = SignatureUtils.getOption("followNestedManifest", options, Boolean.FALSE);
		if (followNestedManifest) {
			final Element signedInfo = (Element) DomUtils.getMatchingChilds(sigElement, "http://www.w3.org/2000/09/xmldsig#", "SignedInfo").item(0);
			final NodeList referencesList = DomUtils.getMatchingChilds(signedInfo, "http://www.w3.org/2000/09/xmldsig#", "Reference");
			for (int i = 0; i < referencesList.getLength(); ++i) {
				final Element reference = (Element) referencesList.item(i);
				final String refType = reference.getAttribute("Type");
				if (refType.endsWith("Manifest") && !refType.equalsIgnoreCase("http://www.w3.org/2000/09/xmldsig#Manifest")) {
					result.getErrors().add(SignatureVerificationError.SIGNATURE_MANIFEST_COULD_NOT_BE_VERIFIED);
				}
			}
		}
	}

	private void verifyXmlDsigSignature(final SignatureVerificationResult result, final Element sigElement, final Document signedContent, final Map<String, Object> options) {
		try {
			final String uri = IdGeneratorFactory.getIdGenerator("uuid").generateId();
			final XMLSignature xmlSignature = new XMLSignature(sigElement, uri);
			final Boolean followNestedManifest = SignatureUtils.getOption("followNestedManifest", options, Boolean.FALSE);
			xmlSignature.setFollowNestedManifests(followNestedManifest);
			xmlSignature.addResourceResolver(new DocumentResolver(uri, signedContent));
			final KeyInfo keyInfo = xmlSignature.getKeyInfo();
			keyInfo.setSecureValidation(false);
			final Extractor extractor = new ForkedExtractor(new Extractor[] { new X509DataExctractor(), new SecurityTokenReferenceExtractor() });
			result.getCertChain().addAll(extractor.extract(keyInfo));
			final X509Certificate signingCert = this.extractEndCertificate(result.getCertChain());
			result.setSigningCert(signingCert);
			if (!xmlSignature.checkSignatureValue(signingCert)) {
				result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
			}
		} catch (Exception e) {
			XmlSignatureBuilder.LOG.error("Unable to verify XmlDsig Signature", e);
			result.getErrors().add(SignatureVerificationError.SIGNATURE_COULD_NOT_BE_VERIFIED);
		}
	}

	private Element obtainSAMLTokenReference(final SAMLToken signatureCredential) throws TechnicalConnectorException {
		String samlRef = ConnectorIOUtils.getResourceAsString("/templates/keyinfo-saml1.1-reference.xml");
		samlRef = StringUtils.replace(samlRef, "${assertionId}", signatureCredential.getAssertion().getAttribute("AssertionID"));
		return ConnectorXmlUtils.toDocument(ConnectorIOUtils.toBytes(samlRef, Charset.UTF_8)).getDocumentElement();
	}

	@Override
	public AdvancedElectronicSignatureEnumeration getSupportedAES() {
		return this.aes;
	}

	private static String ref(final String id) {
		return "#" + id;
	}

	static {
		LOG = LoggerFactory.getLogger(XmlSignatureBuilder.class);
	}
}
