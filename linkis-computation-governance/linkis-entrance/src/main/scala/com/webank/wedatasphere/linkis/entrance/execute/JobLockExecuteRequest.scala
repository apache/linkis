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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * Created by enjoyyin on 2018/9/15.
  */
trait ReconnectExecuteRequest {
  val execId: String
}

trait LabelExecuteRequest {
  val labels: java.util.List[Label[_]]
}

trait StorePathExecuteRequest {
  val storePath: String
}

trait RuntimePropertiesExecuteRequest {
  val properties: java.util.Map[String, Object]
}

trait UserExecuteRequest {
  val submitUser: String
  val executeUser: String
}