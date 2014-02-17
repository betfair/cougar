package com.betfair.cougar.core.impl.exception;

import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.ev.ServiceExceptionHandlingObserver;
import com.betfair.cougar.test.MockException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class ServiceExceptionHandlingObserverTest {

    private ExecutionObserver mockObserver;
    private ArgumentCaptor<ExecutionResult> captor;
    private ServiceExceptionHandlingObserver observer;

    @Before
    public void before() {
        mockObserver = mock(ExecutionObserver.class);
        captor = ArgumentCaptor.forClass(ExecutionResult.class);
        observer = new ServiceExceptionHandlingObserver(mockObserver);
    }

    private ExecutionResult execute(ExecutionResult arg) {
        observer.onResult(arg);
        verify(mockObserver).onResult(captor.capture());
        return captor.getValue();
    }

    @Test
    public void successResult() {
        String text = "message";
        ExecutionResult er = execute(new ExecutionResult(text));

        assertEquals(ExecutionResult.ResultType.Success, er.getResultType());
        assertEquals(text, er.getResult());
    }

    @Test
    public void cougarException() {
        CougarException ce = new CougarFrameworkException("Wibble");
        ExecutionResult er = execute(new ExecutionResult(ce));

        assertEquals(ExecutionResult.ResultType.Fault, er.getResultType());
        assertEquals(ce, er.getFault());
    }

    @Test
    public void cougarClientException() {
        CougarException ce = new ClientException(ServerFaultCode.ServiceCheckedException,"message",new MockException());
        ExecutionResult er = execute(new ExecutionResult(ce));

        assertEquals(ExecutionResult.ResultType.Fault, er.getResultType());
        assertTrue(er.getFault() instanceof CougarServiceException);
        assertEquals(ServerFaultCode.ServiceRuntimeException, er.getFault().getServerFaultCode());
        assertEquals(ce, er.getFault().getCause());
    }

    @Test
    public void serviceCheckedException() {
        CougarApplicationException cae = new MockException();
        ExecutionResult er = execute(new ExecutionResult(cae));

        assertEquals(ExecutionResult.ResultType.Fault, er.getResultType());
        assertEquals(cae, er.getFault().getCause());
    }

    private class ClientException extends CougarClientException {
        private ClientException(ServerFaultCode fault, String message, CougarApplicationException dae) {
            super(fault, message, dae);
        }
    }
}
