<!--
  ~ Copyright 2013, The Sporting Exchange Limited
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
<sharedTypes>

    <dataType name="ComplexObject">
        <description>A (slightly) complex object</description>
        <parameter name="name" type="string" mandatory="false">
            <description>name</description>
        </parameter>
        <parameter name="value2" type="i32">
            <description>value2</description>
        </parameter>
        <parameter name="ok" type="bool">
            <description>something</description>
        </parameter>
    </dataType>

    <dataType name="DateContainer">
        <description>An object returning some dates</description>
        <parameter name="name" type="string" mandatory="true">
            <description>name</description>
        </parameter>
        <parameter name="first" type="dateTime" mandatory="true">
            <description>firstDate</description>
        </parameter>
        <parameter name="last" type="dateTime">
            <description>last</description>
        </parameter>
        <parameter name="difference" type="double">
            <description>difference in ms</description>
        </parameter>
        <parameter name="allDates" type="list(dateTime)">
            <description>The dates passed as strings</description>
        </parameter>
    </dataType>
	
	<dataType name="TestSimpleCacheGetResponseObject">
		<description>Object returned for i32Operation, each parameter is an Integer</description>
		<parameter mandatory="true" name="service" type="string">
			<description>Service cache held in</description>
		</parameter>
		<parameter mandatory="true" name="cache" type="string">
			<description>Name of cache</description>
		</parameter>
		<parameter mandatory="true" name="recordId" type="string">
			<description>Id of requested record</description>
		</parameter>
		<parameter mandatory="true" name="loadCount" type="string">
			<description>Load count when record loaded into cache</description>
		</parameter>
	</dataType>

    <dataType name="PrimitiveLists">
        <description>An object returning some lists and maps of primitives</description>
        <parameter name="dates" type="list(dateTime)">
            <description>The dates</description>
        </parameter>
        <parameter name="bytes" type="list(byte)">
            <description>The bytes</description>
        </parameter>
        <parameter name="i32s" type="list(i32)">
            <description>The i32</description>
        </parameter>
        <parameter name="i64s" type="list(i64)">
            <description>The i64</description>
        </parameter>
        <parameter name="floats" type="list(float)">
            <description>The floats</description>
        </parameter>
        <parameter name="doubles" type="list(double)">
            <description>The doubles</description>
        </parameter>
        <parameter name="strings" type="list(string)">
            <description>The strings</description>
        </parameter>
    </dataType>
        
    <dataType name="LargeRequest">
        <description>A list of complex objects from a request</description>
        <parameter name="size" type="i32" mandatory="false">
            <description>The size of the object list</description>
        </parameter>
        <parameter name="objects" type="list(ComplexObject)" mandatory="true">
            <description>the complex object list</description>
        </parameter>
        <parameter name="oddOrEven" type="string" mandatory="true">
            <description>the size parameter is...</description>
            <validValues>
                <value name="ODD">
                    <description>Odd</description>
                </value>
                <value name="EVEN">
                    <description>Even</description>
                </value>
            </validValues>
		</parameter>
        <parameter name="returnList" type="bool" mandatory="false">
			<description>Whether to return the list that was passed</description>
		</parameter>
    </dataType>

    
    <dataType name="MapDataType">
        <description>A map of simple types to complex objects from a request</description>
        <parameter name="cache" type="map(i32,ComplexObject)" mandatory="true">
            <description>the complex object list</description>
        </parameter>
        <parameter name="someMap" type="map(string,ComplexObject)" mandatory="true">
            <description>the complex object list</description>
        </parameter>
    </dataType>

    <dataType name="SimpleMap">
        <description>A map of simple types to complex objects from a request</description>
        <parameter name="cache" type="map(string,string)" mandatory="true">
            <description>the complex object list</description>
        </parameter>
    </dataType>

    <dataType name="SimpleContainer">
        <description>Some simple types in an object</description>
        <parameter name="simpleInt" type="SimpleInt">
            <description>A simple value</description>
        </parameter>
        <parameter name="simpleDouble" type="SimpleDouble">
            <description>A simple value</description>
        </parameter>
        <parameter name="simpleString" type="SimpleString">
            <description>A simple value</description>
        </parameter>
        <parameter name="simpleEnum" type="SimpleValidValue">
            <description>A simple value</description>
        </parameter>
    </dataType>

    <dataType name="SimpleListContainer">
        <description>Some simple types in an object</description>
        <parameter name="ints" type="list(i32)">
            <description>A simple value</description>
        </parameter>
        <parameter name="strings" type="list(string)">
            <description>A simple value</description>
        </parameter>
        <parameter name="enums" type="set(SimpleValidValue)">
            <description>A simple value</description>
        </parameter>
    </dataType> 
	
	<dataType name="EnumOperationResponseObject">
		<description>Object returned for enumOperation, each parameter is an enum</description>
		<parameter mandatory="true" name="queryParameter" type="string">
			<description>The passed queryParameter</description>
			<validValues>
						<value name="FooQuery">
							<description>Foo parameter</description>
						</value>
						<value name="BarQuery">
							<description>Bar parameter</description>
						</value>
						<value name="FooBarQuery">
							<description>FooBar parameter</description>
						</value>
					</validValues>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="string">
			<description>The passed headerParameter</description>
			<validValues>
						<value name="FooHeader">
							<description>Foo parameter</description>
						</value>
						<value name="BaroHeader">
							<description>Bar parameter</description>
						</value>
						<value name="FooBarHeader">
							<description>FooBar parameter</description>
						</value>
					</validValues>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="string">
			<description>The passed bodyParameter</description>
			<validValues>
						<value name="FooBody">
							<description>Foo parameter</description>
						</value>
						<value name="BarBody">
							<description>Bar parameter</description>
						</value>
						<value name="FooBarBody">
							<description>FooBar parameter</description>
						</value>
					</validValues>
		</parameter>
	</dataType>

	<dataType name="EnumSimpleResponseObject">
		<description>Object returned for enumOperation, each parameter is an enum</description>
		<parameter mandatory="true" name="queryParameter" type="SimpleEnum">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="SimpleEnum">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="SimpleEnum">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
		<dataType name="BodyParamEnumObject">
		<description>Post body Object required for enumOperation, parameter is an enum</description>
		<parameter mandatory="true" name="bodyParameter" type="string">
			<description>The passed bodyParameter</description>
			<validValues>
						<value name="FooBody">
							<description>Foo parameter</description>
						</value>
						<value name="BarBody">
							<description>Bar parameter</description>
						</value>
						<value name="FooBarBody">
							<description>FooBar parameter</description>
						</value>
			</validValues>
		</parameter>
	</dataType>

	<dataType name="EnumSimpleRequestObject">
		<description>Post body Object required for enumOperation, parameter is an enum</description>
		<parameter mandatory="true" name="bodyParameter" type="SimpleEnum">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="BodyParamI32Object">
		<description>Post body Object required for i32Operation, parameter is an Integer</description>
		<parameter mandatory="true" name="bodyParameter" type="i32">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="I32SimpleTypeRequestObject">
		<description>Post body Object required for i32SimpleOperation, parameter is an Integer</description>
		<parameter mandatory="true" name="bodyParameter" type="i32">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>


	<dataType name="BodyParamI64Object">
		<description>Post body Object required for i64Operation, parameter is a Long</description>
		<parameter mandatory="true" name="bodyParameter" type="i64">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
 	
	<dataType name="BodyParamByteObject">
		<description>Post body Object required for byte Operation, parameter is a Byte</description>
		<parameter mandatory="true" name="bodyParameter" type="list(byte)">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="I32OperationResponseObject">
		<description>Object returned for i32Operation, each parameter is an Integer</description>
		<parameter mandatory="true" name="queryParameter" type="i32">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="i32">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="i32">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="I32SimpleOperationResponseObject">
		<description>Object returned for i32Operation, each parameter is an Integer</description>
		<parameter mandatory="true" name="queryParameter" type="i32">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="i32">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="i32">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>


	<dataType name="I64OperationResponseObject">
		<description>Object returned for i64Operation, each parameter is a Long</description>
		<parameter mandatory="true" name="queryParameter" type="i64">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="i64">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="i64">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="ByteOperationResponseObject">
		<description>Object returned for byteOperation, each parameter is a Byte</description>
		<parameter mandatory="true" name="queryParameter" type="byte">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="byte">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="list(byte)">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="FloatOperationResponseObject">
		<description>Object returned for floatOperation, each parameter is a Float</description>
		<parameter mandatory="true" name="queryParameter" type="float">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="float">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="float">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="DoubleOperationResponseObject">
		<description>Object returned for doubleOperation, each parameter is a Double</description>
		<parameter mandatory="true" name="queryParameter" type="double">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="double">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="double">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="BoolOperationResponseObject">
		<description>Object returned for boolOperation, each parameter is a Bool</description>
		<parameter mandatory="true" name="queryParameter" type="bool">
			<description>The passed queryParameter</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="bool">
			<description>The passed headerParameter</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter" type="bool">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="NonMandatoryParamsRequest">
		<description>Simplest post body type with non-mandatory parameters</description>
		<parameter mandatory="false" name="bodyParameter1" type="string">
            <description>message</description>
        </parameter>
	</dataType>

	<dataType name="MandatoryParamsRequest">
		<description>Simplest post body type with mixture of mandatory and non-mandatory parameters</description>
		<parameter mandatory="true" name="bodyParameter1" type="string">
            <description>message</description>
        </parameter>
		<parameter mandatory="false" name="bodyParameter2" type="string">
            <description>message</description>
        </parameter>
	</dataType>
	
	<dataType name="NonMandatoryParamsOperationResponseObject">
		<description>Object returned for nonMandatoryParamsOperationObject, each parameter is a string.  No parameters are mandatory</description>
		<parameter mandatory="false" name="headerParameter" type="string">
			<description>The passed headerParameter. Not returned if not passed.</description>
		</parameter>
		<parameter mandatory="false" name="bodyParameter1" type="string">
			<description>The passed bodyParameter1. Not returned if not passed.</description>
		</parameter>
		<parameter mandatory="false" name="bodyParameter2" type="string">
			<description>The passed bodyParameter2. Not returned if not passed.</description>
		</parameter>
	</dataType>

	<dataType name="MandatoryParamsOperationResponseObject">
		<description>Object returned for mandatoryParamsOperationObject, each parameter is a string.  All parameters are mandatory</description>
		<parameter mandatory="true" name="queryParameter" type="string">
			<description>The passed queryParameter. Mandatory.</description>
		</parameter>
		<parameter mandatory="true" name="headerParameter" type="string">
			<description>The passed headerParameter. Mandatory.</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter1" type="string">
			<description>The passed bodyParameter1. Mandatory.</description>
		</parameter>
		<parameter mandatory="true" name="bodyParameter2" type="string">
			<description>The passed bodyParameter2. Mandatory.</description>
		</parameter>
	</dataType>

	
	<dataType name="BodyParamFloatObject">
		<description>Post body Object required for floatOperation, parameter is a Float</description>
		<parameter mandatory="true" name="bodyParameter" type="float">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="BodyParamDoubleObject">
		<description>Post body Object required for doubleOperation, parameter is a Double</description>
		<parameter mandatory="true" name="bodyParameter" type="double">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="BodyParamBoolObject">
		<description>Post body Object required for boolOperation, parameter is a Boolean</description>
		<parameter mandatory="true" name="bodyParameter" type="bool">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>
	
	<dataType name="HealthStatusInfoRequest">
		<description>Post body Object required for setHealthStatusInfo operation</description>
			<parameter mandatory="false" name="initialiseHealthStatusObject" type="bool">
				<description>Do you want to create a new HealthStatusDetail object</description>
			</parameter>
			<parameter mandatory="false" name="serviceStatusDetail" type="CougarComponentStatuses">
				<description>Detailed status of the service</description>
			</parameter>
			<parameter mandatory="false" name="DBConnectionStatusDetail" type="CougarComponentStatuses">
				<description>Detailed status of the DB Connection Component</description>
			</parameter>
			<parameter mandatory="false" name="cacheAccessStatusDetail" type="CougarComponentStatuses">
				<description>Detailed status of the Cache Access Component</description>
			</parameter>
	</dataType>
	
	<dataType name="BodyParamDateTimeObject">
		<description>Post body Object required for boolOperation, parameter is a Date</description>
		<parameter mandatory="true" name="dateTimeParameter" type="dateTime">
			<description>The passed bodyParameter</description>
		</parameter>
	</dataType>

	<dataType name="BodyParamDateTimeSetObject">
		<description>Post body Object required for dateTimeSetOperation, parameter is a Set(dateTime)</description>
		<parameter mandatory="true" name="dateTimeSet" type="set(dateTime)">
			<description>The passed dateTime Set</description>
		</parameter>
	</dataType>
	<dataType name="BodyParamDateTimeListObject">
		<description>Post body Object required for dateTimeListOperation, parameter is a List(dateTime)</description>
		<parameter mandatory="true" name="dateTimeList" type="list(dateTime)">
			<description>The passed dateTime Set</description>
		</parameter>
	</dataType>
	<dataType name="BodyParamDateTimeMapObject">
		<description>Post body Object required for dateTimeMapOperation, parameter is a Map(string,dateTime)</description>
		<parameter mandatory="true" name="dateTimeMap" type="map(string,dateTime)">
			<description>The passed dateTime Map</description>
		</parameter>
	</dataType>
	<dataType name="BodyParamMapDateTimeKeyObject">
		<description>Post body Object required for mapDateTimeKeyOperation, parameter is a Map(dateTime,string)</description>
		<parameter mandatory="true" name="mapDateTimeKey" type="map(dateTime,string)">
			<description>The passed map with a date time key</description>
		</parameter>
	</dataType>

	<dataType name="DateTimeOperationResponseObject">
		<description>Object returned for boolOperation, each parameter is a dateTime</description>
		<parameter mandatory="true" name="localTime" type="dateTime">
			<description>The passed bodyParameter dateTime converted to local timezone</description>
		</parameter>
		<parameter mandatory="true" name="localTime2" type="dateTime">
			<description>The passed bodyParameter dateTime converted to local timezone</description>
		</parameter>
	</dataType>
	<dataType name="DateTimeSetOperationResponseObject">
		<description>Object returned for DateTimeSetOperation</description>
		<parameter mandatory="true" name="responseSet" type="set(dateTime)">
			<description>The passed set is returned</description>
		</parameter>
	</dataType>
    <dataType name="DateTimeListOperationResponseObject">
		<description>Object returned for DateTimeListOperation</description>
		<parameter mandatory="true" name="responseList" type="list(dateTime)">
			<description>The passed set is returned</description>
		</parameter>
	</dataType>
    <dataType name="DateTimeMapOperationResponseObject">
		<description>Object returned for DateTimeMapOperation</description>
		<parameter mandatory="true" name="responseMap" type="map(string,dateTime)">
			<description>The passed map is returned</description>
		</parameter>
	</dataType>
	<dataType name="MapDateTimeKeyOperationResponseObject">
		<description>Object returned for MapDateTimeKeyOperation</description>
		<parameter mandatory="true" name="responseMap" type="map(dateTime,string)">
			<description>The passed map is returned</description>
		</parameter>
	</dataType>
	
	<dataType name="BodyParamSimpleMapObject">
		<description>Post body Object required for simpleMapOperation, parameter is a Map(String,String)</description>
		<parameter mandatory="true" name="simpleMap" type="map(string,string)">
			<description>The passed simple Map</description>
		</parameter>
	</dataType>

	<dataType name="BodyParamComplexMapObject">
		<description>Post body Object required for complexMapOperation, parameter is a Map(String,SomeComplexObject)</description>
		<parameter mandatory="true" name="complexMap" type="map(string,SomeComplexObject)">
			<description>The passed complex Map</description>
		</parameter>
	</dataType>


	<dataType name="BodyParamSimpleSetObject">
		<description>Post body Object required for simpleSetOperation, parameter is a Set(String)</description>
		<parameter mandatory="true" name="simpleSet" type="set(string)">
			<description>The passed simple Set</description>
		</parameter>
	</dataType>

	<dataType name="BodyParamComplexSetObject">
		<description>Post body Object required for complexSetOperation, parameter is a Set(SomeComplexObject)</description>
		<parameter mandatory="true" name="complexSet" type="set(SomeComplexObject)">
			<description>The passed complex Set</description>
		</parameter>
	</dataType>

	<dataType name="SimpleMapOperationResponseObject">
		<description>Object returned for simpleMapOperation</description>
		<parameter mandatory="true" name="responseMap" type="map(string,string)">
			<description>The passed map is returned</description>
		</parameter>
	</dataType>

	<dataType name="ComplexMapOperationResponseObject">
		<description>Object returned for complexMapOperation</description>
		<parameter mandatory="true" name="responseMap" type="map(string,SomeComplexObject)">
			<description>The passed map is returned</description>
		</parameter>
	</dataType>

	<dataType name="SimpleSetOperationResponseObject">
		<description>Object returned for simpleSetOperation</description>
		<parameter mandatory="true" name="responseSet" type="set(string)">
			<description>The passed set is returned</description>
		</parameter>
	</dataType>

	<dataType name="ComplexSetOperationResponseObject">
		<description>Object returned for complexSetOperation</description>
		<parameter mandatory="true" name="responseSet" type="set(SomeComplexObject)">
			<description>The passed set is returned</description>
		</parameter>
	</dataType>
	
	<dataType name="SomeComplexObject">
		<description>A Vaguely Complex Object</description>
		<parameter mandatory="false" name="stringParameter" type="string">
			<description>A String</description>
		</parameter>
		<parameter mandatory="false" name="dateTimeParameter" type="dateTime">
			<description>A DateTime</description>
		</parameter>
		<parameter mandatory="false" name="enumParameter" type="string">
			<description>An Enum</description>
			<validValues>
				<value name="FOO">
					<description>Foo parameter</description>
				</value>
				<value name="BAR">
					<description>Bar parameter</description>
				</value>
				<value name="FOOBAR">
					<description>FooBar parameter</description>
				</value>
			</validValues>
		</parameter>
		<parameter mandatory="false" name="listParameter" type="list(string)">
			<description>A List of Strings</description>
		</parameter>
	</dataType>
	
</sharedTypes>