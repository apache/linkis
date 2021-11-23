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

package com.webank.wedatasphere.linkis.datax.core.transport.stream;

import com.alibaba.datax.core.util.ClassSize;
import com.webank.wedatasphere.linkis.datax.core.transport.channel.ChannelElement;

import java.nio.ByteBuffer;

/**
 * @author davidhua
 * 2019/3/26
 */
public class ByteBlock implements ChannelElement {

    public static final ByteBlock SEPARATOR = new ByteBlock(ByteBuffer.allocate(0));

    public static final ByteBlock TERMINATE = new ByteBlock(null);
    private ByteBuffer byteStored;

    public ByteBlock(ByteBuffer byteStored){
        this.byteStored = byteStored;
    }

    @Override
    public int getByteSize() {
        return byteStored != null? byteStored.remaining() : 0;
    }

    @Override
    public int getMemorySize() {
        return ClassSize.REFERENCE + ClassSize.ByteBufferHead +
                (byteStored != null? byteStored.remaining() : 0);
    }

    ByteBuffer getByteStored(){
        return byteStored;
    }

    @Override
    public <T>T copyElement() {
        if(this.equals(SEPARATOR) || this.equals(TERMINATE)){
            return (T)this;
        }
        ByteBuffer buffer = null;
        if(null != byteStored) {
            byte[] stored = byteStored.array();
            buffer = ByteBuffer.wrap(stored);
            buffer.position(0);
            buffer.limit(byteStored.limit());
        }
        return (T) new ByteBlock(buffer);
    }
}
