pipeline {
  agent {
    node {
      label 'node2'
    }

  }
  stages {
    stage('build package') {
      steps {
        echo 'start build'
        sh '''source /etc/profile
mvn -N install
mvn clean install -DskipTests
'''
        sh 'cp -r k8s/jars assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package/'
      }
    }

    stage('build image') {
      steps {
        echo 'Build Docker Image Stage'
        sh '''docker build -f k8s/Dockerfile/linkis-mg-gateway.Dockerfile -t linkis-mg-gateway:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-mg-eureka.Dockerfile -t linkis-mg-gateway:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-ps-publicservice.Dockerfile -t linkis-ps-publicservice:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-ps-cs.Dockerfile -t linkis-ps-cs:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-cg-linkismanager.Dockerfile -t linkis-cg-linkismanager:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-cg-entrance.Dockerfile -t linkis-cg-entrance:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-cg-engineplugin.Dockerfile -t linkis-cg-engineplugin:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package

docker build -f k8s/Dockerfile/linkis-cg-engineconnmanager.Dockerfile -t linkis-cg-engineconnmanager:1.2 ./assembly-combined-package/target/apache-linkis-1.1.0-incubating-bin/linkis-package


















'''
        sh '''cd web
sed -i \'/VUE_APP_MN_CONFIG_PREFIX/d\' .env
yarn
yarn build
cd ..
docker build -f k8s/Dockerfile/linkis-web.Dockerfile -t linkis-web:1.2.0 .

'''
      }
    }

    stage('push image') {
      steps {
        sh '''docker tag linkis-mg-gateway:1.2.0 registry.mydomain.com/library/linkis-mg-gateway:1.2.0
docker push registry.mydomain.com/library/linkis-mg-gateway:1.2.0

docker tag linkis-mg-eureka:1.2.0 registry.mydomain.com/library/linkis-mg-eureka:1.2.0
docker push registry.mydomain.com/library/linkis-mg-eureka:1.2.0

docker tag linkis-ps-publicservice:1.2.0 registry.mydomain.com/library/linkis-ps-publicservice:1.2.0
docker push registry.mydomain.com/library/linkis-ps-publicservice:1.2.0

docker tag linkis-ps-cs:1.2.0 registry.mydomain.com/library/linkis-ps-cs:1.2.0
docker push registry.mydomain.com/library/linkis-ps-cs:1.2.0

docker tag linkis-cg-linkismanager:1.2.0 registry.mydomain.com/library/linkis-cg-linkismanager:1.2.0
docker push registry.mydomain.com/library/linkis-cg-linkismanager:1.2.0

docker tag linkis-cg-entrance:1.2.0 registry.mydomain.com/library/linkis-cg-entrance:1.2.0
docker push registry.mydomain.com/library/linkis-cg-entrance:1.2.0

docker tag linkis-cg-engineplugin:1.2.0 registry.mydomain.com/library/linkis-cg-engineplugin:1.2.0
docker push registry.mydomain.com/library/linkis-cg-engineplugin:1.2.0

docker tag linkis-cg-engineconnmanager:1.2.0 registry.mydomain.com/library/linkis-cg-engineconnmanager:1.2.0
docker push registry.mydomain.com/library/linkis-cg-engineconnmanager

docker tag linkis-web:1.2.0 registry.mydomain.com/library/linkis-web:1.2.0
docker push registry.mydomain.com/library/linkis-web:1.2.0'''
      }
    }

    stage('deploy on k8s') {
      steps {
        echo 'deploy on k8s'
        sh '''kubectl apply -f k8s/yaml/configmap/linkis-configmap.yaml --namespace=preprod
kubectl apply -f k8s/yaml/configmap/hadoop-configmap.yaml --namespace=preprod
kubectl apply -f k8s/yaml/configmap/hive-configmap.yaml --namespace=preprod
kubectl apply -f k8s/yaml/configmap/spark-configmap.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-mg-gateway.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-mg-eureka.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-ps-publicservice.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-ps-cs.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-cg-linkismanager.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-cg-entrance.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-cg-engineplugin.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-cg-engineconnmanager.yaml --namespace=preprod
kubectl apply -f k8s/yaml/linkis-web.yaml --namespace=preprod'''
      }
    }

  }
}