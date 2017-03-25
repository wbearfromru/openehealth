package be.medx.soap.handlers;

import java.util.Map;

import org.slf4j.LoggerFactory;

import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPElement;

import java.util.Iterator;

import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;

import org.slf4j.Logger;

import be.medx.soap.ws.WsAddressingHeader;
import be.medx.soap.ws.WsAddressingRelatesTo;

public class WsAddressingHandlerV200508 extends AbstractSOAPHandler
{
    private static final Logger LOG;
    public static final String WS_ADDRESSING_V200508_USE = "be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508.use";
    public static final String MESSAGECONTEXT_WS_ADDRESSING_V200508 = "be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508";
    private static final String NAMESPACE = "http://www.w3.org/2005/08/addressing";
    private static final String WSA_PREFIX = "wsa";
    private static final QName MESSAGEID;
    private static final QName RELATESTO;
    private static final QName RELATIONSHIPTYPE;
    private static final QName TO;
    private static final QName ACTION;
    private static final QName FROM;
    private static final QName REPLYTO;
    private static final QName MUST_UNDERSTAND;
    private static final QName ADDRESS;
    private static final QName FAULTTO;
    
    @Override
    public boolean handleOutbound(final SOAPMessageContext context) {
        final Boolean wsAddressingUse = context.get("be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508.use") == null ? Boolean.FALSE : (Boolean)context.get("be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508.use");
        if (wsAddressingUse) {
            try {
                final WsAddressingHeader header = (WsAddressingHeader)context.get("be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508");
                if (header == null) {
                    WsAddressingHandlerV200508.LOG.warn("No WsAddressingHeader in the requestMap. Skipping the WsAddressingHandler.");
                    return true;
                }
                final SOAPHeader soapHeader = this.getSOAPHeader(context);
                this.processRequiredElements(header, soapHeader);
                this.processOptionalElements(header, soapHeader);
                context.getMessage().saveChanges();
            }
            catch (SOAPException e) {
                WsAddressingHandlerV200508.LOG.error("Error while generating WS-Addressing header", (Throwable)e);
            }
        }
        else {
            WsAddressingHandlerV200508.LOG.warn("WsAddressingHandler is configured but be.ehealth.technicalconnector.handler.WsAddressingHandlerV200508.useproperty was not present or set to FALSE.");
        }
        return true;
    }
    
    private void processOptionalElements(final WsAddressingHeader header, final SOAPHeader soapHeader) throws SOAPException {
        if (header.getTo() != null) {
            soapHeader.addChildElement(WsAddressingHandlerV200508.TO).setTextContent(header.getTo().toString());
        }
        if (header.getMessageID() != null) {
            soapHeader.addChildElement(WsAddressingHandlerV200508.MESSAGEID).setTextContent(header.getMessageID().toString());
        }
        for (final WsAddressingRelatesTo relateTo : header.getRelatesTo()) {
            this.generateRelateToElement(soapHeader, relateTo);
        }
        if (header.getFrom() != null && !header.getFrom().isEmpty()) {
            soapHeader.addChildElement(WsAddressingHandlerV200508.FROM).setTextContent(header.getFrom().toString());
        }
        if (header.getReplyTo() != null && !header.getReplyTo().isEmpty()) {
            soapHeader.addChildElement(WsAddressingHandlerV200508.REPLYTO).addChildElement(WsAddressingHandlerV200508.ADDRESS).setTextContent(header.getReplyTo().toString());
        }
        if (header.getFaultTo() != null && !header.getFaultTo().isEmpty()) {
            soapHeader.addChildElement(WsAddressingHandlerV200508.FAULTTO).addChildElement(WsAddressingHandlerV200508.ADDRESS).setTextContent(header.getFaultTo().toString());
        }
    }
    
    private void generateRelateToElement(final SOAPHeader soapHeader, final WsAddressingRelatesTo relateTo) throws SOAPException {
        final SOAPElement relateToElement = soapHeader.addChildElement(WsAddressingHandlerV200508.RELATESTO);
        if (relateTo.getRelationshipType() != null && !relateTo.getRelationshipType().isEmpty()) {
            relateToElement.addAttribute(WsAddressingHandlerV200508.RELATIONSHIPTYPE, relateTo.getRelationshipType());
        }
        if (relateTo.getRelationshipType() != null) {
            relateToElement.setTextContent(relateTo.getReleatesTo().toString());
        }
    }
    
    private void processRequiredElements(final WsAddressingHeader header, final SOAPHeader soapHeader) throws SOAPException {
        final SOAPElement actionElement = soapHeader.addChildElement(WsAddressingHandlerV200508.ACTION);
        actionElement.addAttribute(WsAddressingHandlerV200508.MUST_UNDERSTAND, header.getMustUnderstand());
        actionElement.setTextContent(header.getAction().toString());
    }
    
    private SOAPHeader getSOAPHeader(final SOAPMessageContext context) throws SOAPException {
        SOAPHeader soapHeader = context.getMessage().getSOAPHeader();
        if (soapHeader == null) {
            context.getMessage().getSOAPPart().getEnvelope().addHeader();
            soapHeader = context.getMessage().getSOAPHeader();
        }
        return soapHeader;
    }
    
    @Override
    public boolean handleFault(final SOAPMessageContext context) {
        this.handleMessage(context);
        return false;
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)WsAddressingHandlerV200508.class);
        MESSAGEID = new QName("http://www.w3.org/2005/08/addressing", "MessageID", "wsa");
        RELATESTO = new QName("http://www.w3.org/2005/08/addressing", "RelatesTo", "wsa");
        RELATIONSHIPTYPE = new QName("http://www.w3.org/2005/08/addressing", "RelationshipType", "wsa");
        TO = new QName("http://www.w3.org/2005/08/addressing", "To", "wsa");
        ACTION = new QName("http://www.w3.org/2005/08/addressing", "Action", "wsa");
        FROM = new QName("http://www.w3.org/2005/08/addressing", "From", "wsa");
        REPLYTO = new QName("http://www.w3.org/2005/08/addressing", "ReplyTo", "wsa");
        MUST_UNDERSTAND = new QName("http://schemas.xmlsoap.org/soap/envelope/", "mustUnderstand", "S");
        ADDRESS = new QName("http://www.w3.org/2005/08/addressing", "Address", "wsa");
        FAULTTO = new QName("http://www.w3.org/2005/08/addressing", "FaultTo", "wsa");
    }
}
