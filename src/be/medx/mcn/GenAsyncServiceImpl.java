package be.medx.mcn;

import java.util.concurrent.TimeUnit;

import javax.xml.soap.SOAPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.cin.nip.async.generic.Confirm;
import be.cin.nip.async.generic.ConfirmResponse;
import be.cin.nip.async.generic.Get;
import be.cin.nip.async.generic.GetResponse;
import be.cin.nip.async.generic.MsgResponse;
import be.cin.nip.async.generic.Post;
import be.cin.nip.async.generic.PostResponse;
import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;
import be.medx.soap.handlers.Duration;
import be.medx.soap.handlers.IncomingSecurityHandler;
import be.medx.soap.handlers.SAMLHolderOfKeyHandler;
import be.medx.soap.handlers.SOAPHeaderLoggerHandler;
import be.medx.soap.handlers.SchemaValidatorHandler;
import be.medx.soap.ws.GenericFeature;
import be.medx.soap.ws.GenericRequest;
import be.medx.soap.ws.GenericWsSender;
import be.medx.soap.ws.HandlerChain;
import be.medx.soap.ws.HandlerPosition;
import be.medx.soap.ws.WsAddressingHeader;
import be.medx.soap.ws.XOPFeature;
import be.medx.xml.JaxbContextFactory;

public class GenAsyncServiceImpl implements GenAsyncService {
	public static final String SERVICE_NAME = "serviceName";
	public static final String SESSION_VALIDATOR = "sessionValidator";
	protected static final String GENASYNC_XSD = "/mycarenet-genasync/XSD/mycarenet-genasync-v1.xsd";
	private static final String PROP_ENDPOINT_GENASYNC_FIRST_PART = "endpoint.genericasync.";
	private static final String PROP_VALIDATION_INCOMING_GENASYNC = "validation.incoming.message.genasync.";
	private static final String PROP_SECURITY_INCOMING_GENASYNC_CREATE_TTL = "security.incoming.message.genasync.timestamp.created.ttl.";
	private static final String PROP_SECURITY_INCOMING_GENASYNC_EXPIRES_TTL = "security.incoming.message.genasync.timestamp.expires.ttl.";
	private static final String PROP_SECURITY_OUTGOING_GENASYNC_TS = "security.outgoing.message.genasync.timestamp.";
	private static final String PROP_THRESHOLD_GENASYNC_FIRST_PART = "threshold.genericasync.";
	private static final String END_PART_V1 = ".v1";
	private static final int DEFAULT_THRESHOLD = 81920;
	private static final Logger LOG;

	private String serviceName;
	private int threshold;
	private String endpoint;
	private GenericWsSender genericWsSender;

	public GenAsyncServiceImpl() {
	}

	public GenAsyncServiceImpl(final String serviceName, final String endpoint, GenericWsSender genericWsSender) {
		this();
		this.serviceName = serviceName;
		this.endpoint = endpoint;
		this.genericWsSender = genericWsSender;
		this.threshold = 81920; // Some strange threshold.
	}

	@Override
	public PostResponse postRequest(final SAMLToken token, final Post request, final WsAddressingHeader header) throws TechnicalConnectorException {
		return this.invoke(token, request, header, PostResponse.class);
	}

	@Override
	public GetResponse getRequest(final SAMLToken token, final Get request, final WsAddressingHeader header) throws TechnicalConnectorException {
		return this.invoke(token, request, header, GetResponse.class);
	}

	@Override
	public ConfirmResponse confirmRequest(final SAMLToken token, final Confirm request, final WsAddressingHeader header) throws TechnicalConnectorException {
		return this.invoke(token, request, header, ConfirmResponse.class);
	}

	protected <T> T invoke(final SAMLToken token, final Object request, final WsAddressingHeader header, final Class<T> clazz) throws TechnicalConnectorException {
		try {
			final GenericRequest genReq = build(token, this.serviceName);
			genReq.setPayload(request, new GenericFeature[] { new XOPFeature(this.threshold) });
			genReq.setWSAddressing(header);
			return this.genericWsSender.send(genReq).asObject(clazz);
		} catch (SOAPException e) {
			throw new TechnicalConnectorException();
		}
	}

	public void bootstrap() {
		JaxbContextFactory.initJaxbContext(new Class[] { Confirm.class });
		JaxbContextFactory.initJaxbContext(new Class[] { ConfirmResponse.class });
		JaxbContextFactory.initJaxbContext(new Class[] { Get.class });
		JaxbContextFactory.initJaxbContext(new Class[] { GetResponse.class });
		JaxbContextFactory.initJaxbContext(new Class[] { Post.class });
		JaxbContextFactory.initJaxbContext(new Class[] { PostResponse.class });
		JaxbContextFactory.initJaxbContext(new Class[] { MsgResponse.class });
		GenAsyncServiceImpl.LOG.debug("bootstrapped GenAsyncServiceImpl");
	}

	protected GenericRequest build(final SAMLToken token, final String serviceName) throws TechnicalConnectorException {
		final GenericRequest request = new GenericRequest();
		request.setEndpoint(this.endpoint);
		final HandlerChain chain = new HandlerChain();
		chain.register(HandlerPosition.SECURITY, new SAMLHolderOfKeyHandler(token, getDuration("security.outgoing.message.genasync.timestamp.", serviceName, 30L)));
		chain.register(HandlerPosition.SECURITY, new IncomingSecurityHandler(getDuration("security.incoming.message.genasync.timestamp.created.ttl.", serviceName, 30L), getDuration("security.incoming.message.genasync.timestamp.expires.ttl.", serviceName, 30L)));
		chain.register(HandlerPosition.SECURITY, new SOAPHeaderLoggerHandler());
		chain.register(HandlerPosition.BEFORE, new SchemaValidatorHandler(2, new String[] { "/mycarenet-genasync/XSD/mycarenet-genasync-v1.xsd" }));
		request.setHandlerChain(chain);
		request.setDefaultHandlerChain();
		return request;
	}

	private static Duration getDuration(final String startKey, final String serviceName, final long defaultDurationInSeconds) throws TechnicalConnectorException {
		return new Duration(defaultDurationInSeconds, TimeUnit.SECONDS);
	}

	static {
		LOG = LoggerFactory.getLogger(GenAsyncServiceImpl.class);
	}
}
