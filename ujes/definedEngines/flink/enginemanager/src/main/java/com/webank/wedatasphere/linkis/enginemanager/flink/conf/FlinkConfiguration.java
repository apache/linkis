package com.webank.wedatasphere.linkis.enginemanager.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.ByteType;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.common.conf.Configuration;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 16:42
 */
public class FlinkConfiguration {

    public static final CommonVars FLINK_CLIENT_MEMORY = CommonVars$.MODULE$.apply("flink.client.memory",new ByteType("2g"), "指定引擎客户端的内存大小");

    public static final CommonVars FLINK_CLIENT_OPTS = CommonVars$.MODULE$.apply("flink.client.java.opts","-server -XX:+UseG1GC -XX:MaxPermSize=512m -XX:PermSize=128m" +
                    "-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps",
            "Specify the option parameter of the FlinkCli process (please modify it carefully!!!)(指定FlinkCli进程的option参数（请谨慎修改！！！）)");

    public static final CommonVars FLINK_ENGINE_SPRING_APPLICATION_NAME = CommonVars$.MODULE$.apply("wds.linkis.engine.application.name", "flinkEngine");

    public static final CommonVars FLINK_CLIENT_EXTRACLASSPATH = CommonVars$.MODULE$.apply("flink.engine.extraClassPath","/appcom/commonlib/webank_bdp_udf.jar", "Specify the full path of the user-defined jar package (multiple separated by English)(指定用户自定义的jar包全路径（多个以英文,分隔）。)");


}
