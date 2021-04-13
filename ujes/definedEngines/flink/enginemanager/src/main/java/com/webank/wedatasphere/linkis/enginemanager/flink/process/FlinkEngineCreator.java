/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.enginemanager.flink.process;

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser;
import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineCreator;
import com.webank.wedatasphere.linkis.enginemanager.EngineResource;
import com.webank.wedatasphere.linkis.enginemanager.TimeoutEngineResource;
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngine;
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scala.collection.JavaConversions;


/**
 *
 * Created by liangqilang on 01 20, 2021
 *
 */
@Component("engineCreator")
public class FlinkEngineCreator extends AbstractEngineCreator {

    private Logger LOG = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessEngineBuilder createProcessEngineBuilder() {
        return new FlinkEngineProcessBuilder();
    }

    @Override
    public ProcessEngine createEngine(ProcessEngineBuilder processEngineBuilder, DWCArgumentsParser parser) {
        EngineResource engineResource=  processEngineBuilder.getEngineResource();
        //获取作业配置
        java.util.Map<String,String> properties = processEngineBuilder.getRequestEngine().properties();
        FlinkEngineProcessBuilder flinkEngineProcessBuilder = (FlinkEngineProcessBuilder)processEngineBuilder;
        //外部传入flink集群、以及版本信息，用于设置多集群、版本
        parser.setConf("--dwc-conf","hadoop.config.dir",flinkEngineProcessBuilder.getHadoopConf());
        parser.setConf("--dwc-conf","flink.config.dir", flinkEngineProcessBuilder.getFlinkConf());
        parser.setConf("--dwc-conf","flink.home",flinkEngineProcessBuilder.getFlinkHome());
        if(engineResource instanceof TimeoutEngineResource){
            return new FlinkCommonProcessEngine(processEngineBuilder,parser,((TimeoutEngineResource)engineResource).getTimeout());
        } else {
            return new FlinkCommonProcessEngine(processEngineBuilder,parser);
        }
    }
}
