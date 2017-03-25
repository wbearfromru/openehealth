package be.medx.soap.handlers;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.MimeHeaders;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.util.Properties;

import org.slf4j.Logger;

public class UserAgentHandler extends AbstractSOAPHandler
{
    private static final String HEADER_NAME = "User-Agent";
    private static final Logger LOG;
    
    public UserAgentHandler() {
    }
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        if (context.getMessage() != null) {
            final MimeHeaders mimeHeaders = context.getMessage().getMimeHeaders();
            if (mimeHeaders != null) {
                final String[] agents = mimeHeaders.getHeader("User-Agent");
                if (ArrayUtils.isNotEmpty((Object[])agents)) {
                    UserAgentHandler.LOG.info("Removing MIME header [User-Agent] with value [" + StringUtils.join((Object[])agents, ",") + "]");
                    mimeHeaders.removeHeader("User-Agent");
                }
                final String value = "Open connector";
                UserAgentHandler.LOG.debug("Adding MIME header [User-Agent] with value [" + value + "]");
                mimeHeaders.addHeader("User-Agent", value);
            }
        }
        return true;
    }
    
    @Override
    public boolean handleFault(final SOAPMessageContext context) {
        return true;
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)UserAgentHandler.class);
    }
}
