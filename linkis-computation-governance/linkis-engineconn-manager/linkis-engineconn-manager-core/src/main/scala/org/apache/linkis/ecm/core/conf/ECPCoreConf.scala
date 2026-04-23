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

package org.apache.linkis.ecm.core.conf

import org.apache.linkis.common.conf.CommonVars

object ECPCoreConf {

  val CORE_DUMP_DISABLE = CommonVars("linkis.ec.core.dump.disable", true).getValue

  /**
   * 物料替换脚本路径 脚本会在每次软链接创建前被调用，参数为：源路径 目标路径 例如：/appcom/Install/linkis/bin/material_replace.sh
   */
  val MATERIAL_REPLACE_SCRIPT_PATH: String =
    CommonVars("linkis.ecm.material.replace.script.path", "").getValue

}
