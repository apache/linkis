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

package org.apache.linkis.engineconnplugin.sqoop.client.config;

import org.apache.linkis.engineconnplugin.sqoop.context.SqoopParamsConfiguration;

import java.util.HashMap;
import java.util.Map;

/** Params mapping */
public final class ParamsMapping {
  public static Map<String, String> mapping;

  static {
    String paramPrefix = SqoopParamsConfiguration.SQOOP_PARAM_PREFIX().getValue();
    mapping = new HashMap<>();
    mapping.put(paramPrefix + "connect", "--connect");
    mapping.put(paramPrefix + "connection.manager", "--connection-manager");
    mapping.put(paramPrefix + "connection.param.file", "--connection-param-file");
    mapping.put(paramPrefix + "driver", "--driver");
    mapping.put(paramPrefix + "hadoop.home", "--hadoop-home");
    mapping.put(paramPrefix + "hadoop.mapred.home", "--hadoop-mapred-home");
    mapping.put(paramPrefix + "help", "help");
    mapping.put(paramPrefix + "password", "--password");
    mapping.put(paramPrefix + "password.alias", "--password-alias");
    mapping.put(paramPrefix + "password.file", "--password-file");
    mapping.put(paramPrefix + "relaxed.isolation", "--relaxed-isolation");
    mapping.put(paramPrefix + "skip.dist.cache", "--skip-dist-cache");
    mapping.put(paramPrefix + "username", "--username");
    mapping.put(paramPrefix + "verbose", "--verbose");
    mapping.put(paramPrefix + "append", "--append");
    mapping.put(paramPrefix + "as.avrodatafile", "--as-avrodatafile");
    mapping.put(paramPrefix + "as.parquetfile", "--as-parquetfile");
    mapping.put(paramPrefix + "as.sequencefile", "--as-sequencefile");
    mapping.put(paramPrefix + "as.textfile", "--as-textfile");
    mapping.put(paramPrefix + "autoreset.to.one.mapper", "--autoreset-to-one-mapper");
    mapping.put(paramPrefix + "boundary.query", "--boundary-query");
    mapping.put(paramPrefix + "case.insensitive", "--case-insensitive");
    mapping.put(paramPrefix + "columns", "--columns");
    mapping.put(paramPrefix + "compression.codec", "--compression-codec");
    mapping.put(paramPrefix + "delete.target.dir", "--delete-target-dir");
    mapping.put(paramPrefix + "direct", "--direct");
    mapping.put(paramPrefix + "direct.split.size", "--direct-split-size");
    mapping.put(paramPrefix + "query", "--query");
    mapping.put(paramPrefix + "fetch.size", "--fetch-size");
    mapping.put(paramPrefix + "inline.lob.limit", "--inline-lob-limit");
    mapping.put(paramPrefix + "num.mappers", "--num-mappers");
    mapping.put(paramPrefix + "mapreduce.job.name", "--mapreduce-job-name");
    mapping.put(paramPrefix + "merge.key", "--merge-key");
    mapping.put(paramPrefix + "split.by", "--split-by");
    mapping.put(paramPrefix + "table", "--table");
    mapping.put(paramPrefix + "target.dir", "--target-dir");
    mapping.put(paramPrefix + "validate", "--validate");
    mapping.put(paramPrefix + "validation.failurehandler", "--validation-failurehandler");
    mapping.put(paramPrefix + "validation.threshold", " --validation-threshold");
    mapping.put(paramPrefix + "validator", "--validator");
    mapping.put(paramPrefix + "warehouse.dir", "--warehouse-dir");
    mapping.put(paramPrefix + "where", "--where");
    mapping.put(paramPrefix + "compress", "--compress");
    mapping.put(paramPrefix + "check.column", "--check-column");
    mapping.put(paramPrefix + "incremental", "--incremental");
    mapping.put(paramPrefix + "last.value", "--last-value");
    mapping.put(paramPrefix + "enclosed.by", "--enclosed-by");
    mapping.put(paramPrefix + "escaped.by", "--escaped-by");
    mapping.put(paramPrefix + "fields.terminated.by", "--fields-terminated-by");
    mapping.put(paramPrefix + "lines.terminated.by", "--lines-terminated-by");
    mapping.put(paramPrefix + "mysql.delimiters", "--mysql-delimiters");
    mapping.put(paramPrefix + "optionally.enclosed.by", "--optionally-enclosed-by");
    mapping.put(paramPrefix + "input.enclosed.by", "--input-enclosed-by");
    mapping.put(paramPrefix + "input.escaped.by", "--input-escaped-by");
    mapping.put(paramPrefix + "input.fields.terminated.by", "--input-fields-terminated-by");
    mapping.put(paramPrefix + "input.lines.terminated.by", "--input-lines-terminated-by");
    mapping.put(paramPrefix + "input.optionally.enclosed.by", "--input-optionally-enclosed-by");
    mapping.put(paramPrefix + "create.hive.table", "--create-hive-table");
    mapping.put(paramPrefix + "hive.delims.replacement", "--hive-delims-replacement");
    mapping.put(paramPrefix + "hive.database", "--hive-database");
    mapping.put(paramPrefix + "hive.drop.import.delims", "--hive-drop-import-delims");
    mapping.put(paramPrefix + "hive.home", "--hive-home");
    mapping.put(paramPrefix + "hive.import", "--hive-import");
    mapping.put(paramPrefix + "hive.overwrite", "--hive-overwrite");
    mapping.put(paramPrefix + "hive.partition.value", "--hive-partition-value");
    mapping.put(paramPrefix + "hive.table", "--hive-table");
    mapping.put(paramPrefix + "column.family", "--column-family");
    mapping.put(paramPrefix + "hbase.bulkload", "--hbase-bulkload");
    mapping.put(paramPrefix + "hbase.create.table", "--hbase-create-table");
    mapping.put(paramPrefix + "hbase.row.key", "--hbase-row-key");
    mapping.put(paramPrefix + "hbase.table", "--hbase-table");
    mapping.put(paramPrefix + "hcatalog.database", "--hcatalog-database");
    mapping.put(paramPrefix + "hcatalog.home", "--hcatalog-home");
    mapping.put(paramPrefix + "hcatalog.partition.keys", "--hcatalog-partition-keys");
    mapping.put(paramPrefix + "hcatalog.partition.values", "--hcatalog-partition-values");
    mapping.put(paramPrefix + "hcatalog.table", "--hcatalog-table");
    mapping.put(paramPrefix + "hive.partition.key", "--hive-partition-key");
    mapping.put(paramPrefix + "map.column.hive", "--map-column-hive");
    mapping.put(paramPrefix + "create.hcatalog.table", "--create-hcatalog-table");
    mapping.put(paramPrefix + "hcatalog.storage.stanza", "--hcatalog-storage-stanza");
    mapping.put(paramPrefix + "accumulo.batch.size", "--accumulo-batch-size");
    mapping.put(paramPrefix + "accumulo.column.family", "--accumulo-column-family");
    mapping.put(paramPrefix + "accumulo.create.table", "--accumulo-create-table");
    mapping.put(paramPrefix + "accumulo.instance", "--accumulo-instance");
    mapping.put(paramPrefix + "accumulo.max.latency", "--accumulo-max-latency");
    mapping.put(paramPrefix + "accumulo.password", "--accumulo-password");
    mapping.put(paramPrefix + "accumulo.row.key", "--accumulo-row-key");
    mapping.put(paramPrefix + "accumulo.table", "--accumulo-table");
    mapping.put(paramPrefix + "accumulo.user", "--accumulo-user");
    mapping.put(paramPrefix + "accumulo.visibility", "--accumulo-visibility");
    mapping.put(paramPrefix + "accumulo.zookeepers", "--accumulo-zookeepers");
    mapping.put(paramPrefix + "bindir", "--bindir");
    mapping.put(paramPrefix + "class.name", "--class-name");
    mapping.put(paramPrefix + "input.null.non.string", "--input-null-non-string");
    mapping.put(paramPrefix + "input.null.string", "--input-null-string");
    mapping.put(paramPrefix + "jar.file", "--jar-file");
    mapping.put(paramPrefix + "map.column.java", "--map-column-java");
    mapping.put(paramPrefix + "null.non.string", "--null-non-string");
    mapping.put(paramPrefix + "null.string", "--null-string");
    mapping.put(paramPrefix + "outdir", "--outdir");
    mapping.put(paramPrefix + "package.name", "--package-name");
    mapping.put(paramPrefix + "conf", "-conf");
    mapping.put(paramPrefix + "D", "-D");
    mapping.put(paramPrefix + "fs", "-fs");
    mapping.put(paramPrefix + "jt", "-jt");
    mapping.put(paramPrefix + "files", "-files");
    mapping.put(paramPrefix + "libjars", "-libjars");
    mapping.put(paramPrefix + "archives", "-archives");
    mapping.put(paramPrefix + "update.key", "--update-key");
    mapping.put(paramPrefix + "update.mode", "--update-mode");
    mapping.put(paramPrefix + "export.dir", "--export-dir");
  }
}
