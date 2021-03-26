Linkis on kubernetes

#Focus on solving the problem
1.During the deployment process, everyone will encounter package conflicts, operating system incompatibility, openjdk, hotspot jdk compatibility issues, docker delivery can fundamentally solve these problems

2.Docker is based on cgroup resource isolation, shared file system, namespace can limit computing resources lightly, and expand and shrink in seconds

     For example: limit a process to only use 20% of the resources of a CPU core
     cd /sys/fs/cgroup/cpu
     touch test 
     $ echo 10000 > cpu.cfs_quota_us 
     $ echo 50000 > cpu.cfs_period_us
     Start a process, please do not do this in production! ! ! !
     $ while :; do :; done &
     Record the current process ID
     $ echo pid >> tasks

#Deployment document
    
##The method of compiling basic image and each microservice image

  Currently all microservice images are uploaded to dockerhub，https://hub.docker.com/repository/docker/wedatasphere/linkis
  
  In consideration of the deployment version of the virtual machine, we add a new pom_k8s.xml，the default packaging method is the same as before. If you want to compile the image yourself, you need to quote pom_k8s.xml
  
    mvn clean package -f  gateway/gateway-ujes-support/pom_k8s.xml
  
  一.create kubernetes  docker secret
  
      k8s/registry.yaml Modify the account, password, and mirror warehouse address of the corresponding harbor
      Or create a secret manually, and then pull the mirror to use. Please modify the private docker warehouse to a private repository address
      $kubectl create secret docker-registry registry-key \
       --docker-server=https://hub.docker.com \
       --docker-username=wedatasphere \
       --docker-password=***** \
       --docker-email=wedatasphere@webank.com
  
  二.NFS/ceph build
  
     After building NFS or ceph, the deployment of microservices such as publicservice needs to modify the address of nfs
  
  三.Basic image creation file (you can skip this step if you use the officially maintained image)
   The method of making a basic image yourself is as follows：
   *  Environment variable settings
   
        > Set the harbor's environment variable address
        ```
        export HARBOR_LINKIS=
        ```
      
   * Basic image compilation and packaging
        > packaging
        ```
        cd Linkis/emr-base
        docker build -t $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 .
        ```
        > push
        ```
        docker push  $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 
        ```
   * Basic dependency package compilation
        ```
        mvn clean install -Dmaven.test.skip=true
        ```

   * Replace the basic image of each microservice Dockerfile with your own private server address
        ```
        find . -name Dockerfile | xargs grep -rl "FROM wedatasphere/linkis" | xargs sed -i "" "s?FROM wedatasphere/linkis?FROM $HARBOR_LINKIS/linkis?"
        find . -name "*.yaml" | xargs grep -rl "image: wedatasphere/linkis" | xargs sed -i "" "s?image: wedatasphere/linkis?image: $HARBOR_LINKIS/linkis?"
        ```
   
   * Compile each microservice image
   
        > There is a Dockerfile corresponding to the module under each microservice basedir
    Integrate with the company's internal CI/CD, you can directly compile the docker image through the mvn docker plugin
        ```
        sh k8s/package.sh
        sh k8s/build.sh
        sh k8s/push.sh
        ```
     
  四、K8S deployment
    
    1.Big data environment configuration
      > It mainly involves the configuration of hadoop, hive, and spark environment. It is necessary to configure several components on each node of k8s
      
      ｜ Component ｜ Version |  Node directory ｜
      ｜ ---  | --- |  ---    |
      | hadoop | 2.7.7 | /opt/hadoop/hadoop-2.7.7 |
      | hive  | 2.3.6  | /opt/hive/apache-hive-2.3.6-bin |
      | spark | 2.4    | /opt/spark/spark-2.4.4-bin-hadoop2.7 |   
      
    2.Start eureka
      Eureka can be deployed on any node node of k8s
      After starting eureka successfully
      Modify eurekaUrl in Linkis/k8s/linkis-eureka-configmap.yaml
      
    3.Modify linkis-*- configmap.yaml in the k8s directory
      Pay special attention to the correctness of the configuration file to avoid low-level problems in subsequent deployments, such as failure to connect to the database, incorrect hdfs address, etc.
      For example, copy gateway/linkis.properties to k8s/linkis-gateway-configmap.yaml
      
    4.Run the startup script in the k8s directory
      sh init.sh
      Observe the services registered on eureka, there are 22 services in total, and there is basically no problem if they start successfully.

   五、When spark is a virtual machine deployment, the virtual layer 2 network of the k8s environment opens up the virtual machine ip segment
   
     Static routing + iptables/netfilter address masquerading
     executor to pod, svc network communication
     
     executor adds static routing table
     route add -net   127.0.0.1  netmask  255.255.0.0  gw  127.0.0.1
     route add -net   127.0.0.1 netmask  255.255.0.0  gw  127.0.0.1
     
     Do ip masque on k8s node
     iptables -t nat -A POSTROUTING -s  127.0.0.1 /24 -d  127.0.0.1/16 -j MASQUERADE
     iptables -t nat -A POSTROUTING -s  127.0.0.1 /24 -d  127.0.0.1 /12 -j  MASQUERADE
