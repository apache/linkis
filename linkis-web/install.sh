#!/usr/bin/env bash
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

#current directory
workDir=$(cd `dirname $0`; pwd)


echo "linkis frontend deployment script"

source $workDir/config.sh
# frontend directory,decompression directory by default

input_port=$1

re='^[0-9]+$'
if  [[ $input_port =~ $re ]] ; then
   echo "try to use special frontend port：${input_port}"
   linkis_port=$input_port
fi


linkis_basepath=$workDir

#To be compatible with MacOS and Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    echo "linkis  install not support Mac OSX operating system"
    exit 1
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
    # linux
    echo "linux"
elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    echo "linkis   not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    echo "linkis  not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "linkis  not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "freebsd"* ]]; then
    # ...
    echo "freebsd"
else
    # Unknown.
    echo "Operating system unknown, please tell us(submit issue) for better service"
    exit 1
fi

# distinguish version
version=`cat /etc/redhat-release|sed -r 's/.* ([0-9]+)\..*/\1/'`


echo "================================== print config info begin =================================="

echo "frontend port：${linkis_port}"
echo "backend address：${linkis_url}"
echo "static file directory：${linkis_basepath}/dist"
echo "current directory：${workDir}"
echo "local ip：${linkis_ipaddr}"

echo "================================== print config info end =================================="
echo ""

portIsOccupy=false
checkPort(){
    pid=`sudo lsof -nP -iTCP:$linkis_port -sTCP:LISTEN`
    if [ "$pid" != "" ];then
      echo "$linkis_port already used"
      portIsOccupy=true
    fi
}

#create nginx conf file
linkisConf(){

	  s_host='$host'
    s_remote_addr='$remote_addr'
    s_proxy_add_x_forwarded_for='$proxy_add_x_forwarded_for'
    s_http_upgrade='$http_upgrade'
    echo "
        server {
            listen       $linkis_port;#access port
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;

            location / {
            root   ${linkis_basepath}/dist; #static directory
            index  index.html index.html;
            }
            location /ws {
            proxy_pass $linkis_url;#Linkis backend address
            proxy_http_version 1.1;
            proxy_set_header Upgrade $s_http_upgrade;
            proxy_set_header Connection "upgrade";
            }

            location /api {
            proxy_pass $linkis_url; #Linkis backend address
            proxy_set_header Host $s_host;
            proxy_set_header X-Real-IP $s_remote_addr;
            proxy_set_header x_real_ipP $s_remote_addr;
            proxy_set_header remote_addr $s_remote_addr;
            proxy_set_header X-Forwarded-For $s_proxy_add_x_forwarded_for;
            proxy_http_version 1.1;
            proxy_connect_timeout 4s;
            proxy_read_timeout 600s;
            proxy_send_timeout 12s;
            proxy_set_header Upgrade $s_http_upgrade;
            proxy_set_header Connection upgrade;
            }

            #error_page  404              /404.html;
            # redirect server error pages to the static page /50x.html
            #
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
            root   /usr/share/nginx/html;
            }
        }
    " > /etc/nginx/conf.d/linkis.conf

}


centos7(){
    # to install nginx
    #sudo rpm -Uvh http://nginx.org/packages/centos/7/noarch/RPMS/nginx-release-centos-7-0.el7.ngx.noarch.rpm
    sudo yum install -y nginx
    echo "install nginx success"

    # config inginx
    linkisConf

    # fix 0.0.0.0:8888 problem
    yum -y install policycoreutils-python
    semanage port -a -t http_port_t -p tcp $linkis_port

    # Open front-end access port
    firewall-cmd --zone=public --add-port=$linkis_port/tcp --permanent

    # restart firewall
    firewall-cmd --reload

    # start nginx
    systemctl restart nginx

    # adjust SELinux parameter
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
    # take effect temporarily
    setenforce 0

}


centos6(){
    # yum
    S_basearch='$basearch'
    S_releasever='$releasever'
    echo "
    [nginx]
    name=nginx repo
    baseurl=http://nginx.org/packages/centos/$E_releasever/$S_basearch/
    gpgcheck=0
    enabled=1
    " >> /etc/yum.repos.d/nginx.repo

    # install nginx
    yum install nginx -y

    # config inginx
    linkisConf

    # firewall
    S_iptables=`sudo lsof -i:$linkis_port | wc -l`
    if [ "$S_iptables" -gt "0" ];then
      # allow to access port,restart firewall
      service iptables restart
    else
      # not allow to access port,add port rule,restart firewall
      iptables -I INPUT 5 -i eth0 -p tcp --dport $linkis_port -m state --state NEW,ESTABLISHED -j ACCEPT
      service iptables save
      service iptables restart
    fi

    # start nginx
    sudo /etc/init.d/nginx start

     # adjust SELinux parameter
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

    # take effect temporarily
    setenforce 0

}

checkPort
if [ "$portIsOccupy" = true ];then
  echo "The port is already in use, please check before installing"
  exit 1
fi

if [ -e /var/run/nginx.pid ]; then
echo "Nginx is already running! Will try to reload nginx config";
# config inginx
linkisConf

sudo nginx -s reload

else

	echo "Starting install nginx and try to starting..."

  # centos 6
  if [[ $version -eq 6 ]]; then
      centos6
  fi

  # centos 7
  if [[ $version -eq 7 ]]; then
      centos7
  fi

fi

echo "Please open the link in the browser：http://${linkis_ipaddr}:${linkis_port}"
