package be.medx.utils;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.exceptions.TechnicalConnectorException;

public final class TimestampUtil {
	private static final Logger LOG;

	public static TimeStampResponse getTimestampResponse(final byte[] tsTokenResponse) throws TechnicalConnectorException {
		try {
			byte[] clonetsTokenResponse = ArrayUtils.clone(tsTokenResponse);
			clonetsTokenResponse = ConnectorIOUtils.base64Decode(clonetsTokenResponse, true);
			final TimeStampResponse tsResp = new TimeStampResponse(clonetsTokenResponse);
			if (tsResp.getTimeStampToken() == null) {
				throw new TSPException("no response for the RFC3161 token");
			}
			return tsResp;
		} catch (TSPException e) {
			TimestampUtil.LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new TechnicalConnectorException();
		} catch (IOException e2) {
			TimestampUtil.LOG.error(e2.getClass().getSimpleName() + ": " + e2.getMessage());
			throw new TechnicalConnectorException();
		}
	}

	public static TimeStampToken getTimestamp(final byte[] tsTokenResponse) throws TechnicalConnectorException {
		try {
			TimestampUtil.LOG.debug("Trying to generate unwrapped TimeStampToken");
			return getTimeStampToken(tsTokenResponse);
		} catch (TechnicalConnectorException e) {
			TimestampUtil.LOG.debug("Trying to generate wrapped TimeStampToken");
			return getTimestampResponse(tsTokenResponse).getTimeStampToken();
		}
	}

	public static TimeStampToken getTimeStampToken(final byte[] tsToken) throws TechnicalConnectorException {
		byte[] cloneTsToken = ArrayUtils.clone(tsToken);
		try {
			cloneTsToken = ConnectorIOUtils.base64Decode(cloneTsToken, true);
			return new TimeStampToken(new CMSSignedData(cloneTsToken));
		} catch (TSPException e) {
			TimestampUtil.LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new TechnicalConnectorException();
		} catch (IOException e2) {
			TimestampUtil.LOG.error(e2.getClass().getSimpleName() + ": " + e2.getMessage());
			throw new TechnicalConnectorException();
		} catch (CMSException e3) {
			TimestampUtil.LOG.error(e3.getClass().getSimpleName() + ": " + e3.getMessage());
			throw new TechnicalConnectorException();
		}
	}

	static {
		LOG = LoggerFactory.getLogger(TimestampUtil.class);
	}
}
