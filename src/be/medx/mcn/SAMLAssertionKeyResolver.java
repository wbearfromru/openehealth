package be.medx.mcn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.medx.utils.ConnectorIOUtils;

public class SAMLAssertionKeyResolver extends KeyResolverSpi {
	private static final String ATTR_VALUE_SAML_1_1_PROFILE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
	private static final String ATTR_VALUE_ASSERTION_ID = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID";
	private static final String XMLNS_SAML = "urn:oasis:names:tc:SAML:1.0:assertion";
	private static final String XMLNS_WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final Logger LOG;
	private static final CertificateFactory CF;

	@Override
	public boolean engineCanResolve(final Element sigElement, final String baseURI, final StorageResolver storage) {
		return this.extract(sigElement) != null;
	}

	public Node extract(final Element sigElement) {
		Element securityTokenReference = null;
		if ("SecurityTokenReference".equals(sigElement.getLocalName()) && "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals(sigElement.getNamespaceURI())) {
			securityTokenReference = sigElement;
		} else {
			final NodeList securityTokenReferenceList = DomUtils.getMatchingChilds(sigElement, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "SecurityTokenReference");
			if (securityTokenReferenceList.getLength() == 1) {
				securityTokenReference = (Element) securityTokenReferenceList.item(0);
			}
		}
		if (securityTokenReference != null) {
			final String securityTokenReferenceTokenType = securityTokenReference.getAttributes().getNamedItemNS("http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd", "TokenType").getTextContent();
			if ("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1".equals(securityTokenReferenceTokenType)) {
				final NodeList keyIdentifierList = DomUtils.getMatchingChilds(securityTokenReference, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "KeyIdentifier");
				for (int j = 0; j < keyIdentifierList.getLength(); ++j) {
					final Node keyIdentifier = keyIdentifierList.item(j);
					final String keyIdentifierValueType = keyIdentifier.getAttributes().getNamedItem("ValueType").getTextContent();
					if ("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID".equals(keyIdentifierValueType)) {
						SAMLAssertionKeyResolver.LOG.debug("SAML1.1 assertion detected.");
						return keyIdentifier;
					}
				}
			}
		}
		return null;
	}

	@Override
	public X509Certificate engineResolveX509Certificate(final Element sigElement, final String baseURI, final StorageResolver storage) throws KeyResolverException {
		final Node keyIdentifier = this.extract(sigElement);
		final String samlAssertionId = keyIdentifier.getTextContent();
		final NodeList samlAssertionList = sigElement.getOwnerDocument().getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion");
		for (int k = 0; k < samlAssertionList.getLength(); ++k) {
			final Element samlAssertion = (Element) samlAssertionList.item(k);
			if (samlAssertionId.equals(samlAssertion.getAttributes().getNamedItem("AssertionID").getTextContent())) {
				final NodeList authenticationStatements = DomUtils.getMatchingChilds(samlAssertion, "urn:oasis:names:tc:SAML:1.0:assertion", "AuthenticationStatement");
				if (authenticationStatements.getLength() > 1) {
					SAMLAssertionKeyResolver.LOG.debug("Multiple AuthenticationStatements found;");
					return null;
				}
				final NodeList x509CertificateList = DomUtils.getMatchingChilds(authenticationStatements.item(0), "http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
				final List<X509Certificate> certList = new ArrayList<X509Certificate>();
				for (int l = 0; l < x509CertificateList.getLength(); ++l) {
					certList.add(this.generate(x509CertificateList.item(l).getTextContent()));
				}
				SAMLAssertionKeyResolver.LOG.debug("X509Certificate(s) detected in AuthenticationStatement [" + certList.size() + "];");
				try {
					final X509Certificate x509Certificate = (X509Certificate) SAMLAssertionKeyResolver.CF.generateCertPath(certList).getCertificates().get(0);
					SAMLAssertionKeyResolver.LOG.debug("returning  X509Certificate [" + x509Certificate.getSubjectX500Principal().getName("RFC1779"));
					return x509Certificate;
				} catch (CertificateException e) {
					SAMLAssertionKeyResolver.LOG.error("", e);
					return null;
				}
			}
		}
		return null;
	}

	private X509Certificate generate(final String keyVale) {
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(Base64.decode(keyVale.getBytes()));
			return (X509Certificate) SAMLAssertionKeyResolver.CF.generateCertificate(in);
		} catch (Exception e) {
			SAMLAssertionKeyResolver.LOG.error("Error while generating certificate.", e);
			return null;
		} finally {
			ConnectorIOUtils.closeQuietly(in);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(SAMLAssertionKeyResolver.class);
		try {
			CF = CertificateFactory.getInstance("X.509", "BC");
		} catch (NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		} catch (CertificateException e2) {
			throw new IllegalArgumentException(e2);
		}
	}
}
