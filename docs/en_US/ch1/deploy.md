## 1 Getting Started

### (1) Prerequisites(Please install the items marked with "required")

- Mysql (5.5+) : Required
- JDK (1.8.0_141) : Required
- Hadoop (2.7.2) : Required
- Hive (1.2.1) : Optional, Hive engine nodes need to be installed.
- Spark (2.1.0) : Optional, Spark engine nodes need to be installed.

Notes:  Linkis itself does not depend on Hadoop, it only calls their clients when running the corresponding tasks.

### (2) Create deploy users

Create deploy users on all machines that needs to be deployed. Since the deploy service executes the job in sudo -u {linux-user} mode, the deploy user needs to have sudo privileges, and it needs to be passwordless.

```
vi	/etc/sudoers
```

For example, a deploy user is a hadoop account.

```
hadoop  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL
```

### (3) ssh passwordless configuration 

Setup passwordless SSH login on deploy machines and other installation machines. If you want to install on deploy machine, you need to get through the ssh of the host machine and other machines. 

## 2 Compile and Package

After getting the project from git, use maven to package the project installation package(If you already have the installation package, install it directly, no need to compile).

(1) Execute the following command in the directory where the outermost project pom.xml locates.

```
	mvn clean install
```

(2) Execute the following command if it is the first time use on local server, otherwise just execute the command in step 1:

```
	mvn -N  install
```

(3) Get the installation package, in the assembly->target directory:

```
	wedatasphere-linkis-0.6.0-dist.tar.gz
```

## 3 Installation 

Unzip the installation package to the installation directory and modify the configuration file after decompression.

```
	tar -xvf  wedatasphere-linkis-0.6.0-dist.tar.gz
```

(1) Modify the basic configuration

```
	vi /conf/config.sh   
```

- Specify deploy user: deployUser
- Specify the installation directory: LINKIS_INSTALL_HOME
- Specify the log storage path: USER_LOG_PATH
- Specify the result set HDFS storage path: RESULT_STORE_PATH
- Specify the machine IP address and port number where each service is installed: * _INSTALL_IP, * _PORT

(2) Modify the configuration of database

```
	vi ./conf/db.sh
```

- Set the connection information of the database
- Including IP address, database name, username, port
- MYSQL_HOST=
- MYSQL_PORT=
- MYSQL_DB=
- MYSQL_USER=
- MYSQL_PASSWORD=

(3)  Execute the installation script

```
	sh ./bin/install.sh  
```

## 4  Start Service

```
    sh  ./bin/start-all.sh
```

