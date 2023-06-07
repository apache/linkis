package org.apache.linkis.engineplugin.spark.client.deployment.crds;


import java.util.Map;

public class SparkApplicationStatus {

  private String sparkApplicationId;
  private String terminationTime;
  private String lastSubmissionAttemptTime;
  private String submissionID;
  private ApplicationState applicationState;
  private Integer executionAttempts;
  private Integer submissionAttempts;
  private DriverInfo driverInfo;
  private Map<String, String> executorState;

  public String getSubmissionID() {
    return submissionID;
  }

  public void setSubmissionID(String submissionID) {
    this.submissionID = submissionID;
  }

  public Integer getSubmissionAttempts() {
    return submissionAttempts;
  }

  public void setSubmissionAttempts(Integer submissionAttempts) {
    this.submissionAttempts = submissionAttempts;
  }

  public String getLastSubmissionAttemptTime() {
    return lastSubmissionAttemptTime;
  }

  public void setLastSubmissionAttemptTime(String lastSubmissionAttemptTime) {
    this.lastSubmissionAttemptTime = lastSubmissionAttemptTime;
  }

  public String getSparkApplicationId() {
    return sparkApplicationId;
  }

  public void setSparkApplicationId(String sparkApplicationId) {
    this.sparkApplicationId = sparkApplicationId;
  }

  public String getTerminationTime() {
    return terminationTime;
  }

  public void setTerminationTime(String terminationTime) {
    this.terminationTime = terminationTime;
  }

  public ApplicationState getApplicationState() {
    return applicationState;
  }

  public void setApplicationState(ApplicationState applicationState) {
    this.applicationState = applicationState;
  }

  public Integer getExecutionAttempts() {
    return executionAttempts;
  }

  public void setExecutionAttempts(Integer executionAttempts) {
    this.executionAttempts = executionAttempts;
  }

  public DriverInfo getDriverInfo() {
    return driverInfo;
  }

  public void setDriverInfo(DriverInfo driverInfo) {
    this.driverInfo = driverInfo;
  }

  public Map<String, String> getExecutorState() {
    return executorState;
  }

  public void setExecutorState(Map<String, String> executorState) {
    this.executorState = executorState;
  }
}
