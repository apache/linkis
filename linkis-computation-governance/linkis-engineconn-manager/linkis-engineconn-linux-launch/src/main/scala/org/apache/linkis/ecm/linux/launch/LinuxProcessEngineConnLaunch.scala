/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.ecm.linux.launch

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.ecm.core.launch.ProcessEngineConnLaunch

class LinuxProcessEngineConnLaunch extends ProcessEngineConnLaunch {

  override protected def execFile: Array[String] = Array("sh", getPreparedExecFile)

  override protected def sudoCommand(user: String, command: String): Array[String] =
    Array("sudo", "su", "-", user, "-c", command)

  override def getPid(): Option[String] = {
    Utils.tryCatch {
      val clazz = Class.forName("java.lang.UNIXProcess");
      val field = clazz.getDeclaredField("pid");
      field.setAccessible(true);
      val pid = field.get(getProcess()).asInstanceOf[Int]
      if (pid > 0) {
        Some(pid.toString)
      } else {
        None
      }
    } { t =>
      logger.info("Failed to acquire pid for shell process", t)
      None
    }
  }

}
