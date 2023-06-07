package org.apache.linkis.engineplugin.spark.client.deployment.crds;


public class ApplicationState {

  private String state;
  private String errorMessage;

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
