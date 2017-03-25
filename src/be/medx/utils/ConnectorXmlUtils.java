// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.utils;

import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Writer;
import java.io.StringWriter;
import java.io.Reader;

import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import java.io.OutputStream;

import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.slf4j.Logger;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.xml.MarshallerHelper;

public final class ConnectorXmlUtils
{
    private static final Logger LOG;
    private static final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";
    
    private ConnectorXmlUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static Element getFirstChildElement(final Node node) {
        Node child;
        for (child = node.getFirstChild(); child != null && child.getNodeType() != 1; child = child.getNextSibling()) {}
        if (child != null) {
            return (Element)child;
        }
        return null;
    }
    
    @Deprecated
    public static void logXmlObject(final Object obj) {
        dump(obj);
    }
    
    public static void dump(final Object obj) {
        if (ConnectorXmlUtils.LOG.isDebugEnabled()) {
            try {
                if (obj != null) {
                    final String xmlString = toString(obj);
                    ConnectorXmlUtils.LOG.debug("Contents of " + obj.getClass().getCanonicalName() + "  : " + xmlString + "");
                }
            }
            catch (Exception e) {
                ConnectorXmlUtils.LOG.error("Error occured while logging contents of object " + obj.getClass().getCanonicalName() + ". Reason: " + e.getMessage());
            }
        }
    }
    
    @Deprecated
    public static String marshal(final Object obj) {
        return toString(obj);
    }
    
    public static byte[] toByteArray(final Node node) {
        ByteArrayOutputStream out = null;
        try {
            final Source source = new DOMSource(node);
            out = new ByteArrayOutputStream();
            final Result result = new StreamResult(out);
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return out.toByteArray();
        }
        catch (TransformerConfigurationException e) {
            ConnectorXmlUtils.LOG.error(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
        catch (TransformerException e2) {
            ConnectorXmlUtils.LOG.error(e2.getClass().getSimpleName() + ":" + e2.getMessage());
        }
        finally {
            ConnectorIOUtils.closeQuietly(out);
        }
        return null;
    }
    
    public static byte[] toByteArray(final Object obj) {
        final MarshallerHelper marshallerHelper = new MarshallerHelper(obj.getClass(), obj.getClass());
        return marshallerHelper.toXMLByteArray(obj);
    }
    
    public static Document toDocument(final byte[] data) throws TechnicalConnectorException {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new ByteArrayInputStream(data));
        }
        catch (SAXException e) {
            throw new TechnicalConnectorException();
        }
        catch (ParserConfigurationException e2) {
            throw new TechnicalConnectorException();
        }
        catch (IOException e3) {
            throw new TechnicalConnectorException();
        }
    }
    
    public static Document toDocument(final Object obj) {
        final MarshallerHelper marshallerHelper = new MarshallerHelper(obj.getClass(), obj.getClass());
        return marshallerHelper.toDocument(obj);
    }
    
    public static Document toDocument(final String xml) throws TechnicalConnectorException {
        if (!isXMLLike(xml)) {
            throw new IllegalArgumentException("Parameter xml doesn't contains a well-formed xml");
        }
        try {
            final InputSource source = new InputSource(new StringReader(xml));
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            final DocumentBuilder builder = dbFactory.newDocumentBuilder();
            return builder.parse(source);
        }
        catch (IOException e) {
            throw new TechnicalConnectorException();
        }
        catch (ParserConfigurationException e2) {
            throw new TechnicalConnectorException();
        }
        catch (SAXException e3) {
            throw new TechnicalConnectorException();
        }
    }
    
    public static Element toElement(final byte[] data) throws TechnicalConnectorException {
        return toDocument(data).getDocumentElement();
    }
    
    public static String toString(final Object obj) {
        final MarshallerHelper marshallerHelper = new MarshallerHelper(obj.getClass(), obj.getClass());
        return marshallerHelper.toString(obj);
    }
    
    public static String toString(final Node node) throws TechnicalConnectorException {
        return toString(new DOMSource(node));
    }
    
    public static String toString(final Source source) throws TechnicalConnectorException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final TransformerFactory tff = TransformerFactory.newInstance();
            final Transformer tf = tff.newTransformer();
            tf.setOutputProperty("omit-xml-declaration", "yes");
            final Result result = new StreamResult(outputStream);
            tf.transform(source, result);
            return new String(outputStream.toByteArray(), "UTF-8");
        }
        catch (Exception e) {
            throw new TechnicalConnectorException();
        }
        finally {
            ConnectorIOUtils.closeQuietly(outputStream);
        }
    }
    
    public static String flatten(final String xml) {
        String result;
        for (result = xml.replaceAll("[\t\n\r]", ""); result.contains(" <"); result = result.replace(" <", "<")) {}
        return result;
    }
    
    public static String format(final String unformattedXml) {
        return format(unformattedXml, null);
    }
    
    public static String format(final String unformattedXml, final Source xslt) {
        if (!isXMLLike(unformattedXml)) {
            return unformattedXml;
        }
        try {
            final Document doc = parseXmlFile(unformattedXml);
            final DOMSource domSource = new DOMSource(doc);
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);
            final TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = null;
            if (xslt != null) {
                transformer = tf.newTransformer(xslt);
            }
            else {
                transformer = tf.newTransformer();
            }
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(1));
            transformer.transform(domSource, result);
            return writer.toString();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException();
        }
        catch (TransformerException e2) {
            throw new RuntimeException();
        }
    }
    
    private static Document parseXmlFile(final String in) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        catch (SAXException e2) {
            throw new RuntimeException(e2);
        }
        catch (IOException e3) {
            throw new RuntimeException(e3);
        }
    }
    
    private static boolean isXMLLike(final String inXMLStr) {
        boolean retBool = false;
        if (inXMLStr != null && inXMLStr.trim().length() > 0 && inXMLStr.trim().startsWith("<")) {
            final Pattern pattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>", 42);
            final Matcher matcher = pattern.matcher(inXMLStr.replaceFirst("<\\?xml.*?>", "").trim());
            retBool = matcher.matches();
        }
        return retBool;
    }
    
    static {
        LOG = LoggerFactory.getLogger(ConnectorXmlUtils.class);
    }
}
