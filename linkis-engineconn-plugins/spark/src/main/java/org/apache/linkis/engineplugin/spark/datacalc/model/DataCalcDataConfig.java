package org.apache.linkis.engineplugin.spark.datacalc.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class DataCalcDataConfig implements Serializable {

  private String type;
  private String name;
  private JSONObject config;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JSONObject getConfig() {
    return config;
  }

  public void setConfig(JSONObject config) {
    this.config = config;
  }
}
