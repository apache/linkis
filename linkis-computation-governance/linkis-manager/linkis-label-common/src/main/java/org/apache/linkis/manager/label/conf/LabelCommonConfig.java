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

package org.apache.linkis.manager.label.conf;

import org.apache.linkis.common.conf.CommonVars;

public class LabelCommonConfig {

  public static final CommonVars<String> LABEL_FACTORY_CLASS =
      CommonVars.apply("wds.linkis.label.factory.clazz", "");

  public static final CommonVars<Double> LABEL_SCORER_BASE_CORE =
      CommonVars.apply("wds.linkis.label.scorer.base.core", 1.0d);

  public static final CommonVars<Integer> LABEL_SCORER_RELATE_LIMIT =
      CommonVars.apply("wds.linkis.label.scorer.relate.limit", 2);

  public static final CommonVars<String> LABEL_ENTITY_PACKAGES =
      CommonVars.apply("wds.linkis.label.entity.packages", "");

  public static final CommonVars<String> SPARK_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.spark.engine.version", "3.2.1");
  public static final String SPARK3_ENGINE_VERSION_CONF = "sparkVersion";
  public static final String SPARK_ENGINE_HOME_CONF = "SPARK_HOME";
  public static final String SPARK_ENGINE_CMD_CONF = "SPARK_CMD";
  public static final String SPARK_ENGINE_PATH_CONF = "PATH";
  public static final String SPARK_ENGINE_CONF_DIR = "SPARK_CONF_DIR";
  public static final CommonVars<String> SPARK3_ENGINE_VERSION =
      CommonVars.apply("linkis.spark3.engine.version", "3.4.4");

  public static final CommonVars<String> SPARK_ENGINE_HOME =
      CommonVars.apply("linkis.spark.engine.home", "/appcom/Install/spark");
  public static final CommonVars<String> SPARK3_ENGINE_HOME =
      CommonVars.apply("linkis.spark3.engine.home", "/appcom/Install/spark3");
  public static final CommonVars<Boolean> USER_DEFAULT_SPAKR_SWITCH =
      CommonVars.apply("linkis.user.default.spark3.switch", false);
  public static final CommonVars<String> SPARK3_ENGINE_CMD =
      CommonVars.apply("linkis.spark3.engine.cmd", "/appcom/Install/spark3-cmd");
  public static final CommonVars<String> SPARK_ENGINE_CMD =
      CommonVars.apply("linkis.spark.engine.cmd", "/appcom/Install/spark-cmd");
  public static final CommonVars<String> SPARK3_ENGINE_PATH =
      CommonVars.apply("linkis.spark3.engine.path", "$SPARK_CMD/bin:$PATH");
  public static final CommonVars<String> SPARK_ENGINE_PATH =
      CommonVars.apply("linkis.spark.engine.path", "$SPARK_CMD/bin:$PATH");

  public static final CommonVars<String> SPARK3_ENGINE_CONFIG =
      CommonVars.apply("linkis.spark3.engine.config", "/appcom/config/spark3-config/spark-submit");

  public static final CommonVars<String> SPARK_ENGINE_CONFIG =
      CommonVars.apply("linkis.spark.engine.config", "/appcom/config/spark-config/spark-submit");
  public static final CommonVars<String> HIVE_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.hive.engine.version", "3.1.3");

  public static final CommonVars<String> PYTHON_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.python.engine.version", "python2");

  public static final CommonVars<String> REPL_ENGINE_VERSION =
      CommonVars.apply("linkis.repl.engine.version", "1");

  public static final CommonVars<String> FILE_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.file.engine.version", "1.0");

  public static final CommonVars<String> HDFS_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.file.engine.version", "1.0");

  public static final CommonVars<String> JDBC_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.jdbc.engine.version", "4");

  public static final CommonVars<String> PIPELINE_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.pipeline.engine.version", "1");

  public static final CommonVars<String> SHELL_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.shell.engine.version", "1");

  public static final CommonVars<String> APPCONN_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.appconn.engine.version", "1");

  public static final CommonVars<String> FLINK_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.flink.engine.version", "1.16.2");

  public static final CommonVars<String> SQOOP_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.sqoop.engine.version", "1.4.6");

  public static final CommonVars<String> DATAX_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.datax.engine.version", "3.0.0");

  public static final CommonVars<String> NEBULA_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.nebula.engine.version", "3.0.0");

  public static final CommonVars<String> DORIS_ENGINE_VERSION =
      CommonVars.apply("linkis.doris.engine.version", "1.2.6");

  public static final CommonVars<String> PRESTO_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.presto.engine.version", "0.234");

  public static final CommonVars<String> HBASE_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.hbase.engine.version", "2.5.3");

  public static final CommonVars<String> OPENLOOKENG_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.openlookeng.engine.version", "1.5.0");

  public static final CommonVars<String> PERMANENT_LABEL =
      CommonVars.apply("wds.linkis.am.permanent.label", "tenant");

  public static final CommonVars<String> ENGINE_CONN_MANAGER_SPRING_NAME =
      CommonVars.apply("wds.linkis.engineconn.manager.name", "linkis-cg-engineconnmanager");

  public static final CommonVars<String> ENGINE_CONN_SPRING_NAME =
      CommonVars.apply("wds.linkis.engineconn.name", "linkis-cg-engineconn");

  public static final CommonVars<String> TRINO_ENGINE_CONN_VERSION =
      CommonVars.apply("wds.linkis.trino.engineconn.version", "371");

  public static final CommonVars<String> ELASTICSEARCH_ENGINE_VERSION =
      CommonVars.apply("wds.linkis.elasticsearch.engine.version", "7.6.2");

  public static final CommonVars<String> SEATUNNEL_ENGINE_CONN_VERSION =
      CommonVars.apply("linkis.seatunnel.engineconn.version", "2.1.2");
}
