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

package com.webank.wedatasphere.linkis.tispark.engine.hook

/**
  * Created by johnnwang on 2019/6/26.
  */
class TiSparkHook extends EngineHook with Logging {


  protected var initTiSparkCode: String = _

  @scala.throws[EngineErrorException]
  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    val db = params.get("spark.tispark.tidb.addr")
    if(StringUtils.isNotEmpty(db)) initTiSparkCode = s"""import org.apache.spark.sql.TiContext\n val ti = new TiContext(spark) \n ti.tidbMapDatabase("$db")"""
    params
  }

  @scala.throws[EngineErrorException]
  override def afterCreatedEngine(executor: EngineExecutor): Unit = {
    info(s"Start to init tiContext: $initTiSparkCode")
    executor.execute(new ExecuteRequest with RunTypeExecuteRequest {
      override val code: String = initTiSparkCode
      override val runType: String = "scala"
    })
    info("Finished to init tiContext")
  }
}
