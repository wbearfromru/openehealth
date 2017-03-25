package be.medx.soap.handlers;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XOPValidationHandler extends DefaultHandler {
	private boolean xop;
	private int endElementAfterXOP;
	private boolean enabled;

	public XOPValidationHandler(final boolean enable) {
		this.endElementAfterXOP = 0;
		this.enabled = enable;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if (this.enabled) {
			this.resetXOP();
			if ("Include".equals(localName) && "http://www.w3.org/2004/08/xop/include".equals(uri) && attributes.getValue("href") != null) {
				this.xop = true;
			}
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (this.xop) {
			final String content = StringUtils.substring(new String(ch), start, start + length);
			if (StringUtils.isNotBlank(content)) {
				this.xop = false;
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (this.xop) {
			++this.endElementAfterXOP;
		}
	}

	public boolean isXop() {
		return this.xop;
	}

	private void resetXOP() {
		if (this.endElementAfterXOP == 2) {
			this.xop = false;
			this.endElementAfterXOP = 0;
		}
	}
}
