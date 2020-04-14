package nl.armatiek.saxon.extensions.http.sample;

import net.sf.saxon.s9api.Processor;
import nl.armatiek.saxon.extensions.http.SendRequest;
import nl.armatiek.saxon.extensions.sample.SampleRunnerBase;

public class SampleRunner extends SampleRunnerBase {

  @Override
  protected void registerExtensionFunctions(Processor proc) {
    proc.registerExtensionFunction(new SendRequest());
    // proc.registerExtensionFunction(new WriteBinary());
  }
  
  public static void main(String [] args) {
    try {
      SampleRunner runner = new SampleRunner();
      runner.runSample("post-x-www-form-urlencoded.xsl");
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

}