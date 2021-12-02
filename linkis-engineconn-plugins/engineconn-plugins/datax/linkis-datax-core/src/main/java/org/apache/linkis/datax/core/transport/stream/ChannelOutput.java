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

package org.apache.linkis.datax.core.transport.stream;

import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.fastjson.JSON;
import org.apache.linkis.datax.core.transport.channel.StreamChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author davidhua
 * 2019/3/26
 */
public class ChannelOutput {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelOutput.class);
    private static final String DEFAULT_ENCODING = "UTF-8";
    private StreamChannel streamChannel;
    private long blockSize;
    private ByteBuffer byteBuffer;
    private byte[] streamMeta;
    private boolean shutdown = false;
    private ChannelOutputStream stream;

    public ChannelOutput(StreamChannel streamChannel){
        this.streamChannel = streamChannel;
        this.blockSize = streamChannel.getBlockSize();
        this.byteBuffer = ByteBuffer.allocate(Math.toIntExact(blockSize));
    }


    public OutputStream createStream() throws IOException{
        return createStream("");
    }

    public OutputStream createStream(String name) throws IOException {
        if(StringUtils.isBlank(name)){
            name = "1";
        }
        StreamMeta streamMeta = new StreamMeta();
        streamMeta.setName(name);
        return createStream(streamMeta, DEFAULT_ENCODING);
    }
    public OutputStream createStream(StreamMeta meta, String encoding) throws IOException{
        if(StringUtils.isBlank(encoding)){
            encoding = DEFAULT_ENCODING;
        }
        String metaJson = JSON.toJSONString(meta);
        return createStream(metaJson.getBytes(encoding));
    }
    private OutputStream createStream(byte[] metaData) throws IOException {
        if(shutdown){
            return null;
        }
        if(null != stream){
            stream.close();
            flush0();
            streamChannel.push(ByteBlock.SEPARATOR);
        }
        this.streamMeta = metaData;
        write0(this.streamMeta, 0, this.streamMeta.length);
        flush0();
        streamChannel.push(ByteBlock.SEPARATOR);
        stream = new ChannelOutputStream();
        return stream;
    }
    public void shutdown(){
        shutdown = true;
        streamChannel.clear();
    }

    public void close(){
        flush0();
        streamChannel.close();
    }
    private void write0(byte[] b,  int off , int len){
        while(len > 0){
            pushToChannel();
            int rest = byteBuffer.remaining();
            if(rest > len){
                byteBuffer.put(b, off, len);
                break;
            }else{
                byteBuffer.put(b, off, rest);
                off += rest;
                len -= rest;
            }
        }
    }

    private void flush0(){
        byteBuffer.flip();
        if(byteBuffer.remaining() > 0){
            streamChannel.push(new ByteBlock(byteBuffer));
        }
        byteBuffer = ByteBuffer.allocate(Math.toIntExact(blockSize));
    }
    private void pushToChannel(){
        if(byteBuffer.remaining() <= 0){
            byteBuffer.flip();
            streamChannel.push(new ByteBlock(byteBuffer));
            byteBuffer = ByteBuffer.allocate(Math.toIntExact(blockSize));
        }
    }
    public class ChannelOutputStream extends OutputStream{
        private boolean isClosed = false;
        @Override
        public void write(int b) throws IOException {
            if(isClosed){
                throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "channel output stream has been closed");
            }
            pushToChannel();
            byteBuffer.put((byte)(b & 0xff));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if(isClosed){
                throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "channel output stream has been closed");
            }
            if(b == null){
                throw new NullPointerException();
            }else if((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) > b.length) || ((off + len) < 0)){
                throw new IndexOutOfBoundsException();
            }else if (len == 0){
                return;
            }
            write0(b, off ,len);
        }

        @Override
        public void flush() throws IOException {
            flush0();
        }

        @Override
        public void close() throws IOException {
            flush();
            isClosed = true;
        }

    }
}
