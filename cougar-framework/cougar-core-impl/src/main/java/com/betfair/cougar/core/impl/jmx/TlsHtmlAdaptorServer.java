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

package com.betfair.cougar.core.impl.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jdmk.comm.CommunicationException;
import com.sun.jdmk.comm.HtmlAdaptorServer;
import com.sun.jdmk.comm.JdmkHtmlRequestHandler;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;

/**
 * Extends HtmlAdaptorServer to add TLS support
 */
public class TlsHtmlAdaptorServer extends HtmlAdaptorServer {

	private static final int SOCKET_TIMEOUT = 10 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(TlsHtmlAdaptorServer.class);

    private final Resource keystoreResource;
    private final String keystorePasswd;
    private final String keystoreType;
    private final String certPasswd;
	private ServerSocket serverSocket;
	private Socket socket;
    private final boolean tlsEnabled;
    private final boolean reuseAddress;
    private InetAddress lastClientAddress;

    public TlsHtmlAdaptorServer(final Resource keystoreResource, final String keystorePasswd,
                                final String certPasswd, final int port, final String keystoreType, final boolean enabled, boolean reuseAddress) {
        this.keystoreResource = keystoreResource;
        this.keystorePasswd = keystorePasswd;
        this.certPasswd = certPasswd;
        this.keystoreType = keystoreType;
        this.tlsEnabled = enabled;
        this.reuseAddress = reuseAddress;
        setPort(port);
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    @Override
    protected void doBind() throws CommunicationException, InterruptedException {
        if (tlsEnabled) {
            final KeyStore keyStore;
            try {
                keyStore = loadKeyStore(keystoreResource, keystorePasswd, keystoreType);
            } catch (Exception e) {
                logger.error("",e);
                throw new RuntimeException("Can't load keystore from " + keystoreResource, e);
            }

            try {
                serverSocket = createSecureServerSocket(keyStore, certPasswd);
            } catch (Exception e) {
                logger.error("",e);
                throw new RuntimeException("Error while creating server socket", e);
            }
        }
        else {
        	try {
        		serverSocket = createServerSocket();
        	} catch (Exception e) {
                logger.error("",e);
                throw new RuntimeException("Error while creating server socket", e);
            }
        }

        logger.info("Created ServerSocket " + serverSocket);
    }

	@Override
    protected void doReceive() throws CommunicationException,
    		InterruptedException {
        try {
            this.socket = this.serverSocket.accept();
            this.socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (IOException e) {
            logger.error("",e);
            throw new CommunicationException(e, "Error while accepting connection on server socket");
        }
    }

    @Override
    protected void doProcess() throws CommunicationException ,InterruptedException {
    	this.lastClientAddress = this.socket.getInetAddress();

        new JdmkHtmlRequestHandler(this.socket, this ,getMBeanServer(), getServedClientCount());

        this.socket = null;
    }

    @Override
    protected void doUnbind() throws CommunicationException,
    		InterruptedException {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            logger.error("",e);
            throw new CommunicationException(e,"Error while closing socket");
        }
    }

    protected ServerSocket createSecureServerSocket(final KeyStore keyStore, final String passwd)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, IOException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, passwd.toCharArray());

        final SSLContext sslcontext = SSLContext.getInstance("SSLv3");
        sslcontext.init(keyManagerFactory.getKeyManagers(), null, null);

        ServerSocket ss = sslcontext.getServerSocketFactory().createServerSocket();
        return bindSocket(ss);
    }


    protected KeyStore loadKeyStore(final Resource keystoreResource, final String passwd, final String type)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance(type);

        InputStream keyStoreStream = null;
        try {
        	keyStoreStream = keystoreResource.getInputStream();
        	keyStore.load(keyStoreStream, passwd.toCharArray());
        	return keyStore;
        } finally {
        	if(keyStoreStream != null)
        		keyStoreStream.close();
        }
    }

    private ServerSocket createServerSocket() throws IOException {
		ServerSocket ss =  new ServerSocket();
        return bindSocket(ss);
	}


    private ServerSocket bindSocket(ServerSocket socket) throws IOException {
        if (reuseAddress) {
            socket.setReuseAddress(true);
        }
        socket.bind(new InetSocketAddress(getPort()), 2 * getMaxActiveClientCount());
        return socket;
    }

    public String getLastConnectedClient() {
        if (this.lastClientAddress == null) {
            return "unknown";
        }
        return this.lastClientAddress.getHostAddress();
    }


}
