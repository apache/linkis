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

package org.apache.linkis.engineplugin.spark.datacalc.util;

import org.apache.linkis.engineplugin.spark.datacalc.api.*;
import org.apache.linkis.engineplugin.spark.datacalc.model.SinkConfig;
import org.apache.linkis.engineplugin.spark.datacalc.model.SourceConfig;
import org.apache.linkis.engineplugin.spark.datacalc.model.TransformConfig;
import org.apache.linkis.engineplugin.spark.datacalc.sink.*;
import org.apache.linkis.engineplugin.spark.datacalc.source.*;
import org.apache.linkis.engineplugin.spark.datacalc.transform.*;
import org.apache.linkis.server.BDPJettyServerHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;

public class PluginUtil {

  private static final Map<String, Class<?>> SOURCE_PLUGINS = getSourcePlugins();
  private static final Map<String, Class<?>> TRANSFORM_PLUGINS = getTransformPlugins();
  private static final Map<String, Class<?>> SINK_PLUGINS = getSinkPlugins();

  private static Map<String, Class<?>> getSourcePlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    classMap.put("managed_jdbc", ManagedJdbcSource.class);
    classMap.put("jdbc", JdbcSource.class);
    classMap.put("file", FileSource.class);
    classMap.put("redis", RedisSource.class);
    classMap.put("datalake", DataLakeSource.class);
    classMap.put("rocketmq", RocketmqSource.class);
    classMap.put("mongo", MongoSource.class);
    classMap.put("elasticsearch", ElasticsearchSource.class);
    classMap.put("solr", SolrSource.class);
    classMap.put("kafka", KafkaSource.class);
    classMap.put("starrocks", StarrocksSource.class);
    return classMap;
  }

  private static Map<String, Class<?>> getTransformPlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    classMap.put("sql", SqlTransform.class);
    return classMap;
  }

  private static Map<String, Class<?>> getSinkPlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    classMap.put("managed_jdbc", ManagedJdbcSink.class);
    classMap.put("jdbc", JdbcSink.class);
    classMap.put("hive", HiveSink.class);
    classMap.put("file", FileSink.class);
    classMap.put("redis", RedisSink.class);
    classMap.put("datalake", DataLakeSink.class);
    classMap.put("rocketmq", RocketmqSink.class);
    classMap.put("mongo", MongoSink.class);
    classMap.put("elasticsearch", ElasticsearchSink.class);
    classMap.put("solr", SolrSink.class);
    classMap.put("kafka", KafkaSink.class);
    classMap.put("starrocks", StarrocksSink.class);
    return classMap;
  }

  public static <T extends SourceConfig> DataCalcSource<T> createSource(
      String name, JsonElement config)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    return createPlugin(SOURCE_PLUGINS, name, config);
  }

  public static <T extends TransformConfig> DataCalcTransform<T> createTransform(
      String name, JsonElement config)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    return createPlugin(TRANSFORM_PLUGINS, name, config);
  }

  public static <T extends SinkConfig> DataCalcSink<T> createSink(String name, JsonElement config)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    return createPlugin(SINK_PLUGINS, name, config);
  }

  static <T extends DataCalcPlugin> T createPlugin(
      Map<String, Class<?>> pluginMap, String name, JsonElement config)
      throws InstantiationException, IllegalAccessException, NoSuchMethodException,
          InvocationTargetException {
    Class<?> type = pluginMap.get(name);
    ParameterizedType genericSuperclass = (ParameterizedType) type.getGenericInterfaces()[0];
    Class<?> configType = (Class<?>) genericSuperclass.getActualTypeArguments()[0];
    T plugin = (T) type.getDeclaredConstructor().newInstance();
    plugin.setConfig(BDPJettyServerHelper.gson().fromJson(config, configType));
    return plugin;
  }
}
