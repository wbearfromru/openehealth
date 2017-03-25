// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.soap.handlers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorXmlUtils;

public class SOAPHeaderLoggerHandler extends AbstractSOAPHandler {
	private static final Logger LOG;
	private static final String PROP_HEADER_LOGGER = "be.ehealth.technicalconnector.handler.SOAPHeaderLoggerHandler.";
	private List<String> propList;

	public SOAPHeaderLoggerHandler() {
		this.propList = Arrays.asList();
	}

	@Override
	public boolean handleMessage(final SOAPMessageContext ctx) {
		try {
			final SOAPHeader header = ctx.getMessage().getSOAPHeader();
			if (header != null) {
				final Iterator it = ctx.getMessage().getSOAPHeader().examineAllHeaderElements();
				while (it.hasNext()) {
					final Object obj = it.next();
					if (obj instanceof Element) {
						final Element el = (Element) obj;
						final String nameValue = "{" + el.getNamespaceURI() + "}" + el.getLocalName();
						if (!this.propList.contains(nameValue)) {
							continue;
						}
						SOAPHeaderLoggerHandler.LOG.info(ConnectorXmlUtils.toString(new DOMSource(el)));
					} else {
						SOAPHeaderLoggerHandler.LOG.error("Unsupported Object with name: [" + obj.getClass().getName() + "]");
					}
				}
			}
		} catch (SOAPException e) {
			SOAPHeaderLoggerHandler.LOG.error("SOAPException: " + e.getMessage(), e);
		} catch (TechnicalConnectorException e2) {
			SOAPHeaderLoggerHandler.LOG.error("TechnicalConnectorException: " + e2.getMessage(), e2);
		}
		return true;
	}

	@Override
	public boolean handleFault(final SOAPMessageContext ctx) {
		return this.handleMessage(ctx);
	}

	static {
		LOG = LoggerFactory.getLogger(SOAPHeaderLoggerHandler.class);
	}
}
