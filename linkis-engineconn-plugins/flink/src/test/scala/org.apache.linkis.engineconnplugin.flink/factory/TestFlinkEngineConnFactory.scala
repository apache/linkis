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

package org.apache.linkis.engineconnplugin.flink.factory

import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration.{FLINK_CONF_DIR, FLINK_CONF_YAML, FLINK_ENV_JAVA_OPTS}
import org.apache.linkis.engineconnplugin.flink.util.FlinkValueFormatUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml

import java.io.{File, FileNotFoundException}
import java.util
import scala.io.Source

class TestFlinkEngineConnFactory {

  @Test
  private  def getExtractJavaOpts(envJavaOpts: String): String = {
    var defaultJavaOpts = ""
    val yamlFilePath = FLINK_CONF_DIR.getValue
    val yamlFile = yamlFilePath + "/" + FLINK_CONF_YAML.getHotValue()
    if (new File(yamlFile).exists()) {
      val source = Source.fromFile(yamlFile)
      try {
        val yamlContent = source.mkString
        val yaml = new Yaml()
        val configMap = yaml.loadAs(yamlContent, classOf[util.LinkedHashMap[String, Object]])
        if (configMap.containsKey(FLINK_ENV_JAVA_OPTS.getValue)) {
          defaultJavaOpts = configMap.get(FLINK_ENV_JAVA_OPTS.getValue).toString
        }
      } finally {
        source.close()
      }
    } else {
      val inputStream = getClass.getResourceAsStream(yamlFile)
      if (inputStream != null) {
        val source = Source.fromInputStream(inputStream)
        try {
          val yamlContent = source.mkString
          val yaml = new Yaml()
          val configMap = yaml.loadAs(yamlContent, classOf[util.LinkedHashMap[String, Object]])
          if (configMap.containsKey(FLINK_ENV_JAVA_OPTS.getValue)) {
            defaultJavaOpts = configMap.get(FLINK_ENV_JAVA_OPTS.getValue).toString
          }
        } finally {
          source.close()
        }
      } else {
        throw new FileNotFoundException("YAML file not found in both file system and classpath.")
      }
    }
    val merged = FlinkValueFormatUtil.mergeAndDeduplicate(defaultJavaOpts, envJavaOpts)
    merged
  }

  @Test
  def testMergeAndDeduplicate: Unit = {
    val defaultJavaOpts = "-Da=3 -Db=4 -XXc=5 -Dk=a1=b";
    val envJavaOpts = "-DjobName=0607_1 -Dlog4j.configuration=./log4j.properties -Da=1 -Dk=a1=c";
    val merged = FlinkValueFormatUtil.mergeAndDeduplicate(defaultJavaOpts, envJavaOpts)
    assertEquals("-Da=1 -Db=4 -XXc=5 -Dk=a1=c -DjobName=0607_1 -Dlog4j.configuration=./log4j.properties", merged)
  }
}
