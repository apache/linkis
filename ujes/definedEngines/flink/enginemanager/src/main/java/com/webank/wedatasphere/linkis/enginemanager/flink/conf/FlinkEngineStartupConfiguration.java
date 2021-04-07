package com.webank.wedatasphere.linkis.enginemanager.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.common.conf.*;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 16:41
 */
public class FlinkEngineStartupConfiguration {


    public static final CommonVars FLINK_CLIENT_MEMORY = CommonVars$.MODULE$.apply("flink.client.memory",new ByteType("2g"), "指定引擎客户端的内存大小");

}
