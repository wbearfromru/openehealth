package be.medx;

import java.util.Map;
import org.w3c.dom.Node;
import org.slf4j.Logger;
import javax.xml.soap.SOAPMessage;
import java.util.HashSet;
import javax.xml.namespace.QName;
import java.util.Set;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

public abstract class AbstractSOAPHandler implements SOAPHandler<SOAPMessageContext>
{
    protected static final String IN = "IN";
    protected static final String OUT = "OUT";
    private static final int BLOCK = 1024;
    
    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
        if ((Boolean)context.get("javax.xml.ws.handler.message.outbound")) {
            return this.handleOutbound(context);
        }
        return this.handleInbound(context);
    }
    
    public boolean handleOutbound(final SOAPMessageContext context) {
        return true;
    }
    
    public boolean handleInbound(final SOAPMessageContext context) {
        return true;
    }
    
    @Override
    public boolean handleFault(final SOAPMessageContext context) {
        return false;
    }
    
    @Override
    public void close(final MessageContext context) {
    }
    
    @Override
    public Set<QName> getHeaders() {
        return new HashSet<QName>();
    }
    
    protected static void dumpMessage(final SOAPMessage msg, final String mode, final Logger log) {
        if (msg == null) {
            return;
        }
        try {
            final String content = ConnectorXmlUtils.toString(msg.getSOAPPart().getEnvelope());
            final int size = content.getBytes(Charset.UTF_8.getName()).length;
            if (content.getBytes().length < 1048576) {
                log.debug("[" + mode + "] - " + size + " bytes - " + content);
            }
            else {
                log.warn("[" + mode + "] - " + size + " bytes - " + "message to large to log");
            }
        }
        catch (Exception e) {
            log.debug("Unable to dump message", (Throwable)e);
        }
    }
}
