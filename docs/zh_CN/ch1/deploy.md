## 1、准备工作

### (1) 基础软件安装(必装项请自行安装)

- Mysql (5.5+) : 必装
- JDK (1.8.0_141) : 必装
- Hadoop(2.7.2) ：选装，如需使用hive\spark引擎功能则需要安装Hadoop，只使用Python无需安装 
- Hive(1.2.1) : 选装，hive引擎节点需要安装
- Spark(2.1.0) : 选装，Spark引擎节点需要安装

  **注意：Linkis本身不依赖Hive、Spark等服务，仅是会调用他们的Client，用于对应任务的运行。**
  
  机器环境变量设置，具体路径根据实际安装情况来设置，有使用到Hadoop,Spark,Hive时，需设置对应的环境变量，
  下方为示例：
    ```
      export JAVA_HOME=/nemo/jdk1.8.0_141
      #HADOOP  
      export HADOOP_HOME=/appcom/Install/hadoop
      export HADOOP_CONF_DIR=/appcom/config/hadoop-config    
      #Spark
      export SPARK_HOME=/appcom/Install/spark
      export SPARK_CONF_DIR=/appcom/config/spark-config/spark-submit
      #Hive
      export HIVE_HOME=/appcom/Install/hive
      export HIVE_CONF_DIR=/appcom/config/hive-config
    ```
 
### (2) 创建用户

例如: **部署用户是hadoop账号**

1. 在所有需要部署的机器上创建部署用户，用于安装
   
         sudo useradd hadoop  
         
    创建使用用户：
    ```
         sudo useradd -Ghadoop xxx
     
         sudo cp ~/.bashrc /home/xxx
    ```

2. 因为Linkis的服务是以 sudo -u ${linux-user} 方式来切换引擎，从而执行作业，所以部署用户需要有 sudo 权限，而且是免密的。

         vi /etc/sudoers

         hadoop  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

### (3) ssh免密配置


在部署机器和其他安装机器上配置ssh免密登录，如果要在部署机上安装，需要将主机器和各个其它机器SSH打通


## 2、编译打包（可跳过）：

   ### 如果用户不想自己编译，可以直接在release页面下载安装包，本步骤可以直接跳过。
   
   从git获取项目代码后，使用maven打包项目安装包。   

   （1） 如果是本地第一次使用，必须在最外层工程pom.xml所在目录先执行以下命令，否则直接执行步骤2即可：
   
         mvn -N  install
         
   （2） 在最外层工程pom.xml所在目录执行以下命令
      
         mvn clean install
         
   （3） 获取安装包，在工程的assembly->target目录下：
   
         wedatasphere-linkis-0.5.0-dist.tar.gz
          
## 3、安装：

   先解压安装包到安装目录，并对解压后文件进行配置修改。
   
      tar -xvf  wedatasphere-linkis-0.5.0-dist.tar.gz
      
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
        
    可以在Eureka界面查看服务启动成功情况，查看方法：
    使用eureka的IP地址+端口号port, 在浏览器中打开，查看服务是否注册成功即可。
   
       
