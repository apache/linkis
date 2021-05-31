#!/bin/bash

#当前路径
workDir=$(cd `dirname $0`; pwd)


echo "dss front-end deployment script"

source $workDir/config.sh
# 前端放置目录，默认为解压目录
dss_basepath=$workDir

#To be compatible with MacOS and Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    echo "dss  install not support Mac OSX operating system"
    exit 1
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
    # linux
    echo "linux"
elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    echo "dss   not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    echo "dss  not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "dss  not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "freebsd"* ]]; then
    # ...
    echo "freebsd"
else
    # Unknown.
    echo "Operating system unknown, please tell us(submit issue) for better service"
    exit 1
fi

# 区分版本
version=`cat /etc/redhat-release|sed -r 's/.* ([0-9]+)\..*/\1/'`


echo "========================================================================配置信息======================================================================="

echo "前端访问端口：${dss_port}"
echo "后端Linkis的地址：${linkis_url}"
echo "静态文件地址：${dss_basepath}/dist"
echo "当前路径：${workDir}"
echo "本机ip：${dss_ipaddr}"

echo "========================================================================配置信息======================================================================="
echo ""


# 创建文件并配置nginx
dssConf(){

	s_host='$host'
    s_remote_addr='$remote_addr'
    s_proxy_add_x_forwarded_for='$proxy_add_x_forwarded_for'
    s_http_upgrade='$http_upgrade'
    echo "
        server {
            listen       $dss_port;# 访问端口
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;
            location /dss/visualis {
            root   ${dss_basepath}/dss/visualis; # 静态文件目录
            autoindex on;
            }
            location / {
            root   ${dss_basepath}/dist; # 静态文件目录
            index  index.html index.html;
            }
            location /ws {
            proxy_pass $linkis_url;#后端Linkis的地址
            proxy_http_version 1.1;
            proxy_set_header Upgrade $s_http_upgrade;
            proxy_set_header Connection "upgrade";
            }

            location /api {
            proxy_pass $linkis_url; #后端Linkis的地址
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
    " > /etc/nginx/conf.d/dss.conf

}


centos7(){
    # nginx是否安装
    #sudo rpm -Uvh http://nginx.org/packages/centos/7/noarch/RPMS/nginx-release-centos-7-0.el7.ngx.noarch.rpm
    sudo yum install -y nginx
    echo "nginx 安装成功"

    # 配置nginx
    dssConf

    # 解决 0.0.0.0:8888 问题
    yum -y install policycoreutils-python
    semanage port -a -t http_port_t -p tcp $dss_port

    # 开放前端访问端口
    firewall-cmd --zone=public --add-port=$dss_port/tcp --permanent

    # 重启防火墙
    firewall-cmd --reload

    # 启动nginx
    systemctl restart nginx

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
    # 临时生效
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

    # 配置nginx
    dssConf

    # 防火墙
    S_iptables=`lsof -i:$dss_port | wc -l`
    if [ "$S_iptables" -gt "0" ];then
    # 已开启端口防火墙重启
    service iptables restart
    else
    # 未开启防火墙添加端口再重启
    iptables -I INPUT 5 -i eth0 -p tcp --dport $dss_port -m state --state NEW,ESTABLISHED -j ACCEPT
    service iptables save
    service iptables restart
    fi

    # start
    /etc/init.d/nginx start

    # 调整SELinux的参数
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

    # 临时生效
    setenforce 0

}

# centos 6
if [[ $version -eq 6 ]]; then
    centos6
fi

# centos 7
if [[ $version -eq 7 ]]; then
    centos7
fi
echo '安装visualis前端,用户自行编译DSS前端安装包，则安装时需要把visualis的前端安装包放置于此'$dss_basepath/dss/visualis'，用于自动化安装:'
cd $dss_basepath/dss/visualis;unzip -o build.zip  > /dev/null
echo "请浏览器访问：http://${dss_ipaddr}:${dss_port}"
