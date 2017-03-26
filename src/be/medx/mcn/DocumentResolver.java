package be.medx.mcn;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.utils.ConnectorXmlUtils;

public class DocumentResolver extends ResourceResolverSpi {
	private static final Logger LOG;
	private Map<String, Document> documents;

	public DocumentResolver(final String baseURI, final Document doc) {
		(this.documents = new HashMap<String, Document>()).put(baseURI, doc);
	}

	public void addDocument(final String baseURI, final Document doc) {
		this.documents.put(baseURI, doc);
	}

	@Override
	public boolean engineCanResolve(final Attr uri, final String baseURI) {
		String id = uri.getNodeValue();
		if (id.startsWith("#")) {
			id = uri.getNodeValue().substring(1);
		}
		if (this.documents.containsKey(id) || this.documents.size() == 1) {
			DocumentResolver.LOG.debug("Can resolve attribute with id [" + id + "]");
			return true;
		}
		DocumentResolver.LOG.debug("Unable resolve attribute with id [" + id + "]");
		return false;
	}

	@Override
	public boolean engineIsThreadSafe() {
		return true;
	}

	@Override
	public XMLSignatureInput engineResolve(final Attr uri, final String baseURI) throws ResourceResolverException {
		Node selectedElem = null;
		String id = uri.getNodeValue();
		if (id.startsWith("#")) {
			id = uri.getNodeValue().substring(1);
		}
		Document selectedDoc = null;
		if (this.documents.containsKey(id)) {
			selectedDoc = this.documents.get(id);
		} else {
			selectedDoc = this.documents.values().toArray(new Document[0])[0];
		}
		if (DocumentResolver.LOG.isDebugEnabled()) {
			try {
				DocumentResolver.LOG.debug("Selected document: " + ConnectorXmlUtils.flatten(ConnectorXmlUtils.toString(new DOMSource(selectedDoc))));
			} catch (TechnicalConnectorException e) {
				DocumentResolver.LOG.error(e.getMessage());
			}
		}
		this.flagAttributeValueAsId(selectedDoc.getDocumentElement(), id);
		selectedElem = selectedDoc.getElementById(id);
		if (DocumentResolver.LOG.isDebugEnabled()) {
			DocumentResolver.LOG.debug("Try to catch an Element with ID " + id + " and Element was " + selectedElem);
		}
		this.processElement(uri, baseURI, selectedElem, id);
		final XMLSignatureInput result = new XMLSignatureInput(selectedElem);
		result.setExcludeComments(true);
		result.setMIMEType("text/xml");
		result.setSourceURI((baseURI != null) ? baseURI.concat(uri.getNodeValue()) : uri.getNodeValue());
		return result;
	}

	private void processElement(final Attr uri, final String baseURI, final Node selectedElem, final String id) throws ResourceResolverException {
		if (selectedElem == null) {
			final Object[] exArgs = { id };
			throw new ResourceResolverException("signature.Verification.MissingID", exArgs, uri, baseURI);
		}
	}

	private void flagAttributeValueAsId(final Element el, final String attrValue) {
		boolean foundAttr = false;
		final NamedNodeMap attrMap = el.getAttributes();
		for (int i = 0; i < attrMap.getLength(); ++i) {
			final Node item = attrMap.item(i);
			if (item.getTextContent().equalsIgnoreCase(attrValue)) {
				el.setIdAttribute(item.getLocalName(), true);
				foundAttr = true;
				break;
			}
		}
		if (!foundAttr) {
			final NodeList childs = el.getChildNodes();
			for (int j = 0; j < childs.getLength(); ++j) {
				final Node child = childs.item(j);
				if (child.getNodeType() == 1) {
					this.flagAttributeValueAsId((Element) childs.item(j), attrValue);
				}
			}
		}
	}

	static {
		LOG = LoggerFactory.getLogger(DocumentResolver.class);
	}
}
