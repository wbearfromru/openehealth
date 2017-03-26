package be.medx.mcn;

import java.security.cert.X509Certificate;

import org.apache.xml.security.utils.RFC2253Parser;
import org.etsi.uri._01903.v1_3.CertIDType;
import org.w3._2000._09.xmldsig.X509IssuerSerialType;

class CertRef extends Ref {
	private X509Certificate cert;

	CertRef(final X509Certificate cert) {
		this.cert = cert;
	}

	@Override
	byte[] getEncoded() throws Exception {
		return this.cert.getEncoded();
	}

	public CertIDType convertToCertID() {
		final CertIDType certId = new CertIDType();
		certId.setCertDigest(this.getDigestAlgAndValue());
		final X509IssuerSerialType x509IssuerSerial = new X509IssuerSerialType();
		x509IssuerSerial.setX509IssuerName(RFC2253Parser.normalize(this.cert.getIssuerX500Principal().getName()));
		x509IssuerSerial.setX509SerialNumber(this.cert.getSerialNumber());
		certId.setIssuerSerial(x509IssuerSerial);
		return certId;
	}
}
