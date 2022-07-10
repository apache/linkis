Helm charts for Linkis 
==========

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# Pre-requisites
* [Kubernetes](https://kubernetes.io/docs/setup/), minimum version v1.21.0+
* [Helm](https://helm.sh/docs/intro/install/), minimum version v3.0.0+.

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
$> sh ./scripts/create-test-kind.sh \
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

## Destroy the local cluster
```shell
# Option 1: delete the helm release only
$> helm delete --namespace linkis linkis-demo 

# Option 2: destroy the KinD cluster, no need to delete
# the helm release first
$> kind delete cluster --name test-helm
```
