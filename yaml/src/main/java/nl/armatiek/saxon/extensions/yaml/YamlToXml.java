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

import static net.sf.saxon.sapling.Saplings.doc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.sapling.SaplingElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BooleanValue;
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
public class YamlToXml extends ExtensionFunctionDefinitionBase {

  private static final StructuredQName qName = new StructuredQName("", EXT_NAMESPACEURI_BASE + "yaml", "yaml-to-xml");

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
        SequenceType.SINGLE_STRING, 
        SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, StaticProperty.EXACTLY_ONE) 
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, StaticProperty.ALLOWS_ZERO_OR_MORE);
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new YamlToXmlCall();
  }

  private static class YamlToXmlCall extends ExtensionFunctionCall {
    
    private SaplingElement createElement(Object obj, String key) {
      String elementName;
      String text;
      if (obj instanceof Integer) {
        elementName = "integer";
        text = Int64Value.makeIntegerValue((long) (Integer) obj).getStringValue();
      } else if (obj instanceof Long) {
        elementName = "long";
        text = Int64Value.makeIntegerValue((Long) obj).getStringValue();
      } else if (obj instanceof Double) {
        elementName = "number";
        text = new DoubleValue((Double) obj).getStringValue();
      } else if (obj instanceof Float) {
        elementName = "number";
        text = new FloatValue((Float) obj).getStringValue();
      } else if (obj instanceof Boolean) {
        elementName = "boolean";
        text = BooleanValue.get((Boolean) obj).getStringValue();
      } else if (obj instanceof Date) {
        elementName = "date";
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime((Date) obj);
        DateValue dateValue = new DateValue(calendar, (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000));
        text = dateValue.getStringValue();
      } else if (obj instanceof byte[]) {
        elementName = "base64Binary";
        text = new Base64BinaryValue((byte[]) obj).getStringValue();
      } else if (obj == null) {
        elementName = "null";
        text = "";
      } else {
        elementName = "string";
        text = String.valueOf(obj);
      }
      SaplingElement result = new SaplingElement(new QName(NamespaceConstant.FN, elementName)).withText(text);
      return (key == null) ? result : result.withAttr("key", key);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private SaplingElement createElements(Object obj, String key) throws XPathException {
      if (obj instanceof Map.Entry) {
        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) obj;
        Object value = entry.getValue();
        if (value instanceof Map || value instanceof List) {
          return createElements(value, entry.getKey());
        } else {
          return createElement(value, entry.getKey());
        }
      } else if (obj instanceof Map || obj instanceof Collection || obj instanceof Object[]) {
        ArrayList<SaplingElement> childs = new ArrayList<SaplingElement>();
        String elementName;
        if (obj instanceof Map) {
          elementName = "map"; 
          for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
            childs.add(createElements(entry, null));
          }
        } else if (obj instanceof Collection) {          
          elementName = (obj instanceof Set) ? "set" : "array";
          for (Object entry: (Collection) obj) {
            childs.add(createElements(entry, null));
          }
        } else {
          elementName = "array";
          for (Object entry: (Object[]) obj) {
            childs.add(createElements(entry, null));
          }
        }
        SaplingElement result = new SaplingElement(new QName(NamespaceConstant.FN, elementName)).withChild(childs.toArray(new SaplingElement[childs.size()]));
        return (key == null) ? result : result.withAttr("key", key);
      } else {
        return createElement(obj, null);
      }
    }
    
    private NodeInfo objToNodeInfo(Object obj, XPathContext context) throws XPathException {
      return doc().withChild(createElements(obj, null)).toNodeInfo(context.getConfiguration());
    }
    
    private LoaderOptions getLoaderOptions(MapItem optionsMap) throws XPathException {
      LoaderOptions options = new LoaderOptions();
      Iterator<KeyValuePair> pairs = optionsMap.keyValuePairs().iterator();
      while (pairs.hasNext()) {
        KeyValuePair pair = pairs.next();
        switch (pair.key.getStringValue()) {
        case "allow-duplicate-keys": 
          options.setAllowDuplicateKeys(((BooleanValue) pair.value).getBooleanValue());
          break;
        case "allow-recursive-keys": 
          options.setAllowRecursiveKeys(((BooleanValue) pair.value).getBooleanValue());
          break;
        case "enum-case-sensitive": 
          options.setEnumCaseSensitive(((BooleanValue) pair.value).getBooleanValue());
          break;
        case "max-aliases-for-collections": 
          options.setMaxAliasesForCollections((int)((IntegerValue) pair.value).longValue());
          break;
        case "process-comments": 
          options.setProcessComments(((BooleanValue) pair.value).getBooleanValue());
          break;  
        default:
          throw new XPathException("Unsupported option \"" + pair.key.getStringValue() + "\"");
        }
      }
      return options;
    }
    
    @Override
    public ZeroOrMore<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
      String yamlText = ((StringValue) arguments[0].head()).getStringValue();
      MapItem optionsMap = null;
      if (arguments.length > 1) {
        optionsMap = (MapItem) arguments[1].head();
      }
      Yaml yamlParser;
      if (optionsMap != null) {
        yamlParser = new Yaml(getLoaderOptions(optionsMap));  
      } else {
        yamlParser = new Yaml();
      }  
      Iterable<Object> objs = yamlParser.loadAll(yamlText);
      ArrayList<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
      Iterator<Object> objIterator = objs.iterator();
      while (objIterator.hasNext()) {
        nodeInfoList.add(objToNodeInfo(objIterator.next(), context));
      }
      return new ZeroOrMore<NodeInfo>(nodeInfoList.toArray(new NodeInfo[nodeInfoList.size()]));
    }
    
  }

}