<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:yaml="http://www.armatiek.com/saxon/functions/yaml"
  exclude-result-prefixes="xs yaml"
  version="3.0">
  
  <xsl:output indent="yes"/>
  
  <xsl:template name="run-sample">
    <xsl:variable name="yaml" as="xs:string">
<![CDATA[
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
]]>
    </xsl:variable>    
    <xsl:variable name="options" as="map(xs:string, item())">
      <xsl:map>
        <xsl:map-entry key="'allow-duplicate-keys'" select="true()"/>
        <xsl:map-entry key="'allow-recursive-keys'" select="false()"/>
        <xsl:map-entry key="'enum-case-sensitive'" select="false()"/>
        <xsl:map-entry key="'max-aliases-for-collections'" select="50"/>
        <xsl:map-entry key="'process-comments'" select="true()"/>
      </xsl:map>
    </xsl:variable>
    <xsl:sequence select="yaml:yaml-to-xml($yaml, $options)"/>
  </xsl:template>
  
</xsl:stylesheet>