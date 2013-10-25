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

package com.betfair.testing.utils.cougar.manager;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.testing.utils.cougar.beans.HttpPageBean;
import com.betfair.testing.utils.cougar.helpers.HttpHelpers;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class HttpPageManager {
	
	private HttpHelpers httpHelpers;

	public HttpPageManager(){
		httpHelpers = new HttpHelpers();
	}
	
	public int getPage(HttpPageBean bean) {

		// Get bean properties
		String requestedProtocol = bean.getProtocol();
		String requestedHost = bean.getHost();
		int requestedPort = bean.getPort();
		String requestedLink = bean.getLink();
		String username = bean.getAuthusername();
		String password = bean.getAuthpassword();

        final SSLSocketFactory sf = new SSLSocketFactory(createEasySSLContext(),SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme https = new Scheme("https", 9999, sf);

		// Set up httpClient to use given auth details and protocol
        DefaultHttpClient client = new DefaultHttpClient();
        client.getConnectionManager().getSchemeRegistry().register(https);
        client.getCredentialsProvider().setCredentials(new AuthScope("localhost", AuthScope.ANY_PORT),new UsernamePasswordCredentials(username, password));
        
		int status = -1;

        InputStream inputStream = null;
		// Make the request
		try{
            final HttpGet httpget = new HttpGet(URIUtils.createURI(requestedProtocol,requestedHost,requestedPort,requestedLink,null,null));
            final HttpResponse httpResponse = client.execute(httpget);
            inputStream = httpResponse.getEntity().getContent();
            status = httpResponse.getStatusLine().getStatusCode();
          
			if(status == HttpStatus.SC_OK){
				bean.setPageLoaded(true);
			 
				byte[] buffer = new byte[(int)httpResponse.getEntity().getContentLength()];
				int read;
				int count = 0;
				while((read = inputStream.read()) != -1){
					buffer[count] = (byte) read;
					count++;
				}
				bean.setPageText(new String(buffer, "UTF-8"));
				bean.setBuffer(buffer);	
			}
		} catch(IOException e1){
            return -1;
		} catch (URISyntaxException e) {
            return -1;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return status;
	}

    public boolean endPointExists(List<String> endpointList, String regex) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);

        // Check each end point in the list for one that matches the given regex
        for (String s : endpointList) {
            Matcher m  = p.matcher(s );
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

	public List<String> parseJmxEndpointPage(HttpPageBean bean) {
		String page = bean.getPageText();

        List<String> endpointList= new ArrayList<String>();

        // Find end points for each protocol used by Cougar (RESCRIPT, SOAP, JSONRPC)
        for (Protocol p : Protocol.values()) {
            int start=0, end=0, absoluteFilePos = 0;
            boolean done = false;
            while (!done)   {
                start = page.substring(absoluteFilePos).indexOf(p.toString());
                if (start != -1) {
                    end = page.substring(absoluteFilePos+start).indexOf("<br>");
                    if (end != -1) {
                        endpointList.add(page.substring(absoluteFilePos+start, absoluteFilePos+start+end).trim());
                        absoluteFilePos+= start+end;
                    }
                } else {
                    done = true;
                }
            }
        }
        return endpointList;
		
	}
	
	public boolean stringExistsOnPage(HttpPageBean bean, String doesItExist) {
        return bean.getPageText().contains(doesItExist);
    }
	
	public boolean clickOnLink(HttpPageBean bean, String linkType, String linkName){
		
		// Get all the links on the page that match the input type and name (using a regex)
		getPage(bean);
		ByteArrayInputStream responseStream = null;
		Document d = null;
		try {
			responseStream =  new ByteArrayInputStream(bean.getBuffer());
			d = httpHelpers.parseInputStream(responseStream);
			
			String regex = "/ViewObjectRes//.*"+linkType+".*"+linkName;
			List<String> links = httpHelpers.getAnchoredLinks(d, regex);
			String linkString = links.get(0);
			
			if(linkString == null){	// request link can't be found on page
				return false;
			}
					
			// Set request URL to that of the link to be clicked (trimming any trailing /)
			String currentPageURL = bean.getRequestedURL();
			if(currentPageURL.endsWith("/")){
				currentPageURL = currentPageURL.substring(0,currentPageURL.length()-1);
			}				
			bean.setRequestedURL(currentPageURL+linkString);
			
			// Reset flag and get linked page
			bean.setPageLoaded(false); 
			getPage(bean);
			return bean.getPageLoaded();
		} finally {
			if(responseStream != null) {
				try {
					responseStream.close();
				} catch (IOException e) {
					// log
				}
			}
		}
	}
	
    private SSLContext createEasySSLContext() {
        SSLContext context=null;
        try {
            context = SSLContext.getInstance("SSL");
            context.init(new KeyManager[0],new TrustManager[]{new NaiveTrustManager()},new SecureRandom());
        } catch(NoSuchAlgorithmException e) {
        } catch(KeyManagementException e){
        }
        return context;
    }
}



