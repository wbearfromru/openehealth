// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.mcn;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.cin.nip.async.generic.GetResponse;
import be.cin.nip.async.generic.MsgResponse;
import be.cin.nip.async.generic.PostResponse;
import be.cin.nip.async.generic.TAck;
import be.cin.nip.async.generic.TAckResponse;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.services.CryptoService;
import be.medx.utils.ConnectorXmlUtils;

public class ResponseObjectBuilderImpl implements ResponseObjectBuilder {
	private static final Logger LOG;
	private CryptoService cryptoService;

	public ResponseObjectBuilderImpl(CryptoService cryptoService) {
		this.cryptoService = cryptoService;
	}

	@Override
	public final boolean handlePostResponse(final PostResponse postResponse) throws TechnicalConnectorException {
		if (postResponse == null || postResponse.getReturn() == null) {
			throw new be.medx.exceptions.TechnicalConnectorException();
		}
		final TAck tack = postResponse.getReturn();
		if (!tack.getResultMajor().equals("urn:nip:tack:result:major:success")) {
			throw new be.medx.exceptions.TechnicalConnectorException();
		}
		boolean hasWarning = false;
		if (tack.getResultMinor() != null && !tack.getResultMinor().isEmpty()) {
			hasWarning = true;
			ResponseObjectBuilderImpl.LOG.info("handlePostResponse : warning : " + tack.getResultMinor());
			ResponseObjectBuilderImpl.LOG.info("handlePostResponse : resultMessage  : " + tack.getResultMessage());
		}
		return hasWarning;
	}

	@Override
	public final Map<Object, SignatureVerificationResult> handleGetResponse(final GetResponse getResponse) throws TechnicalConnectorException {
		final Map<Object, SignatureVerificationResult> validationResult = new HashMap<Object, SignatureVerificationResult>();
		for (final TAckResponse value : getResponse.getReturn().getTAckResponses()) {
			ResponseObjectBuilderImpl.LOG.debug("handleGetResponse : tackResponse : xades : " + value.getXadesT() + ", tack : " + value.getTAck());
			validationResult.putAll(this.validateXadesT("TAckResponse", value, value.getXadesT().getValue()));
		}
		for (final MsgResponse msgResponse : getResponse.getReturn().getMsgResponses()) {
			if (msgResponse.getXadesT() != null) {
				validationResult.putAll(this.validateXadesT("MsgResponse", msgResponse, msgResponse.getXadesT().getValue()));
			}
		}
		if (!validationResult.isEmpty()) {
			if (ResponseObjectBuilderImpl.LOG.isDebugEnabled()) {
				this.logValidationResult(validationResult);
			}
			throw new TechnicalConnectorException();
		}
		return validationResult;
	}

	private void logValidationResult(final Map<Object, SignatureVerificationResult> validationResults) {
		ResponseObjectBuilderImpl.LOG.debug("validationResults : -------------------------");
		for (final Object key : validationResults.keySet()) {
			final SignatureVerificationResult signatureVerificationResult = validationResults.get(key);
			final StringBuilder errorsSb = new StringBuilder();
			for (final SignatureVerificationError error : signatureVerificationResult.getErrors()) {
				errorsSb.append(error).append(" ");
			}
			ResponseObjectBuilderImpl.LOG.debug("key : " + key + "\t" + " validationResult errors : " + errorsSb.toString());
		}
		ResponseObjectBuilderImpl.LOG.debug("--------------------------------------");
	}

	private Map<Object, SignatureVerificationResult> validateXadesT(final String localPart, final Object value, final byte[] xadesT) throws TechnicalConnectorException {
		final Map<Object, SignatureVerificationResult> vResult = new HashMap<Object, SignatureVerificationResult>();
		if (!ArrayUtils.isEmpty(xadesT)) {
			final byte[] signedByteArray = ConnectorXmlUtils.toByteArray(value);
			final Map<String, Object> options = new HashMap<String, Object>();
			SignatureBuilder signatureBuilder = new XmlSignatureBuilder(this.cryptoService, AdvancedElectronicSignatureEnumeration.XAdES_T, new XadesSpecification[] { new XadesBesSpecification(), new XadesTSpecification() });
			final SignatureVerificationResult result = signatureBuilder.verify(signedByteArray, xadesT, options);
			if (!result.isValid()) {
				vResult.put(value, result);
			}
		}
		return vResult;
	}

	public void initialize(final Map<String, Object> parameterMap) throws TechnicalConnectorException {
	}

	static {
		LOG = LoggerFactory.getLogger(ResponseObjectBuilderImpl.class);
	}
}
