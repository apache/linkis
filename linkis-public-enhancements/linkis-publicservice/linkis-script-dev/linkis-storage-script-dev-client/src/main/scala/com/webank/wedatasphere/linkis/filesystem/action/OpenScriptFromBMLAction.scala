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
package com.webank.wedatasphere.linkis.filesystem.action

import com.webank.wedatasphere.linkis.filesystem.conf.WorkspaceClientConf
import com.webank.wedatasphere.linkis.httpclient.request.UserAction


/**
  * Created by patinousward
  */
class OpenScriptFromBMLAction extends WorkspaceGETAction with UserAction{

  private var user:String = _

  override def getURL: String = WorkspaceClientConf.scriptFromBMLUrl

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}
