package org.apache.linkis.engineplugin.spark.client.deployment.crds;



public class RestartPolicy {

  private String type;

  public RestartPolicy(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
