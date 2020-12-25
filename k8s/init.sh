kubectl create -f register.yaml

kubectl apply -f linkis-eureka-configmap.yaml

kubectl create -f linkis-gateway-configmap.yaml
kubectl create -f linkis-metadata-configmap.yaml
kubectl create -f linkis-publicservice-configmap.yaml
kubectl create -f linkis-resourcemanager-configmap.yaml
kubectl create -f linkis-cs-server-configmap.yaml
kubectl create -f linkis-bml-configmap.yaml
kubectl create -f linkis-ujes-hive-enginemanager-configmap.yaml
kubectl create -f linkis-ujes-hive-entrance-configmap.yaml
kubectl create -f linkis-ujes-jdbc-entrance-configmap.yaml
kubectl create -f linkis-ujes-mlsql-entrance-configmap.yaml
kubectl create -f linkis-ujes-pipeline-enginemanager-configmap.yaml
kubectl create -f linkis-ujes-pipeline-entrance-configmap.yaml
kubectl create -f linkis-ujes-python-enginemanager-configmap.yaml
kubectl create -f linkis-ujes-python-entrance-configmap.yaml
kubectl create -f linkis-ujes-shell-enginemanager-configmap.yaml
kubectl create -f linkis-ujes-shell-entrance-configmap.yaml
kubectl create -f linkis-ujes-spark-entrance-configmap.yaml
kubectl create -f linkis-ujes-spark-enginemanager-configmap.yaml

kubectl create -f linkis-dsm-server-configmap.yaml
kubectl create -f linkis-mdm-server-configmap.yaml
kubectl create -f linkis-mdm-service-es-configmap.yaml
kubectl create -f linkis-mdm-service-hive-configmap.yaml
kubectl create -f linkis-mdm-service-mysql-configmap.yaml


kubectl create -f linkis-gateway-deployment.yaml
kubectl create -f linkis-bml-deployment.yaml
kubectl create -f linkis-metadata-deployment.yaml
kubectl create -f linkis-publicservice-deployment.yaml
kubectl create -f linkis-resourcemanager-deployment.yaml
kubectl create -f linkis-cs-server-deployment.yaml
kubectl create -f linkis-ujes-jdbc-entrance-deployment.yaml
kubectl create -f linkis-ujes-hive-entrance-deployment.yaml
kubectl create -f linkis-ujes-hive-enginemanager-deployment.yaml
kubectl create -f linkis-ujes-mlsql-entrance-deployment.yaml
kubectl create -f linkis-ujes-pipeline-entrance-deployment.yaml
kubectl create -f linkis-ujes-pipeline-enginemanager-deployment.yaml
kubectl create -f linkis-ujes-python-entrance-deployment.yaml
kubectl create -f linkis-ujes-python-enginemanager-deployment.yaml
kubectl create -f linkis-ujes-shell-entrance-deployment.yaml
kubectl create -f linkis-ujes-shell-enginemanager-deployment.yaml
kubectl create -f linkis-ujes-spark-entrance-deployment.yaml
kubectl create -f linkis-ujes-spark-enginemanager-deployment.yaml

kubectl create -f linkis-gateway-service.yaml


kubectl create -f linkis-dsm-server-deployment.yaml
kubectl create -f linkis-mdm-server-deployment.yaml
kubectl create -f linkis-mdm-service-es-deployment.yaml
kubectl create -f linkis-mdm-service-hive-deployment.yaml
kubectl create -f linkis-mdm-service-mysql-deployment.yaml

