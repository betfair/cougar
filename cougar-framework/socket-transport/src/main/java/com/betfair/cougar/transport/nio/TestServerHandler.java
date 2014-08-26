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

package com.betfair.cougar.transport.nio;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.socket.SocketTransportCommand;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import com.betfair.cougar.netutil.nio.NioConfig;

public class TestServerHandler extends ExecutionVenueServerHandler {

    public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
		System.out.println("TestServerHandler.sessionCreated " + Thread.currentThread());
	}

	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		System.out.println("TestServerHandler.sessionOpened " + Thread.currentThread());

	}

	public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		t.printStackTrace();
		session.close();
	}

	public void messageReceived(IoSession session, Object msg) throws Exception {
		DataInputStream dis = new DataInputStream((InputStream) msg);//NOSONAR
		int x = dis.readInt();
		System.out.println("TestServerHandler got: " + x + ". Thread " + Thread.currentThread());

		System.out.println("TestServerHandler responding...");
		write(session, x + 1);
	}

	private void write(IoSession session, int msg) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new DataOutputStream(baos).writeInt(msg + 1);
		session.write(baos);
	}

	public static void main(String[] args) throws Exception {

		System.err.println("runServer...");
		TlsNioConfig cfg = new TlsNioConfig();
        cfg.setNioLogger(new NioLogger("ALL"));

		cfg.setListenAddress("127.0.0.1");
		cfg.setListenPort(2222);

		cfg.setReuseAddress(true);
		cfg.setTcpNoDelay(true);

		ExecutionVenueNioServer server = new ExecutionVenueNioServer();
		server.setNioConfig(cfg);
		server.setServerHandler(new TestServerHandler());
		server.start();

		final Socket socket = new Socket("127.0.0.1", 2222);

		ExecutorService exec = Executors.newCachedThreadPool();

		exec.submit(new Runnable() {

			@Override
			public void run() {
				try {
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					while (true) {
						dis.readInt();
						System.err.println("Client got " + dis.readInt());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		for (int i = 0; i < 10; i++) {
			final int j = i;
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream das = new DataOutputStream(baos);
						das.writeInt(j);

						byte[] bytes = baos.toByteArray();

						final ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
						buffer.putInt(4);
						buffer.put(bytes);
						buffer.flip();

						socket.getOutputStream().write(buffer.array());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
