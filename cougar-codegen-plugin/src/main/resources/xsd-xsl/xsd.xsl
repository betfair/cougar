<?xml version="1.0"?>
<!--
  ~ Copyright 2014, The Sporting Exchange Limited
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="1.0" xmlns:exsl="http://exslt.org/common"  extension-element-prefixes="exsl">
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="indent" select="3"/>
	<xsl:key name="simpleTypes" match="simpleType" use="@name"/>
	<xsl:key name="simpleTypesNotEnum" match="simpleType[not(*)]" use="@name"/>
	<xsl:template match="/">
		<xsl:variable name="majorVersion">
			<xsl:call-template name="majorVersion">
				<xsl:with-param name="version" select="interface/@version"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="majorMinorVersion">
			<xsl:call-template name="majorMinorVersion">
				<xsl:with-param name="version" select="interface/@version"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="typesTarget">http://www.betfair.com/servicetypes/v<xsl:value-of select="$majorVersion"/>/<xsl:value-of select="interface/@name"/>/</xsl:variable>
        <xsl:text disable-output-escaping="yes"><![CDATA[<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tns="]]></xsl:text><xsl:value-of select="$typesTarget"/><xsl:text><![CDATA[" targetNamespace="]]></xsl:text><xsl:value-of select="$typesTarget"/><xsl:text disable-output-escaping="yes"><![CDATA[" elementFormDefault="qualified">]]></xsl:text>
        <!--<xsd:schema xmlns:tns="{$typesTarget}" targetNamespace="{$typesTarget}" elementFormDefault="qualified">-->
            <!--<xsl:namespace-alias stylesheet-prefix="tns" result-prefix="tns"/>-->
            <xsd:annotation>
                <xsd:documentation>
                    <xsl:value-of select="interface/@name"/>.xsd v<xsl:value-of select="$majorMinorVersion"/>
                </xsd:documentation>
            </xsd:annotation>
            <!-- Build the wrappers for request/response -->
            <xsl:for-each select="interface/operation">
                <xsl:variable name="elementName">
                    <xsl:call-template name="capitaliseFirst">
                        <xsl:with-param name="string" select="@name"/>
                    </xsl:call-template>
                </xsl:variable>
                <!-- Requests -->
                <xsd:element name="{$elementName}Request" type="tns:{$elementName}RequestType"/>
                <xsd:complexType name="{$elementName}RequestType">
                    <xsd:all>
                        <xsl:for-each select="parameters/request/parameter">
                            <xsl:call-template name="complexBody">
                                <xsl:with-param name="name" select="@name"/>
                                <xsl:with-param name="type" select="@type"/>
                                <xsl:with-param name="mandatory" select="@mandatory"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsd:all>
                </xsd:complexType>

                <!-- Response -->
                <xsd:element name="{$elementName}Response" type="tns:{$elementName}ResponseType"/>
                <xsd:complexType name="{$elementName}ResponseType">
                    <xsl:choose>
                        <xsl:when test="parameters/simpleResponse">
                            <xsl:if test="parameters/simpleResponse/@type != 'void'">
                                <xsd:all>
                                    <xsl:call-template name="complexBody">
                                        <xsl:with-param name="name" select="'response'"/>
                                        <xsl:with-param name="type" select="parameters/simpleResponse/@type"/>
                                        <xsl:with-param name="mandatory" select="parameters/simpleResponse/@mandatory"/>
                                    </xsl:call-template>
                                </xsd:all>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="parameters/complexResponse">
                            <xsd:all>
                                <xsl:for-each select="parameters/complexResponse/parameter">
                                    <xsl:call-template name="complexBody">
                                        <xsl:with-param name="name" select="@name"/>
                                        <xsl:with-param name="type" select="@type"/>
                                        <xsl:with-param name="mandatory" select="@mandatory"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsd:all>
                        </xsl:when>
                    </xsl:choose>
                </xsd:complexType>
            </xsl:for-each>
            <!-- Build the struts -->
            <xsl:for-each select="interface//dataType">
                <xsd:complexType name="{@name}Type">
                    <xsd:all>
                        <xsl:for-each select="parameter">
                            <xsl:call-template name="complexBody">
                                <xsl:with-param name="name" select="@name"/>
                                <xsl:with-param name="type" select="@type"/>
                                <xsl:with-param name="mandatory" select="@mandatory"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsd:all>
                </xsd:complexType>
            </xsl:for-each>
            <xsl:for-each select="interface/exceptionType">
                <xsd:element name="{@name}" type="tns:{@name}Type"/>
                <xsd:complexType name="{@name}Type">
                    <xsd:all>
                        <xsl:for-each select="parameter">
                            <xsl:call-template name="complexBody">
                                <xsl:with-param name="name" select="@name"/>
                                <xsl:with-param name="type" select="@type"/>
                                <xsl:with-param name="mandatory" select="@mandatory"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsd:all>
                </xsd:complexType>
            </xsl:for-each>
        <xsl:text disable-output-escaping="yes"><![CDATA[</xsd:schema>]]></xsl:text>
	</xsl:template>

	<xsl:template name="capitaliseFirst">
		<xsl:param name="string"/>
		<xsl:call-template name="uppercase"><xsl:with-param name="string" select="substring($string, 1, 1)"/></xsl:call-template><xsl:value-of select="substring($string, 2)"/>
	</xsl:template>

	<xsl:template name="lowercaseFirst">
		<xsl:param name="string"/>
		<xsl:call-template name="lowercase"><xsl:with-param name="string" select="substring($string, 1, 1)"/></xsl:call-template><xsl:value-of select="substring($string, 2)"/>
	</xsl:template>

	<xsl:template name="uppercase">
		<xsl:param name="string"/>
		<xsl:value-of select="translate($string,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
	</xsl:template>

	<xsl:template name="lowercase">
		<xsl:param name="string"/>
		<xsl:value-of select="translate($string,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
	</xsl:template>

	<xsl:template name="majorVersion">
		<xsl:param name="version"/>
		<xsl:value-of select="substring-before($version,'.')"/>
	</xsl:template>

	<xsl:template name="majorMinorVersion">
		<xsl:param name="version"/>
		<xsl:variable name="major"><xsl:value-of select="substring-before($version,'.')"/></xsl:variable>
		<xsl:variable name="excludingMajor"><xsl:value-of select="substring-after($version,'.')"/></xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($excludingMajor, '.')">
				<xsl:value-of select="$major"/>.<xsl:value-of select="substring-before($excludingMajor,'.')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$version"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="mapType">
		<xsl:param name="nativeType"/>
		<xsl:choose>
			<xsl:when test="key('simpleTypes',$nativeType)">
				<xsl:for-each select="key('simpleTypes',$nativeType)">
					<xsl:call-template name="mapTypeDirect">
						<xsl:with-param name="nativeType" select="@type"/>
					</xsl:call-template>
				</xsl:for-each>
		    </xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="mapTypeDirect">
					<xsl:with-param name="nativeType" select="$nativeType"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="mapTypeIgnoreEnum">
		<xsl:param name="nativeType"/>
		<xsl:choose>
			<xsl:when test="key('simpleTypesNotEnum',$nativeType)">
				<xsl:for-each select="key('simpleTypes',$nativeType)">
					<xsl:call-template name="mapTypeDirect">
						<xsl:with-param name="nativeType" select="@type"/>
					</xsl:call-template>
				</xsl:for-each>
		    </xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="mapTypeDirect">
					<xsl:with-param name="nativeType" select="$nativeType"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="mapTypeDirect">
		<xsl:param name="nativeType"/>
		<xsl:choose>
			<xsl:when test="$nativeType = 'i64'">xsd:long</xsl:when>
			<xsl:when test="$nativeType = 'i32'">xsd:int</xsl:when>
			<xsl:when test="$nativeType = 'bool'">xsd:boolean</xsl:when>
			<xsl:when test="$nativeType = 'byte'">xsd:byte</xsl:when>
			<xsl:when test="$nativeType = 'float'">xsd:float</xsl:when>
			<xsl:when test="$nativeType = 'double'">xsd:double</xsl:when>
			<xsl:when test="$nativeType = 'string'">xsd:string</xsl:when>
			<xsl:when test="$nativeType = 'dateTime'">xsd:dateTime</xsl:when>
			<xsl:when test="$nativeType = 'list(byte)'">xsd:base64Binary</xsl:when>
			<xsl:when test="starts-with($nativeType, 'set')">
				<xsl:call-template name="mapType">
					<xsl:with-param name="nativeType" select="substring-before(substring-after($nativeType, '('), ')')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with($nativeType, 'list') and $nativeType != 'list(byte)'">
				<xsl:call-template name="mapType">
					<xsl:with-param name="nativeType" select="substring-before(substring-after($nativeType, '('), ')')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with($nativeType, 'map')">
				<xsl:variable name="keyType">
					<xsl:call-template name="mapType">
						<xsl:with-param name="nativeType" select="normalize-space(substring-before(substring-after($nativeType, '('), ','))"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="valueType">
					<xsl:call-template name="mapType">
						<xsl:with-param name="nativeType" select="normalize-space(substring-before(substring-after($nativeType, ','), ')'))"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$keyType"/>,<xsl:value-of select="$valueType"/>
			</xsl:when>
			<xsl:otherwise>tns:<xsl:value-of select="$nativeType"/>Type</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="mapTypeName">
        <xsl:param name="nativeType"/>
        <xsl:choose>
            <xsl:when test="$nativeType = 'xsd:long'">Long</xsl:when>
            <xsl:when test="$nativeType = 'xsd:int'">Integer</xsl:when>
            <xsl:when test="$nativeType = 'xsd:boolean'">Boolean</xsl:when>
            <xsl:when test="$nativeType = 'xsd:byte'">Byte</xsl:when>
            <xsl:when test="$nativeType = 'xsd:float'">Float</xsl:when>
            <xsl:when test="$nativeType = 'xsd:double'">Double</xsl:when>
            <xsl:when test="$nativeType = 'xsd:string'">String</xsl:when>
            <xsl:when test="$nativeType = 'xsd:dateTime'">Date</xsl:when>
            <xsl:when test="$nativeType = 'xsd:base64Binary'">byte[]</xsl:when>
            <xsl:otherwise>
            	<!-- these strings have 'Type' on the end (but always?). This paranoidly removes
            		the last 'Type' from the string if it exists. Can't use a simple
            		substring-before because that would shrink FooTypeType to Foo, instead of
            		FooType -->
            	<xsl:variable name="wrkName" select="substring-after($nativeType,':')"/>
            	<xsl:variable name="wrkType" select="'Type'"/>
            	<xsl:choose>
            		<!-- this is XSLT 1.0 equivalent of 'ends-with' -->
            		<xsl:when test="substring($wrkName, (string-length($wrkName) - string-length($wrkType)) + 1) = $wrkType">
            			<xsl:value-of select="substring($wrkName, 1, (string-length($wrkName) - 4))"/>
            		</xsl:when>
            		<xsl:otherwise>
            			<xsl:value-of select="$wrkName"/>
            		</xsl:otherwise>
            	</xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


	<xsl:template name="complexBody">
		<xsl:param name="type"/>
		<xsl:param name="name"/>
		<xsl:param name="mandatory"/>
		<xsl:variable name="xsdType">
			<xsl:call-template name="mapType">
				<xsl:with-param name="nativeType" select="$type"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="minOccurs">
			<xsl:choose>
				<xsl:when test="$mandatory = 'true'">1</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="(starts-with($type, 'set') or starts-with($type, 'list')) and ($type != 'list(byte)')">
				<xsl:variable name="genericType">
					<xsl:value-of select="substring-before(substring-after($type, '('), ')')"/>
				</xsl:variable>
		        <xsl:variable name="genericDeSimplifiedTypeName">
		            <xsl:call-template name="mapTypeIgnoreEnum">
		                <xsl:with-param name="nativeType" select="$genericType"/>
		            </xsl:call-template>
		        </xsl:variable>
				<xsl:variable name="genericTypeName">
		            <xsl:call-template name="mapTypeName">
		                <xsl:with-param name="nativeType" select="$genericDeSimplifiedTypeName"/>
		            </xsl:call-template>
		        </xsl:variable>
				<xsl:variable name="genericXsdType">
					<xsl:call-template name="mapType">
						<xsl:with-param name="nativeType" select="$genericType"/>
					</xsl:call-template>
				</xsl:variable>
				<xsd:element name="{$name}">
					<xsd:complexType>
						<xsd:sequence>
						 	<xsd:element name="{$genericTypeName}" type="{$genericXsdType}" minOccurs="{$minOccurs}" maxOccurs="unbounded" />
						</xsd:sequence>
					</xsd:complexType>
				</xsd:element>
			</xsl:when>
			<xsl:when test="starts-with($type, 'map')">
                <xsl:variable name="keyType">
                    <xsl:value-of select="normalize-space(substring-before($xsdType, ','))"/>
                </xsl:variable>
                <xsl:variable name="valueType">
                    <xsl:value-of select="normalize-space(substring-after($xsdType, ','))"/>
                </xsl:variable>
                <xsl:comment>map type</xsl:comment>
		        <xsl:variable name="mapValueName">
		            <xsl:call-template name="mapTypeName">
		                <xsl:with-param name="nativeType" select="$valueType"/>
		            </xsl:call-template>
		        </xsl:variable>
				<xsd:element name="{$name}" minOccurs="{$minOccurs}" maxOccurs="1">
					<xsd:complexType>
						   <xsd:sequence>
                                <xsd:element name="entry" minOccurs="0" maxOccurs="unbounded">
					                <xsd:complexType >
					                    <xsd:sequence>
					                      <xsd:element name="{$mapValueName}" type="{$valueType}" minOccurs="0"/>
					                    </xsd:sequence>
                                        <xsd:attribute name="key" type="{$keyType}" />
					                  </xsd:complexType>
                                 </xsd:element>
                           </xsd:sequence>
					</xsd:complexType>
				</xsd:element>
			</xsl:when>
			<xsl:otherwise>
				<xsd:element name="{$name}" type="{$xsdType}" minOccurs="{$minOccurs}" maxOccurs="1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
