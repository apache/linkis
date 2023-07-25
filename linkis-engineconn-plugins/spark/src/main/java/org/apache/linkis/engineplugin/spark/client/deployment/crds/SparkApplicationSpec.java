/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.spark.client.deployment.crds;

import java.util.List;

import io.fabric8.kubernetes.api.model.KubernetesResource;

public class SparkApplicationSpec implements KubernetesResource {

  private String type;

  private String mode;

  private String image;

  private String imagePullPolicy;

  private String mainClass;

  private String mainApplicationFile;

  private String sparkVersion;

  private RestartPolicy restartPolicy;

  private List<Volume> volumes;

  private SparkPodSpec driver;

  private SparkPodSpec executor;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getImagePullPolicy() {
    return imagePullPolicy;
  }

  public void setImagePullPolicy(String imagePullPolicy) {
    this.imagePullPolicy = imagePullPolicy;
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  public String getMainApplicationFile() {
    return mainApplicationFile;
  }

  public void setMainApplicationFile(String mainApplicationFile) {
    this.mainApplicationFile = mainApplicationFile;
  }

  public String getSparkVersion() {
    return sparkVersion;
  }

  public void setSparkVersion(String sparkVersion) {
    this.sparkVersion = sparkVersion;
  }

  public RestartPolicy getRestartPolicy() {
    return restartPolicy;
  }

  public void setRestartPolicy(RestartPolicy restartPolicy) {
    this.restartPolicy = restartPolicy;
  }

  public List<Volume> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<Volume> volumes) {
    this.volumes = volumes;
  }

  public SparkPodSpec getDriver() {
    return driver;
  }

  public void setDriver(SparkPodSpec driver) {
    this.driver = driver;
  }

  public SparkPodSpec getExecutor() {
    return executor;
  }

  public void setExecutor(SparkPodSpec executor) {
    this.executor = executor;
  }

  @Override
  public String toString() {
    return "SparkApplicationSpec{"
        + "type='"
        + type
        + '\''
        + ", mode='"
        + mode
        + '\''
        + ", image='"
        + image
        + '\''
        + ", imagePullPolicy='"
        + imagePullPolicy
        + '\''
        + ", mainClass='"
        + mainClass
        + '\''
        + ", mainApplicationFile='"
        + mainApplicationFile
        + '\''
        + ", sparkVersion='"
        + sparkVersion
        + '\''
        + ", restartPolicy="
        + restartPolicy
        + ", volumes="
        + volumes
        + ", driver="
        + driver
        + ", executor="
        + executor
        + '}';
  }

  public static SparkApplicationSpecBuilder Builder() {
    return new SparkApplicationSpecBuilder();
  }

  public static class SparkApplicationSpecBuilder {
    private String type;
    private String mode;
    private String image;
    private String imagePullPolicy;
    private String mainClass;
    private String mainApplicationFile;
    private String sparkVersion;
    private RestartPolicy restartPolicy;
    private List<Volume> volumes;
    private SparkPodSpec driver;
    private SparkPodSpec executor;

    private SparkApplicationSpecBuilder() {}

    public SparkApplicationSpecBuilder type(String type) {
      this.type = type;
      return this;
    }

    public SparkApplicationSpecBuilder mode(String mode) {
      this.mode = mode;
      return this;
    }

    public SparkApplicationSpecBuilder image(String image) {
      this.image = image;
      return this;
    }

    public SparkApplicationSpecBuilder imagePullPolicy(String imagePullPolicy) {
      this.imagePullPolicy = imagePullPolicy;
      return this;
    }

    public SparkApplicationSpecBuilder mainClass(String mainClass) {
      this.mainClass = mainClass;
      return this;
    }

    public SparkApplicationSpecBuilder mainApplicationFile(String mainApplicationFile) {
      this.mainApplicationFile = mainApplicationFile;
      return this;
    }

    public SparkApplicationSpecBuilder sparkVersion(String sparkVersion) {
      this.sparkVersion = sparkVersion;
      return this;
    }

    public SparkApplicationSpecBuilder restartPolicy(RestartPolicy restartPolicy) {
      this.restartPolicy = restartPolicy;
      return this;
    }

    public SparkApplicationSpecBuilder volumes(List<Volume> volumes) {
      this.volumes = volumes;
      return this;
    }

    public SparkApplicationSpecBuilder driver(SparkPodSpec driver) {
      this.driver = driver;
      return this;
    }

    public SparkApplicationSpecBuilder executor(SparkPodSpec executor) {
      this.executor = executor;
      return this;
    }

    public SparkApplicationSpec build() {
      SparkApplicationSpec sparkApplicationSpec = new SparkApplicationSpec();
      sparkApplicationSpec.type = this.type;
      sparkApplicationSpec.mainClass = this.mainClass;
      sparkApplicationSpec.imagePullPolicy = this.imagePullPolicy;
      sparkApplicationSpec.volumes = this.volumes;
      sparkApplicationSpec.driver = this.driver;
      sparkApplicationSpec.sparkVersion = this.sparkVersion;
      sparkApplicationSpec.mode = this.mode;
      sparkApplicationSpec.mainApplicationFile = this.mainApplicationFile;
      sparkApplicationSpec.executor = this.executor;
      sparkApplicationSpec.image = this.image;
      sparkApplicationSpec.restartPolicy = this.restartPolicy;
      return sparkApplicationSpec;
    }
  }
}
