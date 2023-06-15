## linkis-spark-etl


### Instructions for use

You need to place the corresponding spark connector jars in the spark/jars directory

The spark connector jar can be obtained using the following command

```text
cd /linkis/linkis-engineconn-plugins/spark/scala-2.12

mvn clean install  -Dmaven.test.skip=true
```

The compiled spark connector jar is in the following directory
```text
/linkis/linkis-engineconn-plugins/spark/scala-2.12/target/out/spark/dist/3.2.1/lib
```


### 1. jdbc write jdbc

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"jdbc\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"url\":\"jdbc:postgresql://172.16.16.16:5432/spark_etl_test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"postgres\",\"password\":\"linkis_test\",\"query\":\"select * from public.spark_etl_pg_test_source\"}},{\"name\":\"jdbc\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"url\":\"jdbc:postgresql://172.16.16.16:5432/spark_etl_test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"postgres\",\"password\":\"linkis_test\",\"targetTable\":\"public.spark_etl_pg_test_sink\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
    "plugins":[
                {
            "name":"jdbc",
            "type":"source",
            "config":{
                "resultTable":"test",
				   "url":"jdbc:postgresql://host:5432/spark_etl_test",
                "driver":"org.postgresql.Driver",
                "user":"postgres",
                "password":"linkis_test",
                "query":"select * from public.spark_etl_pg_test_source"
            }
        },
		        {
            "name":"jdbc",
            "type":"sink",
            "config":{
                "sourceTable":"test",
                "url":"jdbc:postgresql://host:5432/spark_etl_test",
                "driver":"org.postgresql.Driver",
                "user":"postgres",
                "password":"linkis_test",
                "targetTable":"public.spark_etl_pg_test_sink"
            }
        }
    ]
}
```


### 2. csv write csv

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"file\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin\",\"serializer\":\"csv\",\"options\":{\"header\":\"true\",\"delimiter\":\";\"},\"columnNames\":[\"name\",\"age\"]}},{\"name\":\"file\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test_csv\",\"saveMode\":\"overwrite\",\"options\":{\"header\":\"true\"},\"serializer\":\"csv\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
  "plugins":[
    {
      "name": "file",
      "type": "source",
      "config": {
        "resultTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin",
        "serializer": "csv",
        "options": {
          "header":"true",
          "delimiter":";"
        },
        "columnNames": ["name", "age"]
      }
    },
    {
      "name": "file",
      "type":"sink",
      "config": {
        "sourceTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test_csv",
        "saveMode": "overwrite",
        "options": {
          "header":"true"
        },
        "serializer": "csv"
      }
    }
  ]
}

```


### 3. csv write delta

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"file\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin\",\"serializer\":\"csv\",\"options\":{\"header\":\"true\",\"delimiter\":\";\"},\"columnNames\":[\"name\",\"age\"]}},{\"name\":\"datalake\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"tableFormat\":\"delta\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test_delta\",\"saveMode\":\"overwrite\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
  "plugins":[
    {
      "name": "file",
      "type": "source",
      "config": {
        "resultTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin",
        "serializer": "csv",
        "options": {
          "header":"true",
          "delimiter":";"
        },
        "columnNames": ["name", "age"]
      }
    },
    {
      "name": "datalake",
      "type":"sink",
      "config": {
        "sourceTable": "test",
        "tableFormat": "delta",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test_delta",
        "saveMode": "overwrite"
      }
    }
  ]
}
```

You need  jars to put under spark/jars directory
```text
delta-core_2.12-2.0.2.jar
delta-storage-2.0.2.jar
```


### 4. csv write mongo

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"file\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test_csv\",\"serializer\":\"csv\",\"options\":{\"header\":\"true\"},\"columnNames\":[\"name\",\"age\"]}},{\"name\":\"mongo\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"uri\":\"mongodb://mongo:Wds%40mongo@wds07:27017/?authSource=admin\",\"database\":\"linkis\",\"collection\":\"test\",\"saveMode\":\"overwrite\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
  "plugins":[
    {
      "name": "file",
      "type": "source",
      "config": {
        "resultTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test_csv",
        "serializer": "csv",
        "options": {
          "header":"true"
        },
        "columnNames": ["name", "age"]
      }
    },
    {
      "name": "mongo",
      "type":"sink",
      "config": {
        "sourceTable": "test",
        "uri": "mongodb://username:password@host:27017/?authSource=admin",
        "database": "linkis",
        "collection": "test",
        "saveMode": "overwrite"
      }
    }
  ]
}
```

You need  jars to put under spark/jars directory
```text
bson-4.0.6.jar
mongo-spark-connector_2.12-3.0.1.jar
mongodb-driver-core-4.0.6.jar
mongodb-driver-sync-4.0.6.jar
```




### 5. csv write hudi

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"file\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin\",\"serializer\":\"csv\",\"options\":{\"header\":\"true\",\"delimiter\":\";\"},\"columnNames\":[\"name\",\"age\"]}},{\"name\":\"datalake\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"tableFormat\":\"hudi\",\"options\":{\"hoodie.table.name\":\"huditest\",\"hoodie.datasource.write.recordkey.field\":\"age\",\"hoodie.datasource.write.precombine.field\":\"age\"},\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test_hudi\",\"saveMode\":\"overwrite\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
  "plugins":[
    {
      "name": "file",
      "type": "source",
      "config": {
        "resultTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin",
        "serializer": "csv",
        "options": {
          "header":"true",
          "delimiter":";"
        },
        "columnNames": ["name", "age"]
      }
    },
    {
      "name": "datalake",
      "type":"sink",
      "config": {
        "sourceTable": "test",
        "tableFormat": "hudi",
        "options": {
          "hoodie.table.name":"huditest",
          "hoodie.datasource.write.recordkey.field":"age",
          "hoodie.datasource.write.precombine.field":"age"
        },
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test_hudi",
        "saveMode": "overwrite"
      }
    }
  ]
}
```

You need  jars to put under spark/jars directory
```text
hudi-spark3.2-bundle_2.12-0.13.0.jar
```

需要修改的配置
```text
将`spark.serializer`设置为`org.apache.spark.serializer.KryoSerializer`
```





### 6. csv write elasticsearch

 Submitting tasks via Linkis-cli
```text
sh ./bin/linkis-cli -engineType spark-3.2.1  -codeType data_calc -code "{\"plugins\":[{\"name\":\"file\",\"type\":\"source\",\"config\":{\"resultTable\":\"test\",\"path\":\"hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin\",\"serializer\":\"csv\",\"options\":{\"header\":\"true\",\"delimiter\":\";\"},\"columnNames\":[\"name\",\"age\"]}},{\"name\":\"elasticsearch\",\"type\":\"sink\",\"config\":{\"sourceTable\":\"test\",\"node\":\"wds07\",\"port\":\"9200\",\"index\":\"estest\",\"options\":{\"es.nodes.wan.only\":\"true\"},\"saveMode\":\"append\"}}]}"  -submitUser hadoop -proxyUser hadoop
```

code
```json
{
  "plugins":[
    {
      "name": "file",
      "type": "source",
      "config": {
        "resultTable": "test",
        "path": "hdfs://linkishdfs/tmp/linkis/spark_etl_test/etltest.dolphin",
        "serializer": "csv",
        "options": {
          "header":"true",
          "delimiter":";"
        },
        "columnNames": ["name", "age"]
      }
    },
    {
      "name": "elasticsearch",
      "type":"sink",
      "config": {
        "sourceTable": "test",
        "node": "host",
        "port": "9200",
        "index": "estest",
        "options": {
          "es.nodes.wan.only":"true"
        },
        "saveMode": "append"
      }
    }
  ]
}
```

You need  jars to put under spark/jars directory
```text
elasticsearch-spark-30_2.12-7.17.7.jar
```



