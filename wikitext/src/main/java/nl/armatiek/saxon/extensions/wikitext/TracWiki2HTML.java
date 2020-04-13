package nl.armatiek.saxon.extensions.wikitext;

import org.eclipse.mylyn.wikitext.tracwiki.TracWikiLanguage;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

/**
 * 
 * @author Maarten Kroon
 */
public class TracWiki2HTML extends ConvertWikiTextFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", NAMESPACE_URI, "tracwiki-2-html");

  public StructuredQName getFunctionQName() {
    return qName;
  }

  public int getMinimumNumberOfArguments() {
    return 1;
  }

  public int getMaximumNumberOfArguments() {
    return 1;
  }

  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_NODE;
  }

  public ExtensionFunctionCall makeCallExpression() {
    return new ConvertWikiTextCall(new TracWikiLanguage());
  }
  
}