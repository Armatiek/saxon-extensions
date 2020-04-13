package nl.armatiek.saxon.extensions.core;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

public abstract class ExtensionFunctionDefinitionBase extends ExtensionFunctionDefinition {

  public static final String EXT_NAMESPACEURI_BASE = "http://www.armatiek.com/saxon/functions/";
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }
  
}