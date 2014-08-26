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

package com.betfair.cougar.transformations.manglers;

import java.util.List;

import org.w3c.dom.Node;

import com.betfair.cougar.codegen.ValidationException;

/**
 * Scan the document for shared include files and flatten thrir structure
 */
public class CommonTypesMangler extends AbstractMangler {

	@Override
	public String getName() {
		return "Common Type Mangler";
	}

	@Override
	public void mangleDocument(Node doc) {
		try {
			// First thing to is to get all sharedObject types defined in the IDL
			List<Node> sharedTags = getChildrenWithName(getName(), doc, "sharedTypes");
			for (Node st: sharedTags) {
		         while (st.hasChildNodes()) {
		             doc.appendChild(st.getFirstChild());
	             }
		        doc.removeChild(st);
			}
		} catch (ValidationException e) {
			throw new IllegalArgumentException("Unable to mangle document", e);
		}

	}
}
