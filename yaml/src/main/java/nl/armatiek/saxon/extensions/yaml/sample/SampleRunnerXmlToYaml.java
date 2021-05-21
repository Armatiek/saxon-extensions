package nl.armatiek.saxon.extensions.yaml.sample;

import net.sf.saxon.s9api.Processor;
import nl.armatiek.saxon.extensions.sample.SampleRunnerBase;
import nl.armatiek.saxon.extensions.yaml.XmlToYaml;

public class SampleRunnerXmlToYaml extends SampleRunnerBase {

  @Override
  protected void registerExtensionFunctions(Processor proc) {
    proc.registerExtensionFunction(new XmlToYaml());
  }
  
  public static void main(String [] args) {
    try {
      SampleRunnerXmlToYaml runner = new SampleRunnerXmlToYaml();
      runner.runSample("xml-to-yaml.xsl");
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

}
