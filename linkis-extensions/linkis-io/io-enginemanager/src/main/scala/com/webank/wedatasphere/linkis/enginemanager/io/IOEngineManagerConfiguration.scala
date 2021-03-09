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

package com.webank.wedatasphere.linkis.enginemanager.io

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by johnnwang on 2018/10/30.
  */
object IOEngineManagerConfiguration {
  val IO_USER = CommonVars("wds.linkis.storage.io.user", "root")
  val EXTRA_JAVA_OPTS = CommonVars.apply("wds.linkis.io.em.opts","-Dfile.encoding=UTF-8")
}
