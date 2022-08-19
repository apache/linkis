package org.apache.linkis.engineplugin.vo;

public class EnginePluginBMLVo extends PageViewVo {
  private String engineConnType;
  private String engineConnVersion;

  public EnginePluginBMLVo(String engineConnType, String engineConnVersion) {
    this.engineConnType = engineConnType;
    this.engineConnVersion = engineConnVersion;
  }

  public String getEngineConnType() {
    return engineConnType;
  }

  public void setEngineConnType(String engineConnType) {
    this.engineConnType = engineConnType;
  }

  public String getEngineConnVersion() {
    return engineConnVersion;
  }

  public void setEngineConnVersion(String engineConnVersion) {
    this.engineConnVersion = engineConnVersion;
  }
}
