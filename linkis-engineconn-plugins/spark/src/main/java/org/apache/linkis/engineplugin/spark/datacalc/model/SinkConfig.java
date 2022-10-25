package org.apache.linkis.engineplugin.spark.datacalc.model;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.AssertTrue;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class SinkConfig extends DataCalcPluginConfig implements Serializable {

  protected String sourceTable;

  protected String sourceQuery;

  public String getSourceTable() {
    return sourceTable;
  }

  public void setSourceTable(String sourceTable) {
    this.sourceTable = sourceTable;
  }

  public String getSourceQuery() {
    return sourceQuery;
  }

  public void setSourceQuery(String sourceQuery) {
    this.sourceQuery = sourceQuery;
  }

  @JSONField(serialize = false)
  @AssertTrue(message = "[sourceTable, sourceQuery] cannot be blank at the same time.")
  public boolean isSourceOK() {
    return StringUtils.isNotBlank(sourceTable) || StringUtils.isNotBlank(sourceQuery);
  }
}
