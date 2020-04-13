package nl.armatiek.saxon.extensions.core;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;

/**
 * @author Maarten Kroon
 *
 */
public abstract class ExtensionFunctionCallBase extends net.sf.saxon.lib.ExtensionFunctionCall {
  
  // http://en.wikipedia.org/wiki/XQuery_API_for_Java
  protected AtomicValue convertJavaObjectToAtomicValue(Object value) throws XPathException {
    try {
      if (value == null) {
        return null;  
      } else if (value instanceof String) {
        return new StringValue((String) value);
      } else if (value instanceof Boolean) {
        return BooleanValue.get(((Boolean) value).booleanValue());
      } else if (value instanceof Integer) {
        return new Int64Value(((Integer) value).intValue());
      } else if (value instanceof Long) {
        return new Int64Value(((Long) value).longValue());
      } else if (value instanceof Double) {
        return new DoubleValue(((Double) value).doubleValue());
      } else if (value instanceof Float) {
        return new FloatValue(((Float) value).floatValue());
      } else if (value instanceof Date) { // includes java.sql.Date, java.sql.Time and java.sql.Timestamp
        Calendar calendar = new GregorianCalendar();
        calendar.setTime((Date) value);
        return new DateTimeValue(calendar, true);
      } else if (value instanceof LocalDate) {
        LocalDate ld = (LocalDate) value;
        return new DateValue(ld.getYear(), (byte)ld.getMonthValue(), (byte)ld.getDayOfMonth());
      } else if (value instanceof LocalDateTime) {
        return DateTimeValue.fromJavaDate(java.sql.Timestamp.valueOf((LocalDateTime) value));
      } else if (value instanceof Byte) {
        return new Int64Value(((Byte) value).byteValue());
      } else if (value instanceof Short) {
        return new Int64Value(((Short) value).shortValue());
      } else if (value instanceof BigDecimal) {
        return new BigDecimalValue((BigDecimal) value);
      } else if (value instanceof BigInteger) {
        return new BigIntegerValue((BigInteger) value);      
      } else if (value instanceof byte[]) {      
        return new Base64BinaryValue((byte[]) value);      
      } else {
        throw new XPathException("Java class not supported converting Java object to AtomicValue (" + value.getClass().toString() + ")");
      }    
    } catch (XPathException xpe) {
      throw xpe;
    } catch (Exception e) {
      throw new XPathException("Error converting Java object to AtomicValue", e);
    }    
  }
  
  protected Sequence convertJavaObjectToSequence(Object value) throws XPathException {
    if (value == null) {
      return EmptySequence.getInstance();
    } if (value instanceof Iterable<?> || value.getClass().isArray()) {
      if (value.getClass().isArray())
        value = Arrays.asList((Object[]) value);
      @SuppressWarnings("unchecked")
      Iterator<Object> iter = ((Iterable<Object>) value).iterator();
      ArrayList<AtomicValue> values = new ArrayList<AtomicValue>();
      while (iter.hasNext()) {
        values.add(convertJavaObjectToAtomicValue(iter.next()));
      }
      return new ZeroOrMore<AtomicValue>(values);
    }     
    return convertJavaObjectToAtomicValue(value); 
  }
  
  protected Object sequenceToJavaObject(Sequence seq, boolean alwaysReturnArray) throws XPathException {
    if (seq.head() == null)
      return null;
    ArrayList<Object> objectList = new ArrayList<Object>();
    SequenceIterator iter = seq.iterate();
    Item item;
    while ((item = iter.next()) != null)
      objectList.add(convertToJava(item));
    if (objectList.size() == 1 && !alwaysReturnArray)
      return objectList.get(0);

    Object[] objectArray = objectList.toArray(new Object[objectList.size()]);
    Object firstObject = objectArray[0];   
   
    if (firstObject instanceof String)
      return Arrays.copyOf(objectArray, objectArray.length, String[].class);
    if (firstObject instanceof BigInteger)
      return Arrays.copyOf(objectArray, objectArray.length, BigInteger[].class);
    if (firstObject instanceof Integer)
      return Arrays.copyOf(objectArray, objectArray.length, Integer[].class);
    if (firstObject instanceof Long)
      return Arrays.copyOf(objectArray, objectArray.length, Long[].class);
    if (firstObject instanceof Short)
      return Arrays.copyOf(objectArray, objectArray.length, Short[].class);
    if (firstObject instanceof Byte)
      return Arrays.copyOf(objectArray, objectArray.length, Byte[].class);
    if (firstObject instanceof BigDecimal)
      return Arrays.copyOf(objectArray, objectArray.length, BigDecimal[].class);
    if (firstObject instanceof Float)
      return Arrays.copyOf(objectArray, objectArray.length, Float[].class);
    if (firstObject instanceof Double)
      return Arrays.copyOf(objectArray, objectArray.length, Double[].class);
    if (firstObject instanceof Boolean)
      return Arrays.copyOf(objectArray, objectArray.length, Boolean[].class);
    if (firstObject instanceof BigDecimal)
      return Arrays.copyOf(objectArray, objectArray.length, BigDecimal[].class);
    if (firstObject instanceof Date)
      return Arrays.copyOf(objectArray, objectArray.length, Date[].class);
    if (firstObject instanceof LocalDate)
      return Arrays.copyOf(objectArray, objectArray.length, LocalDate[].class);
    if (firstObject instanceof LocalDateTime)
      return Arrays.copyOf(objectArray, objectArray.length, LocalDateTime[].class);
    if (firstObject instanceof QName)
      return Arrays.copyOf(objectArray, objectArray.length, QName[].class);
    if (firstObject instanceof Duration)
      return Arrays.copyOf(objectArray, objectArray.length, Duration[].class);
    if (firstObject instanceof byte[])
      return Arrays.copyOf(objectArray, objectArray.length, byte[][].class);
    else
      throw new XPathException("Could not convert sequence to typesafe Java object/array");
  }
  
  protected Object sequenceToJavaObject(Sequence seq) throws XPathException {
    return sequenceToJavaObject(seq, false);
  }
  
  // https://docs.oracle.com/javaee/5/tutorial/doc/bnazq.html
  public Object convertToJava(/* @NotNull */ Item item) throws XPathException {
    if (item instanceof NodeInfo) {
      Object node = item;
      while (node instanceof VirtualNode) {
        // strip off any layers of wrapping
        node = ((VirtualNode) node).getRealNode();
      }
      return node;
    } else if (item instanceof Function) {
      return item;
    } else if (item instanceof ObjectValue) {
      return ((ObjectValue<?>) item).getObject();
    } else {
      AtomicValue value = (AtomicValue) item;
      if (value.getItemType().equals(BuiltInAtomicType.STRING)) {
        return value.getStringValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.INTEGER)) {
        return ((IntegerValue) value).asBigInteger();
      } else if (value.getItemType().equals(BuiltInAtomicType.INT)) {
        return (int) ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.LONG)) {
        return ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.SHORT)) {
        return (short) ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.DECIMAL)) {
        return ((BigDecimalValue) value).getDecimalValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.FLOAT)) {
        return ((FloatValue) value).getFloatValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.DOUBLE)) {
        return ((DoubleValue) value).getDoubleValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.BOOLEAN)) {
        return ((BooleanValue) value).getBooleanValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.BYTE)) {
        return (byte) ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.QNAME)) {
        return ((QualifiedNameValue) value).toJaxpQName();
      } else if (value.getItemType().equals(BuiltInAtomicType.DATE_TIME)) {
        return ((DateTimeValue) value).getCalendar().getTime();
      } else if (value.getItemType().equals(BuiltInAtomicType.BASE64_BINARY)) {
        return ((Base64BinaryValue) value).getBinaryValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.HEX_BINARY)) {
        return ((HexBinaryValue) value).getBinaryValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.UNSIGNED_INT)) {
        return ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.UNSIGNED_SHORT)) {
        return (int) ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.UNSIGNED_BYTE)) {
        return (short) ((NumericValue) value).longValue();
      } else if (value.getItemType().equals(BuiltInAtomicType.TIME)) {
        TimeValue tv = (TimeValue) value;
        return LocalTime.of(tv.getHour(), tv.getMinute(), tv.getSecond(), tv.getMicrosecond() * 1000);
      } else if (value.getItemType().equals(BuiltInAtomicType.DATE)) {
        return ((DateValue) value).getCalendar().getTime();
      } else if (value.getItemType().equals(BuiltInAtomicType.ANY_ATOMIC)) {
        return value.toString();
      } else if (value.getItemType().equals(BuiltInAtomicType.DURATION)) {
        DurationValue dv = ((DurationValue) value);
        try {
          return DatatypeFactory.newInstance().newDuration(dv.getMicroseconds() * 1000);
        } catch (DatatypeConfigurationException e) { 
          throw new RuntimeException(e);
        }
      } else if (value.getItemType().equals(BuiltInAtomicType.NOTATION)) {
        return value.getStringValue();
      } else {
        return value.getStringValue();
      }
    }
  }

  protected Object[] numberedHashTrieMapToArray(HashTrieMap map) throws XPathException {
    ArrayList<Object> objectList = new ArrayList<Object>();
    // if (paramMap.conforms(BuiltInAtomicType.INTEGER, HashTrieMap.SINGLE_MAP_TYPE, context.getConfiguration().getTypeHierarchy()))
    int counter = 1;
    Sequence seq;
    while ((seq = map.get(Int64Value.makeIntegerValue(counter))) != null) {
      objectList.add(sequenceToJavaObject(seq));
      counter++;
    }
    return objectList.toArray(new Object[objectList.size()]);
  }
  
  protected NodeInfo unwrapNodeInfo(NodeInfo nodeInfo) {
    if (nodeInfo != null && nodeInfo.getNodeKind() == Type.DOCUMENT) {
      nodeInfo = nodeInfo.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
    }
    return nodeInfo;
  }
  
  protected void serialize(NodeInfo nodeInfo, Result result, Properties outputProperties) throws XPathException {
    try {
      TransformerFactory factory = new TransformerFactoryImpl();
      Transformer transformer = factory.newTransformer();
      if (outputProperties != null) {
        transformer.setOutputProperties(outputProperties);
      }
      transformer.transform(nodeInfo, result);
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, Writer w, Properties outputProperties) throws XPathException {
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, new StreamResult(w), outputProperties);
        } else {
          w.append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }
  
  protected void serialize(Sequence seq, OutputStream os, Properties outputProperties) throws XPathException {
    String encoding = "UTF-8";
    if (outputProperties != null) {
      encoding = outputProperties.getProperty("encoding", encoding);
    }
    try {
      SequenceIterator iter = seq.iterate(); 
      Item item;
      while ((item = iter.next()) != null) {
        if (item instanceof NodeInfo) {
          serialize((NodeInfo) item, new StreamResult(os), outputProperties);
        } else {
          new OutputStreamWriter(os, encoding).append(item.getStringValue());
        }
      }
    } catch (Exception e) {
      throw new XPathException(e);
    }
  }

  protected String serialize(NodeInfo nodeInfo, Properties props) throws XPathException {
    StringWriter sw = new StringWriter();
    serialize(nodeInfo, new StreamResult(sw), props);
    return sw.toString();
  }
  
  protected String serialize(NodeInfo nodeInfo) throws XPathException {
    Properties props = new Properties();
    props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    props.setProperty(OutputKeys.METHOD, "xml");
    props.setProperty(OutputKeys.INDENT, "no");
    return serialize(nodeInfo, props);
  }
  
  protected Properties getOutputProperties(NodeInfo paramsElem) {
    Properties props = new Properties();
    paramsElem = unwrapNodeInfo(paramsElem);
    AxisIterator iter = paramsElem.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
    NodeInfo paramElem;
    while ((paramElem = iter.next()) != null) {
      props.put(paramElem.getLocalPart(), paramElem.getAttributeValue("", "value"));
    }  
    return props;
  }
    
  protected NodeInfo source2NodeInfo(Source source, Configuration configuration) {        
    Node node = ((DOMSource)source).getNode();
    String baseURI = source.getSystemId();
    DocumentWrapper documentWrapper = new DocumentWrapper(node.getOwnerDocument(), baseURI, configuration);
    return documentWrapper.wrap(node);            
  }
  
  /*
  protected Sequence doCall(XPathContext context, Sequence[] arguments) throws Exception {
    return null;
  }
  
  @Override
  public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
    try {
      return doCall(context, arguments);
    } catch(XPathException xe) {
      throw xe;
    } catch (Exception e) {
      throw new XPathException(e.getMessage(), e);
    }
  }
  */
  
}