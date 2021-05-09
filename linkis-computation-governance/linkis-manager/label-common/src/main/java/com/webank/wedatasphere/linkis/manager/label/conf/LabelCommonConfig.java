/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.label.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

public class LabelCommonConfig {

    public final static CommonVars<String> LABEL_FACTORY_CLASS = CommonVars.apply("wds.linkis.label.label.com.webank.wedatasphere.linkis.entrance.factory.clazz", "");

    public final static CommonVars<Double> LABEL_SCORER_BASE_CORE = CommonVars.apply("wds.linkis.label.scorer.base.core", 1.0d);

    public final static CommonVars<Integer> LABEL_SCORER_RELATE_LIMIT = CommonVars.apply("wds.linkis.label.scorer.relate.limit", 2);

    public final static CommonVars<String> LABEL_ENTITY_PACKAGES = CommonVars.apply("wds.linkis.label.entity.packages", "");

    public final static CommonVars<String> SPARK_ENGINE_VERSION = CommonVars.apply("wds.linkis.spark.engine.version", "2.4.3");

    public final static CommonVars<String> HIVE_ENGINE_VERSION = CommonVars.apply("wds.linkis.hive.engine.version", "1.2.1");

    public final static CommonVars<String> PYTHON_ENGINE_VERSION = CommonVars.apply("wds.linkis.python.engine.version", "python2");

    public final static CommonVars<String> FILE_ENGINE_VERSION = CommonVars.apply("wds.linkis.file.engine.version", "1.0");

    public final static CommonVars<String> HDFS_ENGINE_VERSION = CommonVars.apply("wds.linkis.file.engine.version", "1.0");

    public final static CommonVars<String> JDBC_ENGINE_VERSION = CommonVars.apply("wds.linkis.jdbc.engine.version", "4");

    public final static CommonVars<String> PIPELINE_ENGINE_VERSION = CommonVars.apply("wds.linkis.pipeline.engine.version", "1");

    public final static CommonVars<String> SHELL_ENGINE_VERSION = CommonVars.apply("wds.linkis.shell.engine.version", "1");

    public final static CommonVars<String> ELASTICSEARCH_ENGINE_VERSION = CommonVars.apply("wds.linkis.elasticsearch.engine.version", "7.6.2");

    public final static CommonVars<String> PRESTO_ENGINE_VERSION = CommonVars.apply("wds.linkis.presto.engine.version", "0.234");

    public final static CommonVars<String> APPCONN_ENGINE_VERSION = CommonVars.apply("wds.linkis.appconn.engine.version", "1");
}
