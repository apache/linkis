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

package com.webank.test

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Utils}
import com.webank.wedatasphere.linkis.resourcemanager.utils.YarnUtil
import com.webank.wedatasphere.linkis.resourcemanager.{DriverAndYarnResource, LoadInstanceResource, Resource, YarnResource}

/**
  * Created by johnnwang on 2018/12/29.
  */
object TestResource {

  def main(args: Array[String]): Unit = {
    val info = YarnUtil.getApplicationsInfo("q01")
    print(info)
  }

  //  def main(args: Array[String]): Unit = {
  //    val resource1 = new DriverAndYarnResource(new LoadInstanceResource(100,100,0),
  //      new YarnResource(101440 * 1024l * 1024l,50,2,"q05"))
  //    val resource2 = new DriverAndYarnResource(new LoadInstanceResource(50,50,0),
  //      new YarnResource(103424 * 1024l * 1024l,2,0,"q05"))
  //
  //    println((resource1 - resource2).toString)
  //  }
  //  def minus(resource1:Resource,resource2:Resource) = {
  //    resource1 - resource2
  //  }
}
