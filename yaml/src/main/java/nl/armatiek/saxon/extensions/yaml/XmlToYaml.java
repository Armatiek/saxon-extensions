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
package nl.armatiek.saxon.extensions.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.saxon.extensions.core.ExtensionFunctionDefinitionBase;

/**
 * 
 * 
 * @author Maarten Kroon
 */
public class XmlToYaml extends ExtensionFunctionDefinitionBase {

  private static final StructuredQName qName = new StructuredQName("", EXT_NAMESPACEURI_BASE + "yaml", "xml-to-yaml");

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
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] { 
        SequenceType.makeSequenceType(AnyNodeTest.getInstance(), StaticProperty.ALLOWS_ONE_OR_MORE),
        SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, StaticProperty.EXACTLY_ONE) 
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.OPTIONAL_STRING;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new XmlToYamlCall();
  }

  private static class XmlToYamlCall extends ExtensionFunctionCall {
      
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object nodeInfoToObj(NodeInfo nodeInfo, XPathContext context) throws Exception {  
      if (nodeInfo.getNodeKind() == Type.DOCUMENT) {
        return nodeInfoToObj(nodeInfo.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next(), context);
      }
      switch (nodeInfo.getLocalPart()) {
      case "map":   
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        AxisIterator childIter = nodeInfo.iterateAxis(AxisInfo.CHILD);
        NodeInfo childNode;
        while ((childNode = childIter.next()) != null) {
          String key = childNode.getAttributeValue("", "key");
          if (key == null || "".equals(key))
            throw new XPathException("Missing attribute \"key\"");
          map.put(key, nodeInfoToObj(childNode, context));
        }
        return map;
      case "array":
        List list = new ArrayList();
        childIter = nodeInfo.iterateAxis(AxisInfo.CHILD);
        while ((childNode = childIter.next()) != null) {
          list.add(nodeInfoToObj(childNode, context));
        }
        return list;
      case "string":
        return nodeInfo.getStringValue();
      case "boolean":
        return new Boolean(((BooleanValue) BooleanValue.fromString(nodeInfo.getStringValue())).getBooleanValue());
      case "integer":
        return new Integer((int) ((Int64Value) IntegerValue.stringToInteger(nodeInfo.getStringValue())).longValue());
      case "long":
        return new Long(((Int64Value) IntegerValue.stringToInteger(nodeInfo.getStringValue())).longValue());
      case "number":
      case "double":
        return new Double(DoubleValue.parseNumber(nodeInfo.getStringValue()).getDoubleValue());
      case "float":
        return new Float(FloatValue.parseNumber(nodeInfo.getStringValue()).getDoubleValue());
      case "date":
        DateValue dateValue = (DateValue) new StringConverter.StringToDate(context.getConfiguration().getConversionRules()).convertString(nodeInfo.getStringValue());
        return dateValue.getCalendar().getTime();
      case "dateTime":
        DateTimeValue dateTimeValue = (DateTimeValue) new StringConverter.StringToDateTime(context.getConfiguration().getConversionRules()).convertString(nodeInfo.getStringValue());
        return dateTimeValue.getCalendar().getTime();
      case "base64Binary":
        return new Base64BinaryValue(nodeInfo.getStringValue()).getBinaryValue();
      case "null":
        return null;
      default:
        throw new XPathException("Unsupported datatype \"" + nodeInfo.getLocalPart() + "\"");
      }
    }
    
    private DumperOptions getDumperOptions(MapItem optionsMap) throws XPathException {
      DumperOptions options = new DumperOptions();
      Iterator<KeyValuePair> optionsIter = optionsMap.keyValuePairs().iterator();
      while (optionsIter.hasNext()) {
        KeyValuePair optionsEntry = optionsIter.next();
        switch (optionsEntry.key.getStringValue()) {
        case "allow-unicode": 
          options.setAllowUnicode(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "canonical": 
          options.setCanonical(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "default-flow-style": 
          options.setDefaultFlowStyle(DumperOptions.FlowStyle.valueOf(optionsEntry.value.getStringValue().toUpperCase()));
          break;
        case "default-scalar-style": 
          options.setDefaultScalarStyle(DumperOptions.ScalarStyle.valueOf(optionsEntry.value.getStringValue().toUpperCase()));
          break;
        case "explicit-end": 
          options.setExplicitEnd(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "explicit-start": 
          options.setExplicitStart(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "indent": 
          options.setIndent((int)((IntegerValue) optionsEntry.value).longValue());
          break;
        case "indent-with-indicator": 
          options.setIndentWithIndicator(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "indicator-indent": 
          options.setIndicatorIndent((int)((IntegerValue) optionsEntry.value).longValue());
          break;
        case "line-break": 
          options.setLineBreak(DumperOptions.LineBreak.valueOf(optionsEntry.value.getStringValue().toUpperCase()));
          break;
        case "max-simple-key-length": 
          options.setMaxSimpleKeyLength((int)((IntegerValue) optionsEntry.value).longValue());
          break;
        case "non-printable-style": 
          options.setNonPrintableStyle(DumperOptions.NonPrintableStyle.valueOf(optionsEntry.value.getStringValue().toUpperCase()));
          break;
        case "pretty-flow": 
          options.setPrettyFlow(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "split-lines": 
          options.setSplitLines(((BooleanValue) optionsEntry.value).getBooleanValue());
          break;
        case "tags": 
          Map<String, String> tagsMap = new HashMap<String, String>();
          Iterator<KeyValuePair> tagsIter = ((MapItem) optionsEntry.value).keyValuePairs().iterator();
          while (tagsIter.hasNext()) {
            KeyValuePair tagsEntry = tagsIter.next();
            tagsMap.put(tagsEntry.key.getStringValue(), tagsEntry.value.getStringValue());
          }
          options.setTags(tagsMap);
          break;
        case "time-zone": 
          options.setTimeZone(TimeZone.getTimeZone(optionsEntry.value.getStringValue()));
          break;
        case "version": 
          options.setVersion(DumperOptions.Version.valueOf(optionsEntry.value.getStringValue().toUpperCase()));
          break;
        case "width": 
          options.setWidth((int)((IntegerValue) optionsEntry.value).longValue());
          break;
        default:
          throw new XPathException("Unsupported option \"" + optionsEntry.key.getStringValue() + "\"");
        }
      }
      return options;
    }
    
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
      try {
        MapItem optionsMap = null;
        if (arguments.length > 1) {
          optionsMap = (MapItem) arguments[1].head();
        }
        Yaml yamlSerializer;
        if (optionsMap != null) {
          yamlSerializer = new Yaml(getDumperOptions(optionsMap));  
        } else {
          yamlSerializer = new Yaml();
        }  
        ArrayList<Object> objects = new ArrayList<Object>();
        SequenceIterator iter = arguments[0].iterate();
        Sequence seq;
        while ((seq = iter.next()) != null) {
          objects.add(nodeInfoToObj((NodeInfo) seq, context));
        }
        return new StringValue(yamlSerializer.dumpAll(objects.iterator()));
      } catch (XPathException xpe) {
        throw xpe;
      } catch (Exception e) {
        throw new XPathException(e.getMessage(), e);
      }
    }
    
  }

}