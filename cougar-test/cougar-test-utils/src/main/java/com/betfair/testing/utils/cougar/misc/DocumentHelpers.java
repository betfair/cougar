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

package com.betfair.testing.utils.cougar.misc;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class DocumentHelpers {

	//Tidy is a syntax checker and parser for html and can provide a DOM implementation to the html
	private Tidy tidy;
	
	public DocumentHelpers() {
			
	}
	
	/**
	 * Return a document based on file,
	 * Document tpe 
	 * 
	 * @param file
	 * @return
	 */
	public Document parseFileToDocument(File file, Boolean setXML, Boolean setXHTML, Boolean setParseMark, String  docType){
		
		tidy = new Tidy();
		
		//default format is XML 
		tidy.setXmlOut(setXML);
		
		//set if extensible html
		tidy.setXHTML(setXHTML);
		
		//this removes the tidy meta tag in the header
		tidy.setTidyMark(setParseMark);
		
		tidy.setDocType(docType);
		
		FileInputStream fis;
		Document document = null;
		try {
			fis = new FileInputStream(file);
		
			//parse input stream and return a DOM Document
			document = tidy.parseDOM(fis, null);
			
			fis.close();

		} catch (Exception e) {
			throw new RuntimeException("Unable to parse the file :" + file.getAbsolutePath() + " throws", e);
		}
		
		return document;
	}
	
	/**
	 * Return a document from the input stream
	 * 
	 * @param is
	 * @return
	 */
	public Document parseInputStreamToDocument(InputStream is, Boolean setXML, Boolean setXHTML, Boolean setParseMark, String  docType){
		
		tidy = new Tidy();
		
		//default format is XML 
		tidy.setXmlOut(setXML);
		
		//set if extensible html
		tidy.setXHTML(setXHTML);
		
		//this removes the tidy meta tag in the header
		tidy.setTidyMark(setParseMark);
		
		tidy.setDocType(docType);
		
		Document document = null;
		try {
			//parse input stream and return a DOM Document
			document = tidy.parseDOM(is, null);

		} catch (Exception e) {
			throw new RuntimeException("Unable to parse the given input stream ");
		}
		
	
		
		return document;
	}
	
	
}
