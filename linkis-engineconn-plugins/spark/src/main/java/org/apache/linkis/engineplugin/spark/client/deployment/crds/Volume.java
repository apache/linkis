
package org.apache.linkis.engineplugin.spark.client.deployment.crds;



public class Volume {

  private String name;

  private HostPath hostPath;

  private ConfigMap configMap;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public HostPath getHostPath() {
    return hostPath;
  }

  public void setHostPath(HostPath hostPath) {
    this.hostPath = hostPath;
  }

  public ConfigMap getConfigMap() {
    return configMap;
  }

  public void setConfigMap(ConfigMap configMap) {
    this.configMap = configMap;
  }
}
