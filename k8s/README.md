# Linkis on K8S


## 1. Continuous integration environments

centos 7

Docker 18.06.3

Kubernetes 1.19.7

Jenkins 2.347

jdk 8

Hadoop 2.7.2

The spark 2.4

Hive 2.3.3



## 2. Base image construction

All linkis base images, including JDK and Hadoop, Spark, and Flink clients, share the base images for simplicity because the images are layered



Save JDK, Hadoop, and Spark client directories to the same directory as dockerfile during build



Place mysql-connector-java-5.1.49.jar in the k8S \jars directory



Use the docker command to build the base image, for example, docker build -t linkis-base:1.2.0. -f linkis-base.Dockerfile



After the build is complete, change the image name of FROM in the dockerfile of the 8 services





## 3. Introduction to Jenkinsfile deployment scripts

Build the package

Build Image Build image

Push image Push image

Deploy on K8S Deploy on K8S

 
# Linkis on K8S

## 1.持续集成环境
centos 7  
docker  18.06.3  
kubernetes 1.19.7  
jenkins 2.347  
jdk 8  
hadoop 2.7.2  
spark 2.4  
hive 2.3.3  

## 2.base镜像构建
所有linkis的基础镜像，包括了jdk和hadoop，spark，flink客户端，由于镜像分层共享，因此简单起见共用基础镜像

构建时自行放置jdk,hadoop,spark客户端目录至dockerfile同目录

将mysql-connector-java-5.1.49.jar 放入 k8s\jars目录

使用docker命令构建基础镜像，例如```docker build -t linkis-base:1.2.0 . -f linkis-base.Dockerfile``

构建完成后，修改8个服务的dockerfile中，FROM的镜像名


## 3.Jenkinsfile 部署脚本介绍
build package  构建程序  
build image    构建镜像  
push image     推送镜像  
deploy on k8s  部署到k8s  