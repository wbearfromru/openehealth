package be.medx.soap.ws;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;

import java.util.Iterator;

import javax.xml.soap.AttachmentPart;

import java.lang.annotation.Annotation;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorXmlUtils;
import be.medx.xml.MarshallerHelper;

import javax.xml.soap.SOAPMessage;

public class GenericResponse
{
    private SOAPMessage message;
    
    public GenericResponse(final SOAPMessage message) {
        this.message = message;
    }
    
    public Node asNode() throws SOAPException {
        return this.getFirstChildElement();
    }
    
    public String asString() throws TechnicalConnectorException, SOAPException {
        final Node response = this.getFirstChildElement();
        if (response != null) {
            return ConnectorXmlUtils.toString(response);
        }
        return "";
    }
    
    public SOAPMessage getSOAPMessage() {
        return this.message;
    }
    
    public <T> T asObject(final Class<T> clazz) throws SOAPException {
        if (!clazz.isAnnotationPresent(XmlRootElement.class)) {
            throw new IllegalArgumentException("Class [" + clazz + "] is not annotated with @XMLRootElement");
        }
        this.getSOAPException();
        final MarshallerHelper<T, T> helper = new MarshallerHelper<T, T>(clazz, clazz);
        helper.clearAttachmentPartMap();
        final Iterator<AttachmentPart> attachmentPartIterator = (Iterator<AttachmentPart>)this.message.getAttachments();
        while (attachmentPartIterator.hasNext()) {
            final AttachmentPart element = attachmentPartIterator.next();
            helper.addAttachmentPart(this.getAttachmentPartId(element), element);
        }
        return helper.toObject(this.getFirstChildElement());
    }
    
    private String getAttachmentPartId(final AttachmentPart element) {
        return "cid:" + StringUtils.substringBetween(element.getContentId(), "<", ">");
    }
    
    public byte[] getAttachment(final String cid) throws SOAPException {
        final Iterator<AttachmentPart> attachmentPartIterator = (Iterator<AttachmentPart>)this.message.getAttachments();
        while (attachmentPartIterator.hasNext()) {
            final AttachmentPart element = attachmentPartIterator.next();
            if (StringUtils.equals(cid, this.getAttachmentPartId(element))) {
                return element.getRawContentBytes();
            }
        }
        throw new SOAPException("Unable to find attachment with id [" + cid + "]");
    }
    
    public Source asSource() throws SOAPException {
        return new DOMSource(this.getFirstChildElement());
    }
    
    private Element getFirstChildElement() throws SOAPException {
        this.getSOAPException();
        final Node n = this.message.getSOAPPart().getEnvelope().getBody();
        return ConnectorXmlUtils.getFirstChildElement(n);
    }
    
    public void getSOAPException() throws SOAPException {
        if (this.message == null || this.message.getSOAPBody() == null) {
            throw new SOAPException("No message SOAPmessage recieved");
        }
        final SOAPFault fault = this.message.getSOAPBody().getFault();
        if (fault != null) {
            //GenericResponse.LOG.error("SOAPFault: " + ConnectorXmlUtils.flatten(ConnectorXmlUtils.toString(fault)));
            throw new SOAPFaultException(fault);
        }
    }
}
