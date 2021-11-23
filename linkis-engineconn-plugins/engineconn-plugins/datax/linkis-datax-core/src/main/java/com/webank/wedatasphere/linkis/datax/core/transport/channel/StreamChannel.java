/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datax.core.transport.channel;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.transport.channel.AbstractChannel;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ByteBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author davidhua
 * 2019/3/26
 */
public abstract class StreamChannel extends AbstractChannel<ByteBlock> {

    protected long blockSize = 0;

    private static final Logger LOG = LoggerFactory.getLogger(StreamChannel.class);

    public StreamChannel(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void statPush(Communication currentCommunication, long dataSize) {
    }

    @Override
    protected void statPull(Communication currentCommunication, long dataSize) {

    }

    @Override
    protected long currentDataSpeed(Communication currentCommunication, Communication lastCommunication, long interval) {
        return 0;
    }

    @Override
    protected void updateCounter(Communication currentCommunication, Communication lastCommunication) {

    }

    @Override
    protected void firstPrint() {
        LOG.info("StreamChannel set byte_speed_limit to " + getByteSpeed() +
                (getByteSpeed() <= 0? ", No bps activated." : "."));
    }

    public long getBlockSize(){
        return this.blockSize;
    }
}
