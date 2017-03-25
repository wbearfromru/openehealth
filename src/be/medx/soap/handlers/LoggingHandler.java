package be.medx.soap.handlers;

import java.util.Map;
import org.slf4j.LoggerFactory;
import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.lang.ArrayUtils;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;

public class LoggingHandler extends AbstractSOAPHandler
{
    private static final Logger LOG;
    static final String MESSAGE_ENDPOINT_ADDRESS = "javax.xml.ws.service.endpoint.address";
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        final SOAPMessage msg = context.getMessage();
        if (msg != null && LoggingHandler.LOG.isInfoEnabled()) {
            final String endPoint = (String)context.get("javax.xml.ws.service.endpoint.address");
            final String soapAction = ArrayUtils.toString((Object)msg.getMimeHeaders().getHeader("SOAPAction"));
            LoggingHandler.LOG.info("Invoking webservice on url: [" + endPoint + "] with SOAPAction(s) " + soapAction);
        }
        if (LoggingHandler.LOG.isDebugEnabled()) {
            AbstractSOAPHandler.dumpMessage(msg, "OUT", LoggingHandler.LOG);
        }
        return true;
    }
    
    @Override
    public boolean handleInbound(final SOAPMessageContext context) {
        final SOAPMessage msg = context.getMessage();
        if (LoggingHandler.LOG.isDebugEnabled()) {
            AbstractSOAPHandler.dumpMessage(msg, "IN", LoggingHandler.LOG);
        }
        return true;
    }
    
    @Override
    public boolean handleFault(final SOAPMessageContext c) {
        this.handleMessage(c);
        return true;
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)LoggingHandler.class);
    }
}
