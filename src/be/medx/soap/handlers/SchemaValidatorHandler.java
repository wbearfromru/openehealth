package be.medx.soap.handlers;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import be.medx.utils.SOAPFaultFactory;
import be.medx.utils.ValidatorHelper;

public class SchemaValidatorHandler extends AbstractSOAPHandler {
	private static final Logger LOG;
	private String[] schemaFiles;
	private int verify;
	public static final int VERIFY_INBOUND = 1;
	public static final int VERIFY_OUTBOUND = 2;
	public static final int VERIFY_BOTH = 3;

	public SchemaValidatorHandler(final int verifyType, final String... schemaFile) {
		validVerifyType(verifyType);
		Validate.notEmpty(schemaFile);
		Validate.noNullElements(schemaFile);
		this.verify = verifyType;
		this.schemaFiles = schemaFile;
	}

	@Override
	public boolean handleInbound(final SOAPMessageContext context) {
		if (this.verify == 3 || this.verify == 1) {
			SchemaValidatorHandler.LOG.info("Validating incoming message.");
			this.validate(context, "IN");
		}
		return true;
	}

	@Override
	public boolean handleOutbound(final SOAPMessageContext context) {
		if (this.verify == 3 || this.verify == 2) {
			SchemaValidatorHandler.LOG.info("Validating outgoing message.");
			this.validate(context, "OUT");
		}
		return true;
	}

	private void validate(final SOAPMessageContext context, final String mode) {
		try {
			final SOAPBody body = context.getMessage().getSOAPBody();
			final SOAPFault fault = body.getFault();
			if (fault != null) {
				return;
			}
			final Node payloadNode = body.getFirstChild();
			ValidatorHelper.validate(new DOMSource(payloadNode), this.isXOPEnabled(context), this.schemaFiles);
		} catch (Exception e) {
			AbstractSOAPHandler.dumpMessage(context.getMessage(), mode, SchemaValidatorHandler.LOG);
			SchemaValidatorHandler.LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			throw SOAPFaultFactory.newSOAPFaultException(e.getMessage(), e);
		}
		SchemaValidatorHandler.LOG.info("Message validation done.");
	}

	private boolean isXOPEnabled(final SOAPMessageContext context) {
		boolean xopEnabled = false;
		if (context.containsKey("http://www.w3.org/2004/08/soap/features/http-optimization")) {
			xopEnabled = (Boolean) context.get("http://www.w3.org/2004/08/soap/features/http-optimization");
		}
		return xopEnabled;
	}

	private static void validVerifyType(final int verifyType) {
		if (verifyType < 0 || verifyType > 3) {
			throw new IllegalArgumentException("Verify of type " + verifyType + " is not supported.");
		}
	}

	static {
		LOG = LoggerFactory.getLogger(SchemaValidatorHandler.class);
	}
}
