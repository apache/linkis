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

package org.apache.linkis.engineconnplugin.flink.config

object FlinkExecutionTargetType {

  val YARN_PER_JOB: String = "yarn-per-job"
  val YARN_SESSION: String = "yarn-session"
  val YARN_APPLICATION: String = "yarn-application"

  val KUBERNETES_APPLICATION: String = "kubernetes-application"
  val KUBERNETES_SESSION: String = "kubernetes-session"
  val KUBERNETES_OPERATOR: String = "kubernetes-operator"

  def isYarnExecutionTargetType(targetType: String): Boolean = {
    targetType.equalsIgnoreCase(YARN_PER_JOB) || targetType.equalsIgnoreCase(
      YARN_SESSION
    ) || targetType.equalsIgnoreCase(YARN_APPLICATION)
  }

  def isKubernetesExecutionTargetType(targetType: String): Boolean = {
    targetType.equalsIgnoreCase(KUBERNETES_APPLICATION) || targetType.equalsIgnoreCase(
      KUBERNETES_SESSION
    ) || targetType.equalsIgnoreCase(KUBERNETES_OPERATOR)
  }

}
