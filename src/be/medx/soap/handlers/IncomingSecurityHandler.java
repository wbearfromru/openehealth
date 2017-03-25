package be.medx.soap.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IncomingSecurityHandler extends AbstractSOAPHandler {
	private static final Logger LOG;
	private static final QName WSSE;
	private static final Set<QName> QNAME_LIST;
	private WSSConfig config;

	private IncomingSecurityHandler() {
		this.config = WSSConfig.getNewInstance();
	}

	public IncomingSecurityHandler(final Duration timestampTTL, final Duration timeStampFutureTTL) {
		this();
		this.config.setTimeStampTTL((int) timestampTTL.convert(TimeUnit.SECONDS));
		this.config.setTimeStampFutureTTL((int) timeStampFutureTTL.convert(TimeUnit.SECONDS));
	}

	@Override
	public boolean handleInbound(final SOAPMessageContext context) {
		final SOAPMessage message = context.getMessage();
		final WSSecurityEngine secEngine = new WSSecurityEngine();
		final RequestData requestData = new RequestData();
		requestData.setWssConfig(this.config);
		try {
			final SOAPHeader header = message.getSOAPHeader();
			if (header != null) {
				final NodeList list = header.getElementsByTagNameNS(IncomingSecurityHandler.WSSE.getNamespaceURI(), IncomingSecurityHandler.WSSE.getLocalPart());
				if (list != null) {
					IncomingSecurityHandler.LOG.debug("Verify WS Security Header");
					for (int j = 0; j < list.getLength(); ++j) {
						final List<WSSecurityEngineResult> results = secEngine.processSecurityHeader((Element) list.item(j), requestData);
						for (final WSSecurityEngineResult result : results) {
							if (result.get("validated-token") == Boolean.FALSE) {
								final StringBuffer sb = new StringBuffer();
								sb.append("Unable to validate incoming soap message. Action [");
								sb.append(result.get("action"));
								sb.append("].");
								throw new ProtocolException(sb.toString());
							}
						}
					}
				}
			}
		} catch (WSSecurityException e) {
			throw new ProtocolException(e);
		} catch (SOAPException e2) {
			throw new ProtocolException(e2);
		}
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		return IncomingSecurityHandler.QNAME_LIST;
	}

	static {
		LOG = LoggerFactory.getLogger(IncomingSecurityHandler.class);
		WSSE = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
		(QNAME_LIST = new HashSet<QName>()).add(IncomingSecurityHandler.WSSE);
	}
}
