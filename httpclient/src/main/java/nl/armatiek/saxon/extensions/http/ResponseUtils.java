package nl.armatiek.saxon.extensions.http;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.StandardEntityResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.Statistics;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.StringValue;
import nl.armatiek.saxon.extensions.core.tool.NodeInfoTool;
import nl.armatiek.saxon.extensions.http.Types.Type;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ResponseUtils {
  
  private static final FingerprintedQName nameResponse = new FingerprintedQName("http", Types.EXT_NAMESPACEURI, "response");
  private static final FingerprintedQName nameStatus = new FingerprintedQName("", "", "status");
  private static final FingerprintedQName nameMessage = new FingerprintedQName("", "", "message");
  private static final FingerprintedQName nameHeader = new FingerprintedQName("http", Types.EXT_NAMESPACEURI, "header");
  private static final FingerprintedQName nameName = new FingerprintedQName("", "", "name");
  private static final FingerprintedQName nameValue = new FingerprintedQName("", "", "value");
  private static final FingerprintedQName nameBody = new FingerprintedQName("http", Types.EXT_NAMESPACEURI, "body");
  private static final FingerprintedQName nameMediaType = new FingerprintedQName("", "", "media-type");
  private static final FingerprintedQName nameMethod = new FingerprintedQName("", "", "method");
  
  public static NodeInfo buildResponseElement(final Response response, final XPathContext context) throws XPathException {
    TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
    builder.setStatistics(Statistics.SOURCE_DOCUMENT_STATISTICS); 
    builder.setLineNumbering(false);
    builder.open();
    builder.startDocument(0);
    
    // Root element: 
    builder.startElement(nameResponse, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    builder.attribute(nameStatus, BuiltInAtomicType.UNTYPED_ATOMIC, Integer.toString(response.code()), null, 0);
    builder.attribute(nameMessage, BuiltInAtomicType.UNTYPED_ATOMIC, response.message(), null, 0);
    
    // Response headers:
    Headers responseHeaders = response.headers();
    for (int i=0; i<responseHeaders.size(); i++) {
      builder.startElement(nameHeader, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
      builder.attribute(nameName, BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.name(i), null, 0);
      builder.attribute(nameValue, BuiltInAtomicType.UNTYPED_ATOMIC, responseHeaders.value(i), null, 0);
      builder.endElement();
    }
    
    // Response body:
    ResponseBody body = response.body();
    builder.startElement(nameBody, Untyped.getInstance(), ExplicitLocation.UNKNOWN_LOCATION, 0);
    MediaType mediaType = body.contentType();
    if (mediaType != null) {
      builder.attribute(nameMediaType, BuiltInAtomicType.UNTYPED_ATOMIC, mediaType.toString(), null, 0);
      builder.attribute(nameMethod, BuiltInAtomicType.UNTYPED_ATOMIC, Types.getMethodForMediaType(mediaType), null, 0);
    } else {
      builder.attribute(nameMethod, BuiltInAtomicType.UNTYPED_ATOMIC, "binary", null, 0);
    }
 
    builder.endElement();
   
    builder.endElement();
    
    builder.endDocument();
    builder.close();
    return NodeInfoTool.getFirstChildElement(builder.getCurrentRoot());
  }
  
  public static Item buildResponseContent(final Response response, final XPathContext context, final String overrideMediaType) throws XPathException, IOException {
    ResponseBody body = response.body();
    MediaType specifiedMediaType = body.contentType();
    MediaType mediaType = null;
    if (overrideMediaType != null) {
      mediaType = MediaType.parse(overrideMediaType);
      if (mediaType == null) {
        throw new XPathException("Error parsing media type (\"" + overrideMediaType + "\")", "HC005");
      }
    } else if (specifiedMediaType != null) {
      mediaType = specifiedMediaType;
    } else {
      mediaType = MediaType.parse("application/octet-stream");
    }
    
    String type = mediaType.type();
    String subtype = mediaType.subtype();
    String fullType = type + '/' + subtype;
    
    Type contentType = Types.parseType(fullType);
    switch (contentType) {
    case TEXT:
      return new StringValue(body.string());
    case XML:
    case XHTML:
    case HTML:
      try {
        TinyBuilder builder = new TinyBuilder(context.getConfiguration().makePipelineConfiguration());
        builder.setStatistics(Statistics.SOURCE_DOCUMENT_STATISTICS);
        SAXSource source = new SAXSource(new InputSource(body.byteStream()));
        source.setSystemId(response.request().url().toString());
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setDTDValidationMode(Validation.STRIP);
        parseOptions.setSchemaValidationMode(Validation.STRIP);
        parseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
        parseOptions.setEntityResolver(new StandardEntityResolver());
        parseOptions.setLineNumbering(false);
        if (contentType.equals(Type.HTML)) {
          parseOptions.setXMLReader(new Parser());
        }
        Sender.send(source, builder, parseOptions);
        builder.close();
        return builder.getCurrentRoot();
      } catch (Exception e) {
        throw new XPathException("Error parsing the entity content as XML or HTML", "HC002");
      }
    default:
      return new Base64BinaryValue(body.bytes()); 
    }
    
  }

}