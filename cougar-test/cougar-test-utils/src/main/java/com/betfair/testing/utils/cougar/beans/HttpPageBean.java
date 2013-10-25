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

package com.betfair.testing.utils.cougar.beans;

public class HttpPageBean {
	private String loadedURL;
	private String requestedURL;
	private String protocol;
	private String host;
	private int port;
	private String link;
	private String pageText;
	private String authusername; 
	private String authpassword; 
	private boolean pageLoaded;
	private byte[] buffer;

	public boolean getPageLoaded() {
		return pageLoaded;
	}
	public void setPageLoaded(boolean pageLoaded) {
		this.pageLoaded = pageLoaded;
	}
	public String getLoadedURL() {
		return loadedURL;
	}
	public void setLoadedURL(String loadedURL) {
		this.loadedURL = loadedURL;
	}

	public void setURLParts(String protocol, String host, int port, String link) {
		this.setProtocol(protocol);
		this.setHost(host);
		this.setLink(link);
		this.setPort(port);
		this.setRequestedURL(protocol+"://"+host+":"+port+link);
	}
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	public String getProtocol(){
		return protocol;
	}
	public void setHost(String host){
		this.host = host;
	}
	public String getHost(){
		return host;
	}
	public void setPort(int port){
		this.port = port;
	}
	public int getPort(){
		return port;
	}
	public void setLink(String link){
		this.link = link;
	}
	public String getLink(){
		return link;
	}
	public String getRequestedURL(){
		return requestedURL;
	}
	public void setRequestedURL(String url){
		this.requestedURL = url;
	}
	public String getPageText() {
		return pageText;
	}
	public void setPageText(String pageText) {
		this.pageText = pageText;
	}
	public String getAuthusername() {
		return authusername;
	}
	public void setAuthusername(String authusername) {
		this.authusername = authusername;
	}
	public String getAuthpassword() {
		return authpassword;
	}
	public void setAuthpassword(String authpassword) {
		this.authpassword = authpassword;
	}
	public HttpPageBean getMe() {
		return this; 
	}
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}
	public byte[] getBuffer() {
		return buffer;
	}
	
}

