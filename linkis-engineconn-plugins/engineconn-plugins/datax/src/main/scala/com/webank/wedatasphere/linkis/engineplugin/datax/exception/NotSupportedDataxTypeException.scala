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

package com.webank.wedatasphere.linkis.engineplugin.datax.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

case class DataxSessionStartFailedException(erroCode:Int,
                                            desc:String) extends ErrorException(erroCode, desc){

}

case class DataxCodeErrorException() extends ErrorException(80037, "datax shell code is wrong")
case class DataxResouceNotFound() extends ErrorException(80038, "datax resource not find")