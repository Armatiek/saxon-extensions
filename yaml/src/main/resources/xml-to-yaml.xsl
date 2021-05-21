<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:yaml="http://www.armatiek.com/saxon/functions/yaml"
  exclude-result-prefixes="xs yaml"
  version="3.0">
  
  <xsl:output method="text" indent="yes"/>
  
  <xsl:template name="run-sample">
    <xsl:variable name="xml" as="element()">
      <map xmlns="http://www.w3.org/2005/xpath-functions">
        <string key="doe">a deer, a female deer</string>
        <string key="ray">a drop of golden sun</string>
        <date key="x">2001-12-15</date>
        <number key="pi">3.14159</number>
        <boolean key="xmas">true</boolean>
        <string key="french-hens">3</string>
        <array key="calling-birds">
          <string>huey</string>
          <string>dewey</string>
          <string>louie</string>
          <string>fred</string>
        </array>
        <map key="xmas-fifth-day">
          <string key="calling-birds">four</string>
          <string key="french-hens">3</string>
          <string key="golden-rings">5</string>
          <map key="partridges">
            <string key="count">1</string>
            <string key="location">a pear tree</string>
          </map>
          <string key="turtle-doves">two</string>
          <null key="null1"/>
          <float key="test1">1.0</float>
          <double key="test3">2.1</double>
          <base64Binary key="picture">R0lGODdhDQAIAIAAAAAAANnZ2SwAAAAADQAIAAACF4SDGQar3xxbJ9p0qa7R0YxwzaFME1IAADs=</base64Binary>
        </map>
      </map>
    </xsl:variable>
    <xsl:variable name="options" as="map(xs:string, item())">
      <xsl:map>
        <xsl:map-entry key="'allow-unicode'" select="true()"/> 
        <xsl:map-entry key="'canonical'" select="false()"/> 
        <xsl:map-entry key="'default-flow-style'" select="'auto'"/> 
        <xsl:map-entry key="'default-scalar-style'" select="'double_quoted'"/> 
        <xsl:map-entry key="'explicit-end'" select="true()"/> 
        <xsl:map-entry key="'explicit-start'" select="true()"/> 
        <xsl:map-entry key="'indent'" select="2"/> 
        <xsl:map-entry key="'indent-with-indicator'" select="true()"/> 
        <xsl:map-entry key="'indicator-indent'" select="2"/> 
        <xsl:map-entry key="'line-break'" select="'unix'"/> 
        <xsl:map-entry key="'max-simple-key-length'" select="90"/> 
        <xsl:map-entry key="'non-printable-style'" select="'escape'"/> 
        <xsl:map-entry key="'pretty-flow'" select="true()"/>
        <xsl:map-entry key="'split-lines'" select="true()"/> 
        <xsl:map-entry key="'tags'">
          <xsl:map>
            <xsl:map-entry key="'!foo!'" select="'bar'"/>
            <xsl:map-entry key="'!yaml!'" select="'tag:yaml.org,2002:'"/>
          </xsl:map>
        </xsl:map-entry> 
        <xsl:map-entry key="'time-zone'" select="'UTC'"/> 
        <xsl:map-entry key="'version'" select="'v1_1'"/> 
        <xsl:map-entry key="'width'" select="80"/>
      </xsl:map>
    </xsl:variable>    
    <xsl:sequence select="yaml:xml-to-yaml($xml, $options)"/>
  </xsl:template>
  
</xsl:stylesheet>