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

package com.webank.wedatasphere.linkis.engine.configuration

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}

/**
  * Created by allenlliu on 2019/4/8.
  */
object SparkConfiguration {
  val MAPRED_OUTPUT_COMPRESS = CommonVars[String]("mapred.output.compress", "true", "map输出结果是否压缩")
  val MAPRED_OUTPUT_COMPRESSION_CODEC = CommonVars[String]("mapred.output.compression.codec", "org.apache.hadoop.io.compress.GzipCodec", "map输出结果压缩方式")
  val SPARK_MASTER = CommonVars[String]("spark.master", "yarn", "默认master")
  val SPARK_OUTPUTDIR = CommonVars[String]("spark.outputDir", "/home/georgeqiao", "默认输出路径")
  val SPARK_OUTPUT_RESULT_DIR = CommonVars[String]("spark.result.dir", "hdfs:///user/", "默认输出路径")
  val DWC_SPARK_USEHIVECONTEXT = CommonVars[Boolean]("wds.linkis.spark.useHiveContext", true)
  val SPARK_LANGUAGE_REPL_INIT_TIME = CommonVars[TimeType]("wds.linkis.engine.spark.language-repl.init.time", new TimeType("30s"))
  val SPARK_REPL_CLASSDIR = CommonVars[String]("spark.repl.classdir", "/tmp", "默认master")
  val SPARK_HOME = CommonVars[String]("spark.home", CommonVars[String]("SPARK_HOME", "/appcom/Install/spark").getValue)
  val PROCESS_MAX_THREADS = CommonVars[Int]("wds.linkis.process.threadpool.max", 100)
  val SPARK_NF_FRACTION_LENGTH = CommonVars[Int]("wds.linkis.engine.spark.fraction.length", 30)
  val SPARK_CONSOLE_OUTPUT_NUM = CommonVars[Int]("wds.linkis.spark.output.line.limit", 10)
  val DOLPHIN_LIMIT_LEN = CommonVars("wds.linkis.dolphin.limit.len",5000)
  val MDQ_APPLICATION_NAME = CommonVars("wds.linkis.mdq.application.name", "cloud-datasource")
  val SHOW_DF_MAX_RES = CommonVars("wds.linkis.show.df.max.res",Int.MaxValue)
}
