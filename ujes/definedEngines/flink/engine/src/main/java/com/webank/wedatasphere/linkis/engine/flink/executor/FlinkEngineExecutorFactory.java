package com.webank.wedatasphere.linkis.engine.flink.executor;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutor;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorFactory;
import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.ExecutorInitException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;
import com.webank.wedatasphere.linkis.engine.flink.util.ConfigurationParseUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.JarUtils;
import org.apache.logging.log4j.util.Strings;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 17:18
 */

@Component
public class FlinkEngineExecutorFactory implements EngineExecutorFactory {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private DefaultContext defaultContext;

    private SessionManager sessionManager;

    /**
     * 初始化flink上下文，引擎启动参数
     *
     * @param options
     * @return
     */
    @Override
    public EngineExecutor createExecutor(HashMap<String, String> options) {
        try {
            LOG.info("begin to create executor, the startup params:" + JSON.toString(options));
            Environment defaultEnv = Environment.parse(this.getClass().getClassLoader().getResource("flink-sql-defaults.yaml"));
            String hadoopConfDir = options.getOrDefault("wds.linkis.engine.flink.hadoop.conf.dir", FlinkConfiguration.HADOOP_CONF_DIR.getValue());
            String flinkConfDir = options.getOrDefault("wds.linkis.engine.flink.conf.dir", FlinkConfiguration.FLINK_CONF_DIR.getValue());
            String flinkHome = options.getOrDefault("wds.linkis.engine.flink.home", FlinkConfiguration.FLINK_HOME.getValue());
            String flinkLibRemotePath = options.getOrDefault("wds.linkis.engine.flink.lib.path", FlinkConfiguration.FLINK_LIB_REMOTE_PATH.getValue());
            String flinkDistJarPath = options.getOrDefault("wds.linkis.engine.flink.dist.jar.path", FlinkConfiguration.FLINK_DIST_JAR_PATH.getValue());
            String flinkLibLocalPath = options.getOrDefault("wds.linkis.engine.flink.local.opt.path", FlinkConfiguration.FLINK_OPT_LOCAL_PATH.getValue());
            String flinkOptLocalPath = options.getOrDefault("wds.linkis.engine.flink.local.lib.path", FlinkConfiguration.FLINK_LIB_LOCAL_PATH.getValue());
            String[] providedLibDirsArray = options.getOrDefault("wds.linkis.engine.flink.local.lib.path", FlinkConfiguration.FLINK_LIB_LOCAL_PATH.getValue()).split(";");
            String[] shipDirsArray = options.getOrDefault("wds.linkis.engine.flink.yarn.ship-directories", FlinkConfiguration.FLINK_SHIP_DIRECTORIES.getValue()).split(";");
            String systemConfigurationString = options.getOrDefault("wds.linkis.engine.flink.system.configuration", Strings.EMPTY);
            Configuration systemConfiguration = ConfigurationParseUtils.parseConfiguration(systemConfigurationString);
            defaultContext = new DefaultContext(defaultEnv, systemConfiguration, hadoopConfDir, flinkConfDir, flinkHome, flinkDistJarPath, flinkLibRemotePath, providedLibDirsArray, shipDirsArray, null);
            sessionManager = new SessionManager(defaultContext);
        } catch (Exception e) {
            LOG.error("Fail to init default context,", e);
        }
        return new FlinkEngineExecutor(5000, false, options, this.defaultContext, this.sessionManager);
    }
}
