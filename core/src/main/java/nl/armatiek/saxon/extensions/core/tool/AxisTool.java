package nl.armatiek.saxon.extensions.core.tool;

import net.sf.saxon.tree.iter.AxisIterator;

public class AxisTool {
  
  public static int getCount(final AxisIterator iter) {
    int count = 0;
    while (iter.next() != null) {
      count++;
    }
    return count;
  }

}
