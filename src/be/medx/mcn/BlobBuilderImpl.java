// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorIOUtils;

public class BlobBuilderImpl implements BlobBuilder {
	private static final String PROPERTYBEGINNING = "mycarenet.blobbuilder.";
	private String projectName;

	public BlobBuilderImpl(String projectName) throws TechnicalConnectorException {
		// this.projectName = projectName;
		// Looks like default is always used for GMD
		this.projectName = "default";
	}

	@Override
	public Blob build(final byte[] input) throws TechnicalConnectorException {
		return this.build(input, "blob");
	}

	@Override
	public Blob build(final byte[] input, final String id) throws TechnicalConnectorException {
		return this.build(input, "deflate", id, "text/plain");
	}

	@Override
	public Blob build(final byte[] input, final String encodingType, final String id, final String contentType) throws TechnicalConnectorException {
		return this.build(input, encodingType, id, contentType, null);
	}

	@Override
	public Blob build(final byte[] input, final String encodingType, final String id, final String contentType, final String messageName) throws TechnicalConnectorException {
		if (input == null || input.length == 0) {
			throw new TechnicalConnectorException();
		}
		if (contentType == null || contentType.isEmpty()) {
			throw new TechnicalConnectorException();
		}
		final Blob newBlob = new Blob();
		newBlob.setContentEncoding("none");
		byte[] buff = input;
		if (encodingType.equals("deflate")) {
			newBlob.setContentEncoding(encodingType);
			buff = ConnectorIOUtils.compress(input, "deflate");
		}
		newBlob.setContent(buff);
		newBlob.setContentType(contentType);
		newBlob.setId(id);
		newBlob.setMessageName(messageName);
		newBlob.setHashValue(null);
		return newBlob;
	}

	@Override
	public byte[] checkAndRetrieveContent(final Blob blob) throws TechnicalConnectorException {
		if (blob == null) {
			throw new TechnicalConnectorException();
		}
		return BuilderUtils.checkAndDecompress(blob.getContent(), blob.getContentEncoding(), blob.getHashValue(), blob.isHashTagRequired());
	}

}
