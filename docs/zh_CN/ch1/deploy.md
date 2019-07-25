## 1、准备工作

### (1) 基础软件安装(必装项请自行安装)

- Mysql (5.5+) : 必装
- JDK (1.8.0_141) : 必装
- Hadoop(2.7.2) ：必装， 
- Hive(1.2.1) : 选装，hive引擎节点需要安装
- Spark(2.1.0) :  必装，Spark引擎节点需要安装

  **注意：Linkis本身不依赖Hadoop、Hive、Spark,仅是会调用他们的Client，用于对应任务的运行。**

### (2) 创建部署用户

例如部署用户是hadoop账号<br>
1. 在所有需要部署的机器上创建部署用户
   
     sudo useradd hadoop
     sudo cp ~/.bashrc /home/hadoop

2. 因为Linkis的服务是以 sudo -u ${linux-user} 方式来切换引擎，从而执行作业，所以部署用户需要有 sudo 权限，而且是免密的。

    vi /etc/sudoers

    hadoop  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

### (3) ssh免密配置

在部署机器和其他安装机器上配置ssh免密登录，如果要在部署机上安装，需要将主机器和各个其它机器SSH打通

## 2、编译打包：
   从git获取项目代码后，使用maven打包项目安装包(如已有安装包可以直接进行安装，不需要编译打包)。
   
   （1） 在最外层工程pom.xml所在目录执行以下命令
   
         mvn clean install
   （2） 如果是本地第一次使用，必须先执行以下命令，否则执行步骤1即可：
   
         mvn -N  install
   （3） 获取安装包，在工程的assembly->target目录下：
   
         assembly-0.5.0-SNAPSHOT-dist.tar.gz
          
## 3、安装：
   先解压安装包到安装目录，并对解压后文件进行配置修改。
   
      tar -xvf  assembly-0.5.0-SNAPSHOT-dist.tar.gz
   （1）修改基础配置  
   
       vi /conf/config.sh   
        
   - 指定部署用户：deployUser
   - 指定安装目录：LINKIS_INSTALL_HOME
   - 指定日志存储路径：USER_LOG_PATH
   - 指定结果集HDFS存储路径：RESULT_STORE_PATH
   - 指定各个服务安装所在的机器IP地址和端口号:* _INSTALL_IP, * _PORT
        
   （2）修改数据库配置 
   
       vi ./conf/db.sh 
            
   - 设置数据库的连接信息
   - 包括IP地址、数据库名称、用户名、端口
   - MYSQL_HOST=
   - MYSQL_PORT=
   - MYSQL_DB=
   - MYSQL_USER=
   - MYSQL_PASSWORD=

   （3）执行安装脚本：
   
        sh ./bin/install.sh       

## 4、启动服务：
        sh  ./bin/start-all.sh