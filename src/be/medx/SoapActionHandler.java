package be.medx;


import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;

import org.apache.commons.lang.ArrayUtils;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;

public class SoapActionHandler extends AbstractSOAPHandler
{
    private static final Logger LOG;
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        try {
            boolean hasSoapAction = false;
            if (context.containsKey("javax.xml.ws.soap.http.soapaction.use")) {
                hasSoapAction = (Boolean)context.get("javax.xml.ws.soap.http.soapaction.use");
            }
            if (hasSoapAction) {
                final String soapAction = (String)context.get("javax.xml.ws.soap.http.soapaction.uri");
                SoapActionHandler.LOG.debug("Adding SOAPAction to mimeheader");
                final SOAPMessage msg = context.getMessage();
                final String[] headers = msg.getMimeHeaders().getHeader("SOAPAction");
                if (headers != null) {
                    SoapActionHandler.LOG.warn("Removing SOAPAction with values: " + ArrayUtils.toString((Object)headers));
                    msg.getMimeHeaders().removeHeader("SOAPAction");
                }
                msg.getMimeHeaders().addHeader("SOAPAction", soapAction);
                msg.saveChanges();
            }
            return true;
        }
        catch (SOAPException e) {
            throw new RuntimeException("WSSecurity problem: [SOAPACTION]" + e.getMessage(), e);
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(SoapActionHandler.class);
    }
}
