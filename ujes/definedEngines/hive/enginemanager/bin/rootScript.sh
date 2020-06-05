#!/usr/bin/expect -f

set user [lindex $argv 0]
set command [lindex $argv 1]
set timeout -1

spawn su -
expect "~]# "
send "useradd -d /home/$user $user\r"
expect "~]#" {
    send "echo 'export JAVA_HOME=/usr/local/jdk1.8.0_221' >> /home/$user/.bashrc\r"
    send "echo 'export JRE_HOME=/usr/local/jdk1.8.0_221/jre' >> /home/$user/.bashrc\r"
    send "echo 'export SPARK_HOME=/opt/spark/spark-2.4.4-bin-hadoop2.7' >> /home/$user/.bashrc\r"
    send "echo 'export SPARK_CONF_DIR=/opt/spark/spark-2.4.4-bin-hadoop2.7/conf' >> /home/$user/.bashrc\r"
    send "echo 'export PYSPARK_ALLOW_INSECURE_GATEWAY=1' >> /home/$user/.bashrc\r"
    send "echo 'export HADOOP_HOME=/opt/hadoop/hadoop-2.7.7' >> /home/$user/.bashrc\r"
    send "echo 'export HADOOP_CONF_PATH=/opt/hadoop/hadoop-2.7.7/etc/hadoop' >> /home/$user/.bashrc\r"
    send "echo 'export HADOOP_CONF_DIR=\$HADOOP_HOME/etc/hadoop' >> /home/$user/.bashrc\r"
    send "echo 'export SCALA_HOME=/usr/local/scala-2.11.8' >> /home/$user/.bashrc\r"
    send "echo 'export HIVE_HOME=/opt/hive/apache-hive-2.3.6-bin' >> /home/$user/.bashrc\r"
    send "echo 'export HIVE_CONF_DIR=/opt/hive/apache-hive-2.3.6-bin/conf' >> /home/$user/.bashrc\r"
    send "echo 'export JAVA_HOME=/usr/local/jdk1.8.0_221' >> /home/$user/.bashrc\r"
    send "echo 'PATH=\$PATH:\$JAVA_HOME/bin:\$JRE_HOME/bin:\$SPARK_HOME/bin:\$FLINK_HOME/bin:\$HADOOP_HOME/bin:\$SCALA_HOME/bin:\$MVN_HOME/bin:\$HIVE_HOME/bin'  >> /home/$user/.bashrc\r"}
send "su - $user\r"
expect "~]* "
send "$command \r"
expect "~]* "
send "exit\r"
expect "~]# "
send "exit\r"
expect "~]$ "
