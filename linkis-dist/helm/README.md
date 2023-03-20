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
Welcome to Apache Linkis (v1.3.2)!

.___    .___ .______  .____/\ .___ .________
|   |   : __|:      \ :   /  \: __||    ___/
|   |   | : ||       ||.  ___/| : ||___    \
|   |/\ |   ||   |   ||     \ |   ||       /
|   /  \|   ||___|   ||      \|   ||__:___/
|______/|___|    |___||___\  /|___|   : v1.3.2
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
exec /etc/alternatives/jre/bin/java -server -Xms32m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/linkis/logs/linkis-cli -XX:ErrorFile=/opt/linkis/logs/linkis-cli/ps_err_pid%p.log -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=80 -XX:+DisableExplicitGC    -classpath /opt/linkis/conf/linkis-cli:/opt/linkis/lib/linkis-computation-governance/linkis-client/linkis-cli/*:/opt/linkis/lib/linkis-commons/public-module/*: -Dconf.root=/etc/linkis-conf -Dconf.file=linkis-cli.properties -Dlog.path=/opt/linkis/logs/linkis-cli -Dlog.file=linkis-client..log.20220814162421217892600  org.apache.linkis.cli.application.LinkisClientApplication '-engineType shell-1 -codeType shell -code echo "hello"  -submitUser hadoop -proxyUser hadoop'
OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N
[INFO] LogFile path: /opt/linkis/logs/linkis-cli/linkis-client..log.20220814162421217892600
[INFO] User does not provide usr-configuration file. Will use default config
[INFO] connecting to linkis gateway:http://linkis-demo-mg-gateway.linkis.svc.cluster.local:9001
JobId:1
TaskId:1
ExecId:exec_id018016linkis-cg-entrance10.244.0.13:9104LINKISCLI_hadoop_shell_0
[INFO] Job is successfully submitted!

2022-07-31 16:24:24.024 INFO Program is substituting variables for you
2022-07-31 16:24:24.024 INFO Variables substitution ended successfully
2022-07-31 16:24:24.024 WARN The code you submit will not be limited by the limit
Job with jobId : 1 and execID : LINKISCLI_hadoop_shell_0 submitted
2022-07-31 16:24:25.024 INFO You have submitted a new job, script code (after variable substitution) is
************************************SCRIPT CODE************************************
echo "hello"
************************************SCRIPT CODE************************************
2022-07-31 16:24:25.024 INFO Your job is accepted,  jobID is LINKISCLI_hadoop_shell_0 and jobReqId is 1 in ServiceInstance(linkis-cg-entrance, 10.244.0.13:9104). Please wait it to be scheduled
job is scheduled.
2022-07-31 16:24:25.024 INFO Your job is Scheduled. Please wait it to run.
Your job is being scheduled by orchestrator.
2022-07-31 16:24:25.024 INFO job is running.
2022-07-31 16:24:25.024 INFO Your job is Running now. Please wait it to complete.
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

We introduced a new image, called LDH (Linkis's hadoop all-in-one image), which provides a pseudo-distributed hadoop cluster for testing quickly. This image contains the following hadoop components, the default mode for engines in LDH is on-yarn.

* Hadoop 2.7.2 , including HDFS and YARN
* Hive 2.3.3
* Spark 2.4.3
* Flink 1.12.2
* ZooKeeper 3.5.9

> INFO: The hive in LDH image depends on external mysql, please deploy mysql first before deploying LDH.

To make an LDH image, please run the maven command on the root of the project as below

```shell
$> ./mvnw clean install -Pdocker \
   -Dmaven.javadoc.skip=true \
   -Dmaven.test.skip=true \
   -Dlinkis.build.web=true \
   -Dlinkis.build.ldh=true
```

By default, we download the pre-built binary distributions for each hadoop component from the official site of [Apache Archives](https://archive.apache.org/dist/), which can be very slow for members in some regions.
Downloading the distributions from a faster mirror site manually and moving it into this directory `${HOME}/.linkis-build-cache` can solve this problem.

Run the following command to setup a local kubernetes cluster with LDH on it.

```shell
# create and deploy
$> sh ./scripts/create-kind-cluster.sh \
   && sh ./scripts/install-mysql.sh \
   && sh ./scripts/install-ldh.sh \
   && sh ./scripts/install-charts.sh

...

# take a try
$> kubectl exec -it -n ldh $(kubectl get pod -n ldh -o jsonpath='{.items[0].metadata.name}') -- bash

[root@ldh-96bdc757c-dnkbs /]# hdfs dfs -ls /
Found 4 items
drwxrwxrwx   - root supergroup          0 2022-07-31 02:48 /completed-jobs
drwxrwxrwx   - root supergroup          0 2022-07-31 02:48 /spark2-history
drwxrwxrwx   - root supergroup          0 2022-07-31 02:49 /tmp
drwxrwxrwx   - root supergroup          0 2022-07-31 02:48 /user

[root@ldh-96bdc757c-dnkbs /]# beeline -u jdbc:hive2://ldh.ldh.svc.cluster.local:10000/ -n hadoop
Connecting to jdbc:hive2://ldh.ldh.svc.cluster.local:10000/
Connected to: Apache Hive (version 2.3.3)
Driver: Hive JDBC (version 2.3.3)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.3 by Apache Hive
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> create database demo;
No rows affected (1.306 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> use demo;
No rows affected (0.046 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> create table t1 (id int, data string);
No rows affected (0.709 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> insert into t1 values(1, 'linikis demo');
WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
No rows affected (5.491 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> select * from t1;
+--------+---------------+
| t1.id  |    t1.data    |
+--------+---------------+
| 1      | linikis demo  |
+--------+---------------+
1 row selected (0.39 seconds)
0: jdbc:hive2://ldh.ldh.svc.cluster.local:100> !q

[root@ldh-96bdc757c-dnkbs /]# spark-sql
22/07/31 02:53:18 INFO hive.metastore: Trying to connect to metastore with URI thrift://ldh.ldh.svc.cluster.local:9083
22/07/31 02:53:18 INFO hive.metastore: Connected to metastore.
...
22/07/31 02:53:19 INFO spark.SparkContext: Running Spark version 2.4.3
22/07/31 02:53:19 INFO spark.SparkContext: Submitted application: SparkSQL::10.244.0.6
...
22/07/31 02:53:27 INFO yarn.Client: Submitting application application_1659235712576_0001 to ResourceManager
22/07/31 02:53:27 INFO impl.YarnClientImpl: Submitted application application_1659235712576_0001
22/07/31 02:53:27 INFO cluster.SchedulerExtensionServices: Starting Yarn extension services with app application_1659235712576_0001 and attemptId None
22/07/31 02:53:28 INFO yarn.Client: Application report for application_1659235712576_0001 (state: ACCEPTED)
...
22/07/31 02:53:36 INFO yarn.Client: Application report for application_1659235712576_0001 (state: RUNNING)
...
Spark master: yarn, Application Id: application_1659235712576_0001
22/07/31 02:53:46 INFO thriftserver.SparkSQLCLIDriver: Spark master: yarn, Application Id: application_1659235712576_0001
spark-sql> use demo;
Time taken: 0.074 seconds
22/07/31 02:58:02 INFO thriftserver.SparkSQLCLIDriver: Time taken: 0.074 seconds
spark-sql> select * from t1;
...
1       linikis demo
2       linkis demo spark sql
Time taken: 3.352 seconds, Fetched 2 row(s)
spark-sql> quit;

[root@ldh-96bdc757c-dnkbs /]# zkCli.sh
Connecting to localhost:2181
Welcome to ZooKeeper!
JLine support is enabled
WATCHER::

WatchedEvent state:SyncConnected type:None path:null

[zk: localhost:2181(CONNECTED) 0] get -s /zookeeper/quota

cZxid = 0x0
ctime = Thu Jan 01 00:00:00 UTC 1970
mZxid = 0x0
mtime = Thu Jan 01 00:00:00 UTC 1970
pZxid = 0x0
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 0
[zk: localhost:2181(CONNECTED) 1] quit

[root@ldh-96bdc757c-dnkbs /]# flink run /opt/ldh/current/flink/examples/streaming/TopSpeedWindowing.jar
Executing TopSpeedWindowing example with default input data set.
Use --input to specify file input.
Printing result to stdout. Use --output to specify output path.
...
```

You can access services of LDH in the kubernetes cluster with the endpoint `ldh.ldh.svc.cluster.local`, for example, access hdfs from your pod:

```shell
[root@sample-pod /]# hdfs dfs -ls hdfs://ldh.ldh.svc.cluster.local:9000/
Found 4 items
drwxrwxrwx   - root supergroup          0 2022-07-28 04:58 hdfs://ldh.ldh.svc.cluster.local:9000/completed-jobs
drwxrwxrwx   - root supergroup          0 2022-07-28 05:22 hdfs://ldh.ldh.svc.cluster.local:9000/spark2-history
drwxrwxrwx   - root supergroup          0 2022-07-28 04:58 hdfs://ldh.ldh.svc.cluster.local:9000/tmp
drwxr-xr-x   - root supergroup          0 2022-07-28 05:20 hdfs://ldh.ldh.svc.cluster.local:9000/user
```

Finally, you can access the web ui with `kubectl port-forward` .
