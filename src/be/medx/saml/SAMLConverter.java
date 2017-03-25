package be.medx.saml;

import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import java.io.Writer;

import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;

import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;

import org.w3c.dom.Element;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.soap.enums.Charset;

public final class SAMLConverter
{
    public static Element toElement(final String assertion) throws TechnicalConnectorException {
        try {
            final InputStream sbis = new ByteArrayInputStream(assertion.getBytes(Charset.UTF_8.getName()));
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder db = factory.newDocumentBuilder();
            final Document doc = db.parse(sbis);
            return doc.getDocumentElement();
        }
        catch (SAXException e) {
            throw new TechnicalConnectorException();
        }
        catch (IOException e2) {
            throw new TechnicalConnectorException();
        }
        catch (ParserConfigurationException e3) {
            throw new TechnicalConnectorException();
        }
    }
    
    public static String toXMLString(final Element element) throws TechnicalConnectorException {
        try {
            final Source source = new DOMSource(element);
            final StringWriter stringWriter = new StringWriter();
            final Result result = new StreamResult(stringWriter);
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        }
        catch (TransformerException e) {
            throw new TechnicalConnectorException();
        }
    }
    
    public static Element convert(final Source stsResponse) throws TechnicalConnectorException {
        try {
            final StringWriter stringWriter = new StringWriter();
            final Result result = new StreamResult(stringWriter);
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.transform(stsResponse, result);
            final String xmlResponse = stringWriter.getBuffer().toString();
            return toElement(xmlResponse);
        }
        catch (TransformerException e) {
            throw new TechnicalConnectorException();
        }
    }
}
