/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.baseline;

import com.betfair.baseline.v2.BaselineClient;
import com.betfair.baseline.v2.BaselineService;
import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.co.SimpleConnectedObjectCO;
import com.betfair.baseline.v2.co.VeryComplexObjectCO;
import com.betfair.baseline.v2.co.server.SimpleConnectedObjectServerCO;
import com.betfair.baseline.v2.co.server.VeryComplexObjectServerCO;
import com.betfair.baseline.v2.enumerations.AsyncBehaviour;
import com.betfair.baseline.v2.enumerations.ClientServerEnum;
import com.betfair.baseline.v2.enumerations.CougarComponentStatuses;
import com.betfair.baseline.v2.enumerations.EnumHandling3BodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumHandling3WrappedValueEnum;
import com.betfair.baseline.v2.enumerations.EnumHandlingParam2Enum;
import com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectBodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectHeaderParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectQueryParameterEnum;
import com.betfair.baseline.v2.enumerations.GetReceivedEventsEventEnum;
import com.betfair.baseline.v2.enumerations.LargeRequestOddOrEvenEnum;
import com.betfair.baseline.v2.enumerations.PreOrPostInterceptorException;
import com.betfair.baseline.v2.enumerations.ReceivedEventEventNameEnum;
import com.betfair.baseline.v2.enumerations.SimpleEnum;
import com.betfair.baseline.v2.enumerations.SimpleExceptionErrorCodeEnum;
import com.betfair.baseline.v2.enumerations.SimpleValidValue;
import com.betfair.baseline.v2.enumerations.TestConnectedObjectsProtocolEnum;
import com.betfair.baseline.v2.enumerations.TestParameterStylesHeaderParamEnum;
import com.betfair.baseline.v2.enumerations.TestParameterStylesQAHeaderParamEnum;
import com.betfair.baseline.v2.enumerations.WotsitExceptionErrorCodeEnum;
import com.betfair.baseline.v2.enumerations.WotsitExceptionTypeEnum;
import com.betfair.baseline.v2.events.ListEvent;
import com.betfair.baseline.v2.events.LogMessage;
import com.betfair.baseline.v2.events.LongEvent;
import com.betfair.baseline.v2.events.MapEvent;
import com.betfair.baseline.v2.events.MatchedBet;
import com.betfair.baseline.v2.events.SetEvent;
import com.betfair.baseline.v2.events.TimeTick;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.exception.WotsitException;
import com.betfair.baseline.v2.to.*;
import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.core.api.GateListener;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.events.EventTransportIdentity;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.builder.SetBuilder;
import com.betfair.cougar.core.impl.ev.ConnectedResponseImpl;
import com.betfair.cougar.core.impl.ev.DefaultSubscription;
import com.betfair.cougar.core.impl.logging.AbstractLoggingControl;
import com.betfair.cougar.core.impl.security.SSLAwareTokenResolver;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import com.betfair.nonservice.v3.NonSyncClient;
import com.betfair.tornjak.kpi.aop.KPITimedEvent;
import com.betfair.platform.virtualheap.HListComplex;
import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.MutableHeap;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.OnDemandMonitor;
import com.betfair.tornjak.monitor.Status;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class BaselineServiceImpl implements BaselineService, GateListener {
    public static final String SONIC_TRANSPORT_INSTANCE_ONE="SonicEventTransportImpl:firstSonicInstance";

    private ExecutionObserver timeTickPublishingObserver;
    private ExecutionObserver matchedBetObserver;
    private ExecutionObserver logMessageObserver;
    private ExecutionObserver listMessageObserver;
    private ExecutionObserver setMessageObserver;
    private ExecutionObserver mapMessageObserver;

    private Map<String, ExecutionObserver> longEventNamespacedExecutionObserver = new ConcurrentHashMap<String, ExecutionObserver>();

    private BaselineSyncClient baselineAsClient;
    private BaselineClient baselineAsyncClient;
    private BaselineSyncClient inProcessSyncClient;
    private BaselineSyncClient socketSyncClient;
    private NonSyncClient nonExistentServiceClient;

	private MonitorRegistry monitorRegistry;

    private AbstractLoggingControl loggingControl;

	final static Logger LOGGER = LoggerFactory.getLogger(BaselineServiceImpl.class);

    private List<TimeTick> timeTicks = new ArrayList<TimeTick>();
    private List<MatchedBet> matchedBets = new ArrayList<MatchedBet>();
    private List<LogMessage> logMessages = new ArrayList<LogMessage>();
    private List<ListEvent> listEvents = new ArrayList<ListEvent>();
    private List<SetEvent> setEvents = new ArrayList<SetEvent>();
    private List<MapEvent> mapEvents = new ArrayList<MapEvent>();
    private Set<String> subscriptionsBeingCreated = new HashSet<String>();
    private Map<String, Subscription> pubsubSubscriptions = new ConcurrentHashMap<String, Subscription>();
    private Map<String, List<Subscription>> heapSubscriptions = new ConcurrentHashMap<String, List<Subscription>>();

    private Heap simpleConnectedObjectHeap = new MutableHeap("simpleConnectedObject");
    private Heap simpleConnectedListHeap = new MutableHeap("simpleConnectedList");
    private Heap complexConnectedObjectHeap = new MutableHeap("complexConnectedObject");

    private List<CountDownLatch> outstandingSleeps = new CopyOnWriteArrayList<CountDownLatch>();
    private String instance;

    public void setLoggingControl(AbstractLoggingControl loggingControl) {
        this.loggingControl = loggingControl;
    }

    public void setBaselineAsClient(BaselineSyncClient baselineAsClient) {
        this.baselineAsClient = baselineAsClient;
    }

    public void setBaselineAsyncClient(BaselineClient baselineAsyncClient) {
        this.baselineAsyncClient = baselineAsyncClient;
    }

    public void setInProcessSyncClient(BaselineSyncClient inProcessSyncClient) {
        this.inProcessSyncClient = inProcessSyncClient;
    }

    public void setNonExistentServiceClient(NonSyncClient nonExistentServiceClient) {
        this.nonExistentServiceClient = nonExistentServiceClient;
    }

    public void setSocketSyncClient(BaselineSyncClient socketSyncClient) {
        this.socketSyncClient = socketSyncClient;
    }

    @Override
    public void init(ContainerContext cc) {
        //cc.addProcessingHandlers(handlers);
        cc.registerExtensionLoggerClass(BaselineLogExtension.class, 3);
        cc.registerConnectedObjectExtensionLoggerClass(BaselineLogExtension.class, 3);
        //cc.setAnonymousAccessAllowed(true); // defaults to true
        monitorRegistry = cc.getMonitorRegistry();
        monitorRegistry.addMonitor(new BaselineMonitor("DB"+instance, Status.OK));
        monitorRegistry.addMonitor(new BaselineMonitor("Cache"+instance, Status.OK));
        monitorRegistry.addMonitor(new BaselineMonitor("Service"+instance, Status.OK));
	}

    @Override
    public String echoCougarPropertyValue(RequestContext ctx, String propertyName, TimeConstraints timeConstraints){
        ctx.setRequestLogExtension(new BaselineLogExtension(propertyName, null, null));

        final Map<String,String> props = PropertyConfigurer.getAllLoadedProperties();
        return props.get(propertyName);
    }

    @Override
    public void interceptorCheckedExceptionOperation(RequestContext ctx, PreOrPostInterceptorException preOrPostException, TimeConstraints timeConstraints) throws SimpleException {
        //Null implementation as this method is used as part of a test for pre/post checked service defined exception handling
        ctx.setRequestLogExtension(new BaselineLogExtension(preOrPostException, null, null));
    }

	@Override
	public void listOfComplexOperation(RequestContext ctx, List<ComplexObject> inputList, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(inputList.toArray(), null, null));
	}

	@Override
	public void setOfComplexOperation(RequestContext ctx, Set<ComplexObject> inputSet, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(inputSet.toArray(), null, null));
	}

	@Override
	public void mapOfComplexOperation(RequestContext ctx, Map<String, ComplexObject> inputMap, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(inputMap.toString(), null, null));
	}

    @Override
    public void mandatoryCollectionElementTest ( RequestContext ctx , List<ComplexObject> inputList, Map<String, ComplexObject> inputMap, TimeConstraints timeConstraints)
    throws SimpleException
    {
        ctx.setRequestLogExtension(new BaselineLogExtension(inputList.toArray(), inputMap.toString(), null));
    }

    @Override
    public List<Date> testSimpleDateListGet ( RequestContext ctx , List<Date> inputList, TimeConstraints timeConstraints)
            throws SimpleException
    {
        ctx.setRequestLogExtension(new BaselineLogExtension(inputList.toArray(), null, null));

        return new ArrayList<Date>(inputList);
    }

    @Override
     public Map<String,String> testSimpleMapGet ( RequestContext ctx , Map<String,String> inputMap, TimeConstraints timeConstraints)
             throws SimpleException
     {
         ctx.setRequestLogExtension(new BaselineLogExtension(inputMap.entrySet(), null, null));

         Map<String, String> result = new HashMap<String, String>();

         result.putAll(inputMap);

         return result;
     }

    @Override
    public Set<String> testSimpleSetGet ( RequestContext ctx , Set<String> inputSet, TimeConstraints timeConstraints)
            throws SimpleException
    {
        ctx.setRequestLogExtension(new BaselineLogExtension(inputSet.toArray(), null, null));

        return new HashSet<String>(inputSet);
    }

    @Override
    public List<String> testSimpleListGet ( RequestContext ctx , List<String> inputList, TimeConstraints timeConstraints)
            throws SimpleException
    {
        ctx.setRequestLogExtension(new BaselineLogExtension(inputList.toArray(), null, null));

        return new ArrayList<String>(inputList);
    }


    @KPITimedEvent(value = "Baseline.service.testSimpleGet", catchFailures = true)
    @Override
    public SimpleResponse testSimpleGet(RequestContext ctx, String message, TimeConstraints timeConstraints) throws SimpleException {
        ctx.trace("Starting simple get for {}", message);
        ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));
        if (message.startsWith("FORWARD:")) {
            return baselineAsClient.testSimpleGet(ctx, "FORWARDED:"+ message.substring(8));
        } else {
            SimpleResponse response = new SimpleResponse();
            response.setMessage(message);
            return response;
        }
    }

	@KPITimedEvent(value = "Baseline.service.testSimpleGetQA", catchFailures = true)
	@Override
	public SimpleResponse testSimpleGetQA(RequestContext ctx, String message, TimeConstraints timeConstraints) throws SimpleException {
		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));
		ctx.trace("Starting simple get for {}", message);
		if (message.equalsIgnoreCase("GET_CHANNEL_INFO")) {
			SimpleResponse response = new SimpleResponse();
			response.setMessage("ChannelId: " + ctx.getIdentity().toString());
			return response;
		}
		else if (message.equalsIgnoreCase("DELEGATE")) {
			SimpleResponseDelegate delegate = new SimpleResponseDelegateImpl();
			return new SimpleResponse(delegate);
		}
		else {
			SimpleResponse response = new SimpleResponse();
			response.setMessage("service2-" + message);
			return response;
		}

	}

    @KPITimedEvent(value = "Baseline.service.testLargeGet", catchFailures = true)
    @Override
    public LargeRequest testLargeGet(RequestContext ctx, Integer size, TimeConstraints timeConstraints) throws SimpleException {
        ctx.trace("Starting large get for array of size %d", size);
        ctx.setRequestLogExtension(new BaselineLogExtension(size, null, null));
        LargeRequest result = new LargeRequest();
        result.setObjects(new ArrayList<ComplexObject>());
        result.setSize(size);
        result.setOddOrEven(size % 2 == 0 ? LargeRequestOddOrEvenEnum.EVEN : LargeRequestOddOrEvenEnum.ODD);
        for (int i = 0; i < size; i++) {
            ComplexObject o = new ComplexObject();
            o.setName("name " + i);
            o.setValue1(i);
            o.setValue2(i + 1);
            result.getObjects().add(o);
        }
        return result;
    }

    @KPITimedEvent(value = "Baseline.service.testSimpleTypeReplacement", catchFailures = true)
    @Override
    public SimpleContainer testSimpleTypeReplacement(RequestContext ctx, Integer simpleInt, Double simpleDouble, String simpleString,
                                                     SimpleValidValue simpleEnum, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(simpleInt, simpleDouble, simpleEnum));
        SimpleContainer cont = new SimpleContainer();
        cont.setSimpleInt(simpleInt);
        cont.setSimpleDouble(simpleDouble);
        cont.setSimpleString(simpleString);
        cont.setSimpleEnum(simpleEnum);
        return cont;
    }

    @KPITimedEvent(value = "Baseline.service.testStringableLists", catchFailures = true)
    @Override
    public SimpleListContainer testStringableLists(RequestContext ctx, Set<Integer> intList, List<String> stringList,
                                                   List<SimpleValidValue> enumList, TimeConstraints timeConstraints) throws SimpleException {

        ctx.setRequestLogExtension(new BaselineLogExtension(intList.size(), stringList.size(), enumList.size()));
        SimpleListContainer cont = new SimpleListContainer();
        cont.setInts(new ArrayList<Integer>(intList));
        cont.setStrings(stringList);
        cont.setEnums(new HashSet<SimpleValidValue>(enumList));
        return cont;
    }

    @KPITimedEvent(value = "Baseline.service.testParameterStyles", catchFailures = true)
    @Override
    public List<String> testParameterStyles(RequestContext ctx,
                                            TestParameterStylesHeaderParamEnum headerParam, String secondHeaderParam, String queryParam, Date dateQueryParam, Float ok, TimeConstraints timeConstraints) {

        ctx.setRequestLogExtension(new BaselineLogExtension(queryParam, ok, null));

        List<String> response = new ArrayList<String>();
        response.add("secondHeaderParam=" + secondHeaderParam);
        response.add("queryParam=" + queryParam);
        response.add("headerParam=" + headerParam);
        response.add("dateQueryParam=" + dateInUTC(dateQueryParam));

        response.add("ok=" + ok);
        return response;
    }

	@KPITimedEvent(value = "Baseline.service.testParameterStylesQA", catchFailures = true)
	@Override
	public SimpleResponse testParameterStylesQA(RequestContext ctx,
			TestParameterStylesQAHeaderParamEnum headerParam, String queryParam, Date dateQueryParam, TimeConstraints timeConstraints) {

		ctx.setRequestLogExtension(new BaselineLogExtension(queryParam, null, null));
		SimpleResponse response = new SimpleResponse();
		response.setMessage("headerParam=" + headerParam
                + ",queryParam=" + queryParam + ",dateQueryParam=" + dateInUTC(dateQueryParam));
		return response;
	}

    private String dateInUTC(Date date) {
        if (date == null) {
            return "null";
        }
        return date.toGMTString().replace("GMT","UTC");
    }

    @KPITimedEvent(value = "Baseline.service.testDateRetrieval", catchFailures = true)
    @Override
    public DateContainer testDateRetrieval(RequestContext ctx, DateContainer inputDates, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(inputDates.getFirst(), inputDates.getLast(), inputDates.getDifference()));
        DateContainer response = new DateContainer();
        response.setFirst(inputDates.getFirst());
        response.setLast(inputDates.getLast());
        response.setName("First Passed Date: " + inputDates.getFirst() + ", Second Passed Date: " + inputDates.getLast());
        List<Date> dateList = inputDates.getAllDates();
        response.setAllDates(dateList);
        response.setDifference(inputDates.getDifference());
        return response;
    }

    @KPITimedEvent(value = "Baseline.service.testDoubleHandling", catchFailures = true)
    @Override
    public DoubleResponse testDoubleHandling(RequestContext ctx,
                                             DoubleContainer doubleContainer, Double doubleVal, TimeConstraints timeConstraints)
            throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(doubleContainer.getMap().size(), doubleContainer.getVal(), doubleVal));
        DoubleResponse resp = new DoubleResponse();
        resp.setMap(doubleContainer.getMap());
        resp.setTopLevelVal(doubleVal);
        resp.setVal(doubleContainer.getVal());
        return resp;
    }

    @KPITimedEvent(value = "Baseline.service.testComplexMutator", catchFailures = true)
    @Override
    public SimpleResponse testComplexMutator(RequestContext ctx, ComplexObject message, TimeConstraints timeConstraints) throws SimpleException {
        ctx.trace("Starting complex mutator for {}", message.getName());
        ctx.setRequestLogExtension(new BaselineLogExtension(message, "mutate", null));
        SimpleResponse response = new SimpleResponse();
        Integer val2 = message.getValue2();
        if (val2 == null) {
            val2 = 0;
        }
        response.setMessage(message.getName() + " = " + (message.getValue1() + val2));
        return response;
    }

    @KPITimedEvent(value = "Baseline.service.testLargePost", catchFailures = true)
    @Override
    public SimpleResponse testLargePost(RequestContext ctx, LargeRequest message, TimeConstraints timeConstraints) throws SimpleException {
        ctx.trace("Starting large post with array size {}", message.getSize());
        ctx.setRequestLogExtension(new BaselineLogExtension(message.getOddOrEven(), "largepost", message.getSize()));
        SimpleResponse response = new SimpleResponse();
        response.setMessage("There were " + message.getSize() + " items specified in the list, " + message.getObjects().size()
                + " actually");
        return response;
    }

	@KPITimedEvent(value = "Baseline.service.testLargePostQA", catchFailures = true)
	@Override
	public SimpleResponse testLargePostQA(RequestContext ctx, LargeRequest message, TimeConstraints timeConstraints)
			throws SimpleException {
		ctx.trace("Starting large post with array size {}", message.getSize());
		ctx.setRequestLogExtension(new BaselineLogExtension(message.getOddOrEven(), "largepostQA", message.getSize()));
		SimpleResponse response = new SimpleResponse();

		Boolean returnList = message.getReturnList();
		if ((returnList != null) && (returnList)) {

			List<ComplexObject> complexObjects = message.getObjects();

			StringBuffer namesBuff = new StringBuffer();
			namesBuff.append("Names: ");
			StringBuffer value1sBuff = new StringBuffer();
			value1sBuff.append("Value1s: ");
			StringBuffer value2sBuff = new StringBuffer();
			value2sBuff.append("Value2s: ");

			for (ComplexObject complexObject: complexObjects) {
				if (complexObject == null) {
					namesBuff.append("null");
					value1sBuff.append("null");
					value2sBuff.append("null");
				} else {
					namesBuff.append(complexObject.getName());
					value1sBuff.append(complexObject.getValue1());
					value2sBuff.append( complexObject.getValue2());
				}
			}
			String names = namesBuff.toString();
			String value1s = value1sBuff.toString();
			String value2s = value2sBuff.toString();

			response.setMessage(names + " - " + value1s + " - " + value2s);
		} else {
			response.setMessage("There were " + message.getSize()
					+ " items specified in the list, "
					+ message.getObjects().size() + " actually");
		}
		return response;
	}

    @KPITimedEvent(value = "Baseline.service.testException", catchFailures = true)
    @Override
    public SimpleResponse testException(RequestContext ctx, String responseCode, String message, TimeConstraints timeConstraints) throws SimpleException, WotsitException {
        ctx.trace("Starting exception thrower with message {}", message);
        ResponseCode response;
        try {
            response = ResponseCode.valueOf(responseCode);
        }
        catch (IllegalArgumentException e) {
            response = ResponseCode.InternalError;
        }

        if (message.equals("throwRuntime")) {
            throw new RuntimeException("Requested");
        } else {
            try {
                SimpleExceptionErrorCodeEnum errCode = SimpleExceptionErrorCodeEnum.valueOf(message.toUpperCase(Locale.ENGLISH));
                throw new SimpleException(response, errCode, message);
            }
            catch (IllegalArgumentException e) {
                try {
                    WotsitExceptionErrorCodeEnum errCode = WotsitExceptionErrorCodeEnum.valueOf(message.toUpperCase(Locale.ENGLISH));
                    throw new WotsitException(e,
                            response,
                            errCode, (message.hashCode() % 2) != 0 ? WotsitExceptionTypeEnum.CHEESY : WotsitExceptionTypeEnum.SPICY,
                            String.valueOf(responseCode));
                }
                catch (IllegalArgumentException ex) {
                }
                throw new SimpleException(e,response, SimpleExceptionErrorCodeEnum.NULL, message);
            }
        }
    }

	 @KPITimedEvent(value = "Baseline.service.testExceptionQA", catchFailures = true)
	@Override
	public SimpleResponse testExceptionQA(RequestContext ctx, String message, TimeConstraints timeConstraints)
			throws SimpleException, WotsitException {
		ctx.trace("Starting exception thrower with message {}", message);
		ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));

		try {
			SimpleExceptionErrorCodeEnum errCode = SimpleExceptionErrorCodeEnum
					.valueOf(message);
			throw new SimpleException(ResponseCode.Unauthorised, errCode,
					message);
		} catch (IllegalArgumentException e) {
			try {
				WotsitExceptionErrorCodeEnum errCode = WotsitExceptionErrorCodeEnum
						.valueOf(message);
				throw new WotsitException(e,
						ResponseCode.Forbidden,
						errCode,
						(message.hashCode() % 2 != 0) ? WotsitExceptionTypeEnum.CHEESY
								: WotsitExceptionTypeEnum.SPICY, message);
			} catch (IllegalArgumentException ex) {
			}
			throw new SimpleException(e,ResponseCode.Unauthorised,
					SimpleExceptionErrorCodeEnum.NULL, message);
		}

	}

    @KPITimedEvent(value = "Baseline.service.testNamedCougarException", catchFailures = true)
    @Override
    public SimpleResponse testNamedCougarException(RequestContext ctx, String errorCodeName, TimeConstraints timeConstraints) {
        throw new CougarServiceException(ServerFaultCode.valueOf(errorCodeName), "Test throwing an exception with error code: "+errorCodeName);
    }

    @KPITimedEvent(value = "Baseline.service.testSleep", catchFailures = true)
    @Override
    public void testSleep(RequestContext ctx, Long sleep, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(sleep, null, null));

        if (sleep <= 0) {
            throw new SimpleException(ResponseCode.BadRequest, SimpleExceptionErrorCodeEnum.GENERIC, "Sleep must be > 0");
        }

        CountDownLatch latch = new CountDownLatch(1);
        outstandingSleeps.add(latch);

        try {
            latch.await(sleep, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
        outstandingSleeps.remove(latch);
    }

    @KPITimedEvent(value = "Baseline.service.testSleep", catchFailures = true)
    @Override
    public Integer cancelSleeps(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));


        List<CountDownLatch> allSleeps = new ArrayList<CountDownLatch>(outstandingSleeps);
        outstandingSleeps.removeAll(allSleeps);
        for (CountDownLatch latch : allSleeps) {
            latch.countDown();
        }
        return allSleeps.size();
    }

    @KPITimedEvent(value = "Baseline.service.testSecureService", catchFailures = true)
    public SimpleResponse testSecureService(RequestContext ctx, String message, TimeConstraints timeConstraints) throws SimpleException {
       	throw new UnsupportedOperationException("implement me");
    }

    @Override
    public IdentChain testIdentityChain(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        if (ctx.getIdentity() == null) {
            return null;
        }
        IdentChain result = new IdentChain();
        result.setIdentities(new ArrayList<Ident>());
        for (Identity i: ctx.getIdentity().getIdentities()) {
            Ident ident = new Ident();
            ident.setPrincipal(i.getPrincipal().getName());
            ident.setCredentialName(i.getCredential().getName());
            ident.setCredentialValue((String)i.getCredential().getValue());
            result.getIdentities().add(ident);
        }
        return result;
    }

    @KPITimedEvent(value = "Baseline.service.testNoParams", catchFailures = true)
    @Override
    public NoParamsResponse testNoParams(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        NoParamsResponse response = new NoParamsResponse();
        response.setStatus("hello");
        response.setVersion("1.0.0");
        return response;
    }

    @KPITimedEvent(value = "Baseline.service.testLargeMapGet", catchFailures = true)
    @Override
    public MapDataType testLargeMapGet(RequestContext ctx, Integer size, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(size, null, null));
        MapDataType result = new MapDataType();
        result.setCache(new HashMap<Integer, ComplexObject>());
        result.setSomeMap(new HashMap<String, ComplexObject>());
        for (int i = 0; i < size; i++) {
            ComplexObject o = new ComplexObject();
            o.setName("name " + i);
            o.setValue1(i);
            o.setValue2(i + 1);
            result.getCache().put(i, o);
            result.getSomeMap().put(String.valueOf(i), o);
        }

        return result;
    }

    @KPITimedEvent(value = "Baseline.service.testMapsNameClash", catchFailures = true)
    @Override
    public SimpleResponseMap testMapsNameClash(RequestContext ctx, SimpleMap mapParam, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(mapParam.getCache().size(), null, null));
        Set<Entry<String, String>> entry = mapParam.getCache().entrySet();
        SimpleResponseMap map = new SimpleResponseMap();
        map.setCache(new HashMap<String, String>());
        for (Entry<String, String> ent : entry) {
            map.getCache().put("RESULT:" + ent.getKey(), "RESULT:" + ent.getValue());
        }
        return map;
    }

    @KPITimedEvent(value = "Baseline.service.testListRetrieval", catchFailures = true)
    @Override
    public PrimitiveLists testListRetrieval(RequestContext ctx, Integer seed, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(seed, null, null));
        PrimitiveLists response = new PrimitiveLists();
        Random rnd = new Random(seed);

        response.setBytes(new byte[rnd.nextInt(7) + 3]);
        for (int i = 0; i < response.getBytes().length; i++) {
            response.getBytes()[i] = (byte)rnd.nextInt();
        }

        response.setI32s(new ArrayList<Integer>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getI32s().add(rnd.nextInt());
        }

        response.setI64s(new ArrayList<Long>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getI64s().add(rnd.nextLong());
        }

        response.setFloats(new ArrayList<Float>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getFloats().add(rnd.nextFloat());
        }

        response.setDoubles(new ArrayList<Double>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getDoubles().add(rnd.nextDouble());
        }

        response.setStrings(new ArrayList<String>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getStrings().add(String.valueOf((rnd.nextLong())));
        }

        response.setDates(new ArrayList<Date>());
        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            response.getDates().add(new Date(rnd.nextLong()));
        }
        return response;
    }

    @Override
    public SimpleResponse testBodyParams(RequestContext ctx, String message, Integer value, ComplexObject complex, SimpleValidValue myEnum,
                                         ComplexObject anotherComplex, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(message, value, complex));

        SimpleResponse response = new SimpleResponse();
        response.setMessage("message={" + message + "},value={" + value + "},complex={" + complex + "},myEnum={" + myEnum
                + "},anotherComplex={" + anotherComplex + "}");
        return response;
    }

    @Override
    public List<SimpleResponse> testDirectListReturn(RequestContext ctx, Integer seed, AsyncBehaviour async, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(seed, "List", async));
        List<SimpleResponse> response = new ArrayList<SimpleResponse>();
        Random rnd = new Random(seed);

        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            SimpleResponse sr = new SimpleResponse();
            sr.setMessage(String.valueOf(async));
            response.add(sr);
        }
        if (async == AsyncBehaviour.SYNC) {
            return response;
        } else {
			throw new SimpleException(ResponseCode.ServiceUnavailable,SimpleExceptionErrorCodeEnum.FORBIDDEN ,"Suspend, is no longer supported");
        }
    }

    @Override
    public Map<String, SimpleResponse> testDirectMapReturn(RequestContext ctx, Integer seed, AsyncBehaviour async, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(seed, "Map", async));
        Map<String, SimpleResponse> response = new HashMap<String, SimpleResponse>();
        Random rnd = new Random(seed);

        for (int i = 0; i < rnd.nextInt(7) + 3; i++) {
            SimpleResponse sr = new SimpleResponse();
            sr.setMessage(String.valueOf(async));
            response.put(String.valueOf(i), sr);
        }
        if (async == AsyncBehaviour.SYNC) {
            return response;
        } else {
        	throw new SimpleException(ResponseCode.ServiceUnavailable,SimpleExceptionErrorCodeEnum.FORBIDDEN ,"Suspend, is no longer supported");
        }
    }

    @KPITimedEvent(value = "Baseline.service.kpiTesting", catchFailures = true)
	@Override
	public SimpleResponse kpiTesting(RequestContext ctx, String message, TimeConstraints timeConstraints)
			throws SimpleException {
		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));
		ctx.trace("Starting kpiTesting for {}", message);

		SimpleResponse response = new SimpleResponse();
		response.setMessage("This method uses KPI testing. Message received : " + message);
		return response;
	}

	@Override
	public SimpleResponse waitSeconds(RequestContext ctx, String seconds, TimeConstraints timeConstraints)
			throws SimpleException {
		ctx.setRequestLogExtension(new BaselineLogExtension(seconds, null, null));
		ctx.trace("Starting waitSeconds for {}", seconds);

		try {
			long sec = Long.parseLong(seconds);
			Thread.currentThread().sleep(sec * 1000);

		} catch (NumberFormatException e) {
			throw new SimpleException(ResponseCode.InternalError,SimpleExceptionErrorCodeEnum.GENERIC, e.getMessage());
		} catch (InterruptedException e) {
			throw new SimpleException(ResponseCode.Timeout,SimpleExceptionErrorCodeEnum.TIMEOUT,e.getMessage());
		}
		SimpleResponse response = new SimpleResponse();
		response.setMessage("Waited for " + seconds + " seconds.");
		return response;
	}

	@Override
	public SimpleResponse logMessage(RequestContext ctx, String logString, String logLevel, TimeConstraints timeConstraints)
			throws SimpleException {
		ctx.setRequestLogExtension(new BaselineLogExtension(logString, null, null));
		ctx.trace("Starting logMessage for {}", logString);

        // todo: should change this to an enum..?? at least change to a shorter list of possible values..
        Level level = Level.toLevel(logLevel);
        if (level.equals(Level.ERROR)) {
            LOGGER.error(logString);
        }
        else if (level.equals(Level.WARN)) {
            LOGGER.warn(logString);
        }
        else if (level.equals(Level.INFO)) {
            LOGGER.info(logString);
        }
        else if (level.equals(Level.DEBUG)) {
            LOGGER.debug(logString);
        }
        else
        {
            throw new SimpleException(SimpleExceptionErrorCodeEnum.UNRECOGNIZED_VALUE, "Unsupported log level: "+logLevel);
        }

		SimpleResponse response = new SimpleResponse();
		response.setMessage(logString + " logged at " + logLevel);
		return response;
	}

    @Override
    public Long bulkCaller(RequestContext ctx, Integer cycles, String logLevel, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(cycles, null, null));
        final CountDownLatch latch = new CountDownLatch(cycles);
        ExecutionObserver obs = new ExecutionObserver() {
            @Override
            public void onResult(ExecutionResult executionResult) {
                latch.countDown();
            }
        };
        LOGGER.info("Bulk calling testSimpleGet %d time", cycles);
        long startTime = System.nanoTime();
        for (int i = 0; i < cycles; ++i) {
            baselineAsyncClient.testSimpleGet(ctx, "message:"+i, obs);
        }
        LOGGER.info("Bulk calls complete");
        try { latch.await(); } catch (InterruptedException e) {}
        long timeTaken = System.nanoTime() - startTime;
        LOGGER.info("All Latches returned in %,d ms", timeTaken / 1000000);
        return timeTaken;
    }

    @Override
	public SimpleResponse changeLogLevel(RequestContext ctx, String logName,
			String level, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(logName + ": " + level, null, null));
		ctx.trace("Starting changeLogLevel to {}", level);

		SimpleResponse response = new SimpleResponse();
		if ((logName==null) || (logName.equalsIgnoreCase("")) || (logName.equalsIgnoreCase("service"))) {
            loggingControl.setLogLevel(BaselineServiceImpl.class.getName(), level, false);
			response.setMessage("Service logging level set at " + level);
		} else {
            loggingControl.setLogLevel(logName, level, false);
			response.setMessage(logName + " logging level set at " + level);
		}

        LOGGER.warn("A warning message");
        LOGGER.info("A warning message");

		return response;
	}

	@Override
	public EnumOperationResponseObject enumOperation(RequestContext ctx,
			EnumOperationHeaderParamEnum headerParam, EnumOperationQueryParamEnum queryParam, BodyParamEnumObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		EnumOperationResponseObject responseObject = new EnumOperationResponseObject();

		if (headerParam != null) {
			responseObject.setHeaderParameter(EnumOperationResponseObjectHeaderParameterEnum.valueOf(headerParam.toString()));
		}
		if (queryParam != null) {
			responseObject.setQueryParameter(EnumOperationResponseObjectQueryParameterEnum.valueOf(queryParam.toString()));
		}
		if (message != null && message.getBodyParameter() != null) {
			responseObject.setBodyParameter(EnumOperationResponseObjectBodyParameterEnum.valueOf(message.getBodyParameter().toString()));
		}

		return responseObject;
	}

	@Override
	public I32OperationResponseObject i32Operation(RequestContext ctx, Integer headerParam, Integer queryParam,
			BodyParamI32Object message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		I32OperationResponseObject returnObject = new I32OperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;
	}

	@Override
	public I64OperationResponseObject i64Operation(RequestContext ctx, Long headerParam, Long queryParam,
			BodyParamI64Object message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		I64OperationResponseObject returnObject = new I64OperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;
	}

	@Override
	public ByteOperationResponseObject byteOperation(RequestContext ctx, Byte headerParam, Byte queryParam,
			BodyParamByteObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		ByteOperationResponseObject returnObject = new ByteOperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;

	}

	@Override
	public FloatOperationResponseObject floatOperation(RequestContext ctx, Float headerParam, Float queryParam,
			BodyParamFloatObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		FloatOperationResponseObject returnObject = new FloatOperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;

	}

	@Override
	public DoubleOperationResponseObject doubleOperation(RequestContext ctx, Double headerParam, Double queryParam,
			BodyParamDoubleObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		DoubleOperationResponseObject returnObject = new DoubleOperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;
	}

	@Override
	public BoolOperationResponseObject boolOperation(RequestContext ctx, Boolean headerParam, Boolean queryParam,
			BodyParamBoolObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		BoolOperationResponseObject returnObject = new BoolOperationResponseObject();

		returnObject.setBodyParameter(message.getBodyParameter());
		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);

		return returnObject;
	}

	@Override
	public NonMandatoryParamsOperationResponseObject nonMandatoryParamsOperation(RequestContext ctx, String headerParam,
			String queryParam, NonMandatoryParamsRequest message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		NonMandatoryParamsOperationResponseObject returnObject = new NonMandatoryParamsOperationResponseObject();

		/*
		 *
		 * Path paramters must always be passed regardless of mandatory flag in IDL so assume passed
		 */
		if (headerParam != null) {
			returnObject.setHeaderParameter(headerParam);
		}
		else {
			returnObject.setHeaderParameter(null);
		}
		if (queryParam != null) {
			returnObject.setQueryParameter(queryParam);
		}
		if ((message != null) && (message.getBodyParameter1() != null)) {
			returnObject.setBodyParameter1(message.getBodyParameter1());
		}
		if ((message != null) && (message.getBodyParameter2() != null)) {
			returnObject.setBodyParameter2(message.getBodyParameter2());
		}

		return returnObject;
	}

	@Override
	public MandatoryParamsOperationResponseObject mandatoryParamsOperation(RequestContext ctx, String headerParam,
			String queryParam, MandatoryParamsRequest message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		MandatoryParamsOperationResponseObject returnObject = new MandatoryParamsOperationResponseObject();

		returnObject.setHeaderParameter(headerParam);
		returnObject.setQueryParameter(queryParam);
		returnObject.setBodyParameter1(message.getBodyParameter1());
		if (message.getBodyParameter2() != null) {
			returnObject.setBodyParameter2(message.getBodyParameter2());
		}

		return returnObject;

	}

	private Status toStatus(String statusString) {
        Status s = Status.valueOf(statusString);
        if (s == null) {
            throw new IllegalArgumentException("Unrecognised status: "+ statusString);
        }
        return s;
    }

	@Override
	public SimpleResponse setHealthStatusInfo(RequestContext ctx,
			HealthStatusInfoRequest message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		CougarComponentStatuses cacheAccessStatusDetail = message
				.getCacheAccessStatusDetail();
		CougarComponentStatuses dbConnectionStatusDetail = message
				.getDBConnectionStatusDetail();
		CougarComponentStatuses serviceStatusDetail = message
				.getServiceStatusDetail();
		Boolean initialiseHealthStatusDetail = message.getInitialiseHealthStatusObject();

        // reset the status
		if ((initialiseHealthStatusDetail != null) && (initialiseHealthStatusDetail)) {
			((BaselineMonitor)monitorRegistry.getMonitor("DB"+instance)).setStatus(Status.OK);
			((BaselineMonitor)monitorRegistry.getMonitor("Cache"+instance)).setStatus(Status.OK);
			((BaselineMonitor)monitorRegistry.getMonitor("Service"+instance)).setStatus(Status.OK);
		}

		if (serviceStatusDetail != null) {
			((BaselineMonitor)monitorRegistry.getMonitor("Service"+instance)).setStatus(toStatus(serviceStatusDetail.toString()));
		}

		if (cacheAccessStatusDetail != null) {
			((BaselineMonitor)monitorRegistry.getMonitor("Cache"+instance)).setStatus(toStatus(cacheAccessStatusDetail.toString()));
		}

		if (dbConnectionStatusDetail != null) {
			((BaselineMonitor)monitorRegistry.getMonitor("DB"+instance)).setStatus(toStatus(dbConnectionStatusDetail.toString()));
		}

		SimpleResponse response = new SimpleResponse();
		response.setMessage("Health Status Info set for Baseline app.");
		return response;

	}

	@Override
	public DateTimeOperationResponseObject dateTimeOperation(RequestContext ctx, BodyParamDateTimeObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		DateTimeOperationResponseObject responseObject = new DateTimeOperationResponseObject();

		Date requestDate = message.getDateTimeParameter();
		Date createdDate = new Date(requestDate.getTime());

		responseObject.setLocalTime(requestDate);
		responseObject.setLocalTime2(createdDate);

		return responseObject;

	}

	@Override
	public SimpleMapOperationResponseObject simpleMapOperation(RequestContext ctx, BodyParamSimpleMapObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		SimpleMapOperationResponseObject response = new SimpleMapOperationResponseObject();
		Map<String,String> requestMap = message.getSimpleMap();

		Map<String,String> responseMap = new LinkedHashMap<String,String>();

		Object[] requestMapKeys = requestMap.keySet().toArray();
		Arrays.sort(requestMapKeys);

		for (Object key : requestMapKeys) {
			String value = requestMap.get(key.toString());
			responseMap.put(key.toString(), value);
		}

		response.setResponseMap(responseMap);

		return response;

	}

	@Override
	public ComplexMapOperationResponseObject complexMapOperation(RequestContext ctx, BodyParamComplexMapObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		Map<String,SomeComplexObject> requestMap = message.getComplexMap();
		Object[] requestMapKeys = requestMap.keySet().toArray();
		Arrays.sort(requestMapKeys);

		if (requestMap.size() == 1 && requestMapKeys[0].toString().equalsIgnoreCase("DELEGATE")) {
			ComplexMapOperationResponseObjectDelegate delegate = new ComplexMapOperationResponseObjectDelegateImpl();
			return new ComplexMapOperationResponseObject(delegate);
		}
		else {

			Map<String,SomeComplexObject> responseMap = new LinkedHashMap<String,SomeComplexObject>();

			for (Object key : requestMapKeys) {
				SomeComplexObject responseComplexObject = new SomeComplexObject();
				SomeComplexObject requestComplexObject = requestMap.get(key.toString());
				responseComplexObject.setDateTimeParameter(requestComplexObject.getDateTimeParameter());
				responseComplexObject.setListParameter(requestComplexObject.getListParameter());
				responseComplexObject.setEnumParameter(requestComplexObject.getEnumParameter());
				responseComplexObject.setStringParameter(requestComplexObject.getStringParameter());
				responseMap.put(key.toString(), responseComplexObject);
			}

			ComplexMapOperationResponseObject response = new ComplexMapOperationResponseObject();
			response.setResponseMap(responseMap);
			return response;
		}
	}

	@Override
	public SimpleSetOperationResponseObject simpleSetOperation(RequestContext ctx, BodyParamSimpleSetObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		Set<String> requestSet = message.getSimpleSet();
		Set<String> responseSet = new LinkedHashSet<String>();

		Object[] requestSetArray = requestSet.toArray();

		for (int i = 0; i < requestSetArray.length; i++) {
			Object object = requestSetArray[i];
			if (object == null) {
				requestSetArray[i] = "1111111111111111111111111111";
			}

		}

		Arrays.sort(requestSetArray);

		for (Object object : requestSetArray) {
			if ("1111111111111111111111111111".equals(object)) {
				responseSet.add(null);
			}
			else {
				responseSet.add(String.valueOf(object));
			}
		}

		SimpleSetOperationResponseObject response = new SimpleSetOperationResponseObject();
		response.setResponseSet(responseSet);
		return response;
	}

	@Override
	public ComplexSetOperationResponseObject complexSetOperation(RequestContext ctx, BodyParamComplexSetObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));
		Set<SomeComplexObject> requestSet = message.getComplexSet();
		SetBuilder<SomeComplexObject> responseSet = new SetBuilder<SomeComplexObject>().toLinkedHashSet();

		// Put object from set into a map with the value from the string field as the Key
		// So we can order based on that.

		Map<String,SomeComplexObject> requestSetObjectMap = new HashMap<String,SomeComplexObject>();
		String[] mapKeys = new String[requestSet.size()];
		int counter = 0;
		for (SomeComplexObject someComplexObject : requestSet) {
			requestSetObjectMap.put(someComplexObject.getStringParameter(), someComplexObject);
			mapKeys[counter] = someComplexObject.getStringParameter();
			counter++;
		}

		Arrays.sort(mapKeys);

		for (String key : mapKeys) {
			SomeComplexObject requestComplexObject = requestSetObjectMap.get(key);
			SomeComplexObjectBuilder responseComplexObject = new SomeComplexObjectBuilder()
                .setDateTimeParameter(requestComplexObject.getDateTimeParameter())
                .setListParameter(requestComplexObject.getListParameter())
                .setEnumParameter(requestComplexObject.getEnumParameter())
                .setStringParameter(requestComplexObject.getStringParameter());

			responseSet.add(responseComplexObject);
		}

		ComplexSetOperationResponseObject response = new ComplexSetOperationResponseObjectBuilder()
		    .setResponseSet(responseSet).build();
		return response;
	}

	@Override
	public DateTimeSetOperationResponseObject dateTimeSetOperation(RequestContext ctx, BodyParamDateTimeSetObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		Set<Date> requestSet = message.getDateTimeSet();
		TreeSet<Date> responseSet = new TreeSet<Date>();

		for (Date object : requestSet) {
			responseSet.add(object);
		}

		DateTimeSetOperationResponseObject responseObject = new DateTimeSetOperationResponseObject();
		responseObject.setResponseSet(responseSet);

		return responseObject;

	}

	@Override
	public DateTimeListOperationResponseObject dateTimeListOperation(RequestContext ctx, BodyParamDateTimeListObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		List<Date> requestList = message.getDateTimeList();
		List<Date> responseList = new LinkedList<Date>();

		for (Date object : requestList) {
			responseList.add(object);
		}

		DateTimeListOperationResponseObject responseObject = new DateTimeListOperationResponseObject();
		responseObject.setResponseList(responseList);

		return responseObject;

	}


	@Override
	public DateTimeMapOperationResponseObject dateTimeMapOperation(RequestContext ctx, BodyParamDateTimeMapObject message, TimeConstraints timeConstraints)
			throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		Map<String,Date> requestMap = message.getDateTimeMap();
		Map<String, Date> responseMap = new HashMap<String, Date>();

		for (String key : requestMap.keySet()) {
			responseMap.put(key, requestMap.get(key));
		}

		DateTimeMapOperationResponseObject responseObject = new DateTimeMapOperationResponseObject();
		responseObject.setResponseMap(responseMap);

		return responseObject;

	}

	@Override
	public MapDateTimeKeyOperationResponseObject mapDateTimeKeyOperation(RequestContext ctx, BodyParamMapDateTimeKeyObject message, TimeConstraints timeConstraints)
			throws SimpleException{

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

		Map<Date,String> requestMap = message.getMapDateTimeKey();
		Map<Date, String> responseMap = new HashMap<Date, String>();

		for (Date key : requestMap.keySet()) {
			responseMap.put(key, requestMap.get(key));
		}

		MapDateTimeKeyOperationResponseObject responseObject = new MapDateTimeKeyOperationResponseObject();
		responseObject.setResponseMap(responseMap);

		return responseObject;
	}

	@Override
	public I32SimpleOperationResponseObject i32SimpleTypeOperation(RequestContext ctx, Integer headerParam, Integer queryParam, I32SimpleTypeRequestObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));


		I32SimpleOperationResponseObject response = new I32SimpleOperationResponseObject();

		response.setBodyParameter(message.getBodyParameter());
		response.setHeaderParameter(headerParam);
		response.setQueryParameter(queryParam);

		return response;
	}

	@Override
	public EnumSimpleResponseObject enumSimpleOperation(RequestContext ctx, SimpleEnum headerParam, SimpleEnum queryParam, EnumSimpleRequestObject message, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));


		EnumSimpleResponseObject response = new EnumSimpleResponseObject();

		response.setBodyParameter(message.getBodyParameter());
		response.setHeaderParameter(headerParam);
		response.setQueryParameter(queryParam);

		return response;

	}

	@Override
	public NonMandatoryParamsOperationResponseObject stringListOperation(RequestContext ctx,
			List<String> headerParam, List<String> queryParam, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));

		StringBuffer headerParamsBuff = new StringBuffer();
		for (String headerParamEntry : headerParam) {
			String entry = headerParamEntry + ",";
			headerParamsBuff.append(entry);
		}
		String headerParamEntries = headerParamsBuff.toString();
		headerParamEntries = headerParamEntries.substring(0, headerParamEntries.length() - 1);

		StringBuffer queryParamsBuff = new StringBuffer();
		for (String queryParamEntry : queryParam) {
			String entry = queryParamEntry + ",";
			queryParamsBuff.append(entry);
		}
		String queryParamEntries = queryParamsBuff.toString();
		queryParamEntries = queryParamEntries.substring(0, queryParamEntries.length() - 1);

		NonMandatoryParamsOperationResponseObject responseObject = new NonMandatoryParamsOperationResponseObject();
		responseObject.setHeaderParameter(headerParamEntries);
		responseObject.setQueryParameter(queryParamEntries);

		return responseObject;
	}

	@Override
	public NonMandatoryParamsOperationResponseObject stringSetOperation(RequestContext ctx, Set<String> headerParam,
			Set<String> queryParam, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));

		int loopCounter;

		String[] headerParamsArray = new String[headerParam.size()];
		loopCounter = 0;
		for (String headerParamEntry : headerParam) {
			headerParamsArray[loopCounter] = headerParamEntry;
			loopCounter++;
		}
		Arrays.sort(headerParamsArray, String.CASE_INSENSITIVE_ORDER);

		StringBuffer headerParamsBuff = new StringBuffer();
		for (loopCounter = 0; loopCounter < headerParamsArray.length; loopCounter++) {
			String entry = headerParamsArray[loopCounter] + ",";
			headerParamsBuff.append(entry);

		}
		String headerParamEntries = headerParamsBuff.toString();
		headerParamEntries = headerParamEntries.substring(0, headerParamEntries.length() - 1);

		String[] queryParamsArray = new String[queryParam.size()];
		loopCounter = 0;
		for (String queryParamEntry : queryParam) {
			queryParamsArray[loopCounter] = queryParamEntry;
			loopCounter++;
		}
		Arrays.sort(queryParamsArray, String.CASE_INSENSITIVE_ORDER);

		StringBuffer queryParamsBuff = new StringBuffer();
		for (loopCounter = 0; loopCounter < queryParamsArray.length; loopCounter++) {
			String entry = queryParamsArray[loopCounter] + ",";
			queryParamsBuff.append(entry);

		}
		String queryParamEntries = queryParamsBuff.toString();
		queryParamEntries = queryParamEntries.substring(0, queryParamEntries.length() - 1);

		NonMandatoryParamsOperationResponseObject responseObject = new NonMandatoryParamsOperationResponseObject();
		responseObject.setHeaderParameter(headerParamEntries);
		responseObject.setQueryParameter(queryParamEntries);

		return responseObject;
	}

    @Override
    public SimpleValidValue callWithEnumResponse(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));

        return SimpleValidValue.WEASEL;
    }

	@Override
	public NonMandatoryParamsOperationResponseObject simpleEnumListOperation(RequestContext ctx,
			List<SimpleEnum> headerParam, List<SimpleEnum> queryParam, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));

		StringBuffer headerParamsBuff = new StringBuffer();
		for (SimpleEnum headerParamEntry : headerParam) {
			String entry = headerParamEntry + ",";
			headerParamsBuff.append(entry);
		}
		String headerParamEntries = headerParamsBuff.toString();
		headerParamEntries = headerParamEntries.substring(0, headerParamEntries.length() - 1);

		StringBuffer queryParamsBuff = new StringBuffer();
		for (SimpleEnum queryParamEntry : queryParam) {
			String entry = queryParamEntry + ",";
			queryParamsBuff.append(entry);
		}
		String queryParamEntries = queryParamsBuff.toString();
		queryParamEntries = queryParamEntries.substring(0, queryParamEntries.length() - 1);

		NonMandatoryParamsOperationResponseObject responseObject = new NonMandatoryParamsOperationResponseObject();
		responseObject.setHeaderParameter(headerParamEntries);
		responseObject.setQueryParameter(queryParamEntries);

		return responseObject;
	}

	@Override
	public NonMandatoryParamsOperationResponseObject simpleEnumSetOperation(RequestContext ctx,
			Set<SimpleEnum> headerParam, Set<SimpleEnum> queryParam, TimeConstraints timeConstraints) throws SimpleException {

		ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
		int loopCounter;

		String[] headerParamsArray = new String[headerParam.size()];
		loopCounter = 0;
		for (SimpleEnum headerParamEntry : headerParam) {
			headerParamsArray[loopCounter] = headerParamEntry.toString();
			loopCounter++;
		}
		Arrays.sort(headerParamsArray, String.CASE_INSENSITIVE_ORDER);

		StringBuffer headerParamsBuff = new StringBuffer();
		for (loopCounter = 0; loopCounter < headerParamsArray.length; loopCounter++) {
			String entry = headerParamsArray[loopCounter] + ",";
			headerParamsBuff.append(entry);

		}
		String headerParamEntries = headerParamsBuff.toString();
		headerParamEntries = headerParamEntries.substring(0, headerParamEntries.length() - 1);

		String[] queryParamsArray = new String[queryParam.size()];
		loopCounter = 0;
		for (SimpleEnum queryParamEntry : queryParam) {
			queryParamsArray[loopCounter] = queryParamEntry.toString();
			loopCounter++;
		}
		Arrays.sort(queryParamsArray, String.CASE_INSENSITIVE_ORDER);

		StringBuffer queryParamsBuff = new StringBuffer();
		for (loopCounter = 0; loopCounter < queryParamsArray.length; loopCounter++) {
			String entry = queryParamsArray[loopCounter] + ",";
			queryParamsBuff.append(entry);

		}
		String queryParamEntries = queryParamsBuff.toString();
		queryParamEntries = queryParamEntries.substring(0, queryParamEntries.length() - 1);

		NonMandatoryParamsOperationResponseObject responseObject = new NonMandatoryParamsOperationResponseObject();
		responseObject.setHeaderParameter(headerParamEntries);
		responseObject.setQueryParameter(queryParamEntries);

		return responseObject;
	}

    @Override
    public CallSecurity checkSecurity(RequestContext ctx, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension("", null, null));
        CallSecurity ret = new CallSecurity();
        for (Identity id : ctx.getIdentity().getIdentities()) {
            if (id.getPrincipal().getName().contains(SSLAwareTokenResolver.SSL_CERT_INFO)) {
                ret.setClientSubject(id.getCredential().getValue().toString());
            }
        }
        ret.setSecurityStrengthFactor(ctx.getTransportSecurityStrengthFactor());
        return ret;
    }

    @Override
	public void voidResponseOperation(RequestContext ctx, String message, TimeConstraints timeConstraints)
			throws SimpleException {
		ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

	}

    @Override
    public EnumHandling enumHandling(RequestContext ctx, EnumHandling bodyParameter, Boolean returnUnknown, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        EnumHandling ret = new EnumHandling();
        if (returnUnknown) {
            ret.setParam1(ClientServerEnum.ServerOnly);
            ret.setParam2(EnumHandlingParam2Enum.ServerOnly);
        }
        else {
            ret.setParam1(ClientServerEnum.ClientServer);
            ret.setParam2(EnumHandlingParam2Enum.ClientServer);
        }
        return ret;
    }

    @Override
    public ClientServerEnum enumHandling2(RequestContext ctx, ClientServerEnum bodyParameter, Boolean returnUnknown, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        return returnUnknown ? ClientServerEnum.ServerOnly : ClientServerEnum.ClientServer;
    }

    @Override
    public EnumHandling3WrappedValueEnum enumHandling3(RequestContext ctx, EnumHandling3BodyParameterEnum bodyParameter, Boolean returnUnknown, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        return returnUnknown ? EnumHandling3WrappedValueEnum.ServerOnly : EnumHandling3WrappedValueEnum.ClientServer;
    }

    @Override
    public void callUnknownOperation(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        try {
            nonExistentServiceClient.someOperation(ctx);
        }
        // this won't hit as this call will return a not found..
        catch (com.betfair.nonservice.v3.exception.SimpleException e) {
            throw new SimpleException(SimpleExceptionErrorCodeEnum.valueOf(e.getErrorCode().name()),e.getReason());
        }
    }

    @Override
    public String echoRequestUuid(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null,null,null));
        return ctx.getRequestUUID().toString();
    }

    @Override
    public Boolean simpleEventPublication(RequestContext ctx, TimeContainer time, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(time, null, null));

        TimeTick tte = new TimeTick();
        tte.setTime(time);

        boolean success = false;
        try {
            //This causes the event to be published to the event transport
            timeTickPublishingObserver.onResult(new ExecutionResult(tte));
            success = true;
        } catch (Throwable ex) {
        	LOGGER.error("An exception occurred emitting the matched bet event:", ex);
        }
        return success;
    }

    @Override
    public void emitMatchedBet(RequestContext ctx, MatchedBetStruct bet, MarketStruct market, TimeConstraints timeConstraints) throws SimpleException {
        ctx.trace("Starting simple get for matched bet between accounts [ " + bet.getAccount1() + ", " + bet.getAccount2() + "]");

        ctx.setRequestLogExtension(new BaselineLogExtension(bet, null, null));

        MatchedBet matchedBet = new MatchedBet();
        MatchedBetContainer body = new MatchedBetContainer();
        matchedBet.setBody(body);
        body.setMatchedBet(bet);
        body.setMarket(market);

        try {
            matchedBetObserver.onResult(new ExecutionResult(matchedBet));
        } catch (Throwable ex) {
        	LOGGER.error("An exception occurred emitting the matched bet event:", ex);
        }
    }

    @Override
    public void emitLogMessage(RequestContext ctx, String logString, String logLevel, Long timeStamp, TimeConstraints timeConstraints) throws SimpleException {
        // Set the request log extension using the operation params
	    ctx.setRequestLogExtension(new BaselineLogExtension(logString, null, null));
	    // Construct an instance of the event to be emitted
	    LogMessage lm = new LogMessage();
        LogMessageContainer body = new LogMessageContainer();
        lm.setBody(body);
        // Add the neccessary parameters
	    body.setLogString(logString);
	    body.setLogLevel(logLevel);
	    body.setTimeStamp(timeStamp);
	    try {
            // Create a new global execution observer variable for each event and call OnResult() passing the event instance just created
	        logMessageObserver.onResult(new ExecutionResult(lm));
	    } catch (Throwable ex) {
	        LOGGER.error("An exception occurred emitting the inputted message event:", ex);
	    }
	}

    @Override
    public void emitListEvent(RequestContext ctx, List<String> messageList, TimeConstraints timeConstraints) throws SimpleException {
        StringBuilder builder = new StringBuilder();
        for(String message : messageList){
            builder.append(message);
        }
        String message = builder.toString();

        ctx.trace("Emitting message list [ " + message +" ]");
        ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

        ListEvent lEvent = new ListEvent();
        lEvent.setMessageList(messageList);

        try {
            listMessageObserver.onResult(new ExecutionResult(lEvent));
        } catch (Throwable ex) {
        	LOGGER.error("An exception occurred emitting the message list event:", ex);
        }
    }

    @Override
    public void emitSetEvent(RequestContext ctx, Set<String> messageSet, TimeConstraints timeConstraints) throws SimpleException {
        StringBuilder builder = new StringBuilder();
        for(String message : messageSet){
            builder.append(message);
        }
        String message = builder.toString();

        ctx.trace("Emitting message set [ " + message +" ]");
        ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

        SetEvent sEvent = new SetEvent();
        sEvent.setMessageSet(messageSet);

        try {
            setMessageObserver.onResult(new ExecutionResult(sEvent));
        } catch (Throwable ex) {
        	LOGGER.error("An exception occurred emitting the message set event:", ex);
        }
    }

    @Override
    public void emitMapEvent(RequestContext ctx, Map<String, String> messageMap, TimeConstraints timeConstraints) throws SimpleException {
        StringBuilder builder = new StringBuilder();
        for(String message : messageMap.values()){
            builder.append(message);
        }
        String message = builder.toString();

        ctx.trace("Emitting message map [ " + message +" ]");
        ctx.setRequestLogExtension(new BaselineLogExtension(message, null, null));

        MapEvent mEvent = new MapEvent();
        mEvent.setMessageMap(messageMap);

        try {
            mapMessageObserver.onResult(new ExecutionResult(mEvent));
        } catch (Throwable ex) {
        	LOGGER.error("An exception occurred emitting the message map event:", ex);
        }

    }

    @Override
    public Boolean boolSimpleTypeEcho(RequestContext ctx, Boolean msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Byte byteSimpleTypeEcho(RequestContext ctx, Byte msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Integer i32SimpleTypeEcho(RequestContext ctx, Integer msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Long i64SimpleTypeEcho(RequestContext ctx, Long msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Float floatSimpleTypeEcho(RequestContext ctx, Float msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Double doubleSimpleTypeEcho(RequestContext ctx, Double msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public String stringSimpleTypeEcho(RequestContext ctx, String msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Date dateTimeSimpleTypeEcho(RequestContext ctx, Date msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public List<Integer> i32ListSimpleTypeEcho(RequestContext ctx, List<Integer> msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Set<Integer> i32SetSimpleTypeEcho(RequestContext ctx, Set<Integer> msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public Map<Integer, Integer> i32MapSimpleTypeEcho(RequestContext ctx, Map<Integer, Integer> msg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(msg, null, null));
        return msg;
    }

    @Override
    public String getInferredCountryCode(RequestContext ctx, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        return ctx.getLocation().getInferredCountry();
    }

    @Override
    public void subscribeToOwnEvents(RequestContext ctx, List<String> events, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        for (final String s : events) {
            // ignore dup subs
            boolean subscribe = false;
            synchronized (subscriptionsBeingCreated) {
                if (!pubsubSubscriptions.containsKey(s) && !subscriptionsBeingCreated.contains(s)) {
                    subscriptionsBeingCreated.add(s);
                    subscribe = true;
                }
            }
            if (subscribe) {
                String subId = String.valueOf(new SecureRandom().nextInt());
                if ("TimeTick".equals(s)) {
                    baselineAsClient.subscribeToTimeTick(ctx, new Object[] {subId}, observer(s, timeTicks));
                }
                else if ("MatchedBet".equals(s)) {
                    baselineAsClient.subscribeToMatchedBet(ctx, new Object[] {subId}, observer(s, matchedBets));
                }
                else if ("LogMessage".equals(s)) {
                    baselineAsClient.subscribeToLogMessage(ctx, new Object[] {subId}, observer(s, logMessages));
                }
                else if ("ListEvent".equals(s)) {
                    baselineAsClient.subscribeToListEvent(ctx, new Object[] {subId}, observer(s, listEvents));
                }
                else if ("SetEvent".equals(s)) {
                    baselineAsClient.subscribeToSetEvent(ctx, new Object[] {subId}, observer(s, setEvents));
                }
                else if ("MapEvent".equals(s)) {
                    baselineAsClient.subscribeToMapEvent(ctx, new Object[] {subId}, observer(s, mapEvents));
                }
            }

        }
    }

    @Override
    public void emitLongEvent(RequestContext ctx, String eventNamespace, Long longArg, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(eventNamespace, longArg, null));
        ctx.getLocation().getRemoteAddr();
        ctx.getLocation().getResolvedAddresses();
        if (longEventNamespacedExecutionObserver.containsKey(eventNamespace)) {
            ExecutionObserver observer = longEventNamespacedExecutionObserver.get(eventNamespace);
            LongEvent longEvent = new LongEvent();
            longEvent.setLongArg(longArg);
            observer.onResult(new ExecutionResult(longEvent));
        } else {
            throw new SimpleException(ResponseCode.BadRequest,  SimpleExceptionErrorCodeEnum.GENERIC,
                    "Unknown event namespace: "+ eventNamespace);
        }
    }

    private EventTransportIdentity getEventTransportIdentity(ExecutionContext ctx) {
        List<EventTransportIdentity> transportIdentities = ctx.getIdentity().getIdentities(EventTransportIdentity.class);
        return transportIdentities.get(0);
    }

    private ExecutionObserver observer(final String eventName, final List events) {
        return new ExecutionObserver() {
            @Override
            public void onResult(ExecutionResult executionResult) {
                switch (executionResult.getResultType()) {
                    case Fault:
                        break;
                    case Subscription:
                        synchronized (subscriptionsBeingCreated) {
                            pubsubSubscriptions.put(eventName, executionResult.getSubscription());
                            subscriptionsBeingCreated.remove(eventName);
                        }
                        break;
                    case Success:
                        events.add(executionResult.getResult());
                }
            }
        };
    }

    @Override
    public void unsubscribeFromOwnEvents(RequestContext ctx, List<String> events, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        for (final String s : events) {
            synchronized (subscriptionsBeingCreated) {
                pubsubSubscriptions.remove(s).close();
            }
        }
    }

    @Override
    public List<ReceivedEvent> getReceivedEvents(RequestContext ctx, GetReceivedEventsEventEnum event, TimeConstraints timeConstraints) throws SimpleException {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        List<ReceivedEvent> ret = new ArrayList<ReceivedEvent>();
        if (event == GetReceivedEventsEventEnum.TimeTick) {
            Iterator<TimeTick> it = timeTicks.iterator();
            while (it.hasNext()) {
                TimeTick next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setTimeTick(next.getTime());
                ret.add(re);
                it.remove();
            }
            timeTicks.clear();
        }
        else if (event == GetReceivedEventsEventEnum.MatchedBet) {
            Iterator<MatchedBet> it = matchedBets.iterator();
            while (it.hasNext()) {
                MatchedBet next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setMatchedBet(next.getBody());
                ret.add(re);
                it.remove();
            }
            matchedBets.clear();
        }
        else if (event == GetReceivedEventsEventEnum.LogMessage) {
            Iterator<LogMessage> it = logMessages.iterator();
            while (it.hasNext()) {
                LogMessage next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setLogMessage(next.getBody());
                ret.add(re);
                it.remove();
            }
            logMessages.clear();
        }
        else if (event == GetReceivedEventsEventEnum.ListEvent) {
            Iterator<ListEvent> it = listEvents.iterator();
            while (it.hasNext()) {
                ListEvent next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setListEvent(next.getMessageList());
                ret.add(re);
                it.remove();
            }
            listEvents.clear();
        }
        else if (event == GetReceivedEventsEventEnum.SetEvent) {
            Iterator<SetEvent> it = setEvents.iterator();
            while (it.hasNext()) {
                SetEvent next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setSetEvent(next.getMessageSet());
                ret.add(re);
                it.remove();
            }
            setEvents.clear();
        }
        else if (event == GetReceivedEventsEventEnum.MapEvent) {
            Iterator<MapEvent> it = mapEvents.iterator();
            while (it.hasNext()) {
                MapEvent next = it.next();
                ReceivedEvent re = new ReceivedEvent();
                re.setEventName(ReceivedEventEventNameEnum.valueOf(event.name()));
                re.setMapEvent(next.getMessageMap());
                ret.add(re);
                it.remove();
            }
            mapEvents.clear();
        }
        return ret;
    }

    /**
     * Please note that this Service method is called by the Execution Venue to establish a communication
     * channel from the transport to the Application to publish events.  In essence, the transport subscribes
     * to the app, so this method is called once for each publisher.  The application should hold onto the
     * passed in observer, and call onResult on that observer to emit an event.
     * @param ctx
     * @param args
     * @param executionObserver
     */
    @Override
    public void subscribeToTimeTick(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver) {
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
            this.timeTickPublishingObserver = executionObserver;
        }
    }

    /**
     * Please note that this Service method is called by the Execution Venue to establish a communication
     * channel from the transport to the Application to publish events.  In essence, the transport subscribes
     * to the app, so this method is called once for each publisher at application initialisation.  You should
     * never be calling this directly.  The application should hold onto the passed in observer, and call
     * onResult on that observer to emit an event.
     * @param ctx
     * @param args
     * @param executionObserver
     */
    @Override
    public void subscribeToMatchedBet(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver) {
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
            this.matchedBetObserver = executionObserver;
        }
    }

    @Override
	public void subscribeToLogMessage(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver) {
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
	        this.logMessageObserver = executionObserver;
        }
	}

    @Override
    public void subscribeToListEvent(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver){
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
            this.listMessageObserver = executionObserver;
        }
    }

    @Override
    public void subscribeToSetEvent(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver){
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
            this.setMessageObserver = executionObserver;
        }
    }

    @Override
    public void subscribeToMapEvent(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver){
        if (getEventTransportIdentity(ctx).getPrincipal().getName().equals(SONIC_TRANSPORT_INSTANCE_ONE)) {
            this.mapMessageObserver = executionObserver;
        }
    }

    @Override
    public void subscribeToLongEvent(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver) {
        EventTransportIdentity eventTransportIdentity = getEventTransportIdentity(ctx);
        longEventNamespacedExecutionObserver.put(eventTransportIdentity.getTransportIdentifier(), executionObserver);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public void onCougarStart() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }


    private static final class BaselineMonitor extends OnDemandMonitor {
        private String name;
        private Status status;

        public BaselineMonitor(String name, Status status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Status checkStatus() {
            return status;
        }

        public void setStatus(Status status) {
        	this.status = status;
        }
    }

    @Override
    public void updateSimpleConnectedObject(RequestContext ctx, SimpleConnectedObject updatedObject, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        simpleConnectedObjectHeap.beginUpdate();
        try {
            SimpleConnectedObjectCO root = SimpleConnectedObjectServerCO.rootFrom(simpleConnectedObjectHeap);
            root.setId(updatedObject.getId());
            root.setMessage(updatedObject.getMessage());
        }
        finally {
            simpleConnectedObjectHeap.endUpdate();
        }
    }

    @Override
    public ConnectedResponse simpleConnectedObject(RequestContext ctx, TimeConstraints timeConstraints) {
        ctx.setConnectedObjectLogExtension(new BaselineLogExtension("a","b","c"));
        Subscription sub = createSub(simpleConnectedObjectHeap);
        return new ConnectedResponseImpl(simpleConnectedObjectHeap, sub);
    }

    @Override
    public void updateSimpleConnectedList(RequestContext ctx, List<SimpleConnectedObject> updatedObject, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        simpleConnectedListHeap.beginUpdate();
        try {
            HListComplex<SimpleConnectedObjectServerCO> root = SimpleConnectedObjectServerCO.rootFromAsList(simpleConnectedListHeap);
            ConnectedObjectTestingUtils.updateList(updatedObject, root, ConnectedObjectTestingUtils.simpleConnectedObjectConverter, ConnectedObjectTestingUtils.simpleConnectedObjectIdSource, ConnectedObjectTestingUtils.simpleConnectedObjectCOIdSource);
        } finally {
            simpleConnectedListHeap.endUpdate();
        }
    }

    @Override
    public void appendSimpleConnectedObject(RequestContext ctx, SimpleConnectedObject object, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        simpleConnectedListHeap.beginUpdate();
        try {
            HListComplex<SimpleConnectedObjectServerCO> root = SimpleConnectedObjectServerCO.rootFromAsList(simpleConnectedListHeap);
            SimpleConnectedObjectCO newObject = root.addLast();
            newObject.setId(object.getId());
            newObject.setMessage(object.getMessage());
        } finally {
            simpleConnectedListHeap.endUpdate();
        }
    }

    @Override
    public ConnectedResponse simpleConnectedList(RequestContext ctx, TimeConstraints timeConstraints) {
        ctx.setConnectedObjectLogExtension(new BaselineLogExtension("d","e","f"));
        Subscription sub = createSub(simpleConnectedListHeap);
        return new ConnectedResponseImpl(simpleConnectedListHeap, sub);
    }

    @Override
    public void updateComplexConnectedObject(RequestContext ctx, VeryComplexObject updatedObject, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        complexConnectedObjectHeap.beginUpdate();
        try {
            VeryComplexObjectCO root = VeryComplexObjectServerCO.rootFrom(complexConnectedObjectHeap);
            root.setEnumParameter(updatedObject.getEnumParameter() != null ? updatedObject.getEnumParameter().getCode() : null);
            ConnectedObjectTestingUtils.updateList(updatedObject.getList(), root.getList(), ConnectedObjectTestingUtils.lessComplexObjectConverter, ConnectedObjectTestingUtils.lessComplexObjectIdSource, ConnectedObjectTestingUtils.lessComplexObjectProjectionIdSource);
            ConnectedObjectTestingUtils.updateMap(updatedObject.getMap(), root.getMap(), ConnectedObjectTestingUtils.lessComplexObjectConverter);
            ConnectedObjectTestingUtils.updateList(updatedObject.getSet(), root.getSet(), ConnectedObjectTestingUtils.lessComplexObjectConverter, ConnectedObjectTestingUtils.lessComplexObjectIdSource, ConnectedObjectTestingUtils.lessComplexObjectProjectionIdSource);
        }
        finally {
            complexConnectedObjectHeap.endUpdate();
        }
    }

    @Override
    public ConnectedResponse complexConnectedObject(RequestContext ctx, TimeConstraints timeConstraints) {
        ctx.setConnectedObjectLogExtension(new BaselineLogExtension("g","h","i"));
        Subscription sub = createSub(complexConnectedObjectHeap);
        return new ConnectedResponseImpl(complexConnectedObjectHeap, sub);
    }

    @Override
    public void closeAllSubscriptions(RequestContext ctx, String heapUri, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        List<Subscription> heapSubs = heapSubscriptions.remove(heapUri);
        if (heapSubs != null) {
            for (Subscription sub : new ArrayList<Subscription>(heapSubs)) { // avoid concurrent mods
                sub.close(Subscription.CloseReason.REQUESTED_BY_PUBLISHER);
            }
        }
    }

    @Override
    public Integer getNumSubscriptions(RequestContext ctx, String heapUri, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        List<Subscription> heapSubs = heapSubscriptions.get(heapUri);
        if (heapSubs != null) {
            return heapSubs.size();
        }
        return 0;
    }



    @Override
    public TestResults testConnectedObjects(RequestContext ctx, TestConnectedObjectsProtocolEnum protocol, TimeConstraints timeConstraints) {
        ctx.setRequestLogExtension(new BaselineLogExtension(null, null, null));
        BaselineSyncClient client = null;
        if (protocol == TestConnectedObjectsProtocolEnum.IN_PROCESS) {
            client = inProcessSyncClient;
        }
        else if (protocol == TestConnectedObjectsProtocolEnum.SOCKET) {
            client = socketSyncClient;
        }
        else {
            // implement other protocols when we have the transports
            throw new CougarServiceException(ServerFaultCode.FrameworkError, "Unsupported protocol: "+protocol);
        }

        return ConnectedObjectTestingUtils.testConnectedObjects(ctx, client, protocol);
    }

    private Subscription createSub(Heap heap) {
        CountingSharedSubscription sub = new CountingSharedSubscription();
        List<Subscription> subs = heapSubscriptions.get(heap.getUri());
        if (subs == null) {
            subs = new ArrayList<Subscription>();
            heapSubscriptions.put(heap.getUri(), subs);
        }
        subs.add(sub);
        final List<Subscription> finalSubs = subs;
        sub.addListener(new Subscription.SubscriptionListener() {
            @Override
            public void subscriptionClosed(Subscription subscription, Subscription.CloseReason reason) {
                finalSubs.remove(subscription);
            }
        });
        return sub;
    }

    private static AtomicLong liveSubs = new AtomicLong();

    // an example of how someone might return a shared sub object to count subs in and out
    private class CountingSharedSubscription extends DefaultSubscription {

        public CountingSharedSubscription() {
            liveSubs.incrementAndGet();
        }

        @Override
        public void postClose(CloseReason reason) {
            liveSubs.decrementAndGet();
        }
    }
}
