
### 1. spark-on-k8s-operator官方文档

```text
https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/blob/master/docs/quick-start-guide.md
```


### 2. spark-on-k8s-operator部署

```text
helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

helm install my-release spark-operator/spark-operator --namespace spark-operator --create-namespace  
```

### 3. 如果遇到报错: Message: Forbidden!Configured service account doesn't have access. Service account may have been revoked. pods "spark-pi-driver" is forbidden: error looking up service account spark/spark: serviceaccount "spark" not found.

```text
kubectl create serviceaccount spark

kubectl create clusterrolebinding spark-role --clusterrole=edit --serviceaccount=default:spark --namespace=default
```

### 4. spark-on-k8s-operator测试任务提交

```text
kubectl apply -f examples/spark-pi.yaml
```



### 5. spark-on-k8s-operator卸载(一般不需要,安装出现问题再卸载)

```text
helm uninstall my-release  --namespace spark-operator

kubectl delete serviceaccounts my-release-spark-operator --namespace spark-operator

kubectl delete clusterrole my-release-spark-operator --namespace spark-operator

kubectl delete clusterrolebindings my-release-spark-operator --namespace spark-operator
```
