package org.apache.linkis.engineplugin.spark.datacalc.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public class DataCalcArrayData extends DataCalcPluginConfig implements Serializable {

  private DataCalcDataConfig[] plugins;

  public DataCalcDataConfig[] getPlugins() {
    return plugins;
  }

  public void setPlugins(DataCalcDataConfig[] plugins) {
    this.plugins = plugins;
  }

  public static DataCalcArrayData getData(String data) {
    return JSON.parseObject(data, DataCalcArrayData.class);
  }
}
