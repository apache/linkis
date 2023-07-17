/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.impala.client.thrift;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import javax.net.SocketFactory;
import javax.security.auth.callback.CallbackHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpalaThriftSessionFactory {
  public static final Logger LOG =
      LoggerFactory.getLogger(ImpalaThriftSessionFactory.class.getName());

  protected final Map<String, SessionHolder> sessions;
  protected final int maxConnections;
  protected final Semaphore semaphore;
  protected final SocketFactory socketFactory;
  /** sasl properties */
  protected final CallbackHandler saslCallbackHandler;

  protected final String saslMechanism;
  protected final String saslAuthorizationId;
  protected final String saslProtocol;
  protected final Map<String, String> saslProperties;
  protected int connectionTimeout = 30;

  public ImpalaThriftSessionFactory(
      String[] hosts, int maxConnections, SocketFactory socketFactory) {
    this(hosts, maxConnections, socketFactory, null, null, null, null, null);
  }

  public ImpalaThriftSessionFactory(
      String[] hosts,
      int maxConnections,
      SocketFactory socketFactory,
      String saslMechanism,
      String saslAuthorizationId,
      String saslProtocol,
      Map<String, String> saslProperties,
      CallbackHandler saslCallbackHandler) {
    if (socketFactory == null) {
      socketFactory = SocketFactory.getDefault();
    }

    if (maxConnections <= 0) {
      LOG.warn("Invalid maxConnections value: {}, set to 1", maxConnections);
      maxConnections = 1;
    }

    sessions = new TreeMap<>();
    for (String host : hosts) {
      String[] split = StringUtils.split(host, ":", 2);
      if (split.length == 2
          && StringUtils.isNotBlank(split[0])
          && StringUtils.isNumeric(split[1])) {
        SessionHolder holder = new SessionHolder(host, split[0], Integer.parseInt(split[1]));
        sessions.put(host, holder);
      }
    }

    if (sessions.isEmpty()) {
      throw new IllegalArgumentException("Invalid hosts: " + StringUtils.join(hosts, ','));
    }

    this.socketFactory = socketFactory;
    this.maxConnections = maxConnections;
    this.semaphore = new Semaphore(maxConnections);

    this.saslMechanism = saslMechanism;
    this.saslAuthorizationId = saslAuthorizationId;
    this.saslProtocol = saslProtocol;
    this.saslProperties = saslProperties;
    this.saslCallbackHandler = saslCallbackHandler;
  }

  public int openedSessionCount() {
    return maxConnections - semaphore.availablePermits();
  }

  public ImpalaThriftSession openSession() throws InterruptedException, IOException, TException {
    if (!semaphore.tryAcquire(5, TimeUnit.MINUTES)) {
      String format =
          String.format(
              "Connection pool for [%s] is exhausted, max allow: %d",
              StringUtils.join(sessions.keySet(), ","), maxConnections);
      LOG.error(format);
      throw new IllegalStateException(format);
    }

    /*
     * get random holder with minimal connections
     */
    SessionHolder holder =
        sessions.values().stream()
            .min(
                Comparator.<SessionHolder>comparingInt(o -> o.connections)
                    .thenComparingInt(o -> o.randomInt))
            .get();

    /*
     * create socket
     */
    Socket socket = socketFactory.createSocket(holder.host, holder.port);

    /*
     * create thrift socket
     */
    TSocket tSocket = new TSocket(socket);
    tSocket.setTimeout(connectionTimeout * 1000);
    socket.setSoTimeout(connectionTimeout * 2000);

    TTransport tTransport;
    if (saslCallbackHandler != null) {
      tTransport =
          new TSaslClientTransport(
              saslMechanism,
              saslAuthorizationId,
              saslProtocol,
              holder.name,
              saslProperties,
              saslCallbackHandler,
              tSocket);

    } else {
      tTransport = tSocket;
    }

    if (!tTransport.isOpen()) {
      tTransport.open();
    }

    ImpalaThriftSession impalaSession =
        new ImpalaThriftSession(holder.name, tTransport, holder::release);
    holder.acquire();
    return impalaSession;
  }

  private class SessionHolder {
    private final String name;
    private final String host;
    private final int port;
    private final int randomInt = RandomUtils.nextInt();
    private volatile int connections;

    SessionHolder(String name, String host, int port) {
      this.name = name;
      this.host = host;
      this.port = port;
    }

    void release() {
      semaphore.release();

      synchronized (this) {
        --connections;
      }
    }

    void acquire() {
      synchronized (this) {
        ++connections;
      }
    }
  }
}
