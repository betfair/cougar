/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.transport.jms;

public class JMSPropertyConstants {
    public static final String MESSAGE_ID_FIELD_NAME = "__cougar_messageid";

    public static final String MESSAGE_ROUTING_FIELD_NAME = "__cougar_message_route";

    public static final String COUGAR_ROUTING_SEPARATOR="|";
    public static final String TIMESTAMP_SEPARATOR="@";

    public static final String EVENT_VERSION_FIELD_NAME = "__cougar_event_version";
    public static final String EVENT_NAME_FIELD_NAME = "__cougar_event_name";
}
