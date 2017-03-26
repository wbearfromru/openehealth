package be.medx.mcn;

import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;

import org.apache.xml.security.utils.RFC2253Parser;
import org.etsi.uri._01903.v1_3.CRLIdentifierType;
import org.etsi.uri._01903.v1_3.CRLRefType;
import org.etsi.uri._01903.v1_3.EncapsulatedPKIData;
import org.joda.time.DateTime;

class CrlRef extends Ref {
	private X509CRL crl;

	CrlRef(final X509CRL crl) {
		this.crl = crl;
	}

	@Override
	byte[] getEncoded() throws Exception {
		return this.crl.getEncoded();
	}

	private String getIssuerName() {
		return RFC2253Parser.normalize(this.crl.getIssuerDN().getName());
	}

	private DateTime getIssueTime() {
		return new DateTime(this.crl.getThisUpdate().getTime());
	}

	private BigInteger getIssuerNumber() {
		return BigInteger.valueOf(this.crl.getVersion());
	}

	public CRLRefType convertToXadesCRLRef() {
		final CRLRefType refType = new CRLRefType();
		refType.setDigestAlgAndValue(this.getDigestAlgAndValue());
		final CRLIdentifierType crlIdentifier = new CRLIdentifierType();
		crlIdentifier.setIssuer(this.getIssuerName());
		crlIdentifier.setIssueTime(this.getIssueTime());
		crlIdentifier.setNumber(this.getIssuerNumber());
		refType.setCRLIdentifier(crlIdentifier);
		return refType;
	}

	public EncapsulatedPKIData convertToXadesEncapsulatedPKIData() {
		final EncapsulatedPKIData data = new EncapsulatedPKIData();
		try {
			data.setValue(this.crl.getEncoded());
		} catch (CRLException e) {
			return null;
		}
		return data;
	}
}
