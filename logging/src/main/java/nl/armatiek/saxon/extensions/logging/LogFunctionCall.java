package nl.armatiek.saxon.extensions.logging;

import java.io.StringWriter;
import java.util.Properties;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import nl.armatiek.saxon.extensions.core.ExtensionFunctionCallBase;

public abstract class LogFunctionCall extends ExtensionFunctionCallBase {

  protected abstract void log(String message);
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
    try {
      Properties props = null;
      if (arguments.length == 2) {
        props = getOutputProperties((NodeInfo) arguments[1].head());
      }
      StringWriter sw = new StringWriter();
      serialize(arguments[1], sw, props);  
      log(sw.toString());          
      return EmptySequence.getInstance();
    } catch (Exception e) {
      throw new XPathException("Could not log message", e);
    }
  }
}