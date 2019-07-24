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

package com.webank.wedatasphere.linkis.enginemanager.util

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}
import com.webank.wedatasphere.linkis.common.utils.Logging

/**
  * Created by allenlliu on 2019/4/8.
  */
object PythonConfiguration extends Logging {
  val PYTHON_SCRIPT:CommonVars[String] = CommonVars[String]("python.script", "python3", "Specify a Python startup script that accepts only shared storage paths（指定Python启动脚本，该路径只接受共享存储的路径）.")
  val PYTHON_PATH:CommonVars[String] = CommonVars[String]("python.path", "", "Specify Python's extra path, which only accepts shared storage paths（指定Python额外的path，该路径只接受共享存储的路径）.")
  val PYTHON_JAVA_CLIENT_MEMORY:CommonVars[ByteType] = CommonVars[ByteType]("python.java.client.memory", new ByteType("1g"), "指定Python Java客户端进程的内存大小")
  val PYTHON_JAVA_CLIENT_OPTS:CommonVars[String] = CommonVars[String]("python.java.client.opts", "-server -XX:+UseG1GC -XX:MetaspaceSize=250m -XX:MetaspaceSize=128m " +
    "-Xloggc:%s -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps",
    "Specify the option parameter of the Python Java client process (please modify it carefully!!!)（指定Python Java客户端进程的option参数（请谨慎修改！！！））")
  val PYTHON_CLIENT_MEMORY:CommonVars[ByteType] = CommonVars[ByteType]("python.client.memory", new ByteType("4g"), "Specify the memory size of the Python process（指定Python进程的内存大小）")
  val PYTHON_CLIENT_CORES:CommonVars[Int] = CommonVars[Int]("python.client.cores", 1, "Specify the CPU size that the Python process can consume（指定Python进程能占用的CPU大小）")

  val PYTHON_CAPACITY:CommonVars[Int] = CommonVars[Int]("wds.linkis.query.python.capacity", 100)
  val PYTHON_PARALLELISM:CommonVars[Int] = CommonVars[Int]("wds.linkis.query.python.parallelism", 3)

  val PYTHON_USER_MAX_ALLOCATE_MEMORY:CommonVars[ByteType] = CommonVars[ByteType]("wds.linkis.engine.python.user.memory.max", new ByteType("10g"))
  val PYTHON_USER_MAX_ALLOCATE_SESSIONS:CommonVars[Int] = CommonVars[Int]("wds.linkis.engine.python.user.sessions.max", 3)

  val PYTHON_MAX_PARALLELISM_USERS:CommonVars[Int] = CommonVars[Int]("wds.linkis.engine.python.user.parallelism", 100)
  val PYTHON_USER_MAX_WAITING_SIZE:CommonVars[Int] = CommonVars[Int]("wds.linkis.engine.python.user.waiting.max", 100)

  val PYTHON_LANGUAGE_REPL_INIT_TIME:CommonVars[String] = CommonVars[String]("wds.linkis.engine.python.language-repl.init.time", new String("30s"))
  val PYTHON_ENGINE_CLASS_PATH:CommonVars[String] =  CommonVars[String]("python.engine.class.path", "")

}
