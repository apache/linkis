package org.apache.linkis.engineconnplugin.flink.client.config;


import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;

public class FlinkVersionThreadLocal {
    private static ThreadLocal<String> flinkVersion = new ThreadLocal<>();

    public static String getFlinkVersion() {
        String version = flinkVersion.get();
        if (StringUtils.isBlank(version)){
            version = FlinkEnvConfiguration.FLINK_1_16_2_VERSION();
            flinkVersion.set(version);
        }
        return version;
    }

    public static void setFlinkVersion(String version) {
        flinkVersion.set(version);
    }
}
