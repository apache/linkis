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
package com.webank.wedatasphere.linkis.engineplugin.presto.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
 * Created by yogafire on 2020/5/14
 */
case class PrestoStateInvalidException(message: String) extends ErrorException(60011, message: String)

case class PrestoClientException(message: String) extends ErrorException(60012, message: String)

case class PrestoSourceGroupException(message: String) extends ErrorException(60013, message: String)
