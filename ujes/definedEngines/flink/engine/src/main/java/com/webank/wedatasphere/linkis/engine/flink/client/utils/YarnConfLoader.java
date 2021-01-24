
package com.webank.wedatasphere.linkis.engine.flink.client.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class YarnConfLoader {
	public static YarnConfiguration getYarnConf(String yarnConfDir) {
		YarnConfiguration yarnConf = new YarnConfiguration();
		try {
			File dir = new File(yarnConfDir);
			if (dir.exists() && dir.isDirectory()) {
				File[] xmlFileList = new File(yarnConfDir).listFiles((dir1, name) -> {
					if (name.endsWith(".xml")) {
						return true;
					}
					return false;
				});
				if (xmlFileList != null) {
					for (File xmlFile : xmlFileList) {
						yarnConf.addResource(xmlFile.toURI().toURL());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		haYarnConf(yarnConf);
		return yarnConf;
	}

	private static Configuration haYarnConf(Configuration yarnConf) {
		Iterator<Map.Entry<String, String>> iterator = yarnConf.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.startsWith("yarn.resourcemanager.hostname.")) {
				String rm = key.substring("yarn.resourcemanager.hostname.".length());
				String addressKey = "yarn.resourcemanager.address." + rm;
				if (yarnConf.get(addressKey) == null) {
					yarnConf.set(addressKey, value + ":" + YarnConfiguration.DEFAULT_RM_PORT);
				}
			}
		}
		return yarnConf;
	}
}
