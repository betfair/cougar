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

package com.betfair.cougar.transport.socket.jmx;

import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.jmx.JMXHttpParser;
import com.betfair.cougar.core.api.jmx.JMXHttpParserReader;
import com.betfair.cougar.netutil.nio.NioUtils;
import com.betfair.cougar.netutil.nio.monitoring.SessionWriteQueueMonitor;
import com.betfair.cougar.netutil.nio.monitoring.SessionWriteQueueMonitoring;
import com.betfair.cougar.netutil.nio.HandlerListener;
import com.betfair.cougar.transport.socket.PooledServerConnectedObjectManager;
import org.apache.mina.common.IoSession;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exposes useful socket server internal details for debugging at
 * http://HOST:9999/administration/socketTransportServer.jsp
 */
public class ServerSocketTransportInfo implements JMXHttpParser, HandlerListener {

    private Map<String, IoSession> sessions = new ConcurrentHashMap<String, IoSession>();
    private PooledServerConnectedObjectManager connectedObjectManager;

    public ServerSocketTransportInfo(JMXHttpParserReader reader, PooledServerConnectedObjectManager connectedObjectManager) {
        reader.addCustomParser(this);
        this.connectedObjectManager = connectedObjectManager;
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
        return "socketTransportServer.jsp";
    }

    @Override
    public String process(Map<String, String> params) {
        String sessionIdToBreakDown = params.get("sessionId");
        String heapUriToBreakDown = params.get("heapUri");
        boolean showDetailedSessionBreakdown = "true".equals(params.get("detailedSession"));
        boolean showDetailedHeapBreakdown = "true".equals(params.get("detailedHeap"));
        boolean showKillLinks = "true".equals(params.get("killLinks"));
        String subscriptionIdToKill = params.get("subscriptionIdToKill");
        if (showKillLinks && subscriptionIdToKill != null) {
            IoSession session = sessions.get(sessionIdToBreakDown);
            if (session != null && heapUriToBreakDown != null) {
                connectedObjectManager.terminateSubscription(session, heapUriToBreakDown, subscriptionIdToKill, Subscription.CloseReason.REQUESTED_BY_PUBLISHER_ADMINISTRATOR);
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

        result.append("<hr width='100%'/>\n<table border='1'><tr><td width='50%' valign='top'>\n");

        // --------------------------- Connection orientated view --------------------------------------

        result.append("<h3>Connection Breakdown</h3>\n");
        result.append("<table border='1'><tr><th>Session id</th><th>Remote address</th><th>Heap count</th><th>Write queue depth</th><th></th></tr>\n");
        // show a list of connections, by type (incoming/outgoing)
        // for each connection, show write queue depth
        for (IoSession session : sessions.values()) {
            String sessionId = NioUtils.getSessionId(session);
            SessionWriteQueueMonitor queueMonitor = SessionWriteQueueMonitoring.getSessionMonitor(sessionId);
            String queueLength = queueMonitor != null ? String.valueOf(queueMonitor.getQueueDepth()) : "N/A";
            List<String> heapUris = connectedObjectManager.getHeapsForSession(session);
            int heapCount = heapUris != null ? heapUris.size() : 0;

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
            List<String> heapUris = connectedObjectManager.getHeapsForSession(session);
            int heapCount = heapUris != null ? heapUris.size() : 0;
            if (heapCount != 0) {
                result.append("<hr width='100%'/>\n<h3>Connected object breakdown for connection '").append(sessionIdToBreakDown).append("'</h3>\n");
                String heapUriString = heapUriToBreakDown != null ? "&heapUri="+heapUriToBreakDown : "";

                if (showDetailedSessionBreakdown) {
                    result.append("<p><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=false").append(heapUriString).append("&killLinks=").append(showKillLinks).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("'>Show summary view</a></p>\n");
                }
                else {
                    result.append("<p><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=true").append(heapUriString).append("&killLinks=").append(showKillLinks).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("'>Show detailed view</a></p>\n");
                }

                Collections.sort(heapUris);
                result.append("<table border='1'><tr><th>Heap URI</th>");
                if (showDetailedSessionBreakdown) {
                    result.append("<th>Last delta sent</th><th>Num subscribers</th>");
                }
                result.append("<th></th></tr>\n");
                for (String s : heapUris) {
                    result.append("<tr><td>").append(s).append("</td>");
                    // for each connected object, show last delta sent/received (id), show number of subscribers
                    if (showDetailedSessionBreakdown) {
                        // add last delta sent/num subscribers
                        PooledServerConnectedObjectManager.HeapStateMonitoring heapState = connectedObjectManager.getHeapStateForMonitoring(s);
                        if (heapState != null) {
                            result.append("<td>").append(heapState.getLastUpdateId()).append("</td><td>").append(heapState.getSubscriptionCount()).append("</td>");
                        }
                        else {
                            result.append("<td>N/A</td><td>N/A</td>");
                        }
                    }
                    // add link to connected object detail
                    if (s.equals(heapUriToBreakDown)) {
                        result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("'>Hide breakdown</a></td>");
                    }
                    else {
                        result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionIdToBreakDown)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("&heapUri=").append(s).append("'>Show breakdown</a></td>");
                    }
                    result.append("</tr>\n");
                }
                result.append("</table>");
            }
        }
        result.append("</td><td width='0' valign='top'>\n");

        // --------------------------- Heap orientated view --------------------------------------

        result.append("<h3>Heap breakdown</h3>\n");
        // for each connected object, show list of connections
        Set<String> heapUris = null;
        Collection<String> heapUrisCollection = connectedObjectManager.getHeapUris().values();
        if (heapUrisCollection != null) {
            heapUris = new TreeSet<String>(heapUrisCollection);
        }
        if (heapUris != null) {
            result.append("<table border='1'><tr><th>Heap</th><th>Connection count</th><th>Last delta sent</th><th>Num subscribers</th><th></th></tr>\n");

            for (String heapUri : heapUris) {
                PooledServerConnectedObjectManager.HeapStateMonitoring heapState = connectedObjectManager.getHeapStateForMonitoring(heapUri);
                if (heapState != null) {
                    result.append("<tr><td>").append(heapUri).append("</td><td>").append(heapState.getSessionCount()).append("</td><td>").append(heapState.getLastUpdateId()).append("</td><td>").append(heapState.getSubscriptionCount()).append("</td>");
                    if (heapState.getSessionCount() != 0) {
                        result.append("<td><a href='").append(getPath()).append("?heapUri=").append(URLEncoder.encode(heapUri)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("'>Show connections</a></td>");
                    }
                    result.append("</tr>\n");
                }
            }
            result.append("</table>");
        }

        PooledServerConnectedObjectManager.HeapStateMonitoring heapState = heapUriToBreakDown != null ? connectedObjectManager.getHeapStateForMonitoring(heapUriToBreakDown) : null;
        if (heapState != null) {
            result.append("<hr width='100%'/>\n<h3>Connection breakdown for connected object '").append(heapUriToBreakDown).append("'</h3>\n");

            // Show/hide detailed view

            String sessionIdString = sessionIdToBreakDown != null ? "&sessionId="+URLEncoder.encode(sessionIdToBreakDown) : "";
            if (showDetailedHeapBreakdown) {
                result.append("<p><a href='").append(getPath()).append("?heapUri=").append(URLEncoder.encode(heapUriToBreakDown)).append(sessionIdString).append("&detailedHeap=false").append(sessionIdString).append("&killLinks=").append(showKillLinks).append("&detailedSession=").append(showDetailedSessionBreakdown).append("'>Show summary view</a></p>\n");
            }
            else {
                result.append("<p><a href='").append(getPath()).append("?heapUri=").append(URLEncoder.encode(heapUriToBreakDown)).append(sessionIdString).append("&detailedHeap=true").append(sessionIdString).append("&killLinks=").append(showKillLinks).append("&detailedSession=").append(showDetailedSessionBreakdown).append("'>Show detailed view</a></p>\n");
            }

            SortedMap<String, List<String>> subscriptionIdsBySessionId = heapState.getSubscriptionIdsBySessionId();
            if (subscriptionIdsBySessionId != null) {
                result.append("<table border='1'><tr><th>Session Id</th>");
                if (showDetailedHeapBreakdown) {
                    result.append("<th>Remote Address</th><th>Write Queue Depth</th>");
                }
                result.append("<th></th></tr>\n");
                for (String sessionId  :subscriptionIdsBySessionId.keySet()) {
                    IoSession sessionForSessionId = sessions.get(sessionId);
                    result.append("<tr><td>").append(sessionId).append("</td>");
                    if (showDetailedHeapBreakdown) {
                        SessionWriteQueueMonitor queueMonitor = SessionWriteQueueMonitoring.getSessionMonitor(sessionId);
                        String queueLength = queueMonitor != null ? String.valueOf(queueMonitor.getQueueDepth()) : "N/A";
                        result.append("<td>").append(sessionForSessionId.getRemoteAddress()).append("</td><td>").append(queueLength).append("</td>");
                    }
                    if (sessionId.equals(sessionIdToBreakDown)) {
                        result.append("<td><a href='").append(getPath()).append("?detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("&heapUri=").append(heapUriToBreakDown).append("'>Hide breakdown</a></td>");
                    }
                    else {
                        result.append("<td><a href='").append(getPath()).append("?sessionId=").append(URLEncoder.encode(sessionId)).append("&detailedSession=").append(showDetailedSessionBreakdown).append("&detailedHeap=").append(showDetailedHeapBreakdown).append("&killLinks=").append(showKillLinks).append("&heapUri=").append(heapUriToBreakDown).append("'>Show breakdown</a></td>");
                    }
                    result.append("</tr>\n");
                }
                result.append("</table>\n");
            }

        }
        result.append("</td></tr></table>\n");

        // --------------------------- Subscriber list view --------------------------------------

        // show list of subscribers if requested
        if (heapState != null && session != null) {
            result.append("<hr width='100%'/>\n<h3>Subscriber breakdown for heap '").append(heapUriToBreakDown).append("' on connection '").append(sessionIdToBreakDown).append("'</h3>\n");

            result.append("<table border='1' width='100%' align='center'><tr><th>Connection</th><th>Subscription Count</th><th>Subscription Id</th><th></th></tr>\n");
            SortedMap<String, List<String>> subscriptionIdsBySessionId = heapState.getSubscriptionIdsBySessionId();
            List<String> subscriptionIds = subscriptionIdsBySessionId.get(sessionIdToBreakDown);
            int numSubs = subscriptionIds != null ? subscriptionIds.size() : 0;
            String prefix = "<tr><td>"+sessionIdToBreakDown+"</td><td>"+numSubs+"</td><td>";
            if (subscriptionIds != null) {
                for (String subscriptionId : subscriptionIds) {
                    result.append(prefix).append(subscriptionId).append("</td>");
                    if (showKillLinks) {
                        result.append("<td><a href='").append(getPath()).append(queryString).append("&subscriptionIdToKill=").append(subscriptionId).append("'>Kill</a></td>");
                    }
                    result.append("</tr>\n");
                    prefix = "<tr><td></td><td></td><td>";
                }
            }
            else {
                result.append(prefix).append("</td></tr>\n");
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
