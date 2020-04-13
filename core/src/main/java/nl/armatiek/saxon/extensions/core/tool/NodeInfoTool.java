package nl.armatiek.saxon.extensions.core.tool;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;

public class NodeInfoTool {
  
  public static NodeInfo getFirstChildElement(NodeInfo parentElement) {
    return parentElement.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();    
  }
  
  public static NodeInfo getNextSiblingElement(NodeInfo prevElement) {
    return prevElement.iterateAxis(AxisInfo.FOLLOWING_SIBLING, NodeKindTest.ELEMENT).next();    
  }
  

}
