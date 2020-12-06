package nl.armatiek.saxon.extensions.wikitext;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.parser.markup.AbstractMarkupLanguage;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.ParseXml;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class ConvertWikiTextCall extends ExtensionFunctionCall {

  private AbstractMarkupLanguage language;
  
  public ConvertWikiTextCall(AbstractMarkupLanguage language) {
    this.language = language;
  }
  
  @Override
  public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {            
    try {
      MarkupParser markupParser = new MarkupParser(language);
      String wiki = ((StringValue) arguments[0].head()).getStringValue();
      String html = markupParser.parseToHtml(wiki);
      html = StringEscapeUtils.unescapeHtml4(html);
      ParseXml parseXml = new ParseXml();
      parseXml.setRetainedStaticContext(new RetainedStaticContext(context.getConfiguration()));
      return parseXml.call(context, new Sequence[] {new StringValue(html)});
    } catch (Exception e) {
      throw new XPathException("Error converting " + language.getName() + " to HTML", e);
    }       
  }
}