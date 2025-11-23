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

import org.apache.linkis.engineplugin.impala.client.exception.ImpalaEngineException;
import org.apache.linkis.engineplugin.impala.client.util.ThriftUtil;

import org.apache.hive.service.rpc.thrift.TCloseSessionReq;
import org.apache.hive.service.rpc.thrift.TCloseSessionResp;
import org.apache.hive.service.rpc.thrift.TOpenSessionReq;
import org.apache.hive.service.rpc.thrift.TOpenSessionResp;
import org.apache.hive.service.rpc.thrift.TProtocolVersion;
import org.apache.hive.service.rpc.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpalaThriftSession implements AutoCloseable {
  public static final Logger LOG = LoggerFactory.getLogger(ImpalaThriftSession.class.getName());

  private final String target;
  private final TTransport transport;
  private final Runnable closeCallback;
  private final ImpalaHiveServer2Service.Client client;
  private final TSessionHandle session;

  private volatile boolean closed;

  public ImpalaThriftSession(String target, TTransport transport, Runnable closeCallback)
      throws TException {
    this.target = target;
    this.transport = transport;
    this.closeCallback = closeCallback;
    this.closed = false;

    this.client = new ImpalaHiveServer2Service.Client(new TBinaryProtocol(transport));

    /* open session */
    TOpenSessionReq openReq = new TOpenSessionReq();
    openReq.setClient_protocol(TProtocolVersion.HIVE_CLI_SERVICE_PROTOCOL_V7);
    TOpenSessionResp openResp = client.OpenSession(openReq);
    this.session = openResp.getSessionHandle();
  }

  public TSessionHandle session() {
    return session;
  }

  public ImpalaHiveServer2Service.Client client() {
    return client;
  }

  public String target() {
    return target;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    closed = true;

    try {
      if (session.isSetSessionId()) {
        try {
          TCloseSessionReq req = new TCloseSessionReq(session);
          TCloseSessionResp res = client.CloseSession(req);
          ThriftUtil.checkStatus(res.getStatus());
        } catch (TException | ImpalaEngineException e) {
          LOG.error("Failed to safely close the session for {}", target, e);
        } finally {
          transport.close();
        }
      }
    } finally {
      closeCallback.run();
    }
  }
}
