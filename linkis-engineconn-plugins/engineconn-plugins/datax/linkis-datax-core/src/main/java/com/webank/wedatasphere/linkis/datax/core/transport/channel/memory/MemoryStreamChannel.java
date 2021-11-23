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

package com.webank.wedatasphere.linkis.datax.core.transport.channel.memory;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.webank.wedatasphere.linkis.datax.core.transport.channel.StreamChannel;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ByteBlock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author davidhua
 * 2019/3/22
 */
public class MemoryStreamChannel extends StreamChannel {
    private BlockingQueue<ByteBlock> queue = null;

    public MemoryStreamChannel(final Configuration configuration) {
        super(configuration);
        super.consumeIsolated = true;
        this.queue = new LinkedBlockingQueue<>(this.getCapacity());
        this.blockSize = configuration.getInt(CoreConstant.DATAX_CORE_TRANSPORT_STREAM_CHANNEL_BLOCKSIZE,
                8192);
    }

    @Override
    protected void doPush(ByteBlock byteBlock) {
        try{
            if(byteBlock.getByteSize() > blockSize){
                throw DataXException.asDataXException(FrameworkErrorCode.RUNTIME_ERROR, "the size of byte block is too big");
            }
            long startTime = System.nanoTime();
            this.queue.put(byteBlock);
            waitWriterTime.addAndGet(System.nanoTime() - startTime);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected ByteBlock doPull() {
        try{
            long startTime = System.nanoTime();
            ByteBlock block = this.queue.take();
            waitReaderTime.addAndGet(System.nanoTime() - startTime);
            return block;
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    @Override
    public void close() {
        super.close();
        //push the byte block size 0
        try {
            this.queue.put(ByteBlock.TERMINATE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
