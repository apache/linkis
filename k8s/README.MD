Linkis on kubernetes

#重点解决问题
1.每个人在部署过程中，都会遇到包冲突，操作系统不兼容，openjdk，hotspot jdk兼容问题，docker交付可以从根本上解决这些问题

2.docker基于cgroup资源隔离，共享文件系统，namespace 可以轻量限制计算资源，秒级扩缩容
 例如：限制某个进程只能使用CPU一个核的20%资源
 cd /sys/fs/cgroup/cpu
 touch test 
 $ echo 10000 > cpu.cfs_quota_us 
 $ echo 50000 > cpu.cfs_period_us
 启动一个进程，请不要在生产做这样操作！！！！  
 $ while :; do :; done &
 记录当前进程Id
 $ echo pid >> tasks

#部署文档
##基础镜像及各服务镜像编译方法
  10.206.0.5是我私人的harbor地址，可以修改为公司内部
  registry.yaml 修改对应harbor的账号，密码，镜像内网地址
  基础镜像基于centos最新版本
  目录结构如下
  --linkis 
    --emr-base
       --Dockerfile
       --README.MD
       --hadoop
          --etc
             --conf
               -- hdfs-site.xml
               -- yarn-site.xml
               -- map-red.xml
               ...
       -- hive
          --conf 
       -- jdk-8u211-linux-x64.tar.gz
       -- scala-2.11.0.tgz
       -- spark-2.4.4-bin-hadoop2.7.tgz
 
###基础镜像编译
cd emr-base
docker build -t 10.206.0.5/library/linkis:emr-base-spark2.4.4 .
推送基础镜像到harbor
docker push  10.206.0.5/library/linkis:emr-base-spark2.4.4 



###编译各微服务镜像
各个微服务basedir下面有对应module 的dockerfile
与公司内部的CI/CD集成，可以通过mvn docker插件直接编译docker 镜像
编译
    Linkis 根目录执行
    mvn clean package  -f gateway/gateway-ujes-support/pom.xml
    docker images | grep linkis-gateway
可以看到已经编译好的镜像
    docker push 10.206.0.5/library/linkis:linkis-gateway   

#K8S部署
1.修改k8s目录下的docker secret
  register.yaml  镜像地址，账号，密码
2.修改k8s目录下的linkis-*- configmap.yaml
  例如gateway/linkis.properties 复制到k8s/linkis-gateway-configmap.yaml
3.运行k8s 目录下的 sh init.sh
