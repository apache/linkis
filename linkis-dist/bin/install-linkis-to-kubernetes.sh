#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#variable
WORK_DIR=`cd $(dirname $0); pwd -P`
ROOT_DIR=${WORK_DIR}/..
MIRRORS="ghcr.io"
TAG="latest"
COMMAND="pull-install"
DEBUG=false
WITH_LDH=false
USING_KIND=false
KUBE_NAMESPACE="linkis"
HELM_RELEASE_NAME="linkis-demo"

debug(){
    if [ $DEBUG = true ]; then
        echo $(date "+%Y-%m-%d %H:%M:%S") "debug: "$1
    fi
}

info(){
    echo $(date "+%Y-%m-%d %H:%M:%S") "info: "$1
}


#help info
help() {
    echo "Command            Describe"
    echo "pull-install       pull and install linkis images"
    echo "install            install linkis images"
    echo "pull               pull linkis images"
    echo "reset              delete the test-helm cluster of kind"
    echo "help               print help info"
    echo ""
    echo "Params             Describe"
    echo "-d                 print debug info (default: false)"
    echo "-m                 mirror url (default:ghcr.io , eg: ghcr.dockerproxy.com)"
    echo "-t                 tag name (default:latest)"
    echo "-l                 install linkis with ldh (default:false)"
    echo "-r                 install linkis with ldh (default:false)"
    echo "-n                 kubernetes namespace (default:linkis)"
    echo "--helm-release     helm release name (default:linkis-demo)"
    echo ""
    echo "example:"
    echo "./install-linkis-to-kubernetes.sh pull                                  pull image with ghcr.io"
    echo "./install-linkis-to-kubernetes.sh -tlatest                              pull image with tag"
    echo "./install-linkis-to-kubernetes.sh pull -mghcr.dockerproxy.com           pull image with ghcr.dockerproxy.com or ghcr.nju.edu.cn"
    echo "./install-linkis-to-kubernetes.sh install -l -mghcr.dockerproxy.com     install linkis to kind and kubernetes with ldh"
    echo "./install-linkis-to-kubernetes.sh pull-install -mghcr.dockerproxy.com   pull image and install linkis to kind and kubernetes"
}

#pull the container image of the linkis
pull(){
    if [ $WITH_LDH = true ]; then
      debug ${MIRRORS}/apache/linkis/linkis-ldh:${TAG}
      docker pull ${MIRRORS}/apache/linkis/linkis-ldh:${TAG}
    fi
    debug ${MIRRORS}/apache/linkis/linkis:${TAG}
    docker pull ${MIRRORS}/apache/linkis/linkis:${TAG}
    debug ${MIRRORS}/apache/linkis/linkis-web:${TAG}
    docker pull ${MIRRORS}/apache/linkis/linkis-web:${TAG}
}
#change the label
tag(){
    docker tag  ${MIRRORS}/apache/linkis/linkis:${TAG} linkis:dev
    docker tag  ${MIRRORS}/apache/linkis/linkis-web:${TAG} linkis-web:dev
    if [ $WITH_LDH = true ]; then
      docker tag  ${MIRRORS}/apache/linkis/linkis-ldh:${TAG} linkis-ldh:dev
    fi
}
#create an image to carry mysql
make_linkis_image_with_mysql_jdbc(){
    ${ROOT_DIR}/docker/scripts/make-linkis-image-with-mysql-jdbc.sh
    docker tag linkis:with-jdbc linkis:dev
    ${ROOT_DIR}/docker/scripts/make-ldh-image-with-mysql-jdbc.sh
    docker tag linkis-ldh:with-jdbc linkis-ldh:dev
}
#creating a kind cluster
create_kind_cluster(){
    ${ROOT_DIR}/helm/scripts/create-kind-cluster.sh
}
#mysql installation
install_mysql(){
    ${ROOT_DIR}/helm/scripts/install-mysql.sh $USING_KIND
}
#ldh installation
install_ldh(){
    if [ $WITH_LDH = true ]; then
      ${ROOT_DIR}/helm/scripts/install-ldh.sh $USING_KIND
    fi
}
#linkis installation
install_linkis(){
    if [ $WITH_LDH = true ];then
      ${ROOT_DIR}/helm/scripts/install-charts-with-ldh.sh $KUBE_NAMESPACE $HELM_RELEASE_NAME $USING_KIND
    else
      ${ROOT_DIR}/helm/scripts/install-linkis.sh $KUBE_NAMESPACE $HELM_RELEASE_NAME true $USING_KIND
    fi
}
#display pods
display_pods(){
    kubectl get pods -A
}

install(){
    if [ $USING_KIND = true ]; then
      tag
      make_linkis_image_with_mysql_jdbc
      create_kind_cluster
    fi
    install_mysql
    install_ldh
    install_linkis
    display_pods
}

reset(){
    kind delete clusters test-helm
}

check_docker(){
    docker -v >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "Docker is not installed！"
        exit 1
    fi
}

check_kind(){
    kind --version >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "kind is not installed！"
        exit 1
    fi
}

check_kubectl(){
    kubectl >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "kubectl is not installed！"
        exit 1
    fi
}

check_helm(){
    helm version >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "helm is not installed！"
        exit 1
    fi
}


debug $WORK_DIR

#entrance to the program
main(){

    #argument parsing
    long_opts="debug,mirrors:"
    getopt_cmd=$(getopt -o dm:lt:kn: -l helm-release: -n $(basename $0) -- "$@") || \
                { echo -e "\nERROR: Getopt failed. Extra args\n"; exit 1;}

    eval set -- "$getopt_cmd"
    while true; do
        case "$1" in
            -d) DEBUG=true;;
            -m) MIRRORS=$2;;
            -t) TAG=$2;;
            -l) WITH_LDH=true;;
            -k) USING_KIND=true;;
            -n) KUBE_NAMESPACE=$2;;
            --helm-release) HELM_RELEASE_NAME=$2;;
            --) shift; break;;
        esac
        shift
    done

    #environmental testing
    check_docker
    check_kubectl
    check_helm
    if [ $USING_KIND = true ];then
        check_kind
    fi

    debug "params num:"$#

    #command parsing
    if [ $# -eq 0 ]; then
        COMMAND="pull-install"
    else
        COMMAND=$1
    fi

    debug "command is:"$COMMAND

    if [ $COMMAND = "pull-install" ]; then
        pull
        install
    fi

    if [ $COMMAND = "install" ]; then
        install
    fi

    if [ $COMMAND = "pull" ]; then
        pull
    fi

    if [ $COMMAND = "reset" ]; then
        reset
    fi

    if [ $COMMAND = "help" ]; then
        help
    fi
}

main $@
