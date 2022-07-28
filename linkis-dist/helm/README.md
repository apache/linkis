Helm charts for Linkis 
==========

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# Pre-requisites
> Note: KinD is required only for development and testing.
* [Kubernetes](https://kubernetes.io/docs/setup/), minimum version v1.21.0+
* [Helm](https://helm.sh/docs/intro/install/), minimum version v3.0.0+.
* [KinD](https://kind.sigs.k8s.io/docs/user/quick-start/), minimum version v0.11.0+.


# Installation

```shell
# Deploy Apache Linkis on kubernetes, kubernetes 
# namespace is 'linkis', helm release is 'linkis-demo'

# Option 1, use build-in script
$> ./scripts/install-charts.sh linkis linkis-demo

# Option 2, use `helm` command line
$> helm install --create-namespace -f ./charts/linkis/values.yaml --namespace linkis linkis-demo ./charts/linkis 
```

# Uninstallation

```shell
$> helm delete --namespace linkis linkis-demo 
```

# For developers

We recommend using [KinD](https://kind.sigs.k8s.io/docs/user/quick-start/) for development and testing. 
KinD is a tool for running local Kubernetes clusters using Docker container as “Kubernetes nodes”.

Follow the link below to install the KinD in your development environment.

- [KinD Installation](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)

## Setup a local cluster for test
Once after you have installed KinD, you can run the following command to setup a local kubernetes cluster and deploy an Apache Linkis cluster on it.

```shell
# It will deploy a MySQL instance in the KinD cluster,
# then deploy an Apache Linkis cluster, which will use 
# the MySQL instances above 
$> sh ./scripts/create-kind-cluster.sh \
   && sh ./scripts/install-mysql.sh \
   && sh ./scripts/install-charts.sh
   
Creating cluster "test-helm" ...
 ✓ Ensuring node image (kindest/node:v1.21.1) 🖼 
 ✓ Preparing nodes 📦  
 ✓ Writing configuration 📜 
 ✓ Starting control-plane 🕹️ 
 ✓ Installing CNI 🔌 
 ✓ Installing StorageClass 💾 
Set kubectl context to "kind-test-helm"
You can now use your cluster with:

kubectl cluster-info --context kind-test-helm

Have a nice day! 👋
Image: "linkis:1.3.0" with ID "sha256:917138e97807c3a2d7d7fe4607c1196e7c00406bb3b8f81a3b64e54a4d8fe074" not yet present on node "test-helm-control-plane", loading...
Image: "mysql:5.7" with ID "sha256:efa50097efbdef5884e5ebaba4da5899e79609b78cd4fe91b365d5d9d3205188" not yet present on node "test-helm-control-plane", loading...
namespace/mysql created
service/mysql created
deployment.apps/mysql created

NAME: linkis-demo
LAST DEPLOYED: Wed Jul  6 23:46:30 2022
NAMESPACE: linkis
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
---
Welcome to Apache Linkis (v1.3.0)!

.___    .___ .______  .____/\ .___ .________
|   |   : __|:      \ :   /  \: __||    ___/
|   |   | : ||       ||.  ___/| : ||___    \
|   |/\ |   ||   |   ||     \ |   ||       /
|   /  \|   ||___|   ||      \|   ||__:___/
|______/|___|    |___||___\  /|___|   : v1.3.0
                           \/

Linkis builds a layer of computation middleware between upper applications and underlying engines.
Please visit https://linkis.apache.org/ for details.

Enjoy!

```

## Enable port-forward for jvm remote debug
> INFO: [Understand how port-forward works.](https://kubernetes.io/docs/tasks/access-application-cluster/port-forward-access-application-cluster/)
```shell
# start port-forward for all servers
$> ./scripts/remote-debug-proxy.sh start
- starting port-forwad for [web] with mapping [local->8087:8087->pod] ...
- starting port-forwad for [mg-eureka] with mapping [local->5001:5005->pod] ...
- starting port-forwad for [mg-gateway] with mapping [local->5002:5005->pod] ...
- starting port-forwad for [ps-cs] with mapping [local->5003:5005->pod] ...
- starting port-forwad for [ps-publicservice] with mapping [local->5004:5005->pod] ...
- starting port-forwad for [ps-metadataquery] with mapping [local->5005:5005->pod] ...
- starting port-forwad for [ps-data-source-manager] with mapping [local->5006:5005->pod] ...
- starting port-forwad for [cg-linkismanager] with mapping [local->5007:5005->pod] ...
- starting port-forwad for [cg-entrance] with mapping [local->5008:5005->pod] ...
- starting port-forwad for [cg-engineconnmanager] with mapping [local->5009:5005->pod] ...
- starting port-forwad for [cg-engineplugin] with mapping [local->5010:5005->pod] ...

# Once the port-forward setup, you can configure the jvm remote debugger of you IDE 
# to connect to the local port, which is mapping to a backend server port, and start
# the remote debug process.

# list exists port-forward instances
$> sh ./scripts/remote-debug-proxy.sh list 
hadoop            65439   0.0  0.1  5054328  30344 s013  S     8:01PM   0:00.13 kubectl port-forward -n linkis pod/linkis-demo-cg-engineplugin-548b8cf695-g4hnp 5010:5005 --address=0.0.0.0
hadoop            65437   0.0  0.1  5054596  30816 s013  S     8:01PM   0:00.13 kubectl port-forward -n linkis pod/linkis-demo-cg-engineconnmanager-868d8d4d6f-dqt7d 5009:5005 --address=0.0.0.0
hadoop            65435   0.0  0.1  5051256  31128 s013  S     8:01PM   0:00.14 kubectl port-forward -n linkis pod/linkis-demo-cg-entrance-7dc7b477d4-87fdt 5008:5005 --address=0.0.0.0
hadoop            65433   0.0  0.1  5049708  30092 s013  S     8:01PM   0:00.15 kubectl port-forward -n linkis pod/linkis-demo-cg-linkismanager-6f76bb5c65-vc292 5007:5005 --address=0.0.0.0
hadoop            65431   0.0  0.1  5060716  30012 s013  S     8:01PM   0:00.13 kubectl port-forward -n linkis pod/linkis-demo-ps-data-source-manager-658474588-hjvdw 5006:5005 --address=0.0.0.0
hadoop            65429   0.0  0.1  5059972  31048 s013  S     8:01PM   0:00.14 kubectl port-forward -n linkis pod/linkis-demo-ps-metadataquery-695877dcf7-r9hnx 5005:5005 --address=0.0.0.0
hadoop            65427   0.0  0.1  5052268  30860 s013  S     8:01PM   0:00.14 kubectl port-forward -n linkis pod/linkis-demo-ps-publicservice-788cb9674d-7fp7h 5004:5005 --address=0.0.0.0
hadoop            65423   0.0  0.1  5064312  30428 s013  S     8:01PM   0:00.14 kubectl port-forward -n linkis pod/linkis-demo-ps-cs-6d976869d4-pjfts 5003:5005 --address=0.0.0.0
hadoop            65421   0.0  0.1  5058912  29996 s013  S     8:01PM   0:00.14 kubectl port-forward -n linkis pod/linkis-demo-mg-gateway-7c4f5f7c98-xv9wd 5002:5005 --address=0.0.0.0
hadoop            65419   0.0  0.1  5051780  30564 s013  S     8:01PM   0:00.13 kubectl port-forward -n linkis pod/linkis-demo-mg-eureka-0 5001:5005 --address=0.0.0.0
hadoop            65417   0.0  0.1  5067128  29876 s013  S     8:01PM   0:00.11 kubectl port-forward -n linkis pod/linkis-demo-web-5585ffcddb-swsvh 8087:8087 --address=0.0.0.0

# stop all port-forward instances
$> sh ./scripts/remote-debug-proxy.sh stop
- stopping port-forward for [web] with mapping [local->8087:8087->pod] ...
- stopping port-forward for [mg-eureka] with mapping [local->5001:5005->pod] ...
- stopping port-forward for [mg-gateway] with mapping [local->5002:5005->pod] ...
- stopping port-forward for [ps-cs] with mapping [local->5003:5005->pod] ...
- stopping port-forward for [ps-publicservice] with mapping [local->5004:5005->pod] ...
- stopping port-forward for [ps-metadataquery] with mapping [local->5005:5005->pod] ...
- stopping port-forward for [ps-data-source-manager] with mapping [local->5006:5005->pod] ...
- stopping port-forward for [cg-linkismanager] with mapping [local->5007:5005->pod] ...
- stopping port-forward for [cg-entrance] with mapping [local->5008:5005->pod] ...
- stopping port-forward for [cg-engineconnmanager] with mapping [local->5009:5005->pod] ...
- stopping port-forward for [cg-engineplugin] with mapping [local->5010:5005->pod] ...

```

## Enter into a backend server container
```shell
# Enter into the mg-gateway and submit a job with linkis-cli
$> sh ./scripts/login-pod.sh mg-gateway
``` 
```shell
# in the mg-gateway container
bash-4.2$ ./bin/./linkis-cli -engineType shell-1 -codeType shell -code "echo \"hello\" "  -submitUser hadoop -proxyUser hadoop
=====Java Start Command=====
exec /etc/alternatives/jre/bin/java -server -Xms32m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/linkis/logs/linkis-cli -XX:ErrorFile=/opt/linkis/logs/linkis-cli/ps_err_pid%p.log -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=80 -XX:+DisableExplicitGC    -classpath /opt/linkis/conf/linkis-cli:/opt/linkis/lib/linkis-computation-governance/linkis-client/linkis-cli/*:/opt/linkis/lib/linkis-commons/public-module/*: -Dconf.root=/etc/linkis-conf -Dconf.file=linkis-cli.properties -Dlog.path=/opt/linkis/logs/linkis-cli -Dlog.file=linkis-client..log.20220713120445464374700  org.apache.linkis.cli.application.LinkisClientApplication '-engineType shell-1 -codeType shell -code echo "hello"  -submitUser hadoop -proxyUser hadoop'
...
```

## Destroy the local cluster
```shell
# Option 1: delete the helm release only
$> helm delete --namespace linkis linkis-demo 

# Option 2: destroy the KinD cluster, no need to delete
# the helm release first
$> kind delete cluster --name test-helm
```

## Test with LDH 
We introduced a new image, called LDH (Linkis's hadoop all-in-one image), which provides a pseudo-distributed hadoop cluster for testing quickly. This image contains the following hadoop components, the default mode for engines is on-yarn.
* hadoop 2.7.0 , including hdfs and yarn
* hive 2.3.9
* spark 3.3.0
* flink 1.14.5

> INFO: The hive in LDH image depends on external mysql, please deploy mysql first before deploying LDH.

To make a LDH image, please run the maven command on the root of the project as below

```shell
$> ./mvnw clean install -Pdocker -Dmaven.javadoc.skip=true -Dmaven.test.skip=true -Dlinkis.build.web=true -Dlinkis.build.ldh=true
```

By default, we download the tarball packages for each hadoop component from the official apache mirror site, which can be very slow for members in some regions. To solve this problem, members in these regions can download these tarballs by themselves and put them in the directory: ${HOME}/.linkis-build-cache .

Run the following command to setup a local kubernetes cluster with LDH on it.

```shell
# create and deploy
$> export WITH_LDH=true
$> sh . /scripts/create-kind-cluster.sh \
   && sh . /scripts/install-mysql.sh \
   && sh ./scripts/install-ldh.sh \
   && sh ./scripts/install-charts.sh
   
...

# take a try
$> kubectl exec -it -n ldh $(kubectl get pod -n ldh -o jsonpath='{.items[0].metadata.name}') -- bash

[root@ldh-55dd4c7c5d-t7ssx /]# hdfs dfs -ls /
Found 3 items
drwxrwxrwx   - root supergroup          0 2022-07-28 04:58 /completed-jobs
drwxrwxrwx   - root supergroup          0 2022-07-28 04:58 /spark2-history
drwxrwxrwx   - root supergroup          0 2022-07-28 04:58 /tmp

[root@ldh-55dd4c7c5d-t7ssx /]# beeline -u jdbc:hive2://ldh.ldh.svc.cluster.local:10000/ -n hadoop
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/ldh/1.1.3/apache-hive-2.3.9-bin/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/ldh/1.1.3/hadoop-2.7.0/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://ldh.ldh.svc.cluster.local:10000/
Connected to: Apache Hive (version 2.3.9)
Driver: Hive JDBC (version 2.3.9)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.9 by Apache Hive
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> show databases;
+----------------+
| database_name  |
+----------------+
| default        |
+----------------+
1 row selected (1.551 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> !q
Closing: 0: jdbc:hive2://ldh.ldh.svc.cluster.local:10000/

[root@ldh-55dd4c7c5d-t7ssx /]# spark-sql 
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
22/07/28 05:20:40 WARN Client: Neither spark.yarn.jars nor spark.yarn.archive is set, falling back to uploading libraries under SPARK_HOME.
Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
Spark master: yarn, Application Id: application_1658984291196_0001
spark-sql> show databases;
default
Time taken: 3.409 seconds, Fetched 1 row(s)
spark-sql> exit;
22/07/28 05:22:14 WARN HiveConf: HiveConf of name hive.stats.jdbc.timeout does not exist
22/07/28 05:22:14 WARN HiveConf: HiveConf of name hive.stats.retries.wait does not exist

[root@ldh-55dd4c7c5d-t7ssx /]# flink run /opt/ldh/current/flink/examples/streaming/TopSpeedWindowing.jar
Executing TopSpeedWindowing example with default input data set.
Use --input to specify file input.
Printing result to stdout. Use --output to specify output path.

```

Finally, you can access the web ui by port-forward.
