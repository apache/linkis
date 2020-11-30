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

    ### 基础镜像编译
    cd Linkis/emr-base
    docker build -t zhangrong1027/linkis:emr-base-spark2.4.4 .
    推送基础镜像到harbor
    docker push  zhangrong1027/linkis:emr-base-spark2.4.4 



    ### 编译各微服务镜像
    各个微服务basedir下面有对应module 的dockerfile
    与公司内部的CI/CD集成，可以通过mvn docker插件直接编译docker 镜像
    编译
        Linkis 根目录执行
        mvn clean package  -f gateway/gateway-ujes-support/pom_k8s.xml
        docker images | grep linkis-gateway
    可以看到已经编译好的镜像
        docker push zhangrong1027/linkis:linkis-gateway   

  四、K8S部署
  
    1.启动eureka
      eureka部署在k8s的任意一个node节点上就可以
      启动eureka成功后
      修改Linkis/k8s/linkis-eureka-configmap.yaml中的eurekaUrl
      
    2.修改k8s目录下的linkis-*- configmap.yaml
      特别注意配置文件的正确性，避免后续部署的低级问题出现，如数据库连接不上，hdfs地址不正确等等
      例如gateway/linkis.properties 复制到k8s/linkis-gateway-configmap.yaml
      
    3.运行k8s目录下的 
      sh init.sh
      观察eureka上注册的服务，一共22个，都启动成功基本就没有问题

   五、spark 是虚机部署的情况下，k8s环境虚拟2层网络打通虚机 ip 段
   
     静态路由+iptables/netfilter地址伪装
     executor到pod，svc 网络通
     executor添加静态路由表
     route add -net   10.83.0.0  netmask  255.255.0.0  gw  10.20.20.204
     route add -net   172.27.0.0 netmask  255.255.0.0  gw  10.20.20.204
     k8s node节点上做ip  masque
     iptables -t nat -A POSTROUTING -s  10.20.20.0 /24 -d  10.83.0.0/16 -j MASQUERADE
     iptables -t nat -A POSTROUTING -s  10.20.20.0 /24 -d  172.27.0.0 /12 -j  MASQUERADE
