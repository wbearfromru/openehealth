package be.medx.mcn;

import org.etsi.uri._01903.v1_3.QualifyingProperties;
import org.w3c.dom.Document;

import be.medx.xml.MarshallerHelper;

public class QualifyingPropertiesBuilder {
	private static MarshallerHelper<QualifyingProperties, QualifyingProperties> marshaller;
	private SignedPropertiesBuilder signedProps;
	private UnsignedPropertiesBuilder unsignedProps;

	public QualifyingPropertiesBuilder() {
		this.signedProps = new SignedPropertiesBuilder();
		this.unsignedProps = new UnsignedPropertiesBuilder();
	}

	public SignedPropertiesBuilder getSignedProps() {
		return this.signedProps;
	}

	public UnsignedPropertiesBuilder getUnsignedProps() {
		return this.unsignedProps;
	}

	public Document buildBeforeSigningAsDocument() {
		return QualifyingPropertiesBuilder.marshaller.toDocument(this.buildBeforeSigning());
	}

	private QualifyingProperties buildBeforeSigning() {
		final QualifyingProperties qualProps = new QualifyingProperties();
		qualProps.setSignedProperties(this.getSignedProps().build());
		return qualProps;
	}

	static {
		QualifyingPropertiesBuilder.marshaller = new MarshallerHelper(QualifyingProperties.class, QualifyingProperties.class);
	}
}
