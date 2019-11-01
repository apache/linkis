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

package com.webank.wedatasphere.linkis.enginemanager.impala.conf

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}


/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
object ImpalaEngineConfiguration {
    val IMPALA_CLIENT_MEMORY = CommonVars("impala.client.memory", new ByteType("2g"), "Specify the memory size of the hiveCli client(指定hiveCli客户端的内存大小)")
    
    val IMPALA_CLIENT_OPTS = CommonVars("impala.client.java.opts", "-server -XX:+UseG1GC -XX:MaxPermSize=250m -XX:PermSize=128m " +
      "-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps",
      "Specify the option parameter of the ImpalaCli process (please modify it carefully!!!)(指定ImpalaCli进程的option参数（请谨慎修改！！！）)")
    val IMPALA_CLIENT_EXTRACLASSPATH = CommonVars("impala.client.extraClassPath", "/appcom/commonlib/webank_bdp_udf.jar", "Specify the full path of the user-defined jar package (multiple separated by English)(指定用户自定义的jar包全路径（多个以英文,分隔）。)")
    
    val IMPALA_SESSION_HOOK = CommonVars("wds.linkis.engine.impala.session.hook", "")

    val IMPALA_CAPACITY = CommonVars("wds.linkis.query.impala.capacity", 100)
    val IMPALA_PARALLELISM = CommonVars("wds.linkis.query.impala.parallelism", 3)

    val IMPALA_USER_MAX_ALLOCATE_MEMORY = CommonVars("wds.linkis.engine.impala.user.memory.max", new ByteType("10g"))
    val IMPALA_USER_MAX_ALLOCATE_SESSIONS = CommonVars("wds.linkis.engine.impala.user.sessions.max", 3)

    val IMPALA_MAX_PARALLELISM_USERS = CommonVars("wds.linkis.engine.impala.user.parallelism", 100)
    val IMPALA_USER_MAX_WAITING_SIZE = CommonVars("wds.linkis.engine.impala.user.waiting.max", 100)
    val IMPALA_ENGINE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engine.application.name", "impalaEngine")

}