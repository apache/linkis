package org.apache.linkis.engineplugin.spark.datacalc.sink;

import org.apache.linkis.engineplugin.spark.datacalc.model.SinkConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.util.HashMap;
import java.util.Map;

public class HiveSinkConfig extends SinkConfig {

  @NotBlank private String targetDatabase;

  @NotBlank private String targetTable;

  @NotBlank
  @Pattern(
      regexp = "^(overwrite|append|ignore|error|errorifexists)$",
      message =
          "Unknown save mode: {saveMode}. Accepted save modes are 'overwrite', 'append', 'ignore', 'error', 'errorifexists'.")
  private String saveMode = "overwrite";

  private Boolean strongCheck = true;

  private Boolean writeAsFile = false;

  private Integer numPartitions = 10;

  private Map<String, String> options = new HashMap<>();

  public String getTargetDatabase() {
    return targetDatabase;
  }

  public void setTargetDatabase(String targetDatabase) {
    this.targetDatabase = targetDatabase;
  }

  public String getTargetTable() {
    return targetTable;
  }

  public void setTargetTable(String targetTable) {
    this.targetTable = targetTable;
  }

  public String getSaveMode() {
    return saveMode;
  }

  public void setSaveMode(String saveMode) {
    this.saveMode = saveMode;
  }

  public Boolean getStrongCheck() {
    return strongCheck;
  }

  public void setStrongCheck(Boolean strongCheck) {
    this.strongCheck = strongCheck;
  }

  public Boolean getWriteAsFile() {
    return writeAsFile;
  }

  public void setWriteAsFile(Boolean writeAsFile) {
    this.writeAsFile = writeAsFile;
  }

  public Integer getNumPartitions() {
    return numPartitions;
  }

  public void setNumPartitions(Integer numPartitions) {
    if (numPartitions == null) return;
    this.numPartitions = numPartitions > 20 ? 20 : numPartitions;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
