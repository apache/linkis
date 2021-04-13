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
package com.webank.wedatasphere.linkis.enginemanager.flink.conf;

import com.webank.wedatasphere.linkis.common.conf.ByteType;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.common.conf.Configuration;

/**
 *
 * Created by liangqilang on 01 20, 2021
 *
 */
public class FlinkConfiguration {

    public static final CommonVars FLINK_CLIENT_MEMORY = CommonVars$.MODULE$.apply("flink.client.memory",new ByteType("2g"), "指定引擎客户端的内存大小");

    public static final CommonVars FLINK_CLIENT_OPTS = CommonVars$.MODULE$.apply("flink.client.java.opts","-server -XX:+UseG1GC -XX:MaxPermSize=512m -XX:PermSize=128m" +
                    "-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps",
            "Specify the option parameter of the FlinkCli process (please modify it carefully!!!)(指定FlinkCli进程的option参数（请谨慎修改！！！）)");

    public static final CommonVars FLINK_ENGINE_SPRING_APPLICATION_NAME = CommonVars$.MODULE$.apply("wds.linkis.engine.application.name", "flinkEngine");

    public static final CommonVars FLINK_CLIENT_EXTRACLASSPATH = CommonVars$.MODULE$.apply("flink.engine.extraClassPath","/appcom/commonlib/webank_bdp_udf.jar", "Specify the full path of the user-defined jar package (multiple separated by English)(指定用户自定义的jar包全路径（多个以英文,分隔）。)");


}
