<sharedTypes>
    <dataType name="ComplexObject">
        <description>A (slightly) complex object</description>
        <parameter name="name" type="string" mandatory="false">
            <description>name</description>
        </parameter>
        <parameter name="value1" type="i32" mandatory="true">
            <description>value1</description>
        </parameter>
        <parameter name="value2" type="i32">
            <description>value2</description>
        </parameter>
        <parameter name="ok" type="bool">
            <description>some old stuff</description>
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
        <parameter name="allDates" type="list(string)">
            <description>The dates passed as strings</description>
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
</sharedTypes>