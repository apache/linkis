package org.apache.linkis.engineplugin.spark.client.deployment.crds;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(SparkApplication.VERSION)
@Group(SparkApplication.GROUP)
@Kind(SparkApplication.Kind)
public class SparkApplication extends CustomResource<SparkApplicationSpec, SparkApplicationStatus> implements Namespaced {
  public static final String GROUP = "sparkoperator.k8s.io";
  public static final String VERSION = "v1beta2";

  public static final String Kind = "SparkApplication";


}
