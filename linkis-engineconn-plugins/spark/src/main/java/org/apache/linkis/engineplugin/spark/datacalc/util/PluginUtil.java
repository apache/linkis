package org.apache.linkis.engineplugin.spark.datacalc.util;

import org.apache.linkis.engineplugin.spark.datacalc.api.*;
import org.apache.linkis.engineplugin.spark.datacalc.model.SinkConfig;
import org.apache.linkis.engineplugin.spark.datacalc.model.SourceConfig;
import org.apache.linkis.engineplugin.spark.datacalc.model.TransformConfig;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class PluginUtil {

  private static final Map<String, Class<?>> SOURCE_PLUGINS = getSourcePlugins();
  private static final Map<String, Class<?>> TRANSFORM_PLUGINS = getTransformPlugins();
  private static final Map<String, Class<?>> SINK_PLUGINS = getSinkPlugins();

  private static Map<String, Class<?>> getSourcePlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    // classMap.put("managed_jdbc",
    // org.apache.linkis.engineplugin.spark.datacalc.source.ManagedJdbcSource.class);
    classMap.put("jdbc", org.apache.linkis.engineplugin.spark.datacalc.source.JdbcSource.class);
    classMap.put("file", org.apache.linkis.engineplugin.spark.datacalc.source.FileSource.class);
    return classMap;
  }

  private static Map<String, Class<?>> getTransformPlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    classMap.put("sql", org.apache.linkis.engineplugin.spark.datacalc.transform.SqlTransform.class);
    return classMap;
  }

  private static Map<String, Class<?>> getSinkPlugins() {
    Map<String, Class<?>> classMap = new HashMap<>();
    // classMap.put("managed_jdbc",
    // org.apache.linkis.engineplugin.spark.datacalc.sink.ManagedJdbcSink.class);
    classMap.put("jdbc", org.apache.linkis.engineplugin.spark.datacalc.sink.JdbcSink.class);
    classMap.put("hive", org.apache.linkis.engineplugin.spark.datacalc.sink.HiveSink.class);
    classMap.put("file", org.apache.linkis.engineplugin.spark.datacalc.sink.FileSink.class);
    return classMap;
  }

  public static <T extends SourceConfig> DataCalcSource<T> createSource(
      String name, JSONObject config) throws InstantiationException, IllegalAccessException {
    return createPlugin(SOURCE_PLUGINS, name, config);
  }

  public static <T extends TransformConfig> DataCalcTransform<T> createTransform(
      String name, JSONObject config) throws InstantiationException, IllegalAccessException {
    return createPlugin(TRANSFORM_PLUGINS, name, config);
  }

  public static <T extends SinkConfig> DataCalcSink<T> createSink(String name, JSONObject config)
      throws InstantiationException, IllegalAccessException {
    return createPlugin(SINK_PLUGINS, name, config);
  }

  static <T extends DataCalcPlugin> T createPlugin(
      Map<String, Class<?>> pluginMap, String name, JSONObject config)
      throws InstantiationException, IllegalAccessException {
    Class<?> type = pluginMap.get(name);
    ParameterizedType genericSuperclass = (ParameterizedType) type.getGenericInterfaces()[0];
    Class<?> configType = (Class<?>) genericSuperclass.getActualTypeArguments()[0];
    T plugin = (T) type.newInstance();
    plugin.setConfig(config.toJavaObject(configType));
    return plugin;
  }
}
