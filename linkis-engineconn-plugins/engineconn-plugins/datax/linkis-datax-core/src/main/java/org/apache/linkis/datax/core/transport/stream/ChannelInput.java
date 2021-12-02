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
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.fastjson.JSON;
import org.apache.linkis.datax.core.transport.channel.StreamChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * actually combined with multiply streams
 * @author davidhua
 * 2019/3/26
 */
public class ChannelInput{
    private static final Logger LOG = LoggerFactory.getLogger(ChannelInput.class);

    private StreamChannel streamChannel;
    private ByteBlock byteBlock;
    private ByteBuffer byteBuffer;
    private byte[] tmpBuf;
    private byte[] streamMeta;
    private boolean shutdown = false;
    private ChannelInputStream stream;

    public ChannelInput(StreamChannel streamChannel){
        this.streamChannel = streamChannel;
        tmpBuf = new byte[512];
    }

    public InputStream nextStream() throws IOException {
        if(shutdown){
            return null;
        }
        if(null != stream){
            closeStream();
        }
        if(byteBlock != ByteBlock.TERMINATE){
            //set byteBlock null
            byteBlock = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len = -1;
            while((len = read0(tmpBuf, 0, tmpBuf.length)) > 0){
                byteArrayOutputStream.write(tmpBuf, 0, len);
            }
            if( byteBlock != ByteBlock.TERMINATE){
                streamMeta = byteArrayOutputStream.toByteArray();
                byteBlock = null;
                stream =  new ChannelInputStream();
                return stream;
            }
        }
        return null;
    }

    public StreamMeta streamMetaData(String encoding){
        try{
            String metaJson = new String(streamMeta, encoding);
            return JSON.parseObject(metaJson, StreamMeta.class);
        }catch(Exception e){
            throw DataXException.asDataXException(FrameworkErrorCode.RUNTIME_ERROR, e);
        }
    }

    public void shutdown(){
        try {
            if (null != stream) {
                stream.close();
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        this.shutdown = true;
    }

    private void closeStream() throws IOException {
        while(stream.read(this.tmpBuf, 0, this.tmpBuf.length) != -1){
            ;
        }
        stream.close();
        stream = null;
        streamMeta = null;
    }

    private int read0(byte[] b, int off, int len){
        pullFromChannel();
        if(byteBuffer.remaining() <= 0){
            return -1;
        }
        int start = off;
        while(len > 0 && byteBuffer.remaining() > 0){
            int rest = byteBuffer.remaining();
            if(rest > len){
                byteBuffer.get(b, off, len);
                break;
            }else{
                byteBuffer.get(b, off, rest);
                off += rest;
                len -= rest;
            }
            pullFromChannel();
        }
        return off - start;
    }
    private void pullFromChannel(){
        if(byteBlock != ByteBlock.SEPARATOR && byteBlock != ByteBlock.TERMINATE){
            if(null == byteBuffer || byteBuffer.remaining() <= 0){
                byteBlock = streamChannel.pull();
                byteBuffer = byteBlock.getByteStored();
                if(null == byteBuffer){
                    byteBuffer = ByteBuffer.allocate(0);
                }
            }
        }
    }

    public class ChannelInputStream extends InputStream{
        private boolean isClosed = false;

        @Override
        public int read() throws IOException {
            if(isClosed){
                throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "channel input stream has been closed");
            }
            pullFromChannel();
            return byteBuffer.remaining() > 0? byteBuffer.get() & 0xFF : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if(isClosed){
                throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "channel input stream has been closed");
            }
            if(b == null){
                throw new NullPointerException();
            }else if(off < 0 || len < 0 || len > b.length - off){
                throw new IndexOutOfBoundsException();
            }else if(len == 0){
                return 0;
            }
            return read0(b, off, len);
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
        }

        @Override
        public int available() throws IOException {
            throw DataXException.asDataXException(CommonErrorCode.RUNTIME_ERROR, "channel input stream doesn't support method named 'available'");
        }

    }
}
