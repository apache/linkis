package org.apache.linkis.engineplugin.spark.client.deployment;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesHelper {
    public static KubernetesClient getKubernetesClientWithKubeConf(
      String k8sMasterUrl, String k8sClientCertData, String k8sClientKeyData) {
        Config config =
            new ConfigBuilder()
                .withMasterUrl(k8sMasterUrl)
                .withClientCertData(k8sClientCertData)
                .withClientCertData(k8sClientKeyData)
                .build();
        return new DefaultKubernetesClient(config);
    }
}
