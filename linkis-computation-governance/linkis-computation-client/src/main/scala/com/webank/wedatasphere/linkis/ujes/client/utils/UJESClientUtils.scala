package com.webank.wedatasphere.linkis.ujes.client.utils

import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction.{EngineType, RunType}

/**
  * Created by johnnwang on 2019/9/21.
  */
object UJESClientUtils {
  def toEngineType(engineType: String): EngineType = engineType match {
    case "spark" => EngineType.SPARK
    case "hive" => EngineType.HIVE
    case "shell" => EngineType.SHELL
    case "python" => EngineType.PYTHON
    case _ => EngineType.SPARK
  }

  /**
    * TODO At first Judge engine type
    * @param runType
    * @param engineType
    * @return
    */
  def toRunType(runType:String, engineType: EngineType) : RunType = runType match {
    case "sql" => EngineType.SPARK.SQL
    case "pyspark" => EngineType.SPARK.PYSPARK
    case "scala" => EngineType.SPARK.SCALA
    case "r" => EngineType.SPARK.R
    case "hql" => EngineType.HIVE.HQL
    case "shell" => EngineType.SHELL.SH
    case "python" => EngineType.PYTHON.PY
    case _ => EngineType.SPARK.SQL
  }

}
