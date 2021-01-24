package com.webank.wedatasphere.linkis.enginemanager.flink.process;

import com.webank.wedatasphere.linkis.enginemanager.EngineResource;
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration;
import com.webank.wedatasphere.linkis.enginemanager.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.enginemanager.process.JavaProcessEngineBuilder;
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine;
import org.apache.commons.lang.StringUtils;

/**
 * @program: linkis
 * @description: 启动引擎，解决多集群、多版本问题。
 * @author: hui zhu
 * @create: 2020-07-28 16:43
 */
public class FlinkEngineProcessBuilder extends JavaProcessEngineBuilder {

    @Override
    public void build(EngineResource engineRequest, RequestEngine request) {
        super.build(engineRequest, request);
        this.hadoopConf = StringUtils.isNotEmpty(request.properties().get("hadoop.config.dir"))?request.properties().get("hadoop.config.dir"): EnvConfiguration.HADOOP_CONF_DIR().getValue().toString();
        this.flinkConf = StringUtils.isNotEmpty(request.properties().get("flink.config.dir"))?request.properties().get("flink.config.dir"): EnvConfiguration.FLINK_CONF_DIR().getValue().toString();
        this.flinkHome = StringUtils.isNotEmpty(request.properties().get("flink.home"))?request.properties().get("flink.home"): EnvConfiguration.FLINK_HOME().getValue().toString();
    }

    /**
     *  多集群
     */
    private String hadoopConf;
    /**
     * 多版本
     */
    private String flinkConf;

    /**
     * 多版本
     */
    private String flinkHome;


    @Override
    public String getExtractJavaOpts() {
        return FlinkConfiguration.FLINK_CLIENT_OPTS.getValue(this.request().properties()).toString();
    }

    @Override
    public String getAlias(RequestEngine request) {
        return FlinkConfiguration.FLINK_ENGINE_SPRING_APPLICATION_NAME.getValue().toString();
    }

    @Override
    public String[] getExtractClasspath() {
        StringBuffer extractClasspath = new StringBuffer().append(hadoopConf).append(",").append(flinkConf).append(",").append(flinkHome);
        if (StringUtils.isNotBlank(FlinkConfiguration.FLINK_CLIENT_EXTRACLASSPATH.getValue().toString())){
            extractClasspath.append(",").append(FlinkConfiguration.FLINK_CLIENT_EXTRACLASSPATH.getValue().toString());
        }
        return extractClasspath.toString().split(",");
    }

    @Override
    public void classpathCheck(String[] jarOrFiles) {
      for(String jarOrFile:jarOrFiles){
          checkJarOrFile(jarOrFile);
      }
    }
    //todo Check the jar of the classpath(对classpath的jar进行检查)
    private void checkJarOrFile(String jarOrFile) {

    }

    @Override
    public boolean addApacheConfigPath() {
        return false;
    }

    public String getFlinkConf() {
        return flinkConf;
    }

    public void setFlinkConf(String flinkConf) {
        this.flinkConf = flinkConf;
    }

    public String getFlinkHome() {
        return flinkHome;
    }

    public void setFlinkHome(String flinkHome) {
        this.flinkHome = flinkHome;
    }

    public String getHadoopConf() {
        return hadoopConf;
    }

    public void setHadoopConf(String hadoopConf) {
        this.hadoopConf = hadoopConf;
    }

}
