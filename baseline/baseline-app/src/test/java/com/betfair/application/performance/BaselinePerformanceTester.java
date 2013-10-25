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

package com.betfair.application.performance;

import com.betfair.application.util.BaselineClientConstants;
import com.betfair.application.util.HttpBodyBuilder;
import com.betfair.application.util.HttpCallLogEntry;
import com.betfair.application.util.HttpCallable;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class BaselinePerformanceTester implements BaselineClientConstants {

	// REQUEST MODELLING
    private static final int SOAP_COUNT;
    private static final int REST_XML_COUNT;
    private static final int REST_JSON_COUNT;
    
    private static final int SIMPLE_GET;
    private static final int LARGE_GET;
    private static final int COMPLEX_MUTATOR;
    private static final int LARGE_POST;
    private static final int EXCEPTION;
    private static final int STYLES;
    private static final int GET_TIMEOUT;
    private static final int DATES;
    private static final int MAPS;
    private static int TOTAL_CALL_NUMBER;
    

    private static final int NUM_THREADS;
    private static final int NUM_CALLS;
    private static final int TRACE_EVERY;
    private static final String HOST;
    private static final int PORT;
    private static final String SOAP_ENDPOINT;
    private static final String REST_BASE;
    
    private static final boolean CHECK_LOG;
    private static final Map<String, String> METHOD_NAMES = new HashMap<String, String>();
    
    static {
        // Read in the properties.
        try {
            Properties props = new Properties();
            InputStream is = BaselinePerformanceTester.class.getResourceAsStream("/perftester/perftest.properties");
            props.load(is);
            is.close();
            HOST = props.getProperty("HOST");
            PORT = Integer.parseInt(props.getProperty("PORT"));
            String nullPart = "";
            if (Boolean.valueOf(props.getProperty("NULLTEST"))) {
                nullPart = "/null";
            }
            SOAP_ENDPOINT = "http://"+HOST+":"+PORT+nullPart+"/BaselineService/v2.0";
            REST_BASE = "http://"+HOST+":"+PORT+nullPart+"/baseline/v2.0/";

            NUM_THREADS = Integer.parseInt(props.getProperty("NUM_THREADS"));
            NUM_CALLS = Integer.parseInt(props.getProperty("NUM_CALLS"));
            TRACE_EVERY = Integer.parseInt(props.getProperty("TRACE_EVERY"));
            
            SOAP_COUNT = Integer.parseInt(props.getProperty(SOAP));
            REST_XML_COUNT = Integer.parseInt(props.getProperty("REST_XML"));
            REST_JSON_COUNT = Integer.parseInt(props.getProperty("REST_JSON"));
            
            SIMPLE_GET = Integer.parseInt(props.getProperty("SIMPLE_GET")); TOTAL_CALL_NUMBER += SIMPLE_GET; METHOD_NAMES.put("SIMPLE_GET", "testSimpleGet"); 
            LARGE_GET = Integer.parseInt(props.getProperty("LARGE_GET")); TOTAL_CALL_NUMBER += LARGE_GET; METHOD_NAMES.put("LARGE_GET", "testLargeGet");
            COMPLEX_MUTATOR = Integer.parseInt(props.getProperty("COMPLEX_MUTATOR")); TOTAL_CALL_NUMBER += COMPLEX_MUTATOR; METHOD_NAMES.put("COMPLEX_MUTATOR", "testComplexMutator");
            LARGE_POST = Integer.parseInt(props.getProperty("LARGE_POST")); TOTAL_CALL_NUMBER += LARGE_POST; METHOD_NAMES.put("LARGE_POST", "testLargePost");
            EXCEPTION = Integer.parseInt(props.getProperty("EXCEPTION")); TOTAL_CALL_NUMBER += EXCEPTION; METHOD_NAMES.put("EXCEPTION", "testException");
            STYLES = Integer.parseInt(props.getProperty("STYLES")); TOTAL_CALL_NUMBER += STYLES; METHOD_NAMES.put("STYLES", "testParameterStyles");
            GET_TIMEOUT = Integer.parseInt(props.getProperty("GET_TIMEOUT")); TOTAL_CALL_NUMBER += GET_TIMEOUT; METHOD_NAMES.put("GET_TIMEOUT", "testGetTimeout");
            DATES = Integer.parseInt(props.getProperty("DATES")); TOTAL_CALL_NUMBER += DATES; METHOD_NAMES.put("DATES", "testDateRetrieval");
            MAPS = Integer.parseInt(props.getProperty("MAPS")); TOTAL_CALL_NUMBER += MAPS; METHOD_NAMES.put("MAPS", "testMapRetrieval");
            
            CHECK_LOG = Boolean.valueOf(props.getProperty("CHECK_LOG"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise tester", e);
        }
    }
    
    private static  String CHARSET = "utf-8";
    
    private static final Map<HttpCallLogEntry, CallLogCount> LOG = new TreeMap<HttpCallLogEntry, CallLogCount>();
    


    
    private static final ExecutorService executor = (ExecutorService)Executors.newFixedThreadPool(NUM_THREADS);
    private static ThreadLocal<HttpClient> client = new ThreadLocal<HttpClient>();

    private static RangeFinder<HttpCallable> requests = new RangeFinder<HttpCallable>();
    private static RangeFinder<String> protocols = new RangeFinder<String>();
    
    private static AtomicLong callsRemaining = new AtomicLong(0);
    private static AtomicLong callsMade = new AtomicLong(0);
    private static AtomicLong avgRandomiser = new AtomicLong(0);
    private static AtomicLong bytesReceived = new AtomicLong(0);
    private static AtomicLong totalLogRetries = new AtomicLong(0);
    

    private static void setup() {
        requests.addValue(SIMPLE_GET, new HttpCallable("SIMPLE_GET", REST_BASE+SIMPLE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK, new HttpBodyBuilder(SIMPLE_GET_SOAP)));
        requests.addValue(LARGE_GET, new HttpCallable( "LARGE_GET", REST_BASE+LARGE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK, new HttpBodyBuilder(LARGE_GET_SOAP)));
        requests.addValue(GET_TIMEOUT, new HttpCallable("TIMEOUT", REST_BASE+SIMPLE_TIMEOUT_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK, new HttpBodyBuilder(TIMEOUT_SOAP)));
        requests.addValue(EXCEPTION, new HttpCallable( "EXCEPTION", REST_BASE+EXCEPTION_PATH_UNAUTHORISED, SOAP_ENDPOINT, HttpStatus.SC_UNAUTHORIZED, new HttpBodyBuilder(EXC_GET_SOAP)));
        requests.addValue(STYLES, new HttpCallable(    "STYLES", REST_BASE+STYLES_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK, new HttpBodyBuilder(STYLES_SOAP)));

        requests.addValue(DATES, new HttpCallable("DATES", REST_BASE+"dates", SOAP_ENDPOINT, 
                new HttpBodyBuilder(DATES_BODY_JSON), 
                new HttpBodyBuilder(DATES_BODY_XML),
                new HttpBodyBuilder(DATES_SOAP)));

        requests.addValue(COMPLEX_MUTATOR, new HttpCallable("COMPLEX_MUTATOR", REST_BASE+"complex", SOAP_ENDPOINT, 
                new HttpBodyBuilder(COMPLEX_MUTATOR_BODY_JSON), 
                new HttpBodyBuilder(COMPLEX_MUTATOR_BODY_XML),
                new HttpBodyBuilder(COMPLEX_MUTATOR_SOAP)));

        requests.addValue(LARGE_POST, new HttpCallable("LARGE_POST", REST_BASE+"large", SOAP_ENDPOINT, 
                new HttpBodyBuilder(LARGE_POST_BODY_JSON_START, LARGE_POST_BODY_JSON_REPEAT, LARGE_POST_BODY_JSON_SEPARATOR, LARGE_POST_BODY_JSON_END), 
                new HttpBodyBuilder(LARGE_POST_BODY_XML_START, LARGE_POST_BODY_XML_REPEAT, "", LARGE_POST_BODY_XML_END),
                new HttpBodyBuilder(LARGE_POST_SOAP_START, LARGE_POST_SOAP_REPEAT, "", LARGE_POST_SOAP_END)));

        requests.addValue(MAPS, new HttpCallable("MAPS", REST_BASE+"map1", SOAP_ENDPOINT, 
                new HttpBodyBuilder(MAPS_BODY_JSON), 
                new HttpBodyBuilder(MAPS_BODY_XML),
                new HttpBodyBuilder(MAPS_SOAP)));

        protocols.addValue(SOAP_COUNT, SOAP);
        protocols.addValue(SOAP_COUNT, SOAP);
        protocols.addValue(REST_XML_COUNT, APPLICATION_XML);
        protocols.addValue(REST_XML_COUNT, TEXT_XML);
        protocols.addValue(REST_JSON_COUNT, APPLICATION_JSON);
        protocols.addValue(REST_JSON_COUNT, TEXT_JSON);
        
    }

    
    
    public static void main(String[] args) {
        setup();
        // Create an instance of HttpClient.
        Long time = System.currentTimeMillis();
        final Random rnd = new Random(1);
        for (int i = 0; i < NUM_CALLS; i++) {
            callsRemaining.incrementAndGet();
            executor.execute(new Runnable() {
                public void run() {
                    makeRequest( getRequest(rnd), getContentType(rnd), rnd);
                }
            });
        }
        
        long lastTime = callsRemaining.longValue();
        while (callsRemaining.longValue() > 0) {
            try { Thread.sleep(1000); } catch (Exception ignored) {}
            if (lastTime - 1000 > callsRemaining.longValue()){
                lastTime = callsRemaining.get();
                System.out.print(".");
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Done.");
        
        executor.shutdown();
        
        analyseCalls(time);
    }

    private static HttpCallable getRequest(Random rnd) {
        return requests.getValue(rnd.nextInt(requests.getMaxRange()));
    }
    
    private static String getContentType(Random rnd) {
        return protocols.getValue(rnd.nextInt(protocols.getMaxRange()));
    }
    
    private static void analyseCalls(long time) {
        Map<String, Map<HttpCallLogEntry, CallLogCount>> maps = new TreeMap<String, Map<HttpCallLogEntry,CallLogCount>>();
        maps.put("SOAP      ", new HashMap<HttpCallLogEntry, CallLogCount>());
        maps.put("REST_XML  ", new HashMap<HttpCallLogEntry, CallLogCount>());
        maps.put("REST_JSON ", new HashMap<HttpCallLogEntry, CallLogCount>());
        
        long totalCalls = 0;
        long totalCallTime = 0;
        for (Map.Entry<HttpCallLogEntry, CallLogCount> entry: LOG.entrySet()) {
            HttpCallLogEntry e = entry.getKey();
            CallLogCount c = entry.getValue();
            double callTimeMilli = c.aveCallTime() / 1000000d;
            System.out.format("%16s called %6d times with %2d failures in average time of %8.3f ms (%16s)\n", 
                    e.getMethod(), c.numCalls.get(), c.failures.get(), callTimeMilli, e.getProtocol());
            totalCalls += c.numCalls.get();
            totalCallTime += c.callTime.get();
            
            if (e.getProtocol().equals(SOAP))      maps.get("SOAP      ").put(e, c);
            if (e.getProtocol().endsWith("xml"))  maps.get("REST_XML  ").put(e, c);
            if (e.getProtocol().endsWith("json")) maps.get("REST_JSON ").put(e, c);
        }
        System.out.println();
        for (Map.Entry<String, Map<HttpCallLogEntry, CallLogCount>> me: maps.entrySet()) {
            simpleSummary(me.getKey(), me.getValue());
        }
        System.out.println();
        System.out.format("Average randomiser    : %d\n", avgRandomiser.longValue()/totalCalls);
        System.out.format("Average bytes/msg     : %d\n", bytesReceived.longValue()/totalCalls);
        System.out.format("HTTP Threads          : %d\n", NUM_THREADS);
        System.out.format("Total Log Retries     : %d\n", totalLogRetries.longValue());
        System.out.format("Total Calls made      : %d\n", totalCalls);
        System.out.format("Average time per call : %3.3f ms\n", totalCallTime / (totalCalls * 1000000d));
        System.out.format("Total Time taken      : %3.3f seconds\n", time / 1000.0d);
        System.out.format("TPS                   : %d\n", (int) (totalCalls / (time / 1000.0d)));

    }
    
    public static void simpleSummary(String ident, Map<HttpCallLogEntry, CallLogCount> calls) {
        if (calls.isEmpty()) return;
        
        long totalCalls = 0;
        long totalCallTime = 0;
        for (Map.Entry<HttpCallLogEntry, CallLogCount> entry: calls.entrySet()) {
            CallLogCount c = entry.getValue();
            totalCalls += c.numCalls.get();
            totalCallTime += c.callTime.get();
        }
        System.out.format("%20s: Total Calls: %6d, Ave time per call: %3.3f ms\n", ident, totalCalls,totalCallTime / (totalCalls * 1000000d));
        
    }
    
    private static void makeRequest(HttpCallable call, String contentType, Random rnd) {
        HttpClient httpc = client.get(); 
        if (httpc == null) {
            httpc = new DefaultHttpClient();
            client.set(httpc);
        }
        HttpCallLogEntry cle = new HttpCallLogEntry();
        int randomiser = rnd.nextInt(50);
        avgRandomiser.addAndGet(randomiser);
        HttpUriRequest method = call.getMethod(contentType, new Object[] { randomiser }, randomiser, cle);
        method.addHeader("Authority", "CoUGARUK");
        CallLogCount loggedCount = null;
        // Execute the method.
        synchronized (LOG) {
            loggedCount = LOG.get(cle);
            if (loggedCount == null) {
                loggedCount = new CallLogCount();
                LOG.put(cle, loggedCount);
            }
        }
        long nanoTime = System.nanoTime();

        InputStream is = null;

        try {
            
            if (TRACE_EVERY > 0 && callsMade.incrementAndGet() % TRACE_EVERY == 0) {
                method.addHeader("X-Trace-Me", "true");
            }
            loggedCount.numCalls.incrementAndGet();
            final HttpResponse httpResponse = httpc.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            boolean failed = false;
            
            int expectedHTTPCode = call.expectedResult();
            if (contentType.equals(SOAP) && expectedHTTPCode != HttpStatus.SC_OK) {
                // All SOAP errors are 500.
                expectedHTTPCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
            if (statusCode != expectedHTTPCode) {
                System.err.println("Method failed: " + httpResponse.getStatusLine().getReasonPhrase());
                failed = true;
            }
            // Read the response body.
            is = httpResponse.getEntity().getContent();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
            is.close();
            String result = new String(baos.toByteArray(), CHARSET);
            if (result.length() == 0) {
            	System.err.println("FAILURE: Empty buffer returned");
            	failed = true;
            }
            if (failed) {
                loggedCount.failures.incrementAndGet();
            }
            bytesReceived.addAndGet(baos.toByteArray().length);
            
            if (CHECK_LOG && NUM_THREADS == 1) {
            	File logFile = new File("C:\\perforce\\se\\development\\HEAD\\cougar\\cougar-framework\\baseline\\baseline-launch\\logs\\request-Baseline.log");
            	String lastLine = getLastLine(logFile);
            	int tries = 0;
            	while (!lastLine.contains(METHOD_NAMES.get(call.getName()))) {
            		if (++tries > 5) {
            			System.err.println("LOG FAIL: Call: "+METHOD_NAMES.get(call.getName())+", Line: "+lastLine);	
            		} else {
            			try { Thread.sleep(1); } catch (InterruptedException e) {}
            		}
            		lastLine = getLastLine(logFile);
            	}
            	totalLogRetries.addAndGet(tries);
            }

        } catch (Exception e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
            loggedCount.failures.incrementAndGet();
        } finally {
            // Release the connection.
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { /* ignore */}
            }
            callsRemaining.decrementAndGet();

            nanoTime = System.nanoTime() - nanoTime;
            loggedCount.callTime.addAndGet(nanoTime);
        }
    }
    
    private static String getLastLine(File file) throws IOException {

        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file));
        
        BufferedReader br = new BufferedReader(streamReader);

        String line = null;
        while (br.ready()) {
           line = br.readLine();
        }
        return line;
    }    
        
    private static class CallLogCount {
        private AtomicLong numCalls = new AtomicLong(0);
        private AtomicLong failures = new AtomicLong(0);
        private AtomicLong callTime = new AtomicLong(0);
        
        private long aveCallTime() {
            if (numCalls.longValue() == 0) return 0;
            return callTime.longValue() / (numCalls.longValue());
        }
    }
    
    private static class RangeFinder<T> {
        TreeMap<Integer, T> map = new TreeMap<Integer, T>();
        int rangeVal=0;
        
        public void addValue(int range, T t) {
            if (range == 0) return;
            rangeVal += range;
            map.put(rangeVal, t);
        }
        
        public int getMaxRange() {
            return rangeVal;
        }
        
        public T getValue(int point) {
            if (point < 0 || point >= rangeVal) {
                throw new IllegalArgumentException();
            }
            for (Map.Entry<Integer, T> entry: map.entrySet()) {
                if (point < entry.getKey()) {
                    return entry.getValue();
                }
            }
            throw new IllegalStateException("Tits");
        }
    }
}
