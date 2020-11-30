该目录下放入jdk，scala,spark二进制压缩包编译基础镜像使用，其他linkis module都是基于基础镜像做扩展
spark需要二进制包，因为spark-engine调用了spark-submit脚本

hadoop,hive 配置文件core-site.xml,hdfs-sit.xml,hive-site.xml等也放在同一级目录hive/conf,hadoop/conf，按Dockerfile的指定层级