package org.apache.linkis.engineplugin.spark.datacalc.sink;

import org.apache.linkis.engineplugin.spark.datacalc.model.SinkConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSinkConfig extends SinkConfig {

  @NotBlank
  @Pattern(
      regexp = "^(file|hdfs)://.*",
      message =
          "Invalid path URI, please set the following allowed schemas: 'file://' or 'hdfs://'.")
  private String path;

  @NotBlank private String serializer = "parquet";

  private List<String> partitionBy = new ArrayList<>();

  @NotBlank
  @Pattern(
      regexp = "^(overwrite|append|ignore|error|errorifexists)$",
      message =
          "Unknown save mode: {saveMode}. Accepted save modes are 'overwrite', 'append', 'ignore', 'error', 'errorifexists'.")
  private String saveMode = "overwrite";

  private Map<String, String> options = new HashMap<>();

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getSerializer() {
    return serializer;
  }

  public void setSerializer(String serializer) {
    this.serializer = serializer;
  }

  public List<String> getPartitionBy() {
    return partitionBy;
  }

  public void setPartitionBy(List<String> partitionBy) {
    this.partitionBy = partitionBy;
  }

  public String getSaveMode() {
    return saveMode;
  }

  public void setSaveMode(String saveMode) {
    this.saveMode = saveMode;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
