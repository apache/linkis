/**
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
package com.webank.wedatasphere.linkis.cs.client.http

import com.webank.wedatasphere.linkis
import com.webank.wedatasphere.linkis.cs.client.utils.{ContextClientConf, ContextClientUtils, ContextServerHttpConf}
import com.webank.wedatasphere.linkis.httpclient.request.{GetAction, POSTAction, UserAction}
import org.apache.commons.text.StringEscapeUtils
/**
 * created by cooperyang on 2020/2/11
 * Description:
 */
trait ContextAction extends UserAction{

  private var user:String = "hadoop"

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

abstract class ContextGETAction extends GetAction with ContextAction

abstract class ContextPostAction extends POSTAction with ContextAction {
  override def getRequestPayload: String = ContextClientUtils.gson.toJson(getRequestPayloads)
}

case class ContextCreateAction() extends ContextPostAction{

  override def getURL: String = ContextServerHttpConf.createContextURL

}

case class ContextGetValueAction() extends ContextPostAction{
  override def getURL: String = ContextServerHttpConf.getContextValueURL
}


case class ContextUpdateAction() extends ContextPostAction{

  override def getURL: String = ContextServerHttpConf.updateContextURL

}

case class ContextSetKeyValueAction() extends ContextPostAction{

  override def getURL: String = ContextServerHttpConf.setKeyValueURL

}


case class ContextResetValueAction() extends ContextPostAction{

  override def getURL: String = ContextServerHttpConf.resetKeyValueURL

}


case class ContextResetIDAction() extends ContextPostAction{
  override def getURL: String = ContextServerHttpConf.resetContextIdURL
}


case class ContextRemoveAction(contextId:String,
                               contextKey:String) extends ContextPostAction with UserAction {

  override def getURL: String = ContextServerHttpConf.removeValueURL

}


case class ContextBindIDAction() extends ContextPostAction{
  override def getURL: String = ContextServerHttpConf.onBindIDURL
}

case class ContextBindKeyAction() extends ContextPostAction{
  override def getURL: String = ContextServerHttpConf.onBindKeyURL
}




case class ContextFetchAction(contextId:String) extends ContextGETAction{
  override def getURL: String = ContextServerHttpConf.getContextIDURL
}

case class ContextHeartBeatAction(client:String) extends ContextPostAction {
  override def getURL: String = ContextServerHttpConf.heartBeatURL
}


case class ContextSearchContextAction() extends ContextPostAction{
  override def getURL: String = ContextServerHttpConf.searchURL
}

case class DefaultContextPostAction(url:String) extends ContextPostAction{
  // TODO:  类太多了,放一个default
  override def getURL: String = url
}