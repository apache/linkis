kubectl delete -f register.yaml

kubectl delete -f linkis-gateway-configmap.yaml
kubectl delete -f linkis-metadata-configmap.yaml
kubectl delete -f linkis-publicservice-configmap.yaml
kubectl delete -f linkis-resourcemanager-configmap.yaml
kubectl delete -f linkis-bml-configmap.yaml
kubectl delete -f linkis-ujes-hive-enginemanager-configmap.yaml
kubectl delete -f linkis-ujes-hive-entrance-configmap.yaml
kubectl delete -f linkis-ujes-jdbc-entrance-configmap.yaml
kubectl delete -f linkis-ujes-mlsql-entrance-configmap.yaml
kubectl delete -f linkis-ujes-pipeline-enginemanager-configmap.yaml
kubectl delete -f linkis-ujes-pipeline-entrance-configmap.yaml
kubectl delete -f linkis-ujes-python-enginemanager-configmap.yaml
kubectl delete -f linkis-ujes-python-entrance-configmap.yaml
kubectl delete -f linkis-ujes-shell-enginemanager-configmap.yaml
kubectl delete -f linkis-ujes-shell-entrance-configmap.yaml
kubectl delete -f linkis-ujes-spark-entrance-configmap.yaml
kubectl delete -f linkis-ujes-spark-enginemanager-configmap.yaml

kubectl delete -f linkis-dsm-server-configmap.yaml
kubectl delete -f linkis-mdm-server-configmap.yaml
kubectl delete -f linkis-mdm-service-es-configmap.yaml
kubectl delete -f linkis-mdm-service-hive-configmap.yaml
kubectl delete -f linkis-mdm-service-mysql-configmap.yaml

kubectl delete -f linkis-gateway-deployment.yaml
kubectl delete -f linkis-bml-deployment.yaml
kubectl delete -f linkis-metadata-deployment.yaml
kubectl delete -f linkis-publicservice-deployment.yaml
kubectl delete -f linkis-resourcemanager-deployment.yaml
kubectl delete -f linkis-ujes-jdbc-entrance-deployment.yaml
kubectl delete -f linkis-ujes-hive-entrance-deployment.yaml
kubectl delete -f linkis-ujes-hive-enginemanager-deployment.yaml
kubectl delete -f linkis-ujes-mlsql-entrance-deployment.yaml
kubectl delete -f linkis-ujes-pipeline-entrance-deployment.yaml
kubectl delete -f linkis-ujes-pipeline-enginemanager-deployment.yaml
kubectl delete -f linkis-ujes-python-entrance-deployment.yaml
kubectl delete -f linkis-ujes-python-enginemanager-deployment.yaml
kubectl delete -f linkis-ujes-shell-entrance-deployment.yaml
kubectl delete -f linkis-ujes-shell-enginemanager-deployment.yaml
kubectl delete -f linkis-ujes-spark-entrance-deployment.yaml
kubectl delete -f linkis-ujes-spark-enginemanager-deployment.yaml

kubectl delete -f linkis-gateway-service.yaml

kubectl delete -f linkis-dsm-server-deployment.yaml
kubectl delete -f linkis-mdm-server-deployment.yaml
kubectl delete -f linkis-mdm-service-es-deployment.yaml
kubectl delete -f linkis-mdm-service-hive-deployment.yaml
kubectl delete -f linkis-mdm-service-mysql-deployment.yaml



