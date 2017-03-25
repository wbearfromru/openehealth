package be.medx;

import java.util.Map;
import org.slf4j.LoggerFactory;
import javax.xml.ws.handler.MessageContext;
import java.util.concurrent.TimeUnit;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;

public class ConnectionTimeOutHandler extends AbstractSOAPHandler
{
    private static final Logger LOG;
    private static final String REQUEST_TIMEOUT = "com.sun.xml.internal.ws.request.timeout";
    private static final String CONNECT_TIMEOUT = "com.sun.xml.internal.ws.connect.timeout";
    public static final String REQUEST_TIMEOUT_PROP = "connector.soaphandler.connection.request.timeout";
    public static final String CONNECT_TIMEOUT_PROP = "connector.soaphandler.connection.connection.timeout";
    private static final String DEFAULT_TIME_OUT = "30000";
    
    public ConnectionTimeOutHandler() {
    }
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        final String requestTimeOut = this.getDuration(REQUEST_TIMEOUT_PROP);
        ConnectionTimeOutHandler.LOG.debug("Setting request timeout on: {} milliseconds.", requestTimeOut);
        context.put(REQUEST_TIMEOUT, requestTimeOut);
        context.put(CONNECT_TIMEOUT, requestTimeOut);
        context.put(REQUEST_TIMEOUT_PROP, requestTimeOut);
        context.put(CONNECT_TIMEOUT_PROP, requestTimeOut);
        return true;
    }
    
    private String getDuration(final String requestTimeoutProp) {
        return DEFAULT_TIME_OUT;
    }
    
    @Override
    public boolean handleFault(final SOAPMessageContext context) {
        return this.handleMessage(context);
    }
    
    static {
        LOG = LoggerFactory.getLogger(ConnectionTimeOutHandler.class);
    }
}
