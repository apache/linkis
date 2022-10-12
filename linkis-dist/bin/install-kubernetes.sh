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
MIRRORS="ghcr.io"
COMMAND="pull-install"
DEBUG=false
WITH_LDH=false

#help info
help() {
    echo "Command        Describe"
    echo "pull-install   pull and install linkis images"
    echo "install        pull linkis images"
    echo "pull           pull linkis images"
    echo "help           print help info"
    echo ""
    echo "Params         Describe"
    echo "-m,--mirrors   url (default:ghcr.io , eg: ghcr.dockerproxy.com)"
    echo "-d,--debug     print debug info"
    echo "-l,--ldh       install linkis with ldh"
    echo ""
    echo "example:"
    echo "./install-kubernetes.sh pull                                   pull image with ghcr.io"
    echo "./install-kubernetes.sh pull -m ghcr.dockerproxy.com           pull image with ghcr.dockerproxy.com"
    echo "./install-kubernetes.sh install                                install linkis to kind and kubernetes"
    echo "./install-kubernetes.sh pull-install -m ghcr.dockerproxy.com   pull image and install linkis to kind and kubernetes"
}

#pull the container image of the linkis
pull(){
    docker pull $MIRRORS/apache/incubator-linkis/linkis-ldh:latest
    docker pull $MIRRORS/apache/incubator-linkis/linkis:latest
    docker pull $MIRRORS/apache/incubator-linkis/linkis-web:latest
}
#change the label
tag(){
    docker tag  $MIRRORS/apache/incubator-linkis/linkis:latest linkis:dev
    docker tag  $MIRRORS/apache/incubator-linkis/linkis-web:latest linkis-web:dev
    docker tag  $MIRRORS/apache/incubator-linkis/linkis-ldh:latest linkis-ldh:dev
}
#create an image to carry mysql
make-linikis-image-with-mysql-jdbc(){
    ../docker/scripts/make-linikis-image-with-mysql-jdbc.sh
    docker tag linkis:with-jdbc linkis:dev
}
#creating a kind cluster
create-kind-cluster(){
    ../helm/scripts/create-kind-cluster.sh
}
#mysql installation
install-mysql(){
    ../helm/scripts/install-mysql.sh
}
#ldh installation
install-ldh(){
    ../helm/scripts/install-ldh.sh
}
#linkis installation
install-linkis(){
    if [ $WITH_LDH = true ];then
      ../helm/scripts/install-charts-with-ldh.sh
    else
      ../helm/scripts/install-linkis.sh
    fi
}
#display pods
display-pods(){
    kubectl get pods -a
}

install(){
    tag
    make-linikis-image-with-mysql-jdbc
    create-kind-cluster
    install-mysql
    install-ldh
    install-linkis
    display-pods
}

debug(){
    if [ $DEBUG = true ]; then
        echo $(date "+%Y-%m-%d %H:%M:%S") "debug: "$1
    fi
}

info(){
    echo $(date "+%Y-%m-%d %H:%M:%S") "info: "$1
}


check-docker(){
    docker -v >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "Docker is not installed！"
        exit 1
    fi
}

check-kind(){
    kind --version >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "kind is not installed！"
        exit 1
    fi
}

check-kubectl(){
    kubectl version >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "kubectl is not installed！"
        exit 1
    fi
}

check-helm(){
    helm version >> /dev/null 2>&1
    if [ $? -ne  0 ]; then
        echo "helm is not installed！"
        exit 1
    fi
}


#entrance to the program
main(){

    #environmental testing
    check-docker
    check-kind
    check-kubectl
    check-helm

    #argument parsing
    long_opts="debug,mirrors:"
    getopt_cmd=$(getopt -o dml: --long "$long_opts" \
                -n $(basename $0) -- "$@") || \
                { echo -e "\nERROR: Getopt failed. Extra args\n"; exit 1;}

    eval set -- "$getopt_cmd"
    while true; do
        case "$1" in
            -d|--debug) DEBUG=true;;
            -m|--mirrors) MIRRORS=$2;;
            -l|--ldh) WITH_LDH=true;;
            --) shift; break;;
        esac
        shift
    done

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

    if [ $COMMAND = "help" ]; then
        help
    fi
}

main $@
