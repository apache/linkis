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

package org.apache.linkis.udf.api.rpc

import org.apache.linkis.udf.entity.PythonModuleInfoVO

import scala.collection.JavaConverters._

class ResponsePythonModuleProtocol(val pythonModules: java.util.List[PythonModuleInfoVO])
    extends PythonModuleProtocol {

  // 如果PythonModuleProtocol需要实现某些方法，你可以在这里实现或覆盖它们
  // 例如，下面是一个假设的示例，展示如何可能实现或覆盖一个方法
  def getModulesInfo(): java.util.List[PythonModuleInfoVO] = {
    pythonModules
  }

}
