package nl.armatiek.saxon.extensions.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;

/**
 * XPath extension function that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public class Warn extends LogFunctionDefinition {
  
  private static final StructuredQName qName = new StructuredQName("", EXT_NAMESPACEURI, "warn");

  private static final Logger log = LoggerFactory.getLogger(Warn.class);
  
  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new LogFunctionCall() {
      @Override
      protected void log(String message) {
        log.warn(message);
      }
    };
  }
  
}