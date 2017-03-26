package be.medx.mcn;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.etsi.uri._01903.v1_3.CRLRefsType;
import org.etsi.uri._01903.v1_3.CRLValuesType;
import org.etsi.uri._01903.v1_3.CompleteCertificateRefsType;
import org.etsi.uri._01903.v1_3.CompleteRevocationRefsType;
import org.etsi.uri._01903.v1_3.EncapsulatedPKIData;
import org.etsi.uri._01903.v1_3.OCSPRefsType;
import org.etsi.uri._01903.v1_3.OCSPValuesType;
import org.etsi.uri._01903.v1_3.RevocationValuesType;
import org.etsi.uri._01903.v1_3.SigningCertificate;
import org.etsi.uri._01903.v1_3.UnsignedProperties;
import org.etsi.uri._01903.v1_3.UnsignedSignatureProperties;
import org.etsi.uri._01903.v1_3.XAdESTimeStampType;
import org.w3._2000._09.xmldsig.CanonicalizationMethod;
import org.w3c.dom.Document;

import be.medx.xml.MarshallerHelper;

public class UnsignedPropertiesBuilder {
	private static MarshallerHelper<UnsignedProperties, UnsignedProperties> marshaller;
	private String id;
	private List<XAdESTimeStampType> signatureTimestamps;
	private List<CertRef> completeCertRefs;
	private List<CrlRef> crlRefs;
	private List<OcspRef> ocspRefs;

	public UnsignedPropertiesBuilder() {
		this.signatureTimestamps = new ArrayList<XAdESTimeStampType>();
		this.completeCertRefs = new ArrayList<CertRef>();
		this.crlRefs = new ArrayList<CrlRef>();
		this.ocspRefs = new ArrayList<OcspRef>();
	}

	public String getId() {
		return "xmldsig-" + this.id + "-xades-unsignedprops";
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void addSignatureTimestamp(final byte[] tsToken, final String c14nMethod) {
		final XAdESTimeStampType timestamp = new XAdESTimeStampType();
		final CanonicalizationMethod method = new CanonicalizationMethod();
		method.setAlgorithm(c14nMethod);
		timestamp.setCanonicalizationMethod(method);
		final EncapsulatedPKIData encapsulatedTS = new EncapsulatedPKIData();
		encapsulatedTS.setValue(tsToken);
		timestamp.getEncapsulatedTimeStampsAndXMLTimeStamps().add(encapsulatedTS);
		this.signatureTimestamps.add(timestamp);
	}

	public void addCertificate(final X509Certificate cert) {
		this.completeCertRefs.add(new CertRef(cert));
	}

	public void addOCSPRef(final byte[] oscpEncoded) {
		this.ocspRefs.add(new OcspRef(oscpEncoded));
	}

	public void addCrlRef(final X509CRL crl) {
		this.crlRefs.add(new CrlRef(crl));
	}

	public UnsignedProperties build() {
		if (this.completeCertRefs.isEmpty() && this.signatureTimestamps.isEmpty() && this.crlRefs.isEmpty() && this.ocspRefs.isEmpty()) {
			return null;
		}
		final UnsignedProperties unsignedProperties = new UnsignedProperties();
		unsignedProperties.setId(this.getId());
		final UnsignedSignatureProperties unsignedSignatureProperties = new UnsignedSignatureProperties();
		unsignedSignatureProperties.getSignatureTimeStamps().addAll(this.signatureTimestamps);
		unsignedProperties.setUnsignedSignatureProperties(unsignedSignatureProperties);
		if (!this.completeCertRefs.isEmpty()) {
			final SigningCertificate completeSigningCertRefs = new SigningCertificate();
			for (final CertRef ref : this.completeCertRefs) {
				completeSigningCertRefs.getCerts().add(ref.convertToCertID());
			}
			unsignedSignatureProperties.setCompleteCertificateRefs(new CompleteCertificateRefsType());
			unsignedSignatureProperties.getCompleteCertificateRefs().setCertRefs(completeSigningCertRefs);
		}
		if (!this.crlRefs.isEmpty() || !this.ocspRefs.isEmpty()) {
			unsignedSignatureProperties.setCompleteRevocationRefs(new CompleteRevocationRefsType());
			unsignedSignatureProperties.setRevocationValues(new RevocationValuesType());
			if (!this.crlRefs.isEmpty()) {
				final CRLRefsType crlRefType = new CRLRefsType();
				final CRLValuesType crlValueType = new CRLValuesType();
				for (final CrlRef ref2 : this.crlRefs) {
					crlRefType.getCRLReves().add(ref2.convertToXadesCRLRef());
					crlValueType.getEncapsulatedCRLValues().add(ref2.convertToXadesEncapsulatedPKIData());
				}
				unsignedSignatureProperties.getRevocationValues().setCRLValues(crlValueType);
				unsignedSignatureProperties.getCompleteRevocationRefs().setCRLRefs(crlRefType);
			}
			if (!this.ocspRefs.isEmpty()) {
				final OCSPRefsType ocspRefsType = new OCSPRefsType();
				final OCSPValuesType ocspValueType = new OCSPValuesType();
				for (final OcspRef ref3 : this.ocspRefs) {
					ocspRefsType.getOCSPReves().add(ref3.convertToXadesOCSPRef());
					ocspValueType.getEncapsulatedOCSPValues().add(ref3.convertToXadesEncapsulatedPKIData());
				}
				unsignedSignatureProperties.getRevocationValues().setOCSPValues(ocspValueType);
				unsignedSignatureProperties.getCompleteRevocationRefs().setOCSPRefs(ocspRefsType);
			}
		}
		return unsignedProperties;
	}

	public Document buildAsDocument() {
		final UnsignedProperties unsignedProperties = this.build();
		if (unsignedProperties == null) {
			return null;
		}
		return UnsignedPropertiesBuilder.marshaller.toDocument(unsignedProperties);
	}

	static {
		UnsignedPropertiesBuilder.marshaller = new MarshallerHelper(UnsignedProperties.class, UnsignedProperties.class);
	}
}
