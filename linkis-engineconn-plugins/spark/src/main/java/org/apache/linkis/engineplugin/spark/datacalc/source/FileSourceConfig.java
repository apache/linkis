package org.apache.linkis.engineplugin.spark.datacalc.source;

import org.apache.linkis.engineplugin.spark.datacalc.model.SourceConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.util.Map;

public class FileSourceConfig extends SourceConfig {

  @NotBlank
  @Pattern(
      regexp = "^(file|hdfs)://.*",
      message =
          "Invalid path URI, please set the following allowed schemas: 'file://' or 'hdfs://'.")
  private String path;

  @NotBlank private String serializer = "parquet";

  private String[] columnNames;

  private Map<String, String> options;

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

  public String[] getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(String[] columnNames) {
    this.columnNames = columnNames;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
