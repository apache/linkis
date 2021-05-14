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

package com.webank.wedatasphere.linkis.engineplugin.hive.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

case class NotSupportedHiveTypeException(errCode: Int,
                                         desc: String) extends ErrorException(errCode, desc) {

}

case class HadoopConfSetFailedException(errCode:Int,
                                         desc:String) extends ErrorException(errCode, desc) {

}


case class HiveSessionStartFailedException(erroCode:Int,
                                           desc:String) extends ErrorException(erroCode, desc){

}

/**
  *
  * @param erroCode 41004
  * @param desc hive query fail
  */
case class HiveQueryFailedException(erroCode:Int,
                                    desc:String) extends ErrorException(erroCode, desc){

}