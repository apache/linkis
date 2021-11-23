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

package com.webank.wedatasphere.linkis.datax.common.spi;

import com.alibaba.datax.common.plugin.AbstractJobPlugin;
import com.alibaba.datax.common.plugin.AbstractTaskPlugin;
import com.webank.wedatasphere.linkis.datax.common.constant.TransportMode;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelOutput;

/**
 * @author davidhua
 * 2020/4/8
 */
public class EnhancedReader {

    public static abstract class Job extends AbstractJobPlugin{
        /**
         * If the reader supports transport type of stream
         * @return boolean
         */
        public boolean isSupportStream(){
            return false;
        }

        /**
         * The transport mode the reader in
         * @return boolean
         */
        public TransportMode transportMode(){
            return TransportMode.OFFLINE;
        }
        /**
         * Get meta schema from reader job
         * @return
         */
        public MetaSchema syncMetaData(){
            return null;
        }
    }

    public static abstract class Task extends AbstractTaskPlugin{
        /**
         * if plugin can use stream channel
         * @param outputStream
         */
        public void startRead(ChannelOutput outputStream){
            //do nothing
        }
    }
}
