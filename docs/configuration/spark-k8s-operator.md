
### 1. spark-on-k8s-operator document

```text
https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/blob/master/docs/quick-start-guide.md
```


### 2. spark-on-k8s-operator install

```text
helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

helm install my-release spark-operator/spark-operator --namespace spark-operator --create-namespace  --set webhook.enable=true  
```

### 3. spark-on-k8s-operator test task submit

```text
kubectl apply -f examples/spark-pi.yaml
```

### 4. If an error is reported: Message: Forbidden!Configured service account doesn't have access. Service account may have been revoked. pods "spark-pi-driver" is forbidden: error looking up service account spark/spark: serviceaccount "spark" not found.

```text
kubectl create serviceaccount spark

kubectl create clusterrolebinding spark-role --clusterrole=edit --serviceaccount=default:spark --namespace=default
```

### 5. spark-on-k8s-operator Uninstall (usually not required, uninstall after installation problems)

```text
helm uninstall my-release  --namespace spark-operator

kubectl delete serviceaccounts my-release-spark-operator --namespace spark-operator

kubectl delete clusterrole my-release-spark-operator --namespace spark-operator

kubectl delete clusterrolebindings my-release-spark-operator --namespace spark-operator
```

### 6. Submitting tasks with Restful API
```text
POST /api/rest_j/v1/entrance/submit
```

```json
{
  "executionContent": {
    "spark.app.main.class": "org.apache.spark.examples.SparkPi",
    "spark.app.args": "spark.app.args",
    "runType": "jar",
    "code": "show databases"
  },
  "params": {
    "variable": {
    },
    "configuration": {
      "startup": {
        "spark.executor.memory": "1g",
        "spark.driver.memory": "1g",
        "spark.executor.cores": "1",
        "spark.app.name": "spark-submit-jar-cjtest",
        "spark.app.resource": "local:///opt/spark/examples/jars/spark-examples_2.12-3.2.1.jar",
        "spark.executor.instances": 1,
        "spark.master": "k8soperator",
        "spark.k8s.master.url": "http://ip:port",
        "spark.k8s.username": "username",
        "spark.k8s.password": "password"
      }
    }
  },
  "source":  {
    "scriptPath": "file:///tmp/hadoop/test.sql"
  },
  "labels": {
    "engineType": "spark-3.2.1",
    "engineConnMode": "once",
    "userCreator": "linkis-IDE"
  }
}
```

### 7. Reference document
```text
https://github.com/GoogleCloudPlatform/spark-on-k8s-operator

https://github.com/fabric8io/kubernetes-client/

https://github.com/apple/batch-processing-gateway

https://www.lightbend.com/blog/how-to-manage-monitor-spark-on-kubernetes-introduction-spark-submit-kubernetes-operator

https://www.lightbend.com/blog/how-to-manage-monitor-spark-on-kubernetes-deep-dive-kubernetes-operator-for-spark
```


