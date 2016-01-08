package at.brandl.lws.notice.server.service;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

public class FormattingVisitor implements NodeVisitor {

	private StringBuilder accum = new StringBuilder();

	public void head(Node node, int depth) {

		String name = node.nodeName();
		if (node instanceof TextNode) {
			accum.append(((TextNode) node).text());
		} else if (name.equals("li")) {
			accum.append("\n * ");
		} else if (name.equals("dt")) {
			accum.append("  ");
		} else if (StringUtil.in(name, "p", "tr")) {
			accum.append("\n");
		} else if (StringUtil.in(name, "div", "h1", "h2", "h3", "h4", "h5")
				&& !lastCharIs('\n')) {
			accum.append("\n");
		}
	}

	// hit when all of the node's children (if any) have been visited
	public void tail(Node node, int depth) {

		String name = node.nodeName();
		if (StringUtil.in(name, "br", "dd", "dt", "p")) {
			accum.append("\n");
		}
		if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "div")
				&& !lastCharIs('\n')) {

		}
	}

	private boolean lastCharIs(char character) {
		int length = accum.length();
		return length > 0 && accum.charAt(length - 1) == character;
	}

	@Override
	public String toString() {
		return accum.toString();
	}
}