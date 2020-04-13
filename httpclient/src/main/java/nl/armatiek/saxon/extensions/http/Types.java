package nl.armatiek.saxon.extensions.http;

import java.util.HashSet;
import java.util.Set;

import okhttp3.MediaType;

public class Types {
  
  public static final String EXT_NAMESPACEURI = "http://expath.org/ns/http-client";
  
  /** Media types that must be treated as text types (in addition to text/*): */
  public static Set<String> TEXT_TYPES;
  static {
    TEXT_TYPES = new HashSet<String>();
    TEXT_TYPES.add("application/x-www-form-urlencoded");
    TEXT_TYPES.add("application/xml-dtd");
  }

  /** Media types that must be treated as XML types (in addition to *+xml): */
  public static Set<String> XML_TYPES;
  static {
    XML_TYPES = new HashSet<String>();
    XML_TYPES.add("text/xml");
    XML_TYPES.add("application/xml");
    XML_TYPES.add("text/xml-external-parsed-entity");
    XML_TYPES.add("application/xml-external-parsed-entity");
  }
  
  public enum Type { XML, HTML, XHTML, TEXT, BINARY, BASE64, HEXBIN, MULTIPART, SRC }
  
  public static String getMethodForMediaType(final MediaType mediaType) {
    Type type = parseType(mediaType.type() + '/' + mediaType.subtype());
    return type.toString().toLowerCase();
  }
  
  public static Type parseType(final String type) {
    if (type.startsWith("multipart/")) {
      return Type.MULTIPART;
    } else if ("text/html".equals(type)) {
      return Type.HTML;
    } else if ("application/xhtml+xml".equals(type)) {
      return Type.XHTML;
    } else if (type.endsWith("+xml") || XML_TYPES.contains(type)) {
      return Type.XML;
    } else if (type.startsWith("text/") || TEXT_TYPES.contains(type)) {
      return Type.TEXT;
    } else {
      return Type.BINARY;
    }
  }

}