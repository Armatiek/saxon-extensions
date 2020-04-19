package nl.armatiek.saxon.extensions.http;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.StandardEntityResolver;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.StringValue;
import nl.armatiek.saxon.extensions.core.tool.NodeInfoTool;
import nl.armatiek.saxon.extensions.http.SendRequest.SendRequestCall;

public class HttpClientEntityResolver extends StandardEntityResolver {
  
  private static final FingerprintedQName nameRequest = new FingerprintedQName("http", Types.EXT_NAMESPACEURI, "request");
  private static final FingerprintedQName nameHeader = new FingerprintedQName("http", Types.EXT_NAMESPACEURI, "header");
  private static final FingerprintedQName nameName = new FingerprintedQName("", "", "name");
  private static final FingerprintedQName nameValue = new FingerprintedQName("", "", "value");
  private static final FingerprintedQName nameMethod = new FingerprintedQName("", "", "method");
  private static final FingerprintedQName nameHref = new FingerprintedQName("", "", "href");
  private static final FingerprintedQName nameStatusOnly = new FingerprintedQName("", "", "status-only");
  private static final FingerprintedQName nameOverrideMediaType = new FingerprintedQName("", "", "override-media-type");
  
  private final XPathContext context;
  private final NodeInfo requestNode;
  
  public HttpClientEntityResolver(XPathContext context, NodeInfo requestNode) {
    this.setConfiguration(context.getConfiguration());
    this.context = context;
    this.requestNode = requestNode;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    /* Try to find the entity in XML Catalog: */
    InputSource is = super.resolveEntity(publicId, systemId);
    if (is != null) {
      return is;
    }
    if (systemId.startsWith("http")) {
      /* The entity could not be found in the XML catalog and the request is an HTTP request. 
       * Lets execute this request with the same settings as the "containing" request:  
       */
      try {
        /* Create a new http:request element: */
        PipelineConfiguration config = context.getConfiguration().makePipelineConfiguration();
        TinyBuilder builder = (TinyBuilder) TreeModel.TINY_TREE.makeBuilder(config);
        builder.setLineNumbering(false);
        builder.open();
        builder.startDocument(0);
        builder.startElement(nameRequest, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
        
        /* Copy relevant attribute nodes: */
        AxisIterator attrs = requestNode.iterateAxis(AxisInfo.ATTRIBUTE);
        NodeInfo attr;
        while ((attr = attrs.next()) != null) {
          String local = attr.getLocalPart();
          if ("method".equals(local) || "href".equals(local) || "status-only".equals(local) || "override-media-type".equals(local)) {
            continue;
          }
          NodeName name;
          if (attr.hasFingerprint()) {
            name = new CodedName(attr.getFingerprint(), "", context.getConfiguration().getNamePool());
          } else {
            name = new FingerprintedQName("", "", attr.getLocalPart());
          }
          builder.attribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, attr.getStringValue(), null, 0);  
        }
        
        /* Create new attribute nodes specific for this request */
        builder.attribute(nameMethod, BuiltInAtomicType.UNTYPED_ATOMIC, "GET", null, 0);
        builder.attribute(nameHref, BuiltInAtomicType.UNTYPED_ATOMIC, systemId, null, 0);
        builder.attribute(nameStatusOnly, BuiltInAtomicType.UNTYPED_ATOMIC, "false", null, 0);
        builder.attribute(nameOverrideMediaType, BuiltInAtomicType.UNTYPED_ATOMIC, "text/plain", null, 0);
        
        builder.startContent();

        /* Copy all header elements: */
        AxisIterator headers = requestNode.iterateAxis(AxisInfo.CHILD, new NameTest(Type.ELEMENT, Types.EXT_NAMESPACEURI, "header", context.getConfiguration().getNamePool()));
        NodeInfo header;
        while ((header = headers.next()) != null) {
          builder.startElement(nameHeader, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
          builder.attribute(nameName, BuiltInAtomicType.UNTYPED_ATOMIC, header.getAttributeValue("", "name"), null, 0);  
          builder.attribute(nameValue, BuiltInAtomicType.UNTYPED_ATOMIC, header.getAttributeValue("", "value"), null, 0);  
          builder.endElement();
        }    
        builder.endElement();
        builder.endDocument();
        builder.close();
        
        /* Create new request call: */
        SendRequestCall call = new SendRequest.SendRequestCall();
        Sequence[] args = new Sequence[1];
        args[0] = NodeInfoTool.getFirstChildElement(builder.getCurrentRoot());
        
        /* Execute request call: */
        ZeroOrMore<Item> result = call.call(context, args);
        
        /* Check HTTP status code: */
        NodeInfo responseNode = (NodeInfo) result.itemAt(0);
        String status = responseNode.getAttributeValue("", "status");
        if ("200".equals(status)) {
          InputSource inputSource = new InputSource(new StringReader(((StringValue) result.itemAt(1)).getStringValue()));
          inputSource.setSystemId(systemId);
          return inputSource;
        }
        throw new IOException("Error executing HTTP request \"" + systemId + "\" (status: " + status + ", message: " + responseNode.getAttributeValue("", "message") + ")");
      } catch (XPathException e) {
        throw new IOException("Error executing HTTP request \"" + systemId + "\"", e);
      }
    }
    return null;
  }
  
  

}