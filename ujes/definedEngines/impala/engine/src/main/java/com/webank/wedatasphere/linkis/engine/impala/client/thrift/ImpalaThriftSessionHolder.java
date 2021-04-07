package com.webank.wedatasphere.linkis.engine.impala.client.thrift;

import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import com.webank.wedatasphere.linkis.engine.impala.client.util.ImpalaThriftUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hive.service.cli.thrift.TCloseSessionReq;
import org.apache.hive.service.cli.thrift.TCloseSessionResp;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImpalaThriftSessionHolder implements AutoCloseable {
    private ImpalaThriftClientHolder clientHolder;

    private TTransport transport = null;
    private ImpalaHiveServer2Service.Client client;
    private TSessionHandle session;
    private AtomicInteger idle;

    public ImpalaThriftSessionHolder(ImpalaThriftClientHolder clientHolder, TTransport transport,
                                     ImpalaHiveServer2Service.Client client,
                                     TSessionHandle session) {
        this.transport = transport;
        this.clientHolder = clientHolder;
        this.session = session;
        this.client = client;
        this.idle = new AtomicInteger(0);
    }

    public TSessionHandle session() {
        return session;
    }

    public ImpalaHiveServer2Service.Client client() {
        return client;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    public void shutdown() {
        if (session != null && session.isSetSessionId()) {
            try {
                TCloseSessionReq req = new TCloseSessionReq(session);
                TCloseSessionResp res = client.CloseSession(req);
                ImpalaThriftUtil.checkStatus(res.getStatus());
            } catch (TException | SubmitException e) {
                log.error("Failed to safely close the session.", e);
            }
        }

        transport.close();
    }

    @Override
    public void close() {
        clientHolder.release(this);
    }

    public int idle() {
        return idle.incrementAndGet();
    }

    public ImpalaThriftSessionHolder active() {
        idle.set(0);
        return this;
    }
}
