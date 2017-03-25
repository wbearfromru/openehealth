package be.medx.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.soap.handlers.ErrorCollectorHandler;
import be.medx.soap.handlers.ForkContentHandler;
import be.medx.soap.handlers.SchemaValidatorHandler;
import be.medx.soap.handlers.XOPValidationHandler;
import be.medx.xml.JaxbContextFactory;

import com.gc.iotools.stream.is.InputStreamFromOutputStream;

public final class ValidatorHelper {
	private static final Logger LOG;
	private static SAXParserFactory SAF;
	private static TransformerFactory TRF;

	private ValidatorHelper() {
		throw new UnsupportedOperationException();
	}

	public static void validate(final Source source, final boolean xop, final String... schemaLocations) throws TechnicalConnectorException {
		try {
			final XOPValidationHandler handler = new XOPValidationHandler(xop);
			final ValidatorHandler validator = createValidatorForSchemaFiles(schemaLocations);
			final ErrorCollectorHandler collector = new ErrorCollectorHandler(handler);
			validator.setErrorHandler(collector);
			final SAXParser parser = ValidatorHelper.SAF.newSAXParser();
			parser.parse(convert(source), new ForkContentHandler(new ContentHandler[] { handler, validator }));
			handleValidationResult(collector);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	public static void validate(final Source source, final String... schemaLocations) throws TechnicalConnectorException {
		validate(source, false, schemaLocations);
	}

	public static void validate(final Object jaxbObj, final String rootSchemaFileLocation) throws TechnicalConnectorException {
		validate(jaxbObj, jaxbObj.getClass(), rootSchemaFileLocation);
	}

	public static void validate(final Object jaxbObj, final Class xmlClass, final String rootSchemaFileLocation) throws TechnicalConnectorException {
		if (jaxbObj == null) {
			ValidatorHelper.LOG.error("Message is null");
			throw new TechnicalConnectorException();
		}
		ConnectorXmlUtils.dump(jaxbObj);
		ValidatorHelper.LOG.debug("Validating with schema [" + rootSchemaFileLocation + "]");
		try {
			final JAXBContext jaxbContext = JaxbContextFactory.getJaxbContextForClass(xmlClass);
			final JAXBSource payload = new JAXBSource(jaxbContext, jaxbObj);
			validate(payload, rootSchemaFileLocation);
		} catch (Exception e) {
			throw handleException(e);
		}
		ValidatorHelper.LOG.debug("Message is valid.");
	}

	private static TechnicalConnectorException handleException(final Exception e) {
		if (e instanceof TechnicalConnectorException) {
			return (TechnicalConnectorException) e;
		}
		ValidatorHelper.LOG.error("Unable to validate object.", e);
		return new TechnicalConnectorException();
	}

	private static ValidatorHandler createValidatorForSchemaFiles(final String... schemaFiles) throws SAXException {
		final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema;
		if (schemaFiles.length == 1) {
			final URL schemaurl = SchemaValidatorHandler.class.getResource(schemaFiles[0]);
			schema = schemaFactory.newSchema(schemaurl);
		} else {
			Source[] sources = new Source[0];
			for (int i = 0; i < schemaFiles.length; ++i) {
				final InputStream in = SchemaValidatorHandler.class.getResourceAsStream(schemaFiles[i]);
				if (in != null) {
					final Source source = new StreamSource(in);
					sources = (Source[]) ArrayUtils.add(sources, source);
				}
			}
			schema = schemaFactory.newSchema(sources);
		}
		return schema.newValidatorHandler();
	}

	private static void handleValidationResult(final ErrorCollectorHandler collector) throws TechnicalConnectorException {
		if (collector.hasExceptions("WARN")) {
			final List<String> validationWarning = collector.getExceptionList("WARN");
			for (final String exception : validationWarning) {
				ValidatorHelper.LOG.warn(exception);
			}
		}
		if (collector.hasExceptions("ERROR", "FATAL")) {
			final StringBuilder sb = new StringBuilder();
			final List<String> validationErrors = collector.getExceptionList("ERROR", "FATAL");
			for (final String exception2 : validationErrors) {
				ValidatorHelper.LOG.error(exception2);
				sb.append(exception2);
				sb.append(", ");
			}
			throw new TechnicalConnectorException();
		}
	}

	private static InputStream convert(final Source source) {
		try {
			final InputStreamFromOutputStream<Void> isOs = new InputStreamFromOutputStream<Void>() {
				@Override
				protected Void produce(final OutputStream sink) throws Exception {
					final Result result = new StreamResult(sink);
					final Transformer transformer = ValidatorHelper.TRF.newTransformer();
					transformer.setOutputProperty("omit-xml-declaration", "yes");
					transformer.transform(source, result);
					return null;
				}
			};
			return isOs;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	static {
		LOG = LoggerFactory.getLogger(ValidatorHelper.class);
		ValidatorHelper.SAF = SAXParserFactory.newInstance();
		ValidatorHelper.TRF = TransformerFactory.newInstance();
		ValidatorHelper.SAF.setNamespaceAware(true);
	}
}
