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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.netutil.nio.monitoring.SessionWriteQueueMonitoring;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.common.support.BaseIoServiceConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;
import java.net.InetSocketAddress;

@ManagedResource
public class NioConfig {

    private String listenAddress;
    private int listenPort;
    private Boolean keepAlive;
    private Integer sendBufferSize;
    private Integer recvBufferSize;
    private Boolean tcpNoDelay;
    private Boolean reuseAddress;

    private NioLogger nioLogger;

    private int keepAliveTimeout = 10;
    private int keepAliveInterval = 5;
    private int workerTimeout = 60;
    private long maxWriteQueueSize = 0;
    private boolean useDirectBuffersInMina = false;
    private long rpcTimeoutGranularityMillis = 100;
    private long rpcTimeoutMillis = 0; // 0 means disabled

    public NioConfig() {
    }

    public InetSocketAddress getServerSocketAddress() {
        return new InetSocketAddress(listenAddress, listenPort);
    }

    public void configureSocketAcceptorConfig(SocketAcceptorConfig config) throws IOException {
        if (reuseAddress != null) {
            config.setReuseAddress(reuseAddress);
        }
        configureSocketSessionConfig(config.getSessionConfig());
        configureProtocol(config, true);
    }

    private void configureSocketSessionConfig(SocketSessionConfig config) {
        if (keepAlive != null) {
            config.setKeepAlive(keepAlive);
        }
        if (sendBufferSize != null) {
            config.setSendBufferSize(sendBufferSize);
        }

        if (recvBufferSize != null) {
            config.setReceiveBufferSize(recvBufferSize);
        }

        if (tcpNoDelay != null) {
            config.setTcpNoDelay(tcpNoDelay);
        }

        if (reuseAddress != null) {
            config.setReuseAddress(reuseAddress);
        }
    }


    protected void configureProtocol(BaseIoServiceConfig config, boolean isServer) throws IOException {

        ByteBuffer.setUseDirectBuffers(useDirectBuffersInMina);

        config.getFilterChain().addLast("slowHandling", new SessionWriteQueueMonitoring(nioLogger, maxWriteQueueSize));
        config.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new CougarProtocolEncoder(nioLogger), new CougarProtocolDecoder(nioLogger)));
        if (isServer) {
            config.getFilterChain().addLast("protocol", CougarProtocol.getServerInstance(nioLogger, keepAliveInterval, keepAliveTimeout, null, false, false));
        }
        else {
            config.getFilterChain().addLast("protocol", CougarProtocol.getClientInstance(nioLogger, keepAliveInterval, keepAliveTimeout, null, false, false, rpcTimeoutMillis));
        }

        config.setThreadModel(ThreadModel.MANUAL);
    }


    public synchronized SocketConnectorConfig configureSocketSessionConfig() throws IOException {
        SocketConnectorConfig config = new SocketConnectorConfig();
        configureSocketSessionConfig(config.getSessionConfig());
        configureProtocol(config, false);
        return config;
    }

    @ManagedAttribute
    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    @ManagedAttribute
    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    @ManagedAttribute
    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }


    @ManagedAttribute
    public Integer getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    @ManagedAttribute
    public Integer getRecvBufferSize() {
        return recvBufferSize;
    }

    public void setRecvBufferSize(Integer recvBufferSize) {
        this.recvBufferSize = recvBufferSize;
    }

    @ManagedAttribute
    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    @ManagedAttribute
    public Boolean getReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(Boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @ManagedAttribute
    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    @ManagedAttribute
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    @ManagedAttribute
    public long getMaxWriteQueueSize() {
        return maxWriteQueueSize;
    }

    public void setMaxWriteQueueSize(long maxWriteQueueSize) {
        this.maxWriteQueueSize = maxWriteQueueSize;
    }

    @ManagedAttribute
    public boolean isUseDirectBuffersInMina() {
        return useDirectBuffersInMina;
    }

    public void setUseDirectBuffersInMina(boolean useDirectBuffersInMina) {
        this.useDirectBuffersInMina = useDirectBuffersInMina;
    }

    public void setNioLogger(NioLogger nioLogger) {
        this.nioLogger = nioLogger;
    }

    public NioLogger getNioLogger() {
        return nioLogger;
    }

    public void setWorkerTimeout(int workerTimeout) {
        this.workerTimeout = workerTimeout;
    }

    @ManagedAttribute
    public int getWorkerTimeout() {
        return workerTimeout;
    }

    @ManagedAttribute
    public long getRpcTimeoutGranularityMillis() {
        return rpcTimeoutGranularityMillis;
    }

    @ManagedAttribute
    public long getRpcTimeoutMillis() {
        return rpcTimeoutMillis;
    }

    public void setRpcTimeoutGranularityMillis(long rpcTimeoutGranularityMillis) {
        this.rpcTimeoutGranularityMillis = rpcTimeoutGranularityMillis;
    }

    public void setRpcTimeoutMillis(long rpcTimeoutMillis) {
        this.rpcTimeoutMillis = rpcTimeoutMillis;
    }
}
