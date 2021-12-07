package com.alibaba.datax.core.transport.channel;

import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author davidhua
 */
public abstract class RecordChannel extends AbstractChannel<Record>{

    private static final Logger LOG = LoggerFactory.getLogger(RecordChannel.class);


    public RecordChannel(Configuration configuration) {
        super(configuration);
        this.dataSpeed = configuration.getLong(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD, 10000);
        this.rateLimiterData = RateLimiter.create(this.dataSpeed);
    }

    @Override
    protected void statPush(Communication currentCommunication, long dataSize) {
        currentCommunication.increaseCounter(CommunicationTool.READ_SUCCEED_RECORDS, dataSize);
    }

    @Override
    protected void statPull(Communication currentCommunication, long dataSize) {
        currentCommunication.increaseCounter(CommunicationTool.WRITE_RECEIVED_RECORDS, dataSize);
    }

    @Override
    protected long currentDataSpeed(Communication currentCommunication, Communication lastCommunication
    , long interval) {
        return (CommunicationTool.getTotalReadRecords(currentCommunication) -
            CommunicationTool.getTotalReadRecords(lastCommunication)) * 1000/ interval;
    }

    @Override
    protected void updateCounter(Communication currentCommunication, Communication lastCommunication) {
        lastCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_RECORDS,
                currentCommunication.getLongCounter(CommunicationTool.READ_SUCCEED_RECORDS));
        lastCommunication.setLongCounter(CommunicationTool.READ_FAILED_RECORDS,
                currentCommunication.getLongCounter(CommunicationTool.READ_FAILED_RECORDS));
    }

    @Override
    protected void firstPrint() {
        long dataSpeed = configuration.getLong(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD, 10000);
        LOG.info("RecordChannel set byte_speed_limit to " + getByteSpeed() +
                (getByteSpeed() <= 0 ? ", No bps activated." : "."));
        LOG.info("RecordChannel set record_speed_limit to " + dataSpeed
            + (dataSpeed <= 0 ? ", No tps activated." : "."));
    }
}
