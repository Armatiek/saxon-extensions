/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.armatiek.saxon.extensions.canonicalization;

import java.nio.charset.StandardCharsets;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.saxon.extensions.core.ExtensionFunctionDefinitionBase;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class CanonicalizeXML extends ExtensionFunctionDefinitionBase {

  private static final StructuredQName qName = new StructuredQName("", EXT_NAMESPACEURI_BASE + "canonicalization", "canonicalize-xml");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 1;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new CanonicalizeXMLCall();
  }

  private static class CanonicalizeXMLCall extends ExtensionFunctionCall {
    
    public static final ThreadLocal<Canonicalizer> canon = new ThreadLocal<Canonicalizer>() {
      
      {
        org.apache.xml.security.Init.init();
      }

      @Override
      protected Canonicalizer initialValue() {
        try {
          return Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N11_WITH_COMMENTS);
        } catch (InvalidCanonicalizerException e) {
          throw new Error(e);
        }
      }

    };

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      String xml = ((StringValue) arguments[0].head()).getStringValue();
      try {
        byte[] canonicalized = canon.get().canonicalize(xml.getBytes(StandardCharsets.UTF_8));
        return new StringValue(new String(canonicalized, StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw new XPathException("Error canonicalizing XML", e);
      }
    }
  }

}