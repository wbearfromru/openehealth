// 
// Decompiled by Procyon v0.5.29
// 

package be.medx;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.Marshaller;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

import java.io.Writer;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.lang.annotation.Annotation;

import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Document;

import javax.activation.DataHandler;

import java.util.HashMap;

import javax.xml.soap.AttachmentPart;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

public class MarshallerHelper<X, Y>
{
    private static final Logger LOG;
    public static final int DEFAULT_XOP_THRESHOLD = 10;
    private static DocumentBuilder documentBuilder;
    private AttachmentMarshallerImpl attachmentMarshaller;
    private Map<String, AttachmentPart> attachmentParts;
    private Class<Y> marshallClass;
    private Class<X> unmarshallClass;
    private boolean format;
    private boolean xop;
    private int threshold;
    
    public MarshallerHelper(final Class<X> unmarshallClass, final Class<Y> marshallClass) {
        this.attachmentParts = new HashMap<String, AttachmentPart>();
        this.createMarshaller(unmarshallClass, marshallClass, false, false, 10);
    }
    
    public MarshallerHelper(final Class<X> unmarshallClass, final Class<Y> marshallClass, final boolean format) {
        this.attachmentParts = new HashMap<String, AttachmentPart>();
        this.createMarshaller(unmarshallClass, marshallClass, format, false, 10);
    }
    
    public MarshallerHelper(final Class<X> unmarshallClass, final Class<Y> marshallClass, final boolean format, final boolean xop) {
        this.attachmentParts = new HashMap<String, AttachmentPart>();
        this.createMarshaller(unmarshallClass, marshallClass, format, xop, 10);
    }
    
    public MarshallerHelper(final Class<X> unmarshallClass, final Class<Y> marshallClass, final boolean format, final boolean xop, final int threshold) {
        this.attachmentParts = new HashMap<String, AttachmentPart>();
        this.createMarshaller(unmarshallClass, marshallClass, format, xop, threshold);
    }
    
    public void addAttachmentPart(final String id, final AttachmentPart attachmentPart) {
        this.attachmentParts.put(id, attachmentPart);
    }
    
    public void clearAttachmentPartMap() {
        this.attachmentParts.clear();
    }
    
    private void createMarshaller(final Class<X> inUnmarshallClass, final Class<Y> inMarshallClass, final Boolean format, final Boolean xop, final int threshold) {
        this.format = format;
        this.unmarshallClass = inUnmarshallClass;
        this.marshallClass = inMarshallClass;
        this.xop = xop;
        this.threshold = threshold;
    }
    
    public Map<String, DataHandler> getDataHandlersMap() {
        return this.attachmentMarshaller.getDataHandlerMap();
    }
    
    public Document toDocument(final Y data) {
        try {
            final Document doc = MarshallerHelper.documentBuilder.newDocument();
            if (data.getClass().isAnnotationPresent(XmlRootElement.class)) {
                this.getMarshaller().marshal(data, doc);
            }
            else {
                final JAXBElement<Y> jaxbElement = new JAXBElement<Y>(translate(data.getClass()), this.marshallClass, data);
                this.getMarshaller().marshal(jaxbElement, doc);
            }
            return doc;
        }
        catch (JAXBException e) {
            throw handleException(e);
        }
    }
    
    public X toObject(final byte[] data) {
        try {
            return this.toObject(new ByteArrayInputStream(data));
        }
        catch (TechnicalConnectorException e) {
            return null;
        }
    }
    
    public X toObject(final InputStream inputStream) throws TechnicalConnectorException {
        try {
            final JAXBElement<X> root = this.getUnMarshaller().unmarshal(new StreamSource(inputStream), this.unmarshallClass);
            return root.getValue();
        }
        catch (JAXBException e) {
            throw handleException(e);
        }
        finally {
            ConnectorIOUtils.closeQuietly(inputStream);
        }
    }
    
    public X toObject(final Node source) {
        try {
            return (X)this.getUnMarshaller().unmarshal(source);
        }
        catch (JAXBException e) {
            try {
                MarshallerHelper.LOG.debug("Unable to unmarshall class from source.", (Throwable)e);
                return this.getUnMarshaller().unmarshal(source, this.unmarshallClass).getValue();
            }
            catch (JAXBException e2) {
                e2.setLinkedException(e);
                throw handleException(e2);
            }
        }
    }
    
    public X toObject(final String data) {
        try {
            return this.toObject(ConnectorIOUtils.toBytes(data, Charset.UTF_8));
        }
        catch (TechnicalConnectorException e) {
            MarshallerHelper.LOG.error(e.getMessage(), (Throwable)e);
            return null;
        }
    }
    
    @Deprecated
    public X toObjectNoRootElementRequired(final byte[] data) {
        return this.toObject(data);
    }
    
    public String toString(final Y data) {
        final StringWriter writer = new StringWriter();
        try {
            if (data.getClass().isAnnotationPresent(XmlRootElement.class)) {
                this.getMarshaller().marshal(data, writer);
            }
            else {
                final JAXBElement<Y> jaxbElement = new JAXBElement<Y>(translate(data.getClass()), this.marshallClass, data);
                this.getMarshaller().marshal(jaxbElement, writer);
            }
        }
        catch (JAXBException e) {
            throw handleException(e);
        }
        return writer.toString();
    }
    
    private static QName translate(final Class<?> clazz) {
        for (final Annotation annotation : clazz.getPackage().getAnnotations()) {
            if (annotation instanceof XmlSchema) {
                final XmlSchema schema = (XmlSchema)annotation;
                return new QName(schema.namespace(), clazz.getSimpleName());
            }
        }
        MarshallerHelper.LOG.debug("Unable to determine QName for class:" + clazz + " using package as namespace.");
        return new QName(clazz.getPackage().getName(), clazz.getSimpleName());
    }
    
    @Deprecated
    public String toStringNoRootElementRequired(final Y data) {
        return this.toString(data);
    }
    
    public byte[] toXMLByteArray(final Y data) {
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            if (data.getClass().isAnnotationPresent(XmlRootElement.class)) {
                this.getMarshaller().marshal(data, bos);
            }
            else {
                final JAXBElement<Y> jaxbElement = new JAXBElement<Y>(translate(data.getClass()), this.marshallClass, data);
                this.getMarshaller().marshal(jaxbElement, bos);
            }
            return bos.toByteArray();
        }
        catch (JAXBException e) {
            throw handleException(e);
        }
        finally {
            ConnectorIOUtils.closeQuietly(bos);
        }
    }
    
    @Deprecated
    public byte[] toXMLByteArrayNoRootElementRequired(final Y data) {
        return this.toXMLByteArray(data);
    }
    
    @Deprecated
    public byte[] toXMLByteArrayNoRootElementRequired(final Y data, final QName rootTag) {
        return this.toXMLByteArrayNoRootElementRequired(data);
    }
    
    private static IllegalArgumentException handleException(final JAXBException e) {
        throw new IllegalArgumentException("Unable to (un)marchall class. Reason: " + e, e);
    }
    
    private Marshaller getMarshaller() throws JAXBException {
        this.attachmentMarshaller = new AttachmentMarshallerImpl(this.xop, this.threshold);
        final Marshaller marshaller = JaxbContextFactory.getJaxbContextForClass(this.marshallClass).createMarshaller();
        marshaller.setAttachmentMarshaller(this.attachmentMarshaller);
        marshaller.setProperty("jaxb.encoding", Charset.UTF_8.getName());
        marshaller.setProperty("jaxb.formatted.output", this.format);
        return marshaller;
    }
    
    private Unmarshaller getUnMarshaller() throws JAXBException {
        final AttachmentUnmarshallerImpl attachmentUnmarshaller = new AttachmentUnmarshallerImpl(true);
        attachmentUnmarshaller.getAttachmentPartMap().putAll(this.attachmentParts);
        final Unmarshaller unmarshaller = JaxbContextFactory.getJaxbContextForClass(this.unmarshallClass).createUnmarshaller();
        unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
        return unmarshaller;
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)MarshallerHelper.class);
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            MarshallerHelper.documentBuilder = dbf.newDocumentBuilder();
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
