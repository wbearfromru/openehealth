package be.medx.mcn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Map;

import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.keys.keyresolver.KeyResolver;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.crypto.Credential;
import be.medx.crypto.ExtendedCredential;
import be.medx.exceptions.TechnicalConnectorException;

public final class SignatureUtils {
	private static final Logger LOG;
	private static final String JSR105PROVIDER_CLASSNAME_DEFAULT = "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI";
	public static final String XMLNS_DS = "http://www.w3.org/2000/09/xmldsig#";
	public static final String XMLNS_XADES_1_3_2 = "http://uri.etsi.org/01903/v1.3.2#";
	private static final CertificateFactory CF;
	private static XMLSignatureFactory fac;

	private SignatureUtils() {
		throw new UnsupportedOperationException();
	}

	public static XMLSignatureFactory getXMLSignatureFactory() {
		return SignatureUtils.fac;
	}

	public static <T> T getOption(final String key, final Map<String, Object> optionMap, final T defaultValue) {
		T result = defaultValue;
		if (optionMap == null) {
			return result;
		}
		if (optionMap.containsKey(key) && optionMap.get(key) != null) {
			result = (T) optionMap.get(key);
		}
		SignatureUtils.LOG.info("Using the following " + key + ":" + result);
		return result;
	}

	public static MessageDigest getDigestInstance(final String algorithmURI) throws NoSuchAlgorithmException {
		final String algorithmID = JCEMapper.translateURItoJCEID(algorithmURI);
		if (algorithmID == null) {
			throw new NoSuchAlgorithmException("Could not translate algorithmURI [" + algorithmURI + "]");
		}
		final String provider = JCEMapper.getProviderId();
		MessageDigest md;
		try {
			if (provider == null) {
				md = MessageDigest.getInstance(algorithmID);
			} else {
				md = MessageDigest.getInstance(algorithmID, provider);
			}
		} catch (NoSuchProviderException ex) {
			throw new NoSuchAlgorithmException("Could not find provider for [" + algorithmID + "]", ex);
		}
		return md;
	}

	public static CertPath getCertPath(final Credential cred) throws TechnicalConnectorException {
		if (cred instanceof ExtendedCredential) {
			return ((ExtendedCredential) cred).getCertPath();
		}
		try {
			return SignatureUtils.CF.generateCertPath(Arrays.asList(cred.getCertificateChain()));
		} catch (CertificateException e) {
			throw new TechnicalConnectorException();
		}
	}

	static {
		LOG = LoggerFactory.getLogger(SignatureUtils.class);
		try {
			CF = CertificateFactory.getInstance("X.509", "BC");
		} catch (NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		} catch (CertificateException e2) {
			throw new IllegalArgumentException(e2);
		}
		Security.addProvider(new BouncyCastleProvider());
		KeyResolver.register(new SAMLAssertionKeyResolver(), true);
		try {
			final String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
			SignatureUtils.LOG.info("Instantiating providate with class [" + providerName + "]");
			final Provider provider = (Provider) Class.forName("org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI").newInstance();
			SignatureUtils.LOG.info("Using the following provider: " + provider + " " + provider.getInfo());
			SignatureUtils.fac = XMLSignatureFactory.getInstance("DOM", provider);
		} catch (ClassNotFoundException e3) {
			throw new RuntimeException(e3);
		} catch (InstantiationException e4) {
			throw new RuntimeException(e4);
		} catch (IllegalAccessException e5) {
			throw new RuntimeException(e5);
		}
	}
}
