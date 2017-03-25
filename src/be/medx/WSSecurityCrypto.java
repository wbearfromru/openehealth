package be.medx;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSSecurityCrypto implements Crypto {
	private static final Logger LOG;
	private final PrivateKey privateKey;
	private final X509Certificate certificate;

	public WSSecurityCrypto(final Credential cred) throws TechnicalConnectorException {
		this.privateKey = cred.getPrivateKey();
		this.certificate = cred.getCertificate();
	}

	public WSSecurityCrypto(final PrivateKey privateKey, final X509Certificate certificate) {
		this.privateKey = privateKey;
		this.certificate = certificate;
	}

	public byte[] getBytesFromCertificates(final X509Certificate[] certs) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getBytesFromCertificates");
		return null;
	}

	public CertificateFactory getCertificateFactory() throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getCertificateFactory");
		return null;
	}

	public X509Certificate[] getCertificatesFromBytes(final byte[] data) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getCertificatesFromBytes");
		return null;
	}

	public String getCryptoProvider() {
		WSSecurityCrypto.LOG.debug("getCryptoProvider");
		return null;
	}

	public String getDefaultX509Identifier() throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getDefaultX509Identifier");
		return null;
	}

	public PrivateKey getPrivateKey(final X509Certificate certificate, final CallbackHandler callbackHandler) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getPrivateKey(cert, callback)");
		return null;
	}

	public PrivateKey getPrivateKey(final String identifier, final String password) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getPrivateKey(identifier, password)");
		return this.privateKey;
	}

	public byte[] getSKIBytesFromCert(final X509Certificate cert) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getSKIBytesFromCert");
		return null;
	}

	public X509Certificate[] getX509Certificates(final CryptoType cryptoType) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getX509Certificates");
		final X509Certificate[] certificates = { this.certificate };
		return certificates;
	}

	public String getX509Identifier(final X509Certificate cert) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("getX509Identifier");
		return null;
	}

	public X509Certificate loadCertificate(final InputStream in) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("loadCertificate");
		return null;
	}

	public void setCertificateFactory(final String provider, final CertificateFactory certFactory) {
		WSSecurityCrypto.LOG.debug("setCertifiateFactory");
	}

	public void setCryptoProvider(final String provider) {
		WSSecurityCrypto.LOG.debug("setCryptoProvider");
	}

	public void setDefaultX509Identifier(final String identifier) {
		WSSecurityCrypto.LOG.debug("setDefaultX509Identifier");
	}

	public boolean verifyTrust(final X509Certificate[] certs) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("verifyTrust(certs)");
		return false;
	}

	public boolean verifyTrust(final X509Certificate[] certs, final boolean enableRevocation) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("verifyTrust(certs, enableRevocation)");
		return false;
	}

	public boolean verifyTrust(final PublicKey publicKey) throws WSSecurityException {
		WSSecurityCrypto.LOG.debug("verifyTrust(publicKey)");
		return false;
	}

	static {
		LOG = LoggerFactory.getLogger((Class) WSSecurityCrypto.class);
	}
}
