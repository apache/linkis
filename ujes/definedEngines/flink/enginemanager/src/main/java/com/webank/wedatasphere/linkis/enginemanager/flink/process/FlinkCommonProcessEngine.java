package com.webank.wedatasphere.linkis.enginemanager.flink.process;

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.enginemanager.process.CommonProcessEngine;
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder;
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngineBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-08-11 21:18
 */
public class FlinkCommonProcessEngine extends CommonProcessEngine {



    public FlinkCommonProcessEngine(ProcessEngineBuilder processBuilder, DWCArgumentsParser dwcArgumentsParser, long timeout) {
        super(processBuilder, dwcArgumentsParser, timeout);
    }

    public FlinkCommonProcessEngine(ProcessEngineBuilder processBuilder, DWCArgumentsParser dwcArgumentsParser) {
        super(processBuilder, dwcArgumentsParser);
    }

}
