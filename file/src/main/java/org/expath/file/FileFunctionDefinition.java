package org.expath.file;

import nl.armatiek.saxon.extensions.core.ExtensionFunctionDefinitionBase;

public abstract class FileFunctionDefinition extends ExtensionFunctionDefinitionBase {
  
  protected static final String EXT_NAMESPACEURI = "http://expath.org/ns/file";
  
  @Override
  public boolean hasSideEffects() {
    return true;
  }

}