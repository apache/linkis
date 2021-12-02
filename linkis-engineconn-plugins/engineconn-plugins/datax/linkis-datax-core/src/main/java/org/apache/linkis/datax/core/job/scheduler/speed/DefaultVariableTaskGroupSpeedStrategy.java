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

package org.apache.linkis.datax.core.job.scheduler.speed;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.util.container.CoreConstant;


/**
 * Variable speed speed
 * @author davidhua
 * 2019/10/22
 */
public class DefaultVariableTaskGroupSpeedStrategy implements VariableTaskGroupSpeedStrategy {


    @Override
    public boolean adjustSpeed(Communication communication, Configuration configuration) {
        boolean result = false;
        long channelNum = communication.getLongCounter(CommunicationTool.CHANNEL_RUNNING);
        if(channelNum <= 0){
            return false;
        }
        long globalLimitedByteSpeed = configuration.getLong(CoreConstant.DATAX_JOB_SETTING_SPEED_BYTE, 0);
        if(globalLimitedByteSpeed > 0){
            Long channelLimitedByteSpeed = configuration
                    .getLong(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE);
            if(channelLimitedByteSpeed * channelNum != globalLimitedByteSpeed){
                long adjustedLimitedByteSpeed = globalLimitedByteSpeed / channelNum;
                if(adjustedLimitedByteSpeed != channelLimitedByteSpeed){
                    configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE,
                            adjustedLimitedByteSpeed);
                    result = true;
                }
            }
        }
        long globalLimitedRecordSpeed = configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_RECORD, 0);
        if(globalLimitedRecordSpeed > 0){
            Long channelLimitedRecordSpeed = configuration.getLong(
                    CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD);
            if(channelLimitedRecordSpeed * channelNum != globalLimitedRecordSpeed){
                long adjustedLimitedRecordSpeed = globalLimitedRecordSpeed / channelNum;
                if(adjustedLimitedRecordSpeed != channelLimitedRecordSpeed){
                    configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD,
                            adjustedLimitedRecordSpeed);
                    result = true;
                }
            }
        }
        return result;
    }
}
