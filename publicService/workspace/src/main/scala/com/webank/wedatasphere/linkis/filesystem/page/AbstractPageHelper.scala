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

package com.webank.wedatasphere.linkis.filesystem.page

import java.util

/**
  * Created by johnnwang on 2019/4/17.
  */
abstract class AbstractPageHelper(bodyP: util.ArrayList[util.ArrayList[String]], paramsP: util.HashMap[String, String]) extends PageHelper {

  def commonInit(total: Int): Unit = {
    var pageNow = params.getOrDefault("page", "1").toInt
    var pageSize = params.getOrDefault("pageSize", "-1").toInt
    if (pageSize > total || pageSize < 0) pageSize = total
    totalPage = if (total % pageSize == 0) total / pageSize else total / pageSize + 1
    //pageNow > total should be no need to judge(pageNow > total应该是不需要判断的)
    if (pageNow < 0) pageNow = 1 else if (pageNow > totalPage) pageNow = totalPage
    fromIndex = pageSize * (pageNow - 1)
    toIndex = if (pageSize * pageNow - 1 > total - 1) total - 1 else pageSize * pageNow - 1
  }

  override var totalPage: Int = _
  override protected var body: util.ArrayList[util.ArrayList[String]] = bodyP
  override protected var fromIndex: Int = _
  override protected var toIndex: Int = _
  override protected var params: util.HashMap[String, String] = paramsP
  init
}
