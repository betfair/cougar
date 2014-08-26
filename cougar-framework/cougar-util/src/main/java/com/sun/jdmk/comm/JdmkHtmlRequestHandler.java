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

package com.sun.jdmk.comm;

import java.net.Socket;

import javax.management.MBeanServer;

/**
 * HtmlRequestHandler is package private, so we just extend to make it public..
 */
public class JdmkHtmlRequestHandler extends HtmlRequestHandler {

	public JdmkHtmlRequestHandler(Socket socket, HtmlAdaptorServer htmlAdaptorServer, MBeanServer mBeanServer, int activeClientCount) {
		super(socket, htmlAdaptorServer, mBeanServer, htmlAdaptorServer.getObjectName(), activeClientCount);
	}
}
