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

<<<<<<< HEAD:linkis-extensions/linkis-et-monitor/src/main/scala/org/apache/linkis/monitor/response/KeyvalueResult.scala
package org.apache.linkis.monitor.response

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/configuration/keyvalue")
class KeyvalueResult extends DWSResult {

  @BeanProperty
  var configValues: util.ArrayList[util.Map[String, AnyRef]] = _

  @BeanProperty
  var totalPage: Int = _

=======
package org.apache.linkis.engineplugin.spark.client.deployment.crds;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(SparkApplication.VERSION)
@Group(SparkApplication.GROUP)
@Kind(SparkApplication.Kind)
public class SparkApplication extends CustomResource<SparkApplicationSpec, SparkApplicationStatus>
    implements Namespaced {
  public static final String GROUP = "sparkoperator.k8s.io";
  public static final String VERSION = "v1beta2";

  public static final String Kind = "SparkApplication";
>>>>>>> origin/dev-1.9.0-webank-bak:linkis-engineconn-plugins/spark/src/main/java/org/apache/linkis/engineplugin/spark/client/deployment/crds/SparkApplication.java
}
