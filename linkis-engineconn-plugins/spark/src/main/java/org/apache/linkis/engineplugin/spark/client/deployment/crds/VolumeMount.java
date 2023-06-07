package org.apache.linkis.engineplugin.spark.client.deployment.crds;



public class VolumeMount {

  private String name;

  private String mountPath;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMountPath() {
    return mountPath;
  }

  public void setMountPath(String mountPath) {
    this.mountPath = mountPath;
  }
}
