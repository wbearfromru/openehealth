package be.medx.mcn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorIOUtils;

public final class BuilderUtils {
	private static Logger logger;

	public static byte[] checkAndDecompress(final byte[] content, final String contentEncoding, final byte[] blobHashValue, final boolean hashTagRequired) throws TechnicalConnectorException {
		if (content == null || ArrayUtils.isEmpty(content)) {
			throw new TechnicalConnectorException();
		}
		final byte[] decompressedBlob = decompressBlob(content, contentEncoding);
		if (blobHashValue != null && blobHashValue.length > 0) {
			checkHash(blobHashValue, decompressedBlob);
		} else if (hashTagRequired) {
			throw new TechnicalConnectorException();
		}
		return decompressedBlob;
	}

	public static void checkHash(final byte[] blobHashValue, final byte[] decompressedBlob) throws TechnicalConnectorException {
		try {
			final byte[] calculatedHashValue = buildHash(decompressedBlob);
			if (!Arrays.areEqual(blobHashValue, calculatedHashValue)) {
				final String blobHashAsString = (blobHashValue != null) ? new String(Base64.encode(blobHashValue)) : "";
				final String calculatedHashAsString = (calculatedHashValue != null) ? new String(Base64.encode(calculatedHashValue)) : "";
				throw new TechnicalConnectorException();
			}
		} catch (NoSuchAlgorithmException e) {
			throw new TechnicalConnectorException();
		}
	}

	public static byte[] decompressBlob(byte[] decompressedBlob, final String contentEncoding) {
		if ("none".equals(contentEncoding)) {
			BuilderUtils.logger.warn("decompressBlob called with on blob with contentEncoding " + contentEncoding + " : decompress will be skipped!");
		} else {
			try {
				decompressedBlob = ConnectorIOUtils.decompress(decompressedBlob);
				if (!contentEncoding.equals("deflate")) {
					BuilderUtils.logger.warn("Blob was flagged as not deflated but was.");
				}
			} catch (TechnicalConnectorException e) {
				if (contentEncoding.equals("deflate")) {
					BuilderUtils.logger.warn("Blob was flagged as deflated but wasn't.");
				}
			}
		}
		return decompressedBlob;
	}

	public static byte[] buildHash(final byte[] decompressedBlob) throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(decompressedBlob);
		return md.digest();
	}

	static {
		BuilderUtils.logger = LoggerFactory.getLogger(BuilderUtils.class);
	}
}
