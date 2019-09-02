/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadata.util;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
/**
 * Created by shanhuang on 9/13/18.
 */
public class HiveUtils {

    static Logger logger = Logger.getLogger(HiveUtils.class);

    public static HiveConf getDefaultConf(String userName) {
        HiveConf hiveConf = new HiveConf(getConfiguration(userName), HiveUtils.class);
        String hiveConfPath = DWSConfig.HIVE_CONF_DIR.getValue();
        if(StringUtils.isNotEmpty(hiveConfPath)) {
            logger.info("Load hive configuration from " + hiveConfPath);
            hiveConf.addResource(new Path(hiveConfPath + File.separator + "hive-site.xml"));
        } else {
            hiveConf.addResource("hive-site.xml");
        }
//        if(QueryConf.getBoolean("ide.keytab.enable", false)){
//            String path = new File(QueryConf.get("ide.keytab.file", "/appcom/keytab/") , userName + ".keytab").getPath();
//            String user = userName + "/" + QueryConf.get("ide.keytab.host", "bdphbs030003");
//            try {
//                //hiveConf.setVar(HiveConf.ConfVars.METASTORE_KERBEROS_PRINCIPAL, user);
//                //hiveConf.setVar(HiveConf.ConfVars.METASTORE_KERBEROS_KEYTAB_FILE, path);
//                UserGroupInformation.setConfiguration(hiveConf);
//                UserGroupInformation.loginUserFromKeytab(user, path);
//            } catch (Throwable e) {
//                LOG.error("Failed to login by: " + user + ":" + path, e);
//            }
//        }
        return hiveConf;
    }

    public static HiveMetaStoreClient getMetaStoreClient(HiveConf conf){
        try {
            return new HiveMetaStoreClient(conf);
        } catch (MetaException e) {
            logger.error(e);
        }
        return null;
    }

    private static Configuration getConfiguration(String user) {
        String hadoopConfDir = DWSConfig.HADOOP_CONF_DIR.getValue();
        File confPath = new File(hadoopConfDir);
        if(!confPath.exists() || confPath.isFile()) {
            throw new RuntimeException("Create hadoop configuration failed.");
        }
        Configuration conf = new Configuration();
        conf.addResource(new Path(Paths.get(hadoopConfDir, "core-site.xml").toAbsolutePath().toFile().getAbsolutePath()));
        conf.addResource(new Path(Paths.get(hadoopConfDir, "hdfs-site.xml").toAbsolutePath().toFile().getAbsolutePath()));
        conf.addResource(new Path(Paths.get(hadoopConfDir, "yarn-site.xml").toAbsolutePath().toFile().getAbsolutePath()));
        return conf;
    }
}
