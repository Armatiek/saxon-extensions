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

## YAML parse and serialization functions

Functions to parse and serialize [YAML](https://yaml.org/). These functions are based on the Java 
library [SnakeYAML](https://bitbucket.org/asomov/snakeyaml/wiki/Documentation). 

Namespace: http://www.armatiek.com/saxon/functions/yaml

Functions:

- yaml:yaml-to-xml($yaml as xs:string) as document-node()*
- yaml:yaml-to-xml($yaml as xs:string, $options as map(\*)) as document-node()*
- yaml:xml-to-yaml($nodes as node()*) as xs:string
- yaml:xml-to-yaml($nodes as node()*, $options as map(\*)) as xs:string

Options for yaml:yaml-to-xml(), see also [LoaderOptions](https://www.javadoc.io/doc/org.yaml/snakeyaml/latest/org/yaml/snakeyaml/LoaderOptions.html).

| Name  | Type | Description |
| --- | --- | --- |
| allow-duplicate-keys  | xs:boolean  | Allow/Reject duplicate map keys in the YAML file. Default is to allow. |
| allow-recursive-keys  | xs:boolean  | Allow recursive keys for mappings. By default it is not allowed. |
| enum-case-sensitive  | xs:boolean  | Disables or enables case sensitivity during construct enum constant from string value Default is false. |
| max-aliases-for-collections  | xs:integer  | Restrict the amount of aliases for collections (sequences and mappings) to avoid https://en.wikipedia.org/wiki/Billion_laughs_attack |
| process-comments  | xs:boolean  | Set the comment processing. By default comments are ignored. |

Options for yaml:xml-to-yaml(), see also [DumperOptions](https://www.javadoc.io/doc/org.yaml/snakeyaml/latest/org/yaml/snakeyaml/DumperOptions.html).

| Name  | Type | Description |
| --- | --- | --- |
| allow-unicode          | xs:boolean  | Specify whether to emit non-ASCII printable Unicode characters. The default value is true.  |
| canonical              | xs:boolean  | Force the emitter to produce a canonical YAML document. |
| default-flow-style     | ['auto', 'block', 'flow'] |  |
| default-scalar-style   | ['double_quoted', 'folded', 'literal', 'plain', 'single_quoted'] |  |
| explicit-end           | xs:boolean  |  |
| explicit-start         | xs:boolean  |  |
| indent                 | xs:integer  |  |
| indent-with-indicator  | xs:boolean  | Set to true to add the indent for sequences to the general indent  |
| indicator-indent       | xs:integer  | Set number of white spaces to use for the sequence indicator '-' |
| line-break             | ['mac', 'unix', 'win'] | Specify the line break to separate the lines.  |
| max-simple-key-length  | xs:integer | Define max key length to use simple key (without '?') More info https://yaml.org/spec/1.1/#id934537 |
| non-printable-style    | ['binary', 'escape']  | When String contains non-printable characters they will be converted to binary data with the !!binary tag.  |
| pretty-flow            | xs:boolean | Force the emitter to produce a pretty YAML document when using the flow style.  |
| split-lines            | xs:boolean | Specify whether to split lines exceeding preferred width for scalars. |
| tags                   | map(xs:string, xs:string) |  |
| time-zone              | xs:string eg 'UTC' | Set the timezone to be used for dates. |
| version                | ['v1_0', 'v1_1'] |  |
| width                  | xs:integer | Specify the preferred width to emit scalars. |

Example of XML representation of YAML:

Input YAML:
```yaml
---
 doe: "a deer, a female deer"
 ray: "a drop of golden sun"
 date: 2001-12-14T21:59:43.10-05:00
 pi: 3.14159
 xmas: true
 french-hens: 3
 calling-birds:
   - huey
   - dewey
   - louie
   - fred
 xmas-fifth-day:
   calling-birds: four
   french-hens: 3
   golden-rings: 5
   partridges:
     count: 1
     location: "a pear tree"
   turtle-doves: two
 amount: ~
 foo: True
 picture: !!binary |
  R0lGODdhDQAIAIAAAAAAANn
  Z2SwAAAAADQAIAAACF4SDGQ
  ar3xxbJ9p0qa7R0YxwzaFME
  1IAADs=
```

Result of yaml:yaml-to-xml:
```xml
<map xmlns="http://www.w3.org/2005/xpath-functions">
   <string key="doe">a deer, a female deer</string>
   <string key="ray">a drop of golden sun</string>
   <date key="date">2001-12-15+01:00</date>
   <number key="pi">3.14159</number>
   <boolean key="xmas">true</boolean>
   <integer key="french-hens">3</integer>
   <array key="calling-birds">
      <string>huey</string>
      <string>dewey</string>
      <string>louie</string>
      <string>fred</string>
   </array>
   <map key="xmas-fifth-day">
      <string key="calling-birds">four</string>
      <integer key="french-hens">3</integer>
      <integer key="golden-rings">5</integer>
      <map key="partridges">
         <integer key="count">1</integer>
         <string key="location">a pear tree</string>
      </map>
      <string key="turtle-doves">two</string>
   </map>
   <null key="amount"/>
   <boolean key="foo">true</boolean>
   <base64Binary key="picture">R0lGODdhDQAIAIAAAAAAANnZ2SwAAAAADQAIAAACF4SDGQar3xxbJ9p0qa7R0YxwzaFME1IAADs=</base64Binary>
</map>
```

De following elements/datatypes are supported:

- map
- array
- string
- integer
- long
- number
- float
- double
- boolean
- date
- dateTime
- null
- base64Binary

## Wikitext functions

Functions for the conversion of several wiki dialects to XHTML.

Namespace: http://www.armatiek.com/saxon/functions/wikitext

- wiki:asciidoc-2-html($text as xs:string) as node()?
- wiki:commonmark-2-html($text as xs:string) as node()?
- wiki:confluence-2-html($text as xs:string) as node()? 
- wiki:creole-2-html($text as xs:string) as node()?
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

#### config.xml:
```config.xml
<configuration xmlns="http://saxon.sf.net/ns/configuration" edition="HE">
    <resources>
      <extensionFunction>nl.armatiek.saxon.extensions.canonicalization.CanonicalizeXML</extensionFunction>
   </resources>
</configuration>
```
#### input.xml:
```input.xml
<?xml version="1.0" encoding="UTF-8"?>
<_/>
```

#### test.xsl:
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
java -cp saxon.jar:canonicalization/target/saxon-ext-canonicalization-1.4-uber.jar net.sf.saxon.Transform  -config:config.xml -it:saxon-extensions -xsl:test.xsl -o:output.xml
```
