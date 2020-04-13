package nl.armatiek.saxon.extensions.sample;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public abstract class SampleRunnerBase {
  
  protected abstract void registerExtensionFunctions(Processor proc);
  
  protected void runSample(String xslName) throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(xslName);
    Processor proc = new Processor(false);
    registerExtensionFunctions(proc);
    XsltCompiler comp = proc.newXsltCompiler();
    XsltExecutable exp = comp.compile(new StreamSource(in));
    XsltTransformer trans = exp.load();
    trans.setInitialTemplate(new QName("run-sample"));
    trans.setDestination(proc.newSerializer(System.out));
    trans.transform();
  }
  
}