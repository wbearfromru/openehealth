package be.medx.mcn;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DomUtils {
	private DomUtils() {
		throw new UnsupportedOperationException();
	}

	public static NodeList getMatchingChilds(final Node node, final String namespace, final String localName) {
		final NodeList childs = node.getChildNodes();
		final ArrayNodeList result = new ArrayNodeList();
		for (int i = 0; i < childs.getLength(); ++i) {
			final Node child = childs.item(i);
			if (child.getNodeType() == 1) {
				final String ns = (child.getNamespaceURI() == null) ? "" : child.getNamespaceURI();
				final String tag = child.getLocalName();
				if (tag.equals(localName) && ns.equals(namespace)) {
					result.addNode(child);
				}
			}
		}
		if (result.getLength() == 0) {
			for (int i = 0; i < childs.getLength(); ++i) {
				final Node child = childs.item(i);
				final NodeList list = getMatchingChilds(child, namespace, localName);
				for (int j = 0; j < list.getLength(); ++j) {
					result.addNode(list.item(j));
				}
			}
		}
		return result;
	}

	public static class ArrayNodeList implements NodeList {
		private List<Node> result;

		public ArrayNodeList() {
			this.result = new ArrayList<Node>();
		}

		@Override
		public Node item(final int index) {
			return this.result.get(index);
		}

		@Override
		public int getLength() {
			return this.result.size();
		}

		public void addNode(final Node node) {
			this.result.add(node);
		}
	}
}
