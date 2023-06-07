package org.apache.linkis.engineplugin.spark.client.deployment;




import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.linkis.engineplugin.spark.client.deployment.crds.SparkApplication;
import org.apache.linkis.engineplugin.spark.client.deployment.crds.SparkApplicationList;

/**
 * @Author ChengJie
 * @Date 2023/6/7
 */
public class KubernetesHelper {

    public static KubernetesClient getKubernetesClient() {
        Config config = new ConfigBuilder().withMasterUrl("http://192.168.217.140:30880").withUsername("admin").withPassword("1QAZ2wsx").build();
        return new DefaultKubernetesClient(config);
    }

    public static NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> getSparkApplicationClient(KubernetesClient client) {
        return client.customResources(SparkApplication.class, SparkApplicationList.class);
    }

    public static SparkApplication getSparkApplication(String sparkOperatorName, String namespace) {
        SparkApplication sparkApplication = new SparkApplication();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(sparkOperatorName);
        metadata.setNamespace(namespace);
        sparkApplication.setMetadata(metadata);
        return sparkApplication;
    }
}
