package be.medx.mcn;

import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.ws.soap.SOAPFaultException;

import oasis.names.tc.dss._1_0.core.schema.DocumentHash;
import oasis.names.tc.dss._1_0.core.schema.InputDocuments;
import oasis.names.tc.dss._1_0.core.schema.SignRequest;
import oasis.names.tc.dss._1_0.core.schema.SignResponse;
import oasis.names.tc.dss._1_0.core.schema.Timestamp;

import org.apache.xml.security.algorithms.JCEMapper;
import org.w3._2000._09.xmldsig.DigestMethod;

import be.medx.crypto.Credential;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.services.CryptoService;
import be.medx.soap.ws.GenericRequest;
import be.medx.soap.ws.GenericWsSenderImpl;

public class TimeStampGeneratorImpl implements TimestampGenerator {
	private static final String ENDPOINT_TS_AUTHORITY_V2 = "endpoint.ts.authority.v2";
	private Map<String, Object> options;
	private CryptoService tokenService;
	private final String endpoint = "https://services-acpt.ehealth.fgov.be/TimestampAuthority/v2";

	public TimeStampGeneratorImpl(CryptoService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	public byte[] generate(final String requestId, final String digestAlgoUri, final byte[] digest) throws TechnicalConnectorException {
		final GenericRequest req = new GenericRequest();
		req.setPayload(this.generateSignRequest(requestId, digestAlgoUri, digest));
		final Credential cred = SignatureUtils.getOption("SignatureTimestampCredential", this.options, this.tokenService.getHOKCredential());
		if (cred == null) {
			throw new TechnicalConnectorException();
		}
		req.setCertificateSecured(cred.getCertificate(), cred.getPrivateKey());
		final String tsaEndpoint = SignatureUtils.getOption("SignatureTimestampEndpointTimestampAuthority", this.options, endpoint);
		if (tsaEndpoint == null || tsaEndpoint.isEmpty()) {
			throw new TechnicalConnectorException();
		}
		req.setEndpoint(tsaEndpoint);
		req.setSoapAction("urn:be:fgov:ehealth:timestamping:protocol:v2:stamp");
		req.setDefaultHandlerChain();
		SignResponse response = null;
		try {
			response = new GenericWsSenderImpl().send(req).asObject(SignResponse.class);
		} catch (SOAPFaultException e) {
			throw new TechnicalConnectorException();
		} catch (SOAPException e2) {
			throw new TechnicalConnectorException();
		}
		if (!"urn:oasis:names:tc:dss:1.0:resultmajor:Success".equals(response.getResult().getResultMajor())) {
			throw new TechnicalConnectorException();
		}
		final Timestamp ts = response.getSignatureObject().getTimestamp();
		if (ts.getOther() != null) {
			throw new UnsupportedOperationException("Only RFC3161 TimeStampToken is supported.");
		}
		return ts.getRFC3161TimeStampToken();
	}

	private SignRequest generateSignRequest(final String requestId, final String digestAlgoURI, final byte[] transformed) throws TechnicalConnectorException {
		final SignRequest request = new SignRequest();
		request.setRequestID(requestId);
		request.setProfile(SignatureUtils.getOption("SignatureTimestampProfile", this.options, "urn:ehealth:profiles:timestamping:2.1-cert"));
		final InputDocuments inputDocuments = new InputDocuments();
		final DocumentHash docHash = new DocumentHash();
		docHash.setDigestMethod(new DigestMethod());
		docHash.getDigestMethod().setAlgorithm(digestAlgoURI);
		docHash.setDigestValue(ConnectorCryptoUtils.calculateDigest(JCEMapper.translateURItoJCEID(digestAlgoURI), transformed));
		inputDocuments.getDocumentHash().add(docHash);
		request.setInputDocuments(inputDocuments);
		return request;
	}

	public void initialize(final Map<String, Object> parameterMap) throws TechnicalConnectorException {
		this.options = parameterMap;
	}
}
