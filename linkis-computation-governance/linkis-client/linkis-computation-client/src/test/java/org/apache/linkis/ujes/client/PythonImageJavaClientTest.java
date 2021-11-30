package org.apache.linkis.ujes.client;


import org.apache.commons.io.IOUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.ujes.client.request.JobSubmitAction;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.apache.linkis.ujes.client.response.JobInfoResult;
import org.apache.linkis.ujes.client.response.JobProgressResult;
import org.apache.linkis.ujes.client.response.image.ShowImage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PythonImageJavaClientTest {
    public static void main(String[] args) {

        String user = "hadoop";
        String gatewayIp = "127.0.0.1";
        String password = "hadoop";

        /**
         * python matplotlib 画图样例
         */
        String executeCode = "import numpy as np\n" +
                "import matplotlib.pyplot as plt\n" +
                "from matplotlib.font_manager import FontProperties\n" +
                "plt.figure(1)\n" +
                "plt.figure(2)\n" +
                "ax1 = plt.subplot(211)\n" +
                "ax2 = plt.subplot(212)\n" +
                "ax1.set_title(u\"helloworld\")\n" +
                "x = np.linspace(0, 3, 100)\n" +
                "for i in range(5):\n" +
                "    plt.figure(1)\n" +
                "    plt.plot(x, np.exp(i*x/3))\n" +
                "    plt.sca(ax1)\n" +
                "    plt.plot(x, np.sin(i*x))\n" +
                "    plt.sca(ax2)\n" +
                "    plt.plot(x, np.cos(i*x))\n" +
                "\n" +
                "show.show_matplotlib(plt)";

        /**
         * pyspark 代码样例
         */
        String pysparkExecuteCode = "from pyspark.sql import Row\n" +
                "from pyspark.sql import HiveContext\n" +
                "import pyspark\n" +
                "import matplotlib.pyplot as plt\n" +
                "\n" +
                "plt.rcParams[\"figure.figsize\"] = [7.50, 3.50]\n" +
                "plt.rcParams[\"figure.autolayout\"] = True\n" +
                "\n" +
                "sqlContext = HiveContext(sc)\n" +
                "\n" +
                "test_list = [(1, 'John'), (2, 'James'), (3, 'Jack'), (4, 'Joe')]\n" +
                "rdd = sc.parallelize(test_list)\n" +
                "people = rdd.map(lambda x: Row(id=int(x[0]), name=x[1]))\n" +
                "schemaPeople = sqlContext.createDataFrame(people)\n" +
                "sqlContext.registerDataFrameAsTable(schemaPeople, \"my_table\")\n" +
                "\n" +
                "df = sqlContext.sql(\"Select * from my_table\")\n" +
                "df = df.toPandas()\n" +
                "df.set_index('name').plot()\n" +
                "\n" +
                "show_matplotlib(plt)";



        // 1. 配置ClientBuilder，获取ClientConfig
        DWSClientConfig clientConfig = ((DWSClientConfigBuilder) (DWSClientConfigBuilder.newBuilder()
                .addServerUrl("http://" + gatewayIp + ":9001")  //指定ServerUrl，linkis服务器端网关的地址,如http://{ip}:{port}
                .connectionTimeout(30000)   //connectionTimeOut 客户端连接超时时间
                .discoveryEnabled(false).discoveryFrequency(1, TimeUnit.MINUTES)  //是否启用注册发现，如果启用，会自动发现新启动的Gateway
                .loadbalancerEnabled(true)  // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
                .maxConnectionSize(5)   //指定最大连接数，即最大并发数
                .retryEnabled(false).readTimeout(30000)   //执行失败，是否允许重试
                .setAuthenticationStrategy(new StaticAuthenticationStrategy())   //AuthenticationStrategy Linkis认证方式
                .setAuthTokenKey(user).setAuthTokenValue(password)))  //认证key，一般为用户名;  认证value，一般为用户名对应的密码
                .setDWSVersion("v1").build();  //linkis后台协议的版本，当前版本为v1

        // 2. 通过DWSClientConfig获取一个UJESClient
        UJESClient client = new UJESClientImpl(clientConfig);

        try {
            // 3. 开始执行代码
            System.out.println("user : " + user + ", code : [" + executeCode + "]");
            Map<String, Object> startupMap = new HashMap<String, Object>();
            // 在startupMap可以存放多种启动参数，参见linkis管理台配置
            startupMap.put("wds.linkis.yarnqueue", "q02");
            //指定Label
            Map<String, Object> labels = new HashMap<String, Object>();
            //添加本次执行所依赖的的标签:EngineTypeLabel/UserCreatorLabel/EngineRunTypeLabel
            labels.put(LabelKeyConstant.ENGINE_TYPE_KEY, "python-python2");
            labels.put(LabelKeyConstant.USER_CREATOR_TYPE_KEY, user + "-IDE");
            labels.put(LabelKeyConstant.CODE_TYPE_KEY, "python");
            //指定source
            Map<String, Object> source = new HashMap<String, Object>();
            source.put(TaskConstant.SCRIPTPATH, "LinkisClient-test");
            JobExecuteResult jobExecuteResult = client.submit(JobSubmitAction.builder()
                    .addExecuteCode(executeCode)
                    .setStartupParams(startupMap)
                    .setUser(user)//Job提交用户
                    .addExecuteUser(user)//实际执行用户
                    .setLabels(labels)
                    .setSource(source)
                    .build()
            );
            System.out.println("execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());

            // 4. 获取脚本的执行状态
            JobInfoResult jobInfoResult = client.getJobInfo(jobExecuteResult);
            int sleepTimeMills = 1000;
            while (!jobInfoResult.isCompleted()) {
                // 5. 获取脚本的执行进度
                JobProgressResult progress = client.progress(jobExecuteResult);
                Utils.sleepQuietly(sleepTimeMills);
                jobInfoResult = client.getJobInfo(jobExecuteResult);
            }

            // 6. 获取脚本的Job信息
            JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
            // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
            String resultSet = jobInfo.getResultSetList(client)[0];
            // 8. 通过一个结果集信息，获取具体的结果集
            Object fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser()).build()).getFileContent();
            System.out.println("fileContents: " + fileContents);

            // 将 fileContents 中的图片打印出来
            ShowImage.showImage(fileContents, 1000, 1000);

        } catch (Exception e) {
            e.printStackTrace();
            IOUtils.closeQuietly(client);
        }
        IOUtils.closeQuietly(client);
    }
}
