/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.testing.utils.cougar.helpers;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Class for parsing HTML documents to find links and other resources

public class HttpHelpers {
	
	public Document parseInputStream(InputStream is){
		Tidy tidy = new Tidy();
	    tidy.setQuiet(true);
	    tidy.setShowWarnings(false);
		Document d = tidy.parseDOM(is, null);
		
		return d;
	}
	
	 public List<String> getLinks(Document d, String regex) {
	     return getULRFromTag("link", "href", regex,d);
	 }

	 public List<String> getAnchoredLinks(Document d, String regex) {
	     return getULRFromTag("a", "href", regex,d);
	 }

	 public List<String> getScripts(Document d, String regex) {
	     return getULRFromTag("script", "src", regex,d);
	 }

	 public List<String> getImages(Document d, String regex) {
	     return getULRFromTag("img", "src", regex,d);
	 }
	 
	 private List<String> getULRFromTag(String name, String attribute, String regex,Document d) {
        ArrayList<String> links = new ArrayList<String>();
        NodeList nodes = d.getElementsByTagName(name);
        for (int p = 0; p < nodes.getLength(); p++) {
        	try{
	            String url = nodes.item(p).getAttributes().getNamedItem(attribute).getNodeValue();
	            if (regex==null || url.matches(regex)) {
	                links.add(url);
	            }
        	} catch(DOMException e){
        		
        	}
           
        }
        return links;
    }
}
