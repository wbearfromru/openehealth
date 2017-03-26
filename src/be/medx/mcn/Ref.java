// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import org.apache.xml.security.algorithms.JCEMapper;
import org.etsi.uri._01903.v1_3.DigestAlgAndValueType;
import org.w3._2000._09.xmldsig.DigestMethod;

abstract class Ref {
	private static final String DIGEST_ALGO = "http://www.w3.org/2001/04/xmlenc#sha256";

	public String getDigestAlgUri() {
		return "http://www.w3.org/2001/04/xmlenc#sha256";
	}

	abstract byte[] getEncoded() throws Exception;

	public byte[] getDigestValue() {
		try {
			return ConnectorCryptoUtils.calculateDigest(JCEMapper.translateURItoJCEID("http://www.w3.org/2001/04/xmlenc#sha256"), this.getEncoded());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public DigestMethod getDigestMethod() {
		final DigestMethod method = new DigestMethod();
		method.setAlgorithm("http://www.w3.org/2001/04/xmlenc#sha256");
		return method;
	}

	public DigestAlgAndValueType getDigestAlgAndValue() {
		final DigestAlgAndValueType digestAlgAndValue = new DigestAlgAndValueType();
		digestAlgAndValue.setDigestMethod(this.getDigestMethod());
		digestAlgAndValue.setDigestValue(this.getDigestValue());
		return digestAlgAndValue;
	}
}
