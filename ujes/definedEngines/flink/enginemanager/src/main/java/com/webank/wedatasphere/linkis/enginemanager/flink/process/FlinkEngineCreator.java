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
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 16:43
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
        LOG.info("DWCArgumentsParser:"+  JSON.toString(JavaConversions.mapAsJavaMap(parser.getDWCConfMap())));
        if(engineResource instanceof TimeoutEngineResource){
            return new FlinkCommonProcessEngine(processEngineBuilder,parser,((TimeoutEngineResource)engineResource).getTimeout());
        } else {
            return new FlinkCommonProcessEngine(processEngineBuilder,parser);
        }
    }
}
