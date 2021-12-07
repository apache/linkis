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
import com.alibaba.datax.common.plugin.BasicDataReceiver;
import com.webank.wedatasphere.linkis.datax.common.constant.TransportMode;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelInput;

/**
 * @author davidhua
 * 2020/4/8
 */
public class EnhancedWriter {

    public abstract static class Job extends AbstractJobPlugin{
        /**
         * Post processor's class name
         */
        protected String processor;

        /**
         * If the writer supports transport type of stream
         * @return boolean
         */
        public boolean isSupportStream(){
            return false;
        }

        /**
         * sync meta schema
         * @param metaSchema
         */
        public void syncMetaData(MetaSchema metaSchema){

        }

        /**
         * The transport modes that the writer supports
         * @return modes
         */
        public TransportMode[] transportModes(){
            return new TransportMode[]{TransportMode.OFFLINE};
        }

        public void setProcessor(String processor){
            this.processor = processor;
        }

        public String getProcessors(){
            return processor;
        }
    }

    public abstract static class Task extends AbstractTaskPlugin{
        /**
         *  custom data channel
         * @param receiver
         * @param type
         */
        public void  startWrite(BasicDataReceiver<Object> receiver, Class<?> type){
            //throw unsupport
        }

        /**
         * if plugin can use stream channel
         * @param inputStream
         */
        public void startWrite(ChannelInput inputStream){
            //do nothing
        }
    }
}
