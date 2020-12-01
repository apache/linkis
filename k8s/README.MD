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

  目前所有微服务镜像都上传到dockerhub，https://hub.docker.com/repository/docker/zhangrong1027/linkis,webank
  申请的镜像仓库下来，我们会持续维护docker到新镜像仓库
  
  出于对虚机部署版本的考虑，新增了pom_k8s.xml，默认打包方式跟之前保持一致，如果想要自己编译镜像，需要引用pom_k8s.xml
  
    mvn clean package -f  gateway/gateway-ujes-support/pom_k8s.xml
  
  一.创建kubernetes  docker secret
  
      k8s/registry.yaml 修改对应harbor的账号，密码，镜像仓库地址
      或者手动创建secret，后续拉取镜像使用，私有docker仓库请修改成私有reposity地址
      $kubectl create secret docker-registry registry-key \
       --docker-server=https://hub.docker.com \
       --docker-username=zhangrong1027 \
       --docker-password=***** \
       --docker-email=brian.rongzhang@gmail.com
  
  二.NFS/ceph 搭建
  
     搭建NFS或者ceph完成后，publicservice等微服务的deployment需要修改nfs的地址
  
  三.基础镜像创建文件（使用官方维护的镜像可以跳过该步骤）
   自己制作基础镜像方法如下：
   *  环境变量设置
   
        > 设置harbor的地址环境变量
        ```
        export HARBOR_LINKIS=
        ```
      
   * 基础镜像编译打包
        > 打包
        ```
        cd Linkis/emr-base
        docker build -t $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 .
        ```
        > 推送
        ```
        docker push  $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 
        ```
   * 基础依赖包编译
        ```
        mvn clean install -Dmaven.test.skip=true
        ```

   * 替换各个微服务Dockerfile的基础镜像为自己的私服地址
        ```
        find . -name Dockerfile | xargs grep -rl "FROM chongchuanbing/linkis" | xargs sed -i "" "s?FROM chongchuanbing/linkis?FROM $HARBOR_LINKIS/linkis?"
        grep -rl "image: docker.io/chongchuanbing/linkis" k8s | xargs sed -i "" "s?image: chongchuanbing/linkis?image: $HARBOR_LINKIS/linkis?"
        ```
   
   * 编译各微服务镜像
   
        > 各个微服务basedir下面有对应 module 的 Dockerfile
    与公司内部的CI/CD集成，可以通过mvn docker插件直接编译docker镜像
        ```
        sh k8s/package.sh
        sh k8s/build.sh
        sh k8s/push.sh
        ```
     
  四、K8S部署
    
    1.大数据环境配置
      > 主要涉及 hadoop，hive，spark环境配置，需要将几个组件的配置在k8s各个节点上
      
      ｜ 组件 ｜ 版本 |  节点目录 ｜
      ｜ ---  | --- |  ---    |
      | hadoop | 2.7.7 | /opt/hadoop/hadoop-2.7.7 |
      | hive  | 2.3.6  | /opt/hive/apache-hive-2.3.6-bin |
      | spark | 2.4    | /opt/spark/spark-2.4.4-bin-hadoop2.7 |   
      
    2.启动eureka
      eureka部署在k8s的任意一个node节点上就可以
      启动eureka成功后
      修改Linkis/k8s/linkis-eureka-configmap.yaml中的eurekaUrl
      
    3.修改k8s目录下的linkis-*- configmap.yaml
      特别注意配置文件的正确性，避免后续部署的低级问题出现，如数据库连接不上，hdfs地址不正确等等
      例如gateway/linkis.properties 复制到k8s/linkis-gateway-configmap.yaml
      
    4.运行k8s目录下的 
      sh init.sh
      观察eureka上注册的服务，一共22个，都启动成功基本就没有问题
