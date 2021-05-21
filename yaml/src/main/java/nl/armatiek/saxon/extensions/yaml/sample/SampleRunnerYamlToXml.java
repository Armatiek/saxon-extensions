package nl.armatiek.saxon.extensions.yaml.sample;

import net.sf.saxon.s9api.Processor;
import nl.armatiek.saxon.extensions.sample.SampleRunnerBase;
import nl.armatiek.saxon.extensions.yaml.YamlToXml;

public class SampleRunnerYamlToXml extends SampleRunnerBase {

  @Override
  protected void registerExtensionFunctions(Processor proc) {
    proc.registerExtensionFunction(new YamlToXml());
  }
  
  public static void main(String [] args) {
    try {
      SampleRunnerYamlToXml runner = new SampleRunnerYamlToXml();
      runner.runSample("yaml-to-xml.xsl");
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

}
