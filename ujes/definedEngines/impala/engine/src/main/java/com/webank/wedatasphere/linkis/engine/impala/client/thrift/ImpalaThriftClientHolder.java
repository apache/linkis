package com.webank.wedatasphere.linkis.engine.impala.client.thrift;

import com.webank.wedatasphere.linkis.engine.impala.client.exception.ExceptionCode;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hive.service.cli.thrift.TOpenSessionReq;
import org.apache.hive.service.cli.thrift.TOpenSessionResp;
import org.apache.hive.service.cli.thrift.TProtocolVersion;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImpalaThriftClientHolder implements AutoCloseable {

    private static final int RELEASE_IDLE_COUNT = 60;
    private final int maxConnectionSize;
    private AtomicInteger currentConnectionSize;
    private ImpalaThriftTransportFactory transportFactory = null;
    private AtomicBoolean closed;

    private BlockingQueue<ImpalaThriftSessionHolder> queue;


    public ImpalaThriftClientHolder(ImpalaThriftTransportFactory transportFactory, int maxConnectionSize) throws Exception {
        this.transportFactory = transportFactory;

        this.closed = new AtomicBoolean(false);

        if (maxConnectionSize <= 0) {
            maxConnectionSize = 1;
        }
        this.maxConnectionSize = maxConnectionSize;
        this.queue = new ArrayBlockingQueue<>(maxConnectionSize + 5, true);

        this.queue.put(createSessionHolder());
        this.currentConnectionSize = new AtomicInteger(1);
    }

    private ImpalaThriftSessionHolder createSessionHolder() throws TransportException {
        TSessionHandle session = null;
        ImpalaHiveServer2Service.Client client = null;
        TTransport transport = null;
        try {
            transport = transportFactory.getTransport();
            client = new ImpalaHiveServer2Service.Client(new TBinaryProtocol(transport));
            TOpenSessionReq openReq = new TOpenSessionReq();
            openReq.setClient_protocol(TProtocolVersion.HIVE_CLI_SERVICE_PROTOCOL_V7);
            TOpenSessionResp openResp = client.OpenSession(openReq);
            session = openResp.getSessionHandle();
        } catch (Exception e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }
        if (session == null || !session.isSetSessionId()) {
            throw TransportException.of(ExceptionCode.LoginError);
        }
        ImpalaThriftSessionHolder holder = new ImpalaThriftSessionHolder(this, transport, client, session);
        return holder;
    }

    private ImpalaThriftSessionHolder tryOpen() throws TransportException {
        /* 待关闭的客户端，抛出错误 */
        if (closed.get()) {
            throw TransportException.of(ExceptionCode.ClosedError);
        }

        ImpalaThriftSessionHolder holder = queue.poll();
        if (holder == null) {
            if (currentConnectionSize.incrementAndGet() < maxConnectionSize) {
                holder = createSessionHolder();
            } else {
                currentConnectionSize.decrementAndGet();
            }
        }
        return holder;
    }

    public ImpalaThriftSessionHolder open(long timeout, TimeUnit unit) throws TransportException, InterruptedException {
        ImpalaThriftSessionHolder holder = tryOpen();

        if (holder == null) {
            log.warn("Connection pool contains no element active, waiting for {} {}.");
            holder = queue.poll(timeout, unit);
        }
        if (holder != null) {
            return holder.active();
        }
        return null;
    }


    public ImpalaThriftSessionHolder open() throws TransportException, InterruptedException {
        ImpalaThriftSessionHolder holder = tryOpen();
        if (holder == null) {
            holder = queue.take();
        }
        return holder.active();
    }


    public void release(ImpalaThriftSessionHolder holder) {
        if (closed.get()) {
            currentConnectionSize.decrementAndGet();
            holder.shutdown();
            return;
        }
        queue.offer(holder);
    }

    public void clean() {
        int len = queue.remainingCapacity();
        while (len > 0) {
            ImpalaThriftSessionHolder holder = queue.poll();
            if (holder == null) {
                break;
            }

            if (holder.idle() > RELEASE_IDLE_COUNT) {
                currentConnectionSize.decrementAndGet();
                holder.shutdown();
            } else {
                queue.offer(holder);
            }
            --len;
        }
    }

    @Override
    public synchronized void close() {
        closed.set(true);
        ImpalaThriftSessionHolder holder = null;
        while ((holder = queue.poll()) != null) {
            currentConnectionSize.decrementAndGet();
            holder.shutdown();
        }
    }

    public int activeCount() {
        return currentConnectionSize.get() - queue.size();
    }
}
