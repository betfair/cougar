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

package com.betfair.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectBodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectQueryParameterEnum;
import com.betfair.baseline.v2.to.BodyParamEnumObject;
import com.betfair.baseline.v2.to.EnumOperationResponseObject;
import com.betfair.baseline.v2.to.SimpleResponse;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutionTimingRecorder;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionPostProcessor;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.SimpleOperationDefinition;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.util.RequestUUIDImpl;

public class TestClient {

	private Executable e;

	public TestClient(Executable venue) {
		e = venue;
	}
	
	public void start() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		testAdditionalParameter();
		testAdditionalField();
		testOptionalFieldNotPresent();
		testMandatoryFieldNotPresent();		
		testServerAddedValues();
		testServerRemovedValues();
		testServerAddedResponseValue();		
	}
	
	/**
	 * All mandatory values are set, we're just supplying an additional parameter, simulating what would happen if a param was removed from the servers interface
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	private void testAdditionalParameter() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator coc = new ComplexObjectCreator("com.betfair.baseline.v2.to.ComplexObject");
		Pair<String,String>[] fieldDefs = new Pair[] {
				new Pair<String,String>(String.class.getName(), "name"),
				new Pair<String,String>(Integer.class.getName(), "value1"),
				new Pair<String,String>(Integer.class.getName(), "value2"),
				new Pair<String,String>(Boolean.class.getName(), "ok"),
		};
		coc.create(bos, fieldDefs);
		ByteArrayClassLoader complexObjectCL = new ByteArrayClassLoader(coc.objectType,bos.toByteArray());
		Class<?> complexObjectClass = Class.forName(coc.objectType,true,complexObjectCL);
		Object o = complexObjectClass.newInstance();
		
		Field f = complexObjectClass.getField("value1");
		f.set(o, 1);
		f = complexObjectClass.getField("name");
		f.set(o, "result");
		f = complexObjectClass.getField("value2");
		f.set(o, 2);
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
			new Pair<String, Class<?>>("message",complexObjectClass),
			new Pair<String, Class<?>>("message2",complexObjectClass)
		};
		
		
		ExecutionVenue ev = createExecutionVenue(testComplexMutatorKey,parameters, SimpleResponse.class);
		System.out.print("Starting additional parameter test : ");
		final CountDownLatch cl = new CountDownLatch(1);
		e.execute(ec, testComplexMutatorKey, new Object[] {o,o}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (!executionResult.isFault()) {
					SimpleResponse response = (SimpleResponse) executionResult.getResult();
					System.out.println("result = 3".equals(response.getMessage()) ? "PASS" : "FAIL");
				}
				else {
					System.out.println("FAIL");
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
	
		cl.await();
	}
	
	/**
	 * The object parameter has a field (value3) not present in the server interface
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	private void testAdditionalField() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator coc = new ComplexObjectCreator("com.betfair.baseline.v2.to.ComplexObject");
		Pair<String,String>[] fieldDefs = new Pair[] {
				new Pair<String,String>(String.class.getName(), "name"),
				new Pair<String,String>(Integer.class.getName(), "value1"),
				new Pair<String,String>(Integer.class.getName(), "value2"),
				new Pair<String,String>(Integer.class.getName(), "value3"),
				new Pair<String,String>(Boolean.class.getName(), "ok"),
		};
		coc.create(bos, fieldDefs);
		ByteArrayClassLoader complexObjectCL = new ByteArrayClassLoader(coc.objectType,bos.toByteArray());
		Class<?> complexObjectClass = Class.forName(coc.objectType,true,complexObjectCL);
		Object o = complexObjectClass.newInstance();
		
		Field f = complexObjectClass.getField("value1");
		f.set(o, 1);
		f = complexObjectClass.getField("name");
		f.set(o, "result");
		f = complexObjectClass.getField("value2");
		f.set(o, 2);
		f = complexObjectClass.getField("value3");
		f.set(o, 3);
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("message",complexObjectClass)
		};
		
		ExecutionVenue ev = createExecutionVenue(testComplexMutatorKey,parameters, SimpleResponse.class);
		
		System.out.print("Starting testAdditionalField : ");

		final CountDownLatch cl = new CountDownLatch(1);
		e.execute(ec, testComplexMutatorKey, new Object[] {o,o}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (!executionResult.isFault()) {
					SimpleResponse response = (SimpleResponse) executionResult.getResult();
					System.out.println("result = 3".equals(response.getMessage()) ? "PASS" : "FAIL");
				}
				else {
					System.out.println("FAIL");
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		cl.await();
	}
	
	/**
	 * The object parameter has an optional field (name) not present
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	private void testOptionalFieldNotPresent() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator coc = new ComplexObjectCreator("com.betfair.baseline.v2.to.ComplexObject");
		Pair<String,String>[] fieldDefs = new Pair[] {
				new Pair<String,String>(Integer.class.getName(), "value1"),
				new Pair<String,String>(Integer.class.getName(), "value2"),
				new Pair<String,String>(Boolean.class.getName(), "ok"),
		};
		coc.create(bos, fieldDefs);
		ByteArrayClassLoader complexObjectCL = new ByteArrayClassLoader(coc.objectType,bos.toByteArray());
		Class<?> complexObjectClass = Class.forName(coc.objectType,true,complexObjectCL);
		Object o = complexObjectClass.newInstance();
		
		Field f = complexObjectClass.getField("value1");
		f.set(o, 1);
		f = complexObjectClass.getField("value2");
		f.set(o, 2);
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("message",complexObjectClass)
		};
		
		ExecutionVenue ev = createExecutionVenue(testComplexMutatorKey,parameters, SimpleResponse.class);
		
		System.out.print("Starting testOptionalFieldNotPresent : ");

		final CountDownLatch cl = new CountDownLatch(1);
		e.execute(ec, testComplexMutatorKey, new Object[] {o,o}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (!executionResult.isFault()) {
					SimpleResponse response = (SimpleResponse) executionResult.getResult();
					System.out.println("null = 3".equals(response.getMessage()) ? "PASS" : "FAIL");
				}
				else {
					System.out.println("FAIL");
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		if (!cl.await(2,TimeUnit.SECONDS)) {
			System.out.println("FAIL");
		}
	}
	
	/**
	 * The object parameter has an mandatory field (value1) not present
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	private void testMandatoryFieldNotPresent() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ComplexObjectCreator coc = new ComplexObjectCreator("com.betfair.baseline.v2.to.ComplexObject");
		Pair<String,String>[] fieldDefs = new Pair[] {
				new Pair<String,String>(String.class.getName(), "name"),
				new Pair<String,String>(Integer.class.getName(), "value2"),
				new Pair<String,String>(Boolean.class.getName(), "ok"),
		};
		coc.create(bos, fieldDefs);
		ByteArrayClassLoader complexObjectCL = new ByteArrayClassLoader(coc.objectType,bos.toByteArray());
		Class<?> complexObjectClass = Class.forName(coc.objectType,true,complexObjectCL);
		Object o = complexObjectClass.newInstance();
		
		Field f = complexObjectClass.getField("name");
		f.set(o, "result");
		f = complexObjectClass.getField("value2");
		f.set(o, 2);
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("message",complexObjectClass)
		};
		
		ExecutionVenue ev = createExecutionVenue(testComplexMutatorKey,parameters, SimpleResponse.class);
		
		System.out.print("Starting testMandatoryFieldNotPresent : ");

		final CountDownLatch cl = new CountDownLatch(1);
		e.execute(ec, testComplexMutatorKey, new Object[] {o,o}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (!executionResult.isFault()) {
					System.out.println("FAIL");
				}
				else {
					CougarException ce = executionResult.getFault();
					if (ResponseCode.BadRequest.equals(ce.getResponseCode())) {
						System.out.println("PASS");
					}
					else {
						System.out.println("FAIL");
					}
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		if (!cl.await(2,TimeUnit.SECONDS)) {
			System.out.println("FAIL");
		}
	}

	/**
	 * Test when an  enumeration has less values than server.
	 * The enums are also out of order (compared to server) and the response object has less fields than the servers
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private void testServerAddedValues() throws IOException, ClassNotFoundException, InstantiationException, 
													IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String headerEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum";
		EnumCreator hpc = new EnumCreator(headerEnumType,new String[]{"FooHeader","BarHeader"});
		hpc.create(bos);
		ByteArrayClassLoader enumCL = new ByteArrayClassLoader(headerEnumType,bos.toByteArray());
		Class<Enum> headerParamEnum = (Class<Enum>) Class.forName(headerEnumType,true,enumCL);
		Object header = Enum.valueOf(headerParamEnum, "FooHeader");

		bos.reset();
		String queryEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum";
		EnumCreator qpc = new EnumCreator(queryEnumType,new String[] {"FooBarQuery","BarQuery"});
		qpc.create(bos);
		enumCL = new ByteArrayClassLoader(queryEnumType, bos.toByteArray());
		Class<Enum> queryParamEnum = (Class<Enum>) Class.forName(queryEnumType, true, enumCL);
		Object query = Enum.valueOf(queryParamEnum, "BarQuery");
		
		bos.reset();
		String bodyEnumType = "com.betfair.baseline.v2.enumerations.BodyParamEnumObjectBodyParameterEnum";
		EnumCreator bqc = new EnumCreator(bodyEnumType,new String[] {"FooBarBody"});
		bqc.create(bos);
		enumCL = new ByteArrayClassLoader(bodyEnumType,bos.toByteArray());
		Class<Enum> bodyParamEnum = (Class<Enum>) Class.forName(bodyEnumType, true, enumCL);
		Object body = Enum.valueOf(bodyParamEnum, "FooBarBody");
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("headerParam",headerParamEnum),
				new Pair<String, Class<?>>("queryParam",queryParamEnum),
				new Pair<String, Class<?>>("message",BodyParamEnumObject.class)
		};		
		
		ExecutionVenue ev = createExecutionVenue(enumOperationKey,parameters, EnumOperationResponseObject.class);

		BodyParamEnumObject bodyObject = new BodyParamEnumObject(body);
		final CountDownLatch cl = new CountDownLatch(1);
		System.out.print("Starting testServerAddedValues : ");
		e.execute(ec, enumOperationKey, new Object[] {header,query,bodyObject}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (executionResult.isFault()) {
					System.out.println("FAIL");
				}
				else {
					EnumOperationResponseObject response = (EnumOperationResponseObject) executionResult.getResult();
					if (response.getBodyParameter() == EnumOperationResponseObjectBodyParameterEnum.FooBarBody &&
						response.getQueryParameter() == EnumOperationResponseObjectQueryParameterEnum.BarQuery) {
						System.out.println("PASS");
					}
					else {
						System.out.println("FAIL");
					}
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		if (!cl.await(2,TimeUnit.SECONDS)) {
			System.out.println("FAIL");
		}
	}
	
	/**
	 * Test when an  enumeration has more values than server to simulate the server removing values from its response
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private void testServerRemovedValues() throws IOException, ClassNotFoundException, InstantiationException, 
													IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String headerEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum";
		EnumCreator hpc = new EnumCreator(headerEnumType,new String[]{"FooHeader","BarHeader","FooBarHeader","BazHeader"});
		hpc.create(bos);
		ByteArrayClassLoader enumCL = new ByteArrayClassLoader(headerEnumType,bos.toByteArray());
		Class<Enum> headerParamEnum = (Class<Enum>) Class.forName(headerEnumType,true,enumCL);
		Object header = Enum.valueOf(headerParamEnum, "BazHeader");

		bos.reset();
		String queryEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum";
		EnumCreator qpc = new EnumCreator(queryEnumType,new String[] {"FooBarQuery","BarQuery"});
		qpc.create(bos);
		enumCL = new ByteArrayClassLoader(queryEnumType, bos.toByteArray());
		Class<Enum> queryParamEnum = (Class<Enum>) Class.forName(queryEnumType, true, enumCL);
		Object query = Enum.valueOf(queryParamEnum, "BarQuery");
		
		bos.reset();
		String bodyEnumType = "com.betfair.baseline.v2.enumerations.BodyParamEnumObjectBodyParameterEnum";
		EnumCreator bqc = new EnumCreator(bodyEnumType,new String[] {"FooBarBody"});
		bqc.create(bos);
		enumCL = new ByteArrayClassLoader(bodyEnumType,bos.toByteArray());
		Class<Enum> bodyParamEnum = (Class<Enum>) Class.forName(bodyEnumType, true, enumCL);
		Object body = Enum.valueOf(bodyParamEnum, "FooBarBody");
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("headerParam",headerParamEnum),
				new Pair<String, Class<?>>("queryParam",queryParamEnum),
				new Pair<String, Class<?>>("message",BodyParamEnumObject.class)
		};		
		
		ExecutionVenue ev = createExecutionVenue(enumOperationKey,parameters,EnumOperationResponseObject.class);

		BodyParamEnumObject bodyObject = new BodyParamEnumObject(body);
		final CountDownLatch cl = new CountDownLatch(1);
		System.out.print("Starting testServerRemovedValues : ");
		e.execute(ec, enumOperationKey, new Object[] {header,query,bodyObject}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (executionResult.isFault()) {
					System.out.println("PASS");
				}
				else {
					System.out.println("FAIL");
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		if (!cl.await(2,TimeUnit.SECONDS)) {
			System.out.println("FAIL");
		}
	}
	
	/**
	 * Test when the response contains an enum not in our valid values enum (in this case, FooBarQuery)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private void testServerAddedResponseValue() throws IOException, ClassNotFoundException, InstantiationException, 
													IllegalAccessException, SecurityException, NoSuchFieldException, InterruptedException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String headerEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum";
		EnumCreator hpc = new EnumCreator(headerEnumType,new String[]{"FooHeader","BarHeader","FooBarHeader"});
		hpc.create(bos);
		ByteArrayClassLoader enumCL = new ByteArrayClassLoader(headerEnumType,bos.toByteArray());
		Class<Enum> headerParamEnum = (Class<Enum>) Class.forName(headerEnumType,true,enumCL);
		Object header = Enum.valueOf(headerParamEnum, "FooHeader");

		bos.reset();
		String queryEnumType = "com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum";
		EnumCreator qpc = new EnumCreator(queryEnumType,new String[] {"FooBarQuery","BarQuery"});
		qpc.create(bos);
		enumCL = new ByteArrayClassLoader(queryEnumType, bos.toByteArray());
		Class<Enum> queryParamEnum = (Class<Enum>) Class.forName(queryEnumType, true, enumCL);
		Object query = Enum.valueOf(queryParamEnum, "FooBarQuery");
		
		bos.reset();
		String bodyEnumType = "com.betfair.baseline.v2.enumerations.BodyParamEnumObjectBodyParameterEnum";
		EnumCreator bqc = new EnumCreator(bodyEnumType,new String[] {"FooBarBody"});
		bqc.create(bos);
		enumCL = new ByteArrayClassLoader(bodyEnumType,bos.toByteArray());
		Class<Enum> bodyParamEnum = (Class<Enum>) Class.forName(bodyEnumType, true, enumCL);
		Object body = Enum.valueOf(bodyParamEnum, "FooBarBody");
		
		Pair<String,Class<?>>[] parameters = new Pair[] {
				new Pair<String, Class<?>>("headerParam",headerParamEnum),
				new Pair<String, Class<?>>("queryParam",queryParamEnum),
				new Pair<String, Class<?>>("message",BodyParamEnumObject.class)
		};		
		
		ExecutionVenue ev = createExecutionVenue(enumOperationKey,parameters, EnumOperationResponseObject.class);

		BodyParamEnumObject bodyObject = new BodyParamEnumObject(body);
		final CountDownLatch cl = new CountDownLatch(1);
		System.out.print("Starting testServerRemovedValues : ");
		e.execute(ec, enumOperationKey, new Object[] {header,query,bodyObject}, new ExecutionObserver() {

			@Override
			public void onResult(ExecutionResult executionResult) {
				if (executionResult.isFault()) {
					System.out.println("PASS");
				}
				else {
					System.out.println("FAIL");
				}
				cl.countDown();
				
			}}, ev, DefaultTimeConstraints.NO_CONSTRAINTS);
		
		if (!cl.await(2,TimeUnit.SECONDS)) {
			System.out.println("FAIL");
		}
	}
	
	
	
	
	private ExecutionVenue createExecutionVenue(OperationKey operationKey, Pair<String,Class<?>>[] parameters, Class response) {
		
		Parameter[] params = new Parameter[parameters.length];
		for (int i=0; i<parameters.length; i++) {
			params[i] = new Parameter(parameters[i].first, new ParameterType(parameters[i].second, null),true);
		}
		
		final OperationDefinition operationDef = new SimpleOperationDefinition( operationKey, params,new ParameterType(response, null ) );
		
		
		return new ExecutionVenue() {
			public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, TimeConstraints timeConstraints) {}
			public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, Executor executor, TimeConstraints timeConstraints) {}
			public OperationDefinition getOperationDefinition(OperationKey key) {
				return operationDef;
			}
			public Set<OperationKey> getOperationKeys() {return null;}
            public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder, long maxExecutionTime) {}
			public void setPostProcessors(List<ExecutionPostProcessor> preProcessorList) {}
			public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList) {}
			
		};
	}
	
	private  static final ServiceVersion serviceVersion = new ServiceVersion("v2.0");
	private static final String serviceName = "Baseline";
	
	private static final OperationKey testComplexMutatorKey = new OperationKey(serviceVersion, serviceName, "testComplexMutator", OperationKey.Type.Request);	
	public static final OperationKey enumOperationKey = new OperationKey(serviceVersion, serviceName, "enumOperation", OperationKey.Type.Request);
	
	
	private ExecutionContext ec = new ExecutionContext() {
		public IdentityChain getIdentity() {return null;}
		public GeoLocationDetails getLocation() {
			return new GeoLocationDetails(){
				public String getCountry() {return "GBR";}
				public String getLocation() {return "127.0.0.1";}
				public String getRemoteAddr() {return "127.0.0.1";}
				public List<String> getResolvedAddresses() {return Collections.singletonList("127.0.0.1");}
                public String getInferredCountry() { return "GBR";}
				public boolean isLowConfidenceGeoLocation() {return false;}};
		}
		public Date getReceivedTime() {return new Date();}
        public Date getRequestTime() {return new Date();}
        public RequestUUID getRequestUUID() {return new RequestUUIDImpl();}
		public boolean traceLoggingEnabled() {return false;}

        @Override
        public int getTransportSecurityStrengthFactor() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isTransportSecure() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

	
}
