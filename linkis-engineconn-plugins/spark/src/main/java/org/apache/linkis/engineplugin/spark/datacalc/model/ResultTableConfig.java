package org.apache.linkis.engineplugin.spark.datacalc.model;

import java.io.Serializable;

public interface ResultTableConfig extends Serializable {
  String getResultTable();

  Boolean getPersist();

  String getStorageLevel();
}
