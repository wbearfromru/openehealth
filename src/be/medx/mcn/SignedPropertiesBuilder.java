// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.etsi.uri._01903.v1_3.SignedProperties;
import org.etsi.uri._01903.v1_3.SignedSignatureProperties;
import org.etsi.uri._01903.v1_3.SigningCertificate;
import org.joda.time.DateTime;
import org.w3c.dom.Document;

import be.medx.xml.MarshallerHelper;

public class SignedPropertiesBuilder {
	private static MarshallerHelper<SignedProperties, SignedProperties> marshaller;
	private DateTime signingTime;
	private String id;
	private List<CertRef> signingCertRefs;

	public SignedPropertiesBuilder() {
		this.signingCertRefs = new ArrayList<CertRef>();
	}

	public void setSigningTime(final DateTime signingTime) {
		this.signingTime = signingTime;
	}

	public String getId() {
		return "xmldsig-" + this.id + "-xades-signedprops";
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setSigningCert(final X509Certificate cert) {
		this.signingCertRefs.add(new CertRef(cert));
	}

	public SignedProperties build() {
		final SignedProperties signedProperties = new SignedProperties();
		signedProperties.setId(this.getId());
		final SignedSignatureProperties signedSignatureProperties = new SignedSignatureProperties();
		final SigningCertificate signingCert = new SigningCertificate();
		for (final CertRef signingCertRef : this.signingCertRefs) {
			signingCert.getCerts().add(signingCertRef.convertToCertID());
		}
		signedSignatureProperties.setSigningCertificate(signingCert);
		signedSignatureProperties.setSigningTime(this.signingTime);
		signedProperties.setSignedSignatureProperties(signedSignatureProperties);
		return signedProperties;
	}

	public Document buildAsDocument() {
		return SignedPropertiesBuilder.marshaller.toDocument(this.build());
	}

	static {
		SignedPropertiesBuilder.marshaller = new MarshallerHelper(SignedProperties.class, SignedProperties.class);
	}
}
