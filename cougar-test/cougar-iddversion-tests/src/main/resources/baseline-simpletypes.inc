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
	<simpleType name="SimpleDouble" type="double"/>
	<simpleType name="SimpleString" type="string"/>

	<simpleType name="AsyncBehaviour" type="string">
		<validValues>
			<value name="SYNC">
				<description>synchronous</description>
			</value>
			<value name="ASYNC">
				<description>asynchronous</description>
			</value>
			<value name="TIMEOUT">
				<description>timeout</description>
			</value>
		</validValues>
	</simpleType>	
	
	<simpleType name="SimpleEnum" type="string">
		<validValues>
					<value name="FOO">
						<description>FOO Enum</description>
					</value>
					<value name="BAR">
						<description>BAR Enum</description>
					</value>
					<value name="FOOFOO">
						<description>FOOFOO Enum</description>
					</value>
					<value name="FOOBAR">
						<description>FOOBAR Enum</description>
					</value>
					<value name="BARBAR">
						<description>BARBAR Enum</description>
					</value>
					<value name="BARFOO">
						<description>BARFOO Enum</description>
					</value>
				</validValues>
	</simpleType>
	
	
	<simpleType name="CougarComponentStatuses" type="string">
		<validValues>
            <value name="OK">
                <description>OK Status</description>
            </value>
            <value name="WARN">
                <description>WARN Status</description>
            </value>
            <value name="FAIL">
                <description>FAIL Status</description>
            </value>
        </validValues>
	</simpleType>
	
	<simpleType name="SupportedCaches" type="string">
		<validValues>
					<value name="SimpleCache1">
						<description>SimpleCache1 Enum</description>
					</value>
					<value name="SimpleCache2">
						<description>SimpleCache2 Enum</description>
					</value>
				</validValues>
	</simpleType>
</sharedTypes>