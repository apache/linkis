pipeline {
  agent {
    node {
      label 'node2'
    }

  }
  stages {
    stage('build web') {
      steps {
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
        sh '''docker tag linkis-web:1.2.0 registry.mydomain.com/library/linkis-web:1.2.0
docker push registry.mydomain.com/library/linkis-web:1.2.0'''
      }
    }

    stage('deploy on k8s') {
      steps {
        echo 'deploy on k8s'
        sh 'kubectl apply -f k8s/yaml/linkis-web.yaml'
      }
    }

  }
}
