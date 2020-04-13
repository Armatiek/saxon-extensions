package nl.armatiek.saxon.extensions.logging;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.value.SequenceType;
import nl.armatiek.saxon.extensions.core.ExtensionFunctionDefinitionBase;

/**
 * XPath extension function that logs to Slf4J logging framework.
 * 
 * @author Maarten Kroon
 */
public abstract class LogFunctionDefinition extends ExtensionFunctionDefinitionBase {
  
  protected static final String EXT_NAMESPACEURI = ExtensionFunctionDefinitionBase.EXT_NAMESPACEURI_BASE + "logging";
  
  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] {  
        SequenceType.makeSequenceType(AnyItemType.getInstance(), StaticProperty.ALLOWS_ZERO_OR_MORE),
        SequenceType.OPTIONAL_NODE };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_BOOLEAN;
  }
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }

}