package org.apache.linkis.engineplugin.spark.datacalc.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class DataCalcPluginConfig implements Serializable {

  protected Map<String, String> variables = new HashMap<>();

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> variables) {
    if (variables != null) this.variables = variables;
  }
}
