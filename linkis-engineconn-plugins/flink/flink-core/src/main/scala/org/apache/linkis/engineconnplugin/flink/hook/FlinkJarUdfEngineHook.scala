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

package org.apache.linkis.engineconnplugin.flink.hook

import org.apache.linkis.engineconn.computation.executor.hook.UDFLoadEngineConnHook
import org.apache.linkis.engineconnplugin.flink.client.utils.FlinkUdfUtils
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.udf.utils.ConstantVar
import org.apache.linkis.udf.vo.UDFInfoVo

import org.apache.commons.lang3.StringUtils

class FlinkJarUdfEngineHook extends UDFLoadEngineConnHook {
  override val udfType: BigInt = ConstantVar.UDF_JAR
  override val category: String = ConstantVar.UDF
  override val runType = RunType.SQL

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    val path: String = udfInfo.getPath
    val registerFormat: String = udfInfo.getRegisterFormat

    if (StringUtils.isBlank(path) && StringUtils.isBlank(registerFormat)) {
      logger.warn("Flink udfInfo path or registerFormat cannot is empty")
      return ""
    }

    val udfClassName: String = FlinkUdfUtils.extractUdfClass(registerFormat)
    if (StringUtils.isBlank(udfClassName)) {
      logger.warn("Flink extract udf class name cannot is empty")
      return ""
    }

    FlinkUdfUtils.loadJar(path)

    if (!FlinkUdfUtils.isFlinkUdf(ClassLoader.getSystemClassLoader(), udfClassName)) {
      logger.warn(
        "There is no extends Flink UserDefinedFunction, skip loading flink udf: {} ",
        path
      )
      return ""
    }

    val flinkUdfSql: String =
      FlinkUdfUtils.generateFlinkUdfSql(udfInfo.getUdfName, udfClassName)

    logger.info(
      s"Flink start load udf, udfName:${udfInfo.getUdfName}, udfJar:${path}, udfClass:${udfClassName}\n"
    )

    "%sql\n" + flinkUdfSql
  }

}
