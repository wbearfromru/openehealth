package be.medx.soap.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ForkContentHandler extends DefaultHandler {
	private ContentHandler[] handlers;

	public ForkContentHandler(final ContentHandler... handlers) {
		this.handlers = handlers;
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		for (final ContentHandler handler : this.handlers) {
			handler.setDocumentLocator(locator);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.startDocument();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.endDocument();
		}
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.endPrefixMapping(prefix);
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.startElement(uri, localName, qName, atts);
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.endElement(uri, localName, qName);
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.characters(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.processingInstruction(target, data);
		}
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		for (final ContentHandler handler : this.handlers) {
			handler.skippedEntity(name);
		}
	}
}
