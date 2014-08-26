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

package com.betfair.cougar.client.socket.jmx;

import com.betfair.cougar.client.socket.ClientConnectedObjectManager;
import com.betfair.cougar.client.socket.ClientSubscription;
import com.betfair.cougar.client.socket.HeapState;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.jmx.JMXHttpParser;
import com.betfair.cougar.core.api.jmx.JMXHttpParserReader;
import com.betfair.cougar.netutil.nio.HandlerListener;
import com.betfair.cougar.netutil.nio.NioUtils;
import org.apache.mina.common.IoSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ClientSocketTransportInfo implements JMXHttpParser, HandlerListener, ApplicationContextAware, InitializingBean {

    private Map<String, IoSession> sessions = new ConcurrentHashMap<String, IoSession>();
    private ClientConnectedObjectManager connectedObjectManager;
    private ApplicationContext applicationContext;
    private String jmxHttpParserReaderBeanName;

    public ClientSocketTransportInfo(String jmxHttpParserReaderBeanName, ClientConnectedObjectManager connectedObjectManager) {
        // irritatingly, this has to be passed as a string in case we're sat outside a cougar server, in which case the bean won't exist..
        this.jmxHttpParserReaderBeanName = jmxHttpParserReaderBeanName;
        this.connectedObjectManager = connectedObjectManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            JMXHttpParserReader reader = (JMXHttpParserReader) applicationContext.getBean(jmxHttpParserReaderBeanName);
            reader.addCustomParser(this);
        }
        catch (Exception e) {
            // ignore, probably means that we're not running in a cougar server
        }
    }

    @Override
    public void sessionOpened(IoSession session) {
        sessions.put(NioUtils.getSessionId(session), session);
    }

    @Override
    public void sessionClosed(IoSession session) {
        sessions.remove(NioUtils.getSessionId(session));
    }

    @Override
    public String getPath() {
        return "socketTransportClient.jsp";
    }

    @Override
    public String process(Map<String, String> params) {
        String sessionIdToBreakDown = params.get("sessionId");
        String heapIdToBreakDownString = params.get("heapId");
        Long heapIdToBreakDown = null;
        if (heapIdToBreakDownString != null) {
            try {
                heapIdToBreakDown = Long.parseLong(heapIdToBreakDownString);
            }
            catch (NumberFormatException nfe) {
                // ignore
            }
        }
        boolean showDetailedSessionBreakdown = "true".equals(params.get("detailedSession"));
        boolean showDetailedHeapBreakdown = "true".equals(params.get("detailedHeap"));
        boolean showKillLinks = "true".equals(params.get("killLinks"));
        String subscriptionIdToKill = params.get("subscriptionIdToKill");
        if (showKillLinks && subscriptionIdToKill != null) {
            IoSession session = sessions.get(sessionIdToBreakDown);
            if (session != null && heapIdToBreakDown != null) {
                connectedObjectManager.terminateSubscription(session, heapIdToBreakDown, subscriptionIdToKill, Subscription.CloseReason.REQUESTED_BY_SUBSCRIBER_ADMINISTRATOR);
            }
        }

        final String queryString = getQueryString(params, "subscriptionIdToKill");

        StringBuilder result = new StringBuilder();

        result.append("<!DOCTYPE html>\n");
        result.append("<html><body>\n");

        if (!showKillLinks) {
            result.append("<p align='center'><a href='").append(getPath()).append(queryString).append("&killLinks=true'>Show kill links</a></p>");
        }
        else {
            result.append("<p align='center'><a href='").append(getPath()).append(queryString).append("&killLinks=false'>Hide kill links</a></p>");
        }

        result.append("<hr width='100%'/>\n");

        // --------------------------- Connection orientated view --------------------------------------

        result.append("<h3>Connection Breakdown</h3>\n");
        result.append("<table border='1'><tr><th>Session id</th><th>Remote address</th><th>Heap count</th><th>Read queue depth</th><th></th></tr>\n");
        // show a list of connections, by type (incoming/outgoing)
        // for each connection, show write queue depth
        for (IoSession session : sessions.values()) {
            String sessionId = NioUtils.getSessionId(session);
            ClientConnectedObjectManager.ConnectedHeaps heaps = connectedObjectManager.getHeapsForSession(session);
            int heapCount = heaps != null ? heaps.getHeapCount() : 0;
            long queueLength = heaps != null ? heaps.getQueueLength() : 0;

            result.append("<tr><td>").append(sessionId).append("</td><td>").append(session.getRemoteAddress()).append("</td><td>").append(heapCount).append("</td><td>").append(queueLength).append("</td>");
            if (heapCount != 0) {
                result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionId)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("'>Show connected objects</a></td>");
            }
            result.append("</tr>\n");

        }
        result.append("</table>\n");

        // for each connection, show connected objects that are live
        IoSession session = sessionIdToBreakDown != null ? sessions.get(sessionIdToBreakDown) : null;
        if (session != null) {
            ClientConnectedObjectManager.ConnectedHeaps heaps = connectedObjectManager.getHeapsForSession(session);
            int heapCount = heaps != null ? heaps.getHeapCount() : 0;
            if (heapCount != 0) {
                List<Long> heapIds = heaps.getAllHeapIds();
                result.append("<hr width='100%'/>\n<h3>Connected object breakdown for connection '").append(sessionIdToBreakDown).append("'</h3>\n");
                String heapIdString = heapIdToBreakDown != null ? "&heapId="+heapIdToBreakDown : "";

                if (showDetailedSessionBreakdown) {
                    result.append("<p><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=false").append(heapIdString).append("&killLinks=").append(showKillLinks).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("'>Show summary view</a></p>\n");
                }
                else {
                    result.append("<p><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=true").append(heapIdString).append("&killLinks=").append(showKillLinks).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("'>Show detailed view</a></p>\n");
                }

                Collections.sort(heapIds);
                result.append("<table border='1'><tr><th>Heap URI</th>");
                if (showDetailedSessionBreakdown) {
                    result.append("<th>Last delta received</th><th>Num subscribers</th>");
                }
                result.append("<th></th></tr>\n");
                for (Long id : heapIds) {
                    HeapState heapState = heaps.getHeapState(id);
                    if (heapState != null) {
                    result.append("<tr><td>").append(heapState.getHeapUri()).append("</td>");
                        // for each connected object, show last delta sent/received (id), show number of subscribers
                        if (showDetailedSessionBreakdown) {
                            // add last delta sent/num subscribers
                            result.append("<td>").append(heapState.getLastDeltaId()).append("</td><td>").append(heapState.getSubscriptionCount()).append("</td>");
                        }
                        // add link to connected object detail
                        if (id.equals(heapIdToBreakDown)) {
                            result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("'>Hide breakdown</a></td>");
                        }
                        else {
                            result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("&heapId=").append(id).append("'>Show breakdown</a></td>");
                        }
                        result.append("</tr>\n");
                    }
                }
                result.append("</table>");
            }
        }

        // --------------------------- Subscriber list view --------------------------------------

        // show list of subscribers if requested
        if (session != null && heapIdToBreakDown != null) {
            ClientConnectedObjectManager.ConnectedHeaps heaps = connectedObjectManager.getHeapsForSession(session);
            HeapState heapState = heaps != null ? heaps.getHeapState(heapIdToBreakDown) : null;
            if (heapState != null) {
                result.append("<hr width='100%'/>\n<h3>Subscriber breakdown for heap '").append(heapState.getHeapUri()).append("' on connection '").append(sessionIdToBreakDown).append("'</h3>\n");

                result.append("<table border='1' width='100%' align='center'><tr><th>Subscription Id</th><th></th></tr>\n");
                List<ClientSubscription> subs = new ArrayList<ClientSubscription>(heapState.getSubscriptions().values());
                if (subs != null) {
                    for (ClientSubscription sub : subs) {
                        result.append("<tr><td>").append(sub.getSubscriptionId()).append("</td>");
                        if (showKillLinks) {
                            result.append("<td><a href='").append(getPath()).append(queryString).append("&subscriptionIdToKill=").append(sub.getSubscriptionId()).append("'>Kill</a></td>");
                        }
                        result.append("</tr>\n");
                    }
                }
                else {
                    result.append("<tr><td></td></tr>\n");
                }
            }
            result.append("</table>\n");
        }
        result.append("</body></html>\n");

        return result.toString();
    }

    private String getQueryString(Map<String, String> params, String... excludeParams) {
        List<String> exclusions = Arrays.asList(excludeParams);
        StringBuilder sb = new StringBuilder();
        String sep = "?";
        for (String key : params.keySet()) {
            if (!exclusions.contains(key)) {
                sb.append(sep).append(key).append("=").append(URLEncoder.encode(params.get(key)));
                sep = "&";
            }
        }
        return sb.toString();
    }
}
