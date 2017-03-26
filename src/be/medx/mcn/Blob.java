package be.medx.mcn;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.util.Arrays;

public class Blob {
	private byte[] content;
	private String contentType;
	private String messageName;
	private byte[] hashValue;
	private byte[] xadesValue;
	private String contentEncoding;
	private String id;
	private boolean isHashTagRequired;

	public Blob() {
		this.isHashTagRequired = true;
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setContent(final byte[] content) {
		this.content = Arrays.clone(content);
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public byte[] getHashValue() {
		return this.hashValue;
	}

	public void setHashValue(final byte[] hashValue) {
		this.hashValue = Arrays.clone(hashValue);
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getContentEncoding() {
		return this.contentEncoding;
	}

	public void setContentEncoding(final String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getMessageName() {
		return this.messageName;
	}

	public void setMessageName(final String messageName) {
		this.messageName = messageName;
	}

	public boolean isHashTagRequired() {
		return this.isHashTagRequired;
	}

	public void setHashTagRequired(final boolean isHashTagRequired) {
		this.isHashTagRequired = isHashTagRequired;
	}

	public byte[] getXadesValue() {
		return this.xadesValue;
	}

	public void setXadesValue(final byte[] xadesValue) {
		this.xadesValue = ArrayUtils.clone(xadesValue);
	}
}
