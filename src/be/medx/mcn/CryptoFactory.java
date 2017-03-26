package be.medx.mcn;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ws.security.components.crypto.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.fgov.ehealth.etee.crypto.policies.OCSPOption;
import be.fgov.ehealth.etee.crypto.policies.OCSPPolicy;
import be.fgov.ehealth.etee.crypto.policies.SigningOption;
import be.medx.crypto.Credential;
import be.medx.crypto.KeyStoreManager;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConfigurableFactoryHelper;

public final class CryptoFactory {
	private static final Logger LOG;
	public static final String PROPS_CRYPTO_CLASS = "crypto.classname";
	private static final String DEFAULT_CERT_CHECKER_CLASS = "be.ehealth.technicalconnector.service.etee.impl.CryptoImpl";
	private static final String TIMESTAMP_SIGNATURE_KEYSTORE_PWD = "timestamp.signature.keystore.pwd";
	private static final String TIMESTAMP_SIGNATURE_KEYSTORE_PATH = "timestamp.signature.keystore.path";
	public static final String SIGNING_TIME_EXPIRATION = "be.fgov.ehealth.etee.crypto.policies.SigningOption.SIGNING_TIME_EXPIRATION";
	public static final String SIGNING_CLOCK_SKEW = "be.fgov.ehealth.etee.crypto.policies.SigningOption.CLOCK_SKEW";
	public static final String SIGNING_TIME_TRUST_IMPLICIT = "be.fgov.ehealth.etee.crypto.policies.SigningOption.SIGNING_TIME_TRUST_IMPLICIT";
	public static final String SIGNING_TSA_CERT_STORE = "be.fgov.ehealth.etee.crypto.policies.SigningOption.TSA_CERT_STORE";
	public static final String OCSP_URI = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.OCSP_URI";
	public static final String OCSP_INJECT_RESPONSE = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.INJECT_RESPONSE";
	public static final String OCSP_CLOCK_SKEW = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.CLOCK_SKEW";
	public static final String OCSP_CONNECTION_TIMEOUT = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.CONNECTION_TIMEOUT";
	public static final String OCSP_CERT_STORE = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.CERT_STORE";
	public static final String OCSP_READ_TIMEOUT = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.READ_TIMEOUT";
	public static final String OCSP_CONNECTION_USER_INTERACTION = "be.fgov.ehealth.etee.crypto.policies.OCSPOption.CONNECTION_USER_INTERACTION";
	private static final String PROP_CAKEYSTORE_PATH = "CAKEYSTORE_LOCATION";
	private static final String PROP_CAKEYSTORE_PASSWORD = "CAKEYSTORE_PASSWORD";
	private static final String PROP_KEYSTORE_DIR = "KEYSTORE_DIR";
	private static Map<OCSPOption, Object> ocspOptionMap;
	private static final Object mutex;
	private static ConfigurableFactoryHelper<Crypto> helper;

	public static Crypto getCrypto(final Credential encryption, final Map<String, PrivateKey> decryptionKeys, final String oCSPPolicy) throws TechnicalConnectorException {
		final Map<String, Object> configParameters = new HashMap<String, Object>();
		configParameters.put("datasealer.credential", encryption);
		configParameters.put("dataunsealer.pkmap", decryptionKeys);
		configParameters.put("cryptolib.ocsp.policy", OCSPPolicy.valueOf(oCSPPolicy));
		final Map<SigningOption, Object> signingOptions = new HashMap<SigningOption, Object>();
		signingOptions.put(SigningOption.SIGNING_TIME_EXPIRATION, 5);
		signingOptions.put(SigningOption.CLOCK_SKEW, 300000L);
		signingOptions.put(SigningOption.SIGNING_TIME_TRUST_IMPLICIT, Boolean.FALSE);
		signingOptions.put(SigningOption.TSA_TRUST_STORE, getKeyStore("timestamp.signature.keystore.path", "timestamp.signature.keystore.pwd"));
		signingOptions.put(SigningOption.TSA_CERT_STORE, generateCertStore("be.fgov.ehealth.etee.crypto.policies.SigningOption.TSA_CERT_STORE", new KeyStore[0]));
		configParameters.put("cryptolib.signing.optionmap", signingOptions);
		configParameters.put("cryptolib.ocsp.optionmap", getOCSPOptions());
		return CryptoFactory.helper.getImplementation(configParameters);
	}

	public static Map<OCSPOption, Object> getOCSPOptions() throws TechnicalConnectorException {
		if (CryptoFactory.ocspOptionMap == null) {
			synchronized (CryptoFactory.mutex) {
				(CryptoFactory.ocspOptionMap = new HashMap<OCSPOption, Object>()).put(OCSPOption.OCSP_URI, "");
				final KeyStore trustStore = getCaCertificateStore();
				CryptoFactory.ocspOptionMap.put(OCSPOption.TRUST_STORE, trustStore);
				CryptoFactory.ocspOptionMap.put(OCSPOption.CERT_STORE, generateCertStore("be.fgov.ehealth.etee.crypto.policies.OCSPOption.CERT_STORE", trustStore));
				CryptoFactory.ocspOptionMap.put(OCSPOption.INJECT_RESPONSE, Boolean.FALSE);
				CryptoFactory.ocspOptionMap.put(OCSPOption.CLOCK_SKEW, 300000L);
				CryptoFactory.ocspOptionMap.put(OCSPOption.CONNECTION_TIMEOUT, 3000);
				CryptoFactory.ocspOptionMap.put(OCSPOption.READ_TIMEOUT, 3000);
				CryptoFactory.ocspOptionMap.put(OCSPOption.CONNECTION_USER_INTERACTION, Boolean.FALSE);
			}
		}
		return CryptoFactory.ocspOptionMap;
	}

	public static void resetOCSPOptions() {
		CryptoFactory.ocspOptionMap = null;
	}

	public static KeyStore getCaCertificateStore() throws TechnicalConnectorException {
		return getKeyStore("CAKEYSTORE_LOCATION", "CAKEYSTORE_PASSWORD");
	}

	private static KeyStore getKeyStore(final String key, final String password) throws TechnicalConnectorException {
		try {
			KeyStore keystore = null;
			final char[] pwd = "system".toCharArray();
			final String path = "caCertificateKeystore.jks";
			if (StringUtils.isNotBlank(path)) {
				final String keystorePath = "/cert/" + path;
				try {
					final KeyStoreManager ocspKeyStoreManager = new KeyStoreManager(keystorePath, pwd);
					keystore = ocspKeyStoreManager.getKeyStore();
				} catch (TechnicalConnectorException e) {
					CryptoFactory.LOG.info("Unable to load keystore.", e);
				}
			}
			if (keystore == null) {
				keystore = KeyStore.getInstance("JKS");
				keystore.load(null, password.toCharArray());
			}
			return keystore;
		} catch (Exception e2) {
			throw new TechnicalConnectorException();
		}
	}

	public static Crypto getCrypto(final Credential encryption, final Map<String, PrivateKey> decryptionKeys) throws TechnicalConnectorException {
		return getCrypto(encryption, decryptionKeys, "NONE");
	}

	private static CertStore generateCertStore(final String baseKey, final KeyStore... stores) {
		try {
			final Collection certsAndCrls = new ArrayList();
			for (final KeyStore store : stores) {
				try {
					final Enumeration<String> enumeration = store.aliases();
					while (enumeration.hasMoreElements()) {
						certsAndCrls.add(store.getCertificate(enumeration.nextElement()));
					}
					CryptoFactory.LOG.info("Added truststore in CertStore.");
				} catch (KeyStoreException e) {
					CryptoFactory.LOG.warn("Unable to add truststore to CertStore", e);
				}
			}
			final CertificateFactory factory = CertificateFactory.getInstance("X.509");
			return CertStore.getInstance("Collection", new CollectionCertStoreParameters(certsAndCrls));
		} catch (CertificateException e3) {
			CryptoFactory.LOG.error(e3.getClass().getName() + ":" + e3.getMessage(), e3);
		} catch (InvalidAlgorithmParameterException e4) {
			CryptoFactory.LOG.error(e4.getClass().getName() + ":" + e4.getMessage(), e4);
		} catch (NoSuchAlgorithmException e5) {
			CryptoFactory.LOG.error(e5.getClass().getName() + ":" + e5.getMessage(), e5);
		}
		return null;
	}

	static {
		LOG = LoggerFactory.getLogger(CryptoFactory.class);
		mutex = new Object();
		CryptoFactory.helper = new ConfigurableFactoryHelper<Crypto>("crypto.classname", "be.ehealth.technicalconnector.service.etee.impl.CryptoImpl");
	}
}
