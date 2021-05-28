linkis-cli
============

[English]() | 中文

## 介绍

Linkis-cli 是一个用于向linkis提交任务的命令行程序，

## 安装方式
        //TODO

## 基础案例

您可以参照下面的例子简单地向Linkis提交任务

第一步，检查conf/目录下是否存在默认配置文件`linkis-cli.properties`，且包含以下配置：

        wds.linkis.client.common.gatewayUrl=http://127.0.0.1:9001
        wds.linkis.client.common.authStrategy=token
        wds.linkis.client.common.tokenKey=Validation-Code
        wds.linkis.client.common.tokenValue=BML-AUTH

第二步，进入linkis安装目录，输入指令：

        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop 

第三步，您会在控制台看到任务被提交到linkis并开始执行的信息。

linkis-cli目前仅支持同步提交，即向linkis提交任务后，不断询问任务状态、拉取任务日志，直至任务结束。任务结束时状态如果为成功，linkis-cli还会主动拉取结果集并输出。


## 使用方式
        ./bin/linkis-cli [参数] [cli参数]


## 支持的参数列表
* cli参数

    | 参数      | 说明                     | 数据类型 | 是否必选 |
    | ----------- | -------------------------- | -------- | ---- |
    | --gatewayUrl     | 手动指定linkis gateway地址 | String   | 否  |
    | --authStg   | 指定认证策略         | String   | 否  |
    | --authKey   | 指定认证key            | String   | 否  |
    | --authVal   | 指定认证value          | String   | 否  |
    | --userConf  | 指定配置文件位置   | String   | 否  |

* 参数

    | 参数      | 说明                     | 数据类型 | 是否必选 |
    | ----------- | -------------------------- | -------- | ---- |
    | -engType    | 引擎类型               | String   | 是  |
    | -runType    | 执行类型               | String   | 是  |
    | -code       | 执行代码               | String   | 否  |
    | -codePath   | 本地执行代码文件路径 | String   | 否  |
    | -smtUsr     | 指定提交用户         | String   | 否  |
    | -pxyUsr     | 指定执行用户         | String   | 否  |
    | -creator    | 指定creator       | String   | 否  |
    | -scriptPath | scriptPath               | String   | 否  |
    | -outPath    | 输出结果集到文件的路径 | String   | 否  |
    | -confMap    | configuration map                  | Map      | 否  |
    | -varMap     | 变量替换的variable map     | Map      | 否  |
    | -labelMap   | linkis labelMap        | Map      | 否  |
    | -sourceMap  | 指定linkis sourceMap      | Map      | 否  |


## 详细示例
* 一、添加cli参数

Cli参数可以通过手动指定的方式传入，此方式下会覆盖默认配置文件中的冲突配置项

        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop  --gatewayUrl http://127.0.0.1:9001  --authStg token --authKey [tokenKey] --authVal [tokenValue] 
        

* 二、添加引擎初始参数

引擎的初始参数可以通过`-confMap`参数添加，注意参数的数据类型是Map，命令行的输入格式如下：

        -confMap key1=val1 -confMap key2=val2 ...
        
例如：以下示例设置了引擎启动的yarn队列、spark executor个数等启动参数：

        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -confMap wds.linkis.yarnqueue=q02 -confMap spark.executor.instances=3 -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop  
        
当然，这些参数也支持以配置文件的方式读取，我们稍后会讲到

* 三、添加标签

标签可以通过`-labelMap`参数添加，与`-confMap`一样，`-labelMap`参数的类型也是Map:

        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -labelMap labelKey=labelVal -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop  

* 四、变量替换

Linkis-cli的变量替换通过`${}`符号和`-varMap`共同实现

        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -code "select count(*) from \${key};" -varMap key=testdb.test  -submitUser hadoop -proxyUser hadoop  

执行过程中sql语句会被替换为：

        select count(*) from testdb.test
        
注意`'\$'`中的转义符是为了防止参数被linux提前解析，如果是`-codePath`指定本地脚本方式，则不需要转义符

* 五、使用用户配置

1. linkis-cli支持加载用户自定义配置文件，配置文件路径通过`--userConf`参数指定，配置文件需要是`.properties`文件格式
        
        ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop  --userConf [配置文件路径]
        
        
2. 哪些参数可以配置？

所有参数都可以配置化，例如：

cli参数：

        wds.linkis.client.common.gatewayUrl=http://127.0.0.1:9001
        wds.linkis.client.common.authStrategy=static
        wds.linkis.client.common.tokenKey=[静态认证key]
        wds.linkis.client.common.tokenValue=[静态认证value]


参数：

        wds.linkis.client.label.engineType=spark-2.4.3
        wds.linkis.client.label.codeType=sql
        
Map类参数配置化时，key的格式为

        [Map前缀] + '.' + [key]

Map前缀包括：

        executionMap前缀: wds.linkis.client.exec
        sourceMap前缀: wds.linkis.client.source
        configurationMap前缀: wds.linkis.client.param.conf
        runtimeMap前缀: wds.linkis.client.param.runtime
        labelMap前缀: wds.linkis.client.label
        
注意： 

1. variableMap不支持配置化

2. 当配置的key和指令参数中已输入的key存在冲突时，优先级如下：

        指令参数 > 指令Map类型参数中的key > 用户配置 > 默认配置
        
示例：

配置引擎启动参数：

        wds.linkis.client.param.conf.spark.executor.instances=3
        wds.linkis.client.param.conf.wds.linkis.yarnqueue=q02
        
配置labelMap参数：

        wds.linkis.client.label.myLabel=label123
        
* 六、输出结果集到文件

使用`-outPath`参数指定一个输出目录，linkis-cli会将结果集输出到文件，每个结果集会自动创建一个文件，输出形式如下：

        [user]-task-[taskId]-result-[idx].txt
        
例如：

        shang-task-906-result-1.txt
        shang-task-906-result-2.txt
        shang-task-906-result-3.txt


    
