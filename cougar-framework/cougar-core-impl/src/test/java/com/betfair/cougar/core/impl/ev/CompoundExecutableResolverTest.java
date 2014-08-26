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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.betfair.cougar.core.api.ev.TimeConstraints;
import org.junit.Test;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutableResolver;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationKey;

public class CompoundExecutableResolverTest {

	private OperationKey op1Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Operation1");
	private OperationKey op2Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Operation2");
	private OperationKey op3Key = new OperationKey(new ServiceVersion("v1.0"), "Service1", "Not Registered");
    private ExecutionVenue ev = mock(ExecutionVenue.class);
	private Executable ex1 = new Executable() {

		@Override
		public void execute(ExecutionContext ctx, OperationKey key,
				Object[] args, ExecutionObserver observer,
				ExecutionVenue executionVenue, TimeConstraints timeConstraints) {

		}};
	private Executable ex2 = new Executable() {

		@Override
		public void execute(ExecutionContext ctx, OperationKey key,
				Object[] args, ExecutionObserver observer,
				ExecutionVenue executionVenue, TimeConstraints timeConstraints) {

		}};

	@Test
	public void testRegisterAndResolve() {
		ExecutableResolver simpleResolver1 = mock(ExecutableResolver.class);
		ExecutableResolver simpleResolver2 = mock(ExecutableResolver.class);
		CompoundExecutableResolverImpl compoundResolver = new CompoundExecutableResolverImpl();

		//Set up the resolver behaviour
		when(simpleResolver1.resolveExecutable(op1Key, ev)).thenReturn(ex1);
		when(simpleResolver2.resolveExecutable(op2Key, ev)).thenReturn(ex2);

		//register the resolvers
		compoundResolver.registerExecutableResolver(simpleResolver1);
		compoundResolver.registerExecutableResolver(simpleResolver2);

		//assert that we get the expected response from resolver 1
		Executable actualEx = compoundResolver.resolveExecutable(op1Key, ev);
		assertNotNull(actualEx);
		assertEquals(ex1, actualEx);

		//assert that we get the expected response from resolver 2
		actualEx = compoundResolver.resolveExecutable(op2Key, ev);
		assertNotNull(actualEx);
		assertEquals(ex2, actualEx);

		//assert that we get a null response for an unregistered key
		actualEx = compoundResolver.resolveExecutable(op3Key, ev);
		assertNull(actualEx);
	}
}
