package be.medx.mcn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorIOUtils;

public final class ConnectorCryptoUtils {
	private static final Logger LOG;
	public static final String CONNECTORCRYPTO_ALGO_NAME_KEY = "be.ehealth.technicalconnector.utils.connectorcryptoutils.default_algo_name";
	public static final String CONNECTORCRYPTO_KEYSIZE_KEY = "be.ehealth.technicalconnector.utils.connectorcryptoutils.default_keysize";
	public static final String CONNECTORCRYPTO_MOCK_KEY = "be.ehealth.technicalconnector.utils.connectorcryptoutils.mock.desede";
	private static final String DEFAULT_ALGO_NAME = "AES";
	private static final int DEFAULT_KEYSIZE = 128;
	private static KeyGenerator keyGen;

	public static SecretKey generateKey() throws TechnicalConnectorException {
		return generateKey(128);
	}

	public static SecretKey generateKey(final int keySize) throws TechnicalConnectorException {
		return generateKey("AES", keySize);
	}

	public static SecretKey generateKey(final String algo, final int keySize) throws TechnicalConnectorException {
		try {
			if (ConnectorCryptoUtils.keyGen == null) {
				ConnectorCryptoUtils.keyGen = KeyGenerator.getInstance(algo);
			}
			ConnectorCryptoUtils.keyGen.init(keySize, new SecureRandom());
			return ConnectorCryptoUtils.keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new TechnicalConnectorException();
		}
	}

	public static byte[] decrypt(final Key key, final byte[] encryptedBytes) throws TechnicalConnectorException {
		return decrypt(key, key.getAlgorithm(), encryptedBytes);
	}

	public static byte[] decrypt(final Key key, final String algo, final byte[] encryptedBytes) throws TechnicalConnectorException {
		ByteArrayOutputStream baos = null;
		try {
			final Cipher cipher = Cipher.getInstance(algo, "BC");
			cipher.init(2, key);
			final int index = 0;
			final int blockSize = cipher.getBlockSize();
			baos = new ByteArrayOutputStream();
			try {
				decrypt(encryptedBytes, baos, index, blockSize, new SinglePartOperation(cipher));
			} catch (Exception e) {
				ConnectorCryptoUtils.LOG.debug("Not a SinglePart operation cipher. Trying MultiPartOperation. Reason [" + ExceptionUtils.getRootCauseMessage(e) + "]", e);
				baos.reset();
				decrypt(encryptedBytes, baos, index, blockSize, new MultiPartOperationDecryptor(cipher));
			}
			return baos.toByteArray();
		} catch (Exception e2) {
			throw new TechnicalConnectorException();
		} finally {
			ConnectorIOUtils.closeQuietly(baos);
		}
	}

	private static void decrypt(final byte[] encryptedBytes, final ByteArrayOutputStream baos, int index, final int blockSize, final Decryptor decryptor) throws IOException, IllegalBlockSizeException, BadPaddingException {
		if (blockSize == 0) {
			baos.write(decryptor.doFinal(encryptedBytes, 0, encryptedBytes.length));
		} else {
			while (index < encryptedBytes.length) {
				if (index + blockSize >= encryptedBytes.length) {
					baos.write(decryptor.doFinal(encryptedBytes, index, blockSize));
				} else {
					final byte[] blockResult = decryptor.update(encryptedBytes, index, blockSize);
					if (blockResult != null) {
						baos.write(blockResult);
					}
				}
				index += blockSize;
			}
		}
	}

	public static void setKeyGenerator(final KeyGenerator keyGenerator) {
		ConnectorCryptoUtils.keyGen = keyGenerator;
	}

	public static byte[] calculateDigest(final String digestAlgo, final byte[] content) throws TechnicalConnectorException {
		final String param = "Digest calculation failed for " + digestAlgo + ".";
		try {
			final MessageDigest md = MessageDigest.getInstance(digestAlgo);
			final InputStream fis = new ByteArrayInputStream(content);
			final byte[] dataBytes = new byte[1024];
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			return md.digest();
		} catch (Exception e) {
			throw new TechnicalConnectorException();
		}
	}

	static {
		LOG = LoggerFactory.getLogger(ConnectorCryptoUtils.class);
		Security.addProvider(new BouncyCastleProvider());
	}

	private static class SinglePartOperation implements Decryptor {
		private Cipher cipher;

		public SinglePartOperation(final Cipher cipher) {
			this.cipher = cipher;
		}

		@Override
		public byte[] update(final byte[] input, final int inputOffset, final int inputLen) throws IllegalBlockSizeException, BadPaddingException {
			return this.cipher.doFinal(input, inputOffset, inputLen);
		}

		@Override
		public byte[] doFinal(final byte[] input, final int inputOffset, final int inputLen) throws IllegalBlockSizeException, BadPaddingException {
			return this.cipher.doFinal(input, inputOffset, inputLen);
		}
	}

	private static class MultiPartOperationDecryptor implements Decryptor {
		private Cipher cipher;

		public MultiPartOperationDecryptor(final Cipher cipher) {
			this.cipher = cipher;
		}

		@Override
		public byte[] update(final byte[] input, final int inputOffset, final int inputLen) throws IllegalBlockSizeException, BadPaddingException {
			return this.cipher.update(input, inputOffset, inputLen);
		}

		@Override
		public byte[] doFinal(final byte[] input, final int inputOffset, final int inputLen) throws IllegalBlockSizeException, BadPaddingException {
			return this.cipher.doFinal(input, inputOffset, inputLen);
		}
	}

	private interface Decryptor {
		byte[] update(byte[] p0, int p1, int p2) throws IllegalBlockSizeException, BadPaddingException;

		byte[] doFinal(byte[] p0, int p1, int p2) throws IllegalBlockSizeException, BadPaddingException;
	}
}
