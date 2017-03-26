package be.medx.mcn;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xml.security.utils.RFC2253Parser;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RespID;
import org.etsi.uri._01903.v1_3.EncapsulatedPKIData;
import org.etsi.uri._01903.v1_3.OCSPIdentifierType;
import org.etsi.uri._01903.v1_3.OCSPRefType;
import org.etsi.uri._01903.v1_3.ResponderIDType;
import org.joda.time.DateTime;

class OcspRef extends Ref {
	private BasicOCSPResp ocsp;
	private byte[] ocspEncoded;

	OcspRef(final byte[] ocspEncoded) {
		this.ocspEncoded = ArrayUtils.clone(ocspEncoded);
		try {
			this.ocsp = (BasicOCSPResp) new OCSPResp(ocspEncoded).getResponseObject();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public byte[] getEncoded() throws Exception {
		return this.ocspEncoded;
	}

	public List<X509Certificate> getAssociatedCertificates() {
		final List<X509Certificate> result = new ArrayList<X509Certificate>();
		for (final X509CertificateHolder certificateHolder : this.ocsp.getCerts()) {
			try {
				result.add(new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder));
			} catch (CertificateException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private DateTime getProducedAt() {
		return new DateTime(this.ocsp.getProducedAt().getTime());
	}

	private String getResponderIdByName() {
		final RespID responderId = this.ocsp.getResponderId();
		final ResponderID responderIdAsASN1Object = responderId.toASN1Object();
		final DERTaggedObject derTaggedObject = (DERTaggedObject) responderIdAsASN1Object.toASN1Primitive();
		if (2 == derTaggedObject.getTagNo()) {
			return null;
		}
		final ASN1Primitive derObject = derTaggedObject.getObject();
		final X500Name name = X500Name.getInstance(derObject);
		return RFC2253Parser.normalize(name.toString());
	}

	private byte[] getResponderIdByKey() {
		final RespID responderId = this.ocsp.getResponderId();
		final ResponderID responderIdAsASN1Object = responderId.toASN1Object();
		final DERTaggedObject derTaggedObject = (DERTaggedObject) responderIdAsASN1Object.toASN1Primitive();
		if (2 == derTaggedObject.getTagNo()) {
			final ASN1OctetString keyHashOctetString = (ASN1OctetString) derTaggedObject.getObject();
			return keyHashOctetString.getOctets();
		}
		return null;
	}

	public OCSPRefType convertToXadesOCSPRef() {
		final OCSPRefType refType = new OCSPRefType();
		refType.setDigestAlgAndValue(this.getDigestAlgAndValue());
		final OCSPIdentifierType ocspIdentifier = new OCSPIdentifierType();
		refType.setOCSPIdentifier(ocspIdentifier);
		ocspIdentifier.setProducedAt(this.getProducedAt());
		final ResponderIDType responderId = new ResponderIDType();
		responderId.setByName(this.getResponderIdByName());
		responderId.setByKey(this.getResponderIdByKey());
		ocspIdentifier.setResponderID(responderId);
		return refType;
	}

	public EncapsulatedPKIData convertToXadesEncapsulatedPKIData() {
		final EncapsulatedPKIData data = new EncapsulatedPKIData();
		data.setValue(this.ocspEncoded);
		return data;
	}
}
