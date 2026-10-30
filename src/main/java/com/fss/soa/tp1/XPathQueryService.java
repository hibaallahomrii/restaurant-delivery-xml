package com.fss.soa.tp1;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * FEATURE 2 - XPath querying (JAXP).
 *
 * <p>Steps used (exactly those of the TP):</p>
 * <ol>
 *   <li>parse the XML file into a DOM {@link Document} (DocumentBuilder);</li>
 *   <li>get an {@link XPath} object from an {@link XPathFactory};</li>
 *   <li>{@code compile()} the expression into an {@link XPathExpression};</li>
 *   <li>{@code evaluate()} it on the document, asking for the wanted result type
 *       (NODESET, STRING, NUMBER or BOOLEAN).</li>
 * </ol>
 *
 * <p>Note: the JDK implements XPath <b>1.0</b>. Functions of XPath 2.0 such as
 * {@code distinct-values()}, {@code min()}, {@code max()} or {@code concat()} on node-sets
 * are therefore NOT available - the catalog shows how to get the same results in 1.0.</p>
 */
public class XPathQueryService {

    private final Document document;
    private final XPath xpath;

    public XPathQueryService(InputStream xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);      // our document has no namespace -> simple XPath
        dbf.setIgnoringComments(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try {
            // XXE protection: refuse any DOCTYPE declaration
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException ignored) {
            // feature not supported by this parser: FEATURE_SECURE_PROCESSING still applies
        }
        this.document = dbf.newDocumentBuilder().parse(xml);
        this.xpath = XPathFactory.newInstance().newXPath();
    }

    public Document getDocument() {
        return document;
    }

    // ------------------------------------------------------------------
    //  The 4 result types of XPath 1.0
    // ------------------------------------------------------------------

    /** NODESET: returns a readable text for every selected node. */
    public List<String> selectValues(String expression) throws XPathExpressionException {
        NodeList nodes = selectNodes(expression, document);
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(describe(nodes.item(i)));
        }
        return values;
    }

    /** NODESET relative to a context node (e.g. one order). */
    public NodeList selectNodes(String expression, Object contextNode) throws XPathExpressionException {
        XPathExpression compiled = xpath.compile(expression);
        return (NodeList) compiled.evaluate(contextNode, XPathConstants.NODESET);
    }

    /** STRING: the string-value of the result (first node if it is a node-set). */
    public String selectString(String expression) throws XPathExpressionException {
        return selectString(expression, document);
    }

    public String selectString(String expression, Object contextNode) throws XPathExpressionException {
        XPathExpression compiled = xpath.compile(expression);
        return ((String) compiled.evaluate(contextNode, XPathConstants.STRING)).trim();
    }

    /** NUMBER: e.g. count(...), sum(...). */
    public double selectNumber(String expression) throws XPathExpressionException {
        XPathExpression compiled = xpath.compile(expression);
        return (Double) compiled.evaluate(document, XPathConstants.NUMBER);
    }

    /** BOOLEAN: e.g. boolean(//order[@status='pending']). */
    public boolean selectBoolean(String expression) throws XPathExpressionException {
        return selectBoolean(expression, document);
    }

    public boolean selectBoolean(String expression, Object contextNode) throws XPathExpressionException {
        XPathExpression compiled = xpath.compile(expression);
        return (Boolean) compiled.evaluate(contextNode, XPathConstants.BOOLEAN);
    }

    /** Reads an attribute of a node (empty string if absent). */
    public static String attr(Node node, String name) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null || attributes.getNamedItem(name) == null) {
            return "";
        }
        return attributes.getNamedItem(name).getNodeValue();
    }

    // ------------------------------------------------------------------
    //  Display helper
    // ------------------------------------------------------------------

    /**
     * Converts a node to a human-readable string:
     * attribute -> its value; leaf element -> its text; complex element -> "&lt;tag id=...&gt;".
     */
    static String describe(Node node) {
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE:
                return node.getNodeValue().trim();
            case Node.ELEMENT_NODE:
                if (!hasChildElements(node)) {
                    return node.getTextContent().trim();
                }
                StringBuilder sb = new StringBuilder("<" + node.getNodeName());
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    sb.append(' ').append(attrs.item(i).getNodeName())
                      .append("=\"").append(attrs.item(i).getNodeValue()).append('"');
                }
                return sb.append('>').toString();
            default:
                return node.getNodeName();
        }
    }

    private static boolean hasChildElements(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }
}
