# Linkis on K8S

## base镜像构建
所有linkis的基础镜像，包括了jdk和hadoop，spark，flink客户端，由于镜像分层共享，因此简单起见共用基础镜像

构建时自行放置jdk,hadoop,spark客户端目录至dockerfile同目录

使用docker命令构建基础镜像，例如```docker build -t linkis-base:1.2.0 . -f linkis-base.Dockerfile``

构建完成后，修改8个服务的dockerfile中，FROM的镜像名

## 服务镜像构建

``` shell
# 多线程编译
mvn -N install
mvn -T 12 -B install -pl '!:public-module-combined,!:apache-linkis-combined,!:apache-linkis-combined-package' -DskipTests
mvn install -pl ':public-module-combined,:apache-linkis-combined' -DskipTests

# 单线程编译
mvn -N install
mvn clean install -pl '!:apache-linkis-combined-package' -DskipTests

# 镜像构建，registry按需修改
for project in \$(cat k8s/build.info)
do
    imageName="${registryHost}/luban/\${project}:1.0.3"
    echo "build image: \${imageName}"

    docker build -t \$imageName -f k8s/Dockerfile/\${project}.Dockerfile assembly-combined-package/assembly-combined/target/apache-linkis-1.0.3-incubating-dist
    docker push \$imageName
done
```
## 说明

### 镜像构建
Yaml中的配置仅作参考，根据实际环境自行更改，例如namespace，镜像名根据自己打包的名称，以及仓库名来修改

容器内设置crontab进行kinit，如果没有配置Kerberos可以删除postStart内容

volume使用ceph本地卷挂载，如果不用ceph需要修改挂载方式

conf目录挂载的格式和linkis非k8s目录结构相同，engine plugin服务需要对每个引擎单独挂载conf，其中需要配置eureka注册为IP
``` yaml
eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.cloud.client.ip-address}:${server.port}
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
```
所有conf中需要设置EUREKA_PREFER_IP=true，服务在eureka注册格式必须是IP:端口，在application中配置instance-id: ${spring.cloud.client.ip-address}:${server.port}


## 一些坑

1. hadoop集群需要和k8s集群网络互通，engineconnmanager内部需要启动spark，spark的executor需要能访问ecm，所以必须设置网络为hostNetwork
```
hostNetwork: true
dnsPolicy: ClusterFirstWithHostNet
```

2. 重启pod后，表里记录的组件信息不会删除（偶尔可以自动删除，不稳定）
```
linkis_cg_manager_service_instance 对应服务engineconnmanager
linkis_ps_instance_info 对应服务ps-cs
```

3. 启用Kerberos后，需要申请无域名绑定的keytab，否则容器内hdfs操作会卡4秒，因为K8S dns查询超时

4. 容器内添加新用户的时候，挂载keytab不方便，解决方案是采用proxyuser，代理到同一个keytab来提交