package nl.armatiek.saxon.extensions.core.tool;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import nl.armatiek.saxon.extensions.core.error.SaxonExtensionException;

public class DOMTool {
  
  public static DocumentBuilder getDocumentBuilder(boolean validate, 
      boolean namespaceAware, boolean xincludeAware) throws SaxonExtensionException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
      if (xincludeAware) {
        docFactory.setFeature("http://apache.org/xml/features/xinclude", true);
        docFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        docFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
        docFactory.setXIncludeAware(true);
      }
      docFactory.setNamespaceAware(namespaceAware);
      docFactory.setValidating(validate);
      docFactory.setExpandEntityReferences(true);
      if (validate) {
        docFactory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
        docFactory.setFeature("http://apache.org/xml/features/validation/schema", true);
      }
      docFactory.setIgnoringElementContentWhitespace(true);
      return docFactory.newDocumentBuilder();
    } catch (Exception e) {
      throw new SaxonExtensionException(e);
    }
  }
  
  public static DocumentBuilder getDocumentBuilder(boolean validate, boolean namespaceAware) throws SaxonExtensionException {
    return getDocumentBuilder(validate, namespaceAware, true);
  }
  
  public static Document stringToDocument(String xml) throws Exception {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.parse(new InputSource(new StringReader(xml)));
  }
  
  public static Document inputStreamToDocument(InputStream is) throws Exception {
    if (is == null) {
      return null;
    }
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.parse(is);
  }
  
  public static Document readerToDocument(Reader reader, String systemId) throws Exception {
    if (reader == null) {
      return null;
    }
    DocumentBuilder builder = getDocumentBuilder(false, true);
    InputSource is = new InputSource(reader);
    if (systemId != null) {
      is.setSystemId(systemId);
    }
    return builder.parse(is);
  }
  
  public static Document readerToDocument(Reader reader) throws Exception {
    return readerToDocument(reader, null);
  }
  
  public static Element createElemWithText(Document ownerDoc, String tagName, String text) {
    Element newElem = ownerDoc.createElement(tagName);
    newElem.appendChild(ownerDoc.createTextNode(text));
    return newElem;
  }
  
  public static Element createElemWithCDATA(Document ownerDoc, String tagName, String cdata) {
    Element newElem = ownerDoc.createElement(tagName);
    newElem.appendChild(ownerDoc.createCDATASection(cdata));
    return newElem;
  }
  
  public static String nodeToString(Node node) throws Exception {
    if (node == null) {
      return null;
    }
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    DOMSource source = new DOMSource(node);
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    transformer.transform(source, result);
    return sw.toString();
  }
  
  public static void nodeToWriter(Node node, Writer writer) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    DOMSource source = new DOMSource(node);
    StreamResult result = new StreamResult(writer);
    transformer.transform(source, result);
  }
  
  public static Document getEmptyDOM() throws SaxonExtensionException {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    return builder.newDocument();
  }
  
  public static Document getRootDOM(String elemName) throws SaxonExtensionException {
    DocumentBuilder builder = getDocumentBuilder(false, true);
    Document doc = builder.newDocument();
    doc.appendChild(doc.createElement(elemName));
    return doc;
  }
  
  protected static void getTextFromNode(Node node, StringBuffer buffer, boolean addSpace) {
    switch (node.getNodeType()) {
      case Node.CDATA_SECTION_NODE: 
      case Node.TEXT_NODE:
        buffer.append(node.getNodeValue());
        if (addSpace)
          buffer.append(" ");
    }
    Node child = node.getFirstChild();
    while (child != null) {
      getTextFromNode(child, buffer, addSpace);
      child = child.getNextSibling();
    }
  }
  
  public static String getTextFromNode(Node node, String defaultValue) {
    if (node == null) {
      return defaultValue;
    }
    if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
      return ((Attr)node).getValue();
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      return node.getNodeValue();
    } else {
      StringBuffer text = new StringBuffer();
      getTextFromNode(node, text, true);
      return text.toString().trim();
    }
  }

  public static String getTextFromNode(Node node) {
    return getTextFromNode(node, null);
  }
  
  
  public static String getLocalName(Node node) {
    if (node.getPrefix() == null)
      return node.getNodeName();
    else
      return node.getLocalName();
  }
  
  public static Element getChildElementByLocalName(Element parentElem, String localName) {
    Node child = parentElem.getFirstChild();
    while (child != null) {
      if ((child.getNodeType() == Node.ELEMENT_NODE) && getLocalName(child).equals(localName)) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }
    
  public static Element getFirstChildElement(Element parentElem) {
    if (parentElem == null) {
      return null;
    }
    Node child = parentElem.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }
 
  public static String getValueOfChildElementByLocalName(Element parentElem, String localName) {
    Element childElem = getChildElementByLocalName(parentElem, localName);
    return (childElem != null) ? getTextFromNode(childElem) : null;
  }
  
  public static Node getPreviousSiblingElement(Node node) {
    if (node == null)
      return null;
    Node prevSibling = node.getPreviousSibling();
    while ((prevSibling != null) && (prevSibling.getNodeType() != Node.ELEMENT_NODE))
      prevSibling = prevSibling.getPreviousSibling();
    if ((prevSibling != null) && (prevSibling.getNodeType() == Node.ELEMENT_NODE))
      return prevSibling;
    return null;
  }
  
  public static Element getNextSiblingElement(Node node) {
    if (node == null)
      return null;
    Node nextSibling = node.getNextSibling();
    while ((nextSibling != null) && (nextSibling.getNodeType() != Node.ELEMENT_NODE))
      nextSibling = nextSibling.getNextSibling();
    if ((nextSibling != null) && (nextSibling.getNodeType() == Node.ELEMENT_NODE))
      return (Element) nextSibling;
    return null;
  }
  
  public static Element getFirstChildElementByLocalName(Element parentElem, String localName) {
    Node child = parentElem.getFirstChild();
    while (child != null) {
      if ((child.getNodeType() == Node.ELEMENT_NODE) && getLocalName(child).equals(localName)) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }
  
  public static int getNodePosition(Node node) {
    int index = 0;
    Node tmp = node;
    while (true) {
      tmp = tmp.getPreviousSibling();
      if (tmp == null)
        break;
      ++index;
    }
    return index;
  }
  
  /*
  public static boolean containsNode(NodeList nodes, Node node) {
    for (int i=0; i<nodes.getLength(); i++) {
      if (nodes.item(i).isEqualNode(node)) {
        return true;
      }
    }
    return false;
  }
  */
  
  public static boolean containsNode(Node parent, Node child) {
    Node tmp = parent.getFirstChild();
    while (tmp != null) {
      if (tmp.equals(child))
        return true; 
      tmp = tmp.getNextSibling();
    }
    return false;
  }
  
}
