package org.apache.linkis.engineplugin.spark.datacalc.model;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

import java.io.Serializable;

public abstract class TransformConfig extends DataCalcPluginConfig
    implements ResultTableConfig, Serializable {

  protected String sourceTable;

  @NotBlank protected String resultTable;

  private Boolean persist = false;

  private String storageLevel = "MEMORY_AND_DISK";

  public String getSourceTable() {
    return sourceTable;
  }

  public void setSourceTable(String sourceTable) {
    this.sourceTable = sourceTable;
  }

  public String getResultTable() {
    return resultTable;
  }

  public void setResultTable(String resultTable) {
    this.resultTable = resultTable;
  }

  public Boolean getPersist() {
    return persist;
  }

  public void setPersist(Boolean persist) {
    this.persist = persist;
  }

  public String getStorageLevel() {
    return storageLevel;
  }

  public void setStorageLevel(String storageLevel) {
    if (StringUtils.isNotBlank(storageLevel)) this.storageLevel = storageLevel;
  }
}
