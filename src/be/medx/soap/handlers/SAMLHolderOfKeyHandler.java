package be.medx.soap.handlers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.ws.security.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.saml.SAMLToken;

public class SAMLHolderOfKeyHandler extends AbstractWsSecurityHandler {
	private static final Logger LOG;
	private SAMLToken token;
	private Duration duration;

	public SAMLHolderOfKeyHandler() {
		this(null);
	}

	public SAMLHolderOfKeyHandler(final SAMLToken token) {
		this(token, new Duration(60L, TimeUnit.SECONDS));
	}

	public SAMLHolderOfKeyHandler(final SAMLToken token, final Duration duration) {
		this.token = token;
		this.duration = duration;
	}

	@Override
	protected void addWSSecurity(final SOAPMessageContext context) throws IOException, WSSecurityException, TechnicalConnectorException {
		SAMLToken lazyToken = this.token;
		if (lazyToken == null) {
			throw new TechnicalConnectorException();
		}
		this.buildSignature().on(context.getMessage()).withTimeStamp(this.duration).withSAMLToken(lazyToken).sign(new AbstractWsSecurityHandler.SignedParts[] { AbstractWsSecurityHandler.SignedParts.TIMESTAMP });
	}

	@Override
	protected Logger getLogger() {
		return SAMLHolderOfKeyHandler.LOG;
	}

	static {
		LOG = LoggerFactory.getLogger(SAMLHolderOfKeyHandler.class);
	}
}
