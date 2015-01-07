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

package com.betfair.cougar.core.impl.ev;

import java.util.Date;

import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.impl.logging.RequestLogEvent;
import com.betfair.cougar.util.RequestUUIDImpl;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ServiceExecutableResolverTest {

	private final OperationKey op1Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Operation1");

	private ExecutableResolver simpleResolver;
	private EventLogger eventLogger;
	private ExecutionContext context;
	private ServiceExecutableResolver serviceResolver;
	private Object[] fieldsToLog = new Object[] {"logme"};
	private String logName = "logName";
	private RequestUUID requestuuid = new RequestUUIDImpl();
    private ServiceRegisterableExecutionVenue ev;

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }


	@Before
	public void init() {

		simpleResolver = mock(ExecutableResolver.class);

		ServiceLogManager manager = mock(ServiceLogManager.class);
		when(manager.getLogExtensionClass()).thenAnswer(new Answer<Class<? extends LogExtension>>() {
			@Override
			public Class<? extends LogExtension> answer(
					InvocationOnMock invocation) throws Throwable {
				return TestLogExtension.class;
			}});
		when(manager.getNumLogExtensionFields()).thenReturn(fieldsToLog.length);
		when(manager.getLoggerName()).thenReturn(logName);

        ev = mock(ServiceRegisterableExecutionVenue.class);
        when(ev.getServiceLogManager(anyString(), anyString(), any(ServiceVersion.class))).thenReturn(manager);

		eventLogger = mock(EventLogger.class);

		serviceResolver = new ServiceExecutableResolver();
		serviceResolver.registerExecutableResolver(simpleResolver);
		serviceResolver.setEventLogger(eventLogger);

		context = mock(ExecutionContext.class);
		when(context.getRequestUUID()).thenReturn(requestuuid);
	}

	@Test
	public void testResolveExecutable() {
		Executable executable = mock(Executable.class);
		when(simpleResolver.resolveExecutable(op1Key, ev)).thenReturn(executable);

		//Resolve the executable
		Executable resultExecutable = serviceResolver.resolveExecutable(op1Key, ev);

		//Execute the returned executable
		resultExecutable.execute(context, op1Key, new Object[] {}, mock(ExecutionObserver.class), mock(ExecutionVenue.class), DefaultTimeConstraints.NO_CONSTRAINTS);

		//Verify that the registered executable was invoked with a RequestContext
		verify(executable).execute(any(RequestContext.class), eq(op1Key), any(Object[].class), any(ExecutionObserver.class), any(ExecutionVenue.class), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
	}

	@Test
	public void testLoggingResult() {
		ObserverFunction function = new ObserverFunction() {
			@Override
			public void perform(ExecutionObserver observer) {
				observer.onResult(new ExecutionResult("hello"));
			}
		};
		testLogging(function, new RequestLogEventMatcher(""));
	}

	@Test
	public void testLoggingException() {
		ObserverFunction function = new ObserverFunction() {
			@Override
			public void perform(ExecutionObserver observer) {
				observer.onResult(new ExecutionResult(new CougarFrameworkException("test error")));
			}
		};
		testLogging(function, new RequestLogEventMatcher("DSC-0002"));
	}

	private void testLogging(ObserverFunction function, ArgumentMatcher<RequestLogEvent> matchesRequestLogEvent) {
		TestExecutable executable = new TestExecutable();
		when(simpleResolver.resolveExecutable(op1Key, ev)).thenReturn(executable);

		Executable resultExecutable = serviceResolver.resolveExecutable(op1Key, ev);
		resultExecutable.execute(context, op1Key, new Object[] {}, mock(ExecutionObserver.class), mock(ExecutionVenue.class),DefaultTimeConstraints.NO_CONSTRAINTS);

		LoggableEvent event = mock(LoggableEvent.class);

		RequestContext context = executable.getContext();
		context.addEventLogRecord(event);
		context.setRequestLogExtension(new TestLogExtension());
		ExecutionObserver observer = executable.getObserver();
		function.perform(observer);

		verify(eventLogger).logEvent(argThat(matchesRequestLogEvent), eq(fieldsToLog));
		verify(eventLogger).logEvent(event, null);

		function.perform(observer);
		verifyNoMoreInteractions(eventLogger);
	}

	private interface ObserverFunction {
		public void perform(ExecutionObserver observer);
	}

	private class RequestLogEventMatcher extends ArgumentMatcher<RequestLogEvent> {

		private String errorCode;

		public RequestLogEventMatcher(String errorCode) {
			super();
			this.errorCode = errorCode;
		}

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof RequestLogEvent) {
                RequestLogEvent event = (RequestLogEvent)argument;
                assertEquals(logName, event.getLogName());
                Object[] eventFields = event.getFieldsToLog();
                assertNotNull(eventFields);
                assertEquals(6, eventFields.length);
                //Field 0 is request start time
                assertTrue(eventFields[0] instanceof Date);
                //Field 1 is Request UUID
                assertEquals(requestuuid.toCougarLogString(), eventFields[1]);
                //Field 2 is version without the "v" character
                assertEquals("1.0", eventFields[2]);
                //Field 3 is the operation name
                assertEquals("Operation1", eventFields[3]);
                //Field 4 is the error code
                assertEquals(errorCode, eventFields[4]);
                //Field 5 is the length of time nanos for the operation
                assertTrue(eventFields[5] instanceof Long);
                //If we haven't failed an assert yet then this must match
                return true;
            }
            return false;
		}
	}

	private class TestExecutable implements Executable {

		private ExecutionObserver observer;
		private RequestContext context;

		@Override
		public void execute(ExecutionContext ctx, OperationKey key,
				Object[] args, ExecutionObserver observer,
				ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
			this.observer = observer;
			this.context = (RequestContext)ctx;
		}

		public ExecutionObserver getObserver() {
			return observer;
		}

		public RequestContext getContext() {
			return context;
		}
	}

	public class TestLogExtension implements LogExtension {

		@Override
		public Object[] getFieldsToLog() {
			return fieldsToLog;
		}

	}
}
