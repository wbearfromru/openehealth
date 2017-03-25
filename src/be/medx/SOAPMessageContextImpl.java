package be.medx;

import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashMap;

public final class SOAPMessageContextImpl extends HashMap<String, Object> implements SOAPMessageContext
{
    private static final long serialVersionUID = 1L;
    private transient SOAPMessage soapMessage;
    
    public SOAPMessageContextImpl(final SOAPMessage soapMessage) {
        this.setMessage(soapMessage);
    }
    
    @Override
    public SOAPMessage getMessage() {
        return this.soapMessage;
    }
    
    @Override
    public void setMessage(final SOAPMessage paramSOAPMessage) {
        this.soapMessage = paramSOAPMessage;
    }
    
    @Override
    public void setScope(final String paramString, final MessageContext.Scope paramScope) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MessageContext.Scope getScope(final String paramString) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object[] getHeaders(final QName paramQName, final JAXBContext paramJAXBContext, final boolean paramBoolean) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<String> getRoles() {
        throw new UnsupportedOperationException();
    }
}
