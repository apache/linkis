package org.apache.linkis.engineplugin.spark.client.deployment.crds;

public class DriverInfo {

  private String podName;
  private String webUIAddress;
  private String webUIPort;
  private String webUIServiceName;

  public String getWebUIServiceName() {
    return webUIServiceName;
  }

  public void setWebUIServiceName(String webUIServiceName) {
    this.webUIServiceName = webUIServiceName;
  }

  public String getWebUIPort() {
    return webUIPort;
  }

  public void setWebUIPort(String webUIPort) {
    this.webUIPort = webUIPort;
  }

  public String getWebUIAddress() {
    return webUIAddress;
  }

  public void setWebUIAddress(String webUIAddress) {
    this.webUIAddress = webUIAddress;
  }

  public String getPodName() {
    return podName;
  }

  public void setPodName(String podName) {
    this.podName = podName;
  }
}
