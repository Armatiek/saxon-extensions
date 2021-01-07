# saxon-extensions
XPath/XQuery extension function library for the Saxon XSLT processor.

## EXPath - File implementation

Functions to perform file system related operations such as listing, reading, 
or writing files or directories.

Namespace: http://expath.org/ns/file

These functions are an implementation of the specification: 
[EXPath - File Module](http://expath.org/spec/file)

## EXPath - HTTP Client implementation

Functions that implement a versatile HTTP client interface for XPath and XQuery, 

Namespace: http://expath.org/ns/http-client

These functions are an implementation of the specification:
[EXPath - HTTP Client Module](http://expath.org/spec/http-client) 
based on the Java HTTP client library [OkHttp](https://square.github.io/okhttp/)

Not implemented:
- Multipart responses (multipart requests are supported)
- Other authentication methods than "Basic"

Extensions to the specifications:
- Proxy server support via the attributes "http:request/@proxy-host","http:request/@proxy-port", 
"http:request/@proxy-username", "http:request/@proxy-password"
- Trust all SSL certificates via the attribute http:request/@trust-all-certs 
(xs:boolean, default: false())

Remarks:
- Default timeout (connect/write/read/call) is 30 seconds (can be changed via http:request/@timeout)
- Certificate authorities of the host platform are trusted

## Logging functions

Logging functions that log to the Java 
[http://www.slf4j.org/](SLF4J) framework. See 
[Documentation](http://www.slf4j.org/docs.html) for the configuration 
of the framework. 

Namespace: http://www.armatiek.com/saxon/functions/logging

Functions:

- log:debug($msg as item()*, $params as element(output:serialization-parameters)?) as xs:boolean?
- log:info($msg as item()*, $params as element(output:serialization-parameters)?) as xs:boolean?
- log:warn($msg as item()*, $params as element(output:serialization-parameters)?) as xs:boolean?
- log:error($msg as item()*, $params as element(output:serialization-parameters)?) as xs:boolean?

The second argument provides serialization parameters. These must be 
supplied as an output:serialization-parameters element, having the format described in
[Section 3.1 Setting Serialization Parameters by Means of a Data Model Instance](https://www.w3.org/TR/xslt-xquery-serialization-31/#serparams-in-xdm-instance)

## Wikitext functions

Functions for the conversion of several wiki dialects to XHTML.

Namespace: http://www.armatiek.com/saxon/functions/wikitext

- wiki:confluence-2-html($text as xs:string) as node()? 
- wiki:markdown-2-html($text as xs:string) as node()? 
- wiki:mediawiki-2-html($text as xs:string) as node()? 
- wiki:textile-2-html($text as xs:string) as node()? 
- wiki:tracwiki-2-html($text as xs:string) as node()? 
- wiki:twiki-2-html($text as xs:string) as node()? 

## XML Canonicalization functions

Function for the Canonicalization (c14n) of XML. 

Namespace: http://www.armatiek.com/saxon/functions/canonicalization

- c14n:canonicalize-xml($xml as xs:string) as xs:string


## Using the Extension functions

### Command line

`mvn package`

Create a saxon configuration file and add functions to use. 

config.xml
```config.xml
<configuration xmlns="http://saxon.sf.net/ns/configuration" edition="HE">
    <resources>
      <extensionFunction>nl.armatiek.saxon.extensions.canonicalization.CanonicalizeXML</extensionFunction>
   </resources>
</configuration>
```
 input.xml
```input.xml
<?xml version="1.0" encoding="UTF-8"?>
<_/>
```

test.xsl
```test.xsl
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:c14n="http://www.armatiek.com/saxon/functions/canonicalization"
    exclude-result-prefixes="xs"
    version="3.0">    
    <xsl:template name="saxon-extensions">
        <xsl:sequence select="c14n:canonicalize-xml('&lt;_/>')"/>
    </xsl:template>
</xsl:stylesheet>
```

```
java -cp saxon.jar:canonicalization/target/saxon-ext-canonicalization-1.1-uber.jar net.sf.saxon.Transform  -config:config.xml -it:saxon-extensions -xsl:test.xsl -o:output.xml
```
