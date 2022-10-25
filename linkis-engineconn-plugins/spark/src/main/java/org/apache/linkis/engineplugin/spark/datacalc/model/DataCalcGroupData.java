package org.apache.linkis.engineplugin.spark.datacalc.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public class DataCalcGroupData extends DataCalcPluginConfig implements Serializable {

  private DataCalcDataConfig[] sources;
  private DataCalcDataConfig[] transformations;
  private DataCalcDataConfig[] sinks;

  public DataCalcDataConfig[] getSources() {
    return sources;
  }

  public void setSources(DataCalcDataConfig[] sources) {
    this.sources = sources;
  }

  public DataCalcDataConfig[] getTransformations() {
    return transformations;
  }

  public void setTransformations(DataCalcDataConfig[] transformations) {
    this.transformations = transformations;
  }

  public DataCalcDataConfig[] getSinks() {
    return sinks;
  }

  public void setSinks(DataCalcDataConfig[] sinks) {
    this.sinks = sinks;
  }

  public static DataCalcGroupData getData(String data) {
    return JSON.parseObject(data, DataCalcGroupData.class);
  }
}
